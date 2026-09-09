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

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import org.eclipse.fennec.event.atlas.southbound.common.IngestResult.Outcome;
import org.eclipse.fennec.event.atlas.southbound.common.PayloadIngest;
import org.eclipse.fennec.event.atlas.southbound.common.UnknownPayload;
import org.eclipse.fennec.event.atlas.southbound.sampling.PayloadSampleSet;
import org.eclipse.fennec.event.atlas.southbound.sampling.PayloadSampleSet.CloseReason;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/**
 * Unit tests for {@link SampleWindow}'s one-way close, with the maximum-wait timer it owns.
 * <p>
 * {@link PayloadSampleCollectorTest} covers the promise an operator sees - no set is handed
 * over after sampling is switched off - but that promise is defended twice over, by the
 * cancelled timer here and by the {@code enabled} re-check in the hand-over. These tests pin
 * the timer half on its own, so removing it fails something.
 * @author Ilenia Salvadori
 */
public class SampleWindowTest {

	private final ScheduledExecutorService timers = Executors.newSingleThreadScheduledExecutor();

	@AfterEach
	void stopTimers() {
		timers.shutdownNow();
	}

	@Test
	@DisplayName("Abandoning a window cancels the maximum-wait timer and drops the samples")
	// The disable path used to drop the collector's map entry without closing the window. The
	// timer holds its own reference, so it fired up to maxWaitSeconds later against a window
	// that was never actually closed, and its set was handed over after all.
	void abandon_cancelsTheTimerAndClosesTheWindow() {
		SampleWindow window = window(10, 99, 1800);
		ScheduledFuture<?> timer = schedule();
		window.maxWaitTimer(timer);
		window.offer(json("{\"a\":1}"), List.of("a"));

		window.abandon();

		assertTrue(window.isClosed(), "Abandoning is a close");
		assertTrue(timer.isCancelled(), "The maximum wait must not survive an abandoned window");
		assertNull(window.closeIfOpen(CloseReason.MAX_WAIT, Instant.now()),
				"A timer that still fires must find the window closed and get no set");
	}

	@Test
	@DisplayName("A window closed on its own condition also cancels the timer")
	void close_cancelsTheTimer() {
		SampleWindow window = window(1, 99, 1800);
		ScheduledFuture<?> timer = schedule();
		window.maxWaitTimer(timer);

		PayloadSampleSet closed = window.offer(json("{\"a\":1}"), List.of("a")).closed();

		assertNotNull(closed, "One sample reaches the target of one");
		assertTrue(timer.isCancelled(), "Nothing is left for the maximum wait to close");
	}

	@Test
	@DisplayName("A timer handed to an already-abandoned window is cancelled on arrival")
	// scheduleMaxWait runs after the window is in the map, so the abandon can get there first.
	void maxWaitTimer_afterAbandon_isCancelled() {
		SampleWindow window = window(10, 99, 1800);
		window.abandon();

		ScheduledFuture<?> timer = schedule();
		window.maxWaitTimer(timer);

		assertTrue(timer.isCancelled(), "A closed window has nothing left to close");
		assertFalse(timers.isShutdown(), "The pool itself is unaffected");
	}

	private SampleWindow window(int target, int quiet, long maxWaitSeconds) {
		return new SampleWindow(new WindowKey("sensors/dragino/1", null, PayloadIngest.FORMAT_JSON),
				ChannelSettings.of(target, quiet, maxWaitSeconds, 0), Instant.now());
	}

	/** A real future, far enough out that only a cancel can decide its fate inside a test. */
	private ScheduledFuture<?> schedule() {
		return timers.schedule(() -> {
			// nothing: the test asks whether it was cancelled, never what it does
		}, 1, TimeUnit.HOURS);
	}

	private static UnknownPayload json(String body) {
		return new UnknownPayload(body.getBytes(StandardCharsets.UTF_8), PayloadIngest.FORMAT_JSON,
				"sensors/dragino/1", null, Outcome.EMPTY, Instant.now());
	}
}
