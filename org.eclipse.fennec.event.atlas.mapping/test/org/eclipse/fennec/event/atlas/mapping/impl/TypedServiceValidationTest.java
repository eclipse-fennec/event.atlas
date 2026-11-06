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
 *      Data In Motion - initial API and implementation
 */
package org.eclipse.fennec.event.atlas.mapping.impl;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;

import org.eclipse.emf.common.util.URI;
import org.eclipse.emf.ecore.EAttribute;
import org.eclipse.emf.ecore.EClass;
import org.eclipse.emf.ecore.EPackage;
import org.eclipse.emf.ecore.EcoreFactory;
import org.eclipse.emf.ecore.EcorePackage;
import org.eclipse.emf.ecore.InternalEObject;
import org.eclipse.fennec.event.atlas.model.mapping.MappingFactory;
import org.eclipse.fennec.event.atlas.model.mapping.ProviderMapping;
import org.eclipse.fennec.event.atlas.model.mapping.ReferenceMapping;
import org.eclipse.fennec.event.atlas.model.mapping.ResourceMapping;
import org.eclipse.fennec.event.atlas.model.mapping.ServiceMapping;
import org.eclipse.sensinact.model.core.provider.ProviderPackage;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/**
 * The gate for a service that names its EClass (#91 item 8).
 * <p>
 * Such a service is built by the twin from the EClass, so it carries exactly what the EClass
 * declares. A resource mapping that names anything else is refused at registration rather than
 * skipped per payload, because the one way to make it work - adding the feature to the EClass -
 * is also the operation that breaks a generated EClass for the whole runtime.
 */
public class TypedServiceValidationTest {

	private final EPackage ePackage = EcoreFactory.eINSTANCE.createEPackage();
	private final EClass thermal = EcoreFactory.eINSTANCE.createEClass();

	@BeforeEach
	void serviceModel() {
		ePackage.setName("thermal");
		ePackage.setNsPrefix("thermal");
		ePackage.setNsURI("http://example.org/event.atlas/thermal/1.0");
		thermal.setName("Thermal");
		thermal.getESuperTypes().add(ProviderPackage.Literals.SERVICE);
		thermal.getEStructuralFeatures().add(attribute("temperature"));
		ePackage.getEClassifiers().add(thermal);
	}

	@Test
	@DisplayName("A resource the EClass declares is accepted")
	void declaredResource_isAccepted() {
		assertEquals(List.of(), ProviderMappingRegistryImpl.refusedTypedServices(mapping(thermal, "temperature")));
	}

	@Test
	@DisplayName("A resource the EClass does not declare is refused, and the message says what it has")
	void undeclaredResource_isRefused() {
		List<String> refusals = ProviderMappingRegistryImpl.refusedTypedServices(mapping(thermal, "humidity"));

		assertEquals(1, refusals.size(), () -> "exactly one refusal, but: " + refusals);
		assertTrue(refusals.get(0).contains("humidity"), () -> refusals.get(0));
		assertTrue(refusals.get(0).contains("temperature"),
				() -> "the message should name what the EClass has: " + refusals.get(0));
	}

	@Test
	@DisplayName("metadata is refused although Service declares it, because it is no resource")
	void metadata_isRefused() {
		assertEquals(1, ProviderMappingRegistryImpl.refusedTypedServices(mapping(thermal, "metadata")).size());
	}

	@Test
	@DisplayName("A referencedResource is kept: it also selects the service's source element")
	void referencedResource_isAccepted() {
		ProviderMapping mapping = mapping(thermal, "temperature");
		ReferenceMapping referenced = MappingFactory.eINSTANCE.createReferenceMapping();
		mapping.getServices().get(0).setReferencedResource(referenced);

		assertEquals(List.of(), ProviderMappingRegistryImpl.refusedTypedServices(mapping),
				"one block becomes one service per element through this path, typed or not;"
						+ " what it generates is pruned at registration instead");
	}

	@Test
	@DisplayName("A service without an EClass is left alone, whatever it maps")
	void untypedService_isNotJudged() {
		assertEquals(List.of(), ProviderMappingRegistryImpl.refusedTypedServices(mapping(null, "anything")));
	}

	@Test
	@DisplayName("An EClass that is still a proxy is reported, so the mapping waits for its model")
	void proxyEClass_isReported() {
		EClass proxy = EcoreFactory.eINSTANCE.createEClass();
		((InternalEObject) proxy).eSetProxyURI(URI.createURI("http://example.org/not/deployed#//Thermal"));

		assertEquals(List.of("thermal"), ProviderMappingRegistryImpl.unresolvedServiceEClasses(
				mapping(proxy, "temperature")));
		assertEquals(List.of(), ProviderMappingRegistryImpl.unresolvedServiceEClasses(
				mapping(thermal, "temperature")));
	}

	private ProviderMapping mapping(EClass serviceEClass, String... resourceMids) {
		ServiceMapping service = MappingFactory.eINSTANCE.createServiceMapping();
		service.setMid("thermal");
		service.setServiceEClass(serviceEClass);
		for (String mid : resourceMids) {
			ResourceMapping resource = MappingFactory.eINSTANCE.createResourceMapping();
			resource.setMid(mid);
			service.getResources().add(resource);
		}
		ProviderMapping mapping = MappingFactory.eINSTANCE.createProviderMapping();
		mapping.setMid("test-mapping");
		mapping.getServices().add(service);
		return mapping;
	}

	private static EAttribute attribute(String name) {
		EAttribute attribute = EcoreFactory.eINSTANCE.createEAttribute();
		attribute.setName(name);
		attribute.setEType(EcorePackage.Literals.ESTRING);
		return attribute;
	}
}
