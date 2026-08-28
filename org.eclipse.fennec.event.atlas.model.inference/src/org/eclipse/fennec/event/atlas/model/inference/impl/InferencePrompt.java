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
 * So this states the task, the namespace, the order of work and the absence of a filesystem, and
 * stops.
 * <p>
 * The samples travel as their raw bodies. The agent has to validate its package against every
 * one of them, and it is the payloads it must validate against - the shapes this workspace
 * derives are for deciding what to collect, not for telling an agent what it is looking at.
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
		message.add("2. Author a package for these payloads.");
		message.add("3. Validate it against every sample you were given, not just the first.");
		message.add("4. Register it, then publish it as a draft for human review.");
		message.add("");
		message.add("You have no filesystem. Everything you need is behind your tools; "
				+ "discover before you author.");
		message.add("");
		message.add("Finish your answer with exactly one line, and nothing after it:");
		message.add("RECEIPT: created <nsURI>     - you published a draft");
		message.add("RECEIPT: conflict <nsURI>    - a draft for that namespace already existed");
		message.add("RECEIPT: rejected <reason>   - you did not author a model, and why");
		return message.toString();
	}

	/**
	 * @param sampleSet the samples to infer from. Parameter must not be <code>null</code>
	 * @param namespace the namespace the agent may publish under. Parameter must not be
	 * <code>null</code>
	 * @param maxPayloadChars how much of each payload body to include
	 * @return the user message. Never <code>null</code>
	 */
	static String userMessage(PayloadSampleSet sampleSet, String namespace, int maxPayloadChars) {
		StringJoiner message = new StringJoiner("\n");
		message.add(String.format("Publish under the namespace '%s'.", namespace));
		message.add("");
		message.add(String.format("%s sample(s), collected from one channel over %s second(s).",
				sampleSet.sampleCount(), sampleSet.duration().toSeconds()));
		if (sampleSet.namespaceUri() != null) {
			// only an XMI payload declares one, and it is the model that could not be resolved
			message.add(String.format("The payloads declare the model '%s', which does not exist.",
					sampleSet.namespaceUri()));
		}
		if (sampleSet.lowEvidence()) {
			// the agent should know the set is thin, and say so rather than inventing certainty
			message.add("These are fewer samples than were asked for - the sensor is slow. "
					+ "Treat what varies between them as evidence and what does not as unproven.");
		}
		message.add("");
		message.add("Each sample stands for every payload of its shape that arrived; "
				+ "the count says how many that was.");
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
