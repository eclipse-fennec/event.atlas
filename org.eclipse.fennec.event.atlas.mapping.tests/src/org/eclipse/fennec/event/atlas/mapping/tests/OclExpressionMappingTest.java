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
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Date;
import java.util.Optional;

import org.eclipse.emf.ecore.EcorePackage;
import org.eclipse.fennec.emf.osgi.metadata.MetadataService;
import org.eclipse.fennec.emf.osgi.model.metadata.PackageMetadata;
import org.eclipse.fennec.event.atlas.mapping.ExpressionEvaluator;
import org.eclipse.fennec.event.atlas.mapping.InstancePusher;
import org.eclipse.fennec.event.atlas.mapping.ProviderMappingRegistry;
import org.eclipse.fennec.event.atlas.model.mapping.AdminMapping;
import org.eclipse.fennec.event.atlas.model.mapping.MappingFactory;
import org.eclipse.fennec.event.atlas.model.mapping.NameMapping;
import org.eclipse.fennec.event.atlas.model.mapping.ProviderMapping;
import org.eclipse.fennec.event.atlas.model.mapping.ResourceMapping;
import org.eclipse.fennec.event.atlas.model.mapping.ServiceMapping;
import org.eclipse.fennec.event.atlas.model.mapping.ValueMapping;
import org.eclipse.sensinact.core.command.AbstractSensinactCommand;
import org.eclipse.sensinact.core.command.GatewayThread;
import org.eclipse.sensinact.core.model.SensinactModelManager;
import org.eclipse.sensinact.core.twin.SensinactDigitalTwin;
import org.eclipse.sensinact.core.twin.SensinactProvider;
import org.eclipse.sensinact.core.twin.SensinactService;
import org.eclipse.sensinact.core.twin.TimedValue;
import org.gecko.weather.model.weather.MOSMIXSWeatherReport;
import org.gecko.weather.model.weather.WeatherFactory;
import org.gecko.weather.model.weather.WeatherReports;
import org.gecko.weather.model.weather.WeatherStation;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.osgi.test.common.annotation.InjectService;
import org.osgi.test.junit5.context.BundleContextExtension;
import org.osgi.test.junit5.service.ServiceExtension;
import org.osgi.util.promise.Promise;
import org.osgi.util.promise.PromiseFactory;

/**
 * An OCL expression on the way into the twin, with the real engine behind it.
 * <p>
 * This is where the wiring is proven: the mapping bundle declares the
 * {@link ExpressionEvaluator} port and nothing more, and the value only appears because the OCL
 * bundle is deployed and its service reached {@code InstancePusherImpl}. It also shows what an
 * expression is for - a count and an arithmetic combination, neither of which a feature path can
 * name.
 */
@ExtendWith(BundleContextExtension.class)
@ExtendWith(ServiceExtension.class)
public class OclExpressionMappingTest {

	private static final String MODEL = "ocl-weather-reports";

	@InjectService
	GatewayThread gatewayThread;

	@InjectService
	ProviderMappingRegistry mappingRegistry;

	@InjectService
	InstancePusher instancePusher;

	/** Deployed by …event.atlas.mapping.ocl; without that bundle there is no evaluator at all. */
	@InjectService
	ExpressionEvaluator expressionEvaluator;

	/** The evaluator keys its parse failures by model fingerprint, which this service answers. */
	@InjectService
	MetadataService metadataService;

	@AfterEach
	void tearDown() {
		mappingRegistry.dispose();
	}

	@Test
	@DisplayName("An expression computes a resource value the feature path cannot reach")
	void expression_producesMappedValues() throws Exception {
		mappingRegistry.registerModelMapping(mapping());

		int applied = instancePusher.pushInstance(weatherReports());
		assertEquals(1, applied, "Exactly one mapping should have been applied");

		Promise<Boolean> result = gatewayThread.execute(new AbstractSensinactCommand<Boolean>() {
			@Override
			protected Promise<Boolean> call(SensinactDigitalTwin twin, SensinactModelManager modelManager,
					PromiseFactory pf) {
				try {
					// The provider id comes from an expression on the name mapping.
					SensinactProvider provider = twin.getProvider(MODEL, "10567");
					assertNotNull(provider, "the name expression should have named the provider");

					SensinactService stats = provider.getServices().get("stats");
					assertNotNull(stats, "stats service should exist");

					TimedValue<?> count = stats.getResources().get("reportCount").getValue().getValue();
					assertEquals(2, count.getValue(), "self.reports->size()");

					TimedValue<?> doubled = stats.getResources().get("doubleWindSpeed").getValue().getValue();
					assertEquals(10.0d, doubled.getValue(), "the first report's windSpeed of 5.0, doubled");

					return pf.resolved(true);
				} catch (Exception e) {
					return pf.failed(e);
				}
			}
		});
		assertTrue(result.getValue(), "Twin verification should succeed");
	}

	@Test
	@DisplayName("A deployed payload model is described by the metadata service and reports a fingerprint")
	void payloadPackage_reportsAFingerprint() {
		// The precondition of the parse cache, in a real framework: the unit tests mock the
		// fingerprint, and what the cache then does with it is pinned by ExpressionCacheKeyTest.
		Optional<PackageMetadata> metadata = metadataService
				.getPackageMetadata(WeatherFactory.eINSTANCE.getWeatherPackage());

		assertTrue(metadata.isPresent(), "the weather model should be known to the metadata service");
		String fingerprint = metadata.get().getModelFingerprint();
		assertNotNull(fingerprint, "a model version without a fingerprint cannot key the cache");
		assertTrue(fingerprint.startsWith("fp"), () -> "expected a scheme tag, got " + fingerprint);
	}

	private ProviderMapping mapping() {
		NameMapping name = MappingFactory.eINSTANCE.createNameMapping();
		name.setExpression("self.reports->first().weatherStation.id");

		ServiceMapping stats = MappingFactory.eINSTANCE.createServiceMapping();
		stats.setMid("stats");
		stats.getResources().add(resource("reportCount", EcorePackage.Literals.EINT, "self.reports->size()"));
		stats.getResources().add(resource("doubleWindSpeed", EcorePackage.Literals.EDOUBLE,
				"self.reports->first().windSpeed * 2"));

		NameMapping adminName = MappingFactory.eINSTANCE.createNameMapping();
		adminName.setName("Admin");
		AdminMapping admin = MappingFactory.eINSTANCE.createAdminMapping();
		admin.setMid("admin");
		admin.setName(adminName);

		ProviderMapping mapping = MappingFactory.eINSTANCE.createProviderMapping();
		mapping.setMid(MODEL);
		mapping.setName(name);
		mapping.setAdmin(admin);
		mapping.getProviderClasses().add(WeatherFactory.eINSTANCE.getWeatherPackage().getWeatherReports());
		mapping.getServices().add(stats);
		return mapping;
	}

	private ResourceMapping resource(String mid, org.eclipse.emf.ecore.EDataType type, String expression) {
		ValueMapping valueMapping = MappingFactory.eINSTANCE.createValueMapping();
		valueMapping.setExpression(expression);

		ResourceMapping resource = MappingFactory.eINSTANCE.createResourceMapping();
		resource.setMid(mid);
		resource.setName(mid);
		resource.setEType(type);
		resource.setValueMapping(valueMapping);
		return resource;
	}

	private WeatherReports weatherReports() {
		WeatherFactory factory = WeatherFactory.eINSTANCE;

		WeatherStation weatherStation = factory.createWeatherStation();
		weatherStation.setId("10567");
		weatherStation.setName("GERA");

		WeatherReports weatherReports = factory.createWeatherReports();
		weatherReports.setId("station-10567");
		weatherReports.getReports().add(report(weatherStation, 5.0f));
		weatherReports.getReports().add(report(weatherStation, 7.5f));
		return weatherReports;
	}

	private MOSMIXSWeatherReport report(WeatherStation station, float windSpeed) {
		MOSMIXSWeatherReport report = WeatherFactory.eINSTANCE.createMOSMIXSWeatherReport();
		report.setTimestamp(new Date());
		report.setWeatherStation(station);
		report.setWindSpeed(windSpeed);
		return report;
	}
}
