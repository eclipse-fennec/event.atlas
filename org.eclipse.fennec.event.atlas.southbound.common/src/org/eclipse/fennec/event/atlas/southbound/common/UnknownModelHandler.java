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

import org.osgi.annotation.versioning.ConsumerType;

/**
 * Optional hook offered a payload that {@link PayloadIngest} could not turn into objects
 * because this runtime has no model for it.
 * <p>
 * Ingest drops such a payload with a warning. Registering a handler as an OSGi service adds
 * a <em>side channel</em>: the bytes are additionally offered to something that may be able
 * to produce a model for them - a sample collector feeding model inference, a dead-letter
 * topic, a file the operator can inspect. Nothing about ingest changes: the
 * {@link IngestResult} the adapter sees is the same with and without a handler, and with no
 * handler deployed the runtime behaves exactly as it did before this hook existed.
 * <p>
 * <b>Which payloads arrive here.</b> Exactly the three outcomes that can be a
 * <em>missing model</em>:
 * <table border="1">
 * <caption>Outcomes offered to a handler</caption>
 * <tr><th>Outcome</th><th>Offered</th><th>Why</th></tr>
 * <tr><td>{@link IngestResult.Outcome#MODEL_UNKNOWN}</td><td>yes</td>
 * <td>the payload named an nsURI nothing can resolve, locally or in the Model Atlas</td></tr>
 * <tr><td>{@link IngestResult.Outcome#EMPTY}</td><td>yes</td>
 * <td>the payload read cleanly but yielded no objects - see below</td></tr>
 * <tr><td>{@link IngestResult.Outcome#PARSE_ERROR}</td><td>yes</td>
 * <td>may be a model problem rather than malformed data</td></tr>
 * <tr><td>{@link IngestResult.Outcome#FORMAT_UNSUPPORTED}</td><td>no</td>
 * <td>a codec bundle is missing from the deployment; no model would fix it</td></tr>
 * <tr><td>{@link IngestResult.Outcome#NO_MAPPING}</td><td>no</td>
 * <td>the model exists, the sensinact mapping does not - a different problem</td></tr>
 * <tr><td>{@link IngestResult.Outcome#PUSH_FAILED}</td><td>no</td>
 * <td>the model and the mapping both exist; the twin was unavailable</td></tr>
 * <tr><td>{@link IngestResult.Outcome#APPLIED}</td><td>no</td><td>-</td></tr>
 * </table>
 * <p>
 * {@link IngestResult.Outcome#EMPTY} is the normal case, not an edge case. Only a payload
 * that <em>declares</em> its model - in practice XMI - can reach
 * {@link IngestResult.Outcome#MODEL_UNKNOWN}. A JSON payload declares nothing: it is typed
 * by the codec's {@code typeMapping} discriminator, and one that matches no discriminator
 * makes the codec record a diagnostic and hand back an empty resource. A handler interested
 * in JSON sensors - which is the case this hook exists for - must therefore expect
 * {@link IngestResult.Outcome#EMPTY}.
 * <p>
 * <b>Implementation contract.</b> {@link #onUnknownModel(UnknownPayload)} is called on a
 * hand-off thread, never on the thread that ingested the payload, so a handler may do
 * network I/O. It still should not block indefinitely: the hand-off queue is bounded and
 * payloads offered while it is full are discarded rather than buffered. Exceptions are
 * caught and logged by ingest, so throwing costs the payload and nothing else. Ingest gives
 * no delivery guarantee whatsoever - a handler that must not lose samples has to persist
 * them itself.
 * @author Ilenia Salvadori
 * @since 27.08.2026
 */
@ConsumerType
public interface UnknownModelHandler {

	/**
	 * Offers one payload this runtime has no model for.
	 * @param payload the payload and the context needed to act on it without re-parsing.
	 * Parameter is never <code>null</code>
	 */
	void onUnknownModel(UnknownPayload payload);

}
