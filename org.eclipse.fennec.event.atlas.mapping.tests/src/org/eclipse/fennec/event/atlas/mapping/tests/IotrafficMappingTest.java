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

import java.io.ByteArrayInputStream;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.Hashtable;
import java.util.Map;

import org.eclipse.emf.common.util.URI;
import org.eclipse.emf.ecore.EClass;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.EPackage;
import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.emf.ecore.resource.ResourceSet;
import org.eclipse.emf.ecore.util.EcoreUtil;
import org.eclipse.fennec.event.atlas.mapping.MappingProfileRegistry;
import org.eclipse.fennec.event.atlas.mapping.ProviderMappingRegistry;
import org.eclipse.fennec.event.atlas.model.mapping.MappingProfile;
import org.eclipse.fennec.event.atlas.model.mapping.ProviderMapping;
import org.eclipse.fennec.event.atlas.southbound.common.IngestResult;
import org.eclipse.fennec.event.atlas.southbound.common.IngestResult.Outcome;
import org.eclipse.fennec.event.atlas.southbound.common.PayloadIngest;
import org.eclipse.sensinact.core.command.AbstractSensinactCommand;
import org.eclipse.sensinact.core.command.GatewayThread;
import org.eclipse.sensinact.core.model.SensinactModelManager;
import org.eclipse.sensinact.core.twin.SensinactDigitalTwin;
import org.eclipse.sensinact.core.twin.SensinactProvider;
import org.eclipse.sensinact.core.twin.SensinactService;
import org.eclipse.sensinact.core.twin.TimedValue;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceRegistration;
import org.osgi.test.common.annotation.InjectBundleContext;
import org.osgi.test.common.annotation.InjectService;
import org.osgi.test.junit5.context.BundleContextExtension;
import org.osgi.test.junit5.service.ServiceExtension;
import org.osgi.util.promise.Promise;
import org.osgi.util.promise.PromiseFactory;

/**
 * The iotec iotraffic camera: a line-crossing counter whose payload shape is the one a
 * positional mapping cannot read. Its lines arrive as a JSON object keyed {@code line_1},
 * {@code line_2}, ... in no fixed order, and a reporting window is split over several
 * uplinks of at most four lines each, so every uplink carries a different subset.
 * <p>
 * A service per line, each selecting its line with a {@code collectionFilter}, is what makes
 * that readable: a service whose line is not in this uplink is skipped, the lines that are
 * there are updated, and the ones an earlier uplink delivered keep their values. The model is
 * registered dynamically from its {@code .ecore}, the way a Model Atlas delivers one, so the
 * map entries are the dynamic kind.
 */
@ExtendWith(BundleContextExtension.class)
@ExtendWith(ServiceExtension.class)
public class IotrafficMappingTest {

	private static final String MODEL_URI = "/model/iotraffic.ecore";
	private static final String PROFILE_URI = "/data/iotraffic/iotraffic-camera-profile.xmi";
	private static final String COUNTER_MAPPING_URI = "/data/iotraffic/IotrafficCounterMapping.xmi";
	private static final String STATUS_MAPPING_URI = "/data/iotraffic/IotrafficStatusMapping.xmi";

	private static final String NS_URI = "https://eclipse.org/fennec/lorawan/iotraffic";
	private static final String PROVIDER_MODEL = "iotraffic-camera";
	private static final String PROVIDER_ID = "37a151dd238fb2e7";
	private static final String TOPIC = "iotraffic/jena/uplink";

	@InjectBundleContext
	BundleContext bundleContext;
	@InjectService
	GatewayThread gatewayThread;
	@InjectService
	ProviderMappingRegistry mappingRegistry;
	@InjectService
	MappingProfileRegistry profileRegistry;
	@InjectService
	PayloadIngest payloadIngest;
	@InjectService
	ResourceSet resourceSet;

	private ServiceRegistration<EPackage> modelRegistration;

	@BeforeEach
	void setUp() throws Exception {
		registerModel();
		profileRegistry.registerProfile(load(PROFILE_URI, MappingProfile.class));
		mappingRegistry.registerModelMapping(load(COUNTER_MAPPING_URI, ProviderMapping.class));
		mappingRegistry.registerModelMapping(load(STATUS_MAPPING_URI, ProviderMapping.class));
	}

	@AfterEach
	void tearDown() {
		mappingRegistry.dispose();
		profileRegistry.dispose();
		if (modelRegistration != null) {
			modelRegistration.unregister();
		}
	}

	@Test
	@DisplayName("Every uplink fills the lines it carries and leaves the others standing")
	void splitUplinks_fillTheLinesTheyCarry() throws Exception {
		assertEquals(Outcome.APPLIED, ingest("counter-lines-1-4.json").outcome());

		assertEquals(5, count("line1", "person"));
		assertEquals(2, count("line1", "bicycle"));
		assertEquals(12, count("line3", "person"));

		// The next uplink of the same window carries lines 5 to 8 and nothing of line 1.
		assertEquals(Outcome.APPLIED, ingest("counter-lines-5-8.json").outcome());
		assertEquals(17, count("line7", "car"));
		assertEquals(5, count("line1", "person"), "a line this uplink does not carry keeps its value");

		assertEquals(Outcome.APPLIED, ingest("counter-line-9.json").outcome());
		assertEquals(20, count("line9", "person"));
		assertEquals(30, count("line8", "total"), "and so does one from the uplink before");
	}

	@Test
	@DisplayName("The reporting window and the camera's identity come off the same uplink")
	void counterUplink_reportsItsWindowAndAdmin() throws Exception {
		ingest("counter-lines-1-4.json");

		assertEquals("21:36", value("window", "from"));
		assertEquals("22:56", value("window", "to"));
		assertEquals("iotraffic Camera Test", value("admin", "friendlyName"));
	}

	@Test
	@DisplayName("The health report of the same camera lands on the same provider")
	// Two message types, two mappings, one camera: the UNIFIED profile is what makes the
	// second mapping contribute a service instead of creating a second provider.
	void statusUplink_landsOnTheSameProvider() throws Exception {
		ingest("counter-lines-1-4.json");

		assertEquals(Outcome.APPLIED, ingest("ai-module-status.json").outcome());

		assertEquals(55, count("status", "cpu_temp_c"));
		assertEquals(99, count("status", "gpu_usage_pct"));
		assertEquals(5, count("line1", "person"), "the counting services are untouched by it");
	}

	@Test
	@DisplayName("One channel carries both message types, and each is read as its own type")
	// The camera publishes counting reports and health reports on one topic, so the root type
	// a channel declares can only be the fallback: the message_type discriminator decides.
	void bothMessageTypes_areTypedFromOneChannel() throws Exception {
		assertEquals("CounterUplink", loadWithRootType("counter-lines-1-4.json").eClass().getName());
		assertEquals("StatusUplink", loadWithRootType("ai-module-status.json").eClass().getName());
	}

	@Test
	@DisplayName("A payload the discriminator cannot place is read as the channel's root type")
	void payloadWithoutDiscriminator_fallsBackToTheChannelsRootType() throws Exception {
		byte[] payload = new String(payload("counter-lines-1-4.json"), StandardCharsets.UTF_8)
				.replace("\"message_type\": \"counter_data\",", "").getBytes(StandardCharsets.UTF_8);

		EObject root = load(payload, counterUplink());

		assertEquals("CounterUplink", root.eClass().getName());
	}

	private IngestResult ingest(String payload) throws Exception {
		return payloadIngest.ingest(payload(payload), PayloadIngest.FORMAT_JSON, TOPIC);
	}

	private int count(String service, String resource) throws Exception {
		return ((Number) value(service, resource)).intValue();
	}

	private Object value(String serviceId, String resourceId) throws Exception {
		Promise<Object> value = gatewayThread.execute(new AbstractSensinactCommand<Object>() {
			@Override
			protected Promise<Object> call(SensinactDigitalTwin twin, SensinactModelManager modelManager,
					PromiseFactory pf) {
				try {
					SensinactProvider provider = twin.getProvider(PROVIDER_MODEL, PROVIDER_ID);
					assertNotNull(provider, "the uplink must have created the camera's provider");
					SensinactService service = provider.getServices().get(serviceId);
					assertNotNull(service, "no service " + serviceId + " in " + provider.getServices().keySet());
					TimedValue<?> timed = service.getResources().get(resourceId).getValue().getValue();
					return pf.resolved(timed.getValue());
				} catch (Exception e) {
					return pf.failed(e);
				}
			}
		});
		return value.getValue();
	}

	/** Reads a payload the way the ingest does for a channel that declares its root type. */
	private EObject loadWithRootType(String payload) throws Exception {
		return load(payload(payload), counterUplink());
	}

	private EClass counterUplink() {
		return (EClass) resourceSet.getPackageRegistry().getEPackage(NS_URI).getEClassifier("CounterUplink");
	}

	private EObject load(byte[] payload, EClass rootType) throws Exception {
		Resource resource = resourceSet
				.createResource(URI.createURI("iotraffic-test-" + System.nanoTime() + ".json"));
		resource.load(new ByteArrayInputStream(payload), Map.of("CODEC_ROOT_TYPE", rootType));
		assertTrue(!resource.getContents().isEmpty(), "the payload could not be typed: " + resource.getErrors());
		return resource.getContents().get(0);
	}

	private byte[] payload(String name) throws Exception {
		try (var stream = getClass().getResourceAsStream("/data/iotraffic/" + name)) {
			return stream.readAllBytes();
		}
	}

	private <T> T load(String resourceUri, Class<T> type) throws Exception {
		URL url = getClass().getResource(resourceUri);
		assertNotNull(url, "missing test resource " + resourceUri);
		Resource resource = resourceSet.createResource(URI.createURI(url.toString()));
		resource.load(null);
		EcoreUtil.resolveAll(resource);
		return type.cast(resource.getContents().get(0));
	}

	/** Registers the camera's model the way a Model Atlas would: dynamically, from its .ecore. */
	private void registerModel() throws Exception {
		if (resourceSet.getPackageRegistry().containsKey(NS_URI)) {
			return;
		}
		URL url = getClass().getResource(MODEL_URI);
		assertNotNull(url, "missing test resource " + MODEL_URI);
		Resource resource = resourceSet.createResource(URI.createURI(url.toString()));
		resource.load(null);
		EPackage ePackage = (EPackage) resource.getContents().get(0);
		EcoreUtil.resolveAll(ePackage);
		// The payloads are deserialized in resource sets this test never sees, so the model has
		// to be a service - the same way a Model Atlas publishes one.
		modelRegistration = bundleContext.registerService(EPackage.class, ePackage,
				new Hashtable<>(Map.of("emf.name", ePackage.getName(), "emf.nsUri", NS_URI)));
		// This resource set was created before that, and its registry does not pick a package up
		// after the fact - the mapping documents loaded here would keep unresolved proxies.
		resourceSet.getPackageRegistry().put(NS_URI, ePackage);
	}
}
