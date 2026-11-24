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

import org.eclipse.fennec.event.atlas.mapping.ExpressionEvaluator;
import org.eclipse.fennec.event.atlas.mapping.SensinactMapperConstants;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.condition.Condition;

/**
 * Published while an {@link ExpressionEvaluator} is deployed, so that a component which must
 * not run without one can wait for it.
 * <p>
 * <b>Why a Condition and not a reference.</b> {@link InstancePusherImpl} binds its evaluator
 * as an <em>optional dynamic</em> reference, and deliberately so: the mapping bundle doubles
 * as the metamodel carrier and has to resolve where no expression language is deployed, which
 * is what {@link ExpressionEvaluator} promises. So the pusher is satisfied with no evaluator,
 * every southbound adapter behind it is satisfied too, and a payload that arrives in the gap
 * is mapped with its expressions producing nothing - {@code ValueMapperImpl} falls back and
 * logs FINE, which at the default level is nothing at all. Making the reference mandatory
 * would close the gap and break that promise; a Condition leaves the choice to the
 * deployment.
 * <p>
 * <b>What a deployment does with it.</b> Every component has one implicit
 * {@code osgi.ds.satisfying.condition} reference (OSGi cmpn 112.3.13), defaulted to the
 * framework's True condition, and it is settable from configuration. A runtime whose mappings
 * carry expressions points the pusher's at this one:
 * <pre>
 * "event.atlas.instance.pusher": {
 *     "osgi.ds.satisfying.condition.target":
 *         "(osgi.condition.id=org.eclipse.fennec.event.atlas.mapping.expression.evaluator)"
 * }
 * </pre>
 * and nothing is pushed until expressions can actually be evaluated. A runtime that writes
 * nothing is unaffected, which is why this is a Condition and not a cardinality.
 * <p>
 * <b>It gates the whole pipeline, in both directions.</b> {@code InstancePusher} is a
 * mandatory reference of every ingress - {@code PayloadIngestImpl} and the adapters above it -
 * so the target above holds all of them back, not only the mappings that carry expressions.
 * And the reference below is static: if the evaluator goes away, this Condition goes with it
 * and SCR deactivates everything waiting on it (112.5.9). Where the evaluator is an OCL one,
 * that chain reaches further than it looks - OclExpressionEvaluator takes its OclEngine the
 * same way, and an engine gated on a named OclOperationProvider takes that the same way again.
 *
 * @see SensinactMapperConstants#EXPRESSION_EVALUATOR_CONDITION_ID the id this registers under
 */
@Component(service = Condition.class, property = Condition.CONDITION_ID + "="
		+ SensinactMapperConstants.EXPRESSION_EVALUATOR_CONDITION_ID)
public class ExpressionEvaluatorReadyCondition implements Condition {

	/**
	 * The reference IS the condition - mandatory and static, so this component is satisfied
	 * only while an evaluator is there, and the service it publishes disappears with it.
	 *
	 * @param expressionEvaluator the evaluator whose presence this condition reports; not
	 *            called from here, since the mapper is what evaluates and DS is what reads the
	 *            condition
	 */
	@Activate
	public ExpressionEvaluatorReadyCondition(@Reference ExpressionEvaluator expressionEvaluator) {
		// nothing to do: being activated is the whole behaviour
	}
}
