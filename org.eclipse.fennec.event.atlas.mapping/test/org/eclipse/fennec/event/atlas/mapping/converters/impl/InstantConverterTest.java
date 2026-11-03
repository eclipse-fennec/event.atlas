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
package org.eclipse.fennec.event.atlas.mapping.converters.impl;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;

import java.time.Instant;
import java.util.Date;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/**
 * The timestamp reading that used to sit inside ValueMapperImpl, now testable on its own.
 */
public class InstantConverterTest {

	private static final Instant WHEN = Instant.parse("2026-11-03T08:00:00Z");

	@Test
	@DisplayName("An Instant is already what we want")
	void instant_isItself() {
		assertSame(WHEN, new InstantConverter(null).toInstant(WHEN));
	}

	@Test
	@DisplayName("A java.util.Date is converted")
	void date_isConverted() {
		assertEquals(WHEN, new InstantConverter(null).toInstant(Date.from(WHEN)));
	}

	@Test
	@DisplayName("A Long is read as epoch milliseconds")
	void aLong_isEpochMillis() {
		assertEquals(WHEN, new InstantConverter(null).toInstant(WHEN.toEpochMilli()));
	}

	@Test
	@DisplayName("Without a pattern a string is read as an ISO-8601 instant")
	void noPattern_readsIso() {
		assertEquals(WHEN, new InstantConverter(null).toInstant("2026-11-03T08:00:00Z"));
	}

	@Test
	@DisplayName("A pattern reads what ISO cannot")
	void pattern_readsTheDeviceFormat() {
		assertEquals(WHEN, new InstantConverter("yyyy-MM-dd HH:mm:ss").toInstant("2026-11-03 08:00:00"));
	}

	@Test
	@DisplayName("A pattern that does not fit this value falls back to ISO")
	void patternThatDoesNotFit_fallsBackToIso() {
		assertEquals(WHEN, new InstantConverter("yyyy-MM-dd HH:mm:ss").toInstant("2026-11-03T08:00:00Z"),
				"a payload may carry a mix, and a mapping names one pattern");
	}

	@Test
	@DisplayName("An unusable pattern is ignored rather than failing the timestamp")
	void unusablePattern_leavesIsoParsing() {
		assertEquals(WHEN, new InstantConverter("not a pattern at all").toInstant("2026-11-03T08:00:00Z"));
	}

	@Test
	@DisplayName("A string neither the pattern nor ISO can read is no instant")
	void unparseableString_isNull() {
		assertNull(new InstantConverter("yyyy-MM-dd").toInstant("not a timestamp"));
	}

	@Test
	@DisplayName("A type that is no timestamp at all is no instant")
	void unrelatedType_isNull() {
		assertNull(new InstantConverter(null).toInstant(42.5d));
		assertNull(new InstantConverter(null).toInstant(null));
	}

	@Test
	@DisplayName("As a ValueConverter it answers the same thing")
	void asValueConverter_isTheSame() {
		assertEquals(WHEN, new InstantConverter("yyyy-MM-dd HH:mm:ss").convert("2026-11-03 08:00:00"));
		assertNull(new InstantConverter(null).convert("not a timestamp"),
				"null drops the value, which is what an unreadable timestamp should do");
	}
}
