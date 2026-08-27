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
package org.eclipse.fennec.event.atlas.southbound.sampling;

import static java.util.Objects.requireNonNull;

import java.time.Instant;
import java.util.List;

import org.eclipse.fennec.event.atlas.southbound.common.IngestResult;

/**
 * One payload kept in a {@link PayloadSampleSet}, together with the shape that earned it its
 * slot.
 * <p>
 * A sample stands for every payload of the same shape that arrived while the window was open,
 * not just for one message - {@link #occurrences()} says how many there were. That is what
 * makes a set of ten samples ten pieces of <em>evidence</em> rather than ten copies of the
 * chattiest reading.
 * @param payload the raw payload bytes as the adapter received them. Never <code>null</code>.
 * Treat as read-only
 * @param format the format the payload was read as, an EMF file extension. Never
 * <code>null</code>
 * @param shape the payload's shape: the sorted, de-duplicated list of
 * <code>path:type</code> entries this payload contributes - see
 * {@code ShapeFingerprint} for the syntax. It is the admission key of the window and the
 * evidence an inference reasons over. Never <code>null</code>
 * @param outcome the ingest outcome that offered this payload up. Never <code>null</code>
 * @param timestamp when the payload was ingested. Never <code>null</code>
 * @param occurrences how many payloads of this exact shape arrived while the window was open,
 * this one included. Always at least 1
 * @author Ilenia Salvadori
 * @since 27.08.2026
 */
public record PayloadSample(byte[] payload, String format, List<String> shape, IngestResult.Outcome outcome,
		Instant timestamp, int occurrences) {

	/**
	 * @throws NullPointerException if any reference parameter is <code>null</code>
	 * @throws IllegalArgumentException if {@code occurrences} is below 1
	 */
	public PayloadSample {
		requireNonNull(payload, "Payload must not be null");
		requireNonNull(format, "Format must not be null");
		requireNonNull(outcome, "Outcome must not be null");
		requireNonNull(timestamp, "Timestamp must not be null");
		shape = List.copyOf(requireNonNull(shape, "Shape must not be null"));
		if (occurrences < 1) {
			throw new IllegalArgumentException("A sample stands for at least one payload: " + occurrences);
		}
	}

	/**
	 * @return the payload size in bytes
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
		return String.format("PayloadSample[format=%s, outcome=%s, size=%s, paths=%s, occurrences=%s, timestamp=%s]",
				format, outcome, payload.length, shape.size(), occurrences, timestamp);
	}

}
