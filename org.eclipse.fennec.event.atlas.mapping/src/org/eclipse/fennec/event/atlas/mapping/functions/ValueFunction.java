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
package org.eclipse.fennec.event.atlas.mapping.functions;

import org.osgi.annotation.versioning.ConsumerType;

/**
 * Produces a mapped value that a feature path cannot express.
 * <p>
 * A function is a whiteboard service: register an implementation with the {@link #FUNCTION_ID}
 * service property, and a {@code FeatureMapping} selects it by naming that id in its
 * {@code functionId}.
 * <p>
 * What it receives depends on the mapping:
 * <ul>
 * <li>with no {@code featurePath}, the whole source EObject - the function reads whatever it
 * needs, for instance the mean of two sensors;</li>
 * <li>with a {@code featurePath}, the value that path produced.</li>
 * </ul>
 * <p>
 * A function runs before a {@code converterId}, so a mapping can compute a value and then
 * convert it. The difference from a {@link
 * org.eclipse.fennec.event.atlas.mapping.converters.ValueConverter} is the input: only a
 * function is offered the source object.
 * <p>
 * Implementations must be thread-safe and should be free of side effects.
 */
@ConsumerType
public interface ValueFunction {

	/**
	 * Service property naming the id this function answers to. A function registered without it
	 * is never selected, because no {@code functionId} can address it.
	 */
	String FUNCTION_ID = "event.atlas.function.id";

	/**
	 * @param input the source EObject, or the value the feature path produced
	 * @return the produced value, or <code>null</code> to produce none - the mapping then falls
	 *         back the way an unreadable feature path does
	 * @throws RuntimeException if this input yields no value; the outcome is the same as
	 *         <code>null</code>, and the failure is logged
	 */
	Object apply(Object input);
}
