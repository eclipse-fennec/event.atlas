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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNotSame;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.LogRecord;
import java.util.logging.Logger;

import org.eclipse.fennec.event.atlas.model.inference.ChatCompletion;
import org.eclipse.fennec.event.atlas.southbound.common.IngestResult.Outcome;
import org.eclipse.fennec.event.atlas.southbound.common.PayloadIngest;
import org.eclipse.fennec.event.atlas.southbound.sampling.PayloadSample;
import org.eclipse.fennec.event.atlas.southbound.sampling.PayloadSampleSet;
import org.eclipse.fennec.event.atlas.southbound.sampling.PayloadSampleSet.CloseReason;
import org.eclipse.fennec.event.atlas.southbound.sampling.PayloadSampleSetHandler;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/**
 * Unit tests for {@link ModelInferenceService}: the guards in front of a model call, and what it
 * does with the receipt that comes back.
 * <p>
 * The run is asynchronous, so every assertion about a completion goes through
 * {@link RecordingCompletion#awaitCall()}.
 * @author Ilenia Salvadori
 */
public class ModelInferenceServiceTest {

	private static final String NAMESPACE = "https://example.org/inferred";

	private final List<ModelInferenceService> services = new ArrayList<>();
	private final List<Handler> captures = new ArrayList<>();

	@AfterEach
	void tearDown() {
		// stop the runs first: a lingering one would otherwise log into the next test's capture
		services.forEach(ModelInferenceService::deactivate);
		captures.forEach(Logger.getLogger(ModelInferenceService.class.getName())::removeHandler);
	}

	@Test
	@DisplayName("It is the sample set handler, which is what deploying the bundle turns on")
	// The issue asks for an UnknownModelHandler; that hook takes a single payload, is held by
	// the collector, and a model cannot be inferred from one payload. This is the seam the
	// collector hands its closed windows to.
	void isThePayloadSampleSetHandler() {
		assertTrue(PayloadSampleSetHandler.class.isAssignableFrom(ModelInferenceService.class));
	}

	@Test
	@DisplayName("A sample set is inferred once, with the samples and the namespace in the prompt")
	void sampleSet_runsOneCompletion() throws Exception {
		RecordingCompletion completion = new RecordingCompletion("RECEIPT: created " + NAMESPACE + "/dragino/1.0");
		ModelInferenceService service = service(completion, NAMESPACE, "dragino", 5);

		service.onSampleSet(set("sensors/dragino/1", false, sample("{\"temp\":21.5}")));

		Call call = completion.awaitCall();
		assertTrue(call.systemMessage().contains("'dragino'"), "The runtime's codec type map must reach the prompt");
		assertTrue(call.userMessage().contains(NAMESPACE));
		assertTrue(call.userMessage().contains("{\"temp\":21.5}"));
		assertEquals(1, completion.calls());
		service.deactivate();
	}

	@Test
	@DisplayName("The same payload shapes are not inferred twice")
	// A channel whose draft is waiting for review keeps handing over sample sets; each one would
	// otherwise cost another agent run.
	void repeatedShapes_doNotRunASecondTime() throws Exception {
		RecordingCompletion completion = new RecordingCompletion("RECEIPT: created " + NAMESPACE + "/a/1.0");
		ModelInferenceService service = service(completion, NAMESPACE, "", 5);

		service.onSampleSet(set("sensors/a", false, sample("{\"temp\":21.5}")));
		completion.awaitCall();
		// a second window of the same shapes, from a different channel and with other values
		service.onSampleSet(set("sensors/b", false, sample("{\"temp\":19.0}")));
		service.onSampleSet(set("sensors/a", false, sample("{\"temp\":22.5}")));

		assertNull(completion.pollCall(), "Nothing new was learned, so nothing new is spent");
		assertEquals(1, completion.calls());
	}

	@Test
	@DisplayName("A genuinely new shape is inferred again")
	void newShapes_runAgain() throws Exception {
		RecordingCompletion completion = new RecordingCompletion("RECEIPT: created " + NAMESPACE + "/a/1.0");
		ModelInferenceService service = service(completion, NAMESPACE, "", 5);

		service.onSampleSet(set("sensors/a", false, sample("{\"temp\":21.5}")));
		completion.awaitCall();
		service.onSampleSet(set("sensors/a", false, sampleWithShape("{\"temp\":21.5,\"rssi\":-70}", "rssi:int")));

		assertNotNull(completion.awaitCall());
		assertEquals(2, completion.calls());
	}

	@Test
	@DisplayName("The run cap refuses further inferences and says so")
	void rateLimit_refusesBeyondTheCapAndLogsIt() throws Exception {
		RecordingCompletion completion = new RecordingCompletion("RECEIPT: created " + NAMESPACE + "/a/1.0");
		ModelInferenceService service = service(completion, NAMESPACE, "", 1);
		List<LogRecord> logged = captureLog();

		service.onSampleSet(set("sensors/a", false, sample("{\"a\":1}")));
		completion.awaitCall();
		service.onSampleSet(set("sensors/b", false, sampleWithShape("{\"b\":1}", "b:int")));

		assertNull(completion.pollCall(), "The second set is over the cap");
		assertEquals(1, completion.calls());
		assertTrue(logged.stream().anyMatch(record -> record.getLevel() == Level.WARNING
				&& record.getMessage().contains("the run cap is reached")),
				"A refusal that is not in the log is a silent loss of a model");
	}

	@Test
	@DisplayName("A slow completion does not block the caller")
	// A run is around two and a half minutes; the collector's hand-over thread cannot wait for it.
	void slowCompletion_doesNotBlockTheCollector() throws Exception {
		CountDownLatch entered = new CountDownLatch(1);
		CountDownLatch release = new CountDownLatch(1);
		ModelInferenceService service = service((systemMessage, userMessage) -> {
			entered.countDown();
			try {
				release.await(5, TimeUnit.SECONDS);
			} catch (InterruptedException e) {
				Thread.currentThread().interrupt();
			}
			return "RECEIPT: created " + NAMESPACE + "/a/1.0";
		}, NAMESPACE, "", 5);

		try {
			service.onSampleSet(set("sensors/a", false, sample("{\"a\":1}")));

			assertTrue(entered.await(5, TimeUnit.SECONDS), "The completion should have been called");
			assertEquals(1, release.getCount(), "onSampleSet returned while the agent was still working");
		} finally {
			release.countDown();
		}
	}

	@Test
	@DisplayName("The provider request runs on this component's own daemon thread")
	// The request to the provider is synchronous and can take minutes, so which thread it occupies
	// is the whole safety property: never a broker callback, never an HTTP request thread, never
	// the ingest hand-off or the collector's hand-over thread. Pinned here so a later refactor
	// cannot quietly move it back onto the caller.
	void completion_runsOnItsOwnThread() throws Exception {
		BlockingQueue<Thread> ranOn = new LinkedBlockingQueue<>();
		ModelInferenceService service = service((systemMessage, userMessage) -> {
			ranOn.add(Thread.currentThread());
			return "RECEIPT: created " + NAMESPACE + "/a/1.0";
		}, NAMESPACE, "", 5);
		Thread caller = Thread.currentThread();

		service.onSampleSet(set("sensors/a", false, sample("{\"a\":1}")));

		Thread completionThread = ranOn.poll(5, TimeUnit.SECONDS);
		assertNotNull(completionThread, "The completion should have been called");
		assertNotSame(caller, completionThread, "The caller must never carry the request");
		assertTrue(completionThread.getName().startsWith("event.atlas-model-inference"),
				"Expected one of this component's threads, was " + completionThread.getName());
		assertTrue(completionThread.isDaemon(), "An agent mid-run must not keep the JVM alive");
	}

	@Test
	@DisplayName("A created receipt is reported against the channel, review and all")
	void createdReceipt_isReported() throws Exception {
		RecordingCompletion completion = new RecordingCompletion("RECEIPT: created " + NAMESPACE + "/a/1.0");
		ModelInferenceService service = service(completion, NAMESPACE, "", 5);
		List<LogRecord> logged = captureLog();

		service.onSampleSet(set("sensors/a", false, sample("{\"a\":1}")));
		completion.awaitCall();

		assertTrue(awaitLog(logged, Level.INFO, "A model draft was published"),
				"The published draft must be logged against its channel");
		assertTrue(logged.stream().anyMatch(record -> record.getMessage().contains("reviewed and promoted")),
				"And it must be clear that nothing resolves until a human promotes it");
	}

	@Test
	@DisplayName("A conflict is reported as normal, not as an error")
	void conflictReceipt_isNotAnError() throws Exception {
		RecordingCompletion completion = new RecordingCompletion("RECEIPT: conflict " + NAMESPACE + "/a/1.0");
		ModelInferenceService service = service(completion, NAMESPACE, "", 5);
		List<LogRecord> logged = captureLog();

		service.onSampleSet(set("sensors/a", false, sample("{\"a\":1}")));
		completion.awaitCall();

		assertTrue(awaitLog(logged, Level.INFO, "A draft already existed"));
		assertTrue(logged.stream().noneMatch(record -> record.getLevel() == Level.WARNING),
				"A conflict is the steady state of a model waiting for review");
	}

	@Test
	@DisplayName("A completion that throws is reported as unavailable and not retried straight away")
	// The unreachable case must not turn into a retry storm.
	void unreachableCompletion_isUnavailableAndNotRetried() throws Exception {
		AtomicInteger calls = new AtomicInteger();
		ModelInferenceService service = service((systemMessage, userMessage) -> {
			calls.incrementAndGet();
			throw new IllegalStateException("connection refused");
		}, NAMESPACE, "", 5);
		List<LogRecord> logged = captureLog();

		service.onSampleSet(set("sensors/a", false, sample("{\"a\":1}")));
		assertTrue(awaitLog(logged, Level.WARNING, "Could not infer a model"));
		assertTrue(logged.stream().anyMatch(record -> record.getMessage().contains("connection refused")));

		service.onSampleSet(set("sensors/a", false, sample("{\"a\":1}")));
		Thread.sleep(250);
		assertEquals(1, calls.get(), "The same payloads must not hammer an unreachable completion");
	}

	@Test
	@DisplayName("A completion that does not answer in time is reported as unavailable")
	void timedOutCompletion_isUnavailable() throws Exception {
		CountDownLatch release = new CountDownLatch(1);
		ModelInferenceService service = service((systemMessage, userMessage) -> {
			try {
				release.await(5, TimeUnit.SECONDS);
			} catch (InterruptedException e) {
				Thread.currentThread().interrupt();
			}
			return "RECEIPT: created " + NAMESPACE + "/a/1.0";
		}, NAMESPACE, "", 5, 1);
		List<LogRecord> logged = captureLog();

		try {
			service.onSampleSet(set("sensors/a", false, sample("{\"a\":1}")));

			assertTrue(awaitLog(logged, Level.WARNING, "did not answer within 1s"));
		} finally {
			release.countDown();
		}
	}

	@Test
	@DisplayName("An answer with no receipt is reported as unknown rather than as a rejection")
	void unreadableAnswer_isReportedAsUnknown() throws Exception {
		RecordingCompletion completion = new RecordingCompletion("All done, the model is published!");
		ModelInferenceService service = service(completion, NAMESPACE, "", 5);
		List<LogRecord> logged = captureLog();

		service.onSampleSet(set("sensors/a", false, sample("{\"a\":1}")));
		completion.awaitCall();

		assertTrue(awaitLog(logged, Level.WARNING, "carried no receipt"));
	}

	@Test
	@DisplayName("A low-evidence sample set is recorded alongside the receipt")
	void lowEvidence_travelsWithTheReceipt() throws Exception {
		RecordingCompletion completion = new RecordingCompletion("RECEIPT: created " + NAMESPACE + "/a/1.0");
		ModelInferenceService service = service(completion, NAMESPACE, "", 5);
		List<LogRecord> logged = captureLog();

		service.onSampleSet(set("sensors/a", true, sample("{\"a\":1}")));
		Call call = completion.awaitCall();

		assertTrue(call.userMessage().contains("fewer samples than were asked for"), "The agent is told");
		assertTrue(awaitLog(logged, Level.INFO, "Evidence was thin"), "And so is whoever reviews the draft");
	}

	@Test
	@DisplayName("With no namespace configured nothing is inferred")
	// A run that does not know where it may publish would fail at the end, or publish elsewhere.
	void withoutANamespace_nothingRuns() throws Exception {
		RecordingCompletion completion = new RecordingCompletion("RECEIPT: created x");
		ModelInferenceService service = service(completion, "  ", "", 5);

		service.onSampleSet(set("sensors/a", false, sample("{\"a\":1}")));

		assertNull(completion.pollCall());
	}

	@Test
	@DisplayName("With no completion deployed the set is reported as unavailable, and can be retried")
	void withoutACompletion_reportsUnavailable() throws Exception {
		ModelInferenceService service = service(null, NAMESPACE, "", 5);
		List<LogRecord> logged = captureLog();

		service.onSampleSet(set("sensors/a", false, sample("{\"a\":1}")));

		assertTrue(awaitLog(logged, Level.WARNING, "no chat completion is deployed"));
	}

	/** One completion call, as the agent would have received it. */
	private record Call(String systemMessage, String userMessage) {
	}

	/**
	 * Answers every completion with the same canned agent message, and records what it was asked.
	 */
	private static class RecordingCompletion implements ChatCompletion {

		private final BlockingQueue<Call> received = new LinkedBlockingQueue<>();
		private final AtomicInteger calls = new AtomicInteger();
		private final String answer;

		RecordingCompletion(String answer) {
			this.answer = answer;
		}

		@Override
		public String complete(String systemMessage, String userMessage) {
			calls.incrementAndGet();
			received.add(new Call(systemMessage, userMessage));
			return answer;
		}

		Call awaitCall() throws InterruptedException {
			Call call = received.poll(5, TimeUnit.SECONDS);
			assertNotNull(call, "The completion should have been called");
			return call;
		}

		/**
		 * @return a call that already happened, or <code>null</code>. The short wait is what
		 * makes "nothing was inferred" a real assertion rather than a race
		 */
		Call pollCall() throws InterruptedException {
			return received.poll(300, TimeUnit.MILLISECONDS);
		}

		int calls() {
			return calls.get();
		}
	}

	private ModelInferenceService service(ChatCompletion completion, String namespace, String codecTypeMapId,
			int maxRuns) {
		return service(completion, namespace, codecTypeMapId, maxRuns, 600);
	}

	private ModelInferenceService service(ChatCompletion completion, String namespace, String codecTypeMapId,
			int maxRuns, long timeoutSeconds) {
		ModelInferenceService service = new ModelInferenceService();
		service.activate(config(namespace, codecTypeMapId, maxRuns, timeoutSeconds));
		if (completion != null) {
			// the field is DS-injected at runtime; set it directly here
			inject(service, "chatCompletion", completion);
		}
		services.add(service);
		return service;
	}

	private static ModelInferenceService.Config config(String namespace, String codecTypeMapId, int maxRuns,
			long timeoutSeconds) {
		return new ModelInferenceService.Config() {

			@Override
			public Class<? extends Annotation> annotationType() {
				return ModelInferenceService.Config.class;
			}

			@Override
			public String namespace() {
				return namespace;
			}

			@Override
			public String codecTypeMapId() {
				return codecTypeMapId;
			}

			@Override
			public int maxRunsPerInterval() {
				return maxRuns;
			}

			@Override
			public long intervalSeconds() {
				return 3600;
			}

			@Override
			public long timeoutSeconds() {
				return timeoutSeconds;
			}

			@Override
			public long retryAfterUnavailableSeconds() {
				return 3600;
			}

			@Override
			public int maxPayloadChars() {
				return 4096;
			}
		};
	}

	private static PayloadSample sample(String body) {
		return sampleWithShape(body, "a:int");
	}

	private static PayloadSample sampleWithShape(String body, String shape) {
		return new PayloadSample(body.getBytes(StandardCharsets.UTF_8), PayloadIngest.FORMAT_JSON, List.of(shape),
				Outcome.EMPTY, Instant.now(), 1);
	}

	private static PayloadSampleSet set(String source, boolean lowEvidence, PayloadSample... samples) {
		return new PayloadSampleSet(source, null, PayloadIngest.FORMAT_JSON, List.of(samples),
				lowEvidence ? CloseReason.MAX_WAIT : CloseReason.TARGET_REACHED, samples.length,
				Instant.now().minusSeconds(30), Instant.now());
	}

	/**
	 * Captures what the component logs. The receipt is reported to the log and nowhere else -
	 * that is the deliverable - so this is where the reporting has to be asserted.
	 */
	private List<LogRecord> captureLog() {
		List<LogRecord> logged = new ArrayList<>();
		Logger logger = Logger.getLogger(ModelInferenceService.class.getName());
		Handler capture = new Handler() {

			@Override
			public synchronized void publish(LogRecord record) {
				logged.add(record);
			}

			@Override
			public void flush() {
				// nothing is buffered
			}

			@Override
			public void close() {
				// nothing to release
			}
		};
		capture.setLevel(Level.ALL);
		logger.addHandler(capture);
		logger.setLevel(Level.ALL);
		captures.add(capture);
		return logged;
	}

	/** The reporting happens on the runner thread, after the completion answered. */
	private static boolean awaitLog(List<LogRecord> logged, Level level, String message) throws InterruptedException {
		for (int i = 0; i < 100; i++) {
			synchronized (logged) {
				if (logged.stream().anyMatch(
						record -> record.getLevel() == level && record.getMessage().contains(message))) {
					return true;
				}
			}
			Thread.sleep(50);
		}
		return false;
	}

	private static void inject(Object target, String fieldName, Object value) {
		try {
			Field field = target.getClass().getDeclaredField(fieldName);
			field.setAccessible(true);
			field.set(target, value);
		} catch (ReflectiveOperationException e) {
			throw new IllegalStateException("Could not inject '" + fieldName + "'", e);
		}
	}

}
