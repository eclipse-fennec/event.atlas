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

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.regex.Pattern;
import java.util.logging.Logger;

/**
 * Resolves the {@link ChannelSettings} of a channel: the configured defaults, unless an
 * override matches the channel's name.
 * <p>
 * Per-channel overrides exist because the close conditions are cadence-dependent, and cadence
 * is a property of the sensor rather than of the runtime: ten samples is ten seconds from a
 * 1 Hz sensor and two and a half hours from a 15-minute one. Each override is one
 * configuration string
 *
 * <pre>
 * &lt;glob&gt;;samples=20;quiet=5;maxWait=600;ring=10
 * </pre>
 *
 * where the glob is matched against the whole channel name with {@code *} standing for any
 * run of characters and {@code ?} for one, and every setting after it is optional and falls
 * back to the configured default. {@code sensors/+/weather} style broker wildcards are not
 * interpreted - use {@code sensors/*&#47;weather}. The first matching override wins, so list
 * the specific ones first.
 * @author Ilenia Salvadori
 * @since 27.08.2026
 */
class ChannelSettingsResolver {

	private static final Logger logger = Logger.getLogger(ChannelSettingsResolver.class.getName());

	private final ChannelSettings defaults;
	private final List<ChannelOverride> overrides;

	/**
	 * @param defaults the settings for every channel no override matches. Parameter must not
	 * be <code>null</code>
	 * @param configured the override strings, in precedence order. May be <code>null</code>;
	 * an entry that cannot be read is logged and skipped rather than failing the whole
	 * configuration
	 */
	ChannelSettingsResolver(ChannelSettings defaults, String[] configured) {
		this.defaults = defaults;
		this.overrides = parse(defaults, configured);
	}

	/**
	 * @param source the channel. Parameter must not be <code>null</code>
	 * @return the settings to collect that channel with. Never <code>null</code>
	 */
	ChannelSettings settingsFor(String source) {
		for (ChannelOverride override : overrides) {
			if (override.pattern.matcher(source).matches()) {
				return override.settings;
			}
		}
		return defaults;
	}

	/**
	 * @return how many overrides are in effect, for the activation log
	 */
	int overrideCount() {
		return overrides.size();
	}

	private static List<ChannelOverride> parse(ChannelSettings defaults, String[] configured) {
		List<ChannelOverride> parsed = new ArrayList<>();
		if (configured == null) {
			return List.copyOf(parsed);
		}
		for (String entry : configured) {
			if (entry == null || entry.isBlank()) {
				continue;
			}
			try {
				parsed.add(parse(defaults, entry.trim()));
			} catch (RuntimeException e) {
				logger.warning(String.format(
						"Ignoring the payload sampling override '%s' - %s. Expected "
								+ "<channel glob>;samples=N;quiet=K;maxWait=seconds;ring=N",
						entry, e.getMessage()));
			}
		}
		return List.copyOf(parsed);
	}

	private static ChannelOverride parse(ChannelSettings defaults, String entry) {
		String[] parts = entry.split(";");
		String glob = parts[0].trim();
		if (glob.isEmpty()) {
			throw new IllegalArgumentException("the channel glob is missing");
		}
		int samples = defaults.targetSamples();
		int quiet = defaults.quietSamples();
		long maxWait = defaults.maxWait().toSeconds();
		int ring = 0;
		for (int i = 1; i < parts.length; i++) {
			String setting = parts[i].trim();
			if (setting.isEmpty()) {
				continue;
			}
			int equals = setting.indexOf('=');
			if (equals < 0) {
				throw new IllegalArgumentException("'" + setting + "' is not a key=value setting");
			}
			String key = setting.substring(0, equals).trim().toLowerCase(Locale.ROOT);
			long value = value(setting.substring(equals + 1).trim());
			switch (key) {
			case "samples" -> samples = (int) value;
			case "quiet" -> quiet = (int) value;
			case "maxwait" -> maxWait = value;
			case "ring" -> ring = (int) value;
			default -> throw new IllegalArgumentException("'" + key + "' is not a known setting");
			}
		}
		return new ChannelOverride(toPattern(glob), ChannelSettings.of(samples, quiet, maxWait, ring));
	}

	private static long value(String value) {
		try {
			return Long.parseLong(value);
		} catch (NumberFormatException e) {
			throw new IllegalArgumentException("'" + value + "' is not a number");
		}
	}

	/**
	 * Turns a glob into an anchored regex. Everything but {@code *} and {@code ?} is quoted,
	 * so a topic's slashes, dots and {@code +} characters cannot be read as regex syntax.
	 */
	private static Pattern toPattern(String glob) {
		StringBuilder regex = new StringBuilder();
		StringBuilder literal = new StringBuilder();
		for (char c : glob.toCharArray()) {
			if (c == '*' || c == '?') {
				if (!literal.isEmpty()) {
					regex.append(Pattern.quote(literal.toString()));
					literal.setLength(0);
				}
				regex.append(c == '*' ? ".*" : ".");
			} else {
				literal.append(c);
			}
		}
		if (!literal.isEmpty()) {
			regex.append(Pattern.quote(literal.toString()));
		}
		return Pattern.compile(regex.toString());
	}

	/** One parsed override: the channel pattern it matches, and what it configures. */
	private record ChannelOverride(Pattern pattern, ChannelSettings settings) {
	}

}
