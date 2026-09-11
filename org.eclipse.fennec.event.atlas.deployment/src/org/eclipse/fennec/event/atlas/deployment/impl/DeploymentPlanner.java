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

import java.time.Duration;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.fennec.event.atlas.deployment.ConfigurationRecord;
import org.eclipse.fennec.event.atlas.deployment.DeploymentPlan;
import org.eclipse.fennec.event.atlas.model.deployment.AtlasBinding;
import org.eclipse.fennec.event.atlas.model.deployment.ChangeMode;
import org.eclipse.fennec.event.atlas.model.deployment.ChannelTransport;
import org.eclipse.fennec.event.atlas.model.deployment.EventAtlasDeployment;
import org.eclipse.fennec.event.atlas.model.deployment.HistorizationFilter;
import org.eclipse.fennec.event.atlas.model.deployment.HistoryConfig;
import org.eclipse.fennec.event.atlas.model.deployment.HistoryStorage;
import org.eclipse.fennec.event.atlas.model.deployment.HousekeepingPolicy;
import org.eclipse.fennec.event.atlas.model.deployment.HttpEndpoint;
import org.eclipse.fennec.event.atlas.model.deployment.InMemoryStorage;
import org.eclipse.fennec.event.atlas.model.deployment.InferenceConfig;
import org.eclipse.fennec.event.atlas.model.deployment.IngestChannel;
import org.eclipse.fennec.event.atlas.model.deployment.MqttBroker;
import org.eclipse.fennec.event.atlas.model.deployment.PrefetchMode;
import org.eclipse.fennec.event.atlas.model.deployment.TimescaleStorage;

/**
 * Translates an {@link EventAtlasDeployment} into the ConfigAdmin configurations it asks for.
 * <p>
 * Pure and side-effect free on purpose: everything that decides <em>what</em> a deployment means
 * is here and unit-tested, while {@link DeploymentConfiguratorImpl} only decides <em>when</em> to
 * write it. The two rules that shape the output:
 * <ul>
 * <li><b>An absent section emits nothing.</b> A deployment model is additive - what it does not
 * declare keeps coming from the configurator JSON and its {@code $[env:...]} placeholders, so a
 * runtime can adopt the model one concern at a time and no PID ever has two writers.</li>
 * <li><b>An unset scalar is omitted, not written as zero.</b> This is not cosmetic:
 * {@code keep.count} and {@code max.delete} use {@code -1} as their unset sentinel, so writing a
 * literal {@code 0} would ask housekeeping to keep <em>no</em> values and delete everything.</li>
 * </ul>
 */
public final class DeploymentPlanner {

	/** The mapping metamodel's own nsURI, always required before an Atlas sync can resolve. */
	static final String MAPPING_NSURI = "https://fennec.eclipse.org/event.atlas/mapping/1.0";

	static final String PID_FELIX_HTTP_FACTORY = "org.apache.felix.http";
	static final String PID_JERSEY_WHITEBOARD_FACTORY = "JakartarsServletWhiteboardRuntimeComponent";
	static final String PID_NORTHBOUND_REST = "sensinact.northbound.rest";
	static final String PID_SENSORTHINGS_REST = "sensinact.sensorthings.northbound.rest";
	static final String PID_ATLAS_CLIENT_FACTORY = "org.eclipse.fennec.model.atlas.rest.client";
	static final String PID_ATLAS_PROVIDER_FACTORY = "AtlasEObjectProvider";
	static final String PID_MQTT_BROKER_FACTORY = "sensinact.southbound.mqtt";
	static final String PID_MQTT_CHANNEL_FACTORY = "event.atlas.southbound.mqtt";
	static final String PID_REST_CHANNELS = "event.atlas.southbound.rest";
	static final String PID_INGEST = "event.atlas.southbound.ingest";
	static final String PID_TIMESCALE = "sensinact.history.timescale";
	static final String PID_INMEMORY = "sensinact.history.inmemory";
	static final String PID_HISTORY_FILTER_FACTORY = "sensinact.history.filter";
	static final String PID_HOUSEKEEPING_FACTORY = "sensinact.history.housekeeping";
	static final String PID_SAMPLING = "event.atlas.southbound.sampling";
	static final String PID_INFERENCE = "event.atlas.model.inference";

	/** One named whiteboard per runtime, so its instance name is fixed rather than derived. */
	static final String HTTP_WHITEBOARD_INSTANCE = "eventHttp";
	static final String ATLAS_INSTANCE = "atlas";
	static final String MAPPING_REGISTRY = "sensinact-mappings";

	private DeploymentPlanner() {
	}

	public static DeploymentPlan plan(EventAtlasDeployment deployment) {
		List<ConfigurationRecord> records = new ArrayList<>();
		List<String> problems = new ArrayList<>();
		if (deployment == null) {
			problems.add("No deployment given");
			return new DeploymentPlan(records, problems);
		}
		planHttp(deployment.getHttp(), records);
		planAtlas(deployment.getAtlas(), records, problems);
		planBrokers(deployment.getBrokers(), records, problems);
		planChannels(deployment, records, problems);
		planHistory(deployment.getHistory(), records, problems);
		planInference(deployment.getInference(), records);
		return new DeploymentPlan(records, problems);
	}

	private static void planHttp(HttpEndpoint http, List<ConfigurationRecord> records) {
		if (http == null) {
			return;
		}
		String contextPath = trimmed(http.getContextPath());
		// The Jersey whiteboard selects the HTTP runtime through (rest=true), which is what
		// `runtime.init.rest` publishes; `runtime.init.id` is the runtime's own identifier.
		String runtimeId = contextPath.endsWith("/") ? contextPath.substring(0, contextPath.length() - 1) : contextPath;
		Map<String, Object> whiteboard = new LinkedHashMap<>();
		whiteboard.put("org.osgi.service.http.port", http.getPort());
		whiteboard.put("org.osgi.service.http.host", trimmed(http.getHost()));
		whiteboard.put("org.apache.felix.http.context_path", contextPath);
		whiteboard.put("org.apache.felix.http.name", "Event Http Whiteboard");
		whiteboard.put("org.apache.felix.http.runtime.init.id", runtimeId);
		whiteboard.put("org.apache.felix.http.runtime.init.rest", "true");
		records.add(record(PID_FELIX_HTTP_FACTORY, HTTP_WHITEBOARD_INSTANCE, whiteboard));

		String whiteboardName = trimmed(http.getWhiteboardName());
		Map<String, Object> jersey = new LinkedHashMap<>();
		jersey.put("jersey.jakartars.whiteboard.name", whiteboardName);
		jersey.put("jersey.context.path", trimmed(http.getRestPath()));
		jersey.put("osgi.http.whiteboard.target", "(rest=true)");
		records.add(record(PID_JERSEY_WHITEBOARD_FACTORY, whiteboardName, jersey));

		Map<String, Object> northbound = new LinkedHashMap<>();
		northbound.put("allow.anonymous", http.isAllowAnonymous());
		northbound.put("osgi.jakartars.whiteboard.target", whiteboardTarget(whiteboardName));
		records.add(record(PID_NORTHBOUND_REST, northbound));
	}

	private static void planAtlas(AtlasBinding atlas, List<ConfigurationRecord> records, List<String> problems) {
		if (atlas == null) {
			return;
		}
		String scope = trimmed(atlas.getScope());
		if (scope.isEmpty() || trimmed(atlas.getBaseUri()).isEmpty()) {
			problems.add("Model Atlas binding needs both baseUri and scope - section skipped");
			return;
		}
		Map<String, Object> client = new LinkedHashMap<>();
		client.put("base.uri", trimmed(atlas.getBaseUri()));
		client.put("scope.allow.list", new String[] { scope });
		client.put("default.scope", scope);
		client.put("mode", atlas.getPrefetchMode() == null ? PrefetchMode.EAGER.getName() : atlas.getPrefetchMode().getName());
		client.put("eager.scopes", new String[] { scope });
		client.put("eager.stages", new String[] { trimmed(atlas.getStage()) });
		client.put("drift.check.interval.ms", atlas.getDriftCheckIntervalMs());
		records.add(record(PID_ATLAS_CLIENT_FACTORY, ATLAS_INSTANCE, client));

		Map<String, Object> provider = new LinkedHashMap<>();
		provider.put("atlasScope.target", "(atlas.scope=" + scope + ")");
		provider.put("writer.target", "(emf.eobject.registry.name=" + MAPPING_REGISTRY + ")");
		provider.put("emf.eobject.provider.name", "atlas-mappings");
		provider.put("registries", strings(atlas.getMappingRegistries()));
		provider.put("key.feature", "mid");
		provider.put("required.nsuris", requiredNsUris(atlas));
		provider.put("refresh.interval.ms", atlas.getRefreshIntervalMs());
		records.add(record(PID_ATLAS_PROVIDER_FACTORY, ATLAS_INSTANCE, provider));
	}

	/**
	 * The mapping metamodel is always needed for a mapping to resolve, and leaving it out fails
	 * silently - the sync is simply postponed forever. It is added rather than demanded.
	 */
	private static String[] requiredNsUris(AtlasBinding atlas) {
		List<String> nsUris = new ArrayList<>();
		nsUris.add(MAPPING_NSURI);
		for (String nsUri : atlas.getRequiredNsUris()) {
			String candidate = trimmed(nsUri);
			if (!candidate.isEmpty() && !nsUris.contains(candidate)) {
				nsUris.add(candidate);
			}
		}
		return nsUris.toArray(String[]::new);
	}

	private static void planBrokers(List<MqttBroker> brokers, List<ConfigurationRecord> records, List<String> problems) {
		for (MqttBroker broker : brokers) {
			String id = trimmed(broker.getId());
			if (id.isEmpty()) {
				problems.add("An MQTT broker without an id cannot be referenced by a channel - skipped");
				continue;
			}
			Map<String, Object> properties = new LinkedHashMap<>();
			properties.put("id", id);
			properties.put("protocol", trimmed(broker.getProtocol()));
			properties.put("host", trimmed(broker.getHost()));
			properties.put("port", broker.getPort());
			properties.put("user", trimmed(broker.getUser()));
			putSecretReference(properties, broker.getPasswordVariable());
			properties.put("topics", strings(broker.getTopics()));
			records.add(record(PID_MQTT_BROKER_FACTORY, id, properties));
		}
	}

	private static void planChannels(EventAtlasDeployment deployment, List<ConfigurationRecord> records,
			List<String> problems) {
		List<IngestChannel> channels = deployment.getChannels();
		String codecTypeMapId = "";
		boolean restChannelDeclared = false;
		for (IngestChannel channel : channels) {
			String name = trimmed(channel.getName());
			if (name.isEmpty()) {
				problems.add("An ingest channel without a name cannot be configured - skipped");
				continue;
			}
			if (!trimmed(channel.getCodecTypeMapId()).isEmpty()) {
				codecTypeMapId = trimmed(channel.getCodecTypeMapId());
			}
			if (channel.getTransport() == ChannelTransport.REST) {
				restChannelDeclared = true;
				continue;
			}
			if (channel.getTopics().isEmpty()) {
				problems.add("MQTT channel '" + name + "' declares no topics, which is a refused activation - skipped");
				continue;
			}
			Map<String, Object> properties = new LinkedHashMap<>();
			properties.put("mqttTopics", strings(channel.getTopics()));
			properties.put("mqtt.handler.id", trimmed(channel.getBrokerId()));
			properties.put("format", format(channel));
			properties.put("name", name);
			records.add(record(PID_MQTT_CHANNEL_FACTORY, name, properties));
		}
		// A REST channel needs no per-channel configuration: one application serves them all and
		// the channel is a path segment. What it does need is the whiteboard it is mounted on -
		// without an application of its own it joins the default one, which SensorThings shadows.
		if (restChannelDeclared && deployment.getHttp() != null) {
			records.add(record(PID_REST_CHANNELS,
					Map.of("osgi.jakartars.whiteboard.target", whiteboardTarget(trimmed(deployment.getHttp().getWhiteboardName())))));
		} else if (restChannelDeclared) {
			problems.add("A REST ingest channel needs an http section naming the whiteboard it is served on");
		}
		if (!codecTypeMapId.isEmpty()) {
			records.add(record(PID_INGEST, Map.of("codec.typeMapId", codecTypeMapId)));
		} else if (channels.stream().anyMatch(channel -> "json".equals(format(channel)))) {
			problems.add("A JSON channel is declared but no codecTypeMapId - JSON payloads would be dropped untyped");
		}
	}

	private static void planHistory(HistoryConfig history, List<ConfigurationRecord> records, List<String> problems) {
		if (history == null) {
			return;
		}
		String providerName = trimmed(history.getProviderName());
		HistoryStorage storage = history.getStorage();
		// No storage is a deliberate, useful state: the store still belongs to a configurator
		// JSON bundle, and only the filters and housekeeping below are taken over by the model.
		if (storage == null) {
			planFilters(history.getFilters(), records, problems);
			planHousekeeping(history.getHousekeeping(), records, problems);
			return;
		}
		Map<String, Object> properties = new LinkedHashMap<>();
		String pid;
		if (storage instanceof TimescaleStorage timescale) {
			pid = PID_TIMESCALE;
			properties.put("url", jdbcUrl(timescale));
			properties.put("user", trimmed(timescale.getUser()));
			putSecretReference(properties, timescale.getPasswordVariable());
		} else if (storage instanceof InMemoryStorage inMemory) {
			pid = PID_INMEMORY;
			if (inMemory.getMaxValuesPerResource() > 0) {
				properties.put("max.values.per.resource", inMemory.getMaxValuesPerResource());
			}
		} else {
			problems.add("Unknown history storage " + storage.eClass().getName() + " - section skipped");
			return;
		}
		properties.put("provider", providerName);
		properties.put("max.page.size", storage.getMaxPageSize());
		if (!storage.getIncludeResources().isEmpty()) {
			properties.put("include.resources", strings(storage.getIncludeResources()));
		}
		if (!storage.getExcludeResources().isEmpty()) {
			properties.put("exclude.resources", strings(storage.getExcludeResources()));
		}
		records.add(record(pid, properties));
		// The SensorThings northbound asks the history store for its Observations by name. Both
		// halves come from providerName here, which is the whole reason it is one model attribute.
		records.add(record(PID_SENSORTHINGS_REST, Map.of("history.provider", providerName)));

		planFilters(history.getFilters(), records, problems);
		planHousekeeping(history.getHousekeeping(), records, problems);
	}

	private static void planFilters(List<HistorizationFilter> filters, List<ConfigurationRecord> records,
			List<String> problems) {
		for (HistorizationFilter filter : filters) {
			String name = trimmed(filter.getName());
			if (name.isEmpty()) {
				problems.add("A historization filter without a name cannot be configured - skipped");
				continue;
			}
			ChangeMode mode = filter.getChangeMode() == null ? ChangeMode.ALL : filter.getChangeMode();
			boolean hasThreshold = filter.getChangeThreshold() > 0 || filter.getChangeThresholdPercent() > 0;
			if (mode == ChangeMode.DEADBAND && !hasThreshold) {
				problems.add("Filter '" + name + "' is DEADBAND without a threshold - skipped");
				continue;
			}
			Map<String, Object> properties = new LinkedHashMap<>();
			properties.put("name", name);
			if (!filter.getTargets().isEmpty()) {
				properties.put("target", strings(filter.getTargets()));
			}
			if (!filter.getIncludeResources().isEmpty()) {
				properties.put("include.resources", strings(filter.getIncludeResources()));
			}
			if (!filter.getExcludeResources().isEmpty()) {
				properties.put("exclude.resources", strings(filter.getExcludeResources()));
			}
			properties.put("change.mode", changeMode(mode));
			// The engine declares the thresholds as Strings, so they are written as Strings.
			if (filter.getChangeThreshold() > 0) {
				properties.put("change.threshold", String.valueOf(filter.getChangeThreshold()));
			}
			if (filter.getChangeThresholdPercent() > 0) {
				properties.put("change.threshold.percent", String.valueOf(filter.getChangeThresholdPercent()));
			}
			if (filter.getChangeMaxInterval() != null) {
				properties.put("change.max.interval", iso(filter.getChangeMaxInterval()));
			}
			records.add(record(PID_HISTORY_FILTER_FACTORY, name, properties));
		}
	}

	private static void planHousekeeping(List<HousekeepingPolicy> policies, List<ConfigurationRecord> records,
			List<String> problems) {
		for (HousekeepingPolicy policy : policies) {
			String name = trimmed(policy.getName());
			if (name.isEmpty()) {
				problems.add("A housekeeping policy without a name cannot be configured - skipped");
				continue;
			}
			if (policy.getRetentionPeriod() == null && policy.getKeepCount() <= 0) {
				problems.add("Housekeeping policy '" + name
						+ "' needs at least one of retentionPeriod and keepCount - skipped");
				continue;
			}
			Map<String, Object> properties = new LinkedHashMap<>();
			properties.put("name", name);
			if (!policy.getTargets().isEmpty()) {
				properties.put("target", strings(policy.getTargets()));
			}
			if (policy.getRetentionPeriod() != null) {
				properties.put("retention.period", iso(policy.getRetentionPeriod()));
			}
			// keep.count and max.delete default to -1 for "unset"; a written 0 would mean
			// "keep nothing" and "delete nothing", so an unset value is omitted instead.
			if (policy.getKeepCount() > 0) {
				properties.put("keep.count", (long) policy.getKeepCount());
			}
			if (policy.getMaxDelete() > 0) {
				properties.put("max.delete", (long) policy.getMaxDelete());
			}
			if (policy.getSchedulePeriod() != null) {
				properties.put("schedule.period", iso(policy.getSchedulePeriod()));
			}
			records.add(record(PID_HOUSEKEEPING_FACTORY, name, properties));
		}
	}

	private static void planInference(InferenceConfig inference, List<ConfigurationRecord> records) {
		if (inference == null) {
			return;
		}
		records.add(record(PID_SAMPLING, Map.of("enabled", inference.isSamplingEnabled())));
		Map<String, Object> properties = new LinkedHashMap<>();
		properties.put("namespace", trimmed(inference.getNamespace()));
		properties.put("maxRunsPerInterval", inference.getMaxRunsPerInterval());
		records.add(record(PID_INFERENCE, properties));
	}

	/**
	 * Writes {@code .password} as an {@code $[env:NAME;default=]} reference rather than a value.
	 * <p>
	 * A deployment model is <em>content</em>: stored in a Model Atlas it is exactly as readable as
	 * every other object there, and on at least one deployment every Model Atlas GET is served
	 * unauthenticated. So the model names the environment variable and never the secret.
	 * <p>
	 * The indirection resolves because the Felix interpolation plugin is an OSGi
	 * {@code ConfigurationPlugin}, which Configuration Admin invokes when properties are
	 * <em>delivered to the target service</em> — not when the configuration is created. A value
	 * written here through the ConfigAdmin API is therefore interpolated exactly like one that came
	 * from a configurator JSON resource.
	 */
	private static void putSecretReference(Map<String, Object> properties, String variableName) {
		String name = trimmed(variableName);
		if (!name.isEmpty()) {
			properties.put(".password", "$[env:" + name + ";default=]");
		}
	}

	private static String jdbcUrl(TimescaleStorage timescale) {
		String explicit = trimmed(timescale.getUrl());
		if (!explicit.isEmpty()) {
			return explicit;
		}
		return "jdbc:postgresql://" + trimmed(timescale.getHost()) + ":" + timescale.getPort() + "/"
				+ trimmed(timescale.getDatabase());
	}

	/** {@code ON_CHANGE} is spelled {@code on-change} on the wire. */
	private static String changeMode(ChangeMode mode) {
		return mode.getName().toLowerCase().replace('_', '-');
	}

	private static String format(IngestChannel channel) {
		return channel.getFormat() == null ? "xmi" : channel.getFormat().getName().toLowerCase();
	}

	private static String whiteboardTarget(String whiteboardName) {
		return "(jersey.jakartars.whiteboard.name=" + whiteboardName + ")";
	}

	private static String iso(Duration duration) {
		return duration.toString();
	}

	private static String[] strings(List<String> values) {
		return values.stream().map(DeploymentPlanner::trimmed).filter(value -> !value.isEmpty())
				.toArray(String[]::new);
	}

	private static String trimmed(String value) {
		return value == null ? "" : value.trim();
	}

	private static ConfigurationRecord record(String pid, Map<String, Object> properties) {
		return new ConfigurationRecord(pid, properties);
	}

	private static ConfigurationRecord record(String factoryPid, String name, Map<String, Object> properties) {
		return new ConfigurationRecord(factoryPid + "~" + name, properties);
	}
}
