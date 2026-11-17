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

import java.util.Map;

/**
 * What only a southbound adapter knows about a message: the event type to stamp on it, and
 * the transport detail that goes into the envelope as extension attributes - which broker a
 * message arrived from, which client delivered it.
 * <p>
 * {@link PayloadIngest} fills everything that follows from the payload itself - the data, the
 * schema it was read as, the content type, the time - and the source is already a parameter
 * of {@code ingest}. This carries the rest, so the adapter hands over what it knows rather
 * than a built envelope: an adapter stays a transport and never has to know the envelope
 * model.
 * <p>
 * The type is a deployment choice rather than something this runtime can derive: an MQTT
 * topic carries no notion of an event type, so the value is configured in the bundle that
 * configures the transport. {@link PayloadIngest#DEFAULT_EVENT_TYPE} answers when none is
 * given, which is also what a {@code null} context means.
 * @author Ilenia Salvadori
 */
public record TransportContext(String type, Map<String, String> extensions) {

	/**
	 * @param type the CloudEvents {@code type} to stamp on the envelope. Blank or
	 * <code>null</code> falls back to {@link PayloadIngest#DEFAULT_EVENT_TYPE}
	 * @param extensions the envelope's extension attributes. <code>null</code> is an empty
	 * map; the map is copied, so a caller may reuse its own
	 */
	public TransportContext {
		extensions = extensions == null ? Map.of() : Map.copyOf(extensions);
	}

	/**
	 * @return a context carrying only an event type and no extension attributes
	 */
	public static TransportContext of(String type) {
		return new TransportContext(type, Map.of());
	}

}
