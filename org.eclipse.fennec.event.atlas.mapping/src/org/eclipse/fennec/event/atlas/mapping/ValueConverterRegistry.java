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
package org.eclipse.fennec.event.atlas.mapping;

import java.util.Optional;

import org.eclipse.fennec.event.atlas.mapping.converters.ValueConverter;
import org.osgi.annotation.versioning.ProviderType;

/**
 * Applies the {@link ValueConverter} a mapping names, and decides what happens when it cannot.
 * <p>
 * Looking a converter up and applying it are one call, so that the two failure modes are
 * answered in one place rather than at every call site:
 * <ul>
 * <li>an <em>unknown</em> id is a configuration gap - the value passes through unconverted and
 * the id is logged once, so a typo does not silently empty a resource;</li>
 * <li>a converter that <em>throws</em>, or returns <code>null</code>, has no result to offer -
 * the value is dropped and the resource keeps what it had.</li>
 * </ul>
 */
@ProviderType
public interface ValueConverterRegistry {

	/**
	 * Converts a value with the converter the given id names.
	 *
	 * @param converterId the id a mapping's {@code converterId} carries; a <code>null</code> or
	 *        blank id means no conversion was asked for and the value is returned unchanged
	 * @param value the produced value
	 * @return the value to push, or an empty optional to drop it
	 */
	Optional<Object> convert(String converterId, Object value);
}
