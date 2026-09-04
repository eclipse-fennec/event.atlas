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

import java.nio.charset.StandardCharsets;
import java.util.StringJoiner;

import org.eclipse.fennec.event.atlas.southbound.sampling.PayloadSample;
import org.eclipse.fennec.event.atlas.southbound.sampling.PayloadSampleSet;

/**
 * Composes the two messages of an inference run.
 * <p>
 * <b>The prompt is short and names nothing.</b> That is a finding, not a style choice: a
 * prototype run repeatedly with tool access and no filesystem found the model family's abstract
 * base, its discriminator path, the sibling package's conventions and whether its discriminator
 * value was still free - <em>including when all of those lived only in a model atlas and nothing
 * was deployed locally</em>. Naming the family, the annotation source or which tool to call
 * produced no better model and invited the agent to skip the discovery that found those things.
 * So this states the task, the namespace prefix, the order of work and the absence of a
 * filesystem, and stops.
 * <p>
 * <b>The namespace is a prefix, and the agent completes it.</b> Handing over the configured value
 * as the whole nsURI made it one slot for one model: every run published to the same nsURI, so a
 * second device family or a re-inference after a promotion collided. What the segment should be
 * is deliberately not specified, for the reason above - the agent already derives the family and
 * the sibling conventions by discovery, and it can see how the namespaces it finds are built. The
 * cost is that the nsURI is no longer known before the run, which is what makes the receipt's
 * nsURI load-bearing rather than merely informative.
 * <p>
 * The samples travel as their raw bodies. The agent has to validate its package against every
 * one of them, and it is the payloads it must validate against - a summary cannot be
 * deserialized, and the shapes discard exactly the values (a discriminator's, a timestamp's, a
 * unit's) that decide how a field is typed.
 * <p>
 * <b>The shapes do go in front of them, though, as a {@link ShapeDelta}.</b> That reverses half
 * of an earlier decision here - that the derived shapes were "for deciding what to collect, not
 * for telling an agent what it is looking at" - and the distinction that keeps the rest of it
 * standing is worth stating. What that finding warned against was naming things the agent finds
 * better by discovery: the model family, the annotation source, which tool to call. Which paths
 * are <em>optional</em> is not one of them, because no tool on the far side can answer it - the
 * evidence exists only in this window, and an agent left to reconstruct it reads ten documents
 * in parallel and gets it wrong, then spends its remaining iterations re-modelling. The delta
 * states evidence and draws no conclusion, which is the line that separates the two cases.
 * @author Ilenia Salvadori
 * @since 27.08.2026
 */
final class InferencePrompt {

	private InferencePrompt() {
		// static use only
	}

	/**
	 * @return the system message. Never <code>null</code>
	 */
	static String systemMessage() {
		StringJoiner message = new StringJoiner("\n");
		message.add("You are given sensor payloads that a running system cannot deserialize, "
				+ "because no model for them exists yet. Your task is to produce one.");
		message.add("");
		message.add("Work in this order:");
		message.add("1. Discover what models already exist and how they are built.");
		message.add("2. Before authoring, check whether the models you found already cover part of what "
				+ "these payloads carry. Build on what exists rather than restating it.");
		message.add("3. Author a package for these payloads.");
		message.add("4. Validate it against every sample you were given, not just the first.");
		message.add("5. Register it, then publish it as a draft for human review.");
		message.add("");
		message.add("You have no filesystem. Everything you need is behind your tools; "
				+ "discover before you author.");
		message.add("");
		// Without this the agent re-declares the whole envelope as standalone classes and omits
		// whatever makes a payload recognisable - which is what happened once the payloads stopped
		// carrying a `_type` hint. Stated as an obligation on the result, deliberately naming no
		// annotation source, model family or tool: naming those suppressed the discovery that
		// found them.
		message.add("These payloads carry no indication of their own type, and future payloads of this "
				+ "family will carry none either. Whatever lets the models you discovered be recognised "
				+ "from a payload alone must therefore hold for yours too - a model that can only be "
				+ "used by someone who already knows what it is has not finished the job.");
		message.add("");
		message.add("Anything you write is your final answer and ends the run - there is no way to say "
				+ "something in passing. So write nothing at all until the work above is done: no plan, "
				+ "no progress note, no summary of what you are about to do next. Keep working.");
		message.add("");
		message.add("When you have finished, report what you did in the structure you are given: the "
				+ "status, the namespace you published under, and one sentence a human can act on. "
				+ "Report it even when the run went badly - an outcome nobody records is an outcome "
				+ "that gets paid for twice.");
		return message.toString();
	}

	/**
	 * @param sampleSet the samples to infer from. Parameter must not be <code>null</code>
	 * @param namespacePrefix the prefix the agent's namespace must start with. Parameter must not
	 * be <code>null</code>
	 * @param maxPayloadChars how much of each payload body to include
	 * @param shapeDelta whether to summarise the samples' shapes in front of their bodies
	 * @return the user message. Never <code>null</code>
	 */
	static String userMessage(PayloadSampleSet sampleSet, String namespacePrefix, int maxPayloadChars,
			boolean shapeDelta) {
		StringJoiner message = new StringJoiner("\n");
		message.add(String.format("Publish under a namespace beneath '%s'. Extend that prefix so the namespace "
				+ "identifies this model and no other - a different model published later must not land on it. "
				+ "It is the identity a reviewer promotes and a runtime resolves, so choose one you would "
				+ "choose again for these payloads.", namespacePrefix));
		message.add("");
		message.add(String.format(
				"%s differently shaped sample(s), de-duplicated from the %s payload(s) that arrived on one "
						+ "channel over %s second(s).",
				sampleSet.sampleCount(), sampleSet.payloadsSeen(), sampleSet.duration().toSeconds()));
		if (sampleSet.namespaceUri() != null) {
			// only an XMI payload declares one, and it is the model that could not be resolved
			message.add(String.format("The payloads declare the model '%s', which does not exist.",
					sampleSet.namespaceUri()));
		} else {
			// The complement of the branch above, and the ordinary case: nothing declared a model,
			// so the outcome was EMPTY rather than MODEL_UNKNOWN. Saying so is what stops the agent
			// treating the payloads as self-describing - which is the state a production payload
			// always arrives in, since only a declared-and-unresolvable nsURI sets namespaceUri.
			message.add("Nothing in these payloads states what they are; that is why no model matched "
					+ "them.");
		}
		if (sampleSet.lowEvidence()) {
			// the agent should know the set is thin, and say so rather than inventing certainty
			message.add("These are fewer samples than were asked for - the sensor is slow. "
					+ "Treat what varies between them as evidence and what does not as unproven.");
		}
		message.add("");
		message.add("Each sample stands for every payload of its shape that arrived; "
				+ "the count says how many that was.");
		String delta = shapeDelta ? ShapeDelta.describe(sampleSet) : "";
		if (!delta.isEmpty()) {
			message.add("");
			message.add(delta);
		}
		int index = 0;
		for (PayloadSample sample : sampleSet.samples()) {
			message.add("");
			message.add(String.format("--- sample %s of %s (%s, seen %s time(s), %s bytes) ---", ++index,
					sampleSet.sampleCount(), sample.format(), sample.occurrences(), sample.size()));
			message.add(body(sample, maxPayloadChars));
		}
		return message.toString();
	}

	/**
	 * The payload as text. A truncated body is marked as truncated: an agent that validates
	 * against what it thinks is a whole document would otherwise model a cut-off one.
	 */
	private static String body(PayloadSample sample, int maxPayloadChars) {
		// replacement rather than a strict decode - a broken byte must not cost the whole run
		String text = new String(sample.payload(), StandardCharsets.UTF_8);
		if (maxPayloadChars > 0 && text.length() > maxPayloadChars) {
			return text.substring(0, maxPayloadChars) + "\n… truncated, "
					+ (text.length() - maxPayloadChars) + " more character(s)";
		}
		return text;
	}

}
