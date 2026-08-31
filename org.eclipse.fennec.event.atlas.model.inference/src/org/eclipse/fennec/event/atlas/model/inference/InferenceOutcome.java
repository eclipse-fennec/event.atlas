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
package org.eclipse.fennec.event.atlas.model.inference;

import static java.util.Objects.requireNonNull;

/**
 * What one inference run did.
 * <p>
 * This is the whole answer model inference takes from a completion. It carries no EMF type on
 * purpose: the structured answer an agent returns <em>is</em> an EMF object, but turning it into
 * this is the adapter's job, which is what keeps this bundle free of EMF and therefore incapable
 * of registering an inferred package into the running framework.
 * <p>
 * Four of the statuses are the agent's own account of what it did. The remaining two are the
 * runtime's, for the cases where there is no account to read - and they are the reason this is
 * not simply the generated model type.
 *
 * @param status what became of the model. Never <code>null</code>
 * @param nsUri the namespace the draft was published under, where there is one. May be
 * <code>null</code>
 * @param message one line for a human reading the log. May be <code>null</code>
 * @author Ilenia Salvadori
 */
public record InferenceOutcome(Status status, String nsUri, String message) {

	public InferenceOutcome {
		requireNonNull(status, "Status must not be null");
	}

	public enum Status {

		/** A draft was published for review. The point of the whole exercise. */
		PUBLISHED,
		/**
		 * That namespace already had a draft, so nothing was published. Entirely normal - a
		 * channel keeps handing over sample sets while its first draft waits for review - and
		 * not an error.
		 */
		ALREADY_EXISTS,
		/**
		 * A model was authored but could not be published: a tool failed, a namespace was
		 * refused. The only outcome of the four that says nothing about the payloads, so it is
		 * the only one worth trying again.
		 */
		NOT_PUBLISHED,
		/** The agent declined to author a model, and said why. Its judgement, not a failure. */
		NOT_INFERRED,
		/**
		 * The completion could not be run or did not answer in time. Says nothing about the
		 * payloads either, and is the one that must not turn into a retry storm - so unlike
		 * {@link #NOT_PUBLISHED} it is retried only after a delay.
		 */
		UNAVAILABLE,
		/**
		 * The agent answered, but with nothing that could be read as an outcome. Kept apart from
		 * {@link #NOT_INFERRED} on purpose: the run happened and was paid for, and what it
		 * decided is simply unknown - which is a prompt or a schema problem, not a payload one.
		 */
		UNREADABLE;

		/**
		 * @return <code>true</code> if the same payloads are worth inferring again. A decision
		 * about the payloads stands; a failure to carry it out does not.
		 */
		public boolean isRetryable() {
			return this == UNAVAILABLE || this == NOT_PUBLISHED;
		}
	}

	/** @return an outcome with no namespace, for the statuses that cannot name one */
	public static InferenceOutcome of(Status status, String message) {
		return new InferenceOutcome(status, null, message);
	}
}
