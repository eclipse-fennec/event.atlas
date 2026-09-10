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
import java.time.Duration;

import org.eclipse.emf.common.util.URI;
import org.eclipse.emf.ecore.EPackage;
import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.emf.ecore.resource.ResourceSet;
import org.eclipse.emf.ecore.resource.impl.ResourceSetImpl;
import org.eclipse.emf.ecore.xmi.impl.XMIResourceFactoryImpl;
import org.eclipse.fennec.event.atlas.deployment.ConfigurationRecord;
import org.eclipse.fennec.event.atlas.deployment.DeploymentPlan;
import org.eclipse.fennec.event.atlas.model.deployment.DeploymentPackage;
import org.eclipse.fennec.event.atlas.model.deployment.EventAtlasDeployment;
import org.eclipse.fennec.event.atlas.model.deployment.HistorizationFilter;
import org.junit.jupiter.api.Test;

/**
 * Loads the shipped example models. They are documentation, and documentation that no longer
 * parses is worse than none - so the build reads them the way a runtime would.
 */
class ExampleDeploymentTest {

	@Test
	void theDockerExampleLoadsAndPlansWithoutProblems() throws IOException {
		EventAtlasDeployment deployment = load("deployment-docker.xmi");

		assertThat(deployment.getDeploymentId()).isEqualTo("eventatlas-docker");
		DeploymentPlan plan = DeploymentPlanner.plan(deployment);

		assertThat(plan.problems()).isEmpty();
		assertThat(plan.records().stream().map(ConfigurationRecord::pid)).contains("org.apache.felix.http~eventHttp",
				"AtlasEObjectProvider~atlas", "sensinact.southbound.mqtt~eventatlas-broker",
				"event.atlas.southbound.mqtt~eventatlas-mqtt-json", "event.atlas.southbound.rest",
				"event.atlas.southbound.ingest", "sensinact.history.timescale",
				"sensinact.history.filter~slow-drift", "sensinact.history.housekeeping~cleanup");
	}

	@Test
	void durationsInTheExampleSurviveTheRoundTrip() throws IOException {
		EventAtlasDeployment deployment = load("deployment-docker.xmi");

		HistorizationFilter filter = deployment.getHistory().getFilters().get(0);
		assertThat(filter.getChangeMaxInterval()).isEqualTo(Duration.ofMinutes(15));
		assertThat(deployment.getHistory().getHousekeeping().get(0).getRetentionPeriod())
				.isEqualTo(Duration.ofDays(90));
	}

	@Test
	void theHistoryTuningExampleClaimsOnlyTheNewFactoryPids() throws IOException {
		DeploymentPlan plan = DeploymentPlanner.plan(load("deployment-history-tuning.xmi"));

		assertThat(plan.problems()).isEmpty();
		assertThat(plan.records().stream().map(ConfigurationRecord::pid))
				.containsExactlyInAnyOrder("sensinact.history.filter~temperature-deadband",
						"sensinact.history.filter~changes-only", "sensinact.history.housekeeping~ninety-days");
	}

	private static EventAtlasDeployment load(String fileName) throws IOException {
		Path file = Path.of("model", "examples", fileName);
		assertThat(Files.isRegularFile(file)).as("example %s exists", file).isTrue();

		ResourceSet resourceSet = new ResourceSetImpl();
		resourceSet.getResourceFactoryRegistry().getExtensionToFactoryMap().put("xmi",
				new XMIResourceFactoryImpl());
		resourceSet.getPackageRegistry().put(DeploymentPackage.eNS_URI, DeploymentPackage.eINSTANCE);
		EPackage.Registry.INSTANCE.put(DeploymentPackage.eNS_URI, DeploymentPackage.eINSTANCE);

		Resource resource = resourceSet.createResource(URI.createFileURI(file.toAbsolutePath().toString()));
		resource.load(null);
		assertThat(resource.getErrors()).isEmpty();
		return (EventAtlasDeployment) resource.getContents().get(0);
	}
}
