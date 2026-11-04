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

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.RETURNS_DEEP_STUBS;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.Instant;
import java.util.Map;

import org.eclipse.emf.ecore.EAttribute;
import org.eclipse.emf.ecore.EClass;
import org.eclipse.emf.ecore.EDataType;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.EPackage;
import org.eclipse.emf.ecore.EcoreFactory;
import org.eclipse.emf.ecore.EcorePackage;
import org.eclipse.emf.ecore.util.EcoreUtil;
import org.eclipse.fennec.event.atlas.mapping.ValueMappingException;
import org.eclipse.fennec.event.atlas.model.mapping.MappingFactory;
import org.eclipse.fennec.event.atlas.model.mapping.NameMapping;
import org.eclipse.fennec.event.atlas.model.mapping.ProviderMapping;
import org.eclipse.fennec.event.atlas.model.mapping.ResourceMapping;
import org.eclipse.fennec.event.atlas.model.mapping.ServiceMapping;
import org.eclipse.fennec.event.atlas.model.mapping.ValueMapping;
import org.eclipse.sensinact.core.model.Model;
import org.eclipse.sensinact.core.model.SensinactModelManager;
import org.eclipse.sensinact.core.twin.SensinactDigitalTwin;
import org.eclipse.sensinact.core.twin.SensinactProvider;
import org.eclipse.sensinact.core.twin.SensinactResource;
import org.eclipse.sensinact.core.twin.SensinactService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/**
 * What a service is called in the twin.
 * <p>
 * The mid, unless the mapping's NameMapping <em>produces</em> a name from the payload. The
 * constant deliberately does not win: every mapping written so far carries one as a label
 * beside a differing mid, so reading it as the twin name would rename all of them at once.
 * That is what {@link #constantName_usesTheMid()} guards.
 */
public class ServiceNameMappingTest {

	private final EPackage ePackage = EcoreFactory.eINSTANCE.createEPackage();
	private final EClass reading = EcoreFactory.eINSTANCE.createEClass();
	private final EAttribute id = attribute("id", EcorePackage.Literals.ESTRING);
	private final EAttribute count = attribute("count", EcorePackage.Literals.EINT);

	private final SensinactDigitalTwin twin = mock(SensinactDigitalTwin.class);
	private final SensinactProvider provider = mock(SensinactProvider.class);
	private final SensinactModelManager modelManager = mock(SensinactModelManager.class);

	/** The resource of the service named by the mid. */
	private final SensinactResource byMid = mock(SensinactResource.class);
	/** The resource of a service named by anything else. */
	private final SensinactResource byName = mock(SensinactResource.class);

	@BeforeEach
	void modelAndTwin() {
		ePackage.setName("readings");
		ePackage.setNsPrefix("readings");
		ePackage.setNsURI("http://example.org/event.atlas/readings/1.0");
		reading.setName("Reading");
		reading.getEStructuralFeatures().add(id);
		reading.getEStructuralFeatures().add(count);
		ePackage.getEClassifiers().add(reading);

		when(twin.getProvider(anyString(), anyString())).thenReturn(provider);
	}

	@Test
	@DisplayName("A service with no name mapping keeps its mid")
	void absentName_usesTheMid() throws ValueMappingException {
		servicesInTwin(Map.of("data", service(byMid)));

		map(null);

		verify(byMid).setValue(eq(7), any(Instant.class));
	}

	@Test
	@DisplayName("A constant name is a label, not the service name - the mid still wins")
	void constantName_usesTheMid() throws ValueMappingException {
		servicesInTwin(Map.of("data", service(byMid), "Readings", service(byName)));
		NameMapping name = MappingFactory.eINSTANCE.createNameMapping();
		name.setName("Readings");

		map(name);

		verify(byMid).setValue(eq(7), any(Instant.class));
		verify(byName, never()).setValue(any(), any(Instant.class));
	}

	@Test
	@DisplayName("A produced name names the service")
	void producedName_namesTheService() throws ValueMappingException {
		servicesInTwin(Map.of("data", service(byMid), "line-7", service(byName)));
		NameMapping name = MappingFactory.eINSTANCE.createNameMapping();
		name.getFeaturePath().add(id);

		map(name);

		verify(byName).setValue(eq(7), any(Instant.class));
		verify(byMid, never()).setValue(any(), any(Instant.class));
	}

	@Test
	@DisplayName("A produced name the model does not carry yet is created on it")
	void producedName_missingService_isCreatedOnTheModel() throws ValueMappingException {
		// Empty on the first look, carrying the service after the model gained it - which is
		// what the twin does: a provider picks up a service its model gained (LateServiceOnModelTest).
		doReturn(Map.of(), Map.of("line-7", service(byName))).when(provider).getServices();
		Model model = mock(Model.class, RETURNS_DEEP_STUBS);
		doReturn(Map.of()).when(model).getServices();
		when(modelManager.getModel(anyString())).thenReturn(model);

		NameMapping name = MappingFactory.eINSTANCE.createNameMapping();
		name.getFeaturePath().add(id);

		map(name, modelManager);

		verify(model).createService("line-7");
		verify(byName).setValue(eq(7), any(Instant.class));
	}

	@Test
	@DisplayName("A producer that yields nothing falls back to the constant")
	void producerYieldsNothing_fallsBackToTheConstant() throws ValueMappingException {
		servicesInTwin(Map.of("data", service(byMid), "Readings", service(byName)));
		NameMapping name = MappingFactory.eINSTANCE.createNameMapping();
		name.getFeaturePath().add(id);
		name.setName("Readings");

		map(name, null, instanceWithoutId());

		verify(byName).setValue(eq(7), any(Instant.class));
	}

	@Test
	@DisplayName("A producer that yields nothing and has no constant falls back to the mid")
	void producerYieldsNothing_noConstant_fallsBackToTheMid() throws ValueMappingException {
		servicesInTwin(Map.of("data", service(byMid)));
		NameMapping name = MappingFactory.eINSTANCE.createNameMapping();
		name.getFeaturePath().add(id);

		map(name, null, instanceWithoutId());

		verify(byMid).setValue(eq(7), any(Instant.class));
	}

	@Test
	@DisplayName("Without a model manager a payload-named service is skipped, not an error")
	void producedName_noModelManager_skipsTheService() throws ValueMappingException {
		servicesInTwin(Map.of());
		NameMapping name = MappingFactory.eINSTANCE.createNameMapping();
		name.getFeaturePath().add(id);

		map(name);

		verify(byName, never()).setValue(any(), any(Instant.class));
		verify(byMid, never()).setValue(any(), any(Instant.class));
	}

	@Test
	@DisplayName("A statically named service that the twin does not carry is still an error")
	void staticName_missingService_stillFails() {
		servicesInTwin(Map.of());

		assertThrows(ValueMappingException.class, () -> map(null),
				"a missing static service is a broken mapping, and creating it would hide that");
	}

	private void servicesInTwin(Map<String, SensinactService> services) {
		doReturn(services).when(provider).getServices();
	}

	private SensinactService service(SensinactResource resource) {
		SensinactService service = mock(SensinactService.class);
		doReturn(Map.of("value", resource)).when(service).getResources();
		return service;
	}

	private void map(NameMapping serviceName) throws ValueMappingException {
		map(serviceName, null, instance());
	}

	private void map(NameMapping serviceName, SensinactModelManager manager) throws ValueMappingException {
		map(serviceName, manager, instance());
	}

	private void map(NameMapping serviceName, SensinactModelManager manager, EObject instance)
			throws ValueMappingException {
		new ValueMapperImpl(twin, manager, mapping(serviceName), null, null, null, null).mapInstance(instance);
	}

	private ProviderMapping mapping(NameMapping serviceName) {
		ValueMapping valueMapping = MappingFactory.eINSTANCE.createValueMapping();
		valueMapping.getFeaturePath().add(count);

		ResourceMapping resource = MappingFactory.eINSTANCE.createResourceMapping();
		resource.setMid("value");
		resource.setName("value");
		resource.setEType(EcorePackage.Literals.EINT);
		resource.setValueMapping(valueMapping);

		ServiceMapping service = MappingFactory.eINSTANCE.createServiceMapping();
		service.setMid("data");
		service.setName(serviceName);
		service.getResources().add(resource);

		NameMapping providerName = MappingFactory.eINSTANCE.createNameMapping();
		providerName.setName("reader");

		ProviderMapping mapping = MappingFactory.eINSTANCE.createProviderMapping();
		mapping.setMid("readings");
		mapping.setName(providerName);
		mapping.getProviderClasses().add(reading);
		mapping.getServices().add(service);
		return mapping;
	}

	private EObject instance() {
		EObject instance = instanceWithoutId();
		instance.eSet(id, "line-7");
		return instance;
	}

	private EObject instanceWithoutId() {
		EObject instance = EcoreUtil.create(reading);
		instance.eSet(count, 7);
		return instance;
	}

	private static EAttribute attribute(String name, EDataType type) {
		EAttribute attribute = EcoreFactory.eINSTANCE.createEAttribute();
		attribute.setName(name);
		attribute.setEType(type);
		return attribute;
	}
}
