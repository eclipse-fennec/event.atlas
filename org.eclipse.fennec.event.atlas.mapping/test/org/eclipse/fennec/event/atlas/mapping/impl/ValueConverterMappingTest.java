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

import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.EcorePackage;
import org.eclipse.fennec.event.atlas.mapping.ValueConverterRegistry;
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
 * The converter as the engine sees it: a resource whose valueMapping names one, read through
 * the public extract-only entry point.
 */
public class ValueConverterMappingTest {

	private final LineModel model = new LineModel();
	private final ValueConverterRegistryImpl registry = new ValueConverterRegistryImpl();

	/** The window the fixture's report carries, and what a converted one must not look like. */
	private static final String RAW_WINDOW = "21:36-22:56";

	/**
	 * Marks its output unmistakably. A transformation the data cannot show - upper-casing a
	 * string of digits, say - would let every assertion here pass without the converter ever
	 * being called.
	 */
	private static final ValueConverter MARKING = value -> "converted:" + value;

	@Test
	@DisplayName("A valueMapping's converter rewrites the value on its way into the twin")
	void converter_isAppliedToTheMappedValue() {
		registry.addConverter(MARKING, Map.of(ValueConverter.CONVERTER_ID, "mark"));

		Map<String, Object> values = mapResourceValues(withValueMapping("mark"), registry);

		assertEquals("converted:21:36-22:56", values.get("report.window"));
	}

	@Test
	@DisplayName("An unknown converterId leaves the value in place rather than emptying it")
	void unknownConverter_keepsTheRawValue() {
		registry.addConverter(MARKING, Map.of(ValueConverter.CONVERTER_ID, "mark"));

		Map<String, Object> values = mapResourceValues(withValueMapping("not-deployed"), registry);

		assertEquals(RAW_WINDOW, values.get("report.window"), "the registered converter is not the one named");
	}

	@Test
	@DisplayName("A converter that cannot convert drops the resource's value")
	void failingConverter_dropsTheResource() {
		registry.addConverter(value -> {
			throw new IllegalStateException("no");
		}, Map.of(ValueConverter.CONVERTER_ID, "boom"));

		Map<String, Object> values = mapResourceValues(withValueMapping("boom"), registry);

		assertFalse(values.containsKey("report.window"), "a dropped value is not reported: " + values);
	}

	@Test
	@DisplayName("Without a registry a named converter is skipped, not an error")
	void noRegistry_pushesUnconverted() {
		registry.addConverter(MARKING, Map.of(ValueConverter.CONVERTER_ID, "mark"));

		Map<String, Object> values = mapResourceValues(withValueMapping("mark"), null);

		assertEquals(RAW_WINDOW, values.get("report.window"), "no registry means the converter is never consulted");
	}

	@Test
	@DisplayName("A resource still reading plain valueFeature is untouched by any of this")
	void plainValueFeature_behavesAsBefore() {
		ProviderMapping mapping = mapping();
		ResourceMapping resource = resource();
		resource.getValueFeature().add(model.window);
		mapping.getServices().get(0).getResources().add(resource);

		Map<String, Object> values = mapResourceValues(mapping, registry);

		assertEquals(RAW_WINDOW, values.get("report.window"));
	}

	/** A mapping whose single resource reads 'window' through a valueMapping. */
	private ProviderMapping withValueMapping(String converterId) {
		ProviderMapping mapping = mapping();
		ResourceMapping resource = resource();
		ValueMapping valueMapping = MappingFactory.eINSTANCE.createValueMapping();
		valueMapping.getFeaturePath().add(model.window);
		valueMapping.setConverterId(converterId);
		resource.setValueMapping(valueMapping);
		mapping.getServices().get(0).getResources().add(resource);
		return mapping;
	}

	private Map<String, Object> mapResourceValues(ProviderMapping mapping, ValueConverterRegistry registry) {
		EObject report = model.report("21:36-22:56", model.entry("line_1", 1, 8));
		// Reading an instance never reaches the twin; a mock keeps the constructor happy.
		return new ValueMapperImpl(mock(SensinactDigitalTwin.class), mapping, null, registry)
				.mapResourceValues(report);
	}

	private ProviderMapping mapping() {
		ProviderMapping mapping = MappingFactory.eINSTANCE.createProviderMapping();
		mapping.setMid("lines");
		mapping.setName(MappingFactory.eINSTANCE.createNameMapping());
		mapping.getName().setName("report");
		mapping.getProviderClasses().add(model.report);
		ServiceMapping service = MappingFactory.eINSTANCE.createServiceMapping();
		service.setMid("report");
		mapping.getServices().add(service);
		return mapping;
	}

	private static ResourceMapping resource() {
		ResourceMapping resource = MappingFactory.eINSTANCE.createResourceMapping();
		resource.setMid("window");
		resource.setName("window");
		resource.setEType(EcorePackage.Literals.ESTRING);
		return resource;
	}
}
