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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.eclipse.emf.common.util.URI;
import org.eclipse.emf.ecore.EAnnotation;
import org.eclipse.emf.ecore.EcoreFactory;
import org.eclipse.emf.ecore.InternalEObject;
import org.eclipse.fennec.event.atlas.mapping.MappingProfileRegistry.ValidationResult;
import org.eclipse.fennec.event.atlas.mapping.SensinactMapperConstants;
import org.eclipse.fennec.event.atlas.model.mapping.MappingFactory;
import org.eclipse.fennec.event.atlas.model.mapping.MappingProfile;
import org.eclipse.fennec.event.atlas.model.mapping.ProfileProvider;
import org.eclipse.fennec.event.atlas.model.mapping.ProfileResource;
import org.eclipse.fennec.event.atlas.model.mapping.ProfileService;
import org.eclipse.fennec.event.atlas.model.mapping.ProviderMapping;
import org.eclipse.fennec.event.atlas.model.mapping.ResourceMapping;
import org.eclipse.fennec.event.atlas.model.mapping.ServiceMapping;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/**
 * Conformance validation against a profile that is not usable: an unresolved reference or one
 * without a provider structure has to be reported, not dereferenced.
 */
public class MappingProfileValidationTest {

	@Test
	@DisplayName("A mapping without a profile validates")
	void noProfile_isValid() {
		ProviderMapping mapping = mapping();

		ValidationResult result = new MappingProfileRegistryImpl().validateMapping(mapping);

		assertTrue(result.isValid());
	}

	@Test
	@DisplayName("An unresolved profile reference is an error, not a NullPointerException")
	void proxyProfile_isReportedAsAnError() {
		ProviderMapping mapping = mapping();
		MappingProfile proxy = MappingFactory.eINSTANCE.createMappingProfile();
		((InternalEObject) proxy).eSetProxyURI(URI.createURI("battery-sensor-profile.xmi#battery-sensor"));
		mapping.setProfile(proxy);

		// Every feature of a proxy is null, getProvider() included - reading one used to throw
		// from inside registerModelMapping, where the message was lost and the mapping silently
		// never reached the twin.
		ValidationResult result = new MappingProfileRegistryImpl().validateMapping(mapping);

		assertFalse(result.isValid());
		assertEquals(1, result.getErrors().size());
		assertTrue(result.getErrors().get(0).contains("battery-sensor-profile.xmi#battery-sensor"),
				() -> "the error should name the unresolved reference: " + result.getErrors());
	}

	@Test
	@DisplayName("A profile without a provider structure is an error, not a NullPointerException")
	void profileWithoutProvider_isReportedAsAnError() {
		ProviderMapping mapping = mapping();
		MappingProfile profile = MappingFactory.eINSTANCE.createMappingProfile();
		profile.setProfileId("no-provider");
		mapping.setProfile(profile);

		ValidationResult result = new MappingProfileRegistryImpl().validateMapping(mapping);

		assertFalse(result.isValid());
		assertTrue(result.getErrors().get(0).contains("no-provider"),
				() -> "the error should name the profile: " + result.getErrors());
	}

	@Test
	@DisplayName("A unit from the sensinact.mapping annotation satisfies the profile's expected unit")
	void annotationSuppliedUnit_satisfiesTheProfile() {
		// How a resource generated from a ReferenceMapping gets its unit: the source
		// attribute's annotation is copied onto the mapping, leaving the `unit` field unset.
		ResourceMapping windSpeed = resource("windSpeed", null);
		annotateUnit(windSpeed, "m/s");

		ValidationResult result = validateAgainstProfileExpecting("m/s", windSpeed);

		assertTrue(result.getWarnings().isEmpty(),
				() -> "an annotation-supplied unit should not warn: " + result.getWarnings());
	}

	@Test
	@DisplayName("The unit field wins over the annotation, and a real mismatch still warns")
	void unitField_winsAndMismatchesStillWarn() {
		// A binding or a converting mapping overrides the model's unit through the field.
		ResourceMapping windSpeed = resource("windSpeed", "kn");
		annotateUnit(windSpeed, "m/s");

		assertTrue(validateAgainstProfileExpecting("kn", windSpeed).getWarnings().isEmpty(),
				"the field is what the twin publishes, so it is what the profile is checked against");

		ValidationResult mismatch = validateAgainstProfileExpecting("m/s", windSpeed);
		assertEquals(1, mismatch.getWarnings().size(), () -> mismatch.getWarnings().toString());
		assertTrue(mismatch.getWarnings().get(0).contains("'kn'"),
				() -> "the warning should report the effective unit: " + mismatch.getWarnings());
	}

	@Test
	@DisplayName("A resource with no unit at all still warns when the profile expects one")
	void noUnit_warns() {
		ValidationResult result = validateAgainstProfileExpecting("m/s", resource("windSpeed", null));

		assertEquals(1, result.getWarnings().size(), () -> result.getWarnings().toString());
	}

	/**
	 * Validates a mapping holding <code>resource</code> in service {@code weather} against a
	 * profile that expects <code>expectedUnit</code> for it.
	 */
	private static ValidationResult validateAgainstProfileExpecting(String expectedUnit, ResourceMapping resource) {
		MappingFactory f = MappingFactory.eINSTANCE;

		ProfileResource profileResource = f.createProfileResource();
		profileResource.setResourceId(resource.getMid());
		profileResource.setExpectedUnit(expectedUnit);
		ProfileService profileService = f.createProfileService();
		profileService.setServiceId("weather");
		profileService.getResources().add(profileResource);
		ProfileProvider provider = f.createProfileProvider();
		provider.setProviderId("p");
		provider.getServices().add(profileService);
		MappingProfile profile = f.createMappingProfile();
		profile.setProfileId("unit-profile");
		profile.setProvider(provider);

		ServiceMapping service = f.createServiceMapping();
		service.setMid("weather");
		service.getResources().add(resource);
		ProviderMapping mapping = mapping();
		mapping.getServices().add(service);
		mapping.setProfile(profile);

		return new MappingProfileRegistryImpl().validateMapping(mapping);
	}

	private static ResourceMapping resource(String mid, String unit) {
		ResourceMapping resource = MappingFactory.eINSTANCE.createResourceMapping();
		resource.setMid(mid);
		resource.setUnit(unit);
		return resource;
	}

	private static void annotateUnit(ResourceMapping resource, String unit) {
		EAnnotation annotation = EcoreFactory.eINSTANCE.createEAnnotation();
		annotation.setSource(SensinactMapperConstants.SENSINACT_MAPPING_ANNOTATION_SOURCE);
		annotation.getDetails().put(SensinactMapperConstants.SENSINACT_MAPPING_UNIT, unit);
		resource.getEAnnotations().add(annotation);
	}

	private static ProviderMapping mapping() {
		ProviderMapping mapping = MappingFactory.eINSTANCE.createProviderMapping();
		mapping.setMid("test-mapping");
		return mapping;
	}

}
