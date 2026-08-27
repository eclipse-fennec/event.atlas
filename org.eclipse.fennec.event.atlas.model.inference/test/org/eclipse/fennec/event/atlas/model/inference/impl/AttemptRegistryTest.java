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
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.Duration;
import java.time.Instant;

import org.eclipse.fennec.event.atlas.model.inference.impl.InferenceReceipt.Outcome;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/**
 * Unit tests for {@link AttemptRegistry} - the guard that keeps a channel which keeps failing
 * from re-running inference, and the one place where "no retry storm" is decided.
 * @author Ilenia Salvadori
 */
public class AttemptRegistryTest {

	private static final Instant NOON = Instant.parse("2026-08-27T12:00:00Z");
	private static final String FINGERPRINT = "abc123";

	@Test
	@DisplayName("A fingerprint can be claimed once, and is held while the run is in flight")
	// Claimed before the run rather than after it: a run takes minutes, and the sample sets
	// arriving meanwhile carry the same shapes because nothing has been promoted yet.
	void claim_isHeldFromTheStartOfTheRun() {
		AttemptRegistry registry = new AttemptRegistry(Duration.ofHours(1));

		assertTrue(registry.claim(FINGERPRINT, NOON));
		assertFalse(registry.claim(FINGERPRINT, NOON.plusSeconds(30)), "The run is still going");
		assertEquals(1, registry.size());
	}

	@Test
	@DisplayName("A decision about the payloads stands for good")
	void terminalOutcomes_areNeverRetried() {
		for (Outcome outcome : new Outcome[] { Outcome.CREATED, Outcome.CONFLICT, Outcome.REJECTED,
				Outcome.UNREADABLE }) {
			AttemptRegistry registry = new AttemptRegistry(Duration.ofSeconds(1));
			registry.claim(FINGERPRINT, NOON);
			registry.completed(FINGERPRINT, outcome, NOON);

			assertFalse(registry.claim(FINGERPRINT, NOON.plusSeconds(86400)),
					outcome + " says something about these payloads and must not be reconsidered");
		}
	}

	@Test
	@DisplayName("An unreachable completion is retried, but only after the configured delay")
	// A network outage must neither cause a retry storm nor permanently deny a model to a sensor
	// that never got its chance.
	void unavailable_isRetriedAfterTheDelay() {
		AttemptRegistry registry = new AttemptRegistry(Duration.ofHours(1));
		registry.claim(FINGERPRINT, NOON);
		registry.completed(FINGERPRINT, Outcome.UNAVAILABLE, NOON);

		assertFalse(registry.claim(FINGERPRINT, NOON.plusSeconds(60)), "Not straight away");
		assertFalse(registry.claim(FINGERPRINT, NOON.plusSeconds(3599)));
		assertTrue(registry.claim(FINGERPRINT, NOON.plusSeconds(3601)), "But eventually");
		assertFalse(registry.claim(FINGERPRINT, NOON.plusSeconds(3602)), "And then it is claimed again");
	}

	@Test
	@DisplayName("A run that never started leaves no claim behind")
	void release_letsTheNextSetTryImmediately() {
		AttemptRegistry registry = new AttemptRegistry(Duration.ofHours(1));
		registry.claim(FINGERPRINT, NOON);
		registry.release(FINGERPRINT);

		assertTrue(registry.claim(FINGERPRINT, NOON), "Nothing was attempted, so nothing is remembered");
		assertEquals(1, registry.size());
	}

	@Test
	@DisplayName("Different payload shapes are tracked apart")
	void claims_arePerFingerprint() {
		AttemptRegistry registry = new AttemptRegistry(Duration.ofHours(1));

		assertTrue(registry.claim(FINGERPRINT, NOON));
		assertTrue(registry.claim("other", NOON));
		assertEquals(2, registry.size());
	}

}
