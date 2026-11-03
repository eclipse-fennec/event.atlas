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
import static org.junit.jupiter.api.Assertions.assertTrue;
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
 * A scaling converter produces a Double, which then has to survive the engine's coercion to the
 * resource's declared type. These say what a mapping author has to declare for that to work.
 */
public class ScaledResourceValueTest {

	private final EPackage ePackage = EcoreFactory.eINSTANCE.createEPackage();
	private final EClass uplink = EcoreFactory.eINSTANCE.createEClass();
	private final EAttribute raw = attribute("raw", EcorePackage.Literals.EINT);
	private final ValueConverterRegistryImpl registry = new ValueConverterRegistryImpl();

	{
		ePackage.setName("uplinks");
		ePackage.setNsPrefix("uplinks");
		ePackage.setNsURI("http://example.org/scaled");
		uplink.setName("Uplink");
		uplink.getEStructuralFeatures().add(raw);
		ePackage.getEClassifiers().add(uplink);
		registry.addConverter(tenths(), Map.of(ValueConverter.CONVERTER_ID, "tenths"));
	}

	@Test
	@DisplayName("A floating-point resource takes the scaled value")
	void floatingResource_takesTheScaledValue() {
		Map<String, Object> values = map(EcorePackage.Literals.EDOUBLE);

		assertEquals(25.5d, (Double) values.get("data.scaled"), 1e-9);
	}

	@Test
	@DisplayName("An integer-typed resource drops a fractional scaled value entirely")
	void integerResource_dropsAFractionalValue() {
		Map<String, Object> values = map(EcorePackage.Literals.EINT);

		// Not a rounding: convertValue hands the Double to EcoreUtil.convertToString for EInt,
		// which wants an Integer, and the resulting failure is caught and logged as a warning.
		// The resource reports nothing at all. Declare a floating-point eType for anything a
		// converter scales - this test is here to fail if that ever changes on purpose.
		assertTrue(values.isEmpty(),
				"a scaled value does not fit an EInt resource and is dropped, not rounded: " + values);
	}

	private Map<String, Object> map(EDataType resourceType) {
		ResourceMapping resource = MappingFactory.eINSTANCE.createResourceMapping();
		resource.setMid("scaled");
		resource.setName("scaled");
		resource.setEType(resourceType);
		ValueMapping valueMapping = MappingFactory.eINSTANCE.createValueMapping();
		valueMapping.getFeaturePath().add(raw);
		valueMapping.setConverterId("tenths");
		resource.setValueMapping(valueMapping);

		ServiceMapping service = MappingFactory.eINSTANCE.createServiceMapping();
		service.setMid("data");
		service.getResources().add(resource);

		ProviderMapping mapping = MappingFactory.eINSTANCE.createProviderMapping();
		mapping.setMid("uplinks");
		mapping.setName(MappingFactory.eINSTANCE.createNameMapping());
		mapping.getName().setName("uplink");
		mapping.getProviderClasses().add(uplink);
		mapping.getServices().add(service);

		EObject instance = EcoreUtil.create(uplink);
		instance.eSet(raw, 255);
		return new ValueMapperImpl(mock(SensinactDigitalTwin.class), mapping, null, registry)
				.mapResourceValues(instance);
	}

	/**
	 * value * 0.1 - a tenths-of-a-unit integer payload, as LinearValueConverter scales it.
	 * <p>
	 * This test examines the mapper, and not the component. A converter that gives a Double is
	 * all it needs, so it does not use LinearValueConverter. LinearValueConverterTest examines
	 * the component.
	 */
	private static ValueConverter tenths() {
		return value -> ((Number) value).doubleValue() * 0.1d;
	}

	private static EAttribute attribute(String name, EDataType type) {
		EAttribute attribute = EcoreFactory.eINSTANCE.createEAttribute();
		attribute.setName(name);
		attribute.setEType(type);
		return attribute;
	}
}
