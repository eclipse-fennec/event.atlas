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

import java.time.Duration;
import java.util.List;

import org.eclipse.fennec.event.atlas.deployment.ConfigurationRecord;
import org.eclipse.fennec.event.atlas.deployment.DeploymentPlan;
import org.eclipse.fennec.event.atlas.model.deployment.AtlasBinding;
import org.eclipse.fennec.event.atlas.model.deployment.ChangeMode;
import org.eclipse.fennec.event.atlas.model.deployment.ChannelTransport;
import org.eclipse.fennec.event.atlas.model.deployment.DeploymentFactory;
import org.eclipse.fennec.event.atlas.model.deployment.EventAtlasDeployment;
import org.eclipse.fennec.event.atlas.model.deployment.HistorizationFilter;
import org.eclipse.fennec.event.atlas.model.deployment.HistoryConfig;
import org.eclipse.fennec.event.atlas.model.deployment.HousekeepingPolicy;
import org.eclipse.fennec.event.atlas.model.deployment.HttpEndpoint;
import org.eclipse.fennec.event.atlas.model.deployment.InMemoryStorage;
import org.eclipse.fennec.event.atlas.model.deployment.InferenceConfig;
import org.eclipse.fennec.event.atlas.model.deployment.IngestChannel;
import org.eclipse.fennec.event.atlas.model.deployment.MqttBroker;
import org.eclipse.fennec.event.atlas.model.deployment.PayloadFormat;
import org.eclipse.fennec.event.atlas.model.deployment.TimescaleStorage;
import org.junit.jupiter.api.Test;

class DeploymentPlannerTest {

	private static final DeploymentFactory FACTORY = DeploymentFactory.eINSTANCE;

	@Test
	void emptyDeploymentConfiguresNothing() {
		DeploymentPlan plan = DeploymentPlanner.plan(deployment());

		assertThat(plan.records()).isEmpty();
		assertThat(plan.problems()).isEmpty();
	}

	@Test
	void missingDeploymentIsReportedRatherThanThrown() {
		DeploymentPlan plan = DeploymentPlanner.plan(null);

		assertThat(plan.records()).isEmpty();
		assertThat(plan.problems()).hasSize(1);
	}

	@Test
	void httpSectionConfiguresWhiteboardJerseyAndNorthboundRest() {
		EventAtlasDeployment deployment = deployment();
		HttpEndpoint http = FACTORY.createHttpEndpoint();
		http.setPort(8090);
		deployment.setHttp(http);

		DeploymentPlan plan = DeploymentPlanner.plan(deployment);

		assertThat(pids(plan)).containsExactly("org.apache.felix.http~eventHttp",
				"JakartarsServletWhiteboardRuntimeComponent~eventrest", "sensinact.northbound.rest");
		assertThat(plan.record("org.apache.felix.http~eventHttp").properties())
				.containsEntry("org.osgi.service.http.port", 8090)
				.containsEntry("org.apache.felix.http.context_path", "event/")
				.containsEntry("org.apache.felix.http.runtime.init.id", "event");
		assertThat(plan.record("sensinact.northbound.rest").properties())
				.containsEntry("osgi.jakartars.whiteboard.target", "(jersey.jakartars.whiteboard.name=eventrest)");
	}

	@Test
	void atlasBindingAlwaysRequiresTheMappingMetamodel() {
		EventAtlasDeployment deployment = deployment();
		deployment.setAtlas(atlas("http://localhost:8080/atlas/rest", "jena"));

		DeploymentPlan plan = DeploymentPlanner.plan(deployment);

		assertThat(stringArray(plan, "AtlasEObjectProvider~atlas", "required.nsuris"))
				.containsExactly(DeploymentPlanner.MAPPING_NSURI, "https://datainmotion.de/demo/m5airq/1.0");
	}

	@Test
	void atlasBindingWithoutScopeIsRefused() {
		EventAtlasDeployment deployment = deployment();
		deployment.setAtlas(atlas("http://localhost:8080/atlas/rest", "  "));

		DeploymentPlan plan = DeploymentPlanner.plan(deployment);

		assertThat(plan.records()).isEmpty();
		assertThat(plan.problems()).singleElement().asString().contains("baseUri and scope");
	}

	@Test
	void mqttChannelBindsItsBrokerAndStatesItsFormat() {
		EventAtlasDeployment deployment = deployment();
		MqttBroker broker = FACTORY.createMqttBroker();
		broker.setId("eventatlas-broker");
		broker.getTopics().add("eventatlas/#");
		deployment.getBrokers().add(broker);
		deployment.getChannels().add(mqttChannel("eventatlas-mqtt-json", PayloadFormat.JSON, "eventatlas/json/#"));

		DeploymentPlan plan = DeploymentPlanner.plan(deployment);

		assertThat(plan.record("sensinact.southbound.mqtt~eventatlas-broker").properties())
				.containsEntry("id", "eventatlas-broker").containsEntry("port", 1883);
		ConfigurationRecord channel = plan.record("event.atlas.southbound.mqtt~eventatlas-mqtt-json");
		assertThat(channel.properties()).containsEntry("format", "json")
				.containsEntry("mqtt.handler.id", "eventatlas-broker");
		assertThat((String[]) channel.properties().get("mqttTopics")).containsExactly("eventatlas/json/#");
	}

	@Test
	void mqttChannelWithoutTopicsIsRefusedRatherThanWrittenAsAFailingActivation() {
		EventAtlasDeployment deployment = deployment();
		IngestChannel channel = FACTORY.createIngestChannel();
		channel.setName("silent");
		deployment.getChannels().add(channel);

		DeploymentPlan plan = DeploymentPlanner.plan(deployment);

		assertThat(plan.records()).isEmpty();
		assertThat(plan.problems()).singleElement().asString().contains("no topics");
	}

	@Test
	void jsonChannelWithoutTypeMapIsReportedBecausePayloadsWouldBeDropped() {
		EventAtlasDeployment deployment = deployment();
		deployment.getChannels().add(mqttChannel("json-channel", PayloadFormat.JSON, "eventatlas/json/#"));

		DeploymentPlan plan = DeploymentPlanner.plan(deployment);

		assertThat(plan.problems()).singleElement().asString().contains("codecTypeMapId");
	}

	@Test
	void restChannelConfiguresTheSharedApplicationOnly() {
		EventAtlasDeployment deployment = deployment();
		deployment.setHttp(FACTORY.createHttpEndpoint());
		IngestChannel channel = FACTORY.createIngestChannel();
		channel.setName("rest-xmi");
		channel.setTransport(ChannelTransport.REST);
		deployment.getChannels().add(channel);

		DeploymentPlan plan = DeploymentPlanner.plan(deployment);

		assertThat(pids(plan)).contains("event.atlas.southbound.rest")
				.doesNotContain("event.atlas.southbound.mqtt~rest-xmi");
		assertThat(plan.record("event.atlas.southbound.rest").properties())
				.containsEntry("osgi.jakartars.whiteboard.target", "(jersey.jakartars.whiteboard.name=eventrest)");
	}

	@Test
	void restChannelWithoutAnHttpSectionIsReported() {
		EventAtlasDeployment deployment = deployment();
		IngestChannel channel = FACTORY.createIngestChannel();
		channel.setName("rest-xmi");
		channel.setTransport(ChannelTransport.REST);
		deployment.getChannels().add(channel);

		DeploymentPlan plan = DeploymentPlanner.plan(deployment);

		assertThat(plan.problems()).singleElement().asString().contains("http section");
	}

	@Test
	void theModelNamesTheSecretsVariableAndNeverTheSecret() {
		DeploymentPlan plan = DeploymentPlanner.plan(withHistory(timescale(null)));

		assertThat(plan.record("sensinact.history.timescale").properties())
				.containsEntry(".password", "$[env:TIMESCALE_PWD;default=]");
	}

	@Test
	void aBlankPasswordVariableOmitsThePropertyEntirely() {
		TimescaleStorage storage = timescale(null);
		storage.setPasswordVariable("  ");

		DeploymentPlan plan = DeploymentPlanner.plan(withHistory(storage));

		assertThat(plan.record("sensinact.history.timescale").properties()).doesNotContainKey(".password");
	}

	@Test
	void aBrokerReferencesItsOwnPasswordVariable() {
		EventAtlasDeployment deployment = deployment();
		MqttBroker broker = FACTORY.createMqttBroker();
		broker.setId("eventatlas-broker");
		broker.setPasswordVariable("MY_BROKER_SECRET");
		deployment.getBrokers().add(broker);

		DeploymentPlan plan = DeploymentPlanner.plan(deployment);

		assertThat(plan.record("sensinact.southbound.mqtt~eventatlas-broker").properties())
				.containsEntry(".password", "$[env:MY_BROKER_SECRET;default=]");
	}

	@Test
	void timescaleStorageComposesTheJdbcUrlFromHostPortAndDatabase() {
		DeploymentPlan plan = DeploymentPlanner.plan(withHistory(timescale(null)));

		assertThat(plan.record("sensinact.history.timescale").properties()).containsEntry("url",
				"jdbc:postgresql://db.internal:5432/sensinactHistory");
	}

	@Test
	void anExplicitJdbcUrlWinsOverTheComposedOne() {
		DeploymentPlan plan = DeploymentPlanner.plan(withHistory(timescale("jdbc:postgresql://other/db")));

		assertThat(plan.record("sensinact.history.timescale").properties()).containsEntry("url",
				"jdbc:postgresql://other/db");
	}

	@Test
	void theHistoryProviderNameReachesBothTheStoreAndSensorThings() {
		DeploymentPlan plan = DeploymentPlanner.plan(withHistory(timescale(null)));

		assertThat(plan.record("sensinact.history.timescale").properties()).containsEntry("provider",
				"brokerHistory");
		assertThat(plan.record("sensinact.sensorthings.northbound.rest").properties())
				.containsEntry("history.provider", "brokerHistory");
	}

	@Test
	void inMemoryStorageUsesItsOwnPid() {
		InMemoryStorage storage = FACTORY.createInMemoryStorage();
		storage.setMaxValuesPerResource(500);

		DeploymentPlan plan = DeploymentPlanner.plan(withHistory(storage));

		assertThat(pids(plan)).contains("sensinact.history.inmemory").doesNotContain("sensinact.history.timescale");
		assertThat(plan.record("sensinact.history.inmemory").properties())
				.containsEntry("max.values.per.resource", 500);
	}

	@Test
	void deadbandFilterWritesItsThresholdsAsStrings() {
		EventAtlasDeployment deployment = withHistory(timescale(null));
		HistorizationFilter filter = FACTORY.createHistorizationFilter();
		filter.setName("slow-drift");
		filter.setChangeMode(ChangeMode.DEADBAND);
		filter.setChangeThresholdPercent(5.0);
		filter.setChangeMaxInterval(Duration.ofMinutes(15));
		deployment.getHistory().getFilters().add(filter);

		DeploymentPlan plan = DeploymentPlanner.plan(deployment);

		assertThat(plan.record("sensinact.history.filter~slow-drift").properties())
				.containsEntry("change.mode", "deadband").containsEntry("change.threshold.percent", "5.0")
				.containsEntry("change.max.interval", "PT15M").doesNotContainKey("change.threshold");
	}

	@Test
	void deadbandFilterWithoutAThresholdIsRefused() {
		EventAtlasDeployment deployment = withHistory(timescale(null));
		HistorizationFilter filter = FACTORY.createHistorizationFilter();
		filter.setName("pointless");
		filter.setChangeMode(ChangeMode.DEADBAND);
		deployment.getHistory().getFilters().add(filter);

		DeploymentPlan plan = DeploymentPlanner.plan(deployment);

		assertThat(pids(plan)).doesNotContain("sensinact.history.filter~pointless");
		assertThat(plan.problems()).singleElement().asString().contains("DEADBAND without a threshold");
	}

	@Test
	void onChangeModeIsHyphenatedOnTheWire() {
		EventAtlasDeployment deployment = withHistory(timescale(null));
		HistorizationFilter filter = FACTORY.createHistorizationFilter();
		filter.setName("changes-only");
		filter.setChangeMode(ChangeMode.ON_CHANGE);
		deployment.getHistory().getFilters().add(filter);

		DeploymentPlan plan = DeploymentPlanner.plan(deployment);

		assertThat(plan.record("sensinact.history.filter~changes-only").properties())
				.containsEntry("change.mode", "on-change");
	}

	@Test
	void anUnsetKeepCountIsOmittedRatherThanWrittenAsZero() {
		EventAtlasDeployment deployment = withHistory(timescale(null));
		HousekeepingPolicy policy = FACTORY.createHousekeepingPolicy();
		policy.setName("retention-only");
		policy.setRetentionPeriod(Duration.ofDays(30));
		deployment.getHistory().getHousekeeping().add(policy);

		DeploymentPlan plan = DeploymentPlanner.plan(deployment);

		assertThat(plan.record("sensinact.history.housekeeping~retention-only").properties())
				.containsEntry("retention.period", "PT720H").doesNotContainKey("keep.count")
				.doesNotContainKey("max.delete");
	}

	@Test
	void keepCountIsWrittenAsALongToMatchTheEnginesType() {
		EventAtlasDeployment deployment = withHistory(timescale(null));
		HousekeepingPolicy policy = FACTORY.createHousekeepingPolicy();
		policy.setName("bounded");
		policy.setKeepCount(1000);
		policy.setMaxDelete(50000);
		deployment.getHistory().getHousekeeping().add(policy);

		DeploymentPlan plan = DeploymentPlanner.plan(deployment);

		assertThat(plan.record("sensinact.history.housekeeping~bounded").properties())
				.containsEntry("keep.count", 1000L).containsEntry("max.delete", 50000L);
	}

	@Test
	void housekeepingWithNeitherRetentionNorKeepCountIsRefused() {
		EventAtlasDeployment deployment = withHistory(timescale(null));
		HousekeepingPolicy policy = FACTORY.createHousekeepingPolicy();
		policy.setName("nothing-to-do");
		deployment.getHistory().getHousekeeping().add(policy);

		DeploymentPlan plan = DeploymentPlanner.plan(deployment);

		assertThat(pids(plan)).doesNotContain("sensinact.history.housekeeping~nothing-to-do");
		assertThat(plan.problems()).singleElement().asString().contains("retentionPeriod and keepCount");
	}

	@Test
	void inferenceSectionWritesBothSwitchesEvenWhenOff() {
		EventAtlasDeployment deployment = deployment();
		InferenceConfig inference = FACTORY.createInferenceConfig();
		deployment.setInference(inference);

		DeploymentPlan plan = DeploymentPlanner.plan(deployment);

		assertThat(plan.record("event.atlas.southbound.sampling").properties()).containsEntry("enabled", false);
		assertThat(plan.record("event.atlas.model.inference").properties()).containsEntry("namespace", "")
				.containsEntry("maxRunsPerInterval", 5);
	}

	private static EventAtlasDeployment deployment() {
		EventAtlasDeployment deployment = FACTORY.createEventAtlasDeployment();
		deployment.setDeploymentId("test-deployment");
		return deployment;
	}

	private static AtlasBinding atlas(String baseUri, String scope) {
		AtlasBinding atlas = FACTORY.createAtlasBinding();
		atlas.setBaseUri(baseUri);
		atlas.setScope(scope);
		atlas.getMappingRegistries().add("sensinactmapping");
		atlas.getRequiredNsUris().add("https://datainmotion.de/demo/m5airq/1.0");
		return atlas;
	}

	private static IngestChannel mqttChannel(String name, PayloadFormat format, String topic) {
		IngestChannel channel = FACTORY.createIngestChannel();
		channel.setName(name);
		channel.setFormat(format);
		channel.setBrokerId("eventatlas-broker");
		channel.getTopics().add(topic);
		return channel;
	}

	private static TimescaleStorage timescale(String url) {
		TimescaleStorage storage = FACTORY.createTimescaleStorage();
		storage.setHost("db.internal");
		storage.setUrl(url);
		return storage;
	}

	private static EventAtlasDeployment withHistory(
			org.eclipse.fennec.event.atlas.model.deployment.HistoryStorage storage) {
		EventAtlasDeployment deployment = deployment();
		HistoryConfig history = FACTORY.createHistoryConfig();
		history.setStorage(storage);
		deployment.setHistory(history);
		return deployment;
	}

	private static List<String> pids(DeploymentPlan plan) {
		return plan.records().stream().map(ConfigurationRecord::pid).toList();
	}

	private static String[] stringArray(DeploymentPlan plan, String pid, String key) {
		return (String[]) plan.record(pid).properties().get(key);
	}
}
