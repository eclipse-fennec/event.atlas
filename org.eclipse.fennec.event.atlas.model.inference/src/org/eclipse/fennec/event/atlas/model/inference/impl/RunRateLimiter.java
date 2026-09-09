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
import java.util.ArrayDeque;
import java.util.Deque;

/**
 * A hard cap on inference runs per interval.
 * <p>
 * Not a throughput control: a run costs around two and a half minutes of agent time and real
 * money, and a misconfigured sensor emitting unknown payloads at high frequency would otherwise
 * spend both without limit. The cap counts <em>runs</em> rather than payloads or tokens, because
 * that is what the cost follows - each turn re-reads the whole accumulated context, so the
 * sample set is a rounding error beside the turn count.
 * <p>
 * A sliding window rather than fixed buckets, so that a cap of five per hour cannot be spent
 * twice within a minute across a bucket boundary.
 * @author Ilenia Salvadori
 * @since 27.08.2026
 */
class RunRateLimiter {

	private final int maxRuns;
	private final Duration interval;

	/** The instants of the runs still inside the window, oldest first. */
	private final Deque<Instant> runs = new ArrayDeque<>();

	/**
	 * @param maxRuns how many runs the interval allows. Below 1 nothing is ever allowed, which
	 * is a legitimate way to switch inference off
	 * @param interval the sliding window. Parameter must not be <code>null</code>
	 */
	RunRateLimiter(int maxRuns, Duration interval) {
		this.maxRuns = maxRuns;
		this.interval = interval;
	}

	/**
	 * Records a run if the cap allows one.
	 * @param now the current instant. Parameter must not be <code>null</code>
	 * @return <code>true</code> if the caller may run
	 */
	synchronized boolean tryRun(Instant now) {
		Instant windowStart = now.minus(interval);
		while (!runs.isEmpty() && !runs.peekFirst().isAfter(windowStart)) {
			runs.pollFirst();
		}
		if (runs.size() >= maxRuns) {
			return false;
		}
		runs.addLast(now);
		return true;
	}

	/**
	 * @param now the current instant. Parameter must not be <code>null</code>
	 * @return when the next run would be allowed, for the log line that says why one was
	 * refused. Never <code>null</code>
	 */
	synchronized Instant nextAllowed(Instant now) {
		Instant oldest = runs.peekFirst();
		return oldest == null ? now : oldest.plus(interval);
	}

	/**
	 * Gives a recorded run back, for a run that never happened after all - the completion was
	 * not deployed, or the prompt could not be composed. A refused attempt must not cost the
	 * budget of a real one.
	 */
	synchronized void giveBack() {
		runs.pollLast();
	}

	@Override
	public synchronized String toString() {
		return String.format("at most %s run(s) per %ss", maxRuns, interval.toSeconds());
	}

}
