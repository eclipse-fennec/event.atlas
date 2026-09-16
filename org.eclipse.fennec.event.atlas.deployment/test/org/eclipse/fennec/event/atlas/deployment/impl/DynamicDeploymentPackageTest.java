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
package org.eclipse.fennec.event.atlas.deployment.impl;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

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

/**
 * Loads the example deployments against an EPackage built from the {@code .ecore} alone, with no
 * generated code in sight - the way a Model Atlas loads a registered schema.
 * <p>
 * This is the regression guard for issue #61: {@code EDuration} used to be typed
 * {@code java.time.Duration}, whose only string conversion lived in a GenModel {@code create} body
 * and therefore only ever reached the generated factory. Loaded dynamically, the reflective
 * {@code EFactoryImpl.createFromString} ran instead and every deployment carrying a duration was
 * rejected with {@code The value 'PT15M' is invalid} - so the model was unusable in the very store
 * it exists to be deployed from.
 */
class DynamicDeploymentPackageTest {

	private static final String DEPLOYMENT_NSURI = "https://fennec.eclipse.org/event.atlas/deployment/1.0";

	@Test
	void theDockerExampleLoadsAgainstAPackageBuiltFromTheEcoreAlone() throws IOException {
		EObject deployment = loadDynamically("deployment-docker.xmi");

		assertThat(attribute(deployment, "deploymentId")).isEqualTo("eventatlas-docker");
	}

	@Test
	void theHistoryTuningExampleLoadsAgainstAPackageBuiltFromTheEcoreAlone() throws IOException {
		EObject deployment = loadDynamically("deployment-history-tuning.xmi");

		assertThat(attribute(deployment, "deploymentId")).isEqualTo("eventatlas-history-tuning");
	}

	@Test
	void durationLiteralsSurviveTheDynamicLoad() throws IOException {
		EObject history = (EObject) attribute(loadDynamically("deployment-docker.xmi"), "history");

		EObject filter = first(history, "filters");
		EObject housekeeping = first(history, "housekeeping");

		assertThat(attribute(filter, "changeMaxInterval")).isEqualTo("PT15M");
		assertThat(attribute(housekeeping, "retentionPeriod")).isEqualTo("P90D");
		assertThat(attribute(housekeeping, "schedulePeriod")).isEqualTo("PT24H");
	}

	private static EObject loadDynamically(String exampleName) throws IOException {
		ResourceSet resourceSet = new ResourceSetImpl();
		resourceSet.getResourceFactoryRegistry().getExtensionToFactoryMap().put("ecore",
				new EcoreResourceFactoryImpl());
		resourceSet.getResourceFactoryRegistry().getExtensionToFactoryMap().put("xmi",
				new XMIResourceFactoryImpl());
		resourceSet.getPackageRegistry().put(EcorePackage.eNS_URI, EcorePackage.eINSTANCE);

		// The dynamic package shadows the generated one for this resource set, so nothing in this
		// test can accidentally fall back to DeploymentPackage.eINSTANCE.
		EPackage dynamicPackage = (EPackage) load(resourceSet, Path.of("model", "event-atlas-deployment.ecore"))
				.getContents().get(0);
		assertThat(dynamicPackage.getNsURI()).isEqualTo(DEPLOYMENT_NSURI);
		resourceSet.getPackageRegistry().put(DEPLOYMENT_NSURI, dynamicPackage);

		EObject deployment = load(resourceSet, Path.of("model", "examples", exampleName)).getContents().get(0);
		assertThat(deployment.eClass().getEPackage()).isSameAs(dynamicPackage);
		return deployment;
	}

	private static Resource load(ResourceSet resourceSet, Path file) throws IOException {
		assertThat(Files.isRegularFile(file)).as("%s exists", file).isTrue();
		Resource resource = resourceSet.createResource(URI.createFileURI(file.toAbsolutePath().toString()));
		resource.load(null);
		assertThat(resource.getErrors()).isEmpty();
		return resource;
	}

	private static Object attribute(EObject owner, String featureName) {
		EClass eClass = owner.eClass();
		assertThat(eClass.getEStructuralFeature(featureName)).as("%s has a feature %s", eClass.getName(), featureName)
				.isNotNull();
		return owner.eGet(eClass.getEStructuralFeature(featureName));
	}

	@SuppressWarnings("unchecked")
	private static EObject first(EObject owner, String featureName) {
		List<EObject> values = (List<EObject>) attribute(owner, featureName);
		assertThat(values).as("%s is not empty", featureName).isNotEmpty();
		return values.get(0);
	}
}
