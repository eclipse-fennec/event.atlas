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
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.eclipse.fennec.event.atlas.model.inference.InferenceOutcome.Status;

/**
 * Remembers which sample-set fingerprints have already been inferred, so that a channel that
 * keeps failing does not re-run inference over and over.
 * <p>
 * A fingerprint is claimed <em>before</em> the run starts, not after it finishes: a run takes
 * minutes, and the whole point of the dedup is that the sample sets arriving in the meantime -
 * which are the same shapes again, since nothing has been promoted yet - find the claim already
 * there. This is the workspace's existing {@code EcoreFingerprintRegistry} behaviour, and the
 * reason it registers before triggering generation.
 * <p>
 * Where it differs: that registry keeps a fingerprint claimed even when the generation failed,
 * with a comment that retrying would mean releasing it. Here, only the outcomes that say
 * something about the payloads are permanent, which is the line {@link Status#isRetryable()}
 * draws - a completion that was unreachable, or an agent that authored a model and could not
 * publish it, decided nothing about these payloads and is released again after a configurable
 * delay. The delay is what keeps that from becoming a retry storm.
 * @author Ilenia Salvadori
 * @since 27.08.2026
 */
class AttemptRegistry {

	/** Later than any {@code now}, so a claim carrying it is never re-attempted. */
	private static final Instant NEVER = Instant.MAX;

	/**
	 * When a claimed fingerprint may be attempted again. {@link Instant#MAX} - which every
	 * claim starts as, and which every permanent outcome leaves it as - is never; absent means
	 * it was never claimed at all.
	 */
	private final Map<String, Instant> claims = new ConcurrentHashMap<>();

	private final Duration retryAfterUnavailable;

	/**
	 * @param retryAfterUnavailable how long an unreachable completion blocks a second attempt at
	 * the same fingerprint. Parameter must not be <code>null</code>
	 */
	AttemptRegistry(Duration retryAfterUnavailable) {
		this.retryAfterUnavailable = retryAfterUnavailable;
	}

	/**
	 * Claims a fingerprint for a run about to start.
	 * @param fingerprint the sample set's fingerprint. Parameter must not be <code>null</code>
	 * @param now the current instant. Parameter must not be <code>null</code>
	 * @return <code>true</code> if the caller may run; <code>false</code> if this fingerprint
	 * has already been inferred, or is being inferred right now
	 */
	boolean claim(String fingerprint, Instant now) {
		Instant retryAt = claims.putIfAbsent(fingerprint, NEVER);
		if (retryAt == null) {
			return true;
		}
		if (retryAt.isAfter(now)) {
			// still claimed: a run in flight, or a permanent outcome, or a retry delay running
			return false;
		}
		// the retry delay of an unreachable completion has passed; re-claim it, and lose the
		// race gracefully if another thread got there first
		return claims.replace(fingerprint, retryAt, NEVER);
	}

	/**
	 * Records what the run ended in, which decides whether the claim stands.
	 * @param fingerprint the fingerprint the run was started for. Parameter must not be
	 * <code>null</code>
	 * @param outcome what the run ended in. Parameter must not be <code>null</code>
	 * @param now the current instant. Parameter must not be <code>null</code>
	 */
	void completed(String fingerprint, Status outcome, Instant now) {
		if (outcome.isRetryable()) {
			claims.put(fingerprint, now.plus(retryAfterUnavailable));
		}
		// every other outcome is a decision about these payloads and stands
	}

	/**
	 * Releases a claim for a run that never started - no completion was deployed, say. Unlike a
	 * finished run this leaves nothing behind: the next sample set of that shape must be able to
	 * try again immediately, since nothing was attempted.
	 */
	void release(String fingerprint) {
		claims.remove(fingerprint);
	}

	/**
	 * @return how many fingerprints are claimed, for the log
	 */
	int size() {
		return claims.size();
	}

}
