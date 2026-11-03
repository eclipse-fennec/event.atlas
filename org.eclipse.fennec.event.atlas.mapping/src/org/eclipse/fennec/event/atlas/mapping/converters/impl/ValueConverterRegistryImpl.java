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

import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.eclipse.fennec.event.atlas.mapping.ValueConverterRegistry;
import org.eclipse.fennec.event.atlas.mapping.converters.ValueConverter;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.component.annotations.ReferenceCardinality;
import org.osgi.service.component.annotations.ReferencePolicy;

/**
 * Tracks the {@link ValueConverter} whiteboard and applies converters by id.
 * <p>
 * Nothing here touches sensinact types: the mapping bundle doubles as the metamodel carrier and
 * must resolve without the gateway.
 */
@Component(service = ValueConverterRegistry.class)
public class ValueConverterRegistryImpl implements ValueConverterRegistry {

	private static final Logger logger = Logger.getLogger(ValueConverterRegistryImpl.class.getName());

	private final Map<String, ValueConverter> converters = new ConcurrentHashMap<>();

	/**
	 * Ids already reported as unknown. An id is logged once rather than once per payload - a
	 * mapping that names a missing converter would otherwise flood the log at ingest rate.
	 */
	private final Set<String> reportedUnknownIds = ConcurrentHashMap.newKeySet();

	// The bind methods are public because this package is internal: the bundle exports
	// …mapping and …mapping.converters only, so public here is not API. It lets a test in
	// another package drive the real registry instead of a stand-in.
	@Reference(cardinality = ReferenceCardinality.MULTIPLE, policy = ReferencePolicy.DYNAMIC)
	public void addConverter(ValueConverter converter, Map<String, Object> properties) {
		String id = converterId(properties);
		if (id == null) {
			logger.warning(String.format(
					"Ignoring a %s registered without the '%s' service property - no converterId can address it",
					converter.getClass().getName(), ValueConverter.CONVERTER_ID));
			return;
		}
		ValueConverter previous = converters.put(id, converter);
		if (previous != null) {
			logger.warning(String.format(
					"More than one ValueConverter registered for id '%s' - the later registration wins", id));
		}
		// Let the id be reported again if this converter goes away later.
		reportedUnknownIds.remove(id);
	}

	public void removeConverter(ValueConverter converter, Map<String, Object> properties) {
		String id = converterId(properties);
		if (id != null) {
			converters.remove(id, converter);
		}
	}

	@Override
	public Optional<Object> convert(String converterId, Object value) {
		if (converterId == null || converterId.isBlank()) {
			return Optional.ofNullable(value);
		}
		ValueConverter converter = converters.get(converterId);
		if (converter == null) {
			if (reportedUnknownIds.add(converterId)) {
				logger.warning(String.format(
						"No ValueConverter registered for id '%s' - values pass through unconverted", converterId));
			}
			return Optional.ofNullable(value);
		}
		try {
			return Optional.ofNullable(converter.convert(value));
		} catch (Exception e) {
			logger.log(Level.WARNING, e, () -> String.format(
					"ValueConverter '%s' failed on value %s - dropping it", converterId, value));
			return Optional.empty();
		}
	}

	private static String converterId(Map<String, Object> properties) {
		Object id = properties == null ? null : properties.get(ValueConverter.CONVERTER_ID);
		if (id == null) {
			return null;
		}
		String text = id.toString().trim();
		return text.isEmpty() ? null : text;
	}
}
