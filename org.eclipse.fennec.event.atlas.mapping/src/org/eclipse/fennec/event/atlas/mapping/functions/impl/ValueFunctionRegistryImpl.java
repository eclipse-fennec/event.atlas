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

import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.eclipse.fennec.event.atlas.mapping.ValueFunctionRegistry;
import org.eclipse.fennec.event.atlas.mapping.functions.ValueFunction;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.component.annotations.ReferenceCardinality;
import org.osgi.service.component.annotations.ReferencePolicy;

/**
 * Tracks the {@link ValueFunction} whiteboard and applies functions by id.
 * <p>
 * Nothing here touches sensinact types: the mapping bundle doubles as the metamodel carrier and
 * must resolve without the gateway.
 */
@Component(service = ValueFunctionRegistry.class)
public class ValueFunctionRegistryImpl implements ValueFunctionRegistry {

	private static final Logger logger = Logger.getLogger(ValueFunctionRegistryImpl.class.getName());

	private final Map<String, ValueFunction> functions = new ConcurrentHashMap<>();

	/** Ids already reported as unknown, so a missing function logs once and not per payload. */
	private final Set<String> reportedUnknownIds = ConcurrentHashMap.newKeySet();

	// Public because this package is internal: the bundle exports …mapping, …mapping.converters
	// and …mapping.functions only. It lets a test drive the real registry.
	@Reference(cardinality = ReferenceCardinality.MULTIPLE, policy = ReferencePolicy.DYNAMIC)
	public void addFunction(ValueFunction function, Map<String, Object> properties) {
		String id = functionId(properties);
		if (id == null) {
			logger.warning(String.format(
					"Ignoring a %s registered without the '%s' service property - no functionId can address it",
					function.getClass().getName(), ValueFunction.FUNCTION_ID));
			return;
		}
		ValueFunction previous = functions.put(id, function);
		if (previous != null) {
			logger.warning(String.format(
					"More than one ValueFunction registered for id '%s' - the later registration wins", id));
		}
		reportedUnknownIds.remove(id);
	}

	public void removeFunction(ValueFunction function, Map<String, Object> properties) {
		String id = functionId(properties);
		if (id != null) {
			functions.remove(id, function);
		}
	}

	@Override
	public Optional<Object> apply(String functionId, Object input) {
		if (functionId == null || functionId.isBlank()) {
			return Optional.ofNullable(input);
		}
		ValueFunction function = functions.get(functionId);
		if (function == null) {
			if (reportedUnknownIds.add(functionId)) {
				logger.warning(String.format(
						"No ValueFunction registered for id '%s' - the mapping produces no value", functionId));
			}
			return Optional.empty();
		}
		try {
			return Optional.ofNullable(function.apply(input));
		} catch (Exception e) {
			logger.log(Level.WARNING, e,
					() -> String.format("ValueFunction '%s' failed - producing no value", functionId));
			return Optional.empty();
		}
	}

	private static String functionId(Map<String, Object> properties) {
		Object id = properties == null ? null : properties.get(ValueFunction.FUNCTION_ID);
		if (id == null) {
			return null;
		}
		String text = id.toString().trim();
		return text.isEmpty() ? null : text;
	}
}
