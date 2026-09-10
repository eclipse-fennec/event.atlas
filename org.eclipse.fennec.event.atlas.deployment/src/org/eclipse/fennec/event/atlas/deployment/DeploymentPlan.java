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
package org.eclipse.fennec.event.atlas.deployment;

import java.util.List;
import java.util.Objects;

/**
 * The outcome of translating one {@code EventAtlasDeployment} into configurations.
 * <p>
 * Problems are collected rather than thrown: a deployment model that is wrong in one section
 * still configures the others, which is the behaviour a half-edited XMI needs. Every problem is
 * logged by the configurator, and the record it refers to is not written.
 *
 * @param records  the configurations to write, in application order
 * @param problems human-readable descriptions of what was refused
 */
public record DeploymentPlan(List<ConfigurationRecord> records, List<String> problems) {

	public DeploymentPlan {
		records = records == null ? List.of() : List.copyOf(records);
		problems = problems == null ? List.of() : List.copyOf(problems);
	}

	public boolean hasProblems() {
		return !problems.isEmpty();
	}

	/** @return the record for {@code pid}, or {@code null} when the plan carries none. */
	public ConfigurationRecord record(String pid) {
		Objects.requireNonNull(pid, "pid");
		return records.stream().filter(record -> pid.equals(record.pid())).findFirst().orElse(null);
	}
}
