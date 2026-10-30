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
import static java.util.Objects.nonNull;
import static java.util.Objects.requireNonNull;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.LinkedHashMap;
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
import org.eclipse.fennec.event.atlas.mapping.ValueMapper;
import org.eclipse.fennec.event.atlas.mapping.ValueMappingException;
import org.eclipse.fennec.event.atlas.model.mapping.AdminMapping;
import org.eclipse.fennec.event.atlas.model.mapping.FeatureMapping;
import org.eclipse.fennec.event.atlas.model.mapping.MappingProfile;
import org.eclipse.fennec.event.atlas.model.mapping.NameMapping;
import org.eclipse.fennec.event.atlas.model.mapping.ProviderMapping;
import org.eclipse.fennec.event.atlas.model.mapping.ProviderStrategy;
import org.eclipse.fennec.event.atlas.model.mapping.ReferenceMapping;
import org.eclipse.fennec.event.atlas.model.mapping.ResourceMapping;
import org.eclipse.fennec.event.atlas.model.mapping.ServiceMapping;
import org.eclipse.fennec.event.atlas.model.mapping.TimestampMapping;
import org.eclipse.sensinact.core.twin.SensinactDigitalTwin;
import org.eclipse.sensinact.core.twin.SensinactProvider;
import org.eclipse.sensinact.core.twin.SensinactResource;
import org.eclipse.sensinact.core.twin.SensinactService;
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
	private final ProviderMapping mapping;
	private final String providerModel;
	private final ChangeRuleFilter changeRuleFilter;

	/**
	 * Creates a new ValueMapper instance with custom function registry.
	 *
	 * @param twin The SensiNact digital twin to update with mapped values
	 * @param mapping The ProviderMapping configuration defining the transformation rules
	 * @param functionRegistry Registry of function services keyed by functionId
	 */
	public ValueMapperImpl(SensinactDigitalTwin twin, ProviderMapping mapping) {
		this(twin, mapping, null);
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
		this.twin = requireNonNull(twin, "Digital twin cannot be null");
		this.mapping = requireNonNull(mapping, "Provider mapping cannot be null");
		this.providerModel = determineProviderModel(mapping);
		this.changeRuleFilter = changeRuleFilter;
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


			for (ServiceMapping serviceMapping : mapping.getServices()) {
				// Determine the source object for this service (may be from a collection).
				// A service whose filter matches nothing is not carried by this payload - a
				// device that splits its readings over several uplinks sends a different
				// subset each time, and the services it did not send keep their last values.
				Optional<EObject> serviceSource = getServiceSource(sourceInstance, serviceMapping);
				if (serviceSource.isEmpty()) {
					logger.fine(String.format("Instance carries no source for service %s - skipped",
							serviceMapping.getMid()));
					continue;
				}

				// Service timestamp takes precedence, fallback to provider timestamp
				Instant serviceTimestamp = determineTimestamp(serviceSource.get(), serviceMapping, providerTimestamp);
				mapServiceResources(serviceSource.get(), serviceMapping, provider, serviceTimestamp, providerId);
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
				// Determine the source object for this service (may be from a collection)
				Optional<EObject> source = getServiceSource(sourceInstance, serviceMapping);
				if (source.isEmpty()) {
					continue;
				}
				EObject serviceSource = source.get();

				// Add auto-generated resources from ReferenceMapping if configured. The
				// service source already is the referenced element - getServiceSource
				// resolved it through the very same ReferenceMapping.
				if (serviceMapping.getReferencedResource() != null) {
					ReferenceMapping refMapping = serviceMapping.getReferencedResource();

					for (EStructuralFeature attribute : getFilteredAttributes(serviceSource.eClass(), refMapping)) {
						String resourcePath = serviceMapping.getMid() + "." + attribute.getName();
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
					String resourcePath = serviceMapping.getMid() + "." + resourceMapping.getMid();

					try {
						Optional<Object> value = extractValue(serviceSource, resourceMapping);
						if (value.isPresent()) {
							values.put(resourcePath, value.get());
						}
					} catch (Exception e) {
						logger.warning(String.format("Failed to extract value for resource path %s: %s", resourcePath, e.getMessage()));
					}
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
	 * Extracts a value from an EObject instance using a ResourceMapping configuration.
	 */
	private Optional<Object> extractValue(EObject sourceInstance, ResourceMapping resourceMapping) {
		List<EStructuralFeature> featurePath = resourceMapping.getValueFeature();
		Optional<Object> rawValue = getRawValue(sourceInstance, featurePath);

		if (!rawValue.isPresent()) {
			return Optional.empty();
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
	 * Maps all resources for a specific service.
	 * Handles both explicitly defined resources and automatically generated resources from ReferenceMapping.
	 *
	 * Note: Auto-generated ResourceMappings from ReferenceMapping are created during model registration
	 * by ProviderModelSensinactMapper and are already present in serviceMapping.getResources().
	 */
	private void mapServiceResources(EObject sourceInstance, ServiceMapping serviceMapping,
			SensinactProvider provider, Instant timestamp, String providerId) throws ValueMappingException {

		SensinactService service = provider.getServices().get(serviceMapping.getMid());
		if (service == null) {
			throw new ValueMappingException("Service not found: " + serviceMapping.getMid());
		}

		// Map all resources (both auto-generated and explicitly defined)
		// Auto-generated resources are already in the list thanks to ProviderModelSensinactMapper
		for (ResourceMapping resourceMapping : serviceMapping.getResources()) {
			mapSingleResource(sourceInstance, resourceMapping, service, timestamp, providerId);
		}
//		Go over the temporary ResourceMapping. These are the ones automatically created when the referencedResource is set
		for (ResourceMapping resourceMapping : serviceMapping.getTemporaryResources()) {
			mapSingleResource(sourceInstance, resourceMapping, service, timestamp, providerId);
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
			SensinactService service, Instant timestamp, String providerId) throws ValueMappingException {

		SensinactResource resource = service.getResources().get(resourceMapping.getMid());
		
		
		if (resource == null) {
			throw new ValueMappingException("Resource not found: " + resourceMapping.getMid());
		}

		Optional<Object> value = extractValue(sourceInstance, resourceMapping);
		if (value.isPresent()) {
			// The resource's change rule decides whether this value reaches the twin at all.
			// Interim: the rule describes what the history provider should persist, and is
			// applied here until that provider can apply it itself.
			if (changeRuleFilter != null && !changeRuleFilter.accept(mapping.getMid(), providerId,
					service.getName(), resourceMapping, value.get(), timestamp)) {
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
			if (adminService.getResources().get(resourceMapping.getMid()) == null) {
				logger.warning(String.format("Mapping '%s': the admin service has no resource '%s' (it has %s) - skipped",
						mapping.getMid(), resourceMapping.getMid(), adminService.getResources().keySet()));
				continue;
			}
			mapSingleResource(sourceInstance, resourceMapping, adminService, timestamp, providerId);
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
		// Map friendlyName resource if configured
		if (!adminMapping.getFriendlyNameFeature().isEmpty()) {
			List<EStructuralFeature> featurePath = adminMapping.getFriendlyNameFeature();
			Optional<Object> friendlyNameValue = getRawValue(sourceInstance, featurePath);
			if (friendlyNameValue.isPresent()) {
				friendlyName = (String) friendlyNameValue.get();
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
		if (!adminMapping.getLatitudeRef().isEmpty() && !adminMapping.getLongitudeRef().isEmpty()) {
			Optional<Object> latValue = getRawValue(sourceInstance, adminMapping.getLatitudeRef());
			Optional<Object> lonValue = getRawValue(sourceInstance, adminMapping.getLongitudeRef());
			if (latValue.isPresent() && lonValue.isPresent()) {
				try {
					Coordinates coordinates = new Coordinates(toDouble(lonValue.get()), toDouble(latValue.get()));
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

		// First try: Extract value using the name mapping's feature path and collection index
		List<EStructuralFeature> featurePath = nameMapping.getFeaturePath();
		if (featurePath != null && !featurePath.isEmpty()) {
			Optional<Object> rawValue = getRawValue(sourceInstance, nameMapping);
			if (rawValue.isPresent() && rawValue.get() != null) {
				return rawValue.get().toString();
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

		// First try: Extract value using the timestamp mapping's feature path and collection index
		List<EStructuralFeature> featurePath = timestampMapping.getFeaturePath();
		if (featurePath != null && !featurePath.isEmpty()) {
			Optional<Object> rawValue = getRawValue(sourceInstance, timestampMapping);
			if (rawValue.isPresent() && rawValue.get() != null) {
				return convertToInstant(rawValue.get(), timestampMapping.getHint());
			}
		}

		// Second try: the static timestamp literal. EInstant is backed by String so that a mapping
		// stored in a Model Atlas survives being loaded dynamically (issue #61), so the literal is
		// parsed here - through the same conversion a string picked up from the feature path gets.
		String staticTimestamp = timestampMapping.getTimestamp();
		if (staticTimestamp != null && !staticTimestamp.isBlank()) {
			return convertToInstant(staticTimestamp.trim(), timestampMapping.getHint());
		}

		return null;
	}

	/**
	 * Converts a raw timestamp value to Instant, using optional hint for format patterns.
	 */
	private Instant convertToInstant(Object rawValue, String hint) {
		if (rawValue instanceof Instant) {
			return (Instant) rawValue;
		}
		if (rawValue instanceof Date) {
			return ((Date) rawValue).toInstant();
		}

		if (rawValue instanceof Long) {
			// Assume milliseconds since epoch
			return Instant.ofEpochMilli((Long) rawValue);
		}

		if (rawValue instanceof String) {
			String stringValue = (String) rawValue;
			boolean useHint = nonNull(hint) && !hint.trim().isEmpty();
			try {
				// If hint is provided, use it as DateTimeFormatter pattern first
				if (useHint) {
					DateTimeFormatter formatter = DateTimeFormatter.ofPattern(hint.trim());
					LocalDateTime localDateTime = LocalDateTime.parse(stringValue, formatter);
					return localDateTime.toInstant(ZoneOffset.UTC);
				} else {
					// Try parsing as ISO instant
					return Instant.parse(stringValue);
				}
			} catch (Exception e) {
				// If hint parsing failed, try ISO parsing as fallback
				if (useHint) {
					try {
						return Instant.parse(stringValue);
					} catch (Exception isoException) {
						logger.warning(String.format("Failed to parse timestamp string '%s' using pattern '%s' and as ISO instant: %s", stringValue, hint, e.getMessage()));
					}
				} else {
					logger.warning(String.format("Failed to parse timestamp string '%s' as ISO instant: %s", stringValue, e.getMessage()));
				}
			}
		}

		logger.warning(String.format("Cannot convert timestamp value of type %s to Instant: %s", rawValue.getClass().getName(), rawValue));
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
		List<EStructuralFeature> featurePath = resourceMapping.getValueFeature();
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
	 * Traverses a path of {@link EStructuralFeature}s to retrieve a raw, untyped value.
	 * First checks if a functionId is specified and invokes the registered function.
	 * If no function is found or no functionId is specified, falls back to feature path traversal.
	 * Handles multi-value features by using the collectionIndex from the FeatureMapping.
	 *
	 * @param source         The starting object.
	 * @param featureMapping The feature mapping containing the path and collection index.
	 * @return An {@link Optional} containing the raw value, or empty if not found.
	 */
	private Optional<Object> getRawValue(EObject source, FeatureMapping featureMapping) {
		if (featureMapping == null) {
			return Optional.empty();
		}
		// First priority: Use feature path if available
		List<EStructuralFeature> featurePath = featureMapping.getFeaturePath();
		if (featurePath == null || featurePath.isEmpty()) {
			return Optional.empty();
		}

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
