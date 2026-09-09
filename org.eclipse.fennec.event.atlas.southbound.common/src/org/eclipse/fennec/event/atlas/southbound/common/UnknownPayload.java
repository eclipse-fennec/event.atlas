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

import static java.util.Objects.requireNonNull;

import java.time.Instant;

/**
 * A payload {@link PayloadIngest} could not turn into objects, handed to an
 * {@link UnknownModelHandler} together with everything needed to act on it without
 * re-parsing.
 * <p>
 * Deliberately free of EMF types: a handler bundle can be written and versioned without
 * agreeing with this runtime on a codec or an Ecore version.
 * <p>
 * Value equality is not meaningful here - {@link #payload()} is an array, so the generated
 * {@code equals}/{@code hashCode} compare it by identity. Do not use this record as a map
 * key; key on {@link #source()} and {@link #namespaceUri()}, which is the channel identity a
 * collector groups by.
 * @param payload the raw payload bytes, exactly as the adapter received them. Never
 * <code>null</code>. This is the ingest array itself, not a copy - treat it as read-only
 * @param format the format the payload was read as: an EMF file extension, so
 * {@link PayloadIngest#FORMAT_XMI} or {@link PayloadIngest#FORMAT_JSON}. Already resolved -
 * a media type hint or {@link PayloadIngest#FORMAT_AUTO} never reaches here. Never
 * <code>null</code>
 * @param source where the payload came from: the MQTT topic, or {@code rest/<channel>} for
 * the REST adapter. This is the channel identity - the only thing that distinguishes two
 * sensors publishing the same shape - so it is never <code>null</code>: an adapter that
 * passed none is reported as {@code <unknown source>}
 * @param namespaceUri the nsURI the payload declared and that could not be resolved, or
 * <code>null</code> when the payload declared none. Only an
 * {@link IngestResult.Outcome#MODEL_UNKNOWN} payload carries one; JSON payloads declare
 * nothing
 * @param outcome how the ingest ended - one of {@link IngestResult.Outcome#MODEL_UNKNOWN},
 * {@link IngestResult.Outcome#EMPTY} or {@link IngestResult.Outcome#PARSE_ERROR}, see
 * {@link UnknownModelHandler}. Never <code>null</code>
 * @param timestamp when the payload was ingested. Recorded at the call site rather than left
 * to the handler, because the hand-off is asynchronous and a queued payload would otherwise
 * be timestamped later than it arrived
 * @author Ilenia Salvadori
 * @since 27.08.2026
 */
public record UnknownPayload(byte[] payload, String format, String source, String namespaceUri,
		IngestResult.Outcome outcome, Instant timestamp) {

	/**
	 * @throws NullPointerException if any parameter other than {@code namespaceUri} is
	 * <code>null</code>
	 */
	public UnknownPayload {
		requireNonNull(payload, "Payload must not be null");
		requireNonNull(format, "Format must not be null");
		requireNonNull(source, "Source must not be null");
		requireNonNull(outcome, "Outcome must not be null");
		requireNonNull(timestamp, "Timestamp must not be null");
	}

	/**
	 * @return the payload size in bytes, the one thing about the bytes worth logging
	 */
	public int size() {
		return payload.length;
	}

	/*
	 * (non-Javadoc)
	 * The generated toString would dump the whole payload array; this one stays loggable.
	 * @see java.lang.Record#toString()
	 */
	@Override
	public String toString() {
		return String.format("UnknownPayload[source=%s, format=%s, outcome=%s, size=%s, namespaceUri=%s, timestamp=%s]",
				source, format, outcome, payload.length, namespaceUri, timestamp);
	}

}
