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
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.net.URL;
import java.util.Date;
import java.util.List;
import java.util.Map;

import org.eclipse.emf.common.util.URI;
import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.emf.ecore.resource.ResourceSet;
import org.eclipse.emf.ecore.util.EcoreUtil;
import org.eclipse.fennec.emf.osgi.eobject.registry.EObjectRegistries;
import org.eclipse.fennec.emf.osgi.eobject.registry.EObjectRegistryListener;
import org.eclipse.fennec.emf.osgi.eobject.registry.EObjectRegistryWriter;
import org.eclipse.fennec.event.atlas.mapping.InstancePusher;
import org.eclipse.fennec.event.atlas.mapping.ProviderMappingRegistry;
import org.eclipse.fennec.event.atlas.model.mapping.ProviderMapping;
import org.eclipse.sensinact.core.command.AbstractSensinactCommand;
import org.eclipse.sensinact.core.command.GatewayThread;
import org.eclipse.sensinact.core.model.SensinactModelManager;
import org.eclipse.sensinact.core.twin.SensinactDigitalTwin;
import org.eclipse.sensinact.core.twin.SensinactProvider;
import org.eclipse.sensinact.core.twin.TimedValue;
import org.gecko.weather.model.weather.GeoPosition;
import org.gecko.weather.model.weather.MOSMIXSWeatherReport;
import org.gecko.weather.model.weather.Station;
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
 * Replacing a mapping under an unchanged key, which is how every content change of a
 * ProviderMapping arrives: a file provider re-reading its directory, a Model Atlas syncing an
 * edited object, an operator resolving a key collision. The registry key <em>is</em> the
 * {@code mid}, so both mappings name the same sensinact model - and the update used to end with
 * that model deleted, because the new mapping was registered before the old one was dropped and
 * the old one's {@code deleteModel} then hit the new one's model. Every payload afterwards was
 * routed to a mapping whose model no longer existed and failed with "Failed to map instance to
 * provider" until the runtime was restarted (issue #63).
 */
@ExtendWith(BundleContextExtension.class)
@ExtendWith(ServiceExtension.class)
public class MappingUpdateTest {

	private static final String MAPPING_URI = "/data/WeatherReportsProviderMapping.xmi";
	private static final String WEATHER_MODEL = "dwd-weather-reports";
	private static final String STATION_ID = "10567";
	private static final String SOURCE = "mapping-update-test";

	@InjectService
	GatewayThread gatewayThread;
	@InjectService
	ResourceSet resourceSet;
	@InjectService(filter = "(emf.eobject.registry.name=sensinact-mappings)")
	EObjectRegistryListener mappingListener;
	@InjectService
	ProviderMappingRegistry mappingRegistry;
	@InjectService
	InstancePusher instancePusher;

	private EObjectRegistryWriter writer;

	@BeforeEach
	void setUp() {
		writer = EObjectRegistries.createRegistry("sensinact-mappings");
		writer.getRegistry().addListener(mappingListener);
	}

	@AfterEach
	void tearDown() {
		writer.getRegistry().removeListener(mappingListener);
		mappingRegistry.dispose();
	}

	@Test
	@DisplayName("A mapping replaced under the same mid keeps ingesting into the twin")
	void replacedMapping_keepsIngestingIntoTheTwin() throws Exception {
		ProviderMapping mapping = loadWeatherMapping();
		writer.put(SOURCE, mapping.getMid(), mapping, Map.of());
		assertEquals(1, instancePusher.pushInstance(weatherReports(5.0f)),
				"precondition: the freshly registered mapping applies");

		ProviderMapping replacement = EcoreUtil.copy(mapping);
		writer.put(SOURCE, replacement.getMid(), replacement, Map.of());

		assertEquals(List.of(replacement), mappingRegistry.getProviderMapping(replacement.getProviderClasses().get(0)),
				"the replacement is what payloads are routed to");
		assertEquals(1, instancePusher.pushInstance(weatherReports(9.0f)),
				"and it must still find the model it was mapped onto");
		assertEquals(9.0f, currentWindSpeed(), "the value of the second push reaches the twin");
	}

	@Test
	@DisplayName("A service dropped by the replacement disappears from the model")
	void replacedMapping_dropsWhatTheEditRemoved() throws Exception {
		ProviderMapping mapping = loadWeatherMapping();
		writer.put(SOURCE, mapping.getMid(), mapping, Map.of());
		instancePusher.pushInstance(weatherReports(5.0f));
		assertTrue(modelServices().contains("forecast3H"), "precondition: both services are mapped");

		ProviderMapping withoutForecast = EcoreUtil.copy(mapping);
		withoutForecast.getServices().removeIf(service -> "forecast3H".equals(service.getMid()));
		writer.put(SOURCE, withoutForecast.getMid(), withoutForecast, Map.of());

		assertTrue(modelServices().contains("currentWeather"), "the service the replacement kept is still there");
		assertFalse(modelServices().contains("forecast3H"),
				"the model is rebuilt from the replacement, not merged onto the old one");
	}

	private ProviderMapping loadWeatherMapping() throws Exception {
		URL mappingUrl = getClass().getResource(MAPPING_URI);
		Resource mappingResource = resourceSet.createResource(URI.createURI(mappingUrl.toString()));
		mappingResource.load(null);
		return (ProviderMapping) mappingResource.getContents().get(0);
	}

	private Float currentWindSpeed() throws Exception {
		return onGateway((twin, modelManager) -> {
			SensinactProvider provider = twin.getProvider(WEATHER_MODEL, STATION_ID);
			assertNotNull(provider, "the push must have (re-)created the provider");
			TimedValue<?> windSpeed = provider.getServices().get("currentWeather").getResources().get("windSpeed")
					.getValue().getValue();
			return (Float) windSpeed.getValue();
		});
	}

	private List<String> modelServices() throws Exception {
		return onGateway((twin, modelManager) -> List.copyOf(modelManager.getModel(WEATHER_MODEL).getServices().keySet()));
	}

	private <T> T onGateway(TwinQuery<T> query) throws Exception {
		Promise<T> result = gatewayThread.execute(new AbstractSensinactCommand<T>() {
			@Override
			protected Promise<T> call(SensinactDigitalTwin twin, SensinactModelManager modelManager,
					PromiseFactory pf) {
				try {
					return pf.resolved(query.run(twin, modelManager));
				} catch (Exception e) {
					return pf.failed(e);
				}
			}
		});
		if (result.getFailure() != null) {
			throw new AssertionError("Reading the twin failed", result.getFailure());
		}
		return result.getValue();
	}

	private interface TwinQuery<T> {
		T run(SensinactDigitalTwin twin, SensinactModelManager modelManager) throws Exception;
	}

	private WeatherReports weatherReports(float windSpeed) {
		WeatherFactory factory = WeatherFactory.eINSTANCE;

		WeatherReports weatherReports = factory.createWeatherReports();
		weatherReports.setId("station-" + STATION_ID);

		WeatherStation weatherStation = factory.createWeatherStation();
		weatherStation.setId(STATION_ID);
		weatherStation.setName("GERA");
		GeoPosition location = factory.createGeoPosition();
		location.setLatitude(50.88);
		location.setLongitude(12.13);
		location.setElevation(311);
		weatherStation.setLocation(location);

		Station station = factory.createStation();
		station.setName("GERA");
		station.setLocation(EcoreUtil.copy(location));

		MOSMIXSWeatherReport current = factory.createMOSMIXSWeatherReport();
		current.setId("report-0");
		current.setTimestamp(new Date());
		current.setWeatherStation(weatherStation);
		current.setStation(station);
		current.setWindSpeed(windSpeed);
		weatherReports.getReports().add(current);

		MOSMIXSWeatherReport forecast = factory.createMOSMIXSWeatherReport();
		forecast.setId("report-1");
		forecast.setTimestamp(new Date(System.currentTimeMillis() + 10_800_000L));
		forecast.setWeatherStation(weatherStation);
		forecast.setStation(station);
		forecast.setWindSpeed(windSpeed + 2.5f);
		weatherReports.getReports().add(forecast);

		return weatherReports;
	}
}
