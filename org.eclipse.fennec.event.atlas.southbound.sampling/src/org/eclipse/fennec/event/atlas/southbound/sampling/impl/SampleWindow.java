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
import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ScheduledFuture;

import org.eclipse.fennec.event.atlas.southbound.common.UnknownPayload;
import org.eclipse.fennec.event.atlas.southbound.sampling.PayloadSample;
import org.eclipse.fennec.event.atlas.southbound.sampling.PayloadSampleSet;
import org.eclipse.fennec.event.atlas.southbound.sampling.PayloadSampleSet.CloseReason;

/**
 * One open collection window: the samples gathered for a {@link WindowKey} so far, and the
 * three conditions that end the gathering.
 * <p>
 * Every method that touches the buffer is {@code synchronized}, and closing is a one-way
 * transition guarded by the same lock, because the three close conditions are evaluated from
 * two different threads - the arrival of a payload from the collector's caller, the maximum
 * wait from the timer - and a window that handed its set over twice would spend an inference
 * run twice.
 * <p>
 * The window is deliberately in-memory only: a restart mid-window loses the samples and starts
 * over on the next payload. Persisting it would also give a reviewer the evidence behind a
 * draft, which is worth doing, but it is a separate question from collecting.
 * @author Ilenia Salvadori
 * @since 27.08.2026
 */
class SampleWindow {

	/** What {@link SampleWindow#offer(UnknownPayload, List)} did with a payload. */
	enum Admission {
		/** The payload's shape was new, so it took a slot. */
		ADMITTED,
		/** A sample of that exact shape is already in the window; only its count grew. */
		DUPLICATE_SHAPE,
		/** The window had already been closed - the caller must open a new one. */
		WINDOW_CLOSED
	}

	/**
	 * The outcome of offering one payload: what happened to it, and the closed set if that
	 * payload was the one that ended the window.
	 * @param admission what the window did with the payload. Never <code>null</code>
	 * @param closed the set to hand over, or <code>null</code> if the window is still open
	 */
	record Offer(Admission admission, PayloadSampleSet closed) {
	}

	private final WindowKey key;
	private final ChannelSettings settings;
	private final Instant openedAt;

	/**
	 * The samples, keyed by shape and in arrival order, which is what makes the eviction below
	 * a ring and keeps the handed-over set in the order the shapes were first seen.
	 */
	private final Map<List<String>, Slot> slots = new LinkedHashMap<>();

	private int payloadsSeen;
	/**
	 * Distinct shapes admitted over the window's whole life. Counted separately from
	 * {@link #slots}, which a ring smaller than the target evicts from: the target is how much
	 * evidence to gather, the ring is how much of it to keep, and a small ring must not silently
	 * make the target unreachable.
	 */
	private int admitted;
	private int sinceNewShape;
	private boolean closed;
	private ScheduledFuture<?> maxWaitTimer;

	SampleWindow(WindowKey key, ChannelSettings settings, Instant openedAt) {
		this.key = key;
		this.settings = settings;
		this.openedAt = openedAt;
	}

	/**
	 * Offers one payload to the window and evaluates the two arrival-driven close conditions.
	 * @param payload the payload. Parameter must not be <code>null</code>
	 * @param shape its shape, the admission key. Parameter must not be <code>null</code>
	 * @return what happened. Never <code>null</code>
	 */
	synchronized Offer offer(UnknownPayload payload, List<String> shape) {
		if (closed) {
			return new Offer(Admission.WINDOW_CLOSED, null);
		}
		payloadsSeen++;
		Slot known = slots.get(shape);
		if (known == null) {
			evictIfFull();
			slots.put(List.copyOf(shape), new Slot(payload, shape));
			admitted++;
			sinceNewShape = 0;
		} else {
			// A hundred structurally identical readings are one sample's worth of evidence,
			// but knowing there were a hundred of them is worth keeping.
			known.occurrences++;
			sinceNewShape++;
		}
		if (admitted >= settings.targetSamples()) {
			return new Offer(admission(known), close(CloseReason.TARGET_REACHED, payload.timestamp()));
		}
		if (sinceNewShape >= settings.quietSamples()) {
			return new Offer(admission(known), close(CloseReason.NO_NEW_SHAPES, payload.timestamp()));
		}
		return new Offer(admission(known), null);
	}

	/**
	 * Closes the window if it is still open - the maximum-wait path, and the shutdown path.
	 * @param reason why. Parameter must not be <code>null</code>
	 * @param at the closing timestamp. Parameter must not be <code>null</code>
	 * @return the set to hand over, or <code>null</code> if the window was already closed or
	 * never got a payload. A window closed this way is closed either way, so its key can be
	 * dropped in both cases
	 */
	synchronized PayloadSampleSet closeIfOpen(CloseReason reason, Instant at) {
		if (closed) {
			return null;
		}
		return close(reason, at);
	}

	synchronized boolean isClosed() {
		return closed;
	}

	/**
	 * @return the timestamp the window opened at, for the log line that says how long it took
	 */
	Instant openedAt() {
		return openedAt;
	}

	WindowKey key() {
		return key;
	}

	ChannelSettings settings() {
		return settings;
	}

	/**
	 * Hands the window the timer that will close it on {@link ChannelSettings#maxWait()}, so
	 * that a window closed by one of the other two conditions can cancel it.
	 */
	synchronized void maxWaitTimer(ScheduledFuture<?> timer) {
		if (closed) {
			// closed before the timer was even handed over - it has nothing left to close
			timer.cancel(false);
		} else {
			maxWaitTimer = timer;
		}
	}

	/**
	 * Must be called with the monitor held. Builds the immutable set, marks the window closed
	 * and stops the timer.
	 */
	private PayloadSampleSet close(CloseReason reason, Instant at) {
		closed = true;
		if (maxWaitTimer != null) {
			maxWaitTimer.cancel(false);
			maxWaitTimer = null;
		}
		if (slots.isEmpty()) {
			return null;
		}
		List<PayloadSample> samples = new ArrayList<>(slots.size());
		for (Slot slot : slots.values()) {
			samples.add(new PayloadSample(slot.payload.payload(), slot.payload.format(), slot.shape,
					slot.payload.outcome(), slot.payload.timestamp(), slot.occurrences));
		}
		return new PayloadSampleSet(key.source(), key.namespaceUri(), key.format(), samples, reason, payloadsSeen,
				openedAt, at);
	}

	/**
	 * Drops the oldest sample once the ring is full. Only reachable when the ring is configured
	 * smaller than the target, and it costs evidence: the shape that leaves may be the one
	 * carrying an optional field. It is the bound that keeps a misconfigured sensor farm from
	 * growing this without limit.
	 */
	private void evictIfFull() {
		if (slots.size() < settings.ringSize()) {
			return;
		}
		Iterator<List<String>> oldest = slots.keySet().iterator();
		if (oldest.hasNext()) {
			oldest.next();
			oldest.remove();
		}
	}

	private static Admission admission(Slot known) {
		return known == null ? Admission.ADMITTED : Admission.DUPLICATE_SHAPE;
	}

	/**
	 * One slot of the ring: the first payload of a shape - the earliest evidence, which is also
	 * the one whose timestamp says when the shape appeared - and how many of them arrived.
	 */
	private static final class Slot {

		private final UnknownPayload payload;
		private final List<String> shape;
		private int occurrences = 1;

		Slot(UnknownPayload payload, List<String> shape) {
			this.payload = payload;
			this.shape = shape;
		}
	}

}
