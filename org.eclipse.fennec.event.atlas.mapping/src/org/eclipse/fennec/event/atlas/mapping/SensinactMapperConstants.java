/**
 * Copyright (c) 2012 - 2025 Data In Motion and others.
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

/**
 * 
 * @author ilenia
 * @since Nov 11, 2025
 */
public interface SensinactMapperConstants {
	
	String SENSINACT_MAPPING_ANNOTATION_SOURCE = "sensinact.mapping";
	
	String SENSINACT_MAPPING_UNIT_DESCRIPTION = "sensinact.mapping.unit.description";
	
	String SENSINACT_MAPPING_UNIT = "sensinact.mapping.unit";
	
	String SENSINACT_MAPPING_DESCRIPTION = "sensinact.mapping.description";
	
	String SENSINACT_MAPPING_METADATA_ANNOTATION_SOURCE = "sensinact.mapping.metadata";

	/**
	 * {@code osgi.condition.id} of the Condition published while an
	 * {@link ExpressionEvaluator} is deployed.
	 * <p>
	 * A runtime whose mappings carry expressions points the instance pusher's satisfying
	 * condition at this, on PID {@code event.atlas.instance.pusher}, so that nothing is pushed
	 * before expressions can be evaluated. See
	 * {@code org.eclipse.fennec.event.atlas.mapping.impl.ExpressionEvaluatorReadyCondition}.
	 */
	String EXPRESSION_EVALUATOR_CONDITION_ID = "org.eclipse.fennec.event.atlas.mapping.expression.evaluator";

	/**
	 * Configuration PID of the instance pusher, so that a deployment can reach its component
	 * properties - in particular {@code osgi.ds.satisfying.condition.target} - without naming
	 * an implementation class.
	 */
	String INSTANCE_PUSHER_PID = "event.atlas.instance.pusher";
}
