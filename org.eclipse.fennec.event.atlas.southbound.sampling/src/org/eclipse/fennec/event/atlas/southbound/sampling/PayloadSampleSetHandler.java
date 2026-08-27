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

import org.osgi.annotation.versioning.ConsumerType;

/**
 * Receives the closed collection windows of the payload sample collector - the seam model
 * inference is deployed behind.
 * <p>
 * Registering an implementation as an OSGi service is what gives the collected sets a
 * consumer. With none registered the collector still collects and still logs what it would
 * have handed over, which is a useful commissioning signal on its own; nothing else changes.
 * <p>
 * <b>Implementation contract.</b> {@link #onSampleSet(PayloadSampleSet)} is called on the
 * collector's own hand-over thread, never on the thread that ingested a payload, and
 * exceptions are caught and logged for you. It must still return promptly: a set can take
 * minutes of wall time to collect, so blocking here for the duration of an inference run
 * would sit in front of every other channel's set. Do the expensive work asynchronously.
 * <p>
 * A set is handed over exactly once and is never retried. A consumer that must not lose one
 * has to persist it itself.
 * @author Ilenia Salvadori
 * @since 27.08.2026
 */
@ConsumerType
public interface PayloadSampleSetHandler {

	/**
	 * Offers one closed collection window.
	 * @param sampleSet the distinctly shaped payloads of one channel, never empty. Parameter
	 * is never <code>null</code>
	 */
	void onSampleSet(PayloadSampleSet sampleSet);

}
