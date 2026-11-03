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
package org.eclipse.fennec.event.atlas.mapping.functions.impl;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Map;

import org.eclipse.fennec.event.atlas.mapping.functions.ValueFunction;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

public class ValueFunctionRegistryImplTest {

	private final ValueFunctionRegistryImpl registry = new ValueFunctionRegistryImpl();

	@Test
	@DisplayName("A registered function is applied to the input")
	void registeredFunction_isApplied() {
		register("mark", input -> "produced:" + input);

		assertEquals("produced:raw", registry.apply("mark", "raw").orElseThrow());
	}

	@Test
	@DisplayName("An unknown id produces nothing, where an unknown converter would pass through")
	void unknownId_producesNothing() {
		assertTrue(registry.apply("nobody-registered-this", "raw").isEmpty(),
				"a function may be handed the source object, so there is no identity answer");
	}

	@Test
	@DisplayName("A function that throws produces nothing")
	void throwingFunction_producesNothing() {
		register("boom", input -> {
			throw new IllegalStateException("no");
		});

		assertTrue(registry.apply("boom", "raw").isEmpty());
	}

	@Test
	@DisplayName("A function returning null produces nothing")
	void nullResult_producesNothing() {
		register("swallow", input -> null);

		assertTrue(registry.apply("swallow", "raw").isEmpty());
	}

	@Test
	@DisplayName("No functionId means no function was asked for")
	void blankId_isNotAFunction() {
		assertEquals("raw", registry.apply(null, "raw").orElseThrow());
		assertEquals("raw", registry.apply("   ", "raw").orElseThrow());
	}

	@Test
	@DisplayName("A function registered without the id property is never selected")
	void functionWithoutIdProperty_isIgnored() {
		registry.addFunction(input -> "produced", Map.of());

		assertTrue(registry.apply("produced", "raw").isEmpty());
	}

	@Test
	@DisplayName("Unregistering a function makes its id unknown again")
	void removedFunction_isGone() {
		ValueFunction function = input -> "produced";
		register("gone", function);
		registry.removeFunction(function, Map.of(ValueFunction.FUNCTION_ID, "gone"));

		assertTrue(registry.apply("gone", "raw").isEmpty());
	}

	private void register(String id, ValueFunction function) {
		registry.addFunction(function, Map.of(ValueFunction.FUNCTION_ID, id));
	}
}
