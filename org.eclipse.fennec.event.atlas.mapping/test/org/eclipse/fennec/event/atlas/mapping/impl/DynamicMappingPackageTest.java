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

import static org.assertj.core.api.Assertions.assertThat;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import org.eclipse.emf.common.util.URI;
import org.eclipse.emf.ecore.EClass;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.EPackage;
import org.eclipse.emf.ecore.EcorePackage;
import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.emf.ecore.resource.ResourceSet;
import org.eclipse.emf.ecore.resource.impl.ResourceSetImpl;
import org.eclipse.emf.ecore.xmi.impl.EcoreResourceFactoryImpl;
import org.eclipse.emf.ecore.xmi.impl.XMIResourceFactoryImpl;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

/**
 * Loads a mapping against an EPackage built from the {@code .ecore} alone, with no generated code
 * in sight - the way a Model Atlas loads a registered schema.
 * <p>
 * Mappings travel through a Model Atlas, so the metamodel has to survive that. {@code EInstant} is
 * the attribute that would not: typed {@code java.time.Instant} it had no string conversion the
 * reflective {@code EFactoryImpl.createFromString} could use, which is the same defect issue #61
 * reports for the deployment model's {@code EDuration} - latent here only because no shipped
 * mapping writes a literal timestamp.
 */
class DynamicMappingPackageTest {

	private static final String MAPPING_NSURI = "https://fennec.eclipse.org/event.atlas/mapping/1.0";

	private static final String MAPPING_WITH_A_LITERAL_TIMESTAMP = """
			<?xml version="1.0" encoding="UTF-8"?>
			<mapping:ProviderMapping
			    xmi:version="2.0"
			    xmlns:xmi="http://www.omg.org/XMI"
			    xmlns:mapping="%s"
			    mid="literal-timestamp">
			  <timestamp timestamp="2026-09-16T10:15:30Z"/>
			</mapping:ProviderMapping>
			""".formatted(MAPPING_NSURI);

	@TempDir
	Path tempDir;

	@Test
	void aLiteralTimestampLoadsAgainstAPackageBuiltFromTheEcoreAlone() throws IOException {
		ResourceSet resourceSet = dynamicallyRegisteredMetamodel();
		Path mappingFile = Files.writeString(tempDir.resolve("literal-timestamp.xmi"),
				MAPPING_WITH_A_LITERAL_TIMESTAMP);

		EObject mapping = load(resourceSet, mappingFile).getContents().get(0);

		EObject timestampMapping = (EObject) feature(mapping, "timestamp");
		assertThat(feature(timestampMapping, "timestamp")).isEqualTo("2026-09-16T10:15:30Z");
	}

	private ResourceSet dynamicallyRegisteredMetamodel() throws IOException {
		ResourceSet resourceSet = new ResourceSetImpl();
		resourceSet.getResourceFactoryRegistry().getExtensionToFactoryMap().put("ecore",
				new EcoreResourceFactoryImpl());
		resourceSet.getResourceFactoryRegistry().getExtensionToFactoryMap().put("xmi",
				new XMIResourceFactoryImpl());
		resourceSet.getPackageRegistry().put(EcorePackage.eNS_URI, EcorePackage.eINSTANCE);

		// The dynamic package shadows the generated one for this resource set, so nothing in this
		// test can accidentally fall back to MappingPackage.eINSTANCE.
		EPackage dynamicPackage = (EPackage) load(resourceSet, Path.of("model", "event-atlas-mapping.ecore"))
				.getContents().get(0);
		assertThat(dynamicPackage.getNsURI()).isEqualTo(MAPPING_NSURI);
		resourceSet.getPackageRegistry().put(MAPPING_NSURI, dynamicPackage);
		return resourceSet;
	}

	private static Resource load(ResourceSet resourceSet, Path file) throws IOException {
		assertThat(Files.isRegularFile(file)).as("%s exists", file).isTrue();
		Resource resource = resourceSet.createResource(URI.createFileURI(file.toAbsolutePath().toString()));
		resource.load(null);
		assertThat(resource.getErrors()).isEmpty();
		return resource;
	}

	private static Object feature(EObject owner, String featureName) {
		EClass eClass = owner.eClass();
		assertThat(eClass.getEStructuralFeature(featureName)).as("%s has a feature %s", eClass.getName(), featureName)
				.isNotNull();
		return owner.eGet(eClass.getEStructuralFeature(featureName));
	}
}
