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
import org.eclipse.emf.ecore.InternalEObject;
import org.eclipse.fennec.event.atlas.mapping.MappingProfileRegistry.ValidationResult;
import org.eclipse.fennec.event.atlas.model.mapping.MappingFactory;
import org.eclipse.fennec.event.atlas.model.mapping.MappingProfile;
import org.eclipse.fennec.event.atlas.model.mapping.ProviderMapping;
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

	private static ProviderMapping mapping() {
		ProviderMapping mapping = MappingFactory.eINSTANCE.createProviderMapping();
		mapping.setMid("test-mapping");
		return mapping;
	}

}
