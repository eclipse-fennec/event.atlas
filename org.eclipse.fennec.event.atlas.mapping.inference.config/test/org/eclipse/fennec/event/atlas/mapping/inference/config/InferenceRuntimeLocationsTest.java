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
package org.eclipse.fennec.event.atlas.mapping.inference.config;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/**
 * The file locations this runtime reads its mappings and profiles from must not depend on the
 * directory the JVM happens to be started in.
 * <p>
 * The failure this guards against is silent. {@code FileEObjectProvider} logs one WARNING for a
 * location that does not exist and then syncs an empty state, so the registry comes up with
 * <em>zero</em> entries and every payload afterwards is reported {@code NO_MAPPING} - correctly,
 * because there is genuinely no mapping. Nothing further in the chain can tell that apart from a
 * runtime that was simply never given one. Observed on 2026-08-28: a known EM310-UDL payload
 * deserialized into {@code 1 object(s) of type EM310UDLUplink} and matched nothing, because
 * {@code inference.jar} had been started from the workspace root while the {@code runtime/}
 * skeleton sits in the runtime project.
 * <p>
 * Both halves of the contract are checked, because either alone still breaks: the effective path
 * must be absolute, <em>and</em> the directory it names must be one this workspace actually ships.
 * @author Ilenia Salvadori
 */
class InferenceRuntimeLocationsTest {

	private static final String CONFIG = "org.eclipse.fennec.event.atlas.mapping.inference.config/configs/config.json";
	private static final String BNDRUN = "org.eclipse.fennec.event.atlas.mapping.runtime/inference.bndrun";

	/**
	 * The bnd macro for "the directory of this file", which is the only one that survives an
	 * export: {@code ${basedir}} and {@code ${project}} expand to the staging copy bnd makes.
	 */
	private static final String PROJECT_DIR = "${.}";

	/** {@code "locations": ["$[env:VAR;default=$[prop:VAR;default=THE_DEFAULT]]"]}. */
	private static final Pattern LOCATION = Pattern.compile(
			"\"locations\"\\s*:\\s*\\[\\s*\"(?:\\$\\[env:(?<var>[A-Z0-9_]+);default=)?" + "(?:\\$\\[prop:[A-Z0-9_]+;default=)?(?<path>[^\\]\"]+?)\\]*\"\\s*\\]");

	@Test
	@DisplayName("Every file-provider location the inference runtime uses is absolute and exists")
	void fileProviderLocations_doNotDependOnTheWorkingDirectory() throws IOException {
		Path root = workspaceRoot();
		Map<String, String> overrides = propertiesSetBy(read(root.resolve(BNDRUN)));

		Map<String, String> declared = declaredLocations(read(root.resolve(CONFIG)));
		assertThat(declared)
				.as("The inference config must declare a mappings and a profiles location - if this fails the "
						+ "regex below has drifted from the file, not the config from the contract")
				.hasSize(2);

		declared.forEach((provider, location) -> {
			String variable = variableOf(location);
			// What the runtime ends up with: the bndrun's value if it sets one, the config's own
			// default otherwise. A location declared as a bare literal has no variable to override.
			String effective = variable == null ? defaultOf(location)
					: overrides.getOrDefault(variable, defaultOf(location));

			assertThat(effective)
					.as("Provider '%s' resolves to '%s'. A relative path is resolved against the JVM's working "
							+ "directory, so the exported inference.jar loads nothing unless it happens to be "
							+ "started from the runtime project. Set %s in the -runproperties of %s.", provider,
							effective, variable, BNDRUN)
					.startsWith(PROJECT_DIR + "/");

			Path onDisk = root.resolve(BNDRUN).getParent().resolve(effective.substring(PROJECT_DIR.length() + 1));
			assertThat(onDisk)
					.as("Provider '%s' points at a directory this workspace does not ship", provider)
					.isDirectory();
		});
	}

	/** Provider instance name ({@code mappings}, {@code profiles}) to its raw {@code locations} entry. */
	private static Map<String, String> declaredLocations(String config) {
		Map<String, String> declared = new LinkedHashMap<>();
		Matcher provider = Pattern.compile("\"FileEObjectProvider~([a-zA-Z0-9_]+)\"").matcher(config);
		while (provider.find()) {
			Matcher location = LOCATION.matcher(config);
			if (location.find(provider.end())) {
				declared.put(provider.group(1), location.group(0));
			}
		}
		return declared;
	}

	private static String variableOf(String rawLocation) {
		Matcher matcher = LOCATION.matcher(rawLocation);
		return matcher.find() ? matcher.group("var") : null;
	}

	private static String defaultOf(String rawLocation) {
		Matcher matcher = LOCATION.matcher(rawLocation);
		return matcher.find() ? matcher.group("path") : rawLocation;
	}

	/**
	 * The {@code NAME=value} entries a bndrun sets, whether as {@code -runproperties} or as a
	 * {@code -runvm} {@code -Dname=value}. Only SHOUTING_CASE names are collected, which is what
	 * separates them from the framework's own dotted properties.
	 */
	private static Map<String, String> propertiesSetBy(String bndrun) {
		Map<String, String> properties = new LinkedHashMap<>();
		Matcher matcher = Pattern.compile("(?:-D)?\\b([A-Z][A-Z0-9_]{3,})=([^,\\s\\\\]+)").matcher(bndrun);
		while (matcher.find()) {
			properties.put(matcher.group(1), matcher.group(2));
		}
		return properties;
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
		assertThat(candidate).as("No cnf/build.bnd above %s - cannot locate the workspace root",
				Path.of("").toAbsolutePath()).isNotNull();
		return candidate;
	}
}
