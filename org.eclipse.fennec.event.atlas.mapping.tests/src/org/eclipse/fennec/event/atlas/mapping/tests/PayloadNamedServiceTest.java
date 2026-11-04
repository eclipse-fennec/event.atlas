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
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Date;

import org.eclipse.emf.ecore.EDataType;
import org.eclipse.emf.ecore.EStructuralFeature;
import org.eclipse.emf.ecore.EcorePackage;
import org.eclipse.fennec.event.atlas.mapping.InstancePusher;
import org.eclipse.fennec.event.atlas.mapping.ProviderMappingRegistry;
import org.eclipse.fennec.event.atlas.model.mapping.AdminMapping;
import org.eclipse.fennec.event.atlas.model.mapping.MappingFactory;
import org.eclipse.fennec.event.atlas.model.mapping.NameMapping;
import org.eclipse.fennec.event.atlas.model.mapping.ProviderMapping;
import org.eclipse.fennec.event.atlas.model.mapping.ReferenceMapping;
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
import org.gecko.weather.model.weather.WeatherPackage;
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
 * A service named by its payload, in a real framework.
 * <p>
 * This is where the push-time model growth is proven. Registration builds the model from the
 * mids, so a service whose name a payload decides is not in it; the engine adds it when the
 * first payload names it, and the provider - which already exists - shows it. The unit tests
 * mock the model, so only this one shows that the gateway accepts the sequence.
 */
@ExtendWith(BundleContextExtension.class)
@ExtendWith(ServiceExtension.class)
public class PayloadNamedServiceTest {

	private static final String MODEL = "payload-named-reports";

	private final WeatherPackage weather = WeatherFactory.eINSTANCE.getWeatherPackage();

	@InjectService
	GatewayThread gatewayThread;

	@InjectService
	ProviderMappingRegistry mappingRegistry;

	@InjectService
	InstancePusher instancePusher;

	@AfterEach
	void tearDown() {
		mappingRegistry.dispose();
	}

	@Test
	@DisplayName("One service block makes one service for each element, named from the payload")
	void producedName_makesOneServicePerElement() throws Exception {
		mappingRegistry.registerModelMapping(mapping(producedName(), 0));

		assertEquals(1, instancePusher.pushInstance(reports()), "the mapping should have applied");

		inTwin((twin, modelManager) -> {
			SensinactProvider provider = twin.getProvider(MODEL, "gera");
			assertNotNull(provider, "the provider should exist");

			// Neither service is in the model at registration: the mids give one service
			// called "report", and these two were named by the payload.
			assertEquals(5.0f, windSpeed(provider, "r1"), "the first report reached its own service");
			assertEquals(7.5f, windSpeed(provider, "r2"), "the second report reached its own service");
		});
	}

	@Test
	@DisplayName("A second payload adds the services it carries to the provider that exists")
	void laterPayload_addsMoreServices() throws Exception {
		mappingRegistry.registerModelMapping(mapping(producedName(), 0));
		instancePusher.pushInstance(reports());

		WeatherReports second = reports();
		second.getReports().get(0).setId("r3");
		second.getReports().get(1).setId("r4");
		assertEquals(1, instancePusher.pushInstance(second));

		inTwin((twin, modelManager) -> {
			SensinactProvider provider = twin.getProvider(MODEL, "gera");
			assertNotNull(provider);
			assertTrue(provider.getServices().keySet().containsAll(java.util.Set.of("r1", "r2", "r3", "r4")),
					"a model that grew once keeps growing: " + provider.getServices().keySet());
		});
	}

	@Test
	@DisplayName("A constant name leaves the mid naming the service, and selects one element")
	void constantName_staysOneServiceNamedByTheMid() throws Exception {
		NameMapping constant = MappingFactory.eINSTANCE.createNameMapping();
		constant.setName("Report");
		mappingRegistry.registerModelMapping(mapping(constant, 0));

		assertEquals(1, instancePusher.pushInstance(reports()));

		inTwin((twin, modelManager) -> {
			SensinactProvider provider = twin.getProvider(MODEL, "gera");
			assertNotNull(provider);

			// The guard for every mapping written before this feature: the label does not
			// rename the service, and the collection still yields a single element.
			assertEquals(5.0f, windSpeed(provider, "report"), "the mid names the service");
			assertNull(provider.getServices().get("r1"), "no service should be named from the payload");
			assertNull(provider.getServices().get("Report"), "the constant is a label, not a name");
		});
	}

	@Test
	@DisplayName("A non-zero collectionIndex selects one element even beside a produced name")
	void nonZeroIndex_selectsOneElement() throws Exception {
		mappingRegistry.registerModelMapping(mapping(producedName(), 1));

		assertEquals(1, instancePusher.pushInstance(reports()));

		inTwin((twin, modelManager) -> {
			SensinactProvider provider = twin.getProvider(MODEL, "gera");
			assertNotNull(provider);
			assertEquals(7.5f, windSpeed(provider, "r2"), "only the element at index 1");
			assertNull(provider.getServices().get("r1"), "index 1 was asked for, so only one service");
		});
	}

	private Object windSpeed(SensinactProvider provider, String serviceName) throws Exception {
		SensinactService service = provider.getServices().get(serviceName);
		assertNotNull(service, "no service '" + serviceName + "' - have " + provider.getServices().keySet());
		TimedValue<?> value = service.getResources().get("windSpeed").getValue().getValue();
		return value.getValue();
	}

	private NameMapping producedName() {
		NameMapping name = MappingFactory.eINSTANCE.createNameMapping();
		name.getFeaturePath().add(weather.getWeatherReport_Id());
		return name;
	}

	private ProviderMapping mapping(NameMapping serviceName, int collectionIndex) {
		// A selector only: exclude=false with no filter generates no resources, so the service
		// carries exactly the one written below.
		ReferenceMapping referenced = MappingFactory.eINSTANCE.createReferenceMapping();
		referenced.getFeaturePath().add(weather.getWeatherReports_Reports());
		referenced.setExclude(false);
		referenced.setCollectionIndex(collectionIndex);

		ServiceMapping report = MappingFactory.eINSTANCE.createServiceMapping();
		report.setMid("report");
		report.setName(serviceName);
		report.setReferencedResource(referenced);
		report.getResources().add(resource("windSpeed", EcorePackage.Literals.EFLOAT,
				weather.getMOSMIXSWeatherReport_WindSpeed()));

		NameMapping adminName = MappingFactory.eINSTANCE.createNameMapping();
		adminName.setName("Admin");
		AdminMapping admin = MappingFactory.eINSTANCE.createAdminMapping();
		admin.setMid("admin");
		admin.setName(adminName);

		NameMapping providerName = MappingFactory.eINSTANCE.createNameMapping();
		providerName.setName("gera");

		ProviderMapping mapping = MappingFactory.eINSTANCE.createProviderMapping();
		mapping.setMid(MODEL);
		mapping.setName(providerName);
		mapping.setAdmin(admin);
		mapping.getProviderClasses().add(weather.getWeatherReports());
		mapping.getServices().add(report);
		return mapping;
	}

	private ResourceMapping resource(String mid, EDataType type, EStructuralFeature path) {
		ValueMapping valueMapping = MappingFactory.eINSTANCE.createValueMapping();
		valueMapping.getFeaturePath().add(path);

		ResourceMapping resource = MappingFactory.eINSTANCE.createResourceMapping();
		resource.setMid(mid);
		resource.setName(mid);
		resource.setEType(type);
		resource.setValueMapping(valueMapping);
		return resource;
	}

	private WeatherReports reports() {
		WeatherFactory factory = WeatherFactory.eINSTANCE;

		WeatherStation station = factory.createWeatherStation();
		station.setId("10567");
		station.setName("GERA");

		WeatherReports reports = factory.createWeatherReports();
		reports.setId("station-10567");
		reports.getReports().add(report(station, "r1", 5.0f));
		reports.getReports().add(report(station, "r2", 7.5f));
		return reports;
	}

	private MOSMIXSWeatherReport report(WeatherStation station, String id, float windSpeed) {
		MOSMIXSWeatherReport report = WeatherFactory.eINSTANCE.createMOSMIXSWeatherReport();
		report.setId(id);
		report.setTimestamp(new Date());
		report.setWeatherStation(station);
		report.setWindSpeed(windSpeed);
		return report;
	}

	/** Runs the assertions on the gateway thread, where the twin may be read. */
	private void inTwin(TwinAssertion assertion) throws Exception {
		Promise<Boolean> result = gatewayThread.execute(new AbstractSensinactCommand<Boolean>() {
			@Override
			protected Promise<Boolean> call(SensinactDigitalTwin twin, SensinactModelManager modelManager,
					PromiseFactory pf) {
				try {
					assertion.check(twin, modelManager);
					return pf.resolved(true);
				} catch (Throwable e) {
					return pf.failed(e);
				}
			}
		});
		assertTrue(result.getValue(), "twin verification should succeed");
	}

	@FunctionalInterface
	private interface TwinAssertion {
		void check(SensinactDigitalTwin twin, SensinactModelManager modelManager) throws Exception;
	}
}
