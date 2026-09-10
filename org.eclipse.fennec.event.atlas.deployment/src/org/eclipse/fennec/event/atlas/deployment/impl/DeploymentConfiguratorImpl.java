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

import java.io.IOException;
import java.util.Dictionary;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.eclipse.fennec.emf.osgi.eobject.registry.EObjectRegistryConstants;
import org.eclipse.fennec.emf.osgi.eobject.registry.EObjectRegistryEntry;
import org.eclipse.fennec.emf.osgi.eobject.registry.EObjectRegistryListener;
import org.eclipse.fennec.event.atlas.deployment.ConfigurationRecord;
import org.eclipse.fennec.event.atlas.deployment.DeploymentPlan;
import org.eclipse.fennec.event.atlas.model.deployment.EventAtlasDeployment;
import org.osgi.framework.Constants;
import org.osgi.framework.InvalidSyntaxException;
import org.osgi.service.cm.Configuration;
import org.osgi.service.cm.ConfigurationAdmin;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.ConfigurationPolicy;
import org.osgi.service.component.annotations.Deactivate;
import org.osgi.service.component.annotations.Reference;

/**
 * Applies {@link EventAtlasDeployment} models as ConfigAdmin configurations.
 * <p>
 * The model arrives the same way mappings and profiles do - as content of a named EObject
 * registry, here {@code event-atlas-deployment} (override with the
 * {@code emf.eobject.registry.name} component property). That buys the same content sources for
 * free: a directory of XMI files through a {@code FileEObjectProvider}, or a Model Atlas through
 * an {@code AtlasEObjectProvider}. The registry replays its content on bind, so late binding is
 * indistinguishable from early binding.
 * <p>
 * <b>Every configuration this component writes is stamped with {@code #OWNER_PROPERTY}.</b> That
 * is what keeps the promise that a PID has exactly one writer: a configuration that already
 * exists without the stamp belongs to someone else - typically a block still present in a
 * configurator JSON bundle - and is left alone with a warning rather than fought over. Remove the
 * block from the JSON to hand the PID to the model.
 * <p>
 * Registry callbacks arrive on the writing thread while the registry holds its lock, so the work
 * is handed to a single-threaded executor: ConfigAdmin updates trigger component reactivation and
 * must not run under that lock. Single-threaded, so two models never interleave their writes.
 */
@Component(immediate = true, configurationPid = DeploymentConfiguratorImpl.PID, //
		configurationPolicy = ConfigurationPolicy.OPTIONAL, //
		property = EObjectRegistryConstants.EMF_EOBJECT_REGISTRY_NAME + "=event-atlas-deployment")
public class DeploymentConfiguratorImpl implements EObjectRegistryListener {

	public static final String PID = "event.atlas.deployment";

	/** Marks a configuration as written from a deployment model, and by which one. */
	public static final String OWNER_PROPERTY = "event.atlas.deployment.owner";

	private static final Logger logger = Logger.getLogger(DeploymentConfiguratorImpl.class.getName());

	/** PIDs written per deployment id, so a shrinking model deletes what it no longer asks for. */
	private final Map<String, Set<String>> writtenPids = new ConcurrentHashMap<>();

	private volatile ExecutorService applier;

	@Reference
	private ConfigurationAdmin configurationAdmin;

	@Activate
	void activate() {
		applier = Executors.newSingleThreadExecutor(runnable -> {
			Thread thread = new Thread(runnable, "event-atlas-deployment");
			thread.setDaemon(true);
			return thread;
		});
	}

	@Deactivate
	void deactivate() {
		ExecutorService running = applier;
		applier = null;
		if (running != null) {
			running.shutdownNow();
		}
		// Configurations are deliberately NOT deleted here: a bundle refresh would otherwise
		// tear down the runtime's wiring, and ConfigAdmin is persistent by design.
	}

	@Override
	public void entryAdded(EObjectRegistryEntry entry) {
		submit(entry, false);
	}

	@Override
	public void entryUpdated(EObjectRegistryEntry entry, EObjectRegistryEntry oldEntry) {
		submit(entry, false);
	}

	@Override
	public void entryRemoved(EObjectRegistryEntry entry) {
		submit(entry, true);
	}

	private void submit(EObjectRegistryEntry entry, boolean removal) {
		if (!(entry.object() instanceof EventAtlasDeployment deployment)) {
			logger.log(Level.FINE, () -> "Ignoring registry entry '" + entry.key()
					+ "' - not an EventAtlasDeployment but " + entry.object().eClass().getName());
			return;
		}
		ExecutorService executor = applier;
		if (executor == null) {
			return;
		}
		executor.execute(() -> {
			try {
				if (removal) {
					retract(entry.key());
				} else {
					apply(entry.key(), deployment);
				}
			} catch (RuntimeException e) {
				logger.log(Level.SEVERE, e, () -> "Failed to apply deployment '" + entry.key() + "'");
			}
		});
	}

	/**
	 * Writes what the model asks for and deletes what a previous version of it asked for and this
	 * one no longer does.
	 */
	void apply(String deploymentId, EventAtlasDeployment deployment) {
		DeploymentPlan plan = DeploymentPlanner.plan(deployment);
		plan.problems().forEach(problem -> logger.warning("Deployment '" + deploymentId + "': " + problem));

		Set<String> nowWritten = new LinkedHashSet<>();
		for (ConfigurationRecord record : plan.records()) {
			if (write(deploymentId, record)) {
				nowWritten.add(record.pid());
			}
		}
		Set<String> stale = new HashSet<>(writtenPids.getOrDefault(deploymentId, Set.of()));
		stale.removeAll(nowWritten);
		stale.forEach(pid -> delete(deploymentId, pid));
		writtenPids.put(deploymentId, nowWritten);
		logger.info(() -> "Deployment '" + deploymentId + "': " + nowWritten.size() + " configuration(s) applied, "
				+ stale.size() + " removed" + (plan.hasProblems() ? ", " + plan.problems().size() + " refused" : ""));
	}

	/** Deletes everything a deployment wrote, because the model itself is gone. */
	void retract(String deploymentId) {
		Set<String> pids = writtenPids.remove(deploymentId);
		if (pids == null || pids.isEmpty()) {
			return;
		}
		pids.forEach(pid -> delete(deploymentId, pid));
		logger.info(() -> "Deployment '" + deploymentId + "' removed: " + pids.size() + " configuration(s) deleted");
	}

	private boolean write(String deploymentId, ConfigurationRecord record) {
		try {
			Configuration configuration = configurationFor(record);
			String owner = ownerOf(configuration);
			if (owner == null && configuration.getProperties() != null) {
				logger.warning("Deployment '" + deploymentId + "': " + record.pid()
						+ " already exists and was not written from a deployment model - left untouched. "
						+ "Remove it from the configurator JSON to let the model own it.");
				return false;
			}
			if (owner != null && !owner.equals(deploymentId)) {
				logger.warning("Deployment '" + deploymentId + "': " + record.pid() + " is owned by deployment '"
						+ owner + "' - left untouched. Two deployment models must not configure the same PID.");
				return false;
			}
			configuration.update(propertiesOf(deploymentId, record));
			return true;
		} catch (IOException e) {
			logger.log(Level.SEVERE, e, () -> "Could not write " + record.pid());
			return false;
		}
	}

	private void delete(String deploymentId, String pid) {
		try {
			for (Configuration configuration : existing(pid)) {
				if (deploymentId.equals(ownerOf(configuration))) {
					configuration.delete();
				}
			}
		} catch (IOException | RuntimeException e) {
			logger.log(Level.WARNING, e, () -> "Could not delete " + pid);
		}
	}

	private Configuration configurationFor(ConfigurationRecord record) throws IOException {
		// Location "?" binds the configuration to no single bundle, which is what a
		// configuration written on someone else's behalf needs.
		return record.isFactoryInstance()
				? configurationAdmin.getFactoryConfiguration(record.factoryPid(), record.instanceName(), "?")
				: configurationAdmin.getConfiguration(record.pid(), "?");
	}

	private Configuration[] existing(String pid) throws IOException {
		try {
			Configuration[] found = configurationAdmin
					.listConfigurations("(" + Constants.SERVICE_PID + "=" + pid + ")");
			return found == null ? new Configuration[0] : found;
		} catch (InvalidSyntaxException e) {
			throw new IOException(e);
		}
	}

	private static String ownerOf(Configuration configuration) {
		Dictionary<String, Object> properties = configuration.getProperties();
		if (properties == null) {
			return null;
		}
		Object owner = properties.get(OWNER_PROPERTY);
		return owner instanceof String text ? text : null;
	}

	private static Dictionary<String, Object> propertiesOf(String deploymentId, ConfigurationRecord record) {
		Map<String, Object> ordered = new LinkedHashMap<>(record.properties());
		ordered.put(OWNER_PROPERTY, deploymentId);
		return new Hashtable<>(ordered);
	}
}
