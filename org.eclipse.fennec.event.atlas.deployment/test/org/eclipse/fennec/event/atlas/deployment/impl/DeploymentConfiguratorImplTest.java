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
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.IOException;
import java.util.Dictionary;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Map;

import org.eclipse.fennec.event.atlas.model.deployment.DeploymentFactory;
import org.eclipse.fennec.event.atlas.model.deployment.EventAtlasDeployment;
import org.eclipse.fennec.event.atlas.model.deployment.HistorizationFilter;
import org.eclipse.fennec.event.atlas.model.deployment.HistoryConfig;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.osgi.service.cm.Configuration;
import org.osgi.service.cm.ConfigurationAdmin;

/**
 * Covers the parts that can damage a running runtime: the ownership guard, the deletion of PIDs a
 * shrunken model no longer asks for, and the read-back that makes that deletion survive a restart.
 */
class DeploymentConfiguratorImplTest {

	private static final DeploymentFactory FACTORY = DeploymentFactory.eINSTANCE;
	private static final String FILTER_PID = "sensinact.history.filter~drift";

	private ConfigurationAdmin configurationAdmin;
	private DeploymentConfiguratorImpl configurator;
	private final Map<String, Configuration> configurations = new HashMap<>();

	@BeforeEach
	void setUp() throws Exception {
		configurationAdmin = mock(ConfigurationAdmin.class);
		configurator = new DeploymentConfiguratorImpl();
		configurator.configurationAdmin = configurationAdmin;
		when(configurationAdmin.listConfigurations(anyString())).thenReturn(null);
	}

	@Test
	void aFreshPidIsCreatedAndStampedWithItsOwner() throws Exception {
		Configuration created = emptyConfiguration(FILTER_PID);

		configurator.apply("eventatlas", deploymentWithFilter("drift"));

		Dictionary<String, Object> written = captureUpdate(created);
		assertThat(written.get(DeploymentConfiguratorImpl.OWNER_PROPERTY)).isEqualTo("eventatlas");
		assertThat(written.get("name")).isEqualTo("drift");
	}

	@Test
	void anExistingPidWithNoOwnerStampIsLeftAlone() throws Exception {
		Configuration foreign = configurationWith(FILTER_PID, properties(Map.of("name", "from-json")));

		configurator.apply("eventatlas", deploymentWithFilter("drift"));

		verify(foreign, never()).update(any());
	}

	@Test
	void aPidOwnedByAnotherDeploymentIsLeftAlone() throws Exception {
		Configuration otherDeployments = configurationWith(FILTER_PID,
				properties(Map.of(DeploymentConfiguratorImpl.OWNER_PROPERTY, "somebody-else")));

		configurator.apply("eventatlas", deploymentWithFilter("drift"));

		verify(otherDeployments, never()).update(any());
	}

	@Test
	void aPidTheModelNoLongerAsksForIsDeleted() throws Exception {
		Configuration first = emptyConfiguration(FILTER_PID);
		configurator.apply("eventatlas", deploymentWithFilter("drift"));

		Configuration second = emptyConfiguration("sensinact.history.filter~renamed");
		stampAsOwnedBy(first, "eventatlas");
		listConfigurationsReturns(first);

		configurator.apply("eventatlas", deploymentWithFilter("renamed"));

		verify(first).delete();
		verify(second).update(any());
	}

	@Test
	void configurationsWrittenBeforeARestartAreAdoptedSoTheyCanStillBeDeleted() throws Exception {
		// A fresh instance, as after a restart: nothing in memory, but ConfigAdmin has kept the
		// stamped configuration a previous run wrote.
		Configuration survivor = configurationWith(FILTER_PID,
				properties(Map.of(DeploymentConfiguratorImpl.OWNER_PROPERTY, "eventatlas")));
		listConfigurationsReturns(survivor);
		emptyConfiguration("sensinact.history.filter~renamed");

		configurator.apply("eventatlas", deploymentWithFilter("renamed"));

		verify(survivor).delete();
	}

	@Test
	void retractDeletesEverythingTheDeploymentWrote() throws Exception {
		Configuration owned = configurationWith(FILTER_PID,
				properties(Map.of(DeploymentConfiguratorImpl.OWNER_PROPERTY, "eventatlas")));
		listConfigurationsReturns(owned);

		configurator.retract("eventatlas");

		verify(owned).delete();
	}

	@Test
	void filterValuesAreEscapedSoAWildcardCannotWiden() {
		assertThat(DeploymentConfiguratorImpl.escapeFilterValue("a*b")).isEqualTo("a\\*b");
		assertThat(DeploymentConfiguratorImpl.escapeFilterValue("a)b")).isEqualTo("a\\)b");
		assertThat(DeploymentConfiguratorImpl.escapeFilterValue("plain")).isEqualTo("plain");
	}

	private EventAtlasDeployment deploymentWithFilter(String filterName) {
		EventAtlasDeployment deployment = FACTORY.createEventAtlasDeployment();
		deployment.setDeploymentId("eventatlas");
		HistoryConfig history = FACTORY.createHistoryConfig();
		HistorizationFilter filter = FACTORY.createHistorizationFilter();
		filter.setName(filterName);
		history.getFilters().add(filter);
		deployment.setHistory(history);
		return deployment;
	}

	/** A PID ConfigAdmin does not have yet: {@code getProperties()} is null. */
	private Configuration emptyConfiguration(String pid) throws IOException {
		return configurationWith(pid, null);
	}

	private Configuration configurationWith(String pid, Dictionary<String, Object> existing) throws IOException {
		Configuration configuration = mock(Configuration.class);
		when(configuration.getPid()).thenReturn(pid);
		when(configuration.getProperties()).thenReturn(existing);
		configurations.put(pid, configuration);
		int separator = pid.indexOf('~');
		if (separator > 0) {
			when(configurationAdmin.getFactoryConfiguration(eq(pid.substring(0, separator)),
					eq(pid.substring(separator + 1)), anyString())).thenReturn(configuration);
		} else {
			when(configurationAdmin.getConfiguration(eq(pid), anyString())).thenReturn(configuration);
		}
		return configuration;
	}

	private void stampAsOwnedBy(Configuration configuration, String deploymentId) {
		when(configuration.getProperties())
				.thenReturn(properties(Map.of(DeploymentConfiguratorImpl.OWNER_PROPERTY, deploymentId)));
	}

	private void listConfigurationsReturns(Configuration... found) throws Exception {
		when(configurationAdmin.listConfigurations(anyString())).thenReturn(found);
	}

	private static Dictionary<String, Object> properties(Map<String, Object> values) {
		return new Hashtable<>(values);
	}

	@SuppressWarnings("unchecked")
	private static Dictionary<String, Object> captureUpdate(Configuration configuration) throws IOException {
		org.mockito.ArgumentCaptor<Dictionary<String, Object>> captor = org.mockito.ArgumentCaptor
				.forClass(Dictionary.class);
		verify(configuration).update(captor.capture());
		return captor.getValue();
	}
}
