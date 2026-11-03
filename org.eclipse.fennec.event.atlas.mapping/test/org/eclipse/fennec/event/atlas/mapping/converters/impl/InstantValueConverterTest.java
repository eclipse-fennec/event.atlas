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
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.time.Instant;
import java.util.Date;

import org.eclipse.fennec.event.atlas.mapping.converters.impl.InstantValueConverter.Config;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/**
 * The component gives the same answers as the class behind it. InstantConverterTest covers the
 * conversion itself.
 */
public class InstantValueConverterTest {

	private static final Instant WHEN = Instant.parse("2026-11-03T08:00:00Z");

	@Test
	@DisplayName("A configured pattern reads the format of the device")
	void configuredPattern_isUsed() {
		assertEquals(WHEN, component("yyyy-MM-dd HH:mm:ss").convert("2026-11-03 08:00:00"));
	}

	@Test
	@DisplayName("An empty pattern reads an ISO-8601 instant")
	void emptyPattern_readsIso() {
		assertEquals(WHEN, component("").convert("2026-11-03T08:00:00Z"));
	}

	@Test
	@DisplayName("The component takes the other timestamp types too")
	void otherTypes_areTakenAsWell() {
		assertEquals(WHEN, component("").convert(Date.from(WHEN)));
		assertEquals(WHEN, component("").convert(WHEN.toEpochMilli()));
	}

	@Test
	@DisplayName("A value that is no instant is dropped")
	void unreadableValue_isDropped() {
		assertNull(component("").convert("not a timestamp"),
				"null drops the value in the registry, as an unreadable timestamp must");
	}

	private static InstantValueConverter component(String pattern) {
		Config config = mock(Config.class);
		when(config.pattern()).thenReturn(pattern);
		InstantValueConverter converter = new InstantValueConverter();
		converter.activate(config);
		return converter;
	}
}
