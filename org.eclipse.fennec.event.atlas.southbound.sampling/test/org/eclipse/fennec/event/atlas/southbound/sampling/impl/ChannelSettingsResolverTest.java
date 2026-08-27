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

import java.time.Duration;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/**
 * Unit tests for {@link ChannelSettingsResolver}. Per-channel overrides exist because the
 * close conditions are cadence-dependent and cadence belongs to the sensor: ten samples is ten
 * seconds from a 1 Hz sensor and two and a half hours from a 15-minute one.
 * @author Ilenia Salvadori
 */
public class ChannelSettingsResolverTest {

	private static final ChannelSettings DEFAULTS = ChannelSettings.of(10, 3, 1800, 0);

	@Test
	@DisplayName("A channel no override matches gets the configured defaults")
	void unmatchedChannel_getsTheDefaults() {
		ChannelSettingsResolver resolver = new ChannelSettingsResolver(DEFAULTS,
				new String[] { "sensors/fast/*;samples=2" });

		assertEquals(DEFAULTS, resolver.settingsFor("sensors/slow/rain-gauge"));
		assertEquals(1, resolver.overrideCount());
	}

	@Test
	@DisplayName("An override applies to the channels its glob matches and keeps the other defaults")
	void matchingOverride_appliesAndInheritsWhatItDoesNotSay() {
		ChannelSettingsResolver resolver = new ChannelSettingsResolver(DEFAULTS,
				new String[] { "sensors/fast/*;samples=4;maxWait=60" });

		ChannelSettings fast = resolver.settingsFor("sensors/fast/dragino-1");

		assertEquals(4, fast.targetSamples());
		assertEquals(Duration.ofSeconds(60), fast.maxWait());
		assertEquals(DEFAULTS.quietSamples(), fast.quietSamples(), "An unmentioned setting keeps its default");
		assertEquals(4, fast.ringSize(), "The ring follows the target unless it is given");
	}

	@Test
	@DisplayName("The first matching override wins, so the specific ones go first")
	void overrides_areEvaluatedInOrder() {
		ChannelSettingsResolver resolver = new ChannelSettingsResolver(DEFAULTS,
				new String[] { "sensors/fast/dragino-1;samples=2", "sensors/fast/*;samples=5" });

		assertEquals(2, resolver.settingsFor("sensors/fast/dragino-1").targetSamples());
		assertEquals(5, resolver.settingsFor("sensors/fast/dragino-2").targetSamples());
	}

	@Test
	@DisplayName("A glob matches the whole channel name and quotes everything but * and ?")
	// MQTT topics carry slashes, dots and + characters; read as a regex they would match the
	// wrong channels or fail to compile at all.
	void glob_isAnchoredAndQuotesRegexCharacters() {
		ChannelSettingsResolver resolver = new ChannelSettingsResolver(DEFAULTS,
				new String[] { "sensors/a+b.c/?;samples=2" });

		assertEquals(2, resolver.settingsFor("sensors/a+b.c/1").targetSamples());
		assertEquals(DEFAULTS, resolver.settingsFor("sensors/aab.c/1"), "'+' must not mean 'one or more'");
		assertEquals(DEFAULTS, resolver.settingsFor("sensors/a+bxc/1"), "'.' must not mean 'any character'");
		assertEquals(DEFAULTS, resolver.settingsFor("sensors/a+b.c/12"), "'?' matches exactly one character");
		assertEquals(DEFAULTS, resolver.settingsFor("prefix/sensors/a+b.c/1"), "A glob is anchored at both ends");
	}

	@Test
	@DisplayName("A malformed override is skipped, the rest still apply")
	// The alternative - failing activation - would take payload sampling out of the runtime over
	// a typo in a threshold, and with it the suppression of the ingress' per-payload warnings.
	void malformedOverride_isIgnoredWithoutLosingTheOthers() {
		ChannelSettingsResolver resolver = new ChannelSettingsResolver(DEFAULTS, new String[] { //
				"sensors/broken/*;samples=many", // not a number
				"sensors/broken/*;nonsense=1", // not a setting
				"sensors/broken/*;samples", // not a key=value pair
				";samples=2", // no glob
				"", //
				null, //
				"sensors/good/*;samples=7" });

		assertEquals(1, resolver.overrideCount());
		assertEquals(DEFAULTS, resolver.settingsFor("sensors/broken/1"));
		assertEquals(7, resolver.settingsFor("sensors/good/1").targetSamples());
	}

	@Test
	@DisplayName("Nonsense numbers are clamped rather than rejected")
	void settings_areClampedIntoTheUsableRange() {
		ChannelSettings clamped = ChannelSettings.of(0, -1, 0, -5);

		assertEquals(1, clamped.targetSamples());
		assertEquals(1, clamped.quietSamples());
		assertEquals(Duration.ofSeconds(1), clamped.maxWait());
		assertEquals(1, clamped.ringSize());
	}

	@Test
	@DisplayName("A ring larger than the target is capped, a smaller one is kept")
	void ringSize_neverExceedsTheTarget() {
		assertEquals(10, ChannelSettings.of(10, 3, 60, 50).ringSize(), "More slots than samples is dead memory");
		assertEquals(2, ChannelSettings.of(10, 3, 60, 2).ringSize());
	}

}
