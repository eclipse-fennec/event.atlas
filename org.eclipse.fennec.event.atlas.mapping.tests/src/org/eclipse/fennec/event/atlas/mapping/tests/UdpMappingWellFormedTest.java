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
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.net.URL;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;

import org.eclipse.emf.common.util.URI;
import org.eclipse.emf.ecore.EClass;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.emf.ecore.resource.ResourceSet;
import org.eclipse.fennec.event.atlas.model.mapping.MappingSelector;
import org.eclipse.fennec.event.atlas.model.mapping.ProviderMapping;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;
import org.osgi.test.common.annotation.InjectBundleContext;
import org.osgi.test.junit5.context.BundleContextExtension;
import org.osgi.test.junit5.service.ServiceExtension;

import io.cloudevents.model.ce.CloudEventsPackage;

/**
 * The UDP mappings are reference material for the Jena adapters and nothing in this build
 * executes them - the domain models they map live in Jena's repositories. This reads them
 * anyway, so a typo in a feature name or an {@code xsi:type} is caught here rather than in
 * a deployment.
 * <p>
 * It checks the mapping metamodel only. The {@code href}s into the Jena models stay
 * unresolved proxies, which is not an error: EMF resolves them lazily, and a runtime that
 * has those models resolves them then.
 * @author Ilenia Salvadori
 */
@ExtendWith(BundleContextExtension.class)
@ExtendWith(ServiceExtension.class)
public class UdpMappingWellFormedTest {

	@InjectBundleContext
	BundleContext bundleContext;

	@org.osgi.test.common.annotation.InjectService
	ResourceSet resourceSet;

	@Test
	@DisplayName("Every UDP mapping reads as a ProviderMapping, with no unknown feature")
	void udpMappings_areWellFormed() {
		List<URL> mappings = udpFiles();
		assertFalse(mappings.isEmpty(), "the data/udp folder must be in the bundle");

		for (URL url : mappings) {
			Resource resource = load(url);
			assertTrue(resource.getErrors().isEmpty(),
					() -> url.getFile() + " has load errors: " + resource.getErrors());
			assertEquals(1, resource.getContents().size(), url.getFile());
		}
	}

	@Test
	@DisplayName("Each '-ce' twin is envelope-rooted and says which messages it is for")
	void envelopeRootedTwins_declareTheirRootAndASelector() {
		List<URL> twins = udpFiles().stream().filter(url -> url.getFile().contains("-ce-mapping")).toList();
		assertEquals(4, twins.size(), "one twin per flow");

		for (URL url : twins) {
			EObject root = load(url).getContents().get(0);
			assertTrue(root instanceof ProviderMapping, url.getFile());
			ProviderMapping mapping = (ProviderMapping) root;

			// the root it declared: ce::CloudEvent, which resolves here because the envelope
			// model is deployed - unlike the Jena models the originals name
			List<EClass> providerClasses = mapping.getProviderClasses();
			assertEquals(1, providerClasses.size(), url.getFile());
			assertEquals(CloudEventsPackage.Literals.CLOUD_EVENT, providerClasses.get(0), url.getFile());

			// an envelope-rooted mapping without a selector claims every message of every
			// model, and registration warns about it. These must not be that
			MappingSelector selector = mapping.getSelector();
			assertNotNull(selector, url.getFile() + " must carry a selector");
			assertFalse(selector.getSource().isEmpty(), url.getFile() + " must name its sources");
			assertNotNull(selector.getDataschema(),
					url.getFile() + " must name the payload type, which providerClasses no longer can");
		}
	}

	private List<URL> udpFiles() {
		Bundle bundle = bundleContext.getBundle();
		Enumeration<URL> entries = bundle.findEntries("/data/udp", "*.xmi", false);
		List<URL> urls = new ArrayList<>();
		while (entries != null && entries.hasMoreElements()) {
			urls.add(entries.nextElement());
		}
		return urls;
	}

	private Resource load(URL url) {
		try {
			Resource resource = resourceSet.createResource(URI.createURI(url.toString()));
			resource.load(null);
			return resource;
		} catch (Exception e) {
			throw new AssertionError("Could not read " + url, e);
		}
	}

}
