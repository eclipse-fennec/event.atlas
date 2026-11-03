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

import org.eclipse.fennec.event.atlas.mapping.converters.ValueConverter;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.ConfigurationPolicy;
import org.osgi.service.metatype.annotations.AttributeDefinition;
import org.osgi.service.metatype.annotations.Designate;
import org.osgi.service.metatype.annotations.ObjectClassDefinition;

/**
 * Scales a numeric value: <code>result = value * factor + offset</code>.
 * <p>
 * One configurable component rather than a drawer full of named arithmetic - a Fahrenheit
 * converter, a millivolt converter, a raw-counts converter are the same operation with
 * different numbers, and whichever fixed set we shipped would be missing the one someone needs.
 * <p>
 * Fahrenheit to Celsius is <code>factor = 0.5555555555555556</code> (5/9) and
 * <code>offset = -17.77777777777778</code> (-32 * 5/9); millivolts to volts is
 * <code>factor = 0.001</code>; a tenths-of-a-degree integer is <code>factor = 0.1</code>.
 * <p>
 * The component is a factory and requires configuration, so a runtime that configures none
 * publishes no converter at all. The configuration's
 * {@code event.atlas.converter.id} is what a mapping's {@code converterId} names.
 */
@Component(service = ValueConverter.class, configurationPid = LinearValueConverter.PID,
		configurationPolicy = ConfigurationPolicy.REQUIRE)
@Designate(ocd = LinearValueConverter.Config.class, factory = true)
public class LinearValueConverter implements ValueConverter {

	/** Factory pid: one configuration per scaling a runtime needs. */
	static final String PID = "event.atlas.converter.linear";

	@ObjectClassDefinition(name = "Event Atlas linear value converter",
			description = "Scales a mapped numeric value: result = value * factor + offset.")
	public @interface Config {

		@AttributeDefinition(name = "Converter id",
				description = "The id a mapping's converterId names to select this converter.")
		String event_atlas_converter_id();

		@AttributeDefinition(name = "Factor", description = "Multiplied with the value. 1 leaves it alone.")
		double factor() default 1.0d;

		@AttributeDefinition(name = "Offset",
				description = "Added after the multiplication. 0 leaves the scaled value alone.")
		double offset() default 0.0d;
	}

	private double factor;
	private double offset;

	@Activate
	void activate(Config config) {
		this.factor = config.factor();
		this.offset = config.offset();
	}

	/**
	 * @return the scaled value as a Double
	 * @throws IllegalArgumentException if the value is not a number, which drops it - scaling
	 *         something unscalable is a mapping that does not fit the payload, not a value to
	 *         pass on unchanged
	 */
	@Override
	public Object convert(Object value) {
		return toDouble(value) * factor + offset;
	}

	private double toDouble(Object value) {
		if (value instanceof Number number) {
			return number.doubleValue();
		}
		if (value instanceof String text) {
			try {
				return Double.parseDouble(text.trim());
			} catch (NumberFormatException e) {
				throw new IllegalArgumentException("'" + text + "' is not a number this converter can scale", e);
			}
		}
		throw new IllegalArgumentException("Cannot scale a value of type "
				+ (value == null ? "null" : value.getClass().getName()));
	}
}
