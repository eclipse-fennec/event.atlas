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
package org.eclipse.fennec.event.atlas.mapping.tests;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.net.URL;
import java.time.Duration;
import java.time.Instant;
import java.util.Date;

import org.eclipse.emf.common.util.URI;
import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.emf.ecore.resource.ResourceSet;
import org.eclipse.fennec.event.atlas.mapping.ChangeRuleFilter;
import org.eclipse.fennec.event.atlas.mapping.InstancePusher;
import org.eclipse.fennec.event.atlas.mapping.ProviderMappingRegistry;
import org.eclipse.fennec.event.atlas.model.mapping.ProviderMapping;
import org.eclipse.sensinact.core.command.AbstractSensinactCommand;
import org.eclipse.sensinact.core.command.GatewayThread;
import org.eclipse.sensinact.core.model.SensinactModelManager;
import org.eclipse.sensinact.core.twin.SensinactDigitalTwin;
import org.eclipse.sensinact.core.twin.SensinactProvider;
import org.eclipse.sensinact.core.twin.SensinactResource;
import org.eclipse.sensinact.core.twin.SensinactService;
import org.gecko.weather.model.weather.MOSMIXSWeatherReport;
import org.gecko.weather.model.weather.WeatherFactory;
import org.gecko.weather.model.weather.WeatherReports;
import org.gecko.weather.model.weather.WeatherStation;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.osgi.test.common.annotation.InjectService;
import org.osgi.test.junit5.context.BundleContextExtension;
import org.osgi.test.junit5.service.ServiceExtension;
import org.osgi.util.promise.Promise;
import org.osgi.util.promise.PromiseFactory;

/**
 * Verifies that a resource's {@link org.eclipse.fennec.event.atlas.model.mapping.ChangeRule}
 * is applied on the way into the twin: a value the rule rejects never reaches the resource,
 * so the twin keeps the last accepted one.
 * <p>
 * Both routes a resource can take are covered - hand-written {@code ResourceMapping}s in the
 * {@code explicit} service, and resources auto-generated from a {@code ReferenceMapping} in
 * the {@code generated} service, whose rules come from the reference's bindings.
 * <p>
 * Every push carries its own timestamp, so the time throttle is driven by the payload clock
 * rather than by sleeping.
 * @author Ilenia Salvadori
 * @since 25.08.2026
 */
@ExtendWith(BundleContextExtension.class)
@ExtendWith(ServiceExtension.class)
public class ChangeRuleFilterTest {

	private static final String MAPPING_URI = "/data/ChangeRuleWeatherMapping.xmi";
	private static final String MODEL = "dwd-weather-change-rules";
	private static final String PROVIDER = "10567";
	private static final String EXPLICIT = "explicit";
	private static final String GENERATED = "generated";

	private final Instant base = Instant.parse("2026-08-25T10:00:00Z");

	@InjectService
	GatewayThread gatewayThread;

	@InjectService
	ProviderMappingRegistry mappingRegistry;

	@InjectService
	InstancePusher instancePusher;

	@InjectService
	ResourceSet resourceSet;

	/** Present only if the filter component is deployed - the whole test hinges on it. */
	@InjectService
	ChangeRuleFilter changeRuleFilter;

	@BeforeEach
	void registerMapping() throws Exception {
		URL mappingUrl = getClass().getResource(MAPPING_URI);
		assertNotNull(mappingUrl, MAPPING_URI + " must be part of the test bundle");
		Resource mappingResource = resourceSet.createResource(URI.createURI(mappingUrl.toString()));
		mappingResource.load(null);
		mappingRegistry.registerModelMapping((ProviderMapping) mappingResource.getContents().get(0));
	}

	@AfterEach
	void tearDown() {
		// Un-registering also drops the filter's retained baselines, which is what keeps the
		// tests independent of each other.
		mappingRegistry.dispose();
	}

	@Test
	@DisplayName("A percentage rule drops a change below its threshold and stores one above it")
	void percentageRule_storesOnlyBigEnoughChanges() throws Exception {
		push(reports(10.0f, 100.0f, 280.0f, 1000.0f, 1000.0f, base));
		assertEquals(10.0f, read(EXPLICIT, "windSpeed"), "the first value is always stored");

		push(reports(10.5f, 100.0f, 280.0f, 1000.0f, 1000.0f, base.plus(Duration.ofMinutes(1))));
		assertEquals(10.0f, read(EXPLICIT, "windSpeed"), "5% is below the 10% threshold");

		push(reports(12.0f, 100.0f, 280.0f, 1000.0f, 1000.0f, base.plus(Duration.ofMinutes(2))));
		assertEquals(12.0f, read(EXPLICIT, "windSpeed"), "20% is above the 10% threshold");
	}

	@Test
	@DisplayName("A percentage rule compares against the last stored value, not the previous push")
	void percentageRule_comparesAgainstTheLastStoredValue() throws Exception {
		push(reports(10.0f, 100.0f, 280.0f, 1000.0f, 1000.0f, base));
		// Small steps, each below the threshold measured step-to-step, that add up past it
		// measured against the stored value. A baseline that followed the rejected values
		// would let the drift through unnoticed: 11.2 is 5.7% away from 10.6, but 12% away
		// from the 10.0 the twin actually holds.
		push(reports(10.3f, 100.0f, 280.0f, 1000.0f, 1000.0f, base.plus(Duration.ofMinutes(1))));
		assertEquals(10.0f, read(EXPLICIT, "windSpeed"), "3%");
		push(reports(10.6f, 100.0f, 280.0f, 1000.0f, 1000.0f, base.plus(Duration.ofMinutes(2))));
		assertEquals(10.0f, read(EXPLICIT, "windSpeed"), "6% - still against the stored 10.0");
		push(reports(11.2f, 100.0f, 280.0f, 1000.0f, 1000.0f, base.plus(Duration.ofMinutes(3))));
		assertEquals(11.2f, read(EXPLICIT, "windSpeed"), "12% away from the stored value");
	}

	@Test
	@DisplayName("A count rule stores one value out of every three")
	void countRule_storesOneOfEveryThree() throws Exception {
		push(reports(10.0f, 100.0f, 280.0f, 1000.0f, 1000.0f, base));
		assertEquals(100.0f, read(EXPLICIT, "windDirection"), "the first value is always stored");

		push(reports(10.0f, 110.0f, 280.0f, 1000.0f, 1000.0f, base.plus(Duration.ofMinutes(1))));
		assertEquals(100.0f, read(EXPLICIT, "windDirection"), "second of three");

		push(reports(10.0f, 120.0f, 280.0f, 1000.0f, 1000.0f, base.plus(Duration.ofMinutes(2))));
		assertEquals(100.0f, read(EXPLICIT, "windDirection"), "third of three");

		push(reports(10.0f, 130.0f, 280.0f, 1000.0f, 1000.0f, base.plus(Duration.ofMinutes(3))));
		assertEquals(130.0f, read(EXPLICIT, "windDirection"), "the fourth push opens the next window");
	}

	@Test
	@DisplayName("A time throttle measures the payload's own timestamp")
	void timeThrottle_measuresThePayloadTimestamp() throws Exception {
		push(reports(10.0f, 100.0f, 280.0f, 1000.0f, 1000.0f, base));
		assertEquals(280.0f, read(EXPLICIT, "tempAboveSurface5"), "the first value is always stored");

		push(reports(10.0f, 100.0f, 281.0f, 1000.0f, 1000.0f, base.plus(Duration.ofMinutes(5))));
		assertEquals(280.0f, read(EXPLICIT, "tempAboveSurface5"), "5 minutes is inside the 10 minute window");

		push(reports(10.0f, 100.0f, 282.0f, 1000.0f, 1000.0f, base.plus(Duration.ofMinutes(20))));
		assertEquals(282.0f, read(EXPLICIT, "tempAboveSurface5"), "20 minutes is outside the window");
	}

	@Test
	@DisplayName("A resource without a change rule stores every value")
	void noRule_storesEveryValue() throws Exception {
		push(reports(10.0f, 100.0f, 280.0f, 1000.0f, 1000.0f, base));
		assertEquals(1000.0f, read(EXPLICIT, "visibility"));

		push(reports(10.0f, 100.0f, 280.0f, 1001.0f, 1000.0f, base.plus(Duration.ofMinutes(1))));
		assertEquals(1001.0f, read(EXPLICIT, "visibility"), "a 0.1% change, and no rule to stop it");
	}

	@Test
	@DisplayName("A binding gives the attribute it names its own rule, and the rest the default")
	void bindings_applyPerAttributeToGeneratedResources() throws Exception {
		push(reports(10.0f, 100.0f, 280.0f, 1000.0f, 1000.0f, base));
		assertEquals(10.0f, read(GENERATED, "windSpeed"));
		assertEquals(1000.0f, read(GENERATED, "surfacePressure"));

		push(reports(12.0f, 100.0f, 280.0f, 1000.0f, 1005.0f, base.plus(Duration.ofMinutes(1))));
		assertEquals(12.0f, read(GENERATED, "windSpeed"), "20% passes the binding's percentage rule");
		assertEquals(1000.0f, read(GENERATED, "surfacePressure"),
				"5 is far below the default binding's 1000 delta");
	}

	@Test
	@DisplayName("A binding's unit overrides the one annotated on the source attribute")
	void bindings_overrideTheGeneratedUnit() throws Exception {
		push(reports(10.0f, 100.0f, 280.0f, 1000.0f, 1000.0f, base));

		assertEquals("kn", readUnit(GENERATED, "windSpeed"), "the binding's unit wins");
	}

	/**
	 * Builds a payload with one report, whose timestamp is what every timestamp mapping of the
	 * mapping resolves to.
	 */
	private WeatherReports reports(float windSpeed, float windDirection, float temperature, float visibility,
			float surfacePressure, Instant timestamp) {
		WeatherFactory factory = WeatherFactory.eINSTANCE;

		WeatherStation station = factory.createWeatherStation();
		station.setId(PROVIDER);
		station.setName("GERA");

		MOSMIXSWeatherReport report = factory.createMOSMIXSWeatherReport();
		report.setId("report-0");
		report.setTimestamp(Date.from(timestamp));
		report.setWeatherStation(station);
		report.setWindSpeed(windSpeed);
		report.setWindDirection(windDirection);
		report.setTempAboveSurface5(temperature);
		report.setVisibility(visibility);
		report.setSurfacePressure(surfacePressure);

		WeatherReports reports = factory.createWeatherReports();
		reports.setId("station-" + PROVIDER);
		reports.getReports().add(report);
		return reports;
	}

	private void push(WeatherReports reports) {
		assertEquals(1, instancePusher.pushInstance(reports), "the mapping should have been applied");
	}

	/**
	 * @return the value the twin currently holds for a resource, which is the last value its
	 * change rule accepted
	 */
	private Object read(String serviceMid, String resourceMid) throws Exception {
		return onGateway(resource -> resource.getValue().getValue().getValue(), serviceMid, resourceMid);
	}

	private Object readUnit(String serviceMid, String resourceMid) throws Exception {
		return onGateway(resource -> resource.getMetadataValue("unit").getValue().getValue(), serviceMid,
				resourceMid);
	}

	/**
	 * Runs a read against a resource of the pushed provider on the gateway thread, where all
	 * twin interaction has to happen.
	 */
	private Object onGateway(ResourceRead read, String serviceMid, String resourceMid) throws Exception {
		Promise<Object> result = gatewayThread.execute(new AbstractSensinactCommand<Object>() {

			@Override
			protected Promise<Object> call(SensinactDigitalTwin twin, SensinactModelManager modelManager,
					PromiseFactory pf) {
				try {
					SensinactProvider provider = twin.getProvider(MODEL, PROVIDER);
					assertNotNull(provider, "the push should have created provider " + PROVIDER);
					SensinactService service = provider.getServices().get(serviceMid);
					assertNotNull(service, "service " + serviceMid + " should exist");
					SensinactResource resource = service.getResources().get(resourceMid);
					assertNotNull(resource, "resource " + serviceMid + "." + resourceMid + " should exist");
					return pf.resolved(read.apply(resource));
				} catch (Exception e) {
					return pf.failed(e);
				}
			}
		});
		return result.getValue();
	}

	/**
	 * A read on a resource that may fail - {@link java.util.function.Function} cannot throw.
	 */
	private interface ResourceRead {
		Object apply(SensinactResource resource) throws Exception;
	}

}
