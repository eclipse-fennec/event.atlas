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
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicReference;

import org.eclipse.emf.ecore.EAttribute;
import org.eclipse.emf.ecore.EClass;
import org.eclipse.emf.ecore.EDataType;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.EPackage;
import org.eclipse.emf.ecore.EReference;
import org.eclipse.emf.ecore.EStructuralFeature;
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
	/** A collection to navigate: Reading.samples, each sample with a key and a value. */
	private final EClass sample = EcoreFactory.eINSTANCE.createEClass();
	private final EAttribute sampleKey = attribute("key", EcorePackage.Literals.ESTRING);
	private final EAttribute sampleValue = attribute("value", EcorePackage.Literals.EINT);
	private final EReference samples = EcoreFactory.eINSTANCE.createEReference();

	/** What the last function call was handed. The collection cases are about exactly this. */
	private final AtomicReference<Object> seen = new AtomicReference<>();

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
		sample.setName("Sample");
		sample.getEStructuralFeatures().add(sampleKey);
		sample.getEStructuralFeatures().add(sampleValue);
		samples.setName("samples");
		samples.setEType(sample);
		samples.setContainment(true);
		samples.setUpperBound(-1);
		reading.getEStructuralFeatures().add(samples);
		ePackage.getEClassifiers().add(reading);
		ePackage.getEClassifiers().add(sample);

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

		// Records what it was handed, so a test can assert on the input rather than infer it
		// from the output. It answers with the sample's value where it was given a sample.
		functions.addFunction(input -> {
			seen.set(input);
			if (input instanceof List<?> list) {
				return list.stream().mapToInt(element -> (Integer) ((EObject) element).eGet(sampleValue)).sum();
			}
			return input instanceof EObject eo && eo.eClass() == sample ? eo.eGet(sampleValue) : input;
		}, Map.of(ValueFunction.FUNCTION_ID, "record"));
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

	// ---------------------------------------------------------------------------------------
	// A feature path that meets a collection. A path that PASSES one must select an element to
	// carry on from - by filter, else by collectionIndex. A path that ENDS on one hands the
	// whole collection to the function, because that is what the path found; narrowing it with
	// a filter or a non-zero index selects a single element instead.
	// ---------------------------------------------------------------------------------------

	@Test
	@DisplayName("A path that ends on a collection hands the function the whole collection")
	void collectionPath_handsTheFunctionTheCollection() {
		Map<String, Object> values = map(collectionMapping(0, null, samples));

		List<?> handed = assertInstanceOf(List.class, seen.get(),
				() -> "a producer over a collection must see all of it, not " + seen.get());
		assertEquals(3, handed.size(), "every element, in payload order");
		assertEquals("a", ((EObject) handed.get(0)).eGet(sampleKey));
		assertEquals("c", ((EObject) handed.get(2)).eGet(sampleKey));
		assertEquals(36, values.get("data.value"), "11 + 12 + 13");
	}

	@Test
	@DisplayName("An explicit collectionIndex of 0 cannot narrow it - use a filter")
	void zeroCollectionIndex_doesNotNarrow() {
		map(collectionMapping(0, null, samples));

		assertInstanceOf(List.class, seen.get(),
				"EMF cannot tell an explicit 0 from an unset index, so 0 is not a selection");
	}

	@Test
	@DisplayName("A collectionIndex chooses which element the function is handed")
	void collectionIndex_choosesTheElement() {
		Map<String, Object> values = map(collectionMapping(2, null, samples));

		assertFalse(seen.get() instanceof List, () -> "a non-zero index selects one: " + seen.get());
		assertEquals("c", ((EObject) seen.get()).eGet(sampleKey));
		assertEquals(13, values.get("data.value"));
	}

	@Test
	@DisplayName("A collectionFilter chooses it too, and beats the index")
	void collectionFilter_choosesTheElement() {
		Map<String, Object> values = map(collectionMapping(2, "key=b", samples));

		assertFalse(seen.get() instanceof List, () -> "a filter selects one: " + seen.get());
		assertEquals("b", ((EObject) seen.get()).eGet(sampleKey), "the filter wins over the index");
		assertEquals(12, values.get("data.value"));
	}

	@Test
	@DisplayName("A path may continue past the collection, and the function gets what it ends on")
	void pathPastACollection_handsTheFunctionTheValueBeyondIt() {
		Map<String, Object> values = map(collectionMapping(1, null, samples, sampleValue));

		assertEquals(12, seen.get(), "the attribute of the selected element, not the element");
		assertEquals(12, values.get("data.value"));
	}

	@Test
	@DisplayName("An empty collection reaches the function, which decides what nothing means")
	void emptyCollection_reachesTheFunction() {
		EObject instance = instance();
		instance.eUnset(samples);

		Map<String, Object> values = map(collectionMapping(0, null, samples), instance);

		assertEquals(List.of(), seen.get(),
				"'the camera saw nothing' is a statement, and only the function can read it");
		assertEquals(0, values.get("data.value"), "the sum of no samples");
	}

	@Test
	@DisplayName("A path that finds nothing still produces nothing, and the constant answers")
	void unsetPath_leavesTheConstant() {
		EObject instance = instance();
		instance.eUnset(samples);

		ValueMapping valueMapping = collectionMapping(1, null, samples, sampleValue);
		valueMapping.setValue("99");
		Map<String, Object> values = map(valueMapping, instance);

		assertNull(seen.get(), "the path passes the collection, and an empty one has no element");
		assertEquals(99, values.get("data.value"), "so the mapping's own constant answers");
	}

	private ValueMapping collectionMapping(int collectionIndex, String collectionFilter,
			EStructuralFeature... path) {
		ValueMapping valueMapping = MappingFactory.eINSTANCE.createValueMapping();
		valueMapping.setFunctionId("record");
		valueMapping.setCollectionIndex(collectionIndex);
		valueMapping.setCollectionFilter(collectionFilter);
		for (EStructuralFeature feature : path) {
			valueMapping.getFeaturePath().add(feature);
		}
		return valueMapping;
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
		return map(valueMapping, instance());
	}

	private Map<String, Object> map(ValueMapping valueMapping, EObject instance) {
		NameMapping name = MappingFactory.eINSTANCE.createNameMapping();
		name.setName("reading");
		return new ValueMapperImpl(mock(SensinactDigitalTwin.class), mapping(valueMapping, name), null,
				converters, functions).mapResourceValues(instance);
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
		addSample(instance, "a", 11);
		addSample(instance, "b", 12);
		addSample(instance, "c", 13);
		return instance;
	}

	@SuppressWarnings("unchecked")
	private void addSample(EObject instance, String key, int value) {
		EObject element = EcoreUtil.create(sample);
		element.eSet(sampleKey, key);
		element.eSet(sampleValue, value);
		((List<EObject>) instance.eGet(samples)).add(element);
	}

	private static EAttribute attribute(String name, EDataType type) {
		EAttribute attribute = EcoreFactory.eINSTANCE.createEAttribute();
		attribute.setName(name);
		attribute.setEType(type);
		return attribute;
	}
}
