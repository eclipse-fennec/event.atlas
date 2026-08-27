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
package org.eclipse.fennec.event.atlas.southbound.common;

import org.osgi.annotation.versioning.ProviderType;

/**
 * Transport-agnostic ingress for southbound adapters: turns a raw payload into
 * {@link org.eclipse.emf.ecore.EObject}s and pushes them into the sensinact digital twin.
 * <p>
 * This is the piece every southbound adapter shares. An adapter is then only responsible
 * for its transport - subscribing to an MQTT topic, receiving an HTTP request - and hands
 * the bytes over here.
 * <p>
 * Model resolution is the runtime's own mechanism and needs no configuration: the payload
 * names its model (an XMI root element carries the nsURI), and the resource set resolves it
 * locally first, then fetch-on-miss through the Model Atlas client. A payload whose model
 * cannot be resolved either way is dropped with {@link IngestResult.Outcome#MODEL_UNKNOWN}
 * rather than failing the adapter.
 * <p>
 * Implementations never throw for a bad payload - every foreseeable failure is reported as
 * an {@link IngestResult} and logged - so an adapter can call this directly from a broker
 * callback or a request thread without risking the connection.
 * <p>
 * A payload with no resolvable model is dropped, but not necessarily unnoticed: registering
 * an {@link UnknownModelHandler} service has such payloads additionally offered to it, off
 * the ingest thread and without changing the {@link IngestResult}.
 * @author Ilenia Salvadori
 */
@ProviderType
public interface PayloadIngest {

	/** Format hint for EMF XMI payloads, the format in which models describe themselves. */
	String FORMAT_XMI = "xmi";

	/**
	 * Format hint for JSON payloads, deserialized by the Fennec codec.
	 * <p>
	 * The codec is a separate bundle ({@code org.eclipse.fennec.codec}): it contributes the
	 * EMF resource factory for this extension. A runtime without it reports
	 * {@link IngestResult.Outcome#FORMAT_UNSUPPORTED} for every JSON payload.
	 */
	String FORMAT_JSON = "json";

	/**
	 * Format hint asking for the format to be detected from the payload itself, per message.
	 * <p>
	 * Use it when one channel carries more than one encoding - a broker topic tree where some
	 * publishers send XMI and others JSON, for instance. Detection looks at the first
	 * non-whitespace character only: <code>&lt;</code> means XMI, <code>{</code> or
	 * <code>[</code> mean JSON. It is a cheap structural check, not a validation; a payload that
	 * is neither still fails in the codec and is reported as a parse error.
	 * <p>
	 * An explicit {@link #FORMAT_XMI} or {@link #FORMAT_JSON} always wins over detection, so a
	 * single-format channel can keep saying exactly what it sends.
	 */
	String FORMAT_AUTO = "auto";

	/**
	 * Deserializes a payload and pushes every resulting root object into the digital twin
	 * through all provider mappings registered for its EClass.
	 * @param payload the raw payload bytes. Parameter must not be <code>null</code>
	 * @param formatHint the payload format: either a bare EMF file extension
	 * ({@link #FORMAT_XMI}, {@link #FORMAT_JSON}) or a media type such as
	 * {@code application/json}. <code>null</code> or unrecognized values fall back to
	 * {@link #FORMAT_XMI}
	 * @param source a short human-readable origin used in log messages - an MQTT topic, a
	 * REST path. May be <code>null</code>
	 * @return what happened. Never <code>null</code>
	 * @throws NullPointerException if the payload parameter is <code>null</code>
	 */
	IngestResult ingest(byte[] payload, String formatHint, String source);

}
