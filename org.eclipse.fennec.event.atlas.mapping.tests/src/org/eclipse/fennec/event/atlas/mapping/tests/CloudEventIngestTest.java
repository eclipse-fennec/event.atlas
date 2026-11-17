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
import static org.junit.jupiter.api.Assertions.assertSame;

import java.net.URL;
import java.util.Dictionary;
import java.util.Hashtable;
import java.util.Map;
import java.util.concurrent.atomic.AtomicReference;

import org.eclipse.emf.common.util.URI;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.emf.ecore.resource.ResourceSet;
import org.eclipse.fennec.event.atlas.mapping.InstancePusher;
import org.eclipse.fennec.event.atlas.mapping.ProviderMappingRegistry;
import org.eclipse.fennec.event.atlas.mapping.functions.ValueFunction;
import org.eclipse.fennec.event.atlas.model.mapping.ProviderMapping;
import org.eclipse.fennec.event.atlas.southbound.common.IngestResult;
import org.eclipse.fennec.event.atlas.southbound.common.IngestResult.Outcome;
import org.eclipse.fennec.event.atlas.southbound.common.PayloadIngest;
import org.eclipse.fennec.event.atlas.southbound.common.TransportContext;
import org.eclipse.sensinact.core.command.AbstractSensinactCommand;
import org.eclipse.sensinact.core.command.GatewayThread;
import org.eclipse.sensinact.core.model.SensinactModelManager;
import org.eclipse.sensinact.core.twin.SensinactDigitalTwin;
import org.eclipse.sensinact.core.twin.SensinactProvider;
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

import io.cloudevents.model.ce.CloudEvent;
import io.cloudevents.model.ce.CloudEventsFactory;

/**
 * The CloudEvents envelope in a real runtime (issue #96): the southbound ingest wraps every
 * payload it reads, the envelope reaches the mapping layer with the message's own source on
 * it, and an enveloped push applies exactly the mappings a bare push of the same payload
 * would.
 * <p>
 * Nothing in the engine reads the envelope yet - that is the point of the step, and it is why
 * the test needs a reader of its own. {@code dragino-envelope-mapping.xmi} carries a resource
 * produced by a {@link ValueFunction} with no feature path, so the function is handed the
 * payload itself and can walk to the envelope containing it; it stands in for the selector
 * that will read it for real.
 * <p>
 * It also keeps {@code io.cloudevents.model} in the runtime's bundle set: the mapping bundle
 * imports the package now, so a bndrun that stops providing it fails here rather than at the
 * first message of a deployment.
 * @author Ilenia Salvadori
 */
@ExtendWith(BundleContextExtension.class)
@ExtendWith(ServiceExtension.class)
public class CloudEventIngestTest {

	private static final String MAPPING_URI = "/data/envelope/dragino-envelope-mapping.xmi";
	private static final String SELECTED_MAPPING_URI = "/data/envelope/dragino-selected-mapping.xmi";
	private static final String SELECTED_MODEL = "dragino-selected";
	/** The selected mapping names its provider from the devEui, so it has an id of its own. */
	private static final String SELECTED_PROVIDER_ID = "a8404187d187106e";
	private static final String SITE_B_MAPPING_URI = "/data/envelope/dragino-site-b-mapping.xmi";
	private static final String SITE_B_MODEL = "dragino-site-b";
	private static final String SITE_B_PROVIDER_ID = "site-b-sensor";
	private static final String ROOTED_MAPPING_URI = "/data/envelope/envelope-rooted-mapping.xmi";
	private static final String ROOTED_MODEL = "envelope-rooted";
	private static final String ROOTED_PROVIDER_ID = "envelope-probe";
	private static final String PAYLOAD_URI = "/data/dragino-example.dragino";
	private static final String MODEL = "dragino-envelope";
	private static final String PROVIDER_ID = "9ffbc7be-916a-4597-9d64-0713a3d5e030";
	private static final String TOPIC = "a/b/c";
	private static final String DRAGINO_ECLASS = "https://eclipse.org/fennec/lorawan/dragino#//DraginoLSE01Uplink";

	@InjectBundleContext
	BundleContext bundleContext;

	@InjectService
	GatewayThread gatewayThread;

	@InjectService
	ProviderMappingRegistry mappingRegistry;

	@InjectService
	InstancePusher instancePusher;

	@InjectService
	PayloadIngest payloadIngest;

	@InjectService
	ResourceSet resourceSet;

	/** What the probe function saw, so a test can assert on the envelope the engine met. */
	private final AtomicReference<CloudEvent> seen = new AtomicReference<>();

	private ServiceRegistration<ValueFunction> probeRegistration;

	@BeforeEach
	void setUp() {
		Dictionary<String, Object> properties = new Hashtable<>();
		properties.put(ValueFunction.FUNCTION_ID, "test.envelope.probe");
		probeRegistration = bundleContext.registerService(ValueFunction.class, this::probe, properties);
	}

	@AfterEach
	void tearDown() {
		if (probeRegistration != null) {
			probeRegistration.unregister();
			probeRegistration = null;
		}
		seen.set(null);
		mappingRegistry.dispose();
	}

	@Test
	@DisplayName("A message ingested from 'a/b/c' reaches the mapping with that topic on its envelope")
	void ingest_carriesTheSourceAndTheSchemaToTheMappingLayer() throws Exception {
		mappingRegistry.registerModelMapping(loadMapping());

		IngestResult result = payloadIngest.ingest(payload(), PayloadIngest.FORMAT_XMI, TOPIC,
				new TransportContext("de.jena.test", Map.of("mqtt.broker", "test-broker")));

		assertEquals(Outcome.APPLIED, result.outcome());
		CloudEvent envelope = seen.get();
		assertNotNull(envelope, "the mapping engine never saw an envelope around the payload");
		assertEquals(TOPIC, envelope.getSource(), "the topic a message arrived on is its source");
		assertEquals(DRAGINO_ECLASS, envelope.getDataschema(),
				"dataschema names the EClass the payload was read as");
		assertEquals("de.jena.test", envelope.getType());
		assertEquals("test-broker", envelope.getExtensions().get("mqtt.broker"));
		// the engine navigated the payload, not the envelope: the mapping is payload-rooted
		assertEquals(3.301, batteryLevelInTwin(), 0.0001);
		assertEquals(TOPIC + " " + DRAGINO_ECLASS, sourceResourceInTwin());
	}

	@Test
	@DisplayName("The payload keeps the resource it was read from although the envelope now contains it")
	void ingest_payloadKeepsItsResource() throws Exception {
		mappingRegistry.registerModelMapping(loadMapping());

		payloadIngest.ingest(payload(), PayloadIngest.FORMAT_XMI, TOPIC);

		CloudEvent envelope = seen.get();
		assertNotNull(envelope);
		// `data` is a containment reference, so wrapping takes the payload out of the
		// resource's contents. An href inside a payload - and the package registry an
		// envelope-rooted expression will resolve a type name against - depend on it staying.
		assertNotNull(envelope.getData().eResource(), "the payload must keep a resource");
		assertSame(envelope.eResource(), envelope.getData().eResource());
		assertNotNull(envelope.getData().eResource().getResourceSet());
	}

	@Test
	@DisplayName("A hand-built envelope pushes its payload through the payload's own mappings")
	void pushInstance_withEnvelope_appliesThePayloadMappings() throws Exception {
		mappingRegistry.registerModelMapping(loadMapping());

		assertEquals(1, instancePusher.pushInstance(envelopeAround(loadPayload())),
				"the mapping registered for the payload's EClass must apply");
		assertEquals(3.301, batteryLevelInTwin(), 0.0001);
	}

	@Test
	@DisplayName("An envelope held in an EObject variable is pushed as an envelope, not as a payload")
	void pushInstance_withEnvelopeAsEObject_delegates() throws Exception {
		mappingRegistry.registerModelMapping(loadMapping());

		// Java picks the overload statically, so without the guard this would look for the
		// mappings of ce::CloudEvent and apply none
		EObject envelope = envelopeAround(loadPayload());

		assertEquals(1, instancePusher.pushInstance(envelope));
		assertEquals(3.301, batteryLevelInTwin(), 0.0001);
	}

	// ------------------------------------------------------------------------------------
	// The selector (#97). dragino-selected-mapping.xmi serves 'a/b/#' except 'a/b/ignored/#'.
	// ------------------------------------------------------------------------------------

	@Test
	@DisplayName("A mapping applies on a source its selector includes")
	void selector_appliesOnAnIncludedSource() throws Exception {
		mappingRegistry.registerModelMapping(loadMapping(SELECTED_MAPPING_URI));

		IngestResult result = payloadIngest.ingest(payload(), PayloadIngest.FORMAT_XMI, "a/b/c");

		assertEquals(Outcome.APPLIED, result.outcome());
		assertEquals(1, result.mappingsApplied());
		assertEquals(3.301, batteryLevelInTwin(SELECTED_MODEL, SELECTED_PROVIDER_ID), 0.0001);
	}

	@Test
	@DisplayName("And not on a source its excludes name, although an include covers it")
	void selector_isBeatenByAnExclude() throws Exception {
		mappingRegistry.registerModelMapping(loadMapping(SELECTED_MAPPING_URI));

		// 'a/b/#' covers 'a/b/ignored/x'; the exclude is the only thing that can take it back,
		// because a topic filter has no way to say "except"
		IngestResult result = payloadIngest.ingest(payload(), PayloadIngest.FORMAT_XMI, "a/b/ignored/x");

		assertEquals(Outcome.NO_MAPPING, result.outcome());
	}

	@Test
	@DisplayName("And not on a source no include covers")
	void selector_doesNotApplyElsewhere() throws Exception {
		mappingRegistry.registerModelMapping(loadMapping(SELECTED_MAPPING_URI));

		IngestResult result = payloadIngest.ingest(payload(), PayloadIngest.FORMAT_XMI, "other/topic");

		assertEquals(Outcome.NO_MAPPING, result.outcome());
	}

	@Test
	@DisplayName("A mapping with a selector does not apply to a push that carries no envelope")
	void selector_doesNotApplyToABarePush() throws Exception {
		mappingRegistry.registerModelMapping(loadMapping(SELECTED_MAPPING_URI));

		// there is no source to match, so nothing can - a mapping meant for a programmatic
		// push carries no selector, which is what every other mapping here does
		assertEquals(0, instancePusher.pushInstance(loadPayload()));
	}

	@Test
	@DisplayName("One EClass on two topics maps two ways - which is what issue #97 asks for")
	void selector_separatesTwoMappingsOfOneEClass() throws Exception {
		// both mappings are PAYLOAD-rooted and name the same providerClasses, so the EClass
		// lookup returns both for either message. The selector is what separates them: the
		// candidate list it filters is the payload-rooted one, not only the envelope-rooted
		// one, so nothing here needs an envelope-rooted mapping
		mappingRegistry.registerModelMapping(loadMapping(SELECTED_MAPPING_URI));
		mappingRegistry.registerModelMapping(loadMapping(SITE_B_MAPPING_URI));

		IngestResult fromA = payloadIngest.ingest(payload(), PayloadIngest.FORMAT_XMI, "a/b/c");
		IngestResult fromX = payloadIngest.ingest(payload(), PayloadIngest.FORMAT_XMI, "x/y/z");

		assertEquals(1, fromA.mappingsApplied(), "only the mapping whose selector names a/b/# may apply");
		assertEquals(1, fromX.mappingsApplied(), "only the mapping whose selector names x/y/# may apply");
		// each topic got its own provider naming and its own target model, from one EClass
		assertEquals(3.301, batteryLevelInTwin(SELECTED_MODEL, SELECTED_PROVIDER_ID), 0.0001);
		assertEquals(3.301, batteryLevelInTwin(SITE_B_MODEL, SITE_B_PROVIDER_ID), 0.0001);
	}

	@Test
	@DisplayName("A mapping with no selector applies to an enveloped message too - it expects nothing")
	void noSelector_appliesToAnEnvelopedMessage() throws Exception {
		// the same mapping as the test above, with its selector taken off. Without a selector
		// it serves every message of its EClass, envelope or not - which is why every mapping
		// written before envelopes existed still works unchanged through the ingest
		ProviderMapping siteB = loadMapping(SITE_B_MAPPING_URI);
		siteB.setSelector(null);
		mappingRegistry.registerModelMapping(loadMapping(SELECTED_MAPPING_URI));
		mappingRegistry.registerModelMapping(siteB);

		// 'x/y/#' was the only source it named, and this message comes from a/b/c
		IngestResult result = payloadIngest.ingest(payload(), PayloadIngest.FORMAT_XMI, "a/b/c");

		assertEquals(2, result.mappingsApplied(),
				"the selector-less mapping must apply beside the one whose selector matched");
		assertEquals(3.301, batteryLevelInTwin(SITE_B_MODEL, SITE_B_PROVIDER_ID), 0.0001);
		assertEquals(3.301, batteryLevelInTwin(SELECTED_MODEL, SELECTED_PROVIDER_ID), 0.0001);
	}

	@Test
	@DisplayName("Two mappings that both match both apply - nothing is ranked")
	void selector_everyMatchApplies() throws Exception {
		// the compositional case: the selector-less mapping keeps serving every message of the
		// EClass, and the selected one applies beside it on the sources it names
		mappingRegistry.registerModelMapping(loadMapping(MAPPING_URI));
		mappingRegistry.registerModelMapping(loadMapping(SELECTED_MAPPING_URI));

		IngestResult result = payloadIngest.ingest(payload(), PayloadIngest.FORMAT_XMI, "a/b/c");

		assertEquals(2, result.mappingsApplied(), "both mappings must apply, in either order");
		assertEquals(3.301, batteryLevelInTwin(MODEL, PROVIDER_ID), 0.0001);
		assertEquals(3.301, batteryLevelInTwin(SELECTED_MODEL, SELECTED_PROVIDER_ID), 0.0001);
	}

	// ------------------------------------------------------------------------------------
	// Envelope-rooted mappings. providerClasses names ce::CloudEvent, so the root every
	// feature path starts from is the envelope - which is how a mapping reads the topic a
	// message arrived on without the adapter copying it into the payload first.
	// ------------------------------------------------------------------------------------

	@Test
	@DisplayName("A mapping rooted at CloudEvent reads the envelope's own attributes")
	void envelopeRooted_readsTheEnvelope() throws Exception {
		mappingRegistry.registerModelMapping(loadMapping(ROOTED_MAPPING_URI));

		IngestResult result = payloadIngest.ingest(payload(), PayloadIngest.FORMAT_XMI, "a/b/c",
				new TransportContext("de.jena.test", Map.of()));

		assertEquals(Outcome.APPLIED, result.outcome());
		// the topic in one feature path step, which is what a payload-rooted mapping cannot do
		assertEquals("a/b/c", valueInTwin(ROOTED_MODEL, ROOTED_PROVIDER_ID, "message", "source"));
		assertEquals("de.jena.test", valueInTwin(ROOTED_MODEL, ROOTED_PROVIDER_ID, "message", "type"));
		assertEquals(DRAGINO_ECLASS, valueInTwin(ROOTED_MODEL, ROOTED_PROVIDER_ID, "message", "dataschema"));
	}

	@Test
	@DisplayName("An envelope-rooted expression reads into the payload with no cast")
	void envelopeRooted_readsIntoThePayload() throws Exception {
		mappingRegistry.registerModelMapping(loadMapping(ROOTED_MAPPING_URI));

		payloadIngest.ingest(payload(), PayloadIngest.FORMAT_XMI, "a/b/c");

		// `data` is typed EObject, so no feature path can name a feature below it - but m2x
		// leaves an unresolved property to the evaluator, which looks it up on the RUNTIME
		// type. So `self.data.deduplicationId` needs no oclAsType.
		assertEquals(PROVIDER_ID, valueInTwin(ROOTED_MODEL, ROOTED_PROVIDER_ID, "message", "deduplicationId"));
	}

	@Test
	@DisplayName("A feature path crosses data, because data is a feature of CloudEvent")
	void envelopeRooted_featurePathCrossesData() throws Exception {
		mappingRegistry.registerModelMapping(loadMapping(ROOTED_MAPPING_URI));

		payloadIngest.ingest(payload(), PayloadIngest.FORMAT_XMI, "a/b/c");

		// the first step names CloudEvent/data and the rest navigates the payload's own
		// EClass - navigateFeaturePath is a dynamic eGet loop, so each step only has to be a
		// feature of the object it is applied to
		assertEquals(3.301,
				((Number) valueInTwin(ROOTED_MODEL, ROOTED_PROVIDER_ID, "message", "batteryLevel")).doubleValue(),
				0.0001);
	}

	@Test
	@DisplayName("A referencedResource crosses data too, so one block still makes one service per element")
	void envelopeRooted_oneServicePerElementBelowData() throws Exception {
		mappingRegistry.registerModelMapping(loadMapping(ROOTED_MAPPING_URI));

		payloadIngest.ingest(payload(), PayloadIngest.FORMAT_XMI, "a/b/c");

		// the dragino payload carries two rxInfo entries, and each becomes its own service,
		// named from the element - the shape ilsa-configuration needs for its signals
		assertEquals(-127, ((Number) valueInTwin(ROOTED_MODEL, ROOTED_PROVIDER_ID, "a84041ffff1f4b70", "rssi"))
				.intValue());
		assertEquals(-116, ((Number) valueInTwin(ROOTED_MODEL, ROOTED_PROVIDER_ID, "fcc23dfffe2ea641", "rssi"))
				.intValue());
	}

	@Test
	@DisplayName("Both roots apply to one message - the lookup is by the root each mapping declared")
	void envelopeRooted_appliesBesideThePayloadRooted() throws Exception {
		mappingRegistry.registerModelMapping(loadMapping(MAPPING_URI));
		mappingRegistry.registerModelMapping(loadMapping(ROOTED_MAPPING_URI));

		IngestResult result = payloadIngest.ingest(payload(), PayloadIngest.FORMAT_XMI, "a/b/c");

		assertEquals(2, result.mappingsApplied(), "one mapping of each root must apply");
		assertEquals(3.301, batteryLevelInTwin(MODEL, PROVIDER_ID), 0.0001);
		assertEquals("a/b/c", valueInTwin(ROOTED_MODEL, ROOTED_PROVIDER_ID, "message", "source"));
	}

	@Test
	@DisplayName("An envelope-rooted mapping never fires on a push that builds no envelope")
	void envelopeRooted_doesNotFireOnABarePush() throws Exception {
		mappingRegistry.registerModelMapping(loadMapping(ROOTED_MAPPING_URI));

		// pushInstance(EObject) synthesizes nothing, so there is no envelope to be the root -
		// otherwise a mapping that builds a provider id out of a topic segment would write a
		// nonsense name into the twin on every programmatic push, silently
		assertEquals(0, instancePusher.pushInstance(loadPayload()));
	}

	@Test
	@DisplayName("Its selector is read too, so it does not claim the sources it excluded")
	void envelopeRooted_honoursItsSelector() throws Exception {
		mappingRegistry.registerModelMapping(loadMapping(ROOTED_MAPPING_URI));

		IngestResult result = payloadIngest.ingest(payload(), PayloadIngest.FORMAT_XMI, "other/topic");

		assertEquals(Outcome.NO_MAPPING, result.outcome());
	}

	/**
	 * Stands in for the reader the envelope is being carried for. It is handed the source
	 * EObject because its mapping declares no feature path.
	 */
	private Object probe(Object input) {
		if (input instanceof EObject payload && payload.eContainer() instanceof CloudEvent envelope) {
			seen.set(envelope);
			return envelope.getSource() + " " + envelope.getDataschema();
		}
		return null;
	}

	/**
	 * Wraps a payload the way the southbound ingest does, including putting the envelope in
	 * the payload's resource.
	 */
	private static CloudEvent envelopeAround(EObject payload) {
		CloudEvent envelope = CloudEventsFactory.eINSTANCE.createCloudEvent();
		envelope.setEventId("test-1");
		envelope.setSource(TOPIC);
		envelope.setType("de.jena.test");
		envelope.setDataschema(DRAGINO_ECLASS);
		payload.eResource().getContents().add(envelope);
		envelope.setData(payload);
		return envelope;
	}

	private double batteryLevelInTwin() throws Exception {
		return batteryLevelInTwin(MODEL, PROVIDER_ID);
	}

	private double batteryLevelInTwin(String model, String providerId) throws Exception {
		return ((Number) valueInTwin(model, providerId, "battery", "level")).doubleValue();
	}

	private String sourceResourceInTwin() throws Exception {
		return (String) valueInTwin(MODEL, PROVIDER_ID, "envelope", "source");
	}

	private Object valueInTwin(String model, String providerId, String service, String resource) throws Exception {
		Promise<Object> value = gatewayThread.execute(new AbstractSensinactCommand<Object>() {
			@Override
			protected Promise<Object> call(SensinactDigitalTwin twin, SensinactModelManager modelManager,
					PromiseFactory pf) {
				try {
					SensinactProvider provider = twin.getProvider(model, providerId);
					assertNotNull(provider, "Provider should have been created by the push");
					TimedValue<?> timed = provider.getServices().get(service).getResources().get(resource).getValue()
							.getValue();
					return pf.resolved(timed.getValue());
				} catch (Exception e) {
					return pf.failed(e);
				}
			}
		});
		return value.getValue();
	}

	private byte[] payload() throws Exception {
		try (var stream = getClass().getResourceAsStream(PAYLOAD_URI)) {
			return stream.readAllBytes();
		}
	}

	private EObject loadPayload() throws Exception {
		URL payloadUrl = getClass().getResource(PAYLOAD_URI);
		Resource resource = resourceSet.createResource(URI.createURI(payloadUrl.toString()));
		resource.load(null);
		return resource.getContents().get(0);
	}

	private ProviderMapping loadMapping() throws Exception {
		return loadMapping(MAPPING_URI);
	}

	private ProviderMapping loadMapping(String uri) throws Exception {
		URL mappingUrl = getClass().getResource(uri);
		Resource mappingResource = resourceSet.createResource(URI.createURI(mappingUrl.toString()));
		mappingResource.load(null);
		return (ProviderMapping) mappingResource.getContents().get(0);
	}

}
