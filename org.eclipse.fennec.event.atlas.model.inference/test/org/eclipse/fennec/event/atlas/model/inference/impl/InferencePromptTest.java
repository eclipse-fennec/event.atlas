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

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.List;

import org.eclipse.fennec.event.atlas.southbound.common.IngestResult.Outcome;
import org.eclipse.fennec.event.atlas.southbound.common.PayloadIngest;
import org.eclipse.fennec.event.atlas.southbound.sampling.PayloadSample;
import org.eclipse.fennec.event.atlas.southbound.sampling.PayloadSampleSet;
import org.eclipse.fennec.event.atlas.southbound.sampling.PayloadSampleSet.CloseReason;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/**
 * Unit tests for {@link InferencePrompt}. The prompt that works is short and names nothing - so
 * what is asserted here is mostly that the few things it <em>must</em> carry are there.
 * @author Ilenia Salvadori
 */
public class InferencePromptTest {

	@Test
	@DisplayName("The system message states the order of work, the absence of a filesystem and the report")
	void systemMessage_statesTheTaskAndTheReport() {
		String message = InferencePrompt.systemMessage();

		assertTrue(message.contains("Discover"), "Discovery comes first, and is what found the family");
		assertTrue(message.contains("Validate it against every sample"));
		assertTrue(message.contains("publish it as a draft for human review"));
		assertTrue(message.contains("no filesystem"));
		assertTrue(message.contains("report what you did"), "Without an outcome a run's result is unknown");
		assertTrue(message.contains("ends the run"),
				"Measured 2026-08-31: with a schema set, a progress note IS the final answer - the agent "
						+ "stopped after discovery having said 'Now authoring the Dragino LSE01 package'");
		assertTrue(message.contains("even when the run went badly"),
				"The outcomes worth having are the ones an agent would rather not report");
	}

	@Test
	@DisplayName("The system message no longer dictates a receipt line")
	// The outcome is a schema now. Asking for a line of prose as well is a contradictory
	// instruction, and the prose parser survives only as the adapter's fallback.
	void systemMessage_doesNotAskForAReceiptLine() {
		assertFalse(InferencePrompt.systemMessage().contains("RECEIPT:"),
				"A schema and a marker line are two answers to one question");
	}

	@Test
	@DisplayName("The system message says nothing about a codec type map")
	// The prompt names nothing the agent can discover for itself, and how this runtime types a
	// JSON payload is not something it has to be told.
	void systemMessage_saysNothingAboutACodecTypeMap() {
		assertFalse(InferencePrompt.systemMessage().contains("codec type map"),
				"An instruction the agent cannot act on is worse than none");
	}

	@Test
	@DisplayName("The user message names the namespace and carries every sample body")
	void userMessage_carriesTheNamespaceAndTheSamples() {
		PayloadSampleSet set = set(false, sample("{\"temp\":21.5}", 3), sample("{\"temp\":21}", 1));

		String message = InferencePrompt.userMessage(set, "https://example.org/inferred", 4096, true);

		assertTrue(message.contains("'https://example.org/inferred'"));
		assertTrue(message.contains("{\"temp\":21.5}"), "The agent validates against the payloads themselves");
		assertTrue(message.contains("{\"temp\":21}"));
		assertTrue(message.contains("sample 1 of 2"));
		assertTrue(message.contains("seen 3 time(s)"), "How often a shape arrived is evidence");
	}

	@Test
	@DisplayName("The user message says how many payloads the samples were de-duplicated from")
	// The delta block counts optional paths against that number, and it appeared nowhere else -
	// an unexplained denominator is what made '3 of 513' read as three of four samples.
	void userMessage_statesHowManyPayloadsArrived() {
		PayloadSampleSet set = new PayloadSampleSet("sensors/a", null, PayloadIngest.FORMAT_JSON,
				List.of(sample("{\"temp\":21.5}", 400), sample("{\"temp\":21}", 13)), CloseReason.NO_NEW_SHAPES,
				413, Instant.now(), Instant.now());

		String message = InferencePrompt.userMessage(set, "urn:x", 4096, true);

		assertTrue(message.contains("2 differently shaped sample(s), de-duplicated from the 413 payload(s)"),
				message);
	}

	@Test
	@DisplayName("The user message summarises the shapes in front of the payloads, not instead of them")
	// A delta cannot be deserialized and carries no values, so it goes before the bodies and
	// never replaces them - see ShapeDelta for why it is there at all.
	void userMessage_carriesTheShapeDeltaBeforeTheBodies() {
		PayloadSampleSet set = set(false, sample("{\"temp\":21.5}", 3), sample("{\"temp\":21}", 1));

		String message = InferencePrompt.userMessage(set, "urn:x", 4096, true);

		assertTrue(message.contains("Paths in all 2 samples:"), message);
		assertTrue(message.indexOf("Paths in all 2 samples:") < message.indexOf("--- sample 1 of 2"),
				"The summary is a way into the payloads: " + message);
		assertTrue(message.contains("{\"temp\":21.5}"), "The bodies still travel in full");
	}

	@Test
	@DisplayName("The shape summary can be switched off, and then nothing of it remains")
	// Both arms of a measurement have to come off one jar: a rebuild between runs cannot promise
	// that the prompt differs in the block and in nothing else.
	void userMessage_omitsTheShapeDeltaWhenDisabled() {
		PayloadSampleSet set = set(false, sample("{\"temp\":21.5}", 3), sample("{\"temp\":21}", 1));

		String off = InferencePrompt.userMessage(set, "urn:x", 4096, false);
		String on = InferencePrompt.userMessage(set, "urn:x", 4096, true);

		assertFalse(off.contains("Paths in all"), off);
		assertFalse(off.contains("genuinely absent"), "No trace of the block, not just the headings");
		assertTrue(off.contains("{\"temp\":21.5}"), "The payloads are not what the switch controls");
		assertTrue(off.contains("de-duplicated from the 2 payload(s)"), "Nor is the population line");
		assertTrue(on.length() > off.length(), "The only difference is the block itself");
	}

	@Test
	@DisplayName("The namespace is asked for as a prefix to extend, not as the nsURI to use")
	// Handing the configured value over as the whole nsURI made it one slot for one model: every
	// run published to the same nsURI, so a second device family collided with the first.
	void userMessage_asksForANamespaceBeneathThePrefix() {
		String message = InferencePrompt.userMessage(set(false, sample("{}", 1)), "https://example.org/inferred",
				4096, true);

		assertTrue(message.contains("beneath 'https://example.org/inferred'"),
				"The prefix has to be stated as a prefix: " + message);
		assertTrue(message.contains("identifies this model and no other"),
				"Why it must be extended is the part the agent has to act on");
	}

	@Test
	@DisplayName("The prompt does not say what the appended segment should be")
	// Same finding as the codec type map: the agent derives the family and the sibling
	// conventions by discovery, and naming a scheme invites it to skip the discovery that would
	// have found the right one.
	void userMessage_doesNotPrescribeANamingScheme() {
		String message = InferencePrompt.userMessage(set(false, sample("{}", 1)), "https://example.org/inferred",
				4096, true);

		assertFalse(message.contains("discriminator"), "That is the agent's to find, not ours to dictate");
		assertFalse(message.contains("device family"));
	}

	@Test
	@DisplayName("A low-evidence set tells the agent its evidence is thin")
	void userMessage_flagsLowEvidence() {
		String thin = InferencePrompt.userMessage(set(true, sample("{\"temp\":21.5}", 1)), "urn:x", 4096, true);
		String full = InferencePrompt.userMessage(set(false, sample("{\"temp\":21.5}", 1)), "urn:x", 4096, true);

		assertTrue(thin.contains("fewer samples than were asked for"));
		assertFalse(full.contains("fewer samples than were asked for"));
	}

	@Test
	@DisplayName("A declared but unresolvable model is stated")
	void userMessage_statesTheDeclaredModel() {
		PayloadSampleSet set = new PayloadSampleSet("sensors/a", "http://example.org/nowhere/1.0",
				PayloadIngest.FORMAT_XMI, List.of(sample("<S/>", 1)), CloseReason.TARGET_REACHED, 1, Instant.now(),
				Instant.now());

		assertTrue(InferencePrompt.userMessage(set, "urn:x", 4096, true).contains("http://example.org/nowhere/1.0"));
	}

	@Test
	@DisplayName("Payloads that declare nothing are said to declare nothing")
	// The production case: only a declared-and-unresolvable nsURI sets namespaceUri, so a real
	// payload always lands here. Left unsaid, the agent treats the payloads as self-describing and
	// authors a model that cannot type them - measured 2026-09-04.
	void userMessage_saysWhenNothingIsDeclared() {
		String message = InferencePrompt.userMessage(set(false, sample("{\"temp\":21.5}", 1)), "urn:x", 4096, true);

		assertTrue(message.contains("Nothing in these payloads states what they are"));
	}

	@Test
	@DisplayName("The two declaration branches are mutually exclusive")
	// Emitting both would contradict itself: one says a model was declared, the other that none was.
	void userMessage_declarationBranchesDoNotBothFire() {
		PayloadSampleSet declared = new PayloadSampleSet("sensors/a", "http://example.org/nowhere/1.0",
				PayloadIngest.FORMAT_XMI, List.of(sample("<S/>", 1)), CloseReason.TARGET_REACHED, 1, Instant.now(),
				Instant.now());

		assertFalse(InferencePrompt.userMessage(declared, "urn:x", 4096, true)
				.contains("Nothing in these payloads states what they are"));
		assertFalse(InferencePrompt.userMessage(set(false, sample("{\"temp\":21.5}", 1)), "urn:x", 4096, true)
				.contains("which does not exist"));
	}

	@Test
	@DisplayName("The system message requires a self-typing model, without naming the mechanism")
	// It must state the obligation and not the means: naming the annotation source, the model
	// family or a tool suppressed the discovery that found them.
	void systemMessage_requiresASelfTypingModel() {
		String message = InferencePrompt.systemMessage();

		assertTrue(message.contains("recognised from a payload alone"));
		assertTrue(message.contains("Build on what exists rather than restating it"));
		assertFalse(message.contains("typeDiscriminator"));
		assertFalse(message.contains("typeMapping"));
		assertFalse(message.contains("add_eannotation"));
	}

	@Test
	@DisplayName("A truncated payload is marked as truncated")
	// An agent that validates against what it thinks is a whole document would model a cut-off one.
	void userMessage_marksTruncation() {
		String message = InferencePrompt.userMessage(set(false, sample("{\"a\":\"" + "x".repeat(200) + "\"}", 1)),
				"urn:x", 50, true);

		assertTrue(message.contains("… truncated"));
		assertTrue(message.contains("more character(s)"));
	}

	private static PayloadSample sample(String body, int occurrences) {
		return new PayloadSample(body.getBytes(StandardCharsets.UTF_8), PayloadIngest.FORMAT_JSON,
				List.of("temp:float"), Outcome.EMPTY, Instant.now(), occurrences);
	}

	private static PayloadSampleSet set(boolean lowEvidence, PayloadSample... samples) {
		return new PayloadSampleSet("sensors/a", null, PayloadIngest.FORMAT_JSON, List.of(samples),
				lowEvidence ? CloseReason.MAX_WAIT : CloseReason.TARGET_REACHED, samples.length, Instant.now(),
				Instant.now());
	}

}
