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

import static java.util.Objects.nonNull;
import static java.util.Objects.requireNonNull;

import java.util.List;
import java.util.logging.Logger;

import org.eclipse.emf.ecore.EObject;
import org.eclipse.fennec.event.atlas.mapping.ChangeRuleFilter;
import org.eclipse.fennec.event.atlas.mapping.InstancePusher;
import org.eclipse.fennec.event.atlas.mapping.ProviderMappingRegistry;
import org.eclipse.fennec.event.atlas.mapping.ValueMapperFactory;
import org.eclipse.fennec.event.atlas.model.mapping.ProviderMapping;
import org.eclipse.sensinact.core.command.AbstractSensinactCommand;
import org.eclipse.sensinact.core.command.GatewayThread;
import org.eclipse.sensinact.core.model.SensinactModelManager;
import org.eclipse.sensinact.core.twin.SensinactDigitalTwin;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.component.annotations.ReferenceCardinality;
import org.osgi.service.component.annotations.ReferencePolicy;
import org.osgi.service.component.annotations.ReferencePolicyOption;
import org.osgi.util.promise.Promise;
import org.osgi.util.promise.PromiseFactory;

/**
 * Default {@link InstancePusher}: resolves mappings via the {@link ProviderMappingRegistry}
 * and applies each on the sensinact {@link GatewayThread}.
 * @author Ilenia Salvadori
 * @since 29.07.2026
 */
@Component
public class InstancePusherImpl implements InstancePusher {

	private static final Logger logger = Logger.getLogger(InstancePusherImpl.class.getName());

	@Reference
	private GatewayThread gatewayThread;
	@Reference
	private ProviderMappingRegistry mappingRegistry;
	/**
	 * Optional so the engine still works in a runtime that does not want the interim
	 * change-rule enforcement at all; dynamic so switching the filter's configuration does
	 * not tear this component down.
	 */
	@Reference(cardinality = ReferenceCardinality.OPTIONAL, policy = ReferencePolicy.DYNAMIC, //
			policyOption = ReferencePolicyOption.GREEDY)
	private volatile ChangeRuleFilter changeRuleFilter;

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.fennec.event.atlas.mapping.InstancePusher#pushInstance(org.eclipse.emf.ecore.EObject)
	 */
	@Override
	public int pushInstance(EObject instance) {
		requireNonNull(instance, "Instance must not be null");
		List<ProviderMapping> mappings = List.copyOf(mappingRegistry.getProviderMapping(instance.eClass()));
		if (mappings.isEmpty()) {
			logger.fine(String.format("No provider mapping registered for EClass '%s' - instance is dropped", instance.eClass().getName()));
			return 0;
		}
		Promise<Integer> execute = gatewayThread.execute(new AbstractSensinactCommand<Integer>() {

			@Override
			protected Promise<Integer> call(SensinactDigitalTwin twin, SensinactModelManager modelManager,
					PromiseFactory pf) {
				int applied = 0;
				for (ProviderMapping mapping : mappings) {
					try {
						ValueMapperFactory.createValueMapper(twin, mapping, changeRuleFilter).mapInstance(instance);
						applied++;
					} catch (Throwable e) {
						logger.warning(String.format("Failed pushing instance of '%s' via mapping '%s': %s", instance.eClass().getName(), mapping.getMid(), e.getMessage()));
					}
				}
				return pf.resolved(applied);
			}
		});
		try {
			if (nonNull(execute.getFailure())) {
				throw new IllegalStateException(String.format("Error pushing instance of '%s'",
						instance.eClass().getName()), execute.getFailure());
			}
			return execute.getValue();
		} catch (InterruptedException e) {
			Thread.currentThread().interrupt();
			throw new IllegalStateException(String.format(
					"Error pushing instance of '%s' while waiting for promise to resolve",
					instance.eClass().getName()));
		} catch (java.lang.reflect.InvocationTargetException e) {
			throw new IllegalStateException(String.format("Error pushing instance of '%s'",
					instance.eClass().getName()), e.getCause());
		}
	}

}
