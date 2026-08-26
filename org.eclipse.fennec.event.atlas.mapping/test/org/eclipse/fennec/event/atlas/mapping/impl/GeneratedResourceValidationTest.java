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

import java.util.List;

import org.eclipse.emf.ecore.EAttribute;
import org.eclipse.emf.ecore.EClass;
import org.eclipse.emf.ecore.EcoreFactory;
import org.eclipse.emf.ecore.EcorePackage;
import org.eclipse.fennec.event.atlas.mapping.MappingProfileRegistry.ValidationResult;
import org.eclipse.fennec.event.atlas.model.mapping.MappingFactory;
import org.eclipse.fennec.event.atlas.model.mapping.MappingProfile;
import org.eclipse.fennec.event.atlas.model.mapping.ProfileProvider;
import org.eclipse.fennec.event.atlas.model.mapping.ProfileResource;
import org.eclipse.fennec.event.atlas.model.mapping.ProfileService;
import org.eclipse.fennec.event.atlas.model.mapping.ProviderMapping;
import org.eclipse.fennec.event.atlas.model.mapping.ReferenceMapping;
import org.eclipse.fennec.event.atlas.model.mapping.ResourceMapping;
import org.eclipse.fennec.event.atlas.model.mapping.ServiceMapping;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/**
 * Profile conformance of a service built from a {@code ReferenceMapping}: its resources exist
 * only in {@code temporaryResources}, generated during registration, and validation has to see
 * them.
 */
public class GeneratedResourceValidationTest {

	private static final String SERVICE = "weather";

	@Test
	@DisplayName("A profile resource satisfied by a generated resource validates")
	void generatedResource_satisfiesTheProfile() {
		ProviderMapping mapping = mappingWithAReference();

		generate(mapping);
		ValidationResult result = new MappingProfileRegistryImpl().validateMapping(mapping);

		assertTrue(result.isValid(), () -> "should be conformant: " + result.getErrors());
		assertTrue(result.getWarnings().isEmpty(), () -> result.getWarnings().toString());
	}

	@Test
	@DisplayName("A profile resource nothing generates is still reported as missing")
	void resourceNothingGenerates_isMissing() {
		ProviderMapping mapping = mappingWithAReference();
		// The profile asks for a resource the referenced type has no attribute for
		profileOf(mapping).getProvider().getServices().get(0).getResources().get(0)
				.setResourceId("notAnAttribute");

		generate(mapping);
		ValidationResult result = new MappingProfileRegistryImpl().validateMapping(mapping);

		assertFalse(result.isValid(), "a genuinely absent resource must still fail");
		assertTrue(result.getErrors().get(0).contains("notAnAttribute"),
				() -> result.getErrors().toString());
	}

	@Test
	@DisplayName("An explicit resource shadows a generated one of the same mid")
	void explicitResource_shadowsTheGeneratedOne() {
		ProviderMapping mapping = mappingWithAReference();
		ServiceMapping service = mapping.getServices().get(0);

		// Same mid as the generated resource, with the unit the profile expects
		ResourceMapping explicit = MappingFactory.eINSTANCE.createResourceMapping();
		explicit.setMid("windSpeed");
		explicit.setUnit("m/s");
		service.getResources().add(explicit);
		profileOf(mapping).getProvider().getServices().get(0).getResources().get(0).setExpectedUnit("m/s");

		generate(mapping);
		ValidationResult result = new MappingProfileRegistryImpl().validateMapping(mapping);

		assertTrue(result.getWarnings().isEmpty(),
				() -> "the explicit resource carries the unit, so no mismatch: " + result.getWarnings());
	}

	@Test
	@DisplayName("Generating twice does not duplicate the generated resources")
	void generatingTwice_isIdempotent() {
		ProviderMapping mapping = mappingWithAReference();

		generate(mapping);
		generate(mapping);

		List<ResourceMapping> generated = mapping.getServices().get(0).getTemporaryResources();
		assertEquals(1, generated.size(), () -> "expected one resource, got "
				+ generated.stream().map(ResourceMapping::getMid).toList());
	}

	private static void generate(ProviderMapping mapping) {
		new ProviderModelSensinactMapper.Factory(null).createMapper(null, null)
				.generateReferencedResources(mapping);
	}

	private static MappingProfile profileOf(ProviderMapping mapping) {
		return mapping.getProfile();
	}

	/**
	 * A mapping whose only service is built from a reference to a type with a single
	 * {@code windSpeed} attribute, plus a profile requiring exactly that resource.
	 */
	private static ProviderMapping mappingWithAReference() {
		MappingFactory f = MappingFactory.eINSTANCE;

		EAttribute windSpeed = EcoreFactory.eINSTANCE.createEAttribute();
		windSpeed.setName("windSpeed");
		windSpeed.setEType(EcorePackage.eINSTANCE.getEFloat());
		EClass report = EcoreFactory.eINSTANCE.createEClass();
		report.setName("Report");
		report.getEStructuralFeatures().add(windSpeed);

		ReferenceMapping reference = f.createReferenceMapping();
		reference.setTargetEClass(report);
		reference.setExclude(true);

		ServiceMapping service = f.createServiceMapping();
		service.setMid(SERVICE);
		service.setReferencedResource(reference);

		ProfileResource profileResource = f.createProfileResource();
		profileResource.setResourceId("windSpeed");
		profileResource.setRequired(true);
		ProfileService profileService = f.createProfileService();
		profileService.setServiceId(SERVICE);
		profileService.getResources().add(profileResource);
		ProfileProvider provider = f.createProfileProvider();
		provider.setProviderId("weather-station");
		provider.getServices().add(profileService);
		MappingProfile profile = f.createMappingProfile();
		profile.setProfileId("weather-station");
		profile.setProvider(provider);

		ProviderMapping mapping = f.createProviderMapping();
		mapping.setMid("generated-resource-mapping");
		mapping.getServices().add(service);
		mapping.setProfile(profile);
		return mapping;
	}

}
