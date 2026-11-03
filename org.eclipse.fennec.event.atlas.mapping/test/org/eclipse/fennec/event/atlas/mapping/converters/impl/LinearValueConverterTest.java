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
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import org.eclipse.fennec.event.atlas.mapping.converters.impl.LinearValueConverter.Config;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

public class LinearValueConverterTest {

	@Test
	@DisplayName("A factor alone scales: millivolts to volts")
	void factorOnly_scales() {
		assertEquals(3.3d, (Double) converter(0.001d, 0d).convert(3300), 1e-9);
	}

	@Test
	@DisplayName("Factor and offset together: Fahrenheit to Celsius")
	void factorAndOffset_convertTemperature() {
		LinearValueConverter f2c = converter(5d / 9d, -32d * 5d / 9d);

		assertEquals(100d, (Double) f2c.convert(212), 1e-9);
		assertEquals(0d, (Double) f2c.convert(32), 1e-9);
		assertEquals(-40d, (Double) f2c.convert(-40), 1e-9);
	}

	@Test
	@DisplayName("The defaults leave the value alone")
	void defaults_areIdentity() {
		assertEquals(21.5d, (Double) converter(1.0d, 0.0d).convert(21.5d), 1e-9);
	}

	@Test
	@DisplayName("A numeric string is scaled too")
	void numericString_isScaled() {
		assertEquals(2.0d, (Double) converter(2d, 0d).convert(" 1.0 "), 1e-9);
	}

	@Test
	@DisplayName("Something unscalable is refused, so the value is dropped rather than passed on wrong")
	void unscalableValue_throws() {
		LinearValueConverter converter = converter(2d, 0d);

		assertThrows(IllegalArgumentException.class, () -> converter.convert("not a number"));
		assertThrows(IllegalArgumentException.class, () -> converter.convert(new Object()));
		assertThrows(IllegalArgumentException.class, () -> converter.convert(null));
	}

	private static LinearValueConverter converter(double factor, double offset) {
		Config config = mock(Config.class);
		when(config.factor()).thenReturn(factor);
		when(config.offset()).thenReturn(offset);
		LinearValueConverter converter = new LinearValueConverter();
		converter.activate(config);
		return converter;
	}
}
