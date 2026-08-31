/**
 * Copyright (c) 2012 - 2026 Data In Motion and others.
 * All rights reserved.
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     Data In Motion - initial API and implementation
 */
package org.eclipse.fennec.event.atlas.model.inference.impl;

import java.time.Duration;
import java.time.Instant;
import java.util.Set;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.eclipse.fennec.event.atlas.model.inference.ChatCompletion;
import org.eclipse.fennec.event.atlas.model.inference.InferenceOutcome;
import org.eclipse.fennec.event.atlas.model.inference.InferenceOutcome.Status;
import org.eclipse.fennec.event.atlas.southbound.sampling.PayloadSampleSet;
import org.eclipse.fennec.event.atlas.southbound.sampling.PayloadSampleSetHandler;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.ConfigurationPolicy;
import org.osgi.service.component.annotations.Deactivate;
import org.osgi.service.component.annotations.Modified;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.component.annotations.ReferenceCardinality;
import org.osgi.service.component.annotations.ReferencePolicy;
import org.osgi.service.component.annotations.ReferencePolicyOption;
import org.osgi.service.metatype.annotations.ObjectClassDefinition;

/**
 * Turns a collected payload sample set into a reviewed model draft: decides whether the set is
 * worth spending a model call on, spends one, and reports what came back.
 * <p>
 * <b>Deploying this bundle is what turns inference on.</b> It is a
 * {@link PayloadSampleSetHandler}, which is the seam the payload sample collector hands its
 * closed windows to. (The issue behind this component says it implements
 * {@code UnknownModelHandler}; it cannot - that hook takes a single payload and is already held
 * by the collector, and a model cannot be inferred from one payload. The collector is what the
 * ingress sees, and this is what the collector feeds.)
 * <p>
 * <b>It never handles a metamodel document.</b> The agent behind {@link ChatCompletion}
 * discovers, authors, validates and publishes the package through its own tools; what comes back
 * here is an {@link InferenceOutcome}. Nothing in this bundle can register an inferred package
 * into the running system even if it wanted to - it has no EMF dependency at all, which is the
 * point: the loop is draft, human review, promotion to a released stage, and only then does the
 * runtime resolve the model on its next payload.
 * <p>
 * <b>Everything about a run is guarded.</b> A run is around a hundred agent turns, two and a
 * half minutes and real money, so a set is fingerprinted and skipped if that shape has been
 * inferred before, a hard cap limits runs per interval, the call happens on this component's own
 * thread with a timeout well beyond the measured duration, and an unreachable completion is not
 * retried in a storm.
 * @author Ilenia Salvadori
 * @since 27.08.2026
 */
@Component(service = PayloadSampleSetHandler.class, configurationPid = ModelInferenceService.PID, //
		configurationPolicy = ConfigurationPolicy.OPTIONAL)
public class ModelInferenceService implements PayloadSampleSetHandler {

	/** Configuration pid of this component. */
	static final String PID = "event.atlas.model.inference";

	/**
	 * Configuration of this component.
	 */
	@ObjectClassDefinition
	public @interface Config {

		/**
		 * The namespace <em>prefix</em> a draft must be published beneath - the one thing the
		 * agent cannot work out for itself. Blank switches inference off: a run that does not
		 * know where it may publish would either fail at the end or, worse, publish somewhere
		 * else.
		 * <p>
		 * A prefix, not the nsURI: the agent extends it with a segment identifying the model it
		 * authored, so a second device family does not land on the first one's namespace. The
		 * nsURI it settles on comes back in the outcome. Keep whatever allow-list guards
		 * publication prefix-shaped ({@code …/inferred*}) so the sub-namespaces pass.
		 */
		String namespace() default "";

		/**
		 * How many inference runs {@link #intervalSeconds()} allows. A misconfigured sensor can
		 * emit unknown payloads at high frequency; at the cost of a run that matters.
		 */
		int maxRunsPerInterval() default 5;

		/** The sliding window the run cap applies to. */
		long intervalSeconds() default 3600;

		/**
		 * How long to wait for a completion before recording it as unavailable. A measured run
		 * is around 150 seconds, and this must sit well beyond that - the agent is discovering,
		 * authoring and validating, not answering a request.
		 */
		long timeoutSeconds() default 600;

		/**
		 * How long an unreachable completion blocks a second attempt at the same payloads. Long
		 * enough not to be a retry storm, finite so that an outage does not permanently deny a
		 * model to a sensor that never got its chance.
		 */
		long retryAfterUnavailableSeconds() default 3600;

		/**
		 * How much of each payload body reaches the prompt. Cost is dominated by the agent's
		 * turn count rather than by the samples, so this is generous; it exists so that one
		 * pathological payload cannot fill a context window.
		 */
		int maxPayloadChars() default 4096;
	}

	/**
	 * How many sample sets may wait for a run. Small on purpose: with a cap of a few runs per
	 * hour, a queue any longer would only hold sets whose turn will never come before the rate
	 * limit refuses them anyway.
	 */
	private static final int RUN_QUEUE_CAPACITY = 4;

	private static final Logger logger = Logger.getLogger(ModelInferenceService.class.getName());

	/**
	 * Runs the inferences, one at a time. Serialized deliberately: two agents authoring into the
	 * same namespace at once is how an already-exists outcome is manufactured.
	 */
	private final ExecutorService runs = new ThreadPoolExecutor(0, 1, 60L, TimeUnit.SECONDS,
			new ArrayBlockingQueue<>(RUN_QUEUE_CAPACITY), runnable -> thread(runnable, "event.atlas-model-inference"),
			(task, executor) -> rejectedRun(task));

	/**
	 * The channels with a run queued or in progress, so a second set for the same channel is
	 * dropped rather than queued behind the first.
	 * <p>
	 * Serializing the runs prevents two agents authoring at once but not one authoring straight
	 * after another: a later window on the same channel carries a <em>different</em> set of
	 * shapes as soon as one payload has a field the earlier window never saw, so neither the
	 * fingerprint claim (different shapes, different fingerprint) nor the rate limiter (which
	 * only counts) stops it. The second run is not better informed for having waited - a window
	 * can just as easily miss a shape the first one had - so the first one is left to finish and
	 * the rest are dropped.
	 * <p>
	 * The key is {@link PayloadSampleSet#source()}, which is the MQTT topic
	 * ({@code lorawan/unknown/dragino}) and not the configured channel name - measured, and easy
	 * to get wrong.
	 */
	private final Set<String> inFlight = ConcurrentHashMap.newKeySet();

	/**
	 * Carries the blocking completion call, so that the runner can stop waiting for it. A pool
	 * rather than a single thread because a call that ignores its interrupt would otherwise
	 * block every later run behind it forever.
	 */
	private final ExecutorService calls = Executors
			.newCachedThreadPool(runnable -> thread(runnable, "event.atlas-model-inference-call"));

	private volatile Settings settings = Settings.of(new ConfigDefaults());
	private volatile AttemptRegistry attempts = new AttemptRegistry(Duration.ofSeconds(3600));
	private volatile RunRateLimiter rateLimiter = new RunRateLimiter(5, Duration.ofSeconds(3600));

	/**
	 * Optional and dynamic: this bundle is deployable before an AI stack is, and says so instead
	 * of failing to activate. Until a completion is registered, a closed sample set is reported
	 * and dropped.
	 */
	@Reference(cardinality = ReferenceCardinality.OPTIONAL, policy = ReferencePolicy.DYNAMIC, //
			policyOption = ReferencePolicyOption.GREEDY)
	private volatile ChatCompletion chatCompletion;

	@Activate
	@Modified
	void activate(Config config) {
		Settings resolved = Settings.of(config);
		settings = resolved;
		// Both are rebuilt on a configuration change, which resets the run window and the
		// claims: an operator who changes the cap or the namespace is changing the terms under
		// which those decisions were made.
		rateLimiter = new RunRateLimiter(resolved.maxRunsPerInterval(), resolved.interval());
		attempts = new AttemptRegistry(resolved.retryAfterUnavailable());
		if (resolved.namespace().isBlank()) {
			logger.warning(String.format(
					"Model inference is deployed but has no namespace configured - no inference will be run. "
							+ "Set 'namespace' on the %s configuration.",
					PID));
		} else {
			logger.info(String.format(
					"Model inference is on: publishing drafts beneath '%s', %s, timeout %ss",
					resolved.namespace(), rateLimiter, resolved.timeout().toSeconds()));
		}
	}

	@Deactivate
	void deactivate() {
		// A run in flight is abandoned rather than waited for: it is minutes long, and whatever
		// the agent has already published stands on its own - the outcome is only a report.
		runs.shutdownNow();
		calls.shutdownNow();
	}

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.fennec.event.atlas.southbound.sampling.PayloadSampleSetHandler#onSampleSet(org.eclipse.fennec.event.atlas.southbound.sampling.PayloadSampleSet)
	 */
	@Override
	public void onSampleSet(PayloadSampleSet sampleSet) {
		Settings current = settings;
		if (current.namespace().isBlank()) {
			logger.fine(() -> "No namespace configured - not inferring a model for " + sampleSet.source());
			return;
		}
		// Claimed first, because it is the only guard that touches no other state: a set dropped
		// here must not have spent a fingerprint claim or a rate-limiter token on the way.
		if (!inFlight.add(sampleSet.source())) {
			logger.info(String.format(
					"Channel '%s' is already being inferred - dropping this sample set rather than queueing it "
							+ "behind the run in progress.",
					sampleSet.source()));
			return;
		}
		String fingerprint = SampleSetFingerprint.of(sampleSet);
		Instant now = Instant.now();
		// Claimed before the run starts, not after: a run takes minutes, and the sets arriving
		// meanwhile carry the same shapes again because nothing has been promoted yet.
		if (!attempts.claim(fingerprint, now)) {
			inFlight.remove(sampleSet.source());
			logger.info(String.format(
					"Channel '%s' has handed over payloads that were already inferred (%s) - not running again. "
							+ "The draft is waiting for review, or was declined.",
					sampleSet.source(), shortFingerprint(fingerprint)));
			return;
		}
		if (!rateLimiter.tryRun(now)) {
			attempts.release(fingerprint);
			inFlight.remove(sampleSet.source());
			logger.warning(String.format(
					"Refusing to infer a model for channel '%s' - the run cap is reached (%s). "
							+ "The next run is allowed at %s.",
					sampleSet.source(), rateLimiter, rateLimiter.nextAllowed(now)));
			return;
		}
		try {
			runs.execute(new QueuedRun(sampleSet, fingerprint, current));
		} catch (RuntimeException e) {
			// only reachable while shutting down; the rejection handler takes the queue-full case
			inFlight.remove(sampleSet.source());
			giveBack(fingerprint);
			logger.log(Level.FINE, "Model inference is shutting down - not running one for " + sampleSet.source(), e);
		}
	}

	/**
	 * A run that has been queued. A named type rather than a lambda so that the rejection
	 * handler, which is handed the task and nothing else, can undo the guards a dropped run had
	 * already claimed.
	 */
	private final class QueuedRun implements Runnable {

		private final PayloadSampleSet sampleSet;
		private final String fingerprint;
		private final Settings settings;

		QueuedRun(PayloadSampleSet sampleSet, String fingerprint, Settings settings) {
			this.sampleSet = sampleSet;
			this.fingerprint = fingerprint;
			this.settings = settings;
		}

		@Override
		public void run() {
			try {
				ModelInferenceService.this.run(sampleSet, fingerprint, settings);
			} finally {
				inFlight.remove(sampleSet.source());
			}
		}

		/** This run will never happen: release what it was holding. */
		void dropped() {
			inFlight.remove(sampleSet.source());
			giveBack(fingerprint);
		}
	}

	/**
	 * One inference run, on the runner thread. Records the outcome whatever happens: an
	 * outcome that is never recorded is an outcome that gets retried.
	 */
	private void run(PayloadSampleSet sampleSet, String fingerprint, Settings current) {
		Instant started = Instant.now();
		logger.info(String.format(
				"Inferring a model for channel '%s': %s sample(s)%s, fingerprint %s. This takes minutes.",
				sampleSet.source(), sampleSet.sampleCount(), sampleSet.lowEvidence() ? " (low evidence)" : "",
				shortFingerprint(fingerprint)));
		InferenceOutcome outcome = complete(sampleSet, current);
		attempts.completed(fingerprint, outcome.status(), Instant.now());
		report(sampleSet, outcome, Duration.between(started, Instant.now()), current.namespace());
	}

	/**
	 * Runs the completion with a timeout. The call itself is handed to another thread so that
	 * this one can stop waiting for it; a completion that outlives its timeout is left to finish
	 * on its own - whatever it publishes is reviewed by a human either way, and interrupting an
	 * agent halfway through publishing is worse than not hearing about it.
	 */
	private InferenceOutcome complete(PayloadSampleSet sampleSet, Settings current) {
		ChatCompletion completion = chatCompletion;
		if (completion == null) {
			return InferenceOutcome.of(Status.UNAVAILABLE, "no chat completion is deployed");
		}
		String systemMessage = InferencePrompt.systemMessage();
		String userMessage = InferencePrompt.userMessage(sampleSet, current.namespace(), current.maxPayloadChars());
		Future<InferenceOutcome> call = calls.submit(() -> completion.complete(systemMessage, userMessage));
		try {
			return call.get(current.timeout().toSeconds(), TimeUnit.SECONDS);
		} catch (TimeoutException e) {
			call.cancel(true);
			return InferenceOutcome.of(Status.UNAVAILABLE,
					"the completion did not answer within " + current.timeout().toSeconds() + "s");
		} catch (InterruptedException e) {
			Thread.currentThread().interrupt();
			call.cancel(true);
			return InferenceOutcome.of(Status.UNAVAILABLE, "the run was interrupted");
		} catch (Exception e) {
			// anything the completion threw arrives here wrapped in an ExecutionException
			Throwable cause = e.getCause() == null ? e : e.getCause();
			logger.log(Level.FINE, "The chat completion failed for " + sampleSet.source(), cause);
			return InferenceOutcome.of(Status.UNAVAILABLE, describe(cause));
		}
	}

	/**
	 * The agent chooses the nsURI beneath the configured prefix, so this is the only place that
	 * can notice it did not.
	 * <p>
	 * A warning rather than a failure, for two reasons: the draft is already published by the
	 * time the receipt is read, so there is nothing left to prevent; and publication is guarded
	 * on the server by a prefix allow-list, which means a draft that landed outside this prefix
	 * says the allow-list is wider than the prefix, or that the agent named an nsURI it did not
	 * publish to. Either is an operator's problem, not this run's.
	 */
	private static void warnIfOutsideThePrefix(InferenceOutcome outcome, String namespacePrefix) {
		String nsUri = outcome.nsUri();
		if (nsUri == null || nsUri.startsWith(namespacePrefix)) {
			return;
		}
		logger.warning(String.format(
				"The agent reported the namespace '%s', which is not beneath the configured '%s'. Either the "
						+ "publication allow-list is wider than that prefix, or the draft is not where this says "
						+ "it is - check before promoting it.",
				nsUri, namespacePrefix));
	}

	/**
	 * Logs the outcome against the channel. Only two outcomes are anybody's problem: a
	 * conflict is what a channel that keeps handing over sets while its draft waits for review
	 * looks like, and a rejection is the agent's judgement about the payloads.
	 */
	private static void report(PayloadSampleSet sampleSet, InferenceOutcome outcome, Duration took,
			String namespacePrefix) {
		warnIfOutsideThePrefix(outcome, namespacePrefix);
		String evidence = sampleSet.lowEvidence()
				? String.format(" Evidence was thin: %s sample(s), the window closed on its maximum wait.",
						sampleSet.sampleCount())
				: "";
		String context = String.format("channel '%s' (%s sample(s), %ss)", sampleSet.source(),
				sampleSet.sampleCount(), took.toSeconds());
		String said = outcome.message() == null ? "no further detail" : outcome.message();
		switch (outcome.status()) {
		case PUBLISHED -> logger.info(String.format(
				"A model draft was published for %s under '%s': %s. It has to be reviewed and promoted before this "
						+ "runtime resolves it.%s",
				context, outcome.nsUri(), said, evidence));
		case ALREADY_EXISTS -> logger.info(String.format(
				"A draft already existed for %s under '%s': %s. Nothing to do - it is still waiting for review.%s",
				context, outcome.nsUri(), said, evidence));
		case NOT_PUBLISHED -> logger.warning(String.format(
				"A model was authored for %s but could not be published: %s. The payloads decided nothing here, so "
						+ "the next sample set for them will try again.%s",
				context, said, evidence));
		case NOT_INFERRED -> logger.info(String.format("No model was authored for %s: %s%s", context, said,
				evidence));
		case UNAVAILABLE -> logger.warning(String.format(
				"Could not infer a model for %s: %s. It will not be attempted again for a while.", context, said));
		case UNREADABLE -> logger.warning(String.format(
				"An inference ran for %s but its answer could not be read as an outcome, so what it did is "
						+ "unknown: %s",
				context, said));
		}
	}

	/**
	 * The queue of pending runs is full. Dropping the set is right: with a handful of runs
	 * allowed per interval, a set that cannot even be queued would be refused by the rate
	 * limiter long before its turn came. What it must not do is drop the set while keeping its
	 * claims - a fingerprint held by a run that never happened is never inferred again.
	 */
	private void rejectedRun(Runnable task) {
		logger.warning(String.format(
				"Not inferring a model - %s inference(s) are already queued or running. The sample set is dropped.",
				RUN_QUEUE_CAPACITY));
		if (task instanceof QueuedRun queued) {
			queued.dropped();
		}
	}

	/** Undoes both guards for a run that never happened. */
	private void giveBack(String fingerprint) {
		attempts.release(fingerprint);
		rateLimiter.giveBack();
	}

	private static String describe(Throwable e) {
		return e.getMessage() == null ? e.getClass().getSimpleName() : e.getMessage();
	}

	/** Fingerprints are 64 hex characters; a log line only needs enough to correlate. */
	private static String shortFingerprint(String fingerprint) {
		return fingerprint.substring(0, Math.min(12, fingerprint.length()));
	}

	private static Thread thread(Runnable task, String name) {
		Thread thread = new Thread(task, name);
		// an agent mid-run is not a reason to keep the JVM alive
		thread.setDaemon(true);
		return thread;
	}

	/**
	 * The configuration as validated values, taken once per sample set so that a change halfway
	 * through a run cannot move the namespace or the timeout under it.
	 * @param namespace the namespace a draft may be published under
	 * @param maxRunsPerInterval the run cap
	 * @param interval the window the cap applies to
	 * @param timeout how long to wait for a completion
	 * @param retryAfterUnavailable how long an unreachable completion blocks a retry
	 * @param maxPayloadChars how much of a payload body reaches the prompt
	 */
	private record Settings(String namespace, int maxRunsPerInterval, Duration interval, Duration timeout,
			Duration retryAfterUnavailable, int maxPayloadChars) {

		static Settings of(Config config) {
			return new Settings(config.namespace() == null ? "" : config.namespace().strip(),
					Math.max(0, config.maxRunsPerInterval()), atLeastASecond(config.intervalSeconds()),
					atLeastASecond(config.timeoutSeconds()), atLeastASecond(config.retryAfterUnavailableSeconds()),
					Math.max(0, config.maxPayloadChars()));
		}

		private static Duration atLeastASecond(long seconds) {
			return Duration.ofSeconds(Math.max(1, seconds));
		}
	}

	/**
	 * The annotation defaults, for the field initializers - a component may be handed a sample
	 * set only after {@code activate}, but the guards must never be null.
	 */
	private static class ConfigDefaults implements Config {

		@Override
		public Class<? extends java.lang.annotation.Annotation> annotationType() {
			return Config.class;
		}

		@Override
		public String namespace() {
			return "";
		}

		@Override
		public int maxRunsPerInterval() {
			return 5;
		}

		@Override
		public long intervalSeconds() {
			return 3600;
		}

		@Override
		public long timeoutSeconds() {
			return 600;
		}

		@Override
		public long retryAfterUnavailableSeconds() {
			return 3600;
		}

		@Override
		public int maxPayloadChars() {
			return 4096;
		}
	}

}
