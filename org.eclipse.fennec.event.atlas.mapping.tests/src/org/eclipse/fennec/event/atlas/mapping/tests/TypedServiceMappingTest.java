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

import org.eclipse.emf.ecore.EAttribute;
import org.eclipse.emf.ecore.EClass;
import org.eclipse.emf.ecore.EDataType;
import org.eclipse.emf.ecore.EPackage;
import org.eclipse.emf.ecore.EStructuralFeature;
import org.eclipse.emf.ecore.EcoreFactory;
import org.eclipse.emf.ecore.EcorePackage;
import org.eclipse.fennec.event.atlas.mapping.InstancePusher;
import org.eclipse.fennec.event.atlas.mapping.ProviderMappingRegistry;
import org.eclipse.fennec.event.atlas.model.mapping.AdminMapping;
import org.eclipse.fennec.event.atlas.model.mapping.MappingFactory;
import org.eclipse.fennec.event.atlas.model.mapping.NameMapping;
import org.eclipse.fennec.event.atlas.model.mapping.ProviderMapping;
import org.eclipse.fennec.event.atlas.model.mapping.ResourceMapping;
import org.eclipse.fennec.event.atlas.model.mapping.ServiceMapping;
import org.eclipse.sensinact.core.command.AbstractSensinactCommand;
import org.eclipse.sensinact.core.command.GatewayThread;
import org.eclipse.sensinact.core.model.SensinactModelManager;
import org.eclipse.sensinact.core.twin.SensinactDigitalTwin;
import org.eclipse.sensinact.core.twin.SensinactProvider;
import org.eclipse.sensinact.core.twin.SensinactService;
import org.eclipse.sensinact.model.core.provider.ProviderPackage;
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
 * A service whose EClass the mapping names (#91 item 8).
 * <p>
 * The service is then not built from the resource mappings and is not in the model at all:
 * {@code getOrCreateService(name, EClass)} puts it on the provider, so it carries the resources
 * the EClass declares, with their types. Only a real gateway shows that - and that a frozen
 * model does not stop it.
 * <p>
 * The EClass here is built at run time, which is also what a model delivered as an
 * {@code .ecore} gives: a dynamic EClass. EMF answers a dynamic subclass of the generated
 * {@code Service} with the generated implementation, so the twin takes it.
 */
@ExtendWith(BundleContextExtension.class)
@ExtendWith(ServiceExtension.class)
public class TypedServiceMappingTest {

	private static final String MODEL = "typed-service-reports";
	private static final String PROVIDER = "gera-typed";
	private static final String SERVICE = "thermal";

	private final WeatherPackage weather = WeatherFactory.eINSTANCE.getWeatherPackage();

	private final EPackage ePackage = EcoreFactory.eINSTANCE.createEPackage();
	private final EClass thermal = EcoreFactory.eINSTANCE.createEClass();

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
	@DisplayName("A service named by its EClass carries that EClass's resources, and takes the values")
	void typedService_carriesTheEClassResources() throws Exception {
		mappingRegistry.registerModelMapping(mapping());

		assertEquals(1, instancePusher.pushInstance(reports()), "the mapping should have applied");

		inTwin((twin, modelManager) -> {
			SensinactProvider provider = twin.getProvider(MODEL, PROVIDER);
			assertNotNull(provider, "the provider should exist");

			SensinactService service = provider.getServices().get(SERVICE);
			assertNotNull(service, "the typed service should be on the provider");
			assertTrue(service.getResources().keySet().containsAll(java.util.Set.of("windSpeed", "station")),
					"its resources come from the EClass, but it has " + service.getResources().keySet());

			assertEquals(5.0f, service.getResources().get("windSpeed").getValue().getValue().getValue(),
					"the payload's value should have reached the typed resource");
			assertEquals("10567", service.getResources().get("station").getValue().getValue().getValue(),
					"and so should the second one");
		});
	}

	@Test
	@DisplayName("A typed service is reachable through the twin's own resource lookup too")
	void typedService_isReachableThroughTheTwin() throws Exception {
		mappingRegistry.registerModelMapping(mapping());
		instancePusher.pushInstance(reports());

		inTwin((twin, modelManager) -> assertEquals(5.0f,
				twin.getResource(PROVIDER, SERVICE, "windSpeed").getValue().getValue().getValue(),
				"twin.getResource should find the typed resource"));
	}

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

	/** The service EClass a deployed model would carry: two resources, with their types. */
	private EClass serviceEClass() {
		ePackage.setName("thermal");
		ePackage.setNsPrefix("thermal");
		ePackage.setNsURI("http://example.org/event.atlas/thermal/1.0");
		thermal.setName("Thermal");
		thermal.getESuperTypes().add(ProviderPackage.Literals.SERVICE);
		thermal.getEStructuralFeatures().add(attribute("windSpeed", EcorePackage.Literals.EFLOAT));
		thermal.getEStructuralFeatures().add(attribute("station", EcorePackage.Literals.ESTRING));
		ePackage.getEClassifiers().add(thermal);
		return thermal;
	}

	private ProviderMapping mapping() {
		ServiceMapping service = MappingFactory.eINSTANCE.createServiceMapping();
		service.setMid(SERVICE);
		service.setName(name("Thermal"));
		service.setServiceEClass(serviceEClass());
		service.getResources().add(resource("windSpeed", EcorePackage.Literals.EFLOAT,
				weather.getWeatherReports_Reports(), weather.getMOSMIXSWeatherReport_WindSpeed()));
		service.getResources().add(resource("station", EcorePackage.Literals.ESTRING,
				weather.getWeatherReports_Reports(), weather.getWeatherReport_WeatherStation(),
				weather.getWeatherStation_Id()));

		AdminMapping admin = MappingFactory.eINSTANCE.createAdminMapping();
		admin.setMid("admin");
		admin.setName(name("Admin"));

		ProviderMapping mapping = MappingFactory.eINSTANCE.createProviderMapping();
		mapping.setMid(MODEL);
		mapping.setName(name(PROVIDER));
		mapping.setAdmin(admin);
		mapping.getProviderClasses().add(weather.getWeatherReports());
		mapping.getServices().add(service);
		return mapping;
	}

	private ResourceMapping resource(String mid, EDataType type, EStructuralFeature... path) {
		ResourceMapping resource = MappingFactory.eINSTANCE.createResourceMapping();
		resource.setMid(mid);
		resource.setEType(type);
		for (EStructuralFeature feature : path) {
			resource.getValueFeature().add(feature);
		}
		resource.setTimestamp(MappingFactory.eINSTANCE.createTimestampMapping());
		return resource;
	}

	private NameMapping name(String value) {
		NameMapping name = MappingFactory.eINSTANCE.createNameMapping();
		name.setName(value);
		return name;
	}

	private static EAttribute attribute(String name, EDataType type) {
		EAttribute attribute = EcoreFactory.eINSTANCE.createEAttribute();
		attribute.setName(name);
		attribute.setEType(type);
		return attribute;
	}

	private WeatherReports reports() {
		WeatherFactory factory = WeatherFactory.eINSTANCE;

		WeatherStation station = factory.createWeatherStation();
		station.setId("10567");
		station.setName("GERA");

		MOSMIXSWeatherReport report = factory.createMOSMIXSWeatherReport();
		report.setId("r1");
		report.setWeatherStation(station);
		report.setWindSpeed(5.0f);

		WeatherReports reports = factory.createWeatherReports();
		reports.setId("station-10567");
		reports.getReports().add(report);
		return reports;
	}

	@FunctionalInterface
	private interface TwinAssertion {
		void check(SensinactDigitalTwin twin, SensinactModelManager modelManager) throws Exception;
	}
}
