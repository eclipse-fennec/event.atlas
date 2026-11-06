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
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

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
import org.eclipse.sensinact.core.command.AbstractSensinactCommand;
import org.eclipse.sensinact.core.command.GatewayThread;
import org.eclipse.sensinact.core.model.SensinactModelManager;
import org.eclipse.sensinact.core.twin.SensinactDigitalTwin;
import org.eclipse.sensinact.core.twin.SensinactProvider;
import org.eclipse.sensinact.core.twin.SensinactResource;
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
 * An admin resource sensiNact's admin service does not have, in a real framework (#91 item 8).
 * <p>
 * The admin's five resources are fixed, so the engine gives the provider an admin of an
 * {@code Admin} subclass that declares the extra one. Only a real gateway shows that the
 * exchange is accepted, that the value is then readable, and - the limit worth pinning - that
 * the snapshot consumers do not see it, because they build from what the model DECLARES.
 */
@ExtendWith(BundleContextExtension.class)
@ExtendWith(ServiceExtension.class)
public class AdminExtraResourceMappingTest {

	private static final String MODEL = "admin-extra-reports";
	private static final String PROVIDER = "gera-extra";
	private static final String EXTRA = "viewport";

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
	@DisplayName("An extra admin resource is written, and found by the instance-based lookup")
	void extraAdminResource_isWrittenAndRead() throws Exception {
		mappingRegistry.registerModelMapping(mapping(true));

		assertEquals(1, instancePusher.pushInstance(reports()), "the mapping should have applied");

		inTwin((twin, modelManager) -> {
			SensinactResource extra = twin.getResource(PROVIDER, "admin", EXTRA);
			assertNotNull(extra, "twin.getResource must find the extra admin resource");
			assertEquals("station-10567", extra.getValue().getValue().getValue(),
					"the payload's value must have reached it");

			// The five sensiNact resources must survive the exchange of the admin instance.
			SensinactResource friendlyName = twin.getResource(PROVIDER, "admin", "friendlyName");
			assertNotNull(friendlyName, "the admin must keep its own resources");
			assertEquals(PROVIDER, friendlyName.getValue().getValue().getValue(),
					"the friendly name must not be lost when the admin is retyped");
		});
	}

	@Test
	@DisplayName("The extra resource is invisible to the declaration-based lookups")
	void extraAdminResource_isNotInTheDeclaration() throws Exception {
		mappingRegistry.registerModelMapping(mapping(true));
		instancePusher.pushInstance(reports());

		inTwin((twin, modelManager) -> {
			SensinactProvider provider = twin.getProvider(MODEL, PROVIDER);
			assertNotNull(provider);
			assertFalse(provider.getServices().get("admin").getResources().containsKey(EXTRA),
					"the declared admin service cannot carry it");
			assertNull(twin.snapshotResource(PROVIDER, "admin", EXTRA),
					"a snapshot builds from the declaration, so SensorThings does not see it");
		});
	}

	@Test
	@DisplayName("Without extendAdmin the extra resource is skipped and the admin stays as it is")
	void withoutExtendAdmin_theResourceIsSkipped() throws Exception {
		mappingRegistry.registerModelMapping(mapping(false));
		instancePusher.pushInstance(reports());

		inTwin((twin, modelManager) -> {
			assertNull(twin.getResource(PROVIDER, "admin", EXTRA),
					"nothing may be added to the admin without extendAdmin");
			SensinactProvider provider = twin.getProvider(MODEL, PROVIDER);
			assertTrue(provider.getServices().get("admin").getResources().containsKey("friendlyName"),
					"and the admin must still work");
		});
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

	private ProviderMapping mapping(boolean extendAdmin) {
		ReferenceMapping referenced = MappingFactory.eINSTANCE.createReferenceMapping();
		referenced.getFeaturePath().add(weather.getWeatherReports_Reports());
		referenced.setExclude(false);
		referenced.setCollectionIndex(0);

		ServiceMapping report = MappingFactory.eINSTANCE.createServiceMapping();
		report.setMid("report");
		report.setName(name("Report"));
		report.setReferencedResource(referenced);
		report.getResources().add(resource("windSpeed", EcorePackage.Literals.EFLOAT,
				weather.getMOSMIXSWeatherReport_WindSpeed()));

		AdminMapping admin = MappingFactory.eINSTANCE.createAdminMapping();
		admin.setMid("admin");
		admin.setName(name("Admin"));
		admin.setExtendAdmin(extendAdmin);
		// The extra one: a string the payload carries, like TrafiCam's viewport polygon.
		admin.getResources().add(resource(EXTRA, EcorePackage.Literals.ESTRING,
				weather.getWeatherReports_Id()));

		ProviderMapping mapping = MappingFactory.eINSTANCE.createProviderMapping();
		mapping.setMid(MODEL);
		mapping.setName(name(PROVIDER));
		mapping.setAdmin(admin);
		mapping.getProviderClasses().add(weather.getWeatherReports());
		mapping.getServices().add(report);
		return mapping;
	}

	private ResourceMapping resource(String mid, EDataType type, EStructuralFeature path) {
		ResourceMapping resource = MappingFactory.eINSTANCE.createResourceMapping();
		resource.setMid(mid);
		resource.setEType(type);
		resource.getValueFeature().add(path);
		resource.setTimestamp(MappingFactory.eINSTANCE.createTimestampMapping());
		return resource;
	}

	private NameMapping name(String value) {
		NameMapping name = MappingFactory.eINSTANCE.createNameMapping();
		name.setName(value);
		return name;
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
