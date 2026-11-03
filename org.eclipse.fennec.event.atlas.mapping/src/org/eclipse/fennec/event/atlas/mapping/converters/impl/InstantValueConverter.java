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
 * Makes {@link InstantConverter} available on the converter whiteboard.
 * <p>
 * The two classes have different purposes. {@link InstantConverter} is a plain class. A
 * {@code TimestampMapping} uses it directly, with the pattern from the model, and needs no
 * configuration. This component makes the same operation available to any mapping that names a
 * {@code converterId}. Use it for a resource value that holds a date, because
 * {@code TimestampMapping.hint} applies to a timestamp only.
 * <p>
 * The component is a factory and requires configuration. A runtime that configures no instance
 * publishes no converter.
 */
@Component(service = ValueConverter.class, configurationPid = InstantValueConverter.PID,
		configurationPolicy = ConfigurationPolicy.REQUIRE)
@Designate(ocd = InstantValueConverter.Config.class, factory = true)
public class InstantValueConverter implements ValueConverter {

	/** Factory pid: one configuration for each date format a runtime reads. */
	static final String PID = "event.atlas.converter.instant";

	@ObjectClassDefinition(name = "Event Atlas instant converter",
			description = "Reads a mapped value as an instant. The value can be an Instant, a Date, "
					+ "a Long of epoch milliseconds, or a string.")
	public @interface Config {

		@AttributeDefinition(name = "Converter id",
				description = "The id a mapping's converterId names to select this converter.")
		String event_atlas_converter_id();

		@AttributeDefinition(name = "Pattern", required = false,
				description = "A java.time.format.DateTimeFormatter pattern for a string value. "
						+ "If it is empty, the converter reads a string as an ISO-8601 instant.")
		String pattern() default "";
	}

	private InstantConverter converter;

	@Activate
	void activate(Config config) {
		// The pattern is compiled once here, and not for each value.
		this.converter = new InstantConverter(config.pattern());
	}

	@Override
	public Object convert(Object value) {
		return converter.convert(value);
	}
}
