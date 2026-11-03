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
package org.eclipse.fennec.event.atlas.mapping.converters;

import org.osgi.annotation.versioning.ConsumerType;

/**
 * Post-processes a mapped value on its way into the digital twin.
 * <p>
 * A converter is a whiteboard service: register an implementation with the
 * {@link #CONVERTER_ID} service property and a {@code FeatureMapping} selects it by naming that
 * id in its {@code converterId}. It runs after the value has been produced - by a feature path
 * today, by an OCL expression or a named function once those exist - and before the value is
 * coerced to the resource's declared type, so an implementation may return a different type
 * than it was handed.
 * <p>
 * Implementations must be thread-safe and should be free of side effects: a converter is called
 * once per mapped value, on whatever thread delivered the payload.
 */
@ConsumerType
public interface ValueConverter {

	/**
	 * Service property naming the id this converter answers to. A converter registered without
	 * it is never selected, because no {@code converterId} can address it.
	 */
	String CONVERTER_ID = "event.atlas.converter.id";

	/**
	 * Converts a single mapped value.
	 *
	 * @param value the produced value, never <code>null</code>
	 * @return the converted value, or <code>null</code> to drop it - the resource keeps the
	 *         value it had
	 * @throws RuntimeException if this value cannot be converted; the value is dropped and the
	 *         failure logged. Throwing is for "I cannot convert this", not for reporting a
	 *         value the mapping should never have sent.
	 */
	Object convert(Object value);
}
