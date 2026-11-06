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
 *      Mark Hoffmann - initial API and implementation
 */
package org.eclipse.fennec.event.atlas.mapping.impl;

import static java.util.Objects.isNull;
import static java.util.Objects.requireNonNull;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.logging.Logger;

import org.eclipse.emf.common.util.EList;
import org.eclipse.emf.common.util.Enumerator;
import org.eclipse.emf.ecore.EAttribute;
import org.eclipse.emf.ecore.EClass;
import org.eclipse.emf.ecore.EDataType;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.EStructuralFeature;
import org.eclipse.emf.ecore.util.EcoreUtil;
import org.eclipse.fennec.event.atlas.mapping.ChangeRuleFilter;
import org.eclipse.fennec.event.atlas.mapping.ExpressionEvaluator;
import org.eclipse.fennec.event.atlas.mapping.ValueConverterRegistry;
import org.eclipse.fennec.event.atlas.mapping.ValueFunctionRegistry;
import org.eclipse.fennec.event.atlas.mapping.ValueMapper;
import org.eclipse.fennec.event.atlas.mapping.ValueMappingException;
import org.eclipse.fennec.event.atlas.mapping.converters.impl.InstantConverter;
import org.eclipse.fennec.event.atlas.model.mapping.AdminMapping;
import org.eclipse.fennec.event.atlas.model.mapping.FeatureMapping;
import org.eclipse.fennec.event.atlas.model.mapping.MappingProfile;
import org.eclipse.fennec.event.atlas.model.mapping.MetadataMapping;
import org.eclipse.fennec.event.atlas.model.mapping.NameMapping;
import org.eclipse.fennec.event.atlas.model.mapping.ProviderMapping;
import org.eclipse.fennec.event.atlas.model.mapping.ProviderStrategy;
import org.eclipse.fennec.event.atlas.model.mapping.ReferenceMapping;
import org.eclipse.fennec.event.atlas.model.mapping.ResourceMapping;
import org.eclipse.fennec.event.atlas.model.mapping.ServiceMapping;
import org.eclipse.fennec.event.atlas.model.mapping.TimestampMapping;
import org.eclipse.fennec.event.atlas.model.mapping.ValueMapping;
import org.eclipse.sensinact.core.emf.twin.SensinactEMFProvider;
import org.eclipse.sensinact.core.model.Model;
import org.eclipse.sensinact.core.model.SensinactModelManager;
import org.eclipse.sensinact.core.twin.SensinactDigitalTwin;
import org.eclipse.sensinact.core.twin.SensinactProvider;
import org.eclipse.sensinact.core.twin.SensinactResource;
import org.eclipse.sensinact.core.twin.SensinactService;
import org.eclipse.sensinact.model.core.provider.Admin;
import org.eclipse.sensinact.model.core.provider.ProviderPackage;
import org.eclipse.sensinact.model.core.provider.Provider;
import org.eclipse.sensinact.gateway.geojson.Coordinates;
import org.eclipse.sensinact.gateway.geojson.GeoJsonObject;
import org.eclipse.sensinact.gateway.geojson.Point;

/**
 * Implementation of {@link ValueMapper} that transforms EObject instances into SensiNact provider values
 * using ProviderMapping configurations.
 * 
 * @author Mark Hoffmann
 * @since 31.07.2025
 */
public class ValueMapperImpl implements ValueMapper {

	private static final Logger logger = Logger.getLogger(ValueMapperImpl.class.getName());

	/** The sensiNact admin service: its resources are fixed, a mapping can only feed them. */
	private static final String ADMIN_SERVICE = "admin";

	private final SensinactDigitalTwin twin;
	/** Lets the push path extend the twin model; <code>null</code> where it may not. */
	private final SensinactModelManager modelManager;
	private final ProviderMapping mapping;
	private final String providerModel;
	private final ChangeRuleFilter changeRuleFilter;
	private final ValueConverterRegistry converterRegistry;
	private final ValueFunctionRegistry functionRegistry;
	private final ExpressionEvaluator expressionEvaluator;

	/**
	 * Creates a new ValueMapper instance with custom function registry.
	 *
	 * @param twin The SensiNact digital twin to update with mapped values
	 * @param mapping The ProviderMapping configuration defining the transformation rules
	 * @param functionRegistry Registry of function services keyed by functionId
	 */
	public ValueMapperImpl(SensinactDigitalTwin twin, ProviderMapping mapping) {
		this(twin, mapping, null, null, null);
	}

	/**
	 * Creates a new ValueMapper instance that applies the resources' change rules.
	 *
	 * @param twin The SensiNact digital twin to update with mapped values
	 * @param mapping The ProviderMapping configuration defining the transformation rules
	 * @param changeRuleFilter Decides per resource whether a mapped value is pushed, or
	 * <code>null</code> to push every value
	 */
	public ValueMapperImpl(SensinactDigitalTwin twin, ProviderMapping mapping, ChangeRuleFilter changeRuleFilter) {
		this(twin, mapping, changeRuleFilter, null, null);
	}

	/**
	 * Creates a new ValueMapper instance that applies the resources' change rules and the
	 * converters their value mappings name.
	 *
	 * @param twin The SensiNact digital twin to update with mapped values
	 * @param mapping The ProviderMapping configuration defining the transformation rules
	 * @param changeRuleFilter Decides per resource whether a mapped value is pushed, or
	 * <code>null</code> to push every value
	 * @param converterRegistry Applies the converter a {@code ValueMapping} names, or
	 * <code>null</code> to push every value unconverted
	 */
	public ValueMapperImpl(SensinactDigitalTwin twin, ProviderMapping mapping, ChangeRuleFilter changeRuleFilter,
			ValueConverterRegistry converterRegistry) {
		this(twin, mapping, changeRuleFilter, converterRegistry, null);
	}

	/**
	 * Creates a new ValueMapper instance with every extension point wired in.
	 *
	 * @param twin The SensiNact digital twin to update with mapped values
	 * @param mapping The ProviderMapping configuration defining the transformation rules
	 * @param changeRuleFilter Decides per resource whether a mapped value is pushed, or
	 * <code>null</code> to push every value
	 * @param converterRegistry Applies the converter a mapping names, or <code>null</code> to
	 * push every value unconverted
	 * @param functionRegistry Applies the function a mapping names, or <code>null</code> to
	 * leave every functionId unread
	 */
	public ValueMapperImpl(SensinactDigitalTwin twin, ProviderMapping mapping, ChangeRuleFilter changeRuleFilter,
			ValueConverterRegistry converterRegistry, ValueFunctionRegistry functionRegistry) {
		this(twin, mapping, changeRuleFilter, converterRegistry, functionRegistry, null);
	}

	/**
	 * Creates a new ValueMapper instance with every extension point wired in, expressions
	 * included.
	 *
	 * @param twin The SensiNact digital twin to update with mapped values
	 * @param mapping The ProviderMapping configuration defining the transformation rules
	 * @param changeRuleFilter Decides per resource whether a mapped value is pushed, or
	 * <code>null</code> to push every value
	 * @param converterRegistry Applies the converter a mapping names, or <code>null</code> to
	 * push every value unconverted
	 * @param functionRegistry Applies the function a mapping names, or <code>null</code> to
	 * leave every functionId unread
	 * @param expressionEvaluator Evaluates the expression a mapping carries, or
	 * <code>null</code> to leave every expression unread - which is what a runtime that
	 * deploys no expression language does
	 */
	public ValueMapperImpl(SensinactDigitalTwin twin, ProviderMapping mapping, ChangeRuleFilter changeRuleFilter,
			ValueConverterRegistry converterRegistry, ValueFunctionRegistry functionRegistry,
			ExpressionEvaluator expressionEvaluator) {
		this(twin, null, mapping, changeRuleFilter, converterRegistry, functionRegistry, expressionEvaluator);
	}

	/**
	 * Creates a new ValueMapper instance that may also extend the twin model.
	 * <p>
	 * A service whose name a payload decides cannot exist before that payload arrives, so the
	 * model has to grow at push time. Everything else is unchanged: a statically named service
	 * is built at registration as before, and this mapper only fills it.
	 *
	 * @param twin The SensiNact digital twin to update with mapped values
	 * @param modelManager Lets a payload-named service be created on the provider's model, or
	 * <code>null</code> to leave the model as registration built it - in which case a mapping
	 * that names a service from its payload is skipped with a warning
	 * @param mapping The ProviderMapping configuration defining the transformation rules
	 * @param changeRuleFilter Decides per resource whether a mapped value is pushed, or
	 * <code>null</code> to push every value
	 * @param converterRegistry Applies the converter a mapping names, or <code>null</code> to
	 * push every value unconverted
	 * @param functionRegistry Applies the function a mapping names, or <code>null</code> to
	 * leave every functionId unread
	 * @param expressionEvaluator Evaluates the expression a mapping carries, or
	 * <code>null</code> to leave every expression unread
	 */
	public ValueMapperImpl(SensinactDigitalTwin twin, SensinactModelManager modelManager, ProviderMapping mapping,
			ChangeRuleFilter changeRuleFilter, ValueConverterRegistry converterRegistry,
			ValueFunctionRegistry functionRegistry, ExpressionEvaluator expressionEvaluator) {
		this.twin = requireNonNull(twin, "Digital twin cannot be null");
		this.modelManager = modelManager;
		this.mapping = requireNonNull(mapping, "Provider mapping cannot be null");
		this.providerModel = determineProviderModel(mapping);
		this.changeRuleFilter = changeRuleFilter;
		this.converterRegistry = converterRegistry;
		this.functionRegistry = functionRegistry;
		this.expressionEvaluator = expressionEvaluator;
	}

	@Override
	public void mapInstance(EObject sourceInstance) throws ValueMappingException {
		requireNonNull(sourceInstance, "Source instance cannot be null");

		ValidationResult validation = validateInstance(sourceInstance);
		if (!validation.isValid()) {
			throw new ValueMappingException("Instance validation failed: " + validation.getErrors());
		}

		// Determine provider-level timestamp once
		Instant providerTimestamp = determineTimestamp(sourceInstance, null);
		String providerId = determineProviderId(sourceInstance);
		try {
			SensinactProvider provider = twin.getProvider(providerModel, providerId);
			if (provider == null) {
				provider = twin.createProvider(providerModel, providerId, providerTimestamp);
			}
			// An admin that carries more than sensiNact's five resources exists only as an
			// instance of an Admin subclass, so the provider is retyped once, before anything
			// reads its admin. Re-read afterwards: the update replaces the stored admin.
			if (extendAdmin(provider, providerId)) {
				provider = twin.getProvider(providerModel, providerId);
			}


			for (ServiceMapping serviceMapping : mapping.getServices()) {
				// Determine the source object for this service (may be from a collection).
				// A service whose filter matches nothing is not carried by this payload - a
				// device that splits its readings over several uplinks sends a different
				// subset each time, and the services it did not send keep their last values.
				List<EObject> serviceSources = getServiceSources(sourceInstance, serviceMapping);
				if (serviceSources.isEmpty()) {
					logger.fine(String.format("Instance carries no source for service %s - skipped",
							serviceMapping.getMid()));
					continue;
				}

				// One service per source. There is more than one only where the mapping
				// produces its name, because a set of services needs a name for each.
				LinkedHashSet<String> used = new LinkedHashSet<>();
				for (EObject serviceSource : serviceSources) {
					// The twin name of the service: the mid, unless the mapping produces one
					// from the payload. Resolved against the service source, which is the
					// object its resources read from, so an expression's self is the same
					// object there.
					String serviceName = resolveServiceName(serviceMapping, serviceSource);
					if (!used.add(serviceName) && serviceSources.size() > 1) {
						// Two elements that produce one name are one service. Refusing the
						// payload would throw away the elements that did produce distinct
						// names, so the later value wins and the mapping is reported instead.
						logger.warning(String.format(
								"Mapping %s produced the service name '%s' more than once for provider %s"
								+ " - those elements share one service and the last value wins",
								mapping.getMid(), serviceName, providerId));
					}

					// Service timestamp takes precedence, fallback to provider timestamp
					Instant serviceTimestamp = determineTimestamp(serviceSource, serviceMapping, providerTimestamp);
					mapServiceResources(serviceSource, serviceMapping, provider, serviceTimestamp, providerId,
							serviceName);
				}
			}

			// Map admin service if configured
			if (mapping.getAdmin() != null) {
				// Determine the source object for admin service (may be from a collection)
				Optional<EObject> adminSource = getServiceSource(sourceInstance, mapping.getAdmin());
				if (adminSource.isEmpty()) {
					logger.fine("Instance carries no source for the admin service - skipped");
				} else {
					// Admin timestamp takes precedence, fallback to provider timestamp
					Instant adminTimestamp = determineTimestamp(adminSource.get(), mapping.getAdmin(),
							providerTimestamp);
					// sensiNact stamps the admin resources with the provider's creation instant and keeps
					// the newer value, so an admin update dated from a payload older than the provider it
					// just created would be dropped. The admin values describe the provider, not the
					// measurement, so they take the provider timestamp of this payload when older.
					if (adminTimestamp.isBefore(providerTimestamp)) {
						adminTimestamp = providerTimestamp;
					}
					mapAdminService(adminSource.get(), mapping.getAdmin(), provider, adminTimestamp, providerId);
				}
			}

			logger.fine(String.format("Successfully mapped instance %s to provider %s", sourceInstance.eClass().getName(), providerModel));

		} catch (Exception e) {
			throw new ValueMappingException("Failed to map instance to provider " + providerModel, e);
		}
	}

	@Override
	public Map<String, Object> mapResourceValues(EObject sourceInstance) {
		requireNonNull(sourceInstance, "Source instance cannot be null");

		Map<String, Object> values = new LinkedHashMap<>();

		for (ServiceMapping serviceMapping : mapping.getServices()) {
			try {
				// One entry set per source. There is more than one source only where the
				// mapping produces its service name over a collection - exactly as
				// mapInstance expands it, so the two faces of the engine agree.
				for (EObject serviceSource : getServiceSources(sourceInstance, serviceMapping)) {
					collectServiceValues(serviceSource, serviceMapping, values);
				}
			} catch (ValueMappingException e) {
				logger.warning(String.format("Failed to get service source for %s: %s", serviceMapping.getMid(), e.getMessage()));
			}
		}

		AdminMapping adminMapping = mapping.getAdmin();
		if (adminMapping != null && !adminMapping.getResources().isEmpty()) {
			try {
				EObject adminSource = getServiceSource(sourceInstance, adminMapping).orElse(null);
				if (adminSource != null) {
					for (ResourceMapping resourceMapping : adminMapping.getResources()) {
						String resourcePath = ADMIN_SERVICE + "." + resourceMapping.getMid();
						try {
							Optional<Object> value = extractValue(adminSource, resourceMapping);
							if (value.isPresent()) {
								values.put(resourcePath, value.get());
							}
						} catch (Exception e) {
							logger.warning(String.format("Failed to extract value for resource path %s: %s", resourcePath, e.getMessage()));
						}
					}
				}
			} catch (ValueMappingException e) {
				logger.warning(String.format("Failed to get service source for %s: %s", ADMIN_SERVICE, e.getMessage()));
			}
		}

		return values;
	}

	@Override
	public ValidationResult validateInstance(EObject sourceInstance) {
		requireNonNull(sourceInstance, "Source instance cannot be null");

		ValidationResultImpl result = new ValidationResultImpl();

		// Check if instance type matches any of the expected provider classes
		EClass instanceClass = sourceInstance.eClass();
		boolean typeMatches = mapping.getProviderClasses().stream()
				.anyMatch(providerClass -> providerClass.isSuperTypeOf(instanceClass) ||
						providerClass.equals(instanceClass));

		if (!typeMatches) {
			result.addError("Instance type " + instanceClass.getName() +
					" does not match any expected provider classes");
		}

		// Validate feature paths for all resources
		for (ServiceMapping serviceMapping : mapping.getServices()) {
			// Validate ReferenceMapping if present (preferred approach)
			if (serviceMapping.getReferencedResource() != null) {
				validateReferenceMapping(sourceInstance, serviceMapping.getReferencedResource(),
						"service: " + serviceMapping.getMid(), result);
			}

			// Get the service source for validation (may be from a collection). An empty
			// optional is a service this instance does not carry, which mapInstance skips -
			// so there is nothing to validate rather than something to report.
			try {
				Optional<EObject> serviceSource = getServiceSource(sourceInstance, serviceMapping);
				if (!serviceSource.isEmpty()) {
					for (ResourceMapping resourceMapping : serviceMapping.getResources()) {
						validateResourceMapping(serviceSource.get(), resourceMapping, serviceMapping.getMid(), result);
					}
				}
			} catch (ValueMappingException e) {
				result.addError("Failed to get service source for " + serviceMapping.getMid() + ": " + e.getMessage());
			}
		}

		AdminMapping adminMapping = mapping.getAdmin();
		if (adminMapping != null && !adminMapping.getResources().isEmpty()) {
			try {
				EObject adminSource = getServiceSource(sourceInstance, adminMapping).orElse(null);
				if (adminSource != null) {
					for (ResourceMapping resourceMapping : adminMapping.getResources()) {
						validateResourceMapping(adminSource, resourceMapping, ADMIN_SERVICE, result);
					}
				}
			} catch (ValueMappingException e) {
				result.addError("Failed to get service source for " + ADMIN_SERVICE + ": " + e.getMessage());
			}
		}

		return result;
	}

	/**
	 * Validates a ReferenceMapping configuration.
	 */
	private void validateReferenceMapping(EObject sourceInstance,
			ReferenceMapping referenceMapping,
			String context, ValidationResultImpl result) {

		List<EStructuralFeature> featurePath = referenceMapping.getFeaturePath();

		// Check that feature path is not empty
		if (featurePath == null || featurePath.isEmpty()) {
			result.addError(String.format("ReferenceMapping has no feature path (%s)", context));
			return;
		}

		// Try to navigate the feature path. Element selection - by filter or by index -
		// happens during navigation, so an empty result means the instance does not carry
		// this element: the service is skipped, which is not a validation finding.
		try {
			Optional<Object> targetValue = navigateFeaturePath(sourceInstance, featurePath, context, referenceMapping);

			if (targetValue.isPresent() && !(targetValue.get() instanceof EObject)) {
				result.addError(String.format("ReferenceMapping target is not an EObject (%s)", context));
			}

		} catch (ValueMappingException e) {
			result.addError(String.format("Failed to navigate ReferenceMapping (%s): %s", context, e.getMessage()));
		}
	}

	/**
	 * Applies the converter a feature mapping names, if any.
	 * <p>
	 * Every FeatureMapping carries a converterId, so this is reached from the resource value,
	 * the name and the timestamp alike. It converts what the <em>feature path</em> produced; a
	 * static literal - a NameMapping's name, a TimestampMapping's timestamp - is authored in
	 * its final form and is not run through a converter.
	 *
	 * @param converterId the id the mapping names, may be <code>null</code> or blank
	 * @param value the value the feature path produced
	 * @param context what is being converted, for the log line
	 * @return the value to use, or an empty optional to drop it
	 */
	private Optional<Object> applyConverter(String converterId, Object value, String context) {
		if (converterId == null || converterId.isBlank()) {
			return Optional.ofNullable(value);
		}
		if (converterRegistry == null) {
			// No whiteboard wired in - a plain-Java ValueMapper, or a runtime that deploys no
			// converters. Pushing the value unconverted matches what an unknown id does.
			logger.fine(String.format(
					"%s names converter '%s' but no ValueConverterRegistry is available - using it unconverted",
					context, converterId));
			return Optional.ofNullable(value);
		}
		return converterRegistry.convert(converterId, value);
	}

	/**
	 * The constant a value mapping carries, read as the resource's declared type.
	 * <p>
	 * {@code ValueMapping.value} is an EString, so a resource of another type needs the literal
	 * parsed rather than coerced. {@code EcoreUtil.createFromString} is what reads a literal,
	 * and it is the same call that turns a {@code defaultValueLiteral} into the twin resource's
	 * initial value.
	 *
	 * @return the constant, or an empty optional when there is none or it does not fit the type
	 */
	private Optional<Object> constantValue(ValueMapping valueMapping, ResourceMapping resourceMapping) {
		String literal = valueMapping.getValue();
		if (literal == null || literal.isEmpty()) {
			return Optional.empty();
		}
		EDataType targetType = resourceMapping.getEAttributeType();
		if (targetType == null) {
			return Optional.of(literal);
		}
		try {
			return Optional.ofNullable(EcoreUtil.createFromString(targetType, literal));
		} catch (Exception e) {
			logger.warning(String.format("Resource %s has the constant '%s', which is no %s: %s",
					resourceMapping.getMid(), literal, targetType.getName(), e.getMessage()));
			return Optional.empty();
		}
	}

	/**
	 * Extracts a value from an EObject instance using a ResourceMapping configuration.
	 * <p>
	 * A resource states where its value comes from in one of two ways. A {@code valueMapping}
	 * is the richer one and wins when present: it is a FeatureMapping, so it carries the path
	 * plus the collection selection and the {@code converterId}. Plain {@code valueFeature} is
	 * the original form and stays supported, so a mapping written before valueMapping existed
	 * behaves exactly as it did.
	 * <p>
	 * A {@code valueMapping} may also carry a constant in its {@code value}. The constant is the
	 * last producer: the feature path answers first, and the constant answers when the path
	 * yields nothing - because the resource declares no path, or because this payload does not
	 * carry the feature. A NameMapping's {@code name} and a TimestampMapping's {@code timestamp}
	 * already work this way, so the three literals behave alike.
	 */
	private Optional<Object> extractValue(EObject sourceInstance, ResourceMapping resourceMapping) {
		ValueMapping valueMapping = resourceMapping.getValueMapping();
		Optional<Object> rawValue;
		if (valueMapping == null) {
			rawValue = getRawValue(sourceInstance, resourceMapping.getValueFeature());
		} else {
			if (!resourceMapping.getValueFeature().isEmpty()) {
				logger.fine(String.format(
						"Resource %s sets both valueMapping and valueFeature - the valueFeature is ignored",
						resourceMapping.getMid()));
			}
			rawValue = getRawValue(sourceInstance, valueMapping);
		}

		if (!rawValue.isPresent()) {
			// The constant is the last producer in the chain. It is already written in its
			// final form, so it is not run through a converter - the same rule a NameMapping's
			// name and a TimestampMapping's timestamp follow.
			return valueMapping == null ? Optional.empty() : constantValue(valueMapping, resourceMapping);
		}

		// Convert before coercing to the resource's declared type: a converter may well return
		// a different type than it was handed, and the coercion below is what settles that.
		if (valueMapping != null) {
			rawValue = applyConverter(valueMapping.getConverterId(), rawValue.get(),
					"Resource " + resourceMapping.getMid());
			if (!rawValue.isPresent()) {
				return Optional.empty();
			}
		}

		// Convert the raw value using the resource's type information
		EDataType targetType = resourceMapping.getEAttributeType();
		if (targetType != null) {
			try {
				Object convertedValue = convertValue(rawValue.get(), targetType);
				return Optional.of(convertedValue);
			} catch (Exception e) {
				logger.warning(String.format("Failed to convert value %s to type %s: %s", rawValue.get(), targetType.getName(), e.getMessage()));
				return Optional.empty();
			}
		}

		return rawValue;
	}

	/**
	 * Extracts one service's values from one source into the result map.
	 * <p>
	 * The key prefix is the service's twin name, which is the mid unless the mapping produces
	 * one - so the keys of every mapping written so far are unchanged, and a set of services
	 * gets one group of keys for each member instead of overwriting itself.
	 */
	private void collectServiceValues(EObject serviceSource, ServiceMapping serviceMapping,
			Map<String, Object> values) throws ValueMappingException {

		String servicePrefix = resolveServiceName(serviceMapping, serviceSource);

		// Add auto-generated resources from ReferenceMapping if configured. The service source
		// already is the referenced element - getServiceSources resolved it through the very
		// same ReferenceMapping.
		if (serviceMapping.getReferencedResource() != null) {
			ReferenceMapping refMapping = serviceMapping.getReferencedResource();

			for (EStructuralFeature attribute : getFilteredAttributes(serviceSource.eClass(), refMapping)) {
				String resourcePath = servicePrefix + "." + attribute.getName();
				try {
					Object value = serviceSource.eGet(attribute);
					if (value != null) {
						values.put(resourcePath, value);
					}
				} catch (Exception e) {
					logger.warning(String.format("Failed to extract auto-generated value for resource path %s: %s", resourcePath, e.getMessage()));
				}
			}
		}

		// Add explicitly defined resources
		for (ResourceMapping resourceMapping : serviceMapping.getResources()) {
			String resourcePath = servicePrefix + "." + resourceMapping.getMid();

			try {
				Optional<Object> value = extractValue(serviceSource, resourceMapping);
				if (value.isPresent()) {
					values.put(resourcePath, value.get());
				}
			} catch (Exception e) {
				logger.warning(String.format("Failed to extract value for resource path %s: %s", resourcePath, e.getMessage()));
			}
		}
	}

	/**
	 * The objects a service maps, one service for each.
	 * <p>
	 * Normally one - the element a {@code ReferenceMapping} selects, or the payload itself.
	 * It is a set only where the mapping <em>produces</em> its service name and the path keeps
	 * a collection: a set of services needs a name for each, and only a producer can give one.
	 * A collection with a constant name therefore keeps selecting a single element, which is
	 * what every mapping written so far means by it - the weather mapping's reports[0] and
	 * reports[1] are two positions with two meanings, not a set.
	 * @param rootSource the payload. Must not be <code>null</code>
	 * @param serviceMapping the mapping of the service. Must not be <code>null</code>
	 * @return the sources, possibly empty where the payload carries none
	 */
	private List<EObject> getServiceSources(EObject rootSource, ServiceMapping serviceMapping)
			throws ValueMappingException {

		ReferenceMapping referenced = serviceMapping.getReferencedResource();
		if (referenced == null || !hasProducer(serviceMapping.getName())) {
			return getServiceSource(rootSource, serviceMapping).map(List::of).orElseGet(List::of);
		}
		return expandSources(rootSource, referenced, "service: " + serviceMapping.getMid());
	}

	/**
	 * Navigates a reference path and keeps every element the mapping does not narrow away.
	 * <p>
	 * A {@code collectionFilter} narrows the set and every survivor becomes a service. A
	 * <em>non-zero</em> {@code collectionIndex} still selects one element, because an author
	 * who wrote one means one; a zero index cannot be told from an unset one - EMF generates
	 * {@code eIsSet} as "differs from the default" and the default is 0 - so it expands. Use a
	 * filter to select a single element beside a produced name.
	 * @return every object the path reaches, in payload order
	 */
	private List<EObject> expandSources(EObject rootSource, ReferenceMapping referenced, String context)
			throws ValueMappingException {

		List<EStructuralFeature> featurePath = referenced.getFeaturePath();
		if (featurePath == null || featurePath.isEmpty()) {
			throw new ValueMappingException(
				String.format("ReferenceMapping has no feature path (%s)", context));
		}
		Optional<CollectionFilter> filter = CollectionFilter.of(referenced, context);
		int index = referenced.getCollectionIndex();

		List<Object> current = new ArrayList<>();
		current.add(rootSource);
		for (EStructuralFeature feature : featurePath) {
			List<Object> next = new ArrayList<>();
			for (Object source : current) {
				if (!(source instanceof EObject holder)) {
					throw new ValueMappingException(
						String.format("Cannot navigate feature '%s': value is not an EObject (%s)",
							feature.getName(), context));
				}
				Object value = holder.eGet(feature);
				if (value == null) {
					// This branch carries nothing. Unlike the single-source path it is not an
					// error: a set may well have members that do not answer every feature.
					continue;
				}
				if (value instanceof List<?> collection) {
					next.addAll(survivors(collection, filter, index, context));
				} else {
					next.add(value);
				}
			}
			current = next;
		}

		List<EObject> sources = new ArrayList<>();
		for (Object source : current) {
			if (!(source instanceof EObject element)) {
				throw new ValueMappingException(
					String.format("Referenced target is not an EObject (%s)", context));
			}
			sources.add(element);
		}
		logger.fine(String.format("Expanded %d service source(s) (%s)", sources.size(), context));
		return sources;
	}

	/**
	 * The elements of one collection that a filter keeps, or the one a non-zero index selects.
	 */
	private List<Object> survivors(List<?> collection, Optional<CollectionFilter> filter, int index,
			String context) throws ValueMappingException {

		List<Object> kept = new ArrayList<>();
		for (Object element : collection) {
			if (filter.isEmpty() || filter.get().matches(element)) {
				kept.add(element);
			}
		}
		if (index == 0) {
			return kept;
		}
		if (index < 0 || index >= kept.size()) {
			throw new ValueMappingException(
				String.format("Collection index %d out of bounds for collection of size %d (%s)",
					index, kept.size(), context));
		}
		return List.of(kept.get(index));
	}

	/**
	 * The name a service carries in the twin.
	 * <p>
	 * The mid, unless the mapping's {@code NameMapping} carries a <em>producer</em> - an
	 * expression, a function or a feature path. A constant name does not win: every mapping
	 * written so far fills that slot with a label beside a differing mid, and reading it as the
	 * twin name would rename the services of all of them at once. A producer is new, and today
	 * it is read by nothing, so it can mean this without breaking anything.
	 * <p>
	 * A producer that yields nothing leaves the mapping's own fallback to answer - the constant
	 * first, then the mid - which is the rule every other producer here follows.
	 * @param serviceMapping the mapping of the service. Must not be <code>null</code>
	 * @param serviceSource the object the service reads from. Must not be <code>null</code>
	 * @return the name, never <code>null</code>
	 */
	private String resolveServiceName(ServiceMapping serviceMapping, EObject serviceSource)
			throws ValueMappingException {

		NameMapping nameMapping = serviceMapping.getName();
		if (!hasProducer(nameMapping)) {
			return serviceMapping.getMid();
		}

		Optional<Object> rawValue = getRawValue(serviceSource, nameMapping);
		if (rawValue.isPresent() && rawValue.get() != null) {
			Optional<Object> converted = applyConverter(nameMapping.getConverterId(), rawValue.get(),
					"Service name mapping");
			if (converted.isPresent() && converted.get() != null) {
				String produced = converted.get().toString().trim();
				if (!produced.isEmpty()) {
					return produced;
				}
			}
		}

		if (nameMapping.getName() != null && !nameMapping.getName().isBlank()) {
			logger.fine(String.format("Service %s produced no name from this payload - using '%s'",
					serviceMapping.getMid(), nameMapping.getName()));
			return nameMapping.getName();
		}
		logger.fine(String.format("Service %s produced no name from this payload - using its mid",
				serviceMapping.getMid()));
		return serviceMapping.getMid();
	}

	/**
	 * Whether a mapping computes its value rather than carrying a literal.
	 * @param featureMapping the mapping to test, may be <code>null</code>
	 * @return <code>true</code> where an expression, a function or a feature path is declared
	 */
	private static boolean hasProducer(FeatureMapping featureMapping) {
		if (featureMapping == null) {
			return false;
		}
		return (featureMapping.getExpression() != null && !featureMapping.getExpression().isBlank())
				|| (featureMapping.getFunctionId() != null && !featureMapping.getFunctionId().isBlank())
				|| !featureMapping.getFeaturePath().isEmpty();
	}

	/**
	 * Adds a payload-named service to the provider's model, so the twin can carry it.
	 * <p>
	 * Only a produced name reaches this - the caller keeps a missing static service an error,
	 * because creating it here would hide a broken mapping. The service is built by the same
	 * code registration uses, so it carries the same resource types, units and metadata.
	 * @return the new service, or <code>null</code> where the model may not grow
	 */
	private SensinactService growModel(ServiceMapping serviceMapping, SensinactProvider provider,
			String serviceName) {

		if (modelManager == null) {
			logger.warning(String.format(
					"Mapping %s names service '%s' from its payload, but this mapper cannot extend the"
					+ " model - the service is skipped", mapping.getMid(), serviceName));
			return null;
		}
		Model model = modelManager.getModel(providerModel);
		if (model == null) {
			logger.warning(String.format("No model '%s' to add service '%s' to", providerModel, serviceName));
			return null;
		}
		ProviderModelSensinactMapper.createServiceOn(model, serviceMapping, serviceName);
		logger.fine(String.format("Added service '%s' to model '%s' from mapping %s", serviceName,
				providerModel, mapping.getMid()));
		// Read it back from the provider rather than returning what the model built: the twin
		// service is what carries values, and a provider that exists already picks up a service
		// its model gained.
		return provider.getServices().get(serviceName);
	}

	/**
	 * Puts a service of the EClass the mapping names on the provider.
	 * <p>
	 * {@code getOrCreateService(name, EClass)} goes to {@code ModelNexus.createServiceInstance},
	 * which puts a typed service into the provider without touching the model - so a frozen
	 * model does not stop it, and the service's resources are the ones the EClass declares,
	 * with their types, units and metadata. It is the same call for a service named by its mid
	 * and for one named by the payload.
	 *
	 * @return the service, or <code>null</code> where this runtime cannot create one, in which
	 * case the service is skipped and the rest of the payload still lands
	 */
	private SensinactService createTypedService(ServiceMapping serviceMapping, SensinactProvider provider,
			String serviceName) {
		EClass serviceEClass = serviceMapping.getServiceEClass();
		if (serviceEClass.eIsProxy()) {
			logger.warning(String.format("Mapping %s: the EClass of service '%s' is an unresolved proxy (%s)"
					+ " - the service is skipped", mapping.getMid(), serviceName,
					EcoreUtil.getURI(serviceEClass)));
			return null;
		}
		if (!(provider instanceof SensinactEMFProvider emfProvider)) {
			logger.warning(String.format("Mapping %s: service '%s' is typed %s, which needs the EMF twin - the"
					+ " service is skipped", mapping.getMid(), serviceName, serviceEClass.getName()));
			return null;
		}
		SensinactService service = emfProvider.getOrCreateService(serviceName, serviceEClass);
		logger.fine(String.format("Mapping %s: service '%s' created on provider '%s' as a %s", mapping.getMid(),
				serviceName, provider.getName(), serviceEClass.getName()));
		return service;
	}

	/**
	 * Maps all resources for a specific service.
	 * Handles both explicitly defined resources and automatically generated resources from ReferenceMapping.
	 *
	 * Note: Auto-generated ResourceMappings from ReferenceMapping are created during model registration
	 * by ProviderModelSensinactMapper and are already present in serviceMapping.getResources().
	 */
	private void mapServiceResources(EObject sourceInstance, ServiceMapping serviceMapping,
			SensinactProvider provider, Instant timestamp, String providerId, String serviceName)
			throws ValueMappingException {

		SensinactService service = provider.getServices().get(serviceName);
		if (service == null) {
			// A statically named service is built at registration, so a missing one is a broken
			// mapping and fails the instance, as it always has. A payload-named one cannot
			// exist before this payload, so it is created here - and where it cannot be, the
			// service is skipped and the other services of this payload still land.
			if (serviceMapping.getServiceEClass() != null) {
				// A typed service is never in the model, so it is missing on the first payload
				// whether its name is a mid or produced.
				service = createTypedService(serviceMapping, provider, serviceName);
			} else if (!hasProducer(serviceMapping.getName())) {
				throw new ValueMappingException("Service not found: " + serviceName);
			} else {
				service = growModel(serviceMapping, provider, serviceName);
			}
			if (service == null) {
				return;
			}
		}

		// Map all resources (both auto-generated and explicitly defined)
		// Auto-generated resources are already in the list thanks to ProviderModelSensinactMapper
		for (ResourceMapping resourceMapping : serviceMapping.getResources()) {
			mapSingleResource(sourceInstance, resourceMapping, service, timestamp, providerId, serviceName);
		}
//		Go over the temporary ResourceMapping. These are the ones automatically created when the referencedResource is set
		for (ResourceMapping resourceMapping : serviceMapping.getTemporaryResources()) {
			mapSingleResource(sourceInstance, resourceMapping, service, timestamp, providerId, serviceName);
		}
	}

	/**
	 * Writes the metadata entries whose value the payload decides.
	 * <p>
	 * {@code extraMetadata} carries what is the same for every payload and is written into the
	 * twin model once, at registration. A {@code MetadataMapping} is read per push instead: it
	 * is a {@code FeatureMapping}, so the value comes from a sibling feature of the mapped
	 * value, from a function or from an expression, and a {@code converterId} post-processes
	 * it. Where both name one key, the produced value overwrites the constant.
	 * <p>
	 * <b>A producer that yields nothing leaves the entry as it is.</b> A payload that does not
	 * carry the value is not a statement that the metadata is gone - the same reason a
	 * collectionFilter matching nothing skips a service rather than failing the instance.
	 * <p>
	 * Nothing here fails the push. Metadata describes the value; losing a description must not
	 * cost the reading it describes.
	 */
	private void mapResourceMetadata(EObject sourceInstance, ResourceMapping resourceMapping,
			SensinactResource resource, Instant timestamp, String serviceName) {

		for (MetadataMapping metadataMapping : resourceMapping.getMetadata()) {
			String key = metadataMapping.getKey();
			if (key == null || key.isBlank()) {
				logger.warning(String.format(
						"A metadata mapping of resource %s.%s carries no key - it is ignored",
						serviceName, resourceMapping.getMid()));
				continue;
			}
			try {
				Optional<Object> value = getRawValue(sourceInstance, metadataMapping);
				if (value.isPresent() && value.get() != null) {
					value = applyConverter(metadataMapping.getConverterId(), value.get(),
							String.format("Metadata '%s'", key));
				}
				if (value.isEmpty() || value.get() == null) {
					logger.fine(String.format(
							"No value for metadata '%s' of %s.%s - the entry is left as it is",
							key, serviceName, resourceMapping.getMid()));
					continue;
				}
				resource.setMetadataValue(key, value.get(), timestamp);
				logger.finest(String.format("Set metadata %s.%s[%s] = %s", serviceName,
						resourceMapping.getMid(), key, value.get()));
			} catch (Exception e) {
				logger.warning(String.format("Failed to set metadata '%s' of %s.%s: %s", key,
						serviceName, resourceMapping.getMid(), e.getMessage()));
			}
		}
	}

	/**
	 * Returns the list of attributes to map based on ReferenceMapping filter configuration.
	 *
	 * @param eClass The EClass of the referenced object
	 * @param refMapping The ReferenceMapping with filter configuration
	 * @return List of attributes that should be mapped
	 */
	private List<EStructuralFeature> getFilteredAttributes(EClass eClass,
			ReferenceMapping refMapping) {

		// Get all attributes from the EClass
		List<EAttribute> allAttributes = eClass.getEAllAttributes();
		List<EAttribute> filterList = refMapping.getFilter();
		boolean exclude = refMapping.isExclude();

		// If no filter is specified
		if (filterList == null || filterList.isEmpty()) {
			// exclude=true (default): include all attributes
			// exclude=false: include nothing (no attributes specified)
			return exclude ? new java.util.ArrayList<>(allAttributes) : Collections.emptyList();
		}

		List<EStructuralFeature> result = new java.util.ArrayList<>();

		if (exclude) {
			// exclude=true: include all attributes EXCEPT those in filter
			for (EStructuralFeature attr : allAttributes) {
				if (!filterList.contains(attr)) {
					result.add(attr);
				}
			}
		} else {
			// exclude=false: include ONLY those attributes in filter
			for (EStructuralFeature attr : filterList) {
				if (allAttributes.contains(attr)) {
					result.add(attr);
				}
			}
		}

		return result;
	}

	/**
	 * Maps a single resource value.
	 */
	private void mapSingleResource(EObject sourceInstance, ResourceMapping resourceMapping, 
			SensinactService service, Instant timestamp, String providerId, String serviceName)
			throws ValueMappingException {

		SensinactResource resource = service.getResources().get(resourceMapping.getMid());
		if (resource == null) {
			// A resource that only the stored instance carries - an extra admin resource - is
			// not in that map, which is built from what the service DECLARES. The twin's own
			// lookup resolves the service EClass from the instance and finds it.
			resource = twin.getResource(providerId, serviceName, resourceMapping.getMid());
		}
		if (resource == null) {
			throw new ValueMappingException("Resource not found: " + resourceMapping.getMid());
		}

		// Before the value, deliberately: the change rule below may drop the value and return,
		// and that rule describes what the history keeps of the *value*. It says nothing about
		// a unit or a signal group, and a resource whose value rarely moves still carries
		// metadata that a payload decides.
		mapResourceMetadata(sourceInstance, resourceMapping, resource, timestamp, service.getName());

		Optional<Object> value = extractValue(sourceInstance, resourceMapping);
		if (value.isPresent()) {
			// The resource's change rule decides whether this value reaches the twin at all.
			// Interim: the rule describes what the history provider should persist, and is
			// applied here until that provider can apply it itself.
			// The name this mapper resolved, not what the twin echoes back: it is what tells
			// the services of a set apart, so each keeps its own last accepted value.
			if (changeRuleFilter != null && !changeRuleFilter.accept(mapping.getMid(), providerId,
					serviceName, resourceMapping, value.get(), timestamp)) {
				return;
			}
			try {
				resource.setValue(value.get(), timestamp);
				logger.finest(String.format("Set resource %s.%s = %s", service.getName(), resource.getName(), value.get()));
			} catch (Exception e) {
				throw new ValueMappingException("Failed to set resource value for " + 
						service.getName() + "." + resource.getName(), e);
			}
		} else {
			logger.fine(String.format("No value extracted for resource %s.%s", service.getName(), resource.getName()));
		}
	}


	/**
	 * Gives the provider's admin the resources the mapping declares beyond sensiNact's five,
	 * by replacing the admin instance with one of an {@code Admin} subclass that declares them.
	 * <p>
	 * It runs on every push and does its work once: the guard is the twin's own instance-based
	 * lookup, so a provider whose admin already carries them costs three map reads. The gateway
	 * keeps the subclass and copies the old admin's values onto it
	 * ({@code EMFCompareUtil.copyOldService}), so friendly name, location and the timestamps
	 * survive the exchange.
	 *
	 * @param provider the provider to retype
	 * @param providerId its id, for the twin lookups and the log
	 * @return <code>true</code> when the admin was replaced, i.e. the caller must read the
	 * provider again
	 */
	private boolean extendAdmin(SensinactProvider provider, String providerId) {
		List<ResourceMapping> extras = AdminEClassBuilder.extraResources(mapping.getAdmin());
		if (extras.isEmpty()) {
			return false;
		}
		if (extras.stream().allMatch(extra -> carriesExtraAdminResource(providerId, extra.getMid()))) {
			return false;
		}
		if (!(provider instanceof SensinactEMFProvider emfProvider)) {
			logger.warning(String.format("Mapping '%s': the admin resources %s need the EMF twin, which this "
					+ "runtime does not provide - skipped", mapping.getMid(), midsOf(extras)));
			return false;
		}
		Provider stored = emfProvider.getEMFProvider();
		Admin admin = stored.getAdmin();
		if (admin != null && admin.eClass() != ProviderPackage.Literals.ADMIN) {
			// The gateway retypes an admin upward only, so a provider whose admin is already a
			// subclass cannot take a different one. This is the edited-mapping case.
			logger.warning(String.format("Mapping '%s': provider '%s' already has a %s admin, so the admin "
					+ "resources %s cannot be added to it - delete the provider to pick up the new admin",
					mapping.getMid(), providerId, admin.eClass().getName(), midsOf(extras)));
			return false;
		}
		EClass adminEClass = AdminEClassBuilder.adminEClass(mapping.getMid(), extras);
		Admin extended = (Admin) EcoreUtil.create(adminEClass);
		if (admin != null) {
			// Every value the current admin holds, metadata included: the update merges the
			// instance it is given, so an unset friendlyName would read as a change to null.
			admin.eClass().getEAllStructuralFeatures().forEach(feature -> extended.eSet(feature, admin.eGet(feature)));
		}
		stored.setAdmin(extended);
		// Not waited on: this runs on the gateway thread, where the update is applied before the
		// next read. The one failure it can have - an admin that is already a subclass - is
		// refused above instead.
		emfProvider.update(stored);
		logger.fine(String.format("Mapping '%s': provider '%s' admin is now a %s, carrying %s", mapping.getMid(),
				providerId, adminEClass.getName(), midsOf(extras)));
		return true;
	}

	/**
	 * Whether the stored admin instance carries this resource. The declared {@code Admin} never
	 * does, so this asks the twin, whose lookup goes through the instance.
	 */
	private boolean carriesExtraAdminResource(String providerId, String mid) {
		return mid != null && twin.getResource(providerId, ADMIN_SERVICE, mid) != null;
	}

	private static List<String> midsOf(List<ResourceMapping> resources) {
		return resources.stream().map(ResourceMapping::getMid).toList();
	}

	/**
	 * Maps admin service values.
	 */
	private void mapAdminService(EObject sourceInstance, AdminMapping adminMapping, 
			SensinactProvider provider, Instant timestamp, String providerId) throws ValueMappingException {

		SensinactService adminService = provider.getServices().get(ADMIN_SERVICE);
		if (adminService == null) {
			throw new ValueMappingException("Admin service not found");
		}
		mapFriendlyName(sourceInstance, adminMapping, timestamp, adminService);
		mapLocation(sourceInstance, adminMapping, timestamp, adminService);
		mapAdminResources(sourceInstance, adminMapping, adminService, timestamp, providerId);
	}

	/**
	 * Maps the admin mapping's own {@code resources} onto the admin service. The admin service's
	 * resources are the fixed set sensiNact defines, and {@code description} is the one meant to
	 * be fed this way ({@code friendlyName} and {@code location} have their own mapping features).
	 * A mid the admin service does not have is skipped with a warning rather than failing the
	 * payload: the registry refuses such a mapping at registration, this covers mappings that
	 * bypass it.
	 */
	private void mapAdminResources(EObject sourceInstance, AdminMapping adminMapping,
			SensinactService adminService, Instant timestamp, String providerId) throws ValueMappingException {
		for (ResourceMapping resourceMapping : adminMapping.getResources()) {
			if (adminService.getResources().get(resourceMapping.getMid()) == null
					&& !carriesExtraAdminResource(providerId, resourceMapping.getMid())) {
				logger.warning(String.format("Mapping '%s': the admin service has no resource '%s' (it has %s) - skipped",
						mapping.getMid(), resourceMapping.getMid(), adminService.getResources().keySet()));
				continue;
			}
			mapSingleResource(sourceInstance, resourceMapping, adminService, timestamp, providerId, ADMIN_SERVICE);
		}
	}

	/**
	 * @param sourceInstance
	 * @param adminMapping
	 * @param timestamp
	 * @param adminService
	 * @throws ValueMappingException
	 */
	private void mapFriendlyName(EObject sourceInstance, AdminMapping adminMapping, Instant timestamp,
			SensinactService adminService) throws ValueMappingException {
		String friendlyName = adminMapping.getFriendlyName();
		SensinactResource friendlyNameResource = adminService.getResources().get("friendlyName");
		requireNonNull(friendlyNameResource, "No friendly name resource available in admin service");
		// friendlyNameMapping first, then the plain feature path; the friendlyName attribute is
		// the constant that answers when neither produces a value.
		if (declares(adminMapping.getFriendlyNameMapping(), adminMapping.getFriendlyNameFeature())) {
			Optional<Object> friendlyNameValue = adminValue(sourceInstance,
					adminMapping.getFriendlyNameMapping(), adminMapping.getFriendlyNameFeature(),
					"admin.friendlyName");
			if (friendlyNameValue.isPresent()) {
				// String.valueOf, not a cast: an expression or a converter may produce any type,
				// and the resource takes a string.
				friendlyName = String.valueOf(friendlyNameValue.get());
			} else {
				logger.fine("No value extracted for admin.friendlyName");
			}
		}
		if (Objects.nonNull(friendlyName)) {
			try {
				friendlyNameResource.setValue(friendlyName, timestamp);
				logger.finest(String.format("Set admin.friendlyName = %s", adminMapping.getFriendlyName()));
			} catch (Exception e) {
				logger.warning(String.format("Failed to set admin friendlyName: %s", e.getMessage()));
			}
		}
	}

	/**
	 * Whether an admin slot names a source at all.
	 * @return <code>true</code> where either the mapping or the plain reference path is set
	 */
	private static boolean declares(FeatureMapping featureMapping, List<EStructuralFeature> referencePath) {
		return featureMapping != null || !referencePath.isEmpty();
	}

	/**
	 * One admin value, from the richer mapping where the admin carries one.
	 * <p>
	 * A {@code FeatureMapping} <em>replaces</em> the plain {@code *Ref} path rather than falling
	 * back to it - the same rule an expression follows against a feature path - because the two
	 * describe the same slot and the richer one was written later. It is what lets an admin value
	 * be computed rather than only read: {@code "'ILSA ' + self.intersectionName"}, or a converter
	 * that takes one element out of a value holding several, which a path cannot address.
	 * <p>
	 * The sibling constant ({@code friendlyName}, {@code latitude}, …) stays the fallback and is
	 * applied by the caller, so a payload that carries nothing leaves the mapping's own value in
	 * place.
	 * @param what names the slot in a log line. Must not be <code>null</code>
	 * @return the value, or empty where this payload yields none
	 */
	private Optional<Object> adminValue(EObject source, FeatureMapping featureMapping,
			List<EStructuralFeature> referencePath, String what) throws ValueMappingException {

		if (featureMapping != null) {
			Optional<Object> produced = getRawValue(source, featureMapping);
			if (produced.isEmpty() || produced.get() == null) {
				return Optional.empty();
			}
			return applyConverter(featureMapping.getConverterId(), produced.get(), what);
		}
		if (referencePath.isEmpty()) {
			return Optional.empty();
		}
		return getRawValue(source, referencePath);
	}

	/**
	 * The admin location: the point read through {@code latitudeRef}/{@code longitudeRef}, else the
	 * static {@code latitude}/{@code longitude} of the mapping. A mapping that defines neither, or
	 * a payload without the referenced values, leaves the resource untouched - another source may
	 * own the provider's location.
	 */
	private void mapLocation(EObject sourceInstance, AdminMapping adminMapping, Instant timestamp,
			SensinactService adminService) throws ValueMappingException {
		SensinactResource locationResource = adminService.getResources().get("location");
		requireNonNull(locationResource, "No location resource available in admin service");
		GeoJsonObject location = lookupLocation(adminMapping);
		if (declares(adminMapping.getLatitudeMapping(), adminMapping.getLatitudeRef())
				&& declares(adminMapping.getLongitudeMapping(), adminMapping.getLongitudeRef())) {
			Optional<Object> latValue = adminValue(sourceInstance, adminMapping.getLatitudeMapping(),
					adminMapping.getLatitudeRef(), "admin.latitude");
			Optional<Object> lonValue = adminValue(sourceInstance, adminMapping.getLongitudeMapping(),
					adminMapping.getLongitudeRef(), "admin.longitude");
			if (latValue.isPresent() && lonValue.isPresent()) {
				try {
					// The elevation is optional and keeps the mapping's own value as its
					// fallback, exactly as the static path does.
					Object elevation = adminValue(sourceInstance, adminMapping.getElevationMapping(),
							adminMapping.getElevationRef(), "admin.elevation")
							.orElse(adminMapping.getElevation());
					Coordinates coordinates = new Coordinates(toDouble(lonValue.get()),
							toDouble(latValue.get()), toDouble(elevation));
					location = new Point(coordinates, Collections.emptyList(), Collections.emptyMap());
				} catch (Exception e) {
					throw new ValueMappingException("Failed to read the admin location from the instance", e);
				}
			} else {
				logger.fine("No value extracted for admin.location");
			}
		}
		if (location == null) {
			return;
		}
		try {
			locationResource.setValue(location, timestamp);
			logger.finest(String.format("Set admin.location = %s", location));
		} catch (Exception e) {
			logger.warning(String.format("Failed to set admin location: %s", e.getMessage()));
		}
	}

	private String determineProviderId(EObject sourceInstance) throws ValueMappingException {
		NameMapping nameMapping = mapping.getName();
		return getNameMapping(nameMapping, sourceInstance);
	}

	/**
	 * @param nameMapping
	 * @param sourceInstance
	 * @return
	 * @throws ValueMappingException 
	 */
	private String getNameMapping(NameMapping nameMapping, EObject sourceInstance) throws ValueMappingException {
		if (nameMapping == null) {
			throw new ValueMappingException("A mapping is needed to determine a name from an instance");
		}

		// First try: the producer chain - a function, or the feature path with its collection
		// selection. getRawValue answers empty when the mapping declares neither.
		Optional<Object> rawValue = getRawValue(sourceInstance, nameMapping);
		if (rawValue.isPresent() && rawValue.get() != null) {
			Optional<Object> converted = applyConverter(nameMapping.getConverterId(), rawValue.get(),
					"Name mapping");
			// A dropped value is no name from this path, so the fallbacks below take over.
			if (converted.isPresent() && converted.get() != null) {
				return converted.get().toString();
			}
		}

		// Second try: Use the name property as fallback
		if (nameMapping.getName() != null) {
			return nameMapping.getName().toString();
		}

		// Final fallback: use mapping's MID + instance hash
		String fallback = mapping.getMid() + "-" + Integer.toHexString(sourceInstance.hashCode());
		logger.fine(String.format("Name mapping extraction failed, using fallback provider ID: %s", fallback));
		return fallback;
	}

	/**
	 * Determines the timestamp for provider-level mapping.
	 */
	private Instant determineTimestamp(EObject sourceInstance, ServiceMapping serviceMapping) {
		return determineTimestamp(sourceInstance, serviceMapping, null);
	}

	/**
	 * Determines the timestamp with proper precedence handling.
	 * 
	 * @param sourceInstance The source EObject instance
	 * @param serviceMapping The service mapping (null for provider-level)  
	 * @param providerTimestamp The provider timestamp to use as fallback (can be null)
	 * @return The determined timestamp
	 */
	private Instant determineTimestamp(EObject sourceInstance, ServiceMapping serviceMapping, Instant providerTimestamp) {
		// 1. Service timestamp takes precedence if available
		if (serviceMapping != null && serviceMapping.getTimestamp() != null) {
			Instant serviceTimestamp = extractTimestamp(sourceInstance, serviceMapping.getTimestamp());
			if (serviceTimestamp != null) {
				return serviceTimestamp;
			}
		}

		// 2. If no service timestamp but provider timestamp exists, use provider timestamp
		if (providerTimestamp != null) {
			return providerTimestamp;
		}

		// 3. Try provider-level timestamp if not already determined
		if (mapping.getTimestamp() != null) {
			Instant extractedProviderTimestamp = extractTimestamp(sourceInstance, mapping.getTimestamp());
			if (extractedProviderTimestamp != null) {
				return extractedProviderTimestamp;
			}
		}

		// 4. Final fallback: current time
		return Instant.now();
	}

	/**
	 * Extracts timestamp from source instance using TimestampMapping configuration.
	 * Similar to getNameMapping logic - first try feature path, then static timestamp value.
	 */
	private Instant extractTimestamp(EObject sourceInstance, TimestampMapping timestampMapping) {
		if (timestampMapping == null) {
			return null;
		}

		// First try: the producer chain - a function, or the feature path with its collection
		// selection. getRawValue answers empty when the mapping declares neither.
		Optional<Object> rawValue = getRawValue(sourceInstance, timestampMapping);
		if (rawValue.isPresent() && rawValue.get() != null) {
			Optional<Object> converted = applyConverter(timestampMapping.getConverterId(), rawValue.get(),
					"Timestamp mapping");
			// A dropped value leaves the static literal below, and then the caller's own
			// fallback chain, to answer - exactly as an unreadable feature path would.
			if (converted.isPresent() && converted.get() != null) {
				return new InstantConverter(timestampMapping.getHint()).toInstant(converted.get());
			}
		}

		// Second try: the static timestamp literal. EInstant is backed by String so that a mapping
		// stored in a Model Atlas survives being loaded dynamically (issue #61), so the literal is
		// parsed here - through the same conversion a string picked up from the feature path gets.
		String staticTimestamp = timestampMapping.getTimestamp();
		if (staticTimestamp != null && !staticTimestamp.isBlank()) {
			return new InstantConverter(timestampMapping.getHint()).toInstant(staticTimestamp.trim());
		}

		return null;
	}


	/**
	 * Determines the provider ID based on the mapping strategy.
	 */
	private String determineProviderModel(ProviderMapping providerMapping) {
		MappingProfile profile = providerMapping.getProfile();
		if (profile != null && profile.getProviderStrategy() == ProviderStrategy.UNIFIED) {
			return profile.getProvider().getProviderId();
		} else {
			return providerMapping.getFqMid();
		}
	}

	/**
	 * Converts a raw value to the target EDataType.
	 */
	private Object convertValue(Object rawValue, EDataType targetType) {
		if (rawValue == null) {
			return null;
		}

		Class<?> targetClass = targetType.getInstanceClass();
		if (targetClass != null && targetClass.isAssignableFrom(rawValue.getClass())) {
			// No conversion needed
			return rawValue;
		}
		//If enumerator names and literals differ then we have a problem with ECoreUtil. 
		//We need to pass the enum name and not the literal.
		if(rawValue instanceof Enumerator eEnum) {
				rawValue = eEnum.getName();
		}
		// Use EMF's built-in conversion
		String stringValue = EcoreUtil.convertToString(targetType, rawValue);
		return EcoreUtil.createFromString(targetType, stringValue);
	}

	/**
	 * Validates a resource mapping against a source instance.
	 */
	private void validateResourceMapping(EObject sourceInstance, ResourceMapping resourceMapping, 
			String serviceName, ValidationResultImpl result) {
		ValueMapping valueMapping = resourceMapping.getValueMapping();
		if (valueMapping != null && producesWithoutAPath(valueMapping)) {
			// An expression, a function or a constant is a value source in its own right, so
			// the resource is not missing anything. Only a producer that reads the payload can
			// be checked against this instance, and none of these three does.
			return;
		}
		List<EStructuralFeature> featurePath = valueMapping == null
				? resourceMapping.getValueFeature()
				: valueMapping.getFeaturePath();
		if (featurePath == null || featurePath.isEmpty()) {
			result.addWarning("Empty feature path for resource " + serviceName + "." + resourceMapping.getMid());
			return;
		}

		// Try to navigate the path to check if it's valid
		try {
			Optional<Object> value = getRawValue(sourceInstance, featurePath);
			if (!value.isPresent()) {
				result.addWarning("Feature path returns no value for resource " + 
						serviceName + "." + resourceMapping.getMid());
			}
		} catch (Exception e) {
			result.addError("Invalid feature path for resource " + serviceName + "." + 
					resourceMapping.getMid() + ": " + e.getMessage());
		}
	}

	/**
	 * Whether a value mapping names a value source that needs no feature path of its own.
	 */
	private static boolean producesWithoutAPath(ValueMapping valueMapping) {
		return isSet(valueMapping.getExpression()) || isSet(valueMapping.getFunctionId())
				|| isSet(valueMapping.getValue());
	}

	private static boolean isSet(String text) {
		return text != null && !text.isBlank();
	}

	/**
	 * Implementation of ValidationResult.
	 */
	private static class ValidationResultImpl implements ValidationResult {
		private final List<String> errors = new ArrayList<>();
		private final List<String> warnings = new ArrayList<>();

		void addError(String error) {
			errors.add(error);
		}

		void addWarning(String warning) {
			warnings.add(warning);
		}

		@Override
		public boolean isValid() {
			return errors.isEmpty();
		}

		@Override
		public List<String> getErrors() {
			return new ArrayList<>(errors);
		}

		@Override
		public List<String> getWarnings() {
			return new ArrayList<>(warnings);
		}
	}

	/**
	 * Determines the source EObject for a specific service based on collection configuration.
	 * First checks if referencedResource (ReferenceMapping) is set - this is the preferred approach.
	 * Falls back to legacy collectionFeature approach for backward compatibility.
	 * If neither is configured, returns the root source object.
	 *
	 * @param rootSource The root source object
	 * @param serviceMapping The service mapping configuration
	 * @return The source EObject to use for this service, or an empty optional when the
	 *         service selects a collection element this instance does not carry
	 * @throws ValueMappingException If collection extraction fails
	 */
	private Optional<EObject> getServiceSource(EObject rootSource, ServiceMapping serviceMapping)
			throws ValueMappingException {

		// New approach: Check if referencedResource is configured
		if (serviceMapping.getReferencedResource() != null) {
			return getReferencedSource(rootSource, serviceMapping.getReferencedResource(),
					"service: " + serviceMapping.getMid());
		}

		// No collection configuration: use root source directly
		return Optional.of(rootSource);
	}

	/**
	 * Navigates a ReferenceMapping to extract the target EObject.
	 * Handles both single-valued and multi-valued references, applying collection index/filter as needed.
	 *
	 * @param source The source EObject to start navigation from
	 * @param referenceMapping The ReferenceMapping configuration
	 * @param context Context string for error messages (e.g., "service: weather")
	 * @return The target EObject, or an empty optional when the mapping's
	 *         {@code collectionFilter} matches nothing in this instance
	 * @throws ValueMappingException If navigation fails
	 */
	private Optional<EObject> getReferencedSource(EObject source, ReferenceMapping referenceMapping,
			String context) throws ValueMappingException {

		List<EStructuralFeature> featurePath = referenceMapping.getFeaturePath();
		if (featurePath == null || featurePath.isEmpty()) {
			throw new ValueMappingException(
				String.format("ReferenceMapping has no feature path (%s)", context));
		}

		// Navigate to the target, picking an element wherever the path crosses a collection
		Optional<Object> targetValue = navigateFeaturePath(source, featurePath, context, referenceMapping);
		if (targetValue.isEmpty()) {
			return Optional.empty();
		}

		if (!(targetValue.get() instanceof EObject)) {
			throw new ValueMappingException(
				String.format("Referenced target is not an EObject (%s)", context));
		}

		return Optional.of((EObject) targetValue.get());
	}

	/**
	 * Picks the element a {@link FeatureMapping} selects out of a collection: by its
	 * {@code collectionFilter} when one is set, otherwise by its {@code collectionIndex}.
	 *
	 * @return the selected element, or an empty optional when a filter matches nothing - the
	 *         payload does not carry this element, which is a skip, not a failure
	 * @throws ValueMappingException if there is no filter and the index does not address an
	 *         element, which is a mapping that does not fit the payload at all
	 */
	private Optional<Object> selectElement(List<?> collection, FeatureMapping selector, String context)
			throws ValueMappingException {

		Optional<CollectionFilter> filter = CollectionFilter.of(selector, context);
		if (filter.isPresent()) {
			Optional<Object> selected = filter.get().select(collection);
			if (selected.isEmpty()) {
				logger.fine(String.format("No element matches filter %s (%s)", filter.get(), context));
			}
			return selected;
		}

		if (collection.isEmpty()) {
			throw new ValueMappingException(
				String.format("Referenced collection is empty (%s)", context));
		}

		int index = selector == null ? 0 : selector.getCollectionIndex();
		if (index < 0 || index >= collection.size()) {
			throw new ValueMappingException(
				String.format("Collection index %d out of bounds for collection of size %d (%s)",
					index, collection.size(), context));
		}

		logger.fine(String.format("Extracted element at index %s (%s)", index, context));
		return Optional.of(collection.get(index));
	}

	/**
	 * Navigates a feature path from a source object to retrieve the target value.
	 * <p>
	 * Wherever the path crosses a collection - at its end or in the middle - the
	 * {@code selector}'s filter or index picks the element to carry on from. A path may
	 * therefore continue past a collection, which is what addresses the value of an EMap
	 * entry: {@code lines} selects the entry, {@code value} navigates into it.
	 *
	 * @param source The source EObject
	 * @param featurePath The path of features to traverse
	 * @param context Context string for error messages
	 * @param selector The mapping whose collection filter/index picks collection elements
	 * @return The value at the end of the path, or an empty optional when a collection filter
	 *         along the path matches nothing
	 * @throws ValueMappingException If navigation fails
	 */
	private Optional<Object> navigateFeaturePath(EObject source, List<EStructuralFeature> featurePath, String context,
			FeatureMapping selector) throws ValueMappingException {

		Object currentValue = source;
		try {
			for (int i = 0; i < featurePath.size(); i++) {
				EStructuralFeature feature = featurePath.get(i);

				if (!(currentValue instanceof EObject)) {
					throw new ValueMappingException(
						String.format("Cannot navigate feature '%s': current value is not an EObject (%s)",
							feature.getName(), context));
				}

				currentValue = ((EObject) currentValue).eGet(feature);

				if (currentValue == null) {
					throw new ValueMappingException(
						String.format("Feature '%s' returned null during path navigation (%s)",
							feature.getName(), context));
				}

				if (currentValue instanceof List<?> collection) {
					Optional<Object> element = selectElement(collection, selector,
							String.format("%s, feature '%s'", context, feature.getName()));
					if (element.isEmpty()) {
						return Optional.empty();
					}
					currentValue = element.get();
				}
			}
			return Optional.of(currentValue);
		} catch (ValueMappingException e) {
			throw e;
		} catch (Exception e) {
			throw new ValueMappingException(
				String.format("Failed to navigate feature path (%s): %s", context, e.getMessage()), e);
		}
	}

	/**
	 * Produces the raw, untyped value a feature mapping stands for.
	 * <p>
	 * Three producers can answer, in this order: the {@code expression}, the {@code functionId},
	 * and the plain feature path. The first one the mapping declares is the one that answers -
	 * it is not a fallback chain, because a declared producer that yields nothing means this
	 * payload carries no value for the resource, not that the next producer should guess one.
	 * What a producer yields still meets the mapping's own literal fallback further up
	 * (a NameMapping's name, a TimestampMapping's timestamp, a ValueMapping's constant).
	 *
	 * @param source         The starting object.
	 * @param featureMapping The feature mapping containing the path and collection index.
	 * @return An {@link Optional} containing the raw value, or empty if not found.
	 */
	private Optional<Object> getRawValue(EObject source, FeatureMapping featureMapping) {
		if (featureMapping == null) {
			return Optional.empty();
		}
		List<EStructuralFeature> featurePath = featureMapping.getFeaturePath();
		boolean hasPath = featurePath != null && !featurePath.isEmpty();

		// The expression is the first producer and reads the source object itself, so a
		// feature path alongside it is redundant rather than an input - unlike a function's.
		String expression = featureMapping.getExpression();
		if (expression != null && !expression.isBlank()) {
			if (hasPath) {
				logger.fine(String.format(
						"A mapping sets both an expression and a feature path %s - the feature path is ignored",
						featurePath));
			}
			return applyExpression(expression, source);
		}

		// The function is a producer. With a feature path it is handed what the path found;
		// without one it is handed the whole source object and reads what it needs itself.
		String functionId = featureMapping.getFunctionId();
		if (functionId != null && !functionId.isBlank()) {
			if (!hasPath) {
				return applyFunction(functionId, source);
			}
			Optional<Object> pathValue = followPathForFunction(source, featureMapping, featurePath);
			return pathValue.isEmpty() ? Optional.empty() : applyFunction(functionId, pathValue.get());
		}

		if (!hasPath) {
			return Optional.empty();
		}
		return followPath(source, featureMapping, featurePath);
	}

	/**
	 * The input of a function: what its feature path found, including a collection.
	 * <p>
	 * A path that <em>passes</em> a collection has to select one element to carry on from, and
	 * {@link #followPath} does that - by filter, else by {@code collectionIndex}. A path that
	 * <em>ends</em> on one is a different question: a function is a producer, and the thing its
	 * path found is the collection itself. Handing it the first element instead would truncate
	 * the input silently, which is the one failure a mapping author cannot see in the result.
	 * <p>
	 * So the collection is passed whole unless the mapping narrows it, and "narrows" is the
	 * rule {@code ForEachServiceTest} already pins for a service name: a {@code collectionFilter}
	 * or a <b>non-zero</b> {@code collectionIndex} selects one element, while an explicit
	 * {@code collectionIndex="0"} cannot - EMF generates {@code eIsSet} as "differs from the
	 * default" and the default is 0, so it is indistinguishable from an unset one. Use a filter
	 * to select the first element beside a function.
	 * <p>
	 * An <b>empty</b> collection is passed on as an empty list rather than swallowed. For a
	 * value that would mean "no value", but for a producer over a collection it is a statement:
	 * the camera saw nothing in this window, and the function decides whether that is an empty
	 * FeatureCollection or no value at all.
	 *
	 * @return what to apply the function to, or an empty optional where the path found nothing
	 */
	private Optional<Object> followPathForFunction(EObject source, FeatureMapping featureMapping,
			List<EStructuralFeature> featurePath) {

		EStructuralFeature last = featurePath.get(featurePath.size() - 1);
		if (!last.isMany() || narrowsACollection(featureMapping)) {
			return followPath(source, featureMapping, featurePath);
		}
		Optional<Object> owner = featurePath.size() == 1 ? Optional.of(source)
				: followPath(source, featureMapping, featurePath.subList(0, featurePath.size() - 1));
		if (owner.isEmpty() || !(owner.get() instanceof EObject eo)) {
			return Optional.empty();
		}
		return Optional.ofNullable(eo.eGet(last));
	}

	/** Whether the mapping selects one element of a collection. See {@link #followPathForFunction}. */
	private static boolean narrowsACollection(FeatureMapping featureMapping) {
		String filter = featureMapping.getCollectionFilter();
		return featureMapping.getCollectionIndex() != 0 || (filter != null && !filter.isBlank());
	}

	/**
	 * Evaluates the expression a feature mapping carries.
	 *
	 * @return the computed value, or an empty optional - which leaves the mapping's own
	 *         fallback to answer, exactly as an unreadable feature path does
	 */
	private Optional<Object> applyExpression(String expression, EObject source) {
		if (expressionEvaluator == null) {
			// No expression language deployed. Producing nothing is right: the alternative
			// would be to fall through to a feature path the expression was written to replace.
			logger.fine(String.format(
					"A mapping carries an expression but no ExpressionEvaluator is available: %s", expression));
			return Optional.empty();
		}
		return expressionEvaluator.evaluate(expression, source);
	}

	/**
	 * Applies the function a feature mapping names.
	 *
	 * @return the produced value, or an empty optional - which leaves the mapping's own fallback
	 *         to answer, exactly as an unreadable feature path does
	 */
	private Optional<Object> applyFunction(String functionId, Object input) {
		if (functionRegistry == null) {
			// No whiteboard wired in. Producing nothing is right: the alternative would push the
			// source object, or the unprocessed path value, as if it were the computed one.
			logger.fine(String.format(
					"A mapping names function '%s' but no ValueFunctionRegistry is available", functionId));
			return Optional.empty();
		}
		return functionRegistry.apply(functionId, input);
	}

	/**
	 * Walks a feature path, selecting an element wherever it crosses a collection.
	 */
	private Optional<Object> followPath(EObject source, FeatureMapping featureMapping,
			List<EStructuralFeature> featurePath) {

		int collectionIndex = featureMapping.getCollectionIndex(); // Default is 0
		Optional<CollectionFilter> filter = CollectionFilter.of(featureMapping, "feature path " + featurePath);

		Object currentValue = source;
		try {
			for (EStructuralFeature feature : featurePath) {
				if (currentValue instanceof EObject eo) {
					currentValue = eo.eGet(feature);

					// Handle multi-value features: by filter when one is set, else by index
					if (currentValue instanceof EList<?>) {
						List<?> list = (List<?>) currentValue;
						if (list.isEmpty()) {
							return Optional.empty();
						}

						if (filter.isPresent()) {
							Optional<Object> selected = filter.get().select(list);
							if (selected.isEmpty()) {
								return Optional.empty();
							}
							currentValue = selected.get();
							continue;
						}

						// Use the collectionIndex from FeatureMapping (default 0)
						if (collectionIndex < 0 || collectionIndex >= list.size()) {
							logger.warning(String.format("Collection index %s out of bounds for list of size %s, using index 0", collectionIndex, list.size()));
							collectionIndex = 0;
						}

						currentValue = list.get(collectionIndex);
					}
				} else {
					// The path is invalid if a non-EObject is encountered mid-path.
					return Optional.empty();
				}
			}
			return Optional.ofNullable(currentValue);
		} catch (Exception e) {
			// An error during path traversal (e.g., feature not found)
			logger.severe(String.format("Error getting raw value for feature path: %s from source %s: %s", featurePath, source.eClass().getName(), e.getMessage()));
			return Optional.empty();
		}
	}

	/**
	 * Legacy method for backward compatibility - uses default collection index of 0.
	 *
	 * @deprecated Use {@link #getRawValue(EObject, FeatureMapping)} instead.
	 */
	@Deprecated
	private Optional<Object> getRawValue(EObject source, List<EStructuralFeature> featurePath) {
		// Create a temporary FeatureMapping-like object for backward compatibility
		// In practice, this should be replaced by proper FeatureMapping usage
		if (featurePath == null || featurePath.isEmpty()) {
			return Optional.empty();
		}

		Object currentValue = source;
		try {
			for (EStructuralFeature feature : featurePath) {
				if (currentValue instanceof EObject eo) {
					currentValue = eo.eGet(feature);

					// Handle multi-value features: take first element (backward compatible behavior)
					if (currentValue instanceof EList<?>) {
						List<?> list = (List<?>) currentValue;
						if (list.isEmpty()) {
							return Optional.empty();
						}
						currentValue = list.get(0);
					}
				} else {
					return Optional.empty();
				}
			}
			return Optional.ofNullable(currentValue);
		} catch (Exception e) {
			logger.severe(String.format("Error getting raw value for feature path: %s from source %s: %s", featurePath, source.eClass().getName(), e.getMessage()));
			return Optional.empty();
		}
	}

	/**
	 * Looks up for location data in the {@link AdminMapping} 
	 * @param adminMapping the {@link AdminMapping}
	 * @return the {@link GeoJsonObject} or <code>null</code>
	 */
	private GeoJsonObject lookupLocation(AdminMapping adminMapping) {
		requireNonNull(adminMapping);
		Double latitude = adminMapping.getLatitude();
		if (isNull(latitude)) {
			// No latitude: no location
			return null;
		}

		Double longitude = adminMapping.getLongitude();
		if (isNull(longitude)) {
			// No longitude: no location
			return null;
		}

		// Altitude is optional
		Double altitude = adminMapping.getElevation();

		Coordinates coordinates = new Coordinates(toDouble(longitude), toDouble(latitude), toDouble(altitude));
		return new Point(coordinates, Collections.emptyList(), Collections.emptyMap());
	}

	/**
	 * Tries to convert the given object to a double. Returns {@link Double#NaN} in
	 * case of an error.
	 *
	 * @param value Input value
	 * @return The double representation of the given object, or {@link Double#NaN}
	 *         in case of an error
	 */
	static Double toDouble(final Object value) {
		if (value == null) {
			return Double.NaN;
		}

		if (value instanceof Number number) {
			return number.doubleValue();
		}

		if (value instanceof CharSequence) {
			return Double.parseDouble(value.toString());
		}

		return Double.NaN;
	}
}
