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
package org.eclipse.fennec.event.atlas.southbound.sampling.impl;

import org.eclipse.fennec.event.atlas.southbound.sampling.PayloadSampleSet;

/**
 * Identifies a collection window. Two payloads share a window exactly when all three parts
 * match.
 * <p>
 * {@code source} is the channel - an MQTT topic, a REST channel - and is the part that must
 * never be shared: two sensors publishing the same shape on different topics are two models.
 * {@code namespaceUri} separates payloads that declare different unresolvable models on one
 * channel. {@code format} is not in the issue's key but belongs there for the same reason: a
 * channel configured for format detection can carry both XMI and JSON, and a sample set
 * mixing the two describes nothing an inference could author.
 * @param source the channel identity. Never <code>null</code>
 * @param namespaceUri the declared, unresolvable nsURI, or <code>null</code> when the payloads
 * declare none
 * @param format the format the payloads were read as. Never <code>null</code>
 * @author Ilenia Salvadori
 * @since 27.08.2026
 * @see PayloadSampleSet
 */
record WindowKey(String source, String namespaceUri, String format) {

	/*
	 * (non-Javadoc)
	 * @see java.lang.Record#toString()
	 */
	@Override
	public String toString() {
		return namespaceUri == null ? String.format("'%s' (%s)", source, format)
				: String.format("'%s' (%s, %s)", source, format, namespaceUri);
	}

}
