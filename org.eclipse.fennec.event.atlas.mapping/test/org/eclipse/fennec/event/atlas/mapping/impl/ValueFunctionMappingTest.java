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
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

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
import org.eclipse.fennec.event.atlas.mapping.converters.ValueConverter;
import org.eclipse.fennec.event.atlas.mapping.converters.impl.ValueConverterRegistryImpl;
import org.eclipse.fennec.event.atlas.mapping.functions.ValueFunction;
import org.eclipse.fennec.event.atlas.mapping.functions.impl.ValueFunctionRegistryImpl;
import org.eclipse.fennec.event.atlas.model.mapping.MappingFactory;
import org.eclipse.fennec.event.atlas.model.mapping.NameMapping;
import org.eclipse.fennec.event.atlas.model.mapping.ProviderMapping;
import org.eclipse.fennec.event.atlas.model.mapping.ResourceMapping;
import org.eclipse.fennec.event.atlas.model.mapping.ServiceMapping;
import org.eclipse.fennec.event.atlas.model.mapping.ValueMapping;
import org.eclipse.sensinact.core.twin.SensinactDigitalTwin;
import org.eclipse.sensinact.core.twin.SensinactProvider;
import org.eclipse.sensinact.core.twin.SensinactResource;
import org.eclipse.sensinact.core.twin.SensinactService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

/**
 * What a {@code functionId} produces. A function with no feature path is handed the source
 * object; a function with one is handed what that path found.
 */
public class ValueFunctionMappingTest {

	private final EPackage ePackage = EcoreFactory.eINSTANCE.createEPackage();
	private final EClass reading = EcoreFactory.eINSTANCE.createEClass();
	private final EAttribute tempA = attribute("tempA", EcorePackage.Literals.EINT);
	private final EAttribute tempB = attribute("tempB", EcorePackage.Literals.EINT);
	private final EAttribute device = attribute("device", EcorePackage.Literals.ESTRING);

	private final ValueFunctionRegistryImpl functions = new ValueFunctionRegistryImpl();
	private final ValueConverterRegistryImpl converters = new ValueConverterRegistryImpl();

	{
		ePackage.setName("functions");
		ePackage.setNsPrefix("functions");
		ePackage.setNsURI("http://example.org/functions");
		reading.setName("Reading");
		reading.getEStructuralFeatures().add(tempA);
		reading.getEStructuralFeatures().add(tempB);
		reading.getEStructuralFeatures().add(device);
		ePackage.getEClassifiers().add(reading);

		// A producer: it is handed the whole source object and reads two features itself.
		functions.addFunction(input -> {
			EObject source = (EObject) input;
			return (((Integer) source.eGet(tempA)) + ((Integer) source.eGet(tempB))) / 2;
		}, Map.of(ValueFunction.FUNCTION_ID, "mean"));

		// A transformer: it is handed what the feature path found.
		functions.addFunction(input -> ((Integer) input) * 2, Map.of(ValueFunction.FUNCTION_ID, "double"));

		// A producer that makes a name out of the source object.
		functions.addFunction(input -> "cam-" + ((EObject) input).eGet(device),
				Map.of(ValueFunction.FUNCTION_ID, "deviceName"));

		converters.addConverter(value -> "converted:" + value, Map.of(ValueConverter.CONVERTER_ID, "mark"));
	}

	@Test
	@DisplayName("A function with no feature path is handed the source object")
	void functionWithoutPath_readsTheSourceObject() {
		Map<String, Object> values = map(valueMapping("mean", null, null, null));

		assertEquals(25, values.get("data.value"), "the mean of 20 and 30");
	}

	@Test
	@DisplayName("A function with a feature path is handed what the path found")
	void functionWithPath_transformsThePathValue() {
		Map<String, Object> values = map(valueMapping("double", tempA, null, null));

		assertEquals(40, values.get("data.value"), "tempA is 20");
	}

	@Test
	@DisplayName("A converter runs after the function, so the two compose")
	void function_thenConverter() {
		Map<String, Object> values = map(valueMapping("mean", null, "mark", null));

		assertEquals("converted:25", values.get("data.value"));
	}

	@Test
	@DisplayName("An unknown function id produces nothing, and the constant answers")
	void unknownFunction_leavesTheConstant() {
		Map<String, Object> values = map(valueMapping("not-deployed", null, null, "33"));

		assertEquals(33, values.get("data.value"));
	}

	@Test
	@DisplayName("An unknown function id with no constant reports no value at all")
	void unknownFunction_withoutConstant_reportsNothing() {
		Map<String, Object> values = map(valueMapping("not-deployed", null, null, null));

		assertFalse(values.containsKey("data.value"), () -> "expected no value, got " + values);
	}

	@Test
	@DisplayName("A function on the name mapping decides the provider id")
	void functionOnTheName_decidesTheProviderId() throws ValueMappingException {
		SensinactResource resource = mock(SensinactResource.class);
		SensinactService data = mock(SensinactService.class);
		doReturn(Map.of("value", resource)).when(data).getResources();
		SensinactProvider provider = mock(SensinactProvider.class);
		doReturn(Map.of("data", data)).when(provider).getServices();
		SensinactDigitalTwin twin = mock(SensinactDigitalTwin.class);
		when(twin.getProvider(anyString(), anyString())).thenReturn(provider);

		NameMapping name = MappingFactory.eINSTANCE.createNameMapping();
		name.setFunctionId("deviceName");
		ProviderMapping mapping = mapping(valueMapping(null, tempA, null, null), name);

		new ValueMapperImpl(twin, mapping, null, converters, functions).mapInstance(instance());

		ArgumentCaptor<String> id = ArgumentCaptor.forClass(String.class);
		verify(twin).getProvider(anyString(), id.capture());
		assertEquals("cam-cam-1", id.getValue());
	}

	private ValueMapping valueMapping(String functionId, EAttribute path, String converterId, String constant) {
		ValueMapping valueMapping = MappingFactory.eINSTANCE.createValueMapping();
		valueMapping.setFunctionId(functionId);
		valueMapping.setConverterId(converterId);
		valueMapping.setValue(constant);
		if (path != null) {
			valueMapping.getFeaturePath().add(path);
		}
		return valueMapping;
	}

	private Map<String, Object> map(ValueMapping valueMapping) {
		NameMapping name = MappingFactory.eINSTANCE.createNameMapping();
		name.setName("reading");
		return new ValueMapperImpl(mock(SensinactDigitalTwin.class), mapping(valueMapping, name), null,
				converters, functions).mapResourceValues(instance());
	}

	private ProviderMapping mapping(ValueMapping valueMapping, NameMapping name) {
		ResourceMapping resource = MappingFactory.eINSTANCE.createResourceMapping();
		resource.setMid("value");
		resource.setName("value");
		// EString, so a converted value is still readable - see ScaledResourceValueTest for why
		// a scaled value needs a floating-point type.
		resource.setEType(valueMapping.getConverterId() == null
				? EcorePackage.Literals.EINT
				: EcorePackage.Literals.ESTRING);
		resource.setValueMapping(valueMapping);

		ServiceMapping service = MappingFactory.eINSTANCE.createServiceMapping();
		service.setMid("data");
		service.getResources().add(resource);

		ProviderMapping mapping = MappingFactory.eINSTANCE.createProviderMapping();
		mapping.setMid("readings");
		mapping.setName(name);
		mapping.getProviderClasses().add(reading);
		mapping.getServices().add(service);
		return mapping;
	}

	private EObject instance() {
		EObject instance = EcoreUtil.create(reading);
		instance.eSet(tempA, 20);
		instance.eSet(tempB, 30);
		instance.eSet(device, "cam-1");
		return instance;
	}

	private static EAttribute attribute(String name, EDataType type) {
		EAttribute attribute = EcoreFactory.eINSTANCE.createEAttribute();
		attribute.setName(name);
		attribute.setEType(type);
		return attribute;
	}
}
