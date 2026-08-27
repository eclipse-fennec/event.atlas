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

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/**
 * Unit tests for {@link RunRateLimiter}. A run costs minutes of agent time and real money, so
 * the cap is a hard one - the clock is passed in rather than read, so the window can be tested
 * without waiting for it.
 * @author Ilenia Salvadori
 */
public class RunRateLimiterTest {

	private static final Instant NOON = Instant.parse("2026-08-27T12:00:00Z");

	@Test
	@DisplayName("Runs are allowed up to the cap and refused after it")
	void cap_isHard() {
		RunRateLimiter limiter = new RunRateLimiter(2, Duration.ofHours(1));

		assertTrue(limiter.tryRun(NOON));
		assertTrue(limiter.tryRun(NOON.plusSeconds(60)));
		assertFalse(limiter.tryRun(NOON.plusSeconds(120)));
	}

	@Test
	@DisplayName("The window slides, so a cap cannot be spent twice at a bucket boundary")
	void window_slides() {
		RunRateLimiter limiter = new RunRateLimiter(2, Duration.ofHours(1));
		limiter.tryRun(NOON);
		limiter.tryRun(NOON.plusSeconds(1800));

		assertFalse(limiter.tryRun(NOON.plusSeconds(3599)), "Both runs are still inside the hour");
		assertTrue(limiter.tryRun(NOON.plusSeconds(3601)), "The first has left the window");
		assertFalse(limiter.tryRun(NOON.plusSeconds(3602)), "The second has not");
	}

	@Test
	@DisplayName("The limiter says when the next run would be allowed")
	void nextAllowed_namesTheTimeTheLogNeeds() {
		RunRateLimiter limiter = new RunRateLimiter(1, Duration.ofHours(1));
		limiter.tryRun(NOON);

		assertEquals(NOON.plusSeconds(3600), limiter.nextAllowed(NOON.plusSeconds(10)));
	}

	@Test
	@DisplayName("A run that never happened is given back to the budget")
	void giveBack_returnsTheAllowance() {
		RunRateLimiter limiter = new RunRateLimiter(1, Duration.ofHours(1));
		assertTrue(limiter.tryRun(NOON));
		limiter.giveBack();

		assertTrue(limiter.tryRun(NOON.plusSeconds(1)), "A refused attempt must not cost a real one's budget");
	}

	@Test
	@DisplayName("A cap of zero switches inference off")
	void capOfZero_allowsNothing() {
		assertFalse(new RunRateLimiter(0, Duration.ofHours(1)).tryRun(NOON));
	}

	@Test
	@DisplayName("An empty limiter reports now as the next allowed instant")
	void nextAllowed_withoutRuns_isNow() {
		assertEquals(NOON, new RunRateLimiter(1, Duration.ofHours(1)).nextAllowed(NOON));
	}

}
