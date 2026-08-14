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

/**
 * Outcome of a single {@link PayloadIngest#ingest(byte[], String, String)} call.
 * <p>
 * Every ingest ends in exactly one {@link Outcome}. Southbound adapters use it to decide
 * what to report back to their transport (an HTTP status, a log level, a metric); the
 * {@link #detail()} carries the one piece of context that makes the outcome actionable -
 * the unresolvable nsURI, the EClass without a mapping, the parse failure message.
 * @param outcome what happened. Never <code>null</code>
 * @param roots the number of root {@link org.eclipse.emf.ecore.EObject}s deserialized from
 * the payload; 0 unless the payload was deserialized
 * @param mappingsApplied the number of provider mappings that were successfully applied
 * across all roots; 0 unless the outcome is {@link Outcome#APPLIED}
 * @param detail a short human-readable explanation, or <code>null</code> when there is
 * nothing to add
 * @author Ilenia Salvadori
 */
public record IngestResult(Outcome outcome, int roots, int mappingsApplied, String detail) {

	/**
	 * The mutually exclusive results of an ingest attempt.
	 */
	public enum Outcome {
		/** The payload was deserialized and at least one provider mapping was applied. */
		APPLIED,
		/**
		 * The payload was deserialized, but no provider mapping is registered for the
		 * EClass of any of its roots - the data is dropped. Usually a timing or
		 * configuration issue: the mapping is not deployed, not in the atlas, or its
		 * provider classes resolved against a different EPackage instance.
		 */
		NO_MAPPING,
		/** The payload was deserialized but contained no objects at all. */
		EMPTY,
		/**
		 * The payload names a model that cannot be resolved - neither deployed locally nor
		 * obtainable from the Model Atlas - so it cannot be deserialized at all.
		 */
		MODEL_UNKNOWN,
		/** The payload is not readable in the requested format. */
		PARSE_ERROR,
		/**
		 * The payload was deserialized and a mapping matched, but pushing it into the
		 * digital twin failed as a whole (for example the sensinact gateway thread is
		 * unavailable). Unlike the other outcomes this one is worth retrying.
		 */
		PUSH_FAILED
	}

	/**
	 * @return <code>true</code> if the payload reached the digital twin
	 */
	public boolean isApplied() {
		return outcome == Outcome.APPLIED;
	}

	public static IngestResult applied(int roots, int mappingsApplied) {
		return new IngestResult(Outcome.APPLIED, roots, mappingsApplied, null);
	}

	public static IngestResult noMapping(int roots, String detail) {
		return new IngestResult(Outcome.NO_MAPPING, roots, 0, detail);
	}

	public static IngestResult empty() {
		return new IngestResult(Outcome.EMPTY, 0, 0, null);
	}

	public static IngestResult modelUnknown(String nsUri) {
		return new IngestResult(Outcome.MODEL_UNKNOWN, 0, 0, nsUri);
	}

	public static IngestResult parseError(String detail) {
		return new IngestResult(Outcome.PARSE_ERROR, 0, 0, detail);
	}

	public static IngestResult pushFailed(int roots, String detail) {
		return new IngestResult(Outcome.PUSH_FAILED, roots, 0, detail);
	}

}
