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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;

import java.util.List;

import org.eclipse.emf.ecore.EAttribute;
import org.eclipse.emf.ecore.EClass;
import org.eclipse.emf.ecore.EDataType;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.EPackage;
import org.eclipse.emf.ecore.EcoreFactory;
import org.eclipse.emf.ecore.EcorePackage;
import org.eclipse.emf.ecore.util.EcoreUtil;
import org.eclipse.fennec.emf.osgi.metadata.MetadataService;
import org.eclipse.fennec.m2x.ocl.api.OclConfiguration;
import org.eclipse.fennec.m2x.ocl.api.OclEngine;
import org.eclipse.fennec.m2x.ocl.api.OclOperation;
import org.eclipse.fennec.m2x.ocl.api.OclOperationProvider;
import org.eclipse.fennec.m2x.ocl.engine.OclEngines;
import org.eclipse.fennec.m2x.ocl.parser.OclParserSupport;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/**
 * How a value the standard library cannot open is reached anyway.
 * <p>
 * A datatype whose instance class is a Java array - ILSA's {@code GeoJSON.bbox}, typed
 * {@code Array1D} = {@code java.lang.Double[]} - is one scalar to OCL, so no collection
 * operation takes an element out of it. An {@link OclOperation} contributed <em>on that
 * datatype</em> does, and then a mapping needs no converter for it.
 * <p>
 * In a deployed runtime one condition applies: {@code ocl.customOperationsEnabled} on PID
 * {@code DefaultOclEngine} defaults to {@code false}. One property is enough - the engine
 * propagates it into the default evaluation options, which is what an {@code evaluate} without
 * options uses. Every published {@code OclOperationProvider} binds, since m2x
 * {@code 0.1.1} ({@code 9e8bfee}, "bind every operation provider, not one of them"), so
 * contributions from several bundles compose.
 */
public class ContributedOperationTest {

	private final EPackage ePackage = EcoreFactory.eINSTANCE.createEPackage();
	private final EClass config = EcoreFactory.eINSTANCE.createEClass();
	private final EDataType array1D = EcoreFactory.eINSTANCE.createEDataType();
	private final EAttribute bbox = EcoreFactory.eINSTANCE.createEAttribute();

	{
		array1D.setName("Array1D");
		array1D.setInstanceClassName("java.lang.Double[]");
		bbox.setName("bbox");
		bbox.setEType(array1D);
		config.setName("Config");
		config.getEStructuralFeatures().add(bbox);
		ePackage.setName("configs");
		ePackage.setNsPrefix("configs");
		ePackage.setNsURI("http://example.org/event.atlas/configs/1.0");
		ePackage.getEClassifiers().add(array1D);
		ePackage.getEClassifiers().add(config);
	}

	@Test
	@DisplayName("An operation contributed on the datatype indexes a Java array")
	void contributedOperation_indexesTheArray() {
		OclExpressionEvaluator evaluator = new OclExpressionEvaluator(engineWithAt(),
				mock(MetadataService.class));

		assertEquals(11.6d, evaluator.evaluate("self.bbox.at(1)", instance()).orElse(null));
		assertEquals(50.9d, evaluator.evaluate("self.bbox.at(2)", instance()).orElse(null));
	}

	@Test
	@DisplayName("Without the contribution the same expression produces nothing")
	void withoutTheContribution_nothingIsProduced() {
		OclExpressionEvaluator evaluator = new OclExpressionEvaluator(
				OclEngines.create(new OclParserSupport()), mock(MetadataService.class));

		assertTrue(evaluator.evaluate("self.bbox.at(1)", instance()).isEmpty());
	}

	/** What a payload-model bundle would publish as its OclOperationProvider. */
	private OclEngine engineWithAt() {
		OclOperationProvider provider = () -> List.of(OclOperation.of("at", array1D,
				EcorePackage.Literals.EDOUBLE_OBJECT,
				(owner, arguments) -> ((Double[]) owner)[((Number) arguments[0]).intValue() - 1]));
		return OclEngines.create(OclConfiguration.builder(new OclParserSupport())
				.customOperationsEnabled(true)
				.addOperationProvider(provider)
				.build());
	}

	private EObject instance() {
		EObject instance = EcoreUtil.create(config);
		instance.eSet(bbox, new Double[] { 11.6d, 50.9d });
		return instance;
	}
}
