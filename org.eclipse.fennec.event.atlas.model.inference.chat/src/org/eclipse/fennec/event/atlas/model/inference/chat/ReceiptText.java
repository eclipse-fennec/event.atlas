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

import java.util.Locale;

import org.eclipse.fennec.event.atlas.model.inference.InferenceOutcome;
import org.eclipse.fennec.event.atlas.model.inference.InferenceOutcome.Status;

/**
 * Reads an outcome out of an agent's prose, for the paths where there is no structured answer to
 * read instead.
 * <p>
 * This is the fallback, not the contract. It exists for three cases: the synchronous adapter,
 * which cannot finish an agentic run anyway and is not worth a schema; a turn that exhausted its
 * continuations, which has text but never reached its structured answer; and a provider that
 * returns something the schema cannot be recovered from. Everywhere else the agent answers in
 * {@code InferenceResult} and this is not consulted.
 * <p>
 * It scans from the end, because the receipt is the last thing an agent says but not always the
 * last line it writes, and takes the first line carrying the marker.
 * @author Ilenia Salvadori
 */
final class ReceiptText {

	private static final String MARKER = "receipt:";

	private ReceiptText() {
		// static use only
	}

	/**
	 * @param answer the agent's final message. May be <code>null</code>
	 * @return what the text says the agent did. Never <code>null</code>
	 */
	static InferenceOutcome read(String answer) {
		if (answer == null || answer.isBlank()) {
			return InferenceOutcome.of(Status.UNREADABLE, "the agent answered nothing");
		}
		String[] lines = answer.split("\\R");
		for (int i = lines.length - 1; i >= 0; i--) {
			InferenceOutcome outcome = readLine(lines[i]);
			if (outcome != null) {
				return outcome;
			}
		}
		return InferenceOutcome.of(Status.UNREADABLE, firstLine(answer));
	}

	private static InferenceOutcome readLine(String line) {
		String trimmed = line.strip();
		int marker = trimmed.toLowerCase(Locale.ROOT).indexOf(MARKER);
		if (marker < 0) {
			return null;
		}
		String[] words = trimmed.substring(marker + MARKER.length()).strip().split("\\s+", 2);
		String detail = words.length > 1 ? words[1].strip() : null;
		return switch (words[0].toLowerCase(Locale.ROOT)) {
		case "created", "published" -> new InferenceOutcome(Status.PUBLISHED, detail, detail);
		case "conflict", "exists" -> new InferenceOutcome(Status.ALREADY_EXISTS, detail, detail);
		case "rejected" -> InferenceOutcome.of(Status.NOT_INFERRED, detail);
		// a marker with something else after it is not an outcome this can act on
		default -> InferenceOutcome.of(Status.UNREADABLE, trimmed);
		};
	}

	private static String firstLine(String answer) {
		String first = answer.strip().split("\\R", 2)[0].strip();
		return first.length() > 200 ? first.substring(0, 200) + "…" : first;
	}

}
