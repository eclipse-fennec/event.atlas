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
package org.eclipse.fennec.event.atlas.mapping.ocl;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;
import java.util.Optional;

import org.eclipse.emf.ecore.EAttribute;
import org.eclipse.emf.ecore.EClass;
import org.eclipse.emf.ecore.EDataType;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.EPackage;
import org.eclipse.emf.ecore.EcoreFactory;
import org.eclipse.emf.ecore.EcorePackage;
import org.eclipse.emf.ecore.util.EcoreUtil;
import org.eclipse.fennec.emf.osgi.metadata.MetadataService;
import org.eclipse.fennec.m2x.ocl.api.OclContext;
import org.eclipse.fennec.m2x.ocl.api.OclEngine;
import org.eclipse.fennec.m2x.ocl.engine.OclEngines;
import org.eclipse.fennec.m2x.ocl.parser.OclParserSupport;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/**
 * The evaluator against the real OCL engine: what an expression reads from a payload, and what
 * each way of failing produces - which is always "no value", never an exception.
 */
public class OclExpressionEvaluatorTest {

	private final EPackage ePackage = EcoreFactory.eINSTANCE.createEPackage();
	private final EClass reading = EcoreFactory.eINSTANCE.createEClass();
	private final EAttribute temperature = attribute("temperature", EcorePackage.Literals.EDOUBLE);
	private final EAttribute battery = attribute("battery", EcorePackage.Literals.EINT);
	private final EAttribute device = attribute("device", EcorePackage.Literals.ESTRING);
	private final EAttribute tags = attribute("tags", EcorePackage.Literals.ESTRING);

	// A bare mock reports no metadata for the package, so the cache keys by nsURI here. What
	// this test measures is the evaluation, not the key - ExpressionCacheKeyTest covers that.
	private final OclExpressionEvaluator evaluator = new OclExpressionEvaluator(
			OclEngines.create(new OclParserSupport()), mock(MetadataService.class));

	{
		ePackage.setName("readings");
		ePackage.setNsPrefix("readings");
		ePackage.setNsURI("http://example.org/readings");
		tags.setUpperBound(-1);
		reading.setName("Reading");
		List.of(temperature, battery, device, tags).forEach(reading.getEStructuralFeatures()::add);
		ePackage.getEClassifiers().add(reading);
	}

	@Test
	@DisplayName("An expression computes from the payload's own features")
	void arithmetic() {
		assertEquals(68.0, evaluator.evaluate("self.temperature * 1.8 + 32", instance()).orElseThrow());
	}

	@Test
	@DisplayName("An expression reduces a collection to one value")
	void collection() {
		assertEquals(2, evaluator.evaluate("self.tags->size()", instance()).orElseThrow());
	}

	@Test
	@DisplayName("An expression can be conditional")
	void conditional() {
		assertEquals("low", evaluator.evaluate("if self.battery < 10 then 'low' else 'ok' endif", instance())
				.orElseThrow());
	}

	@Test
	@DisplayName("String operations are available")
	void strings() {
		assertEquals("SENSOR-1", evaluator.evaluate("self.device.toUpperCase()", instance()).orElseThrow());
	}

	@Test
	@DisplayName("The expressions the user guide shows all evaluate")
	void guideExamples() {
		EObject reading = instance();

		assertEquals(2, evaluator.evaluate("self.tags->size()", reading).orElseThrow());
		assertEquals("indoor", evaluator.evaluate("self.tags->first()", reading).orElseThrow());
		assertEquals(1, evaluator.evaluate("self.tags->select(t | t = 'lab')->size()", reading).orElseThrow());
		assertEquals("SENSOR-1", evaluator.evaluate("self.device.toUpperCase()", reading).orElseThrow());
		assertEquals("low", evaluator.evaluate("if self.battery < 10 then 'low' else 'ok' endif", reading)
				.orElseThrow());
	}

	@Test
	@DisplayName("An unset feature yields no value rather than null")
	void unsetFeature_producesNothing() {
		EObject empty = EcoreUtil.create(reading);

		assertTrue(evaluator.evaluate("self.device", empty).isEmpty());
	}

	@Test
	@DisplayName("An expression that does not parse produces no value instead of throwing")
	void unparseable_producesNothing() {
		assertTrue(evaluator.evaluate("self.temperature *", instance()).isEmpty());
	}

	@Test
	@DisplayName("An expression naming a feature the payload does not have produces no value")
	void unknownFeature_producesNothing() {
		assertTrue(evaluator.evaluate("self.humidity", instance()).isEmpty());
	}

	@Test
	@DisplayName("A failed expression is remembered, so the same text does not reach the engine again")
	void unparseable_isRememberedAndStaysEmpty() throws Exception {
		// The engine is a spy, not a mock: the parse failure is the real one, and what is
		// counted is how often the evaluator asks. Two empty results prove nothing on their
		// own - an evaluation failure is empty too, and that branch deliberately does not cache.
		OclEngine spied = spy(OclEngines.create(new OclParserSupport()));
		OclExpressionEvaluator counting = new OclExpressionEvaluator(spied, mock(MetadataService.class));

		assertTrue(counting.evaluate("self.temperature *", instance()).isEmpty());
		assertTrue(counting.evaluate("self.temperature *", instance()).isEmpty());

		// One call only. This also pins that the real engine reports a malformed text as an
		// OclParseException, which is the premise ExpressionCacheKeyTest stubs.
		verify(spied, times(1)).evaluate(anyString(), any(OclContext.class));
	}

	@Test
	@DisplayName("No expression and no payload are not errors either")
	void nothingToEvaluate() {
		assertEquals(Optional.empty(), evaluator.evaluate(null, instance()));
		assertEquals(Optional.empty(), evaluator.evaluate("   ", instance()));
		assertEquals(Optional.empty(), evaluator.evaluate("self.device", null));
	}

	@SuppressWarnings("unchecked")
	private EObject instance() {
		EObject instance = EcoreUtil.create(reading);
		instance.eSet(temperature, 20.0);
		instance.eSet(battery, 5);
		instance.eSet(device, "sensor-1");
		((List<String>) instance.eGet(tags)).addAll(List.of("indoor", "lab"));
		return instance;
	}

	private static EAttribute attribute(String name, EDataType type) {
		EAttribute attribute = EcoreFactory.eINSTANCE.createEAttribute();
		attribute.setName(name);
		attribute.setEType(type);
		return attribute;
	}
}
