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

import java.util.Arrays;
import java.util.Dictionary;
import java.util.Hashtable;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Stream;

import org.eclipse.fennec.event.atlas.southbound.common.PayloadIngest;
import org.eclipse.fennec.event.atlas.southbound.common.TransportContext;
import org.eclipse.sensinact.gateway.southbound.mqtt.api.IMqttMessage;
import org.eclipse.sensinact.gateway.southbound.mqtt.api.IMqttMessageListener;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceRegistration;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.ConfigurationPolicy;
import org.osgi.service.component.annotations.Deactivate;
import org.osgi.service.component.annotations.Reference;

/**
 * MQTT southbound adapter: subscribes to configured topics and pushes every payload into
 * the sensinact digital twin through the shared {@link PayloadIngest}.
 * <p>
 * The broker connection itself is not our concern - it is sensinact's own
 * {@code MqttClientHandler}, configured through the factory pid
 * {@code sensinact.southbound.mqtt} (host, port, credentials, TLS, subscribed topics). This
 * component only publishes an {@link IMqttMessageListener} on the whiteboard and receives
 * what that client delivers.
 * <p>
 * The whiteboard service is registered by hand from {@link #activate(BundleContext, Config)}
 * rather than declared through {@code @Component(service = ...)}: the topic filters are a
 * configuration value and DS cannot compute service properties. Hence {@code service = {}}.
 * <p>
 * Note that sensinact binds <em>every</em> listener to <em>every</em> configured broker
 * client - its listener reference carries no target filter - so if more than one broker is
 * configured, set {@code mqtt.handler.id} to the {@code id} of the client this adapter
 * should listen to.
 * @author Ilenia Salvadori
 */
@Component(name = "MqttPayloadListener", service = {}, configurationPid = MqttPayloadListener.PID, configurationPolicy = ConfigurationPolicy.REQUIRE)
public class MqttPayloadListener implements IMqttMessageListener {

	/** Configuration pid of this adapter (a factory pid: one instance per topic set). */
	public static final String PID = "event.atlas.southbound.mqtt";

	private static final Logger logger = Logger.getLogger(MqttPayloadListener.class.getName());

	/**
	 * Configuration of one MQTT ingest channel.
	 */
	public @interface Config {

		/**
		 * The MQTT topic filters to ingest, e.g. {@code sensors/+/telemetry}. Required -
		 * there is deliberately no "everything" default, so that adding this adapter to a
		 * runtime cannot silently start consuming a whole shared broker.
		 */
		String[] mqttTopics() default {};

		/**
		 * The {@code id} of the {@code sensinact.southbound.mqtt} client whose messages
		 * this adapter should handle. Empty accepts messages from every configured client.
		 */
		String mqtt_handler_id() default "";

		/**
		 * Payload format, see {@link PayloadIngest#ingest(byte[], String, String)}. XMI
		 * payloads name their own model and need no further configuration.
		 */
		String format() default PayloadIngest.FORMAT_XMI;

		/** A name for this channel, used in the service registration and in logs. */
		String name() default "event-atlas-mqtt";

		/**
		 * The CloudEvents {@code type} stamped on every message of this channel.
		 * <p>
		 * An MQTT topic carries no notion of an event type, so this runtime has nothing to
		 * derive one from and the value is a deployment choice. The default names this
		 * adapter, so a message says which transport it came in on even when no deployment
		 * configured a type.
		 * <p>
		 * The key is spelled the way a CloudEvents-aware messaging layer spells it
		 * ({@code cloudEvents.type} in {@code org.gecko.messaging}, for instance), so a
		 * deployment that ever meets one does not have to learn a second name. The
		 * <em>default</em> is this adapter's own, and a runtime whose transport brings a
		 * default of its own should set the key explicitly rather than inherit either.
		 */
		String cloudEvents_type() default DEFAULT_EVENT_TYPE;
	}

	/** The event type of a message this adapter received, unless a deployment names one. */
	public static final String DEFAULT_EVENT_TYPE = "org.eclipse.fennec.event.atlas.mqtt.message";

	/**
	 * Envelope extension naming the broker client a message was delivered by - the {@code id}
	 * of the {@code sensinact.southbound.mqtt} configuration, which is what tells two brokers
	 * carrying the same topic apart. Taken from the message rather than from
	 * {@code mqtt.handler.id}, which may be the empty "any broker" filter.
	 */
	static final String EXTENSION_BROKER = "mqtt.broker";

	/** Envelope extension naming the ingest channel - this adapter's configured name. */
	static final String EXTENSION_CHANNEL = "mqtt.channel";

	@Reference
	private PayloadIngest payloadIngest;

	private ServiceRegistration<IMqttMessageListener> registration;
	private String handlerId;
	private String format;
	private String name;
	private String eventType;

	@Activate
	void activate(BundleContext context, Config config) {
		String[] topics = normalizeTopics(config.mqttTopics());
		if (topics.length == 0) {
			// Fail activation rather than defaulting to '#': subscribing to an entire
			// broker because a topic list was forgotten is not a helpful default.
			throw new IllegalArgumentException(
					"No mqttTopics configured for '" + PID + "' - refusing to activate an MQTT adapter "
							+ "without topic filters");
		}
		handlerId = config.mqtt_handler_id() == null ? "" : config.mqtt_handler_id().trim();
		format = config.format();
		name = config.name();
		eventType = config.cloudEvents_type();

		Dictionary<String, Object> properties = new Hashtable<>();
		properties.put(IMqttMessageListener.MQTT_TOPICS_FILTERS, topics);
		properties.put("name", name);
		registration = context.registerService(IMqttMessageListener.class, this, properties);

		logger.info(String.format("MQTT southbound adapter '%s' listening on %s (format %s%s)", name,
				Arrays.toString(topics), format,
				handlerId.isEmpty() ? ", any broker" : ", broker '" + handlerId + "'"));
	}

	@Deactivate
	void deactivate() {
		if (registration != null) {
			registration.unregister();
			registration = null;
		}
	}

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.sensinact.gateway.southbound.mqtt.api.IMqttMessageListener#onMqttMessage(java.lang.String, java.lang.String, org.eclipse.sensinact.gateway.southbound.mqtt.api.IMqttMessage)
	 */
	@Override
	public void onMqttMessage(String messageHandlerId, String topic, IMqttMessage message) {
		// Nothing may escape from here: this runs on the MQTT client's dispatch thread, and
		// one bad payload must never cost us the broker connection.
		try {
			if (!accepts(messageHandlerId)) {
				return;
			}
			byte[] payload = message.getPayload();
			if (payload == null || payload.length == 0) {
				logger.warning(String.format("Empty MQTT payload on topic '%s' - nothing to ingest", topic));
				return;
			}
			logger.fine(String.format("MQTT message received on topic '%s' (%s bytes)", topic, payload.length));
			// The topic is the source: it is what a mapping reads to tell two devices of one
			// family apart, and it is reported exactly as it would be written in a topic
			// filter. The broker and the channel are what only this adapter knows.
			TransportContext transport = new TransportContext(eventType,
					Map.of(EXTENSION_BROKER, messageHandlerId == null ? "" : messageHandlerId,
							EXTENSION_CHANNEL, name));
			// The ingest logs every outcome itself, so nothing more is needed here.
			payloadIngest.ingest(payload, format, topic, transport);
		} catch (Throwable e) {
			logger.log(Level.SEVERE, "Error handling MQTT message on topic '" + topic + "'", e);
		}
	}

	/**
	 * @return whether messages from the given broker client should be handled here
	 */
	private boolean accepts(String messageHandlerId) {
		return handlerId.isEmpty() || handlerId.equals(messageHandlerId);
	}

	/**
	 * Config Admin may deliver a topic list as a real array or, depending on how the
	 * configuration was written, as a single comma-separated string. Accept both, and drop
	 * blanks.
	 */
	private static String[] normalizeTopics(String[] configured) {
		if (configured == null) {
			return new String[0];
		}
		return Arrays.stream(configured).filter(t -> t != null && !t.isBlank())
				.flatMap(t -> t.contains(",") ? Stream.of(t.split(",")) : Stream.of(t)).map(String::trim)
				.filter(t -> !t.isEmpty()).toArray(String[]::new);
	}

}
