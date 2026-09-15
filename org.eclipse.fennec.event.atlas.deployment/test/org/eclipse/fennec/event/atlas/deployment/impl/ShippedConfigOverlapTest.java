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
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.regex.Pattern;

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
import org.junit.jupiter.api.Test;

/**
 * Pins the claim that applying {@code deployment-docker.xmi} to the shipped image changes nothing.
 * <p>
 * That claim only holds while every PID the example plans is spelled exactly as the docker
 * configurator bundle spells it — a factory instance name that differs by one character is a
 * <em>different</em> PID, the ownership guard never sees it, and both configurations end up active.
 * For the MQTT channels that means the same payload ingested once per client. The claim was wrong
 * in exactly that way until 2026-09-15, and asserting the planner's own spelling (as
 * {@link ExampleDeploymentTest} does) could not catch it, because both sides were the planner.
 * <p>
 * So this test compares against the other side: the JSON the image actually ships.
 */
class ShippedConfigOverlapTest {

	/** Block comments are legal in configurator JSON; nothing here nests them or hides one in a string. */
	private static final Pattern BLOCK_COMMENT = Pattern.compile("/\\*.*?\\*/", Pattern.DOTALL);

	private static final Path DOCKER_CONFIGS = Path
			.of("../org.eclipse.fennec.event.atlas.mapping.docker.config/configs");

	/**
	 * The PIDs the example is allowed to create rather than claim, because no shipped JSON writes
	 * them. Keep this list short and justified — every entry is a way the example changes a stock
	 * image.
	 */
	private static final Set<String> DELIBERATELY_NEW = Set.of(
			// the two factory PIDs the history rework added; this is the whole point of the model
			"sensinact.history.filter~slow-drift", "sensinact.history.housekeeping~cleanup");

	@Test
	void everyPidTheDockerExamplePlansIsAlreadyOwnedByTheShippedConfig() throws IOException {
		Set<String> shipped = pidsShippedInTheDockerImage();
		assertThat(shipped).as("the docker configurator bundle was readable").isNotEmpty();

		DeploymentPlan plan = DeploymentPlanner.plan(dockerExample());

		List<String> wouldBeCreated = plan.records().stream().map(ConfigurationRecord::pid)
				.filter(pid -> !shipped.contains(pid)).filter(pid -> !DELIBERATELY_NEW.contains(pid)).toList();

		assertThat(wouldBeCreated)
				.as("PIDs the example would CREATE alongside the shipped ones instead of claiming them — "
						+ "each is a duplicate component, not a no-op. Shipped: %s", shipped)
				.isEmpty();
	}

	@Test
	void theMqttInstanceNamesMatchTheIdAndNameTheyCarry() throws IOException {
		String config = strippedJson(DOCKER_CONFIGS.resolve("config.json"));

		// The model derives an instance name from the broker's id and a channel's name, so the
		// shipped JSON has to agree or the two describe different configurations.
		assertThat(config).contains("\"sensinact.southbound.mqtt~eventatlas-broker\"")
				.contains("\"id\": \"eventatlas-broker\"")
				.contains("\"event.atlas.southbound.mqtt~eventatlas-mqtt-xmi\"")
				.contains("\"name\": \"eventatlas-mqtt-xmi\"")
				.contains("\"event.atlas.southbound.mqtt~eventatlas-mqtt-json\"")
				.contains("\"name\": \"eventatlas-mqtt-json\"");
	}

	private static Set<String> pidsShippedInTheDockerImage() throws IOException {
		Set<String> pids = new LinkedHashSet<>();
		// All four resources the bundle ships — inference.json included, or the example's
		// inference section looks like something the model would create.
		for (String resource : List.of("config.json", "sensinact.json", "timescale.json", "inference.json")) {
			Path file = DOCKER_CONFIGS.resolve(resource);
			if (!Files.isRegularFile(file)) {
				continue;
			}
			for (String key : topLevelKeys(strippedJson(file))) {
				if (!key.startsWith(":")) {
					pids.add(key);
				}
			}
		}
		return pids;
	}

	/**
	 * The top-level object keys, read without a JSON parser on the test path. Configurator
	 * resources are one flat object of PID to properties, so a key is a quoted string at nesting
	 * depth one followed by a colon.
	 */
	private static List<String> topLevelKeys(String json) {
		List<String> keys = new java.util.ArrayList<>();
		int depth = 0;
		boolean inString = false;
		StringBuilder current = new StringBuilder();
		for (int i = 0; i < json.length(); i++) {
			char c = json.charAt(i);
			if (inString) {
				if (c == '\\') {
					i++;
				} else if (c == '"') {
					inString = false;
					if (depth == 1) {
						int next = nextNonWhitespace(json, i + 1);
						if (next >= 0 && json.charAt(next) == ':') {
							keys.add(current.toString());
						}
					}
				} else {
					current.append(c);
				}
				continue;
			}
			switch (c) {
			case '"' -> {
				inString = true;
				current.setLength(0);
			}
			case '{', '[' -> depth++;
			case '}', ']' -> depth--;
			default -> {
			}
			}
		}
		return keys;
	}

	private static int nextNonWhitespace(String text, int from) {
		for (int i = from; i < text.length(); i++) {
			if (!Character.isWhitespace(text.charAt(i))) {
				return i;
			}
		}
		return -1;
	}

	private static String strippedJson(Path file) throws IOException {
		return BLOCK_COMMENT.matcher(Files.readString(file)).replaceAll("");
	}

	private static EventAtlasDeployment dockerExample() throws IOException {
		Path file = Path.of("model", "examples", "deployment-docker.xmi");
		ResourceSet resourceSet = new ResourceSetImpl();
		resourceSet.getResourceFactoryRegistry().getExtensionToFactoryMap().put("xmi", new XMIResourceFactoryImpl());
		resourceSet.getPackageRegistry().put(DeploymentPackage.eNS_URI, DeploymentPackage.eINSTANCE);
		EPackage.Registry.INSTANCE.put(DeploymentPackage.eNS_URI, DeploymentPackage.eINSTANCE);
		Resource resource = resourceSet.createResource(URI.createFileURI(file.toAbsolutePath().toString()));
		resource.load(null);
		return (EventAtlasDeployment) resource.getContents().get(0);
	}
}
