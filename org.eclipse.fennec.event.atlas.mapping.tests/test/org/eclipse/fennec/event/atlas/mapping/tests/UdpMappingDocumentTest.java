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

import static org.assertj.core.api.Assertions.assertThat;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import org.eclipse.emf.common.util.URI;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.EPackage;
import org.eclipse.emf.ecore.InternalEObject;
import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.emf.ecore.resource.ResourceSet;
import org.eclipse.emf.ecore.resource.impl.ResourceSetImpl;
import org.eclipse.emf.ecore.xmi.impl.XMIResourceFactoryImpl;
import org.eclipse.fennec.event.atlas.model.mapping.MappingPackage;
import org.eclipse.fennec.event.atlas.model.mapping.MappingProfile;
import org.eclipse.fennec.event.atlas.model.mapping.ProviderMapping;
import org.eclipse.fennec.event.atlas.model.mapping.ProviderStrategy;
import org.eclipse.fennec.event.atlas.model.mapping.ServiceMapping;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

/**
 * The UDP mappings and profiles are documents, and this reads them the way a runtime does.
 * <p>
 * They map Jena payload models that are not deployed here, so nothing can be registered or
 * pushed - what can be checked is that each document still parses against the current
 * metamodel. That is what catches a feature renamed out from under them, and it is why these
 * files are in the repository rather than only in the UDP workspace.
 */
public class UdpMappingDocumentTest {

	private static final Path UDP = Path.of("data", "udp");

	@ParameterizedTest
	@ValueSource(strings = { "ilsa-configuration-mapping.xmi", "ilsa-signal-state-mapping.xmi",
			"traficam-config-mapping.xmi", "traficam-detections-mapping.xmi", "ilsa-profile.xmi",
			"traficam-profile.xmi" })
	@DisplayName("Every UDP document parses against the mapping metamodel")
	void documentParses(String fileName) throws IOException {
		Resource resource = load(fileName);

		assertThat(resource.getErrors()).as("load errors of %s", fileName).isEmpty();
		assertThat(resource.getContents()).as("contents of %s", fileName).hasSize(1);
	}

	@ParameterizedTest
	@ValueSource(strings = { "ilsa-profile.xmi", "traficam-profile.xmi" })
	@DisplayName("Both profiles unify the provider model, which is what they exist for")
	void profileIsUnified(String fileName) throws IOException {
		MappingProfile profile = (MappingProfile) load(fileName).getContents().get(0);

		assertThat(profile.getProviderStrategy()).isEqualTo(ProviderStrategy.UNIFIED);
		assertThat(profile.getProvider()).isNotNull();
		assertThat(profile.getProvider().getProviderId()).isNotBlank();
		assertThat(profile.getProvider().getAdmin()).as("an admin both mappings of a pair carry").isNotNull();
	}

	@Test
	@DisplayName("Each mapping names its pair's profile by profileId, the fragment the registry looks up")
	void everyMappingNamesItsProfile() throws IOException {
		assertProfileOf("ilsa-configuration-mapping.xmi", "ilsa-profile.xmi", "ilsa");
		assertProfileOf("ilsa-signal-state-mapping.xmi", "ilsa-profile.xmi", "ilsa");
		assertProfileOf("traficam-config-mapping.xmi", "traficam-profile.xmi", "traficam");
		assertProfileOf("traficam-detections-mapping.xmi", "traficam-profile.xmi", "traficam");
	}

	@Test
	@DisplayName("A typed service names an EClass of the Jena provider models")
	void typedServicesNameAJenaEClass() throws IOException {
		assertServiceEClass("ilsa-configuration-mapping.xmi", "signal",
				"https://model.data.jena.de/sensinact/ilsa/1.0#//Signal");
		assertServiceEClass("ilsa-signal-state-mapping.xmi", "signal",
				"https://model.data.jena.de/sensinact/ilsa/1.0#//Signal");
		assertServiceEClass("traficam-config-mapping.xmi", "observedArea",
				"https://model.data.jena.de/sensinact/traficam/1.0#//ObservedObjects");
		assertServiceEClass("traficam-detections-mapping.xmi", "class",
				"https://model.data.jena.de/sensinact/traficam/1.0#//ObservedObjects");
	}

	@Test
	@DisplayName("The TrafiCam admin extends the admin service, and declares the viewport it adds")
	void traficamExtendsItsAdmin() throws IOException {
		ProviderMapping mapping = mapping("traficam-config-mapping.xmi");

		assertThat(mapping.getAdmin().isExtendAdmin()).as("extendAdmin").isTrue();
		assertThat(mapping.getAdmin().getResources()).singleElement()
				.extracting(resource -> resource.getMid()).isEqualTo("viewport");
	}

	/**
	 * The profile reference must carry the profileId as its fragment, not "/".
	 * <p>
	 * Read unresolved on purpose: these documents sit beside each other, so EMF would resolve
	 * the reference and hide the very thing under test. A runtime fed from a Model Atlas has no
	 * document to resolve and looks the fragment up in the profile registry instead.
	 */
	private void assertProfileOf(String mappingFile, String profileFile, String profileId) throws IOException {
		ProviderMapping mapping = mapping(mappingFile);
		Object unresolved = mapping.eGet(MappingPackage.Literals.PROVIDER_MAPPING__PROFILE, false);
		assertThat(unresolved).as("the profile of %s", mappingFile).isInstanceOf(MappingProfile.class);

		MappingProfile profile = (MappingProfile) unresolved;
		assertThat(profile.eIsProxy()).as("%s must reference its profile, not contain it", mappingFile).isTrue();
		URI uri = ((InternalEObject) profile).eProxyURI();
		assertThat(uri).as("%s must reference its profile as a proxy", mappingFile).isNotNull();
		assertThat(uri.fragment()).as("the fragment of %s is what the registry looks up", mappingFile)
				.isEqualTo(profileId);
		assertThat(uri.trimFragment().lastSegment()).isEqualTo(profileFile);

		MappingProfile declared = (MappingProfile) load(profileFile).getContents().get(0);
		assertThat(declared.getProfileId()).as("and it must be the id the profile declares").isEqualTo(profileId);
	}

	private void assertServiceEClass(String mappingFile, String serviceMid, String expectedUri) throws IOException {
		List<ServiceMapping> services = mapping(mappingFile).getServices().stream()
				.filter(service -> serviceMid.equals(service.getMid())).toList();
		assertThat(services).as("service '%s' of %s", serviceMid, mappingFile).hasSize(1);

		EObject serviceEClass = services.get(0).getServiceEClass();
		assertThat(serviceEClass).as("serviceEClass of '%s'", serviceMid).isNotNull();
		assertThat(((InternalEObject) serviceEClass).eProxyURI()).hasToString(expectedUri);
	}

	private ProviderMapping mapping(String fileName) throws IOException {
		return (ProviderMapping) load(fileName).getContents().get(0);
	}

	private Resource load(String fileName) throws IOException {
		Path file = UDP.resolve(fileName);
		assertThat(Files.isRegularFile(file)).as("%s exists", file).isTrue();

		ResourceSet resourceSet = new ResourceSetImpl();
		resourceSet.getResourceFactoryRegistry().getExtensionToFactoryMap().put("xmi",
				new XMIResourceFactoryImpl());
		resourceSet.getPackageRegistry().put(MappingPackage.eNS_URI, MappingPackage.eINSTANCE);
		EPackage.Registry.INSTANCE.put(MappingPackage.eNS_URI, MappingPackage.eINSTANCE);

		Resource resource = resourceSet.createResource(URI.createFileURI(file.toAbsolutePath().toString()));
		resource.load(null);
		return resource;
	}
}
