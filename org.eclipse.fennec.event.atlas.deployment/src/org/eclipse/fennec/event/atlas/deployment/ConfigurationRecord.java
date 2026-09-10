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

import java.util.Map;
import java.util.Objects;

/**
 * One ConfigAdmin configuration a deployment model asks for.
 * <p>
 * {@code pid} is written the way the Felix Configurator writes it, so the two are directly
 * comparable when a deployment mixes both sources: a singleton configuration is just its PID,
 * a factory instance is {@code factoryPid~name}. The {@code ~} form is what
 * {@link org.osgi.service.cm.ConfigurationAdmin#getFactoryConfiguration(String, String, String)}
 * addresses, which is why the name is kept rather than left to ConfigAdmin to invent.
 *
 * @param pid        the configuration PID, {@code factoryPid~name} for a factory instance
 * @param properties the properties to write; never {@code null}
 */
public record ConfigurationRecord(String pid, Map<String, Object> properties) {

	public ConfigurationRecord {
		Objects.requireNonNull(pid, "pid");
		properties = properties == null ? Map.of() : Map.copyOf(properties);
	}

	/** @return {@code true} when this record addresses a factory instance. */
	public boolean isFactoryInstance() {
		return pid.indexOf('~') > 0;
	}

	/** @return the factory PID of a factory instance, or the PID itself otherwise. */
	public String factoryPid() {
		int separator = pid.indexOf('~');
		return separator > 0 ? pid.substring(0, separator) : pid;
	}

	/** @return the instance name of a factory instance, or {@code null} otherwise. */
	public String instanceName() {
		int separator = pid.indexOf('~');
		return separator > 0 ? pid.substring(separator + 1) : null;
	}
}
