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
package org.eclipse.fennec.event.atlas.mqtt.southbound.adapter;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.nio.charset.StandardCharsets;
import java.util.Dictionary;

import org.eclipse.fennec.event.atlas.southbound.common.IngestResult;
import org.eclipse.fennec.event.atlas.southbound.common.PayloadIngest;
import org.eclipse.sensinact.gateway.southbound.mqtt.api.IMqttMessage;
import org.eclipse.sensinact.gateway.southbound.mqtt.api.IMqttMessageListener;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.ArgumentMatchers;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceRegistration;

/**
 * Unit tests for {@link MqttPayloadListener}. The MQTT client and the ingest are both
 * mocked, which is enough: everything this adapter owns - topic normalization, broker
 * filtering, and never letting an exception reach the dispatch thread - is observable
 * without a broker.
 * @author Ilenia Salvadori
 */
@ExtendWith(MockitoExtension.class)
public class MqttPayloadListenerTest {

	@Mock
	private PayloadIngest payloadIngest;
	@Mock
	private BundleContext bundleContext;
	@Mock
	private ServiceRegistration<IMqttMessageListener> registration;
	@Mock
	private IMqttMessage message;

	private MqttPayloadListener listener;

	@BeforeEach
	void setUp() {
		listener = new MqttPayloadListener();
		inject(listener, "payloadIngest", payloadIngest);
	}

	@Test
	@DisplayName("Topic filters are published as the whiteboard property sensinact reads")
	@SuppressWarnings({ "unchecked", "rawtypes" })
	void activate_publishesTopicFiltersAsServiceProperty() {
		whenRegistering();

		listener.activate(bundleContext, config(new String[] { "sensors/+/telemetry" }, "", "xmi"));

		ArgumentCaptor<Dictionary> properties = ArgumentCaptor.forClass(Dictionary.class);
		verify(bundleContext).registerService(eq(IMqttMessageListener.class),
				ArgumentMatchers.<IMqttMessageListener>any(), properties.capture());
		assertArrayEquals(new String[] { "sensors/+/telemetry" },
				(String[]) properties.getValue().get(IMqttMessageListener.MQTT_TOPICS_FILTERS));
	}

	@Test
	@DisplayName("A comma-separated topic list is split, trimmed and blanks dropped")
	@SuppressWarnings({ "unchecked", "rawtypes" })
	void activate_normalizesCommaSeparatedTopics() {
		whenRegistering();

		listener.activate(bundleContext, config(new String[] { "a/#, b/# ", "", "  ", "c/#" }, "", "xmi"));

		ArgumentCaptor<Dictionary> properties = ArgumentCaptor.forClass(Dictionary.class);
		verify(bundleContext).registerService(eq(IMqttMessageListener.class),
				ArgumentMatchers.<IMqttMessageListener>any(), properties.capture());
		assertArrayEquals(new String[] { "a/#", "b/#", "c/#" },
				(String[]) properties.getValue().get(IMqttMessageListener.MQTT_TOPICS_FILTERS));
	}

	@Test
	@DisplayName("Activation without topics fails rather than subscribing to the whole broker")
	void activate_withoutTopics_refusesToStart() {
		assertThrows(IllegalArgumentException.class,
				() -> listener.activate(bundleContext, config(new String[0], "", "xmi")));
		verify(bundleContext, never()).registerService(eq(IMqttMessageListener.class),
				ArgumentMatchers.<IMqttMessageListener>any(), any());
	}

	@Test
	@DisplayName("A message is handed to the ingest with the topic as its source")
	void onMqttMessage_ingestsPayload() {
		whenRegistering();
		listener.activate(bundleContext, config(new String[] { "sensors/#" }, "", "xmi"));
		when(message.getPayload()).thenReturn("<payload/>".getBytes(StandardCharsets.UTF_8));
		when(payloadIngest.ingest(any(), any(), any())).thenReturn(IngestResult.applied(1, 1));

		listener.onMqttMessage("broker-1", "sensors/a/telemetry", message);

		verify(payloadIngest).ingest(any(byte[].class), eq("xmi"), eq("sensors/a/telemetry"));
	}

	@Test
	@DisplayName("With mqtt.handler.id set, messages from other brokers are ignored")
	void onMqttMessage_filtersByHandlerId() {
		whenRegistering();
		listener.activate(bundleContext, config(new String[] { "sensors/#" }, "broker-1", "xmi"));
		lenient().when(message.getPayload()).thenReturn("<payload/>".getBytes(StandardCharsets.UTF_8));

		listener.onMqttMessage("broker-2", "sensors/a", message);

		verify(payloadIngest, never()).ingest(any(), any(), any());
	}

	@Test
	@DisplayName("An empty payload is dropped without reaching the ingest")
	void onMqttMessage_withEmptyPayload_dropsIt() {
		whenRegistering();
		listener.activate(bundleContext, config(new String[] { "sensors/#" }, "", "xmi"));
		when(message.getPayload()).thenReturn(new byte[0]);

		listener.onMqttMessage("broker-1", "sensors/a", message);

		verify(payloadIngest, never()).ingest(any(), any(), any());
	}

	@Test
	@DisplayName("An ingest failure never escapes onto the MQTT dispatch thread")
	void onMqttMessage_containsUnexpectedFailures() {
		whenRegistering();
		listener.activate(bundleContext, config(new String[] { "sensors/#" }, "", "xmi"));
		when(message.getPayload()).thenReturn("<payload/>".getBytes(StandardCharsets.UTF_8));
		when(payloadIngest.ingest(any(), any(), any())).thenThrow(new RuntimeException("boom"));

		assertDoesNotThrow(() -> listener.onMqttMessage("broker-1", "sensors/a", message));
	}

	@Test
	@DisplayName("Deactivation unregisters the whiteboard service")
	void deactivate_unregisters() {
		whenRegistering();
		listener.activate(bundleContext, config(new String[] { "sensors/#" }, "", "xmi"));

		listener.deactivate();

		verify(registration).unregister();
	}

	@SuppressWarnings("unchecked")
	private void whenRegistering() {
		lenient().when(bundleContext.registerService(eq(IMqttMessageListener.class),
				ArgumentMatchers.<IMqttMessageListener>any(), any())).thenReturn(registration);
	}

	private static MqttPayloadListener.Config config(String[] topics, String handlerId, String format) {
		return new MqttPayloadListener.Config() {
			@Override
			public Class<? extends Annotation> annotationType() {
				return MqttPayloadListener.Config.class;
			}

			@Override
			public String[] mqttTopics() {
				return topics;
			}

			@Override
			public String mqtt_handler_id() {
				return handlerId;
			}

			@Override
			public String format() {
				return format;
			}

			@Override
			public String name() {
				return "test-channel";
			}
		};
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
