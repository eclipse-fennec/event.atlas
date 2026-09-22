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
package org.eclipse.fennec.event.atlas.mapping.docker.config;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Optional;
import java.util.regex.MatchResult;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Stream;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

/**
 * A runtime that configures the <em>batch</em> chat completion must also deploy
 * {@code org.eclipse.fennec.codec.jsonschema} and allow-list the host it polls. Both are needed
 * only by the batch path, neither shows up at resolve time, and both fail <em>after</em> the batch
 * has been accepted - so a missing one costs a billed run (issue #68). A config bundle that
 * configures no {@code ClaudeBatchMessageService} needs neither and is skipped.
 * @author Ilenia Salvadori
 */
class BatchInferenceWiringTest {

	/** Its presence in a config bundle is what switches the batch path on. */
	private static final String BATCH_SERVICE = "ClaudeBatchMessageService";

	private static final String SCHEMA_RENDERER = "org.eclipse.fennec.codec.jsonschema";

	private static final String URI_HANDLER_PID = "org.eclipse.fennec.emf.osgi.urihandler.http";

	/** A config bundle and the bndrun that deploys it. */
	private record Runtime(String configBundle, String bndrun) {

		@Override
		public String toString() {
			return configBundle;
		}
	}

	static Stream<Runtime> deployedRuntimes() {
		return Stream.of(
				new Runtime("org.eclipse.fennec.event.atlas.mapping.docker.config",
						"org.eclipse.fennec.event.atlas.mapping.runtime/eventatlas.runtime_docker.bndrun"),
				new Runtime("org.eclipse.fennec.event.atlas.mapping.inference.config",
						"org.eclipse.fennec.event.atlas.mapping.runtime/inference.bndrun"),
				new Runtime("org.eclipse.fennec.event.atlas.mapping.local.config",
						"org.eclipse.fennec.event.atlas.mapping.runtime/launch.bndrun"));
	}

	@ParameterizedTest(name = "{0}")
	@MethodSource("deployedRuntimes")
	@DisplayName("A runtime that configures the batch API deploys the JSON-schema renderer")
	void batchRuntime_deploysTheSchemaRenderer(Runtime runtime) throws IOException {
		Path root = workspaceRoot();
		String configuration = allConfigsOf(root, runtime);
		if (!configuration.contains(BATCH_SERVICE)) {
			return;
		}
		assertTrue(deployedBundles(read(root.resolve(runtime.bndrun()))).contains(SCHEMA_RENDERER), String.format(
				"%s configures %s, but %s does not deploy %s - add it to -runrequires by identity (nothing "
						+ "imports it as a package) and re-resolve.",
				runtime.configBundle(), BATCH_SERVICE, runtime.bndrun(), SCHEMA_RENDERER));
	}

	@ParameterizedTest(name = "{0}")
	@MethodSource("deployedRuntimes")
	@DisplayName("A runtime that configures the batch API allow-lists the host it polls")
	void batchRuntime_allowsOutboundResolutionOfTheProviderHost(Runtime runtime) throws IOException {
		Path root = workspaceRoot();
		String configuration = allConfigsOf(root, runtime);
		if (!configuration.contains(BATCH_SERVICE)) {
			return;
		}
		String host = batchHostOf(configuration);
		assertTrue(allowedHostsOf(configuration).contains(host), String.format(
				"%s configures %s against host '%s', but allow-lists %s on %s - every status poll and the "
						+ "result fetch would be blocked.",
				runtime.configBundle(), BATCH_SERVICE, host, allowedHostsOf(configuration), URI_HANDLER_PID));
	}

	/** Every configurator resource of a bundle, concatenated - which file carries what is its own business. */
	private static String allConfigsOf(Path root, Runtime runtime) throws IOException {
		try (Stream<Path> resources = Files.list(root.resolve(runtime.configBundle()).resolve("configs"))) {
			return resources.filter(resource -> resource.toString().endsWith(".json")).sorted().map(resource -> {
				try {
					return read(resource);
				} catch (IOException e) {
					throw new UncheckedIOException(e);
				}
			}).reduce("", (all, resource) -> all + "\n" + resource);
		}
	}

	/** The bsns a bndrun's resolved {@code -runbundles} deploys - not what {@code -runrequires} asks for. */
	private static List<String> deployedBundles(String bndrun) {
		String runbundles = bndrun.substring(bndrun.indexOf("-runbundles:"));
		return Pattern.compile("(?m)^\\s*([a-zA-Z0-9._-]+);version").matcher(runbundles).results()
				.map(match -> match.group(1)).toList();
	}

	/**
	 * The host the batch service polls, read out of its own {@code base.url}. The value is a nest
	 * of {@code $[env:...;default=$[prop:...;default=URL]]}, so the URL is the innermost literal -
	 * which is the one an image started with no variables set actually uses.
	 */
	private static String batchHostOf(String configuration) {
		String batchBlock = configuration.substring(configuration.indexOf(BATCH_SERVICE));
		Matcher baseUrl = Pattern.compile("\"base\\.url\"\\s*:\\s*\"([^\"]+)\"").matcher(batchBlock);
		assertTrue(baseUrl.find(), "The " + BATCH_SERVICE + " block declares no base.url");
		Optional<String> literal = Pattern.compile("https?://[^\\]\"\\s]+").matcher(baseUrl.group(1)).results()
				.map(MatchResult::group).reduce((first, last) -> last);
		assertTrue(literal.isPresent(), "No literal URL in the base.url of " + BATCH_SERVICE + ": " + baseUrl.group(1));
		return URI.create(literal.get()).getHost();
	}

	private static List<String> allowedHostsOf(String configuration) {
		int pid = configuration.indexOf(URI_HANDLER_PID);
		if (pid < 0) {
			return List.of();
		}
		Matcher hosts = Pattern.compile("\"allowedHosts\"\\s*:\\s*\\[([^\\]]*)\\]").matcher(configuration);
		if (!hosts.find(pid)) {
			return List.of();
		}
		return Pattern.compile("\"([^\"]+)\"").matcher(hosts.group(1)).results().map(match -> match.group(1))
				.toList();
	}

	private static String read(Path file) throws IOException {
		return Files.readString(file, StandardCharsets.UTF_8);
	}

	/**
	 * Walks up from the working directory to the workspace root, so the test does not care
	 * whether it is run per project or from the root.
	 */
	private static Path workspaceRoot() {
		Path candidate = Path.of("").toAbsolutePath();
		while (candidate != null && !Files.isRegularFile(candidate.resolve("cnf/build.bnd"))) {
			candidate = candidate.getParent();
		}
		assertNotNull(candidate, String.format("No cnf/build.bnd above %s - cannot locate the workspace root",
				Path.of("").toAbsolutePath()));
		return candidate;
	}
}
