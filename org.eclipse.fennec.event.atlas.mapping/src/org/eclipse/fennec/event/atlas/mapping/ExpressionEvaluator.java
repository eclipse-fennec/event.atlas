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

import org.eclipse.emf.ecore.EObject;
import org.osgi.annotation.versioning.ProviderType;

/**
 * Evaluates the expression a {@code FeatureMapping} carries against the payload.
 * <p>
 * The expression is the first producer in the chain: it wins over a {@code functionId} and over
 * the plain {@code featurePath}, and a {@code converterId} then post-processes its result. It
 * reads the source object itself, so it reaches values a feature path cannot name - an
 * arithmetic combination of two features, a collection reduced to one number, a conditional.
 * <p>
 * The language is OCL, and <b>nothing in this bundle implements it</b>. The mapping bundle
 * doubles as the metamodel carrier and has to resolve where no evaluator is deployed, so the
 * implementation lives in its own bundle ({@code …event.atlas.mapping.ocl}) and arrives as a
 * service. Where none is, an expression produces nothing and the mapping's own fallback
 * answers - the same thing an unreadable feature path does.
 * <p>
 * Implementations must be thread-safe: one evaluator serves every payload.
 */
@ProviderType
public interface ExpressionEvaluator {

	/**
	 * Evaluates an expression against the payload that is being mapped.
	 * <p>
	 * {@code self} is the source object, so {@code self.temperature * 1.8 + 32} reads the
	 * payload's own features. An expression that cannot be parsed, that fails, or that yields
	 * the OCL {@code null} or {@code invalid} value produces no value rather than an error:
	 * one unmappable resource must not cost the payload its other resources.
	 *
	 * @param expression the expression text a mapping carries; a <code>null</code> or blank one
	 *        means none was asked for and yields an empty optional
	 * @param self the source EObject the expression is evaluated on
	 * @return the computed value, or an empty optional when there is none
	 */
	Optional<Object> evaluate(String expression, EObject self);
}
