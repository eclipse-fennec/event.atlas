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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Collections;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Set;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;

import org.eclipse.fennec.event.atlas.southbound.common.IngestResult.Outcome;
import org.eclipse.fennec.event.atlas.southbound.common.PayloadIngest;
import org.eclipse.fennec.event.atlas.southbound.common.UnknownPayload;
import org.eclipse.fennec.event.atlas.southbound.sampling.PayloadSample;
import org.eclipse.fennec.event.atlas.southbound.sampling.PayloadSampleSet;
import org.eclipse.fennec.event.atlas.southbound.sampling.PayloadSampleSet.CloseReason;
import org.eclipse.fennec.event.atlas.southbound.sampling.PayloadSampleSetHandler;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/**
 * Unit tests for {@link PayloadSampleCollector}: the window key, shape admission, the three
 * close conditions, and the promise that a set is handed over exactly once.
 * <p>
 * The hand-over is asynchronous, so every assertion about a set goes through
 * {@link RecordingHandler#awaitSet()} rather than reading a field after the call.
 * @author Ilenia Salvadori
 */
public class PayloadSampleCollectorTest {

	private static final String CHANNEL = "sensors/dragino/1";

	private final List<PayloadSampleCollector> collectors = new ArrayList<>();

	@AfterEach
	void tearDown() {
		collectors.forEach(PayloadSampleCollector::deactivate);
	}

	@Test
	@DisplayName("Payloads from different channels never share a window")
	void windows_areOnePerChannel() throws Exception {
		RecordingHandler handler = new RecordingHandler();
		PayloadSampleCollector collector = collector(handler, 2, 99, 600, 0, 10);

		collector.onUnknownModel(json(CHANNEL, "{\"a\":1}"));
		collector.onUnknownModel(json("sensors/dragino/2", "{\"a\":1}"));
		collector.onUnknownModel(json(CHANNEL, "{\"b\":1}"));
		collector.onUnknownModel(json("sensors/dragino/2", "{\"c\":1}"));

		PayloadSampleSet first = handler.awaitSet();
		PayloadSampleSet second = handler.awaitSet();
		assertEquals(CHANNEL, first.source());
		assertEquals("sensors/dragino/2", second.source());
		assertEquals(2, first.sampleCount());
		assertEquals(2, first.payloadsSeen(), "Only that channel's payloads may count towards its window");
		assertEquals(2, second.payloadsSeen());
	}

	@Test
	@DisplayName("Two payloads of the same shape occupy one slot, a new shape opens another")
	void admission_isByShapeNotByPayload() throws Exception {
		RecordingHandler handler = new RecordingHandler();
		PayloadSampleCollector collector = collector(handler, 3, 99, 600, 0, 10);

		// five readings of one shape, then two more shapes
		for (int i = 0; i < 5; i++) {
			collector.onUnknownModel(json(CHANNEL, "{\"temp\":" + (20 + i) + "}"));
		}
		collector.onUnknownModel(json(CHANNEL, "{\"temp\":20,\"rssi\":-70}"));
		collector.onUnknownModel(json(CHANNEL, "{\"temp\":20.5}"));

		PayloadSampleSet set = handler.awaitSet();
		assertEquals(3, set.sampleCount(), "Three distinct shapes, not seven payloads");
		assertEquals(7, set.payloadsSeen());
		PayloadSample repeated = set.samples().get(0);
		assertEquals(5, repeated.occurrences(), "The repeated shape must report how often it arrived");
		assertEquals(List.of("temp:int"), repeated.shape());
		assertEquals(1, set.samples().get(2).occurrences());
	}

	@Test
	@DisplayName("A window closes when the target number of distinct shapes is reached")
	void window_closesAtTheTarget() throws Exception {
		RecordingHandler handler = new RecordingHandler();
		PayloadSampleCollector collector = collector(handler, 3, 99, 600, 0, 10);

		collector.onUnknownModel(json(CHANNEL, "{\"a\":1}"));
		collector.onUnknownModel(json(CHANNEL, "{\"b\":1}"));
		assertNull(handler.poll(), "Two of three samples must not hand anything over yet");

		collector.onUnknownModel(json(CHANNEL, "{\"c\":1}"));

		PayloadSampleSet set = handler.awaitSet();
		assertEquals(CloseReason.TARGET_REACHED, set.closeReason());
		assertFalse(set.lowEvidence());
		assertEquals(3, set.sampleCount());
	}

	@Test
	@DisplayName("A window closes early once the configured number of payloads adds no new shape")
	// What makes a fast, monotonous sensor cheap: a 1 Hz device repeating one shape is done in K
	// readings instead of waiting for distinct shapes that will never come.
	void window_closesWhenTheShapesStopChanging() throws Exception {
		RecordingHandler handler = new RecordingHandler();
		PayloadSampleCollector collector = collector(handler, 10, 2, 600, 0, 10);

		collector.onUnknownModel(json(CHANNEL, "{\"temp\":1}"));
		collector.onUnknownModel(json(CHANNEL, "{\"temp\":2}"));
		assertNull(handler.poll(), "One repeat is not yet quiet enough");
		collector.onUnknownModel(json(CHANNEL, "{\"temp\":3}"));

		PayloadSampleSet set = handler.awaitSet();
		assertEquals(CloseReason.NO_NEW_SHAPES, set.closeReason());
		assertEquals(1, set.sampleCount());
		assertEquals(3, set.payloadsSeen());
		assertFalse(set.lowEvidence(), "Nothing more was coming - that is not thin evidence, it is all there is");
	}

	@Test
	@DisplayName("A window closes on the maximum wait and says its evidence is thin")
	// The condition that keeps a 15-minute sensor from never producing a set at all.
	void window_closesOnMaxWaitAndIsFlagged() throws Exception {
		RecordingHandler handler = new RecordingHandler();
		PayloadSampleCollector collector = collector(handler, 10, 99, 1, 0, 10);

		collector.onUnknownModel(json(CHANNEL, "{\"temp\":1}"));

		PayloadSampleSet set = handler.awaitSet();
		assertEquals(CloseReason.MAX_WAIT, set.closeReason());
		assertTrue(set.lowEvidence(), "Whoever reviews the model must know how little stood behind it");
		assertEquals(1, set.sampleCount(), "The sample count travels with the flag");
		assertFalse(collector.isCollecting(CHANNEL), "The window must be gone, not left closed in the map");
	}

	@Test
	@DisplayName("A payload arriving after its window closed starts the next one")
	void payloadAfterClose_opensANewWindow() throws Exception {
		RecordingHandler handler = new RecordingHandler();
		PayloadSampleCollector collector = collector(handler, 2, 99, 600, 0, 10);

		collector.onUnknownModel(json(CHANNEL, "{\"a\":1}"));
		collector.onUnknownModel(json(CHANNEL, "{\"b\":1}"));
		assertEquals(2, handler.awaitSet().sampleCount());

		collector.onUnknownModel(json(CHANNEL, "{\"c\":1}"));
		collector.onUnknownModel(json(CHANNEL, "{\"d\":1}"));

		PayloadSampleSet second = handler.awaitSet();
		assertEquals(2, second.sampleCount());
		assertEquals(List.of("c:int"), second.samples().get(0).shape(), "The second window starts empty");
	}

	@Test
	@DisplayName("Under concurrent arrivals every window is handed over exactly once")
	void concurrentArrivals_handOverEachWindowOnce() throws Exception {
		RecordingHandler handler = new RecordingHandler();
		int target = 4;
		int threads = 8;
		int perThread = 5;
		PayloadSampleCollector collector = collector(handler, target, 999, 600, 0, 10);
		CountDownLatch start = new CountDownLatch(1);
		ExecutorService pushers = Executors.newFixedThreadPool(threads);
		try {
			for (int t = 0; t < threads; t++) {
				int thread = t;
				pushers.execute(() -> {
					try {
						start.await(5, TimeUnit.SECONDS);
						for (int i = 0; i < perThread; i++) {
							// every payload has a shape of its own, so every one is admitted
							collector.onUnknownModel(json(CHANNEL, "{\"f" + thread + "_" + i + "\":1}"));
						}
					} catch (InterruptedException e) {
						Thread.currentThread().interrupt();
					}
				});
			}
			start.countDown();
			pushers.shutdown();
			assertTrue(pushers.awaitTermination(10, TimeUnit.SECONDS), "The pushers should have finished");
		} finally {
			pushers.shutdownNow();
		}

		int expectedSets = threads * perThread / target;
		List<PayloadSampleSet> sets = handler.awaitSets(expectedSets);
		Set<PayloadSampleSet> distinct = Collections.newSetFromMap(new IdentityHashMap<>());
		distinct.addAll(sets);
		assertEquals(expectedSets, distinct.size(), "No set may be handed over twice");
		assertNull(handler.poll(), "And no set beyond the ones the samples add up to");
		sets.forEach(set -> assertEquals(target, set.sampleCount(), "Every window closes on its own target"));
	}

	@Test
	@DisplayName("While a window is open the channel is reported as collecting")
	// This is what turns the ingress' warning per dropped payload into one line per window:
	// commissioning a new sensor must not look like an outage.
	void isCollecting_isTrueWhileTheWindowIsOpen() throws Exception {
		RecordingHandler handler = new RecordingHandler();
		PayloadSampleCollector collector = collector(handler, 2, 99, 600, 0, 10);

		assertFalse(collector.isCollecting(CHANNEL), "Nothing is collected before the first payload");
		collector.onUnknownModel(json(CHANNEL, "{\"a\":1}"));
		assertTrue(collector.isCollecting(CHANNEL));
		assertFalse(collector.isCollecting("sensors/other"), "Only the channels actually being collected");
		assertFalse(collector.isCollecting(null));

		collector.onUnknownModel(json(CHANNEL, "{\"b\":1}"));
		handler.awaitSet();

		assertFalse(collector.isCollecting(CHANNEL), "A closed window stops suppressing the warnings");
	}

	@Test
	@DisplayName("The concurrent window cap drops payloads of further channels")
	void maxWindows_capsTheChannelsCollectedAtOnce() throws Exception {
		RecordingHandler handler = new RecordingHandler();
		PayloadSampleCollector collector = collector(handler, 2, 99, 600, 0, 1);

		collector.onUnknownModel(json(CHANNEL, "{\"a\":1}"));
		collector.onUnknownModel(json("sensors/other", "{\"a\":1}"));
		collector.onUnknownModel(json("sensors/other", "{\"b\":1}"));

		assertTrue(collector.isCollecting(CHANNEL));
		assertFalse(collector.isCollecting("sensors/other"), "The second channel must not have opened a window");
		assertNull(handler.poll(), "And must not have handed anything over");
	}

	@Test
	@DisplayName("A ring smaller than the target keeps the newest samples and still closes at the target")
	void ringSize_boundsWhatIsKeptWithoutMovingTheTarget() throws Exception {
		RecordingHandler handler = new RecordingHandler();
		PayloadSampleCollector collector = collector(handler, 4, 99, 600, 2, 10);

		for (String field : List.of("a", "b", "c", "d")) {
			collector.onUnknownModel(json(CHANNEL, "{\"" + field + "\":1}"));
		}

		PayloadSampleSet set = handler.awaitSet();
		assertEquals(CloseReason.TARGET_REACHED, set.closeReason(), "The ring must not make the target unreachable");
		assertEquals(2, set.sampleCount(), "Only the ring's worth is retained");
		assertEquals(4, set.payloadsSeen());
		assertEquals(List.of("c:int"), set.samples().get(0).shape(), "The oldest samples are the ones evicted");
		assertEquals(List.of("d:int"), set.samples().get(1).shape());
	}

	@Test
	@DisplayName("A per-channel override changes that channel's close conditions only")
	void channelOverride_appliesToItsChannelAlone() throws Exception {
		RecordingHandler handler = new RecordingHandler();
		PayloadSampleCollector collector = collector(handler, 10, 99, 600, 0, 10, "sensors/fast/*;samples=2");

		collector.onUnknownModel(json("sensors/fast/1", "{\"a\":1}"));
		collector.onUnknownModel(json("sensors/fast/1", "{\"b\":1}"));
		collector.onUnknownModel(json("sensors/slow/1", "{\"a\":1}"));
		collector.onUnknownModel(json("sensors/slow/1", "{\"b\":1}"));

		PayloadSampleSet set = handler.awaitSet();
		assertEquals("sensors/fast/1", set.source(), "Only the overridden channel closes after two samples");
		assertEquals(2, set.sampleCount());
		assertNull(handler.poll());
		assertTrue(collector.isCollecting("sensors/slow/1"), "The default channel is still collecting");
	}

	@Test
	@DisplayName("XMI payloads that declare different unresolvable models are collected apart")
	void namespaceUri_separatesWindowsOnOneChannel() throws Exception {
		RecordingHandler handler = new RecordingHandler();
		PayloadSampleCollector collector = collector(handler, 2, 99, 600, 0, 10);

		collector.onUnknownModel(xmi(CHANNEL, "http://example.org/a/1.0", "<a:S xmlns:a=\"x\" v=\"1\"/>"));
		collector.onUnknownModel(xmi(CHANNEL, "http://example.org/b/1.0", "<b:S xmlns:b=\"y\" v=\"1\"/>"));
		assertNull(handler.poll(), "Two models are two windows, neither of which has two samples yet");

		collector.onUnknownModel(xmi(CHANNEL, "http://example.org/a/1.0", "<a:S xmlns:a=\"x\" w=\"1\"/>"));

		PayloadSampleSet set = handler.awaitSet();
		assertEquals("http://example.org/a/1.0", set.namespaceUri());
		assertEquals(PayloadIngest.FORMAT_XMI, set.format());
		assertEquals(2, set.sampleCount());
	}

	@Test
	@DisplayName("A handler that throws costs its set and nothing else")
	void throwingHandler_doesNotStopTheCollector() throws Exception {
		RecordingHandler handler = new RecordingHandler(new IllegalStateException("inference unreachable"));
		PayloadSampleCollector collector = collector(handler, 2, 99, 600, 0, 10);

		collector.onUnknownModel(json(CHANNEL, "{\"a\":1}"));
		collector.onUnknownModel(json(CHANNEL, "{\"b\":1}"));
		handler.awaitSet();

		collector.onUnknownModel(json(CHANNEL, "{\"c\":1}"));
		collector.onUnknownModel(json(CHANNEL, "{\"d\":1}"));

		assertNotNull(handler.awaitSet(), "The hand-over thread must survive a failing handler");
	}

	@Test
	@DisplayName("With no handler deployed the collector still collects and closes windows")
	void withoutHandler_collectingIsStillSafe() {
		PayloadSampleCollector collector = collector(null, 2, 99, 600, 0, 10);

		collector.onUnknownModel(json(CHANNEL, "{\"a\":1}"));
		assertTrue(collector.isCollecting(CHANNEL));
		collector.onUnknownModel(json(CHANNEL, "{\"b\":1}"));

		assertFalse(collector.isCollecting(CHANNEL), "The window closes with nobody to hand it to");
	}

	@Test
	@DisplayName("Unreadable payloads of a channel share one slot instead of filling the ring")
	void unparseablePayloads_doNotFillTheWindow() throws Exception {
		RecordingHandler handler = new RecordingHandler();
		PayloadSampleCollector collector = collector(handler, 10, 3, 600, 0, 10);

		// the first is admitted, the next three add no new shape - which is the quiet limit
		for (int i = 0; i < 4; i++) {
			collector.onUnknownModel(new UnknownPayload(("garbage " + i).getBytes(StandardCharsets.UTF_8),
					PayloadIngest.FORMAT_JSON, CHANNEL, null, Outcome.PARSE_ERROR, Instant.now()));
		}

		PayloadSampleSet set = handler.awaitSet();
		assertEquals(CloseReason.NO_NEW_SHAPES, set.closeReason());
		assertEquals(1, set.sampleCount());
		assertEquals(4, set.samples().get(0).occurrences());
		assertEquals(Outcome.PARSE_ERROR, set.samples().get(0).outcome(), "The outcome travels with the sample");
	}

	/**
	 * Records the sets it is handed and lets a test wait for them.
	 */
	private static class RecordingHandler implements PayloadSampleSetHandler {

		private final BlockingQueue<PayloadSampleSet> received = new LinkedBlockingQueue<>();
		private final RuntimeException failWith;

		RecordingHandler() {
			this(null);
		}

		RecordingHandler(RuntimeException failWith) {
			this.failWith = failWith;
		}

		@Override
		public void onSampleSet(PayloadSampleSet sampleSet) {
			received.add(sampleSet);
			if (failWith != null) {
				throw failWith;
			}
		}

		PayloadSampleSet awaitSet() throws InterruptedException {
			PayloadSampleSet sampleSet = received.poll(5, TimeUnit.SECONDS);
			assertNotNull(sampleSet, "A sample set should have been handed over");
			return sampleSet;
		}

		List<PayloadSampleSet> awaitSets(int count) throws InterruptedException {
			List<PayloadSampleSet> sets = new ArrayList<>(count);
			for (int i = 0; i < count; i++) {
				sets.add(awaitSet());
			}
			return sets;
		}

		/**
		 * @return a set that was already handed over, or <code>null</code>. The short wait is
		 * what makes "nothing was handed over" a real assertion rather than a race
		 */
		PayloadSampleSet poll() throws InterruptedException {
			return received.poll(250, TimeUnit.MILLISECONDS);
		}
	}

	@Test
	@DisplayName("Disabled, it declines every payload and hands nothing over")
	// The default. A runtime that merely deploys this bundle - a docker image shipping every
	// feature - must buffer nothing until an operator asks for it.
	void disabled_collectsNothing() throws Exception {
		RecordingHandler handler = new RecordingHandler();
		PayloadSampleCollector collector = new PayloadSampleCollector();
		collector.activate(disabledConfig());
		inject(collector, "sampleSetHandler", handler);
		collectors.add(collector);

		for (int i = 0; i < 20; i++) {
			collector.onUnknownModel(json(CHANNEL, "{\"shape" + i + "\":1}"));
		}

		assertNull(handler.poll(), "A disabled collector must not hand a set over");
	}

	@Test
	@DisplayName("Switching it off abandons the windows already open")
	// Holding a half-collected set across a disable would leak it: nothing is going to consume it.
	void disabling_abandonsOpenWindows() throws Exception {
		RecordingHandler handler = new RecordingHandler();
		PayloadSampleCollector collector = collector(handler, 5, 99, 600, 0, 10);

		collector.onUnknownModel(json(CHANNEL, "{\"a\":1}"));
		collector.onUnknownModel(json(CHANNEL, "{\"b\":1}"));

		collector.activate(disabledConfig());

		collector.onUnknownModel(json(CHANNEL, "{\"c\":1}"));
		assertNull(handler.poll(), "Neither the abandoned window nor the new payload may be handed over");
	}

	private static PayloadSampleCollector.Config disabledConfig() {
		return new PayloadSampleCollector.Config() {

			@Override
			public Class<? extends Annotation> annotationType() {
				return PayloadSampleCollector.Config.class;
			}

			@Override
			public boolean enabled() {
				return false;
			}

			@Override
			public int targetSamples() {
				return 10;
			}

			@Override
			public int quietSamples() {
				return 3;
			}

			@Override
			public long maxWaitSeconds() {
				return 1800;
			}

			@Override
			public int ringSize() {
				return 0;
			}

			@Override
			public int maxWindows() {
				return 100;
			}

			@Override
			public String[] channels() {
				return new String[0];
			}
		};
	}

	private PayloadSampleCollector collector(PayloadSampleSetHandler handler, int target, int quiet, long maxWaitSeconds,
			int ring, int windows, String... channels) {
		PayloadSampleCollector collector = new PayloadSampleCollector();
		collector.activate(config(target, quiet, maxWaitSeconds, ring, windows, channels));
		if (handler != null) {
			// the field is DS-injected at runtime; set it directly here
			inject(collector, "sampleSetHandler", handler);
		}
		collectors.add(collector);
		return collector;
	}

	private static PayloadSampleCollector.Config config(int target, int quiet, long maxWaitSeconds, int ring,
			int windows, String... channels) {
		return new PayloadSampleCollector.Config() {

			@Override
			public Class<? extends Annotation> annotationType() {
				return PayloadSampleCollector.Config.class;
			}

			@Override
			public boolean enabled() {
				// These tests are about what the collector does when it is collecting; the
				// disabled path has its own tests below.
				return true;
			}

			@Override
			public int targetSamples() {
				return target;
			}

			@Override
			public int quietSamples() {
				return quiet;
			}

			@Override
			public long maxWaitSeconds() {
				return maxWaitSeconds;
			}

			@Override
			public int ringSize() {
				return ring;
			}

			@Override
			public int maxWindows() {
				return windows;
			}

			@Override
			public String[] channels() {
				return channels;
			}
		};
	}

	private static UnknownPayload json(String source, String body) {
		return new UnknownPayload(body.getBytes(StandardCharsets.UTF_8), PayloadIngest.FORMAT_JSON, source, null,
				Outcome.EMPTY, Instant.now());
	}

	private static UnknownPayload xmi(String source, String namespaceUri, String body) {
		return new UnknownPayload(body.getBytes(StandardCharsets.UTF_8), PayloadIngest.FORMAT_XMI, source, namespaceUri,
				Outcome.MODEL_UNKNOWN, Instant.now());
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
