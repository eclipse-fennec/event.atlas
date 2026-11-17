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

import java.util.ArrayList;
import java.util.List;

import org.eclipse.fennec.event.atlas.mapping.SourceFilter;
import org.eclipse.fennec.event.atlas.model.mapping.MappingPackage;
import org.eclipse.fennec.event.atlas.model.mapping.MappingSelector;

import io.cloudevents.model.ce.CloudEvent;

/**
 * A {@link MappingSelector} with its source filters compiled, and the one place that decides
 * whether a mapping applies to a message.
 * <p>
 * A selector is a statement about the <em>envelope</em>: which sources a mapping serves, which
 * event type, which payload schema. Every condition it sets must hold; a condition it leaves
 * unset says nothing and is not a condition. Across mappings nothing is ranked - every mapping
 * whose selector matches applies, exactly as every mapping of an EClass applies without one.
 * <p>
 * <b>A selector never matches a push that carries no envelope.</b> There is no source, no type
 * and no schema to compare, so nothing can match - it is an ordinary non-match rather than a
 * special case. A mapping that is meant for a programmatic push too simply carries no selector,
 * which is what every mapping shipped here does.
 * @author Ilenia Salvadori
 */
final class CompiledSelector {

	private final List<SourceFilter> includes;
	private final List<SourceFilter> excludes;
	/** The type the envelope must carry, or <code>null</code> when the selector names none. */
	private final String requiredType;
	/** The schema the envelope must carry, or <code>null</code> when the selector names none. */
	private final String requiredDataschema;

	private CompiledSelector(List<SourceFilter> includes, List<SourceFilter> excludes, String requiredType,
			String requiredDataschema) {
		this.includes = includes;
		this.excludes = excludes;
		this.requiredType = requiredType;
		this.requiredDataschema = requiredDataschema;
	}

	/**
	 * Compiles a selector, refusing one that cannot do its job.
	 * <p>
	 * Both refusals are loud on purpose, because the failure they prevent is silent: a filter
	 * that never parses matches nothing and takes a mapping out of service without saying so,
	 * and an empty selector matches every message while still excluding the mapping from a
	 * programmatic push - a mapping that behaves like two different mappings depending on how
	 * it is reached.
	 * @param selector the selector to compile. Parameter must not be <code>null</code>
	 * @return the compiled selector. Never <code>null</code>
	 * @throws IllegalArgumentException if the selector is empty or carries a filter that is
	 * not a valid source filter. The message names what is wrong, because the caller reports
	 * it against the mapping that carries it
	 */
	static CompiledSelector compile(MappingSelector selector) {
		List<SourceFilter> includes = compileAll(selector.getSource(), MappingPackage.Literals.MAPPING_SELECTOR__SOURCE.getName());
		List<SourceFilter> excludes = compileAll(selector.getSourceExcludes(), MappingPackage.Literals.MAPPING_SELECTOR__SOURCE_EXCLUDES.getName());
		String requiredType = blankToNull(selector.getType());
		String requiredDataschema = blankToNull(selector.getDataschema());
		if (includes.isEmpty() && excludes.isEmpty() && requiredType == null && requiredDataschema == null) {
			throw new IllegalArgumentException(
					"the selector sets no condition - remove it, or name what it selects. An empty selector "
							+ "matches every message and still takes the mapping out of a push that carries no "
							+ "envelope");
		}
		return new CompiledSelector(includes, excludes, requiredType, requiredDataschema);
	}

	private static List<SourceFilter> compileAll(List<String> filters, String attribute) {
		List<SourceFilter> compiled = new ArrayList<>(filters.size());
		for (int i = 0; i < filters.size(); i++) {
			String filter = filters.get(i);
			if (filter == null || filter.isBlank()) {
				throw new IllegalArgumentException(
						String.format("%s[%s] is empty - a filter that matches nothing is never what was meant",
								attribute, i));
			}
			try {
				compiled.add(SourceFilter.compile(filter.trim()));
			} catch (IllegalArgumentException e) {
				throw new IllegalArgumentException(String.format("%s[%s]: %s", attribute, i, e.getMessage()), e);
			}
		}
		return List.copyOf(compiled);
	}

	/**
	 * @param envelope the message's envelope, or <code>null</code> for a push that has none
	 * @return whether the mapping carrying this selector applies to that message
	 */
	boolean matches(CloudEvent envelope) {
		if (envelope == null) {
			return false;
		}
		String source = envelope.getSource();
		// an include list says "only these"; no include list says nothing about the source
		if (!includes.isEmpty() && includes.stream().noneMatch(filter -> filter.matches(source))) {
			return false;
		}
		// an exclude beats every include, which is what lets a general mapping name the one
		// site a specific mapping replaces - a topic filter itself cannot say "except"
		if (excludes.stream().anyMatch(filter -> filter.matches(source))) {
			return false;
		}
		if (requiredType != null && !requiredType.equals(envelope.getType())) {
			return false;
		}
		if (requiredDataschema == null) {
			// the selector names no schema, so the schema is not a condition at all
			return true;
		}
		// the selector names one, so an envelope that carries none fails it: a selector naming
		// a schema is saying which payload it is for, and silence does not answer that. Hence
		// equals on the required value, which is never null here, rather than on the
		// envelope's, which may be
		return requiredDataschema.equals(envelope.getDataschema());
	}

	private static String blankToNull(String value) {
		return value == null || value.isBlank() ? null : value.trim();
	}

}
