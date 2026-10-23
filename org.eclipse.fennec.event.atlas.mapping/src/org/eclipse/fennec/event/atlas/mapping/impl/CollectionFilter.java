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
package org.eclipse.fennec.event.atlas.mapping.impl;

import java.util.List;
import java.util.Optional;
import java.util.logging.Logger;

import org.eclipse.emf.common.util.Enumerator;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.EStructuralFeature;
import org.eclipse.fennec.event.atlas.model.mapping.FeatureMapping;

/**
 * Picks one element out of a multi-valued feature by content rather than by position: the
 * {@code collectionFilter} of a {@link FeatureMapping}.
 * <p>
 * The expression is <code>&lt;path&gt;=&lt;literal&gt;</code>. The path is a dot-separated
 * chain of feature <em>names</em> read from the collection element, so an EMap entry - which
 * EMF models as an element with a {@code key} and a {@code value} feature - is addressed as
 * {@code key=line_5} or {@code value.line_number=5}. Comparison is on the string form of the
 * value, so {@code 5} matches an {@code EInt} 5 and a literal may be quoted.
 * <p>
 * Position is unusable wherever the payload is a JSON object: its members arrive in whatever
 * order the sender wrote them (the iotraffic camera scrambles its lines), and a device that
 * splits its readings over several uplinks sends a different subset each time. A filter that
 * matches nothing is therefore not an error - it means this payload does not carry that
 * element - which is what lets a mapping declare every line it ever expects and have each
 * uplink update the ones it brought.
 */
final class CollectionFilter {

	private static final Logger logger = Logger.getLogger(CollectionFilter.class.getName());

	private final String[] path;
	private final String expected;

	private CollectionFilter(String[] path, String expected) {
		this.path = path;
		this.expected = expected;
	}

	/**
	 * @param featureMapping the mapping whose {@code collectionFilter} to read; may be null
	 * @return the parsed filter, or an empty optional when none is set or it is malformed -
	 *         in both cases the caller falls back to {@code collectionIndex}
	 */
	static Optional<CollectionFilter> of(FeatureMapping featureMapping, String context) {
		if (featureMapping == null) {
			return Optional.empty();
		}
		String expression = featureMapping.getCollectionFilter();
		if (expression == null || expression.isBlank()) {
			return Optional.empty();
		}
		int equals = expression.indexOf('=');
		if (equals < 1) {
			logger.warning(String.format(
					"Ignoring collection filter '%s' (%s): expected <featurePath>=<value>, using collectionIndex",
					expression, context));
			return Optional.empty();
		}
		String[] path = expression.substring(0, equals).trim().split("\\.");
		String expected = unquote(expression.substring(equals + 1).trim());
		for (String segment : path) {
			if (segment.isBlank()) {
				logger.warning(String.format(
						"Ignoring collection filter '%s' (%s): empty path segment, using collectionIndex",
						expression, context));
				return Optional.empty();
			}
		}
		return Optional.of(new CollectionFilter(path, expected));
	}

	/**
	 * @return the first matching element, or an empty optional when the collection carries none
	 */
	Optional<Object> select(List<?> collection) {
		return collection.stream().map(Object.class::cast).filter(this::matches).findFirst();
	}

	boolean matches(Object element) {
		if (!(element instanceof EObject)) {
			return false;
		}
		Object value = element;
		for (String segment : path) {
			if (!(value instanceof EObject current)) {
				return false;
			}
			EStructuralFeature feature = current.eClass().getEStructuralFeature(segment);
			if (feature == null) {
				return false;
			}
			value = current.eGet(feature);
		}
		return value != null && expected.equals(literal(value));
	}

	/**
	 * @return the string form a filter literal is compared against: an enum reports its name,
	 *         matching how {@code ValueMapperImpl} hands enum values to the twin
	 */
	private static String literal(Object value) {
		return value instanceof Enumerator enumerator ? enumerator.getName() : String.valueOf(value);
	}

	private static String unquote(String literal) {
		if (literal.length() > 1 && (literal.startsWith("\"") && literal.endsWith("\"")
				|| literal.startsWith("'") && literal.endsWith("'"))) {
			return literal.substring(1, literal.length() - 1);
		}
		return literal;
	}

	@Override
	public String toString() {
		return String.join(".", path) + "=" + expected;
	}
}
