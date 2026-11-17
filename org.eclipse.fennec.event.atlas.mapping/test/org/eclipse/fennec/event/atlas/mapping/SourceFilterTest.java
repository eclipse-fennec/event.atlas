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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.ValueSource;

/**
 * MQTT topic filter semantics, which is what {@link SourceFilter} promises a mapping author
 * who pastes a filter out of the broker configuration.
 * <p>
 * The cases are the ones MQTT 3.1.1 §4.7 spells out, plus the two places this class
 * deliberately differs from a broker.
 * @author Ilenia Salvadori
 */
public class SourceFilterTest {

	@Nested
	@DisplayName("Literal filters")
	class Literal {

		@ParameterizedTest
		@CsvSource({ "a/b/c, a/b/c, true", "a/b/c, a/b, false", "a/b, a/b/c, false", "a/b/c, a/b/d, false",
				"A/b, a/b, false", "/a, /a, true", "/a, a, false", "a//b, a//b, true",
				"a//b, a/b, false", "a, '', false" })
		@DisplayName("match the source level by level, empty levels and case included")
		void literal(String filter, String source, boolean expected) {
			assertEquals(expected, SourceFilter.compile(filter).matches(source));
		}
	}

	@Nested
	@DisplayName("'+' matches exactly one level")
	class SingleLevel {

		@ParameterizedTest
		@CsvSource({ "5g/ilsa/+/signalstate, 5g/ilsa/paradiesbruecke/signalstate, true",
				"5g/ilsa/+/signalstate, 5g/ilsa/lobedaer-platz/signalstate, true",
				"5g/ilsa/+/signalstate, 5g/ilsa/a/b/signalstate, false",
				"5g/ilsa/+/signalstate, 5g/ilsa/signalstate, false", "+, a, true", "+, a/b, false",
				"+/+, a/b, true", "rest/+, rest/counter, true", "rest/+, rest/a/b, false" })
		void singleLevel(String filter, String source, boolean expected) {
			assertEquals(expected, SourceFilter.compile(filter).matches(source));
		}

		@Test
		@DisplayName("and an empty level is a level")
		void singleLevel_matchesAnEmptyLevel() {
			// the spec is explicit about it, and a device publishing 'a//b' is otherwise
			// invisible to a filter that looks right
			assertTrue(SourceFilter.compile("a/+/b").matches("a//b"));
		}
	}

	@Nested
	@DisplayName("'#' matches the rest, and the parent")
	class MultiLevel {

		@ParameterizedTest
		@CsvSource({ "5g/ilsa/#, 5g/ilsa/paradiesbruecke/signalstate, true", "5g/ilsa/#, 5g/ilsa/x, true",
				"5g/ilsa/#, 5g/ilsa, true", "5g/ilsa/#, 5g/ilsax, false", "5g/ilsa/#, 5g, false",
				"#, a/b/c, true", "#, a, true", "iotraffic/#, iotraffic/jena/uplink, true" })
		void multiLevel(String filter, String source, boolean expected) {
			assertEquals(expected, SourceFilter.compile(filter).matches(source));
		}

		@Test
		@DisplayName("so one filter covers a tree and its root, which is why '#' is tested before the length")
		void multiLevel_matchesTheParent() {
			assertTrue(SourceFilter.compile("a/#").matches("a"));
			assertTrue(SourceFilter.compile("a/#").matches("a/b"));
		}
	}

	@Nested
	@DisplayName("A malformed filter is refused, with a reason")
	class Malformed {

		@ParameterizedTest
		@ValueSource(strings = { "a/#/c", "a#", "#/a", "a/b#", "a+", "a/b+c", "+a", "" })
		@DisplayName("because an exclude that silently never matches is worse than a refusal")
		void malformed(String filter) {
			// the callers turn this into a collected refusal naming the mapping, or an ignored
			// configuration entry naming itself - never a silent non-match
			IllegalArgumentException e = assertThrows(IllegalArgumentException.class,
					() -> SourceFilter.compile(filter));
			assertTrue(e.getMessage().contains("invalid") || e.getMessage().contains("empty"),
					"the message must say what is wrong: " + e.getMessage());
			assertFalse(SourceFilter.isValid(filter));
		}

		@Test
		@DisplayName("A null filter is refused too, and null never matches")
		void nullHandling() {
			assertThrows(NullPointerException.class, () -> SourceFilter.compile(null));
			assertFalse(SourceFilter.isValid(null));
			assertFalse(SourceFilter.compile("a/#").matches(null));
		}
	}

	@Nested
	@DisplayName("Where this differs from a broker")
	class Departures {

		@Test
		@DisplayName("'*' is an ordinary character, not 'any text' as codec.rootType used to read it")
		void star_isLiteral() {
			SourceFilter filter = SourceFilter.compile("rest/*");

			assertTrue(filter.matches("rest/*"), "it matches itself, as MQTT has it");
			assertFalse(filter.matches("rest/counter"), "it is not a wildcard any more");
			// a reader that has to warn about the change asks this rather than parsing again
			assertTrue(filter.hasStar());
			assertFalse(SourceFilter.compile("rest/+").hasStar());
		}

		@Test
		@DisplayName("A leading '$' is not special - that is a broker's rule about what it serves")
		void dollar_isNotSpecial() {
			assertTrue(SourceFilter.compile("#").matches("$SYS/broker/uptime"));
			assertTrue(SourceFilter.compile("+/broker/uptime").matches("$SYS/broker/uptime"));
		}
	}

	@Test
	@DisplayName("The filter text survives, because the refusal and log messages name it")
	void filterText_isKept() {
		SourceFilter filter = SourceFilter.compile("5g/ilsa/#");

		assertEquals("5g/ilsa/#", filter.filter());
		assertEquals("5g/ilsa/#", filter.toString());
	}

}
