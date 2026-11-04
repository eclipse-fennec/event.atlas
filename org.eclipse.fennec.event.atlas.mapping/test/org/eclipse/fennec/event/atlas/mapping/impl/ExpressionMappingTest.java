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
import java.util.Optional;

import org.eclipse.emf.ecore.EAttribute;
import org.eclipse.emf.ecore.EClass;
import org.eclipse.emf.ecore.EDataType;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.EPackage;
import org.eclipse.emf.ecore.EcoreFactory;
import org.eclipse.emf.ecore.EcorePackage;
import org.eclipse.emf.ecore.util.EcoreUtil;
import org.eclipse.fennec.event.atlas.mapping.ExpressionEvaluator;
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
 * Where an {@code expression} sits among the producers. The expression language itself is not
 * exercised here - that is the OCL bundle's own test; what is pinned here is the precedence and
 * what happens where no evaluator is deployed.
 */
public class ExpressionMappingTest {

	private final EPackage ePackage = EcoreFactory.eINSTANCE.createEPackage();
	private final EClass reading = EcoreFactory.eINSTANCE.createEClass();
	private final EAttribute tempA = attribute("tempA", EcorePackage.Literals.EINT);
	private final EAttribute device = attribute("device", EcorePackage.Literals.ESTRING);

	private final ValueFunctionRegistryImpl functions = new ValueFunctionRegistryImpl();
	private final ValueConverterRegistryImpl converters = new ValueConverterRegistryImpl();

	/** Answers the expressions it knows, and produces nothing for any other - as a real one does. */
	private final ExpressionEvaluator evaluator = (expression, self) -> Optional
			.ofNullable(Map.<String, Object> of("computed", 99, "name", "expr-name").get(expression));

	{
		ePackage.setName("expressions");
		ePackage.setNsPrefix("expressions");
		ePackage.setNsURI("http://example.org/expressions");
		reading.setName("Reading");
		reading.getEStructuralFeatures().add(tempA);
		reading.getEStructuralFeatures().add(device);
		ePackage.getEClassifiers().add(reading);

		functions.addFunction(input -> 7, Map.of(ValueFunction.FUNCTION_ID, "seven"));
		converters.addConverter(value -> "converted:" + value, Map.of(ValueConverter.CONVERTER_ID, "mark"));
	}

	@Test
	@DisplayName("An expression produces the value")
	void expression_produces() {
		Map<String, Object> values = map(valueMapping("computed", null, null, null, null), evaluator);

		assertEquals(99, values.get("data.value"));
	}

	@Test
	@DisplayName("An expression wins over a functionId")
	void expression_winsOverFunction() {
		Map<String, Object> values = map(valueMapping("computed", "seven", null, null, null), evaluator);

		assertEquals(99, values.get("data.value"), "the function would have produced 7");
	}

	@Test
	@DisplayName("An expression wins over a feature path")
	void expression_winsOverFeaturePath() {
		Map<String, Object> values = map(valueMapping("computed", null, tempA, null, null), evaluator);

		assertEquals(99, values.get("data.value"), "the path would have produced 20");
	}

	@Test
	@DisplayName("A converter runs after the expression, so the two compose")
	void expression_thenConverter() {
		Map<String, Object> values = map(valueMapping("computed", null, null, "mark", null), evaluator);

		assertEquals("converted:99", values.get("data.value"));
	}

	@Test
	@DisplayName("An expression that produces nothing leaves the constant to answer")
	void expressionWithoutResult_leavesTheConstant() {
		Map<String, Object> values = map(valueMapping("self.nothing", null, null, null, "33"), evaluator);

		assertEquals(33, values.get("data.value"));
	}

	@Test
	@DisplayName("An expression that produces nothing does not fall back to the feature path")
	void expressionWithoutResult_doesNotUseTheFeaturePath() {
		Map<String, Object> values = map(valueMapping("self.nothing", null, tempA, null, null), evaluator);

		assertFalse(values.containsKey("data.value"), () -> "expected no value, got " + values);
	}

	@Test
	@DisplayName("With no evaluator deployed an expression produces nothing, and the constant answers")
	void noEvaluator_leavesTheConstant() {
		Map<String, Object> values = map(valueMapping("computed", null, tempA, null, "33"), null);

		assertEquals(33, values.get("data.value"), "the feature path is not a fallback for an expression");
	}

	@Test
	@DisplayName("An expression on the name mapping decides the provider id")
	void expressionOnTheName_decidesTheProviderId() throws ValueMappingException {
		SensinactResource resource = mock(SensinactResource.class);
		SensinactService data = mock(SensinactService.class);
		doReturn(Map.of("value", resource)).when(data).getResources();
		SensinactProvider provider = mock(SensinactProvider.class);
		doReturn(Map.of("data", data)).when(provider).getServices();
		SensinactDigitalTwin twin = mock(SensinactDigitalTwin.class);
		when(twin.getProvider(anyString(), anyString())).thenReturn(provider);

		NameMapping name = MappingFactory.eINSTANCE.createNameMapping();
		name.setExpression("name");
		ProviderMapping mapping = mapping(valueMapping(null, null, tempA, null, null), name);

		new ValueMapperImpl(twin, mapping, null, converters, functions, evaluator).mapInstance(instance());

		ArgumentCaptor<String> id = ArgumentCaptor.forClass(String.class);
		verify(twin).getProvider(anyString(), id.capture());
		assertEquals("expr-name", id.getValue());
	}

	private ValueMapping valueMapping(String expression, String functionId, EAttribute path, String converterId,
			String constant) {
		ValueMapping valueMapping = MappingFactory.eINSTANCE.createValueMapping();
		valueMapping.setExpression(expression);
		valueMapping.setFunctionId(functionId);
		valueMapping.setConverterId(converterId);
		valueMapping.setValue(constant);
		if (path != null) {
			valueMapping.getFeaturePath().add(path);
		}
		return valueMapping;
	}

	private Map<String, Object> map(ValueMapping valueMapping, ExpressionEvaluator expressionEvaluator) {
		NameMapping name = MappingFactory.eINSTANCE.createNameMapping();
		name.setName("reading");
		return new ValueMapperImpl(mock(SensinactDigitalTwin.class), mapping(valueMapping, name), null,
				converters, functions, expressionEvaluator).mapResourceValues(instance());
	}

	private ProviderMapping mapping(ValueMapping valueMapping, NameMapping name) {
		ResourceMapping resource = MappingFactory.eINSTANCE.createResourceMapping();
		resource.setMid("value");
		resource.setName("value");
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
