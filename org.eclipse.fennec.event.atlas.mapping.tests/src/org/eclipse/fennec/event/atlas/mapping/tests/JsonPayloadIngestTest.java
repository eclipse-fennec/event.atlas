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

import java.net.URL;
import java.nio.charset.StandardCharsets;

import org.eclipse.emf.common.util.URI;
import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.emf.ecore.resource.ResourceSet;
import org.eclipse.fennec.event.atlas.mapping.ProviderMappingRegistry;
import org.eclipse.fennec.event.atlas.model.mapping.ProviderMapping;
import org.eclipse.fennec.event.atlas.southbound.common.IngestResult;
import org.eclipse.fennec.event.atlas.southbound.common.IngestResult.Outcome;
import org.eclipse.fennec.event.atlas.southbound.common.PayloadIngest;
import org.eclipse.sensinact.core.command.AbstractSensinactCommand;
import org.eclipse.sensinact.core.command.GatewayThread;
import org.eclipse.sensinact.core.model.SensinactModelManager;
import org.eclipse.sensinact.core.twin.SensinactDigitalTwin;
import org.eclipse.sensinact.core.twin.SensinactProvider;
import org.eclipse.sensinact.core.twin.TimedValue;
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
 * Ingests a JSON payload, which takes an EMF JSON codec in the runtime: the resource factory
 * for the <code>json</code> file extension is what {@link PayloadIngest} selects for a JSON
 * channel. Without that bundle EMF answers with its wildcard factory - XMI - and every JSON
 * payload dies in a SAX parser (issue #17), so this is the test that keeps
 * {@code org.eclipse.fennec.codec} in the runtime's bundle set.
 * <p>
 * The payload is the JSON twin of {@code data/dragino-example.dragino}: a LoRaWAN uplink as a
 * network server would POST it, not something the codec produced itself.
 * @author Ilenia Salvadori
 */
@ExtendWith(BundleContextExtension.class)
@ExtendWith(ServiceExtension.class)
public class JsonPayloadIngestTest {

	private static final String DRAGINO_MAPPING_URI = "/data/battery/dragino-battery-mapping_no-profile.xmi";
	private static final String DRAGINO_JSON_PAYLOAD_URI = "/data/dragino-example.json";
	private static final String DRAGINO_MODEL = "dragino-battery-sensor";
	private static final String PROVIDER_ID = "9ffbc7be-916a-4597-9d64-0713a3d5e030";
	private static final String TOPIC = "waterpark/sensors/soil";

	@InjectService
	GatewayThread gatewayThread;

	@InjectService
	ProviderMappingRegistry mappingRegistry;

	@InjectService
	PayloadIngest payloadIngest;

	@InjectService
	ResourceSet resourceSet;

	@AfterEach
	void tearDown() {
		mappingRegistry.dispose();
	}

	@Test
	@DisplayName("A JSON payload reaches the digital twin through the registered mapping")
	void ingest_jsonPayload_isDeserializedAndPushed() throws Exception {
		mappingRegistry.registerModelMapping(loadDraginoMapping());

		IngestResult result = payloadIngest.ingest(draginoJson(), PayloadIngest.FORMAT_JSON, TOPIC);

		assertEquals(Outcome.APPLIED, result.outcome(), "A JSON payload must not be handed to the XMI parser");
		assertEquals(1, result.mappingsApplied());
		assertEquals(3.301, batteryLevelInTwin(), 0.0001, "The value must survive deserialization");
	}

	@Test
	@DisplayName("The same payload is ingested when the format is detected rather than declared")
	void ingest_jsonPayloadWithAutoFormat_isDeserializedAndPushed() throws Exception {
		mappingRegistry.registerModelMapping(loadDraginoMapping());

		IngestResult result = payloadIngest.ingest(draginoJson(), PayloadIngest.FORMAT_AUTO, TOPIC);

		assertEquals(Outcome.APPLIED, result.outcome());
	}

	@Test
	@DisplayName("A malformed JSON payload is a parse error, not a missing codec")
	void ingest_malformedJson_reportsParseError() {
		IngestResult result = payloadIngest.ingest("{ this is not json".getBytes(StandardCharsets.UTF_8),
				PayloadIngest.FORMAT_JSON, TOPIC);

		assertEquals(Outcome.PARSE_ERROR, result.outcome());
	}

	@Test
	@DisplayName("A JSON payload naming an unresolvable model is reported with the codec's reason")
	// The JSON codec does not throw for a `_type` it cannot resolve the way the XMI parser does
	// (that one raises PackageNotFoundException -> MODEL_UNKNOWN): it records a diagnostic and
	// hands back an empty resource. The outcome is EMPTY, and the diagnostic is what makes it
	// actionable.
	void ingest_jsonPayloadNamingAnUnknownModel_reportsTheCodecDiagnostic() throws Exception {
		byte[] payload = new String(draginoJson(), StandardCharsets.UTF_8)
				.replace("lorawan/dragino#//DraginoLSE01Uplink", "lorawan/nowhere#//Nope")
				.getBytes(StandardCharsets.UTF_8);

		IngestResult result = payloadIngest.ingest(payload, PayloadIngest.FORMAT_JSON, TOPIC);

		assertEquals(Outcome.EMPTY, result.outcome());
		assertNotNull(result.detail(), "The codec's load diagnostic must be reported");
		assertTrue(result.detail().contains("lorawan/nowhere#//Nope"),
				"The diagnostic must name the type that could not be resolved: " + result.detail());
	}

	private double batteryLevelInTwin() throws Exception {
		Promise<Double> batteryLevel = gatewayThread.execute(new AbstractSensinactCommand<Double>() {
			@Override
			protected Promise<Double> call(SensinactDigitalTwin twin, SensinactModelManager modelManager,
					PromiseFactory pf) {
				try {
					SensinactProvider provider = twin.getProvider(DRAGINO_MODEL, PROVIDER_ID);
					assertNotNull(provider, "Provider should have been created by the push");
					TimedValue<?> value = provider.getServices().get("battery").getResources().get("level").getValue()
							.getValue();
					return pf.resolved(((Number) value.getValue()).doubleValue());
				} catch (Exception e) {
					return pf.failed(e);
				}
			}
		});
		return batteryLevel.getValue();
	}

	private byte[] draginoJson() throws Exception {
		try (var payload = getClass().getResourceAsStream(DRAGINO_JSON_PAYLOAD_URI)) {
			return payload.readAllBytes();
		}
	}

	private ProviderMapping loadDraginoMapping() throws Exception {
		URL mappingUrl = getClass().getResource(DRAGINO_MAPPING_URI);
		Resource mappingResource = resourceSet.createResource(URI.createURI(mappingUrl.toString()));
		mappingResource.load(null);
		return (ProviderMapping) mappingResource.getContents().get(0);
	}

}
