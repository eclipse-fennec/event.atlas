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

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Optional;

import org.eclipse.emf.ecore.EClass;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.EPackage;
import org.eclipse.emf.ecore.EcoreFactory;
import org.eclipse.emf.ecore.util.EcoreUtil;
import org.eclipse.fennec.emf.osgi.metadata.MetadataService;
import org.eclipse.fennec.emf.osgi.model.metadata.MetadataFactory;
import org.eclipse.fennec.emf.osgi.model.metadata.PackageMetadata;
import org.eclipse.fennec.m2x.ocl.api.OclContext;
import org.eclipse.fennec.m2x.ocl.api.OclEngine;
import org.eclipse.fennec.m2x.ocl.api.OclParseException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/**
 * What the evaluator does with an expression it already failed to parse.
 * <p>
 * The verdict belongs to a model version, not to an nsURI. The fingerprint names that version,
 * so a model that changes its content is parsed again and a restart is not needed. The engine
 * here is a mock, because what is measured is how often the evaluator asks it.
 */
public class ExpressionCacheKeyTest {

	private static final String EXPRESSION = "self.notAFeature";

	private final EPackage ePackage = EcoreFactory.eINSTANCE.createEPackage();
	private final EClass reading = EcoreFactory.eINSTANCE.createEClass();
	private final OclEngine engine = mock(OclEngine.class);
	private final MetadataService metadata = mock(MetadataService.class);
	private final OclExpressionEvaluator evaluator = new OclExpressionEvaluator(engine, metadata);

	{
		ePackage.setName("readings");
		ePackage.setNsPrefix("readings");
		ePackage.setNsURI("http://example.org/readings");
		reading.setName("Reading");
		ePackage.getEClassifiers().add(reading);
	}

	@Test
	@DisplayName("The same model version parses a bad expression one time only")
	void sameFingerprint_parsesOnce() throws Exception {
		fingerprint("fp1:aaaa");
		when(engine.evaluate(anyString(), any(OclContext.class))).thenThrow(new OclParseException("no such feature"));

		assertTrue(evaluator.evaluate(EXPRESSION, instance()).isEmpty());
		assertTrue(evaluator.evaluate(EXPRESSION, instance()).isEmpty());

		verify(engine, times(1)).evaluate(anyString(), any(OclContext.class));
	}

	@Test
	@DisplayName("A changed model version parses the expression again, with no restart")
	void changedFingerprint_parsesAgain() throws Exception {
		fingerprint("fp1:aaaa");
		when(engine.evaluate(anyString(), any(OclContext.class))).thenThrow(new OclParseException("no such feature"));
		assertTrue(evaluator.evaluate(EXPRESSION, instance()).isEmpty());

		// The model now carries the feature, so the package reports another fingerprint.
		fingerprint("fp1:bbbb");
		when(engine.evaluate(anyString(), any(OclContext.class))).thenReturn(42);

		assertTrue(evaluator.evaluate(EXPRESSION, instance()).isPresent(), "the new model version is parsed again");
		verify(engine, times(2)).evaluate(anyString(), any(OclContext.class));
	}

	@Test
	@DisplayName("With no fingerprint for the package the key falls back to the nsURI")
	void noFingerprint_stillCaches() throws Exception {
		// The service is mandatory, but it still reports nothing for a package it cannot describe.
		when(metadata.getPackageMetadata(ePackage)).thenReturn(Optional.empty());
		when(engine.evaluate(anyString(), any(OclContext.class))).thenThrow(new OclParseException("no such feature"));

		assertTrue(evaluator.evaluate(EXPRESSION, instance()).isEmpty());
		assertTrue(evaluator.evaluate(EXPRESSION, instance()).isEmpty());

		verify(engine, times(1)).evaluate(anyString(), any(OclContext.class));
	}

	private void fingerprint(String fingerprint) {
		PackageMetadata packageMetadata = MetadataFactory.eINSTANCE.createPackageMetadata();
		packageMetadata.setModelFingerprint(fingerprint);
		when(metadata.getPackageMetadata(ePackage)).thenReturn(Optional.of(packageMetadata));
	}

	private EObject instance() {
		return EcoreUtil.create(reading);
	}
}
