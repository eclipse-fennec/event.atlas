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

import org.osgi.annotation.versioning.ConsumerType;

/**
 * Runs one agentic chat completion: the single call model inference makes, and the only thing
 * it needs from an AI stack.
 * <p>
 * <b>Why this exists rather than a direct dependency.</b> The completion this needs is
 * {@code org.eclipse.fennec.ai.chat.completion.api.ChatCompletionService.complete(String, String)},
 * and its signature is mirrored here deliberately - an implementation is a one-line delegation.
 * That bundle is not published anywhere this workspace can resolve from, and a runtime
 * dependency that no index can fetch is a build that fails in CI rather than a feature (see the
 * repository's notes on {@code central.mvn}). Keeping the seam here also keeps
 * {@code event.atlas} free of an opinion about <em>which</em> AI stack a deployment uses, the
 * same way the mapping bundle keeps its sensinact imports optional.
 * <p>
 * <b>What the implementation is expected to be.</b> Not a request/response API call: the
 * completion is an agent with tools - it discovers the models that already exist, authors a
 * package, validates it against the samples and publishes it, and only then answers. A measured
 * run is around 100 turns and two and a half minutes, so
 * {@link #complete(String, String)} may block for minutes. It is called on model inference's
 * own runner thread and never on an ingest or collector thread, and the caller applies its own
 * timeout, so the implementation does not need one.
 * <p>
 * The answer is an account of what the agent did, not a metamodel document; nothing here ever
 * transports an Ecore package. Turning whatever the provider actually returned - a structured
 * object, a line of prose - into that account is the implementation's job, which is what keeps
 * this bundle free of EMF.
 * @author Ilenia Salvadori
 * @since 27.08.2026
 */
@ConsumerType
public interface ChatCompletion {

	/**
	 * Runs one completion and reports what the agent did.
	 * @param systemMessage the instructions. Parameter is never <code>null</code>
	 * @param userMessage the task, carrying the payload samples. Parameter is never
	 * <code>null</code>
	 * @return what became of the model. Never <code>null</code>; an answer that cannot be read
	 * as an outcome is {@link InferenceOutcome.Status#UNREADABLE} rather than a failure
	 * @throws RuntimeException if the completion could not be run at all; inference records
	 * that as {@link InferenceOutcome.Status#UNAVAILABLE} and does not retry it in a storm
	 */
	InferenceOutcome complete(String systemMessage, String userMessage);

}
