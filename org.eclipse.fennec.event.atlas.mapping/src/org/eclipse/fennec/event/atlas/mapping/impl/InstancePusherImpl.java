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

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;
import java.util.logging.Logger;

import org.eclipse.emf.ecore.EObject;
import org.eclipse.fennec.event.atlas.mapping.ChangeRuleFilter;
import org.eclipse.fennec.event.atlas.mapping.ExpressionEvaluator;
import org.eclipse.fennec.event.atlas.mapping.InstancePusher;
import org.eclipse.fennec.event.atlas.mapping.ProviderMappingRegistry;
import org.eclipse.fennec.event.atlas.mapping.SensinactMapperConstants;
import org.eclipse.fennec.event.atlas.mapping.ValueConverterRegistry;
import org.eclipse.fennec.event.atlas.mapping.ValueFunctionRegistry;
import org.eclipse.fennec.event.atlas.mapping.ValueMapperFactory;
import org.eclipse.fennec.event.atlas.model.mapping.MappingSelector;
import org.eclipse.fennec.event.atlas.model.mapping.ProviderMapping;
import org.eclipse.sensinact.core.command.AbstractSensinactCommand;
import org.eclipse.sensinact.core.command.GatewayThread;
import org.eclipse.sensinact.core.model.SensinactModelManager;
import org.eclipse.sensinact.core.twin.SensinactDigitalTwin;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.component.annotations.ReferenceCardinality;
import org.osgi.service.component.annotations.ReferencePolicy;
import org.osgi.util.promise.Promise;
import org.osgi.util.promise.PromiseFactory;

import io.cloudevents.model.ce.CloudEvent;

/**
 * Default {@link InstancePusher}: resolves mappings via the {@link ProviderMappingRegistry}
 * and applies each on the sensinact {@link GatewayThread}.
 * <p>
 * <b>The PID is here so a deployment can gate it.</b> This component takes configuration under
 * {@link SensinactMapperConstants#INSTANCE_PUSHER_PID} as well as under its component name, and
 * it carries - as every component does - an implicit {@code osgi.ds.satisfying.condition}
 * reference defaulted to the framework's True condition (OSGi cmpn 112.3.13). A runtime whose
 * mappings carry expressions points that at
 * {@link ExpressionEvaluatorReadyCondition}, because the {@code ExpressionEvaluator} reference
 * below is optional: without it this pusher is satisfied with no expression language deployed
 * and maps payloads whose expressions silently produce nothing. The policy belongs to the
 * deployment, not here - a runtime whose mappings carry no expression wants neither the wait
 * nor the coupling.
 *
 * @author Ilenia Salvadori
 * @since 29.07.2026
 */
@Component(configurationPid = { SensinactMapperConstants.INSTANCE_PUSHER_PID, Component.NAME })
public class InstancePusherImpl implements InstancePusher {

	private static final Logger logger = Logger.getLogger(InstancePusherImpl.class.getName());

	@Reference
	private GatewayThread gatewayThread;
	@Reference
	private ProviderMappingRegistry mappingRegistry;
	
	private final AtomicReference<ChangeRuleFilter> changeRuleFilter = new AtomicReference<>();
	private final AtomicReference<ValueConverterRegistry> converterRegistry = new AtomicReference<>();
	private final AtomicReference<ValueFunctionRegistry> functionRegistry = new AtomicReference<>();
	private final AtomicReference<ExpressionEvaluator> expressionEvaluator = new AtomicReference<>();
	
	/**
	 * Optional so the engine still works in a runtime that does not want the interim
	 * change-rule enforcement at all; dynamic so switching the filter's configuration does
	 * not tear this component down.
	 */
	@Reference(cardinality = ReferenceCardinality.OPTIONAL, policy = ReferencePolicy.DYNAMIC)
	public void setChangeRuleFilter(ChangeRuleFilter changeRuleFilter) {
		this.changeRuleFilter.set(changeRuleFilter);
	}
	
	public void unsetChangeRuleFilter(ChangeRuleFilter changeRuleFilter) {
		this.changeRuleFilter.compareAndSet(changeRuleFilter, null);
	}

	/**
	 * Optional and dynamic for the same reasons: a runtime that deploys no ValueConverter at
	 * all pushes every value unconverted, and converters coming and going must not tear this
	 * component down.
	 */
	@Reference(cardinality = ReferenceCardinality.OPTIONAL, policy = ReferencePolicy.DYNAMIC)
	public void setValueConverterRegistry(ValueConverterRegistry converterRegistry) {
		this.converterRegistry.set(converterRegistry);
	}
	
	public void unsetValueConverterRegistry(ValueConverterRegistry converterRegistry) {
		this.converterRegistry.compareAndSet(converterRegistry, null);
	}

	/** Optional and dynamic for the same reasons as the two above. */
	@Reference(cardinality = ReferenceCardinality.OPTIONAL, policy = ReferencePolicy.DYNAMIC)
	public void setValueFunctionRegistry(ValueFunctionRegistry functionRegistry) {
		this.functionRegistry.set(functionRegistry);
	}
	
	public void unsetValueFunctionRegistry(ValueFunctionRegistry functionRegistry) {
		this.functionRegistry.compareAndSet(functionRegistry, null);
	}

	/**
	 * Optional and dynamic for the same reasons again. Here it is also what keeps the
	 * expression language out of this bundle's resolution: a runtime that deploys no
	 * evaluator reads no expression, and every other producer works as before.
	 */
	@Reference(cardinality = ReferenceCardinality.OPTIONAL, policy = ReferencePolicy.DYNAMIC)
	public void setExpressionEvaluator(ExpressionEvaluator expressionEvaluator) {
		this.expressionEvaluator.set(expressionEvaluator);
	}
	
	public void unsetExpressionEvaluator(ExpressionEvaluator expressionEvaluator) {
		this.expressionEvaluator.compareAndSet(expressionEvaluator, null);
	}

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.fennec.event.atlas.mapping.InstancePusher#pushInstance(org.eclipse.emf.ecore.EObject)
	 */
	@Override
	public int pushInstance(EObject instance) {
		requireNonNull(instance, "Instance must not be null");
		// Java picks an overload from the static type of the argument, so a CloudEvent held in
		// an EObject variable would land here and be mapped as a payload of its own EClass.
		// Delegating keeps one object behaving the same however the caller holds it.
		if (instance instanceof CloudEvent cloudEvent) {
			return pushInstance(cloudEvent);
		}
		List<ProviderMapping> registered = List.copyOf(mappingRegistry.getProviderMapping(instance.eClass()));
		List<RootedMapping> applications = rootedAt(select(registered, null), instance);
		if (applications.isEmpty()) {
			logNothingToApply(registered.size(), instance.eClass().getName(), null);
			return 0;
		}
		return apply(applications, instance.eClass().getName());
	}

	/**
	 * A mapping and the object it is applied to - the root it declared with its
	 * {@code providerClasses}.
	 * <p>
	 * The pair exists because one message can feed both kinds at once: a payload-rooted
	 * mapping navigates from the payload, an envelope-rooted one from the envelope, and they
	 * are applied together in one pass over the gateway.
	 */
	private record RootedMapping(ProviderMapping mapping, EObject root) {
	}

	private static List<RootedMapping> rootedAt(List<ProviderMapping> mappings, EObject root) {
		return mappings.stream().map(mapping -> new RootedMapping(mapping, root)).toList();
	}

	/**
	 * Applies each mapping to the root it declared, on the gateway thread. The one place that
	 * talks to the twin; every entry point ends here.
	 * @param describing what to name in a failure that fails the whole push
	 */
	private int apply(List<RootedMapping> applications, String describing) {
		Promise<Integer> execute = gatewayThread.execute(new AbstractSensinactCommand<Integer>() {

			@Override
			protected Promise<Integer> call(SensinactDigitalTwin twin, SensinactModelManager modelManager,
					PromiseFactory pf) {
				int applied = 0;
				for (RootedMapping application : applications) {
					EObject root = application.root();
					ProviderMapping mapping = application.mapping();
					try {
						ValueMapperFactory.createValueMapper(twin, modelManager, mapping, changeRuleFilter.get(),
								converterRegistry.get(), functionRegistry.get(), expressionEvaluator.get())
								.mapInstance(root);
						applied++;
					} catch (Throwable e) {
						// the cause names the resource or feature, the outer message only says that it failed
						StringBuilder why = new StringBuilder(String.valueOf(e.getMessage()));
						for (Throwable c = e.getCause(); c != null && c != c.getCause(); c = c.getCause()) {
							why.append(" <- ").append(c.getMessage());
						}
						logger.warning(String.format("Failed pushing instance of '%s' via mapping '%s': %s",
								root.eClass().getName(), mapping.getMid(), why));
					}
				}
				return pf.resolved(applied);
			}
		});
		try {
			if (nonNull(execute.getFailure())) {
				throw new IllegalStateException(String.format("Error pushing instance of '%s'", describing),
						execute.getFailure());
			}
			return execute.getValue();
		} catch (InterruptedException e) {
			Thread.currentThread().interrupt();
			throw new IllegalStateException(String.format(
					"Error pushing instance of '%s' while waiting for promise to resolve", describing));
		} catch (InvocationTargetException e) {
			throw new IllegalStateException(String.format("Error pushing instance of '%s'", describing),
					e.getCause());
		}
	}

	/**
	 * Says why a push applied nothing. The two cases read alike from the outside and need
	 * different fixes: deploy the mapping, or widen the selector of one that is already there.
	 */
	private void logNothingToApply(int registered, String eClasses, CloudEvent envelope) {
		if (registered == 0) {
			logger.fine(() -> String.format("No provider mapping registered for EClass '%s' - instance is dropped",
					eClasses));
		} else {
			logger.fine(() -> String.format(
					"None of the %s provider mapping(s) registered for EClass '%s' selects %s - instance is dropped",
					registered, eClasses, describe(envelope)));
		}
	}

	/* 
	 * (non-Javadoc)
	 * @see org.eclipse.fennec.event.atlas.mapping.InstancePusher#pushInstance(io.cloudevents.model.ce.CloudEvent)
	 */
	@Override
	public int pushInstance(CloudEvent cloudEvent) {
		requireNonNull(cloudEvent, "CloudEvent must not be null");
		EObject data = cloudEvent.getData();
		// Two lookups, because a mapping declares its own root with providerClasses: a domain
		// EClass means the payload is the root, CloudEvent means the envelope is. Both kinds
		// apply to one message, each handed the object it asked for, and the selector of each
		// candidate is matched against the same envelope.
		List<ProviderMapping> payloadRooted = data == null ? List.of()
				: List.copyOf(mappingRegistry.getProviderMapping(data.eClass()));
		List<ProviderMapping> envelopeRooted = List.copyOf(mappingRegistry.getProviderMapping(cloudEvent.eClass()));

		List<RootedMapping> applications = new ArrayList<>(payloadRooted.size() + envelopeRooted.size());
		applications.addAll(rootedAt(select(payloadRooted, cloudEvent), data));
		applications.addAll(rootedAt(select(envelopeRooted, cloudEvent), cloudEvent));
		if (applications.isEmpty()) {
			// An envelope with no payload is not an error: an envelope-rooted mapping may well
			// have something to read, and if none applies there was simply nothing to map.
			logNothingToApply(payloadRooted.size() + envelopeRooted.size(), describeRoots(cloudEvent), cloudEvent);
			return 0;
		}
		return apply(applications, describeRoots(cloudEvent));
	}

	/**
	 * @return the EClasses a message offered a mapping - the payload's, and the envelope's
	 */
	private static String describeRoots(CloudEvent cloudEvent) {
		EObject data = cloudEvent.getData();
		return data == null ? cloudEvent.eClass().getName()
				: data.eClass().getName() + "/" + cloudEvent.eClass().getName();
	}

	/**
	 * Keeps the mappings that apply to this message: every one without a selector, plus every
	 * one whose selector matches the envelope.
	 * <p>
	 * Nothing is ranked. Two mappings that both match both apply, which is what lets them fill
	 * different services of one provider - the compositional case a {@code UNIFIED} profile
	 * already builds on. A general mapping that must not apply where a specific one does says
	 * so with a {@code sourceExcludes}, by name, rather than being outranked by whatever else
	 * happens to be in the registry.
	 */
	private List<ProviderMapping> select(List<ProviderMapping> registered, CloudEvent envelope) {
		List<ProviderMapping> selected = new ArrayList<>(registered.size());
		for (ProviderMapping mapping : registered) {
			MappingSelector selector = mapping.getSelector();
			if (selector == null) {
				// no selector means every message of this EClass, deliberately - including a
				// push that carries no envelope at all
				selected.add(mapping);
				continue;
			}
			if (envelope == null) {
				continue;
			}
			try {
				// compiled per push rather than cached: it is a split of one or two short
				// filters, against a gateway round trip. A cache would have to be invalidated
				// with the mapping, and the registry is where that would belong
				if (CompiledSelector.compile(selector).matches(envelope)) {
					selected.add(mapping);
				}
			} catch (IllegalArgumentException e) {
				// unreachable through the registry, which refuses such a mapping by name at
				// registration; FINE because the only way here is a programmatic registration,
				// and a per-payload warning would flood
				logger.fine(() -> String.format("Mapping '%s' has an unusable selector, so it selects nothing: %s",
						mapping.getMid(), e.getMessage()));
			}
		}
		return selected;
	}

	/**
	 * @return how to name, in a log line, what the mappings were asked to select
	 */
	private static String describe(CloudEvent envelope) {
		return envelope == null ? "a push that carries no envelope"
				: String.format("source '%s'", envelope.getSource());
	}

}
