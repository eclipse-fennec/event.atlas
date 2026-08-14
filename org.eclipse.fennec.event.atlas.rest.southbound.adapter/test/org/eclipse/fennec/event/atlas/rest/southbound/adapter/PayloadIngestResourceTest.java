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
package org.eclipse.fennec.event.atlas.rest.southbound.adapter;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.lang.reflect.Field;
import java.nio.charset.StandardCharsets;

import org.eclipse.fennec.event.atlas.southbound.common.IngestResult;
import org.eclipse.fennec.event.atlas.southbound.common.PayloadIngest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import jakarta.ws.rs.core.Response;

/**
 * Unit tests for {@link PayloadIngestResource}: every {@link IngestResult.Outcome} must map
 * to a distinct, meaningful HTTP status, because that mapping is the endpoint's contract
 * with the devices posting to it.
 * @author Ilenia Salvadori
 */
@ExtendWith(MockitoExtension.class)
public class PayloadIngestResourceTest {

	private static final byte[] PAYLOAD = "<payload/>".getBytes(StandardCharsets.UTF_8);

	@Mock
	private PayloadIngest payloadIngest;

	private PayloadIngestResource resource;

	@BeforeEach
	void setUp() {
		resource = new PayloadIngestResource();
		inject(resource, "payloadIngest", payloadIngest);
	}

	@Test
	@DisplayName("A payload that reaches the twin answers 200")
	void ingest_applied_answersOk() {
		when(payloadIngest.ingest(any(), any(), any())).thenReturn(IngestResult.applied(1, 2));

		Response response = resource.ingest("weather", "application/xml", PAYLOAD);

		assertEquals(200, response.getStatus());
		assertTrue(response.getEntity().toString().contains("2 mapping(s)"));
	}

	@Test
	@DisplayName("A payload nobody maps answers 202, not 200 - it was understood but dropped")
	void ingest_noMapping_answersAccepted() {
		when(payloadIngest.ingest(any(), any(), any())).thenReturn(IngestResult.noMapping(1, "Sensor"));

		Response response = resource.ingest("weather", "application/xml", PAYLOAD);

		assertEquals(202, response.getStatus());
		assertTrue(response.getEntity().toString().contains("Sensor"));
	}

	@Test
	@DisplayName("An unresolvable model answers 422 and names the nsURI")
	void ingest_modelUnknown_answersUnprocessable() {
		when(payloadIngest.ingest(any(), any(), any()))
				.thenReturn(IngestResult.modelUnknown("http://example.org/nowhere/1.0"));

		Response response = resource.ingest("weather", "application/xml", PAYLOAD);

		assertEquals(422, response.getStatus());
		assertTrue(response.getEntity().toString().contains("http://example.org/nowhere/1.0"));
	}

	@Test
	@DisplayName("An unparseable payload answers 400")
	void ingest_parseError_answersBadRequest() {
		when(payloadIngest.ingest(any(), any(), any())).thenReturn(IngestResult.parseError("unexpected token"));

		assertEquals(400, resource.ingest("weather", "application/json", PAYLOAD).getStatus());
	}

	@Test
	@DisplayName("A payload with no objects answers 400")
	void ingest_empty_answersBadRequest() {
		when(payloadIngest.ingest(any(), any(), any())).thenReturn(IngestResult.empty());

		assertEquals(400, resource.ingest("weather", "application/xml", PAYLOAD).getStatus());
	}

	@Test
	@DisplayName("A twin write failure answers 503, so the sender knows to retry")
	void ingest_pushFailed_answersServiceUnavailable() {
		when(payloadIngest.ingest(any(), any(), any())).thenReturn(IngestResult.pushFailed(1, "gateway down"));

		assertEquals(503, resource.ingest("weather", "application/xml", PAYLOAD).getStatus());
	}

	@Test
	@DisplayName("An empty body is rejected before the ingest is involved")
	void ingest_emptyBody_answersBadRequestWithoutIngesting() {
		assertEquals(400, resource.ingest("weather", "application/xml", new byte[0]).getStatus());
		assertEquals(400, resource.ingest("weather", "application/xml", null).getStatus());
		verify(payloadIngest, never()).ingest(any(), any(), any());
	}

	@Test
	@DisplayName("The content type is passed through as the format hint and the channel as the source")
	void ingest_passesContentTypeAndChannel() {
		when(payloadIngest.ingest(any(), any(), any())).thenReturn(IngestResult.applied(1, 1));

		resource.ingest("weather", "application/json", PAYLOAD);

		verify(payloadIngest).ingest(any(byte[].class), eq("application/json"), eq("rest/weather"));
	}

	private static void inject(Object target, String fieldName, Object value) {
		try {
			Field field = target.getClass().getDeclaredField(fieldName);
			field.setAccessible(true);
			field.set(target, value);
		} catch (ReflectiveOperationException e) {
			throw new IllegalStateException("Could not inject '" + fieldName + "'", e);
		}
	}

}
