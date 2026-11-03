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
import static org.mockito.Mockito.mock;

import java.util.Map;

import org.eclipse.emf.ecore.EAttribute;
import org.eclipse.emf.ecore.EClass;
import org.eclipse.emf.ecore.EDataType;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.EPackage;
import org.eclipse.emf.ecore.EcoreFactory;
import org.eclipse.emf.ecore.EcorePackage;
import org.eclipse.emf.ecore.util.EcoreUtil;
import org.eclipse.fennec.event.atlas.mapping.converters.ValueConverter;
import org.eclipse.fennec.event.atlas.mapping.converters.impl.ValueConverterRegistryImpl;
import org.eclipse.fennec.event.atlas.model.mapping.MappingFactory;
import org.eclipse.fennec.event.atlas.model.mapping.ProviderMapping;
import org.eclipse.fennec.event.atlas.model.mapping.ResourceMapping;
import org.eclipse.fennec.event.atlas.model.mapping.ServiceMapping;
import org.eclipse.fennec.event.atlas.model.mapping.ValueMapping;
import org.eclipse.sensinact.core.twin.SensinactDigitalTwin;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/**
 * A ValueMapping may carry a constant in its {@code value}. The feature path answers first, and
 * the constant answers when the path yields nothing.
 */
public class ConstantResourceValueTest {

	private final EPackage ePackage = EcoreFactory.eINSTANCE.createEPackage();
	private final EClass uplink = EcoreFactory.eINSTANCE.createEClass();
	private final EAttribute reading = attribute("reading", EcorePackage.Literals.ESTRING);
	private final ValueConverterRegistryImpl registry = new ValueConverterRegistryImpl();

	{
		ePackage.setName("constants");
		ePackage.setNsPrefix("constants");
		ePackage.setNsURI("http://example.org/constants");
		uplink.setName("Uplink");
		uplink.getEStructuralFeatures().add(reading);
		ePackage.getEClassifiers().add(uplink);
		registry.addConverter(value -> "converted:" + value, Map.of(ValueConverter.CONVERTER_ID, "mark"));
	}

	@Test
	@DisplayName("A resource with no feature path reports its constant")
	void noPath_reportsTheConstant() {
		Map<String, Object> values = map(valueMapping("fixed", null, null), EcorePackage.Literals.ESTRING, null);

		assertEquals("fixed", values.get("data.value"));
	}

	@Test
	@DisplayName("The feature path wins over the constant")
	void featurePath_winsOverTheConstant() {
		Map<String, Object> values = map(valueMapping("fixed", reading, null), EcorePackage.Literals.ESTRING,
				"from the payload");

		assertEquals("from the payload", values.get("data.value"));
	}

	@Test
	@DisplayName("A payload that does not carry the feature falls back to the constant")
	void absentFeature_fallsBackToTheConstant() {
		Map<String, Object> values = map(valueMapping("fixed", reading, null), EcorePackage.Literals.ESTRING, null);

		assertEquals("fixed", values.get("data.value"),
				"the literals of NameMapping and TimestampMapping behave the same way");
	}

	@Test
	@DisplayName("The constant is read as the resource's declared type")
	void constant_isReadAsTheDeclaredType() {
		Map<String, Object> values = map(valueMapping("42", null, null), EcorePackage.Literals.EINT, null);

		assertEquals(Integer.valueOf(42), values.get("data.value"));
	}

	@Test
	@DisplayName("A constant that is no value of the declared type is dropped")
	void constantOfTheWrongType_isDropped() {
		Map<String, Object> values = map(valueMapping("not a number", null, null), EcorePackage.Literals.EINT, null);

		assertFalse(values.containsKey("data.value"), () -> "expected no value, got " + values);
	}

	@Test
	@DisplayName("A converter does not touch the constant, because it is written in its final form")
	void converter_doesNotTouchTheConstant() {
		Map<String, Object> values = map(valueMapping("fixed", null, "mark"), EcorePackage.Literals.ESTRING, null);

		assertEquals("fixed", values.get("data.value"));
	}

	@Test
	@DisplayName("A converter still applies to a value the feature path produced")
	void converter_stillAppliesToThePathValue() {
		Map<String, Object> values = map(valueMapping("fixed", reading, "mark"), EcorePackage.Literals.ESTRING,
				"from the payload");

		assertEquals("converted:from the payload", values.get("data.value"));
	}

	private ValueMapping valueMapping(String constant, EAttribute path, String converterId) {
		ValueMapping valueMapping = MappingFactory.eINSTANCE.createValueMapping();
		valueMapping.setValue(constant);
		valueMapping.setConverterId(converterId);
		if (path != null) {
			valueMapping.getFeaturePath().add(path);
		}
		return valueMapping;
	}

	private Map<String, Object> map(ValueMapping valueMapping, EDataType resourceType, String payloadValue) {
		ResourceMapping resource = MappingFactory.eINSTANCE.createResourceMapping();
		resource.setMid("value");
		resource.setName("value");
		resource.setEType(resourceType);
		resource.setValueMapping(valueMapping);

		ServiceMapping service = MappingFactory.eINSTANCE.createServiceMapping();
		service.setMid("data");
		service.getResources().add(resource);

		ProviderMapping mapping = MappingFactory.eINSTANCE.createProviderMapping();
		mapping.setMid("constants");
		mapping.setName(MappingFactory.eINSTANCE.createNameMapping());
		mapping.getName().setName("uplink");
		mapping.getProviderClasses().add(uplink);
		mapping.getServices().add(service);

		EObject instance = EcoreUtil.create(uplink);
		if (payloadValue != null) {
			instance.eSet(reading, payloadValue);
		}
		return new ValueMapperImpl(mock(SensinactDigitalTwin.class), mapping, null, registry)
				.mapResourceValues(instance);
	}

	private static EAttribute attribute(String name, EDataType type) {
		EAttribute attribute = EcoreFactory.eINSTANCE.createEAttribute();
		attribute.setName(name);
		attribute.setEType(type);
		return attribute;
	}
}
