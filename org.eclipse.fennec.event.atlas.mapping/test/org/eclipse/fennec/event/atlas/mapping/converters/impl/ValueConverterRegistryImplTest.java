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
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Map;
import java.util.Optional;

import org.eclipse.fennec.event.atlas.mapping.converters.ValueConverter;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/**
 * Pins what the registry does with an id, which is where the two failure modes are decided.
 */
public class ValueConverterRegistryImplTest {

	private final ValueConverterRegistryImpl registry = new ValueConverterRegistryImpl();

	@Test
	@DisplayName("A registered converter is applied to the value")
	void registeredConverter_isApplied() {
		register("mark", value -> "converted:" + value);

		assertEquals("converted:raw", registry.convert("mark", "raw").orElseThrow());
	}

	@Test
	@DisplayName("An unknown id passes the value through rather than emptying the resource")
	void unknownId_passesThrough() {
		Object value = new Object();

		assertSame(value, registry.convert("nobody-registered-this", value).orElseThrow());
	}

	@Test
	@DisplayName("A converter that throws has no result, so the value is dropped")
	void throwingConverter_dropsTheValue() {
		register("boom", value -> {
			throw new IllegalStateException("cannot convert " + value);
		});

		assertTrue(registry.convert("boom", 1).isEmpty());
	}

	@Test
	@DisplayName("A converter returning null drops the value too")
	void nullResult_dropsTheValue() {
		register("swallow", value -> null);

		assertTrue(registry.convert("swallow", 1).isEmpty());
	}

	@Test
	@DisplayName("No converterId means no conversion was asked for")
	void blankId_isNotAConversion() {
		assertEquals(7, registry.convert(null, 7).orElseThrow());
		assertEquals(7, registry.convert("   ", 7).orElseThrow());
	}

	@Test
	@DisplayName("A converter registered without the id property is never selected")
	void converterWithoutIdProperty_isIgnored() {
		registry.addConverter(value -> "converted", Map.of());

		// A real id, not a blank one: a blank id short-circuits before the lookup, so asserting
		// on it would pass whether or not the property-less converter had been taken.
		assertEquals("raw", registry.convert("converted", "raw").orElseThrow());
	}

	@Test
	@DisplayName("Unregistering a converter makes its id unknown again")
	void removedConverter_isGone() {
		ValueConverter converter = value -> "converted";
		register("gone", converter);
		registry.removeConverter(converter, Map.of(ValueConverter.CONVERTER_ID, "gone"));

		Optional<Object> result = registry.convert("gone", "raw");

		assertEquals("raw", result.orElseThrow(), "an id with no converter passes the value through");
	}

	private void register(String id, ValueConverter converter) {
		registry.addConverter(converter, Map.of(ValueConverter.CONVERTER_ID, id));
	}
}
