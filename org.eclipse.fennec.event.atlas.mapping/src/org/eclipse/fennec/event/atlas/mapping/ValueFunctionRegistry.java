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

import org.eclipse.fennec.event.atlas.mapping.functions.ValueFunction;
import org.osgi.annotation.versioning.ProviderType;

/**
 * Applies the {@link ValueFunction} a mapping names, and decides what happens when it cannot.
 * <p>
 * An unknown id **drops** the value, where an unknown converter id passes it through. The two
 * differ because the inputs differ: a converter is handed a finished value, so the unconverted
 * value is still a reading, while a function may be handed the whole source object, and no
 * identity answer exists for that. A dropped value then meets the mapping's own fallback - a
 * NameMapping's literal, a TimestampMapping's literal, a ValueMapping's constant.
 */
@ProviderType
public interface ValueFunctionRegistry {

	/**
	 * Applies the function the given id names.
	 *
	 * @param functionId the id a mapping's {@code functionId} carries; a <code>null</code> or
	 *        blank id means no function was asked for and the input is returned unchanged
	 * @param input the source EObject, or the value the feature path produced
	 * @return the produced value, or an empty optional when there is none
	 */
	Optional<Object> apply(String functionId, Object input);
}
