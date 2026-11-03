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
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;

import org.eclipse.emf.ecore.EAttribute;
import org.eclipse.emf.ecore.EClass;
import org.eclipse.emf.ecore.EcoreFactory;
import org.eclipse.emf.ecore.EcorePackage;
import org.eclipse.fennec.event.atlas.model.mapping.MappingFactory;
import org.eclipse.fennec.event.atlas.model.mapping.ProviderMapping;
import org.eclipse.fennec.event.atlas.model.mapping.ReferenceMapping;
import org.eclipse.fennec.event.atlas.model.mapping.ReferenceResourceBinding;
import org.eclipse.fennec.event.atlas.model.mapping.ResourceMapping;
import org.eclipse.fennec.event.atlas.model.mapping.ServiceMapping;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/**
 * What a reference mapping's bindings put on the resources it generates: a bound converter
 * needs a valueMapping to live on, an unbound attribute keeps the original valueFeature shape.
 */
public class GeneratedConverterBindingTest {

	private static final String SERVICE = "weather";

	private final EAttribute temperature = attribute("temperature", EcorePackage.eINSTANCE.getEFloat());
	private final EAttribute humidity = attribute("humidity", EcorePackage.eINSTANCE.getEFloat());

	@Test
	@DisplayName("A bound converter reaches the generated resource through a valueMapping")
	void boundConverter_landsOnTheGeneratedResource() {
		ProviderMapping mapping = mapping(binding("f2c", temperature));

		generate(mapping);

		ResourceMapping generated = generated(mapping, "temperature");
		assertNotNull(generated.getValueMapping(), "a converter has nowhere to live without one");
		assertEquals("f2c", generated.getValueMapping().getConverterId());
		assertEquals(List.of(temperature), generated.getValueMapping().getFeaturePath());
		assertTrue(generated.getValueFeature().isEmpty(),
				"the path belongs to the valueMapping now, so it is not duplicated");
	}

	@Test
	@DisplayName("An attribute no binding converts keeps the plain valueFeature shape")
	void unboundAttribute_keepsValueFeature() {
		ProviderMapping mapping = mapping(binding("f2c", temperature));

		generate(mapping);

		ResourceMapping generated = generated(mapping, "humidity");
		assertNull(generated.getValueMapping(),
				"nothing to carry besides the path, so the generated shape is unchanged");
		assertEquals(List.of(humidity), generated.getValueFeature());
	}

	@Test
	@DisplayName("A converter would otherwise hit every attribute of the referenced type")
	void namedBinding_convertsOnlyWhatItNames() {
		ProviderMapping mapping = mapping(binding("f2c", temperature));

		generate(mapping);

		assertEquals("f2c", generated(mapping, "temperature").getValueMapping().getConverterId());
		assertNull(generated(mapping, "humidity").getValueMapping(),
				"humidity is not a temperature - this is why the binding exists");
	}

	@Test
	@DisplayName("A default binding converts every generated resource")
	void defaultBinding_appliesToAll() {
		ProviderMapping mapping = mapping(binding("round"));

		generate(mapping);

		assertEquals("round", generated(mapping, "temperature").getValueMapping().getConverterId());
		assertEquals("round", generated(mapping, "humidity").getValueMapping().getConverterId());
	}

	@Test
	@DisplayName("A binding naming the attribute wins over the default, as every setting does")
	void namedBinding_beatsTheDefault() {
		ProviderMapping mapping = mapping(binding("f2c", temperature), binding("round"));

		generate(mapping);

		assertEquals("f2c", generated(mapping, "temperature").getValueMapping().getConverterId());
		assertEquals("round", generated(mapping, "humidity").getValueMapping().getConverterId());
	}

	@Test
	@DisplayName("Without any binding nothing changes about the generated resources")
	void noBindings_generateTheOriginalShape() {
		ProviderMapping mapping = mapping();

		generate(mapping);

		assertNull(generated(mapping, "temperature").getValueMapping());
		assertEquals(List.of(temperature), generated(mapping, "temperature").getValueFeature());
	}

	private static void generate(ProviderMapping mapping) {
		new ProviderModelSensinactMapper.Factory(null).createMapper(null, null)
				.generateReferencedResources(mapping);
	}

	private static ResourceMapping generated(ProviderMapping mapping, String mid) {
		return mapping.getServices().get(0).getTemporaryResources().stream()
				.filter(resource -> mid.equals(resource.getMid()))
				.findFirst()
				.orElseThrow(() -> new AssertionError("no generated resource '" + mid + "'"));
	}

	/** A binding for the named attributes, or the level default when none are named. */
	private static ReferenceResourceBinding binding(String converterId, EAttribute... attributes) {
		ReferenceResourceBinding binding = MappingFactory.eINSTANCE.createReferenceResourceBinding();
		binding.setConverterId(converterId);
		binding.getAttributes().addAll(List.of(attributes));
		return binding;
	}

	private ProviderMapping mapping(ReferenceResourceBinding... bindings) {
		MappingFactory f = MappingFactory.eINSTANCE;

		EClass report = EcoreFactory.eINSTANCE.createEClass();
		report.setName("Report");
		report.getEStructuralFeatures().add(temperature);
		report.getEStructuralFeatures().add(humidity);

		ReferenceMapping reference = f.createReferenceMapping();
		reference.setTargetEClass(report);
		reference.setExclude(true);
		reference.getBindings().addAll(List.of(bindings));

		ServiceMapping service = f.createServiceMapping();
		service.setMid(SERVICE);
		service.setReferencedResource(reference);

		ProviderMapping mapping = f.createProviderMapping();
		mapping.setMid("converter-binding-mapping");
		mapping.getServices().add(service);
		return mapping;
	}

	private static EAttribute attribute(String name, org.eclipse.emf.ecore.EDataType type) {
		EAttribute attribute = EcoreFactory.eINSTANCE.createEAttribute();
		attribute.setName(name);
		attribute.setEType(type);
		return attribute;
	}
}
