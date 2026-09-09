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
package org.eclipse.fennec.event.atlas.mapping.tests;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Hashtable;
import java.util.List;
import java.util.Map;

import org.eclipse.emf.common.util.URI;
import org.eclipse.emf.ecore.EAttribute;
import org.eclipse.emf.ecore.EClass;
import org.eclipse.emf.ecore.EPackage;
import org.eclipse.emf.ecore.EcoreFactory;
import org.eclipse.emf.ecore.EcorePackage;
import org.eclipse.emf.ecore.InternalEObject;
import org.eclipse.fennec.emf.osgi.eobject.registry.EObjectRegistries;
import org.eclipse.fennec.emf.osgi.eobject.registry.EObjectRegistryListener;
import org.eclipse.fennec.emf.osgi.eobject.registry.EObjectRegistryWriter;
import org.eclipse.fennec.event.atlas.mapping.ProviderMappingRegistry;
import org.eclipse.fennec.event.atlas.model.mapping.MappingFactory;
import org.eclipse.fennec.event.atlas.model.mapping.ProviderMapping;
import org.eclipse.fennec.event.atlas.model.mapping.NameMapping;
import org.eclipse.fennec.event.atlas.model.mapping.ResourceMapping;
import org.eclipse.fennec.event.atlas.model.mapping.ServiceMapping;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceRegistration;
import org.osgi.test.common.annotation.InjectBundleContext;
import org.osgi.test.common.annotation.InjectService;
import org.osgi.test.junit5.context.BundleContextExtension;
import org.osgi.test.junit5.service.ServiceExtension;

/**
 * A mapping may arrive before the model it maps. That is not a corner case but the normal
 * startup order of any runtime whose domain EPackages come from a Model Atlas: the file
 * provider loads and validates its mappings synchronously at activation, while the atlas
 * client publishes its EPackages after an HTTP round trip. Measured on 2026-08-31, the
 * mapping was rejected 76 log lines before its package appeared.
 * <p>
 * Rejecting such a mapping permanently makes it unrecoverable, because a file provider never
 * re-loads: the runtime then reports {@code NO_MAPPING} for a payload it has a perfectly good
 * mapping for, with nothing in the log after startup to say why. So an entry whose provider
 * classes cannot be resolved <em>yet</em> is parked and retried when EPackages arrive, while
 * an entry that is malformed - not a mapping, no {@code mid}, no provider classes at all - is
 * still dropped on the spot. This is the same bargain {@code resolveProfile} already makes,
 * whose javadoc promises that "a later entry update or atlas refresh tries again".
 * @author Ilenia Salvadori
 */
@ExtendWith(BundleContextExtension.class)
@ExtendWith(ServiceExtension.class)
public class LateModelMappingTest {

	private static final String NS_URI = "http://example.org/event.atlas/late/1.0";
	private static final String SOURCE = "late-model-test";
	private static final String MID = "late-model-mapping";

	@InjectBundleContext
	BundleContext bundleContext;
	@InjectService(filter = "(emf.eobject.registry.name=sensinact-mappings)")
	EObjectRegistryListener mappingListener;
	@InjectService
	ProviderMappingRegistry mappingRegistry;

	private EObjectRegistryWriter writer;
	private ServiceRegistration<?> ePackageRegistration;

	@AfterEach
	void tearDown() {
		if (writer != null) {
			writer.sync(SOURCE, List.of());
			writer.getRegistry().removeListener(mappingListener);
		}
		if (ePackageRegistration != null) {
			ePackageRegistration.unregister();
		}
	}

	@Test
	@DisplayName("A mapping whose model arrives later is registered once its EPackage shows up")
	void mappingForALateModel_isRegisteredWhenTheEPackageArrives() throws Exception {
		EPackage lateModel = lateModel();
		EClass lateSensor = (EClass) lateModel.getEClassifier("LateSensor");

		writer = EObjectRegistries.createRegistry("sensinact-mappings");
		writer.getRegistry().addListener(mappingListener);
		writer.put(SOURCE, MID, mappingFor(), Map.of());

		assertTrue(mappingRegistry.getProviderMapping(lateSensor).isEmpty(),
				"the model is not available yet, so the mapping cannot be registered");

		// What the atlas client does once its HTTP round trip returns.
		ePackageRegistration = bundleContext.registerService(EPackage.class, lateModel,
				new Hashtable<>(Map.of("emf.name", "late", "emf.nsUri", NS_URI)));

		ProviderMapping registered = awaitRegistration(lateSensor);
		assertEquals(MID, registered.getMid(), "the parked mapping must be registered once its model is there");

		// Registration alone is not the point: the mapping has to be able to read a payload.
		ResourceMapping value = registered.getServices().get(0).getResources().get(0);
		assertFalse(value.getValueFeature().get(0).eIsProxy(),
				"the feature path must be resolved too, or every push reads null");
		assertEquals(lateSensor.getEStructuralFeature("value"), value.getValueFeature().get(0),
				"and resolved to the feature of the model that arrived, not a fresh copy of it");
	}

	/**
	 * The registration happens off the bind thread - a DS bind callback must not block on the
	 * gateway - so the assertion has to wait for it rather than read straight through.
	 */
	private ProviderMapping awaitRegistration(EClass providerClass) throws InterruptedException {
		for (int attempt = 0; attempt < 100; attempt++) {
			List<ProviderMapping> registered = mappingRegistry.getProviderMapping(providerClass);
			if (!registered.isEmpty()) {
				return registered.get(0);
			}
			Thread.sleep(50);
		}
		throw new AssertionError("No mapping registered for " + providerClass.getName() + " within 5s");
	}

	/** A model no runtime has at startup, standing in for one an atlas publishes late. */
	private static EPackage lateModel() {
		EPackage ePackage = EcoreFactory.eINSTANCE.createEPackage();
		ePackage.setName("late");
		ePackage.setNsPrefix("late");
		ePackage.setNsURI(NS_URI);

		EClass sensor = EcoreFactory.eINSTANCE.createEClass();
		sensor.setName("LateSensor");
		EAttribute id = EcoreFactory.eINSTANCE.createEAttribute();
		id.setName("id");
		id.setEType(EcorePackage.Literals.ESTRING);
		EAttribute value = EcoreFactory.eINSTANCE.createEAttribute();
		value.setName("value");
		value.setEType(EcorePackage.Literals.EDOUBLE);
		sensor.getEStructuralFeatures().add(id);
		sensor.getEStructuralFeatures().add(value);
		ePackage.getEClassifiers().add(sensor);
		return ePackage;
	}

	/**
	 * The mapping as a file provider would hand it over: everything resolved except
	 * {@code providerClasses}, which is the proxy an nsURI href leaves behind while the
	 * package it names is unknown.
	 */
	private static ProviderMapping mappingFor() {
		ProviderMapping mapping = MappingFactory.eINSTANCE.createProviderMapping();
		mapping.setMid(MID);

		EClass providerClass = EcoreFactory.eINSTANCE.createEClass();
		((InternalEObject) providerClass).eSetProxyURI(URI.createURI(NS_URI + "#//LateSensor"));
		mapping.getProviderClasses().add(providerClass);

		NameMapping name = MappingFactory.eINSTANCE.createNameMapping();
		name.setName("Late Sensor");
		mapping.setName(name);

		ServiceMapping service = MappingFactory.eINSTANCE.createServiceMapping();
		service.setMid("measurement");
		ResourceMapping resource = MappingFactory.eINSTANCE.createResourceMapping();
		resource.setMid("value");
		resource.setName("Value");
		resource.setEType(EcorePackage.Literals.EDOUBLE);
		// A feature path is a proxy too, and nothing validates it - a mapping registered with
		// this left unresolved pushes every payload and reads nothing, reporting "the feature
		// 'null' is not a valid feature" per value.
		EAttribute valueFeature = EcoreFactory.eINSTANCE.createEAttribute();
		((InternalEObject) valueFeature).eSetProxyURI(URI.createURI(NS_URI + "#//LateSensor/value"));
		resource.getValueFeature().add(valueFeature);
		service.getResources().add(resource);
		mapping.getServices().add(service);
		return mapping;
	}
}
