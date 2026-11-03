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

import static java.util.Objects.nonNull;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.Date;
import java.util.logging.Logger;

import org.eclipse.fennec.event.atlas.mapping.converters.ValueConverter;

/**
 * Reads whatever a feature path produced as an {@link Instant}.
 * <p>
 * This is what a {@code TimestampMapping} does to the value it found, lifted out of the mapper
 * so that it is one thing with one set of tests. It is a {@link ValueConverter} but deliberately
 * not a component: a {@code TimestampMapping} carries its own pattern in the model and must keep
 * working in any runtime that has this bundle, with nothing deployed or configured. Registering
 * an instance of this class on the whiteboard is a separate decision and costs nothing extra.
 * <p>
 * {@code Instant}, {@link Date} and a {@code Long} of epoch milliseconds are taken as they are.
 * A string is read with the pattern when one was given, and as an ISO-8601 instant otherwise -
 * or as a fallback when the pattern does not fit.
 */
public class InstantConverter implements ValueConverter {

	private static final Logger logger = Logger.getLogger(InstantConverter.class.getName());

	/** The compiled pattern, or <code>null</code> when there is none to use. */
	private final DateTimeFormatter formatter;
	private final String pattern;

	/**
	 * @param hint a {@link DateTimeFormatter} pattern, or <code>null</code>/blank for ISO-8601.
	 *        An unusable pattern is reported and then ignored, leaving ISO parsing - the same
	 *        outcome as a pattern that simply does not fit the value, and never a failure that
	 *        would cost the whole payload its timestamp.
	 */
	public InstantConverter(String hint) {
		this.pattern = nonNull(hint) && !hint.isBlank() ? hint.trim() : null;
		DateTimeFormatter compiled = null;
		if (pattern != null) {
			try {
				compiled = DateTimeFormatter.ofPattern(pattern);
			} catch (IllegalArgumentException e) {
				logger.warning(String.format("'%s' is not a usable timestamp pattern, reading as ISO-8601 instead: %s",
						pattern, e.getMessage()));
			}
		}
		this.formatter = compiled;
	}

	/**
	 * @return the value as an Instant, or <code>null</code> when it cannot be read as one -
	 *         which drops it, here and on the whiteboard alike
	 */
	@Override
	public Object convert(Object value) {
		return toInstant(value);
	}

	/**
	 * The typed form of {@link #convert(Object)}, for callers that already know they want an
	 * Instant.
	 *
	 * @param rawValue the value a feature path produced, or a static literal
	 * @return the instant, or <code>null</code> if this value is not one
	 */
	public Instant toInstant(Object rawValue) {
		if (rawValue instanceof Instant instant) {
			return instant;
		}
		if (rawValue instanceof Date date) {
			return date.toInstant();
		}
		if (rawValue instanceof Long millis) {
			// Assume milliseconds since epoch
			return Instant.ofEpochMilli(millis);
		}
		if (rawValue instanceof String text) {
			return parse(text);
		}
		logger.warning(String.format("Cannot convert timestamp value of type %s to Instant: %s",
				rawValue == null ? "null" : rawValue.getClass().getName(), rawValue));
		return null;
	}

	private Instant parse(String text) {
		if (formatter != null) {
			try {
				return LocalDateTime.parse(text, formatter).toInstant(ZoneOffset.UTC);
			} catch (Exception e) {
				// The pattern did not fit. ISO is still worth a try - a payload may carry a
				// mix, and a mapping names one pattern.
				try {
					return Instant.parse(text);
				} catch (Exception isoException) {
					logger.warning(String.format(
							"Failed to parse timestamp string '%s' using pattern '%s' and as ISO instant: %s",
							text, pattern, e.getMessage()));
					return null;
				}
			}
		}
		try {
			return Instant.parse(text);
		} catch (Exception e) {
			logger.warning(String.format("Failed to parse timestamp string '%s' as ISO instant: %s",
					text, e.getMessage()));
			return null;
		}
	}
}
