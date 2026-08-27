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

import java.util.List;
import java.util.StringJoiner;
import java.util.logging.Logger;

import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.EStructuralFeature;

/**
 * Reads the agent's final message out of a chat-completion response.
 * <p>
 * Done reflectively, over {@code content} blocks carrying a {@code text}, rather than against a
 * provider's response class. A completion response is a generated EMF object whose concrete
 * type is per-provider - the shared {@code ChatCompletionResponse} is an empty marker interface
 * - so casting would pin this bundle to one provider's model bundle for the sake of one string.
 * The block layout it walks (a response with many content blocks, each optionally carrying
 * text) is the shape of the Messages API itself, not of one client.
 * <p>
 * Anything else - tool-use blocks, thinking blocks - is skipped: they carry no {@code text}
 * feature. What is left is what the agent said, which is where the receipt line lives.
 * @author Ilenia Salvadori
 * @since 27.08.2026
 */
final class AnswerText {

	private static final Logger logger = Logger.getLogger(AnswerText.class.getName());

	/** The many-valued feature holding the response's blocks. */
	private static final String CONTENT = "content";

	/** The text of a single block, and the fallback feature on a flat response. */
	private static final String TEXT = "text";

	private AnswerText() {
		// static use only
	}

	/**
	 * @param response the completion response. May be <code>null</code>
	 * @return the agent's message, or <code>null</code> if the response carries none - which
	 * model inference records as an unreadable receipt rather than as a failure
	 */
	static String of(EObject response) {
		if (response == null) {
			return null;
		}
		String fromBlocks = fromContentBlocks(response);
		if (fromBlocks != null) {
			return fromBlocks;
		}
		// a provider whose response carries the message directly rather than in blocks
		String flat = stringFeature(response, TEXT);
		if (flat == null) {
			logger.warning(String.format(
					"Cannot read the agent's answer from a %s - it carries neither text content blocks nor a text "
							+ "attribute",
					response.eClass().getName()));
		}
		return flat;
	}

	private static String fromContentBlocks(EObject response) {
		EStructuralFeature content = response.eClass().getEStructuralFeature(CONTENT);
		if (content == null || !content.isMany()) {
			return null;
		}
		Object blocks = response.eGet(content);
		if (!(blocks instanceof List<?> list)) {
			return null;
		}
		// several text blocks in one answer are one message split up, so they are joined
		StringJoiner text = new StringJoiner("\n");
		for (Object block : list) {
			if (block instanceof EObject eObject) {
				String blockText = stringFeature(eObject, TEXT);
				if (blockText != null) {
					text.add(blockText);
				}
			}
		}
		return text.length() == 0 ? null : text.toString();
	}

	/**
	 * @return the value of a string feature, or <code>null</code> if the class has no such
	 * feature, it is not a single string, or it is unset
	 */
	private static String stringFeature(EObject object, String name) {
		EStructuralFeature feature = object.eClass().getEStructuralFeature(name);
		if (feature == null || feature.isMany()) {
			return null;
		}
		Object value = object.eGet(feature);
		return value instanceof String string && !string.isBlank() ? string : null;
	}

	/**
	 * Reports the token usage of a run, when the response carries it.
	 * <p>
	 * Worth a log line of its own: the cost of an inference run is dominated by the request
	 * prefix - the tool definitions an MCP server re-sends on every one of a hundred turns - so
	 * the input-token count is the number that tells an operator whether the deployment's tool
	 * allow-list is doing its job.
	 * @param response the completion response. May be <code>null</code>
	 * @return a short description of the usage, or <code>null</code> if the response reports
	 * none
	 */
	static String usageOf(EObject response) {
		if (response == null) {
			return null;
		}
		EStructuralFeature usageFeature = response.eClass().getEStructuralFeature("usage");
		if (usageFeature == null || usageFeature.isMany() || !(response.eGet(usageFeature) instanceof EObject usage)) {
			return null;
		}
		StringJoiner reported = new StringJoiner(", ");
		for (EStructuralFeature feature : usage.eClass().getEAllStructuralFeatures()) {
			Object value = usage.eGet(feature);
			if (value instanceof Number number && number.longValue() > 0) {
				reported.add(feature.getName() + "=" + number);
			}
		}
		return reported.length() == 0 ? null : reported.toString();
	}

}
