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
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.Instant;
import java.util.List;
import java.util.Map;

import org.eclipse.emf.ecore.EAttribute;
import org.eclipse.emf.ecore.EClass;
import org.eclipse.emf.ecore.EDataType;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.EPackage;
import org.eclipse.emf.ecore.EReference;
import org.eclipse.emf.ecore.EcoreFactory;
import org.eclipse.emf.ecore.EcorePackage;
import org.eclipse.emf.ecore.util.EcoreUtil;
import org.eclipse.fennec.event.atlas.mapping.ChangeRuleFilter;
import org.eclipse.fennec.event.atlas.mapping.ValueMappingException;
import org.eclipse.fennec.event.atlas.model.mapping.MappingFactory;
import org.eclipse.fennec.event.atlas.model.mapping.NameMapping;
import org.eclipse.fennec.event.atlas.model.mapping.ProviderMapping;
import org.eclipse.fennec.event.atlas.model.mapping.ReferenceMapping;
import org.eclipse.fennec.event.atlas.model.mapping.ResourceMapping;
import org.eclipse.fennec.event.atlas.model.mapping.ServiceMapping;
import org.eclipse.fennec.event.atlas.model.mapping.ValueMapping;
import org.eclipse.sensinact.core.twin.SensinactDigitalTwin;
import org.eclipse.sensinact.core.twin.SensinactProvider;
import org.eclipse.sensinact.core.twin.SensinactResource;
import org.eclipse.sensinact.core.twin.SensinactService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/**
 * One service block, one service for each element of a collection.
 * <p>
 * The trigger is intrinsic: the path keeps a collection <em>and</em> the service's NameMapping
 * produces a name. A set of services needs a name for each, and only a producer can give one -
 * so a collection with a constant name keeps selecting a single element, which is what every
 * mapping written before this feature means by it.
 */
public class ForEachServiceTest {

	private final EPackage ePackage = EcoreFactory.eINSTANCE.createEPackage();
	private final EClass counter = EcoreFactory.eINSTANCE.createEClass();
	private final EClass line = EcoreFactory.eINSTANCE.createEClass();
	private final EAttribute lineName = attribute("name", EcorePackage.Literals.ESTRING);
	private final EAttribute lineCount = attribute("count", EcorePackage.Literals.EINT);
	private final EReference lines = EcoreFactory.eINSTANCE.createEReference();
	/** A second level of nesting, as the ILSA path has three. */
	private final EClass group = EcoreFactory.eINSTANCE.createEClass();
	private final EReference groupLines = EcoreFactory.eINSTANCE.createEReference();
	private final EReference groups = EcoreFactory.eINSTANCE.createEReference();

	private final SensinactDigitalTwin twin = mock(SensinactDigitalTwin.class);
	private final SensinactProvider provider = mock(SensinactProvider.class);

	private final SensinactResource valueOfA = mock(SensinactResource.class);
	private final SensinactResource valueOfB = mock(SensinactResource.class);
	private final SensinactResource valueOfMid = mock(SensinactResource.class);

	@BeforeEach
	void modelAndTwin() {
		ePackage.setName("counters");
		ePackage.setNsPrefix("counters");
		ePackage.setNsURI("http://example.org/event.atlas/counters/1.0");
		line.setName("Line");
		line.getEStructuralFeatures().add(lineName);
		line.getEStructuralFeatures().add(lineCount);
		lines.setName("lines");
		lines.setEType(line);
		lines.setContainment(true);
		lines.setUpperBound(-1);
		groupLines.setName("groupLines");
		groupLines.setEType(line);
		groupLines.setContainment(true);
		groupLines.setUpperBound(-1);
		group.setName("Group");
		group.getEStructuralFeatures().add(groupLines);
		groups.setName("groups");
		groups.setEType(group);
		groups.setContainment(true);
		groups.setUpperBound(-1);
		counter.setName("Counter");
		counter.getEStructuralFeatures().add(lines);
		counter.getEStructuralFeatures().add(groups);
		ePackage.getEClassifiers().add(line);
		ePackage.getEClassifiers().add(group);
		ePackage.getEClassifiers().add(counter);

		doReturn(Map.of("a", service(valueOfA), "b", service(valueOfB), "line", service(valueOfMid)))
				.when(provider).getServices();
		when(twin.getProvider(anyString(), anyString())).thenReturn(provider);
	}

	@Test
	@DisplayName("A produced name over a collection makes one service for each element")
	void producedName_overCollection_isOneServicePerElement() throws ValueMappingException {
		map(producedName(), null, 0);

		verify(valueOfA).setValue(eq(1), any(Instant.class));
		verify(valueOfB).setValue(eq(2), any(Instant.class));
	}

	@Test
	@DisplayName("A constant name over a collection still selects a single element")
	void constantName_overCollection_staysOneService() throws ValueMappingException {
		NameMapping constant = MappingFactory.eINSTANCE.createNameMapping();
		constant.setName("Lines");

		map(constant, null, 0);

		// The mid names it, index 0 selects the element - exactly as before the feature.
		verify(valueOfMid).setValue(eq(1), any(Instant.class));
		verify(valueOfA, never()).setValue(any(), any(Instant.class));
		verify(valueOfB, never()).setValue(any(), any(Instant.class));
	}

	@Test
	@DisplayName("A collectionFilter narrows the set, and every survivor is a service")
	void filter_narrowsTheSet() throws ValueMappingException {
		map(producedName(), "name=a", 0);

		verify(valueOfA).setValue(eq(1), any(Instant.class));
		verify(valueOfB, never()).setValue(any(), any(Instant.class));
	}

	@Test
	@DisplayName("A non-zero collectionIndex still selects one element beside a produced name")
	void nonZeroIndex_selectsOne() throws ValueMappingException {
		map(producedName(), null, 1);

		verify(valueOfB).setValue(eq(2), any(Instant.class));
		verify(valueOfA, never()).setValue(any(), any(Instant.class));
	}

	@Test
	@DisplayName("Each service of the set keeps its own change-rule state, keyed by its twin name")
	void changeRuleFilter_seesEachServiceName() throws ValueMappingException {
		ChangeRuleFilter filter = mock(ChangeRuleFilter.class);
		when(filter.accept(anyString(), anyString(), anyString(), any(), any(), any())).thenReturn(true);

		new ValueMapperImpl(twin, null, mapping(producedName(), null, 0), filter, null, null, null)
				.mapInstance(instance());

		verify(filter).accept(eq("readings"), anyString(), eq("a"), any(), eq(1), any());
		verify(filter).accept(eq("readings"), anyString(), eq("b"), any(), eq(2), any());
	}

	@Test
	@DisplayName("mapResourceValues keys one group per element, not one that overwrites itself")
	void mapResourceValues_keysByProducedName() {
		Map<String, Object> values = new ValueMapperImpl(twin, null, mapping(producedName(), null, 0), null,
				null, null, null).mapResourceValues(instance());

		assertEquals(1, values.get("a.value"));
		assertEquals(2, values.get("b.value"));
	}

	@Test
	@DisplayName("A path that crosses two collections is flattened, one service per leaf")
	void nestedCollections_areFlattened() throws ValueMappingException {
		// The shape of the ILSA configuration, which reaches its signals through
		// subcircles -> serialPorts -> modules -> signals.
		ProviderMapping nested = mapping(producedName(), null, 0);
		ReferenceMapping referenced = nested.getServices().get(0).getReferencedResource();
		referenced.getFeaturePath().clear();
		referenced.getFeaturePath().add(groups);
		referenced.getFeaturePath().add(groupLines);

		EObject instance = EcoreUtil.create(counter);
		@SuppressWarnings("unchecked")
		List<EObject> collection = (List<EObject>) instance.eGet(groups);
		collection.add(groupOf("a", 1));
		collection.add(groupOf("b", 2));

		new ValueMapperImpl(twin, null, nested, null, null, null, null).mapInstance(instance);

		verify(valueOfA).setValue(eq(1), any(Instant.class));
		verify(valueOfB).setValue(eq(2), any(Instant.class));
	}

	private EObject groupOf(String name, int count) {
		EObject element = EcoreUtil.create(group);
		@SuppressWarnings("unchecked")
		List<EObject> collection = (List<EObject>) element.eGet(groupLines);
		collection.add(lineOf(name, count));
		return element;
	}

	private NameMapping producedName() {
		NameMapping name = MappingFactory.eINSTANCE.createNameMapping();
		name.getFeaturePath().add(lineName);
		return name;
	}

	private void map(NameMapping serviceName, String collectionFilter, int collectionIndex)
			throws ValueMappingException {
		new ValueMapperImpl(twin, null, mapping(serviceName, collectionFilter, collectionIndex), null, null,
				null, null).mapInstance(instance());
	}

	private ProviderMapping mapping(NameMapping serviceName, String collectionFilter, int collectionIndex) {
		ReferenceMapping referenced = MappingFactory.eINSTANCE.createReferenceMapping();
		referenced.getFeaturePath().add(lines);
		referenced.setExclude(false);                 // a selector only: it generates no resources
		referenced.setCollectionFilter(collectionFilter);
		referenced.setCollectionIndex(collectionIndex);

		ValueMapping valueMapping = MappingFactory.eINSTANCE.createValueMapping();
		valueMapping.getFeaturePath().add(lineCount);

		ResourceMapping resource = MappingFactory.eINSTANCE.createResourceMapping();
		resource.setMid("value");
		resource.setName("value");
		resource.setEType(EcorePackage.Literals.EINT);
		resource.setValueMapping(valueMapping);

		ServiceMapping service = MappingFactory.eINSTANCE.createServiceMapping();
		service.setMid("line");
		service.setName(serviceName);
		service.setReferencedResource(referenced);
		service.getResources().add(resource);

		NameMapping providerName = MappingFactory.eINSTANCE.createNameMapping();
		providerName.setName("counter-1");

		ProviderMapping mapping = MappingFactory.eINSTANCE.createProviderMapping();
		mapping.setMid("readings");
		mapping.setName(providerName);
		mapping.getProviderClasses().add(counter);
		mapping.getServices().add(service);
		return mapping;
	}

	private EObject instance() {
		EObject instance = EcoreUtil.create(counter);
		@SuppressWarnings("unchecked")
		List<EObject> collection = (List<EObject>) instance.eGet(lines);
		collection.add(lineOf("a", 1));
		collection.add(lineOf("b", 2));
		return instance;
	}

	private EObject lineOf(String name, int count) {
		EObject element = EcoreUtil.create(line);
		element.eSet(lineName, name);
		element.eSet(lineCount, count);
		return element;
	}

	private SensinactService service(SensinactResource resource) {
		SensinactService service = mock(SensinactService.class);
		doReturn(Map.of("value", resource)).when(service).getResources();
		return service;
	}

	private static EAttribute attribute(String name, EDataType type) {
		EAttribute attribute = EcoreFactory.eINSTANCE.createEAttribute();
		attribute.setName(name);
		attribute.setEType(type);
		return attribute;
	}
}
