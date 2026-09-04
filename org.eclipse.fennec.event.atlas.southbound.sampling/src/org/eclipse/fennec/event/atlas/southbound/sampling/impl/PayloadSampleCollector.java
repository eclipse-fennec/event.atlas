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
package org.eclipse.fennec.event.atlas.southbound.sampling.impl;

import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.eclipse.fennec.event.atlas.southbound.common.UnknownModelHandler;
import org.eclipse.fennec.event.atlas.southbound.common.UnknownPayload;
import org.eclipse.fennec.event.atlas.southbound.sampling.PayloadSampleSet;
import org.eclipse.fennec.event.atlas.southbound.sampling.PayloadSampleSet.CloseReason;
import org.eclipse.fennec.event.atlas.southbound.sampling.PayloadSampleSetHandler;
import org.eclipse.fennec.event.atlas.southbound.sampling.impl.SampleWindow.Admission;
import org.eclipse.fennec.event.atlas.southbound.sampling.impl.SampleWindow.Offer;
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
 * Buffers the payloads a runtime has no model for into per-channel sample sets, and hands each
 * closed set to a {@link PayloadSampleSetHandler} exactly once.
 * <p>
 * This is the {@link UnknownModelHandler} the ingress offers its unhandled payloads to, and it
 * exists because inferring a model from <em>one</em> payload cannot work - see
 * {@link PayloadSampleSet} for the three reasons. It answers
 * {@link #isCollecting(String)} while a window is open, which is what turns the ingress'
 * per-payload warnings into a single line per window: commissioning a new sensor should not
 * look like an outage.
 * <p>
 * Everything here is bounded by construction - a ring per window, a cap on concurrent windows,
 * a bounded hand-over queue - so a misconfigured sensor farm publishing at high frequency
 * costs samples rather than the runtime.
 * @author Ilenia Salvadori
 * @since 27.08.2026
 */
@Component(service = UnknownModelHandler.class, configurationPid = PayloadSampleCollector.PID, //
		configurationPolicy = ConfigurationPolicy.OPTIONAL)
public class PayloadSampleCollector implements UnknownModelHandler {

	/** Configuration pid of this component. */
	static final String PID = "event.atlas.southbound.sampling";

	/**
	 * Configuration of this component. The three close conditions can additionally be
	 * overridden per channel, see {@link #channels()}.
	 */
	@ObjectClassDefinition
	public @interface Config {

		/**
		 * Whether to collect at all. <b>Off by default</b>, so a runtime that merely deploys
		 * this bundle - a docker image shipping every feature, say - buffers nothing and pays
		 * nothing until an operator asks for it. The component still registers as an
		 * {@link UnknownModelHandler} when disabled and declines each payload, which keeps the
		 * switch a configuration change rather than a redeploy.
		 */
		boolean enabled() default false;

		/**
		 * How many distinctly shaped payloads to gather before handing the set over - N. Ten
		 * is enough to expose a type discriminator and to widen the types of the fields that
		 * vary, without waiting for a slow sensor forever.
		 */
		int targetSamples() default 10;

		/**
		 * How many consecutive payloads may add no new shape before the set is handed over
		 * early - K. This is what makes a fast, monotonous sensor cheap: a 1 Hz device
		 * repeating one shape is done in K readings instead of waiting for N distinct ones
		 * that will never come.
		 */
		int quietSamples() default 3;

		/**
		 * How long a window may stay open before it is handed over with whatever it has, and
		 * flagged as low evidence. The condition that keeps a 15-minute sensor from never
		 * producing a set at all.
		 */
		long maxWaitSeconds() default 1800;

		/**
		 * How many samples a window retains. 0 means as many as {@link #targetSamples()};
		 * anything smaller keeps the newest that many and evicts the rest, which bounds memory
		 * at the cost of evidence. The target still decides when the window closes.
		 */
		int ringSize() default 0;

		/**
		 * How many channels may be collected at the same time. A payload for a channel beyond
		 * the cap is dropped as it was before, so a sensor farm announcing hundreds of unknown
		 * models cannot grow this without limit.
		 */
		int maxWindows() default 100;

		/**
		 * Per-channel overrides of the close conditions, most specific first - each one a
		 * {@code <channel glob>;samples=N;quiet=K;maxWait=seconds;ring=N} string, see
		 * {@link ChannelSettingsResolver}. Cadence is a property of the sensor, not of the
		 * runtime.
		 */
		String[] channels() default {};
	}

	/**
	 * How many closed sets may wait for the {@link PayloadSampleSetHandler}. A set costs
	 * minutes of wall time to collect, so this is generous compared to the ingest hand-off -
	 * but still bounded, because a handler that has stopped taking them must not be able to
	 * accumulate them.
	 */
	private static final int HANDOVER_QUEUE_CAPACITY = 16;

	/**
	 * How often a payload is re-offered to a freshly opened window before it is given up on.
	 * Each retry means another payload closed the window in between, so under any real arrival
	 * rate one is enough; the bound only exists so that a pathological interleaving cannot spin.
	 */
	private static final int MAX_ADMISSION_ATTEMPTS = 8;

	private static final Logger logger = Logger.getLogger(PayloadSampleCollector.class.getName());

	private final Map<WindowKey, SampleWindow> windows = new ConcurrentHashMap<>();

	/** Sets dropped because the hand-over queue was full; for the throttled warning. */
	private final AtomicLong handoverDropped = new AtomicLong();

	/**
	 * Closes windows that reach their maximum wait. Separate from {@link #handover} on purpose:
	 * a slow handler must not be able to hold up the close of every other channel's window.
	 */
	private final ScheduledExecutorService timers = Executors
			.newSingleThreadScheduledExecutor(runnable -> thread(runnable, "event.atlas-sample-window-timer"));

	/**
	 * Hands closed sets over. One thread, so a handler sees the sets of a runtime one at a
	 * time, and bounded, so it cannot be handed more than it can hold.
	 */
	private final ExecutorService handover = new ThreadPoolExecutor(0, 1, 30L, TimeUnit.SECONDS,
			new ArrayBlockingQueue<>(HANDOVER_QUEUE_CAPACITY),
			runnable -> thread(runnable, "event.atlas-sample-set-handover"), (task, executor) -> rejectedHandover());

	/** Never null between activation and deactivation. */
	private volatile ChannelSettingsResolver settings = new ChannelSettingsResolver(
			ChannelSettings.of(10, 3, 1800, 0), new String[0]);

	private volatile int maxWindows = 100;

	/** Mirrors {@link Config#enabled()}; false until activation says otherwise. */
	private volatile boolean enabled;

	/**
	 * Optional so the collector can be deployed before anything consumes its sets - the
	 * activation log then reports what would have been handed over, which is a useful
	 * commissioning signal on its own; dynamic so deploying inference does not tear the
	 * collector down and lose every open window with it.
	 */
	@Reference(cardinality = ReferenceCardinality.OPTIONAL, policy = ReferencePolicy.DYNAMIC, //
			policyOption = ReferencePolicyOption.GREEDY)
	private volatile PayloadSampleSetHandler sampleSetHandler;

	@Activate
	@Modified
	void activate(Config config) {
		enabled = config.enabled();
		if (!enabled) {
			// Abandon anything already open: a set collected under the old setting is not
			// evidence anybody is going to consume now, and holding it would leak.
			windows.clear();
			logger.info("Payload sampling is disabled - unknown payloads are dropped as before");
			return;
		}
		ChannelSettings defaults = ChannelSettings.of(config.targetSamples(), config.quietSamples(),
				config.maxWaitSeconds(), config.ringSize());
		ChannelSettingsResolver resolved = new ChannelSettingsResolver(defaults, config.channels());
		settings = resolved;
		maxWindows = Math.max(1, config.maxWindows());
		// Windows already open keep the settings they opened with: re-deciding a close
		// condition halfway through a window would hand over a set that satisfies neither the
		// old nor the new configuration.
		logger.info(String.format(
				"Collecting payload samples for unknown models: %s, at most %s channel(s) at a time, "
						+ "%s per-channel override(s)",
				defaults, maxWindows, resolved.overrideCount()));
	}

	@Deactivate
	void deactivate() {
		timers.shutdownNow();
		handover.shutdownNow();
		// Open windows are abandoned rather than handed over: a partial set collected up to a
		// shutdown is not evidence anybody asked for, and the consumer is going away too.
		int abandoned = windows.size();
		windows.clear();
		if (abandoned > 0) {
			logger.info(String.format("Payload sampling stopped - %s open collection window(s) abandoned", abandoned));
		}
	}

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.fennec.event.atlas.southbound.common.UnknownModelHandler#onUnknownModel(org.eclipse.fennec.event.atlas.southbound.common.UnknownPayload)
	 */
	@Override
	public void onUnknownModel(UnknownPayload payload) {
		if (!enabled) {
			// Declining here rather than not registering at all: the reference in PayloadIngest
			// is dynamic, so flipping `enabled` takes effect on the next payload with no
			// service churn and no restart.
			return;
		}
		// The collector does not care WHY the payload was unhandled - it takes whatever the
		// hook gives it. The shape is what decides whether it is new evidence.
		List<String> shape = ShapeFingerprint.of(payload);
		WindowKey key = new WindowKey(payload.source(), payload.namespaceUri(), payload.format());
		// A window can be closed - by its timer, or by another thread's payload - between being
		// looked up and being offered this payload, in which case the payload belongs to the
		// next window rather than to the set that was just handed over. Retry until it lands
		// somewhere; every retry means some other payload closed a window, so this terminates.
		for (int attempt = 0; attempt < MAX_ADMISSION_ATTEMPTS; attempt++) {
			SampleWindow window = openWindow(key, payload.timestamp());
			if (window == null) {
				return;
			}
			Offer offer = window.offer(payload, shape);
			if (offer.admission() == Admission.WINDOW_CLOSED) {
				windows.remove(key, window);
				continue;
			}
			if (offer.closed() != null) {
				// Remove before handing over, so the next payload of this channel starts a new
				// window instead of being offered to a closed one.
				windows.remove(key, window);
				handOver(offer.closed());
			}
			return;
		}
		logger.warning(String.format(
				"Gave up admitting a payload from channel %s after %s attempts - it is dropped. Windows for that "
						+ "channel are closing as fast as its payloads arrive.",
				key, MAX_ADMISSION_ATTEMPTS));
	}

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.fennec.event.atlas.southbound.common.UnknownModelHandler#isCollecting(java.lang.String)
	 */
	@Override
	public boolean isCollecting(String source) {
		if (source == null) {
			return false;
		}
		// Any format, any nsURI: what the ingress asks is whether dropping this payload is
		// expected for that channel, and it is as soon as one of its windows is open.
		return windows.keySet().stream().anyMatch(key -> source.equals(key.source()));
	}

	/**
	 * Looks up the channel's window, opening one - with its maximum-wait timer - if there is
	 * none or the one there has been closed.
	 * <p>
	 * The whole decision happens inside a single {@link Map#compute(Object, java.util.function.BiFunction)}
	 * because two payloads of a new channel arriving at once must not each create a window: the
	 * loser's window would be visible to nobody, and the samples put into it would only ever
	 * surface when its maximum wait expired.
	 * @return the window to offer the payload to, or <code>null</code> if the concurrent-window
	 * cap is reached
	 */
	private SampleWindow openWindow(WindowKey key, Instant now) {
		// Read outside the mapping function: it must not touch the map it is computing on.
		// Only the growth path is capped, and only for a channel that has no window yet - an
		// open window must keep accepting payloads however many other channels there are.
		boolean capped = windows.size() >= maxWindows;
		SampleWindow[] opened = new SampleWindow[1];
		SampleWindow window = windows.compute(key, (windowKey, existing) -> {
			if (existing != null && !existing.isClosed()) {
				return existing;
			}
			if (existing == null && capped) {
				// returning null leaves the key unmapped
				return null;
			}
			opened[0] = new SampleWindow(windowKey, settings.settingsFor(windowKey.source()), now);
			return opened[0];
		});
		if (window == null) {
			logger.warning(String.format(
					"Not collecting samples for channel %s - already collecting %s channel(s), the configured "
							+ "maximum. The payload is dropped as it was before.",
					key, maxWindows));
			return null;
		}
		if (opened[0] != null) {
			logger.info(String.format("Collecting payload samples for channel %s: %s", key, window.settings()));
			scheduleMaxWait(key, window);
		}
		return window;
	}

	private void scheduleMaxWait(WindowKey key, SampleWindow window) {
		try {
			window.maxWaitTimer(timers.schedule(() -> closeOnMaxWait(key, window),
					window.settings().maxWait().toMillis(), TimeUnit.MILLISECONDS));
		} catch (RuntimeException e) {
			// the timer pool is shutting down; the window simply has no maximum wait left
			logger.log(Level.FINE, "Could not schedule the maximum wait for " + key, e);
		}
	}

	private void closeOnMaxWait(WindowKey key, SampleWindow window) {
		PayloadSampleSet closed = window.closeIfOpen(CloseReason.MAX_WAIT, Instant.now());
		// The window is closed either way now, so its key must go in both cases - otherwise a
		// channel would keep a closed window with no timer left to close it.
		windows.remove(key, window);
		if (closed != null) {
			handOver(closed);
		}
	}

	/**
	 * Logs the closed window and passes it to the handler, off the calling thread.
	 */
	private void handOver(PayloadSampleSet sampleSet) {
		logger.info(String.format(
				"Sample set ready for channel '%s' (%s%s): %s distinct shape(s) from %s payload(s) in %ss, closed "
						+ "because %s%s",
				sampleSet.source(), sampleSet.format(),
				sampleSet.namespaceUri() == null ? "" : ", " + sampleSet.namespaceUri(), sampleSet.sampleCount(),
				sampleSet.payloadsSeen(), sampleSet.duration().toSeconds(), sampleSet.closeReason(),
				sampleSet.lowEvidence() ? " - LOW EVIDENCE" : ""));
		PayloadSampleSetHandler handler = sampleSetHandler;
		if (handler == null) {
			logger.info(String.format(
					"No sample set handler is deployed - the set for channel '%s' is discarded. Deploy model "
							+ "inference to act on it.",
					sampleSet.source()));
			return;
		}
		handover.execute(() -> notifyHandler(handler, sampleSet));
	}

	/**
	 * A handler is third-party code doing network I/O: anything it throws stays here, because
	 * the thread it would kill is the one every later set needs.
	 */
	private static void notifyHandler(PayloadSampleSetHandler handler, PayloadSampleSet sampleSet) {
		try {
			handler.onSampleSet(sampleSet);
		} catch (Throwable e) {
			logger.log(Level.WARNING,
					String.format("The sample set handler failed on %s - the set is dropped", sampleSet), e);
		}
	}

	/**
	 * The hand-over queue is full or shutting down. Warned about on the first occurrence and
	 * every sixteenth after it: a set represents minutes of collection, so losing one is worth
	 * saying, but a handler that has stopped consuming must not turn that into a log flood.
	 */
	private void rejectedHandover() {
		long dropped = handoverDropped.incrementAndGet();
		if (dropped == 1 || dropped % HANDOVER_QUEUE_CAPACITY == 0) {
			logger.warning(String.format(
					"The sample set handler is not keeping up or is shutting down - %s collected set(s) dropped",
					dropped));
		}
	}

	private static Thread thread(Runnable task, String name) {
		Thread thread = new Thread(task, name);
		// neither collecting nor handing over is a reason to keep the JVM alive
		thread.setDaemon(true);
		return thread;
	}

}
