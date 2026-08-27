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
package org.eclipse.fennec.event.atlas.model.inference.chat;

import java.io.IOException;
import java.time.Duration;
import java.time.Instant;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.eclipse.emf.ecore.EObject;
import org.eclipse.fennec.ai.chat.completion.api.ChatCompletionService;
import org.eclipse.fennec.event.atlas.model.inference.ChatCompletion;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

/**
 * Binds model inference's {@link ChatCompletion} port to a Fennec AI
 * {@link ChatCompletionService}.
 * <p>
 * This bundle exists so that exactly one bundle in the workspace depends on an AI stack. A
 * runtime that does not want model inference does not deploy it; a runtime that wants it with a
 * different provider replaces it. The inference bundle itself keeps compiling and testing
 * against its own port, and keeps its manifest free of EMF - which is what makes "it can never
 * register a package into the running system" a property rather than a promise.
 * <p>
 * The completion is called synchronously here on purpose: the port documents that a call may
 * block for minutes, and it is invoked on model inference's own call thread with its own
 * timeout. The service also offers a {@code completeAsync} returning a {@code Promise}; there is
 * nothing to gain from it while the caller is a dedicated thread that is going to wait anyway.
 * @author Ilenia Salvadori
 * @since 27.08.2026
 */
@Component
public class ChatCompletionAdapter implements ChatCompletion {

	private static final Logger logger = Logger.getLogger(ChatCompletionAdapter.class.getName());

	@Reference
	private ChatCompletionService chatCompletionService;

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.fennec.event.atlas.model.inference.ChatCompletion#complete(java.lang.String, java.lang.String)
	 */
	@Override
	public String complete(String systemMessage, String userMessage) {
		Instant started = Instant.now();
		EObject response;
		try {
			response = chatCompletionService.complete(systemMessage, userMessage);
		} catch (IOException e) {
			// The port's contract: an unchecked throw is what inference records as an
			// unavailable receipt, which is exactly what a transport failure is.
			throw new IllegalStateException("The chat completion could not be reached: " + describe(e), e);
		} catch (RuntimeException e) {
			// The client answers a non-2xx by failing to deserialize the body - an HTTP error
			// payload is not a completion response - so this is where a rejected API key or a
			// wrong endpoint arrives, with a message that says nothing about either. Name the
			// two suspects here: the alternative is an operator reading "not of expected type"
			// off an unavailable receipt and going looking for a model problem.
			throw new IllegalStateException(String.format(
					"The chat completion failed and its answer could not be read (%s). An HTTP error from the "
							+ "provider looks exactly like this - check that api.key is set in the environment and "
							+ "that base.url points at the provider's messages endpoint.",
					describe(e)), e);
		}
		String usage = AnswerText.usageOf(response);
		logger.log(Level.INFO, () -> String.format("The chat completion answered in %ss%s",
				Duration.between(started, Instant.now()).toSeconds(), usage == null ? "" : " (" + usage + ")"));
		return AnswerText.of(response);
	}

	private static String describe(Throwable e) {
		return e.getMessage() == null ? e.getClass().getSimpleName() : e.getMessage();
	}

}
