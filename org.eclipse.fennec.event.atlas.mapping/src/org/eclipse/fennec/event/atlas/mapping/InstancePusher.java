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

import org.eclipse.emf.ecore.EObject;
import org.osgi.annotation.versioning.ProviderType;

import io.cloudevents.model.ce.CloudEvent;

/**
 * Ingress for domain model instances: looks up the {@link ProviderMappingRegistry} for
 * mappings matching the instance's EClass and pushes the instance into the sensinact
 * digital twin through each of them.
 * <p>
 * The lookup is keyed by the exact {@link org.eclipse.emf.ecore.EClass} instance, so the
 * incoming object must be created from the same EPackage the registered mappings resolved
 * their provider classes against.
 * @author Ilenia Salvadori
 * @since 29.07.2026
 */
@ProviderType
public interface InstancePusher {

	/**
	 * Pushes a domain model instance into the sensinact digital twin using all registered
	 * {@link org.eclipse.fennec.event.atlas.model.mapping.ProviderMapping}s whose provider
	 * classes match the instance's EClass. Mappings that fail to apply are logged and
	 * skipped; the push is executed synchronously on the sensinact gateway thread.
	 * <p>
	 * No envelope is built here. A programmatic push has no message behind it, so there is no
	 * source to report and nothing an envelope could honestly carry; a caller that has one
	 * uses {@link #pushInstance(io.cloudevents.model.ce.CloudEvent)} instead.
	 * @param instance the instance to push. Parameter must not be <code>null</code>
	 * @return the number of mappings successfully applied; 0 if no mapping is registered
	 * for the instance's EClass
	 * @throws NullPointerException if the instance parameter is <code>null</code>
	 * @throws IllegalStateException if the gateway execution fails as a whole
	 */
	int pushInstance(EObject instance);
	
	/**
	 * Overloaded method that directly takes a CloudEvent object
	 * <p>
	 * The mappings applied are the ones registered for the EClass of the envelope's
	 * {@code data}: the envelope is carried so that a mapping can be selected and read by it,
	 * and until that exists an enveloped push applies exactly what a bare push of the same
	 * payload would.
	 * <p>
	 * A {@link io.cloudevents.model.ce.CloudEvent} passed to {@link #pushInstance(EObject)} -
	 * which is what happens whenever the caller's variable is typed {@code EObject}, because
	 * Java picks an overload statically - is delegated here, so one object behaves the same
	 * however it is held.
	 * @param cloudEvent the CloudEvent containing the object to push and additional information needed in the mapping
	 * @return the number of mappings successfully applied; 0 if no mapping is registered
	 * for that CloudEvent data
	 * @throws NullPointerException if the instance parameter is <code>null</code>
	 * @throws IllegalStateException if the gateway execution fails as a whole
	 */
	int pushInstance(CloudEvent cloudEvent);

}
