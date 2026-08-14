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
package org.eclipse.fennec.event.atlas.southbound.common.impl;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.lang.reflect.Field;
import java.nio.charset.StandardCharsets;

import org.eclipse.emf.ecore.EClass;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.EPackage;
import org.eclipse.emf.ecore.EcoreFactory;
import org.eclipse.emf.ecore.EcorePackage;
import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.emf.ecore.resource.ResourceSet;
import org.eclipse.emf.ecore.resource.impl.ResourceSetImpl;
import org.eclipse.emf.ecore.xmi.impl.XMIResourceFactoryImpl;
import org.eclipse.emf.ecore.xmi.impl.XMIResourceImpl;
import org.eclipse.fennec.emf.osgi.ResourceSetFactory;
import org.eclipse.fennec.event.atlas.mapping.InstancePusher;
import org.eclipse.fennec.event.atlas.southbound.common.IngestResult;
import org.eclipse.fennec.event.atlas.southbound.common.IngestResult.Outcome;
import org.eclipse.fennec.event.atlas.southbound.common.PayloadIngest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

/**
 * Unit tests for {@link PayloadIngestImpl}. The {@link ResourceSetFactory} is stubbed with
 * plain EMF resource sets so the deserialization paths - including the "model is unknown"
 * one, which relies on EMF's own {@code PackageNotFoundException} - are exercised for real;
 * only the {@link InstancePusher} (which would need a sensinact gateway) is mocked.
 * @author Ilenia Salvadori
 */
@ExtendWith(MockitoExtension.class)
public class PayloadIngestImplTest {

	private static final String KNOWN_NS_URI = "http://example.org/southbound/test/1.0";
	private static final String UNKNOWN_NS_URI = "http://example.org/southbound/nowhere/1.0";

	@Mock
	private InstancePusher instancePusher;

	private PayloadIngestImpl ingest;
	private EPackage testPackage;

	@BeforeEach
	void setUp() throws Exception {
		testPackage = createTestPackage();
		ingest = new PayloadIngestImpl();
		// The component fields are DS-injected at runtime; set them directly here.
		inject(ingest, "instancePusher", instancePusher);
		inject(ingest, "resourceSetFactory", (ResourceSetFactory) this::createResourceSet);
	}

	@Test
	@DisplayName("A payload whose model resolves and whose mapping applies reports APPLIED")
	void ingest_withKnownModelAndMapping_reportsApplied() {
		when(instancePusher.pushInstance(any(EObject.class))).thenReturn(1);

		IngestResult result = ingest.ingest(sensorXmi(), PayloadIngest.FORMAT_XMI, "sensors/test");

		assertEquals(Outcome.APPLIED, result.outcome());
		assertEquals(1, result.roots());
		assertEquals(1, result.mappingsApplied());
		assertTrue(result.isApplied());
		verify(instancePusher).pushInstance(any(EObject.class));
	}

	@Test
	@DisplayName("A payload that deserializes but has no registered mapping reports NO_MAPPING")
	void ingest_withoutMapping_reportsNoMappingAndNamesTheEClass() {
		when(instancePusher.pushInstance(any(EObject.class))).thenReturn(0);

		IngestResult result = ingest.ingest(sensorXmi(), PayloadIngest.FORMAT_XMI, "sensors/test");

		assertEquals(Outcome.NO_MAPPING, result.outcome());
		assertEquals(1, result.roots());
		assertEquals(0, result.mappingsApplied());
		// the detail must name the EClass, that is what makes the warning actionable
		assertEquals("Sensor", result.detail());
	}

	@Test
	@DisplayName("A payload naming an unresolvable model reports MODEL_UNKNOWN with the nsURI")
	void ingest_withUnresolvableModel_reportsModelUnknown() {
		IngestResult result = ingest.ingest(unknownModelXmi(), PayloadIngest.FORMAT_XMI, "sensors/test");

		assertEquals(Outcome.MODEL_UNKNOWN, result.outcome());
		assertEquals(UNKNOWN_NS_URI, result.detail(), "The unresolvable nsURI must be reported");
		verify(instancePusher, never()).pushInstance(any());
	}

	@Test
	@DisplayName("A malformed payload reports PARSE_ERROR and is never pushed")
	void ingest_withMalformedPayload_reportsParseError() {
		IngestResult result = ingest.ingest("this is not xmi at all".getBytes(StandardCharsets.UTF_8),
				PayloadIngest.FORMAT_XMI, "sensors/test");

		assertEquals(Outcome.PARSE_ERROR, result.outcome());
		assertNotNull(result.detail());
		verify(instancePusher, never()).pushInstance(any());
	}

	@Test
	@DisplayName("A gateway failure is contained and reported as PUSH_FAILED, not rethrown")
	void ingest_whenPushFails_reportsPushFailedWithoutThrowing() {
		when(instancePusher.pushInstance(any(EObject.class)))
				.thenThrow(new IllegalStateException("gateway thread unavailable"));

		IngestResult result = ingest.ingest(sensorXmi(), PayloadIngest.FORMAT_XMI, "sensors/test");

		assertEquals(Outcome.PUSH_FAILED, result.outcome());
		assertEquals("gateway thread unavailable", result.detail());
	}

	@Test
	@DisplayName("A null payload is a programming error and fails fast")
	void ingest_withNullPayload_throws() {
		assertThrows(NullPointerException.class, () -> ingest.ingest(null, PayloadIngest.FORMAT_XMI, "sensors/test"));
	}

	@Test
	@DisplayName("Media types and blank hints resolve to a usable format")
	void ingest_withMediaTypeHint_stillDeserializes() {
		when(instancePusher.pushInstance(any(EObject.class))).thenReturn(1);

		// "application/xml; charset=utf-8" and null must both land on XMI
		assertEquals(Outcome.APPLIED,
				ingest.ingest(sensorXmi(), "application/xml; charset=utf-8", "rest/ingest").outcome());
		assertEquals(Outcome.APPLIED, ingest.ingest(sensorXmi(), null, null).outcome());
	}

	/**
	 * Each ingest must get a fresh resource set - that is what lets the atlas client's
	 * configurator resolve unknown nsURIs remotely.
	 */
	@Test
	@DisplayName("Every ingest uses a fresh ResourceSet")
	void ingest_usesAFreshResourceSetPerPayload() {
		when(instancePusher.pushInstance(any(EObject.class))).thenReturn(1);
		java.util.List<ResourceSet> created = new java.util.ArrayList<>();
		inject(ingest, "resourceSetFactory", (ResourceSetFactory) () -> {
			ResourceSet rs = createResourceSet();
			created.add(rs);
			return rs;
		});

		ingest.ingest(sensorXmi(), PayloadIngest.FORMAT_XMI, "sensors/test");
		ingest.ingest(sensorXmi(), PayloadIngest.FORMAT_XMI, "sensors/test");

		assertEquals(2, created.size());
		assertTrue(created.get(0) != created.get(1), "Resource sets must not be reused across payloads");
	}

	private ResourceSet createResourceSet() {
		ResourceSet resourceSet = new ResourceSetImpl();
		resourceSet.getResourceFactoryRegistry().getExtensionToFactoryMap().put("xmi", new XMIResourceFactoryImpl());
		resourceSet.getPackageRegistry().put(KNOWN_NS_URI, testPackage);
		return resourceSet;
	}

	/**
	 * A minimal dynamic model: one {@code Sensor} EClass with a {@code value} attribute.
	 */
	private static EPackage createTestPackage() {
		EPackage ePackage = EcoreFactory.eINSTANCE.createEPackage();
		ePackage.setName("test");
		ePackage.setNsPrefix("test");
		ePackage.setNsURI(KNOWN_NS_URI);
		EClass sensor = EcoreFactory.eINSTANCE.createEClass();
		sensor.setName("Sensor");
		var value = EcoreFactory.eINSTANCE.createEAttribute();
		value.setName("value");
		value.setEType(EcorePackage.Literals.EDOUBLE);
		sensor.getEStructuralFeatures().add(value);
		ePackage.getEClassifiers().add(sensor);
		return ePackage;
	}

	private static byte[] sensorXmi() {
		return ("<?xml version=\"1.0\" encoding=\"UTF-8\"?>"
				+ "<test:Sensor xmlns:test=\"" + KNOWN_NS_URI + "\" value=\"21.5\"/>")
						.getBytes(StandardCharsets.UTF_8);
	}

	private static byte[] unknownModelXmi() {
		return ("<?xml version=\"1.0\" encoding=\"UTF-8\"?>"
				+ "<nowhere:Sensor xmlns:nowhere=\"" + UNKNOWN_NS_URI + "\" value=\"21.5\"/>")
						.getBytes(StandardCharsets.UTF_8);
	}

	private static void inject(Object target, String fieldName, Object value) {
		try {
			Field field = target.getClass().getDeclaredField(fieldName);
			field.setAccessible(true);
			field.set(target, value);
		} catch (ReflectiveOperationException e) {
			throw new IllegalStateException("Could not inject '" + fieldName + "'", e);
		}
	}

	@Test
	@DisplayName("A payload that reads cleanly but yields no objects reports EMPTY")
	void ingest_withEmptyResource_reportsEmpty() {
		// a resource factory whose resources load successfully but produce no contents
		inject(ingest, "resourceSetFactory", (ResourceSetFactory) () -> {
			ResourceSet resourceSet = new ResourceSetImpl();
			resourceSet.getResourceFactoryRegistry().getExtensionToFactoryMap().put("xmi",
					(Resource.Factory) uri -> new XMIResourceImpl(uri) {
						@Override
						public void doLoad(java.io.InputStream inputStream, java.util.Map<?, ?> options) {
							// accepts anything, contributes nothing
						}
					});
			return resourceSet;
		});

		IngestResult result = ingest.ingest(sensorXmi(), PayloadIngest.FORMAT_XMI, "sensors/test");

		assertEquals(Outcome.EMPTY, result.outcome());
		assertEquals(0, result.roots());
		verify(instancePusher, never()).pushInstance(any());
	}

}
