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
package org.eclipse.fennec.event.atlas.mapping;

import java.util.Objects;

/**
 * Matches an ingest source - an MQTT topic, or {@code rest/<channel>} - against an MQTT topic
 * filter.
 * <p>
 * It is the runtime's single answer to "does this source match this pattern", shared by
 * everything that selects on a source: a mapping's selector, and the {@code codec.rootType}
 * entries of {@code event.atlas.southbound.ingest}. One class rather than one matcher per
 * caller, because a mapping author pastes a filter from the broker configuration and must not
 * have to know which of two dialects reads it.
 * <p>
 * The semantics are MQTT's, in full:
 * <ul>
 * <li>a source is a list of levels separated by {@code /};</li>
 * <li><b>{@code +}</b> matches exactly one level, and an empty one counts - {@code a/+/b}
 * matches {@code a//b};</li>
 * <li><b>{@code #}</b> matches the remaining levels <em>and the parent</em>, so {@code a/#}
 * matches {@code a}, {@code a/b} and {@code a/b/c}. It must be the last level;</li>
 * <li>a wildcard occupies a whole level: {@code a/+} is a filter, {@code a+} is not, and
 * neither is {@code a/#/c};</li>
 * <li>everything else is literal and case-sensitive.</li>
 * </ul>
 * <p>
 * Two deliberate departures from a broker's own matching, both because a source here is not
 * always a topic a broker served:
 * <ul>
 * <li>a leading {@code $} is not special. A broker keeps {@code #} from matching its
 * {@code $SYS} tree, which is a rule about what a server should publish to a subscriber, not
 * about what a filter means;</li>
 * <li>{@code *} is an ordinary character, as MQTT has it. It used to stand for "any text" in
 * {@code codec.rootType}, so a pattern carrying one is worth a migration warning where it is
 * read - see {@code PayloadIngestImpl} - but it is not a syntax error, and refusing it would
 * make a legitimate topic unmatchable for good.</li>
 * </ul>
 * <p>
 * Instances are immutable and safe to share.
 * @author Ilenia Salvadori
 */
public final class SourceFilter {

	/** Matches the remaining levels, and the parent of them. */
	private static final String MULTI_LEVEL = "#";

	/** Matches exactly one level, empty included. */
	private static final String SINGLE_LEVEL = "+";

	private static final char SEPARATOR = '/';

	private final String filter;
	private final String[] levels;

	private SourceFilter(String filter, String[] levels) {
		this.filter = filter;
		this.levels = levels;
	}

	/**
	 * Compiles an MQTT topic filter.
	 * @param filter the filter, for example {@code 5g/ilsa/+/signalstate} or {@code rest/#}.
	 * Parameter must not be <code>null</code>
	 * @return the compiled filter. Never <code>null</code>
	 * @throws IllegalArgumentException if the filter is empty or misplaces a wildcard. The
	 * message names the filter and what is wrong with it, because the callers report it to
	 * whoever wrote it - a refused mapping, or an ignored configuration entry
	 */
	public static SourceFilter compile(String filter) {
		Objects.requireNonNull(filter, "Filter must not be null");
		if (filter.isEmpty()) {
			throw new IllegalArgumentException("A source filter must not be empty");
		}
		String[] levels = split(filter);
		for (int i = 0; i < levels.length; i++) {
			String level = levels[i];
			if (level.contains(MULTI_LEVEL)) {
				if (!MULTI_LEVEL.equals(level)) {
					throw new IllegalArgumentException(String.format(
							"Source filter '%s' is invalid: '#' must be a level of its own, as in 'a/b/#'", filter));
				}
				if (i != levels.length - 1) {
					throw new IllegalArgumentException(String.format(
							"Source filter '%s' is invalid: '#' matches the rest of a source, so it must be the "
									+ "last level", filter));
				}
			} else if (level.contains(SINGLE_LEVEL) && !SINGLE_LEVEL.equals(level)) {
				throw new IllegalArgumentException(String.format(
						"Source filter '%s' is invalid: '+' must be a level of its own, as in 'a/+/c'", filter));
			}
		}
		return new SourceFilter(filter, levels);
	}

	/**
	 * @param filter the filter to check. May be <code>null</code>
	 * @return whether {@link #compile(String)} would accept it. For reporting <em>why</em> a
	 * filter is refused, catch the exception instead - it carries the reason
	 */
	public static boolean isValid(String filter) {
		if (filter == null) {
			return false;
		}
		try {
			compile(filter);
			return true;
		} catch (IllegalArgumentException e) {
			return false;
		}
	}

	/**
	 * @param source the ingest source - an MQTT topic, or {@code rest/<channel>}. May be
	 * <code>null</code>, which matches nothing
	 * @return whether this filter matches it
	 */
	public boolean matches(String source) {
		if (source == null) {
			return false;
		}
		String[] sourceLevels = split(source);
		for (int i = 0; i < levels.length; i++) {
			// checked before the length, so that 'a/#' matches 'a' itself - '#' covers the
			// parent level too, which is what makes one filter cover a tree and its root
			if (MULTI_LEVEL.equals(levels[i])) {
				return true;
			}
			if (i >= sourceLevels.length) {
				return false;
			}
			if (!SINGLE_LEVEL.equals(levels[i]) && !levels[i].equals(sourceLevels[i])) {
				return false;
			}
		}
		// no wildcard was left to absorb them, so a longer source is a different source
		return sourceLevels.length == levels.length;
	}

	/**
	 * @return the filter text this was compiled from, for log and refusal messages
	 */
	public String filter() {
		return filter;
	}

	/**
	 * @return whether the filter carries a {@code *}. It is an ordinary character here and
	 * matches only itself, which is not what it meant in {@code codec.rootType} before this
	 * class - a reader that has to warn about the change asks this
	 */
	public boolean hasStar() {
		return filter.indexOf('*') >= 0;
	}

	/**
	 * Splits on the separator, keeping empty levels: {@code /a} is two levels and {@code a//b}
	 * is three, both of which MQTT distinguishes from the shorter source.
	 */
	private static String[] split(String text) {
		return text.split(String.valueOf(SEPARATOR), -1);
	}

	/*
	 * (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return filter;
	}

}
