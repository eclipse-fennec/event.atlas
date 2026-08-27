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
package org.eclipse.fennec.event.atlas.model.inference.impl;

import static java.util.Objects.requireNonNull;

import java.util.Locale;

/**
 * What one inference run did, read out of the agent's final message.
 * <p>
 * The agent authors and publishes the package through its own tools, so nothing comes back here
 * but a report of what happened. The prompt asks for it as a single last line,
 * {@code RECEIPT: created <nsURI>}, and this is what reads that line back.
 * @param outcome what the run achieved. Never <code>null</code>
 * @param detail the nsURI for {@link Outcome#CREATED} and {@link Outcome#CONFLICT}, the reason
 * otherwise, or <code>null</code> when the agent gave none
 * @author Ilenia Salvadori
 * @since 27.08.2026
 */
record InferenceReceipt(Outcome outcome, String detail) {

	/** The receipt line's marker, as the prompt asks for it. */
	private static final String MARKER = "receipt:";

	/**
	 * What an inference run can end in. Only two of these are worth an operator's attention.
	 */
	enum Outcome {
		/** A draft was published for review. The point of the whole exercise. */
		CREATED,
		/**
		 * That namespace already has a draft. Entirely normal - a channel keeps handing over
		 * sample sets while its first draft waits for review - and not an error.
		 */
		CONFLICT,
		/** The agent declined to author a model, and said why. Its judgement, not a failure. */
		REJECTED,
		/**
		 * The completion could not be run or did not answer in time. The only outcome that says
		 * nothing about the payloads, and the one that must not turn into a retry storm.
		 */
		UNAVAILABLE,
		/**
		 * The agent answered, but with nothing this could read a receipt out of. Kept apart from
		 * {@link #REJECTED} on purpose: the run happened and was paid for, and what it decided
		 * is simply unknown - which is a prompt problem, not a payload one.
		 */
		UNREADABLE
	}

	InferenceReceipt {
		requireNonNull(outcome, "Outcome must not be null");
	}

	/**
	 * Reads the receipt out of an agent's answer.
	 * <p>
	 * Scans from the end, because the receipt is asked for as the last line but an agent may
	 * still add a closing remark after it, and takes the first line that carries the marker.
	 * @param answer the agent's final message. May be <code>null</code>
	 * @return the receipt. Never <code>null</code>
	 */
	static InferenceReceipt read(String answer) {
		if (answer == null || answer.isBlank()) {
			return new InferenceReceipt(Outcome.UNREADABLE, "the agent answered nothing");
		}
		String[] lines = answer.split("\\R");
		for (int i = lines.length - 1; i >= 0; i--) {
			InferenceReceipt receipt = readLine(lines[i]);
			if (receipt != null) {
				return receipt;
			}
		}
		return new InferenceReceipt(Outcome.UNREADABLE, firstLine(answer));
	}

	private static InferenceReceipt readLine(String line) {
		String trimmed = line.strip();
		int marker = trimmed.toLowerCase(Locale.ROOT).indexOf(MARKER);
		if (marker < 0) {
			return null;
		}
		String[] words = trimmed.substring(marker + MARKER.length()).strip().split("\\s+", 2);
		String detail = words.length > 1 ? words[1].strip() : null;
		return switch (words[0].toLowerCase(Locale.ROOT)) {
		case "created" -> new InferenceReceipt(Outcome.CREATED, detail);
		case "conflict" -> new InferenceReceipt(Outcome.CONFLICT, detail);
		case "rejected" -> new InferenceReceipt(Outcome.REJECTED, detail);
		// a marker with something else after it is not a receipt this can act on
		default -> new InferenceReceipt(Outcome.UNREADABLE, trimmed);
		};
	}

	private static String firstLine(String answer) {
		String first = answer.strip().split("\\R", 2)[0].strip();
		return first.length() > 200 ? first.substring(0, 200) + "…" : first;
	}

}
