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
	@DisplayName("The system message states the order of work, the absence of a filesystem and the receipt")
	void systemMessage_statesTheTaskAndTheReceipt() {
		String message = InferencePrompt.systemMessage();

		assertTrue(message.contains("Discover"), "Discovery comes first, and is what found the family");
		assertTrue(message.contains("Validate it against every sample"));
		assertTrue(message.contains("publish it as a draft for human review"));
		assertTrue(message.contains("no filesystem"));
		assertTrue(message.contains("RECEIPT: created <nsURI>"), "Without the receipt a run's outcome is unknown");
		assertTrue(message.contains("RECEIPT: conflict <nsURI>"));
		assertTrue(message.contains("RECEIPT: rejected <reason>"));
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

		String message = InferencePrompt.userMessage(set, "https://example.org/inferred", 4096);

		assertTrue(message.contains("'https://example.org/inferred'"));
		assertTrue(message.contains("{\"temp\":21.5}"), "The agent validates against the payloads themselves");
		assertTrue(message.contains("{\"temp\":21}"));
		assertTrue(message.contains("sample 1 of 2"));
		assertTrue(message.contains("seen 3 time(s)"), "How often a shape arrived is evidence");
	}

	@Test
	@DisplayName("A low-evidence set tells the agent its evidence is thin")
	void userMessage_flagsLowEvidence() {
		String thin = InferencePrompt.userMessage(set(true, sample("{\"temp\":21.5}", 1)), "urn:x", 4096);
		String full = InferencePrompt.userMessage(set(false, sample("{\"temp\":21.5}", 1)), "urn:x", 4096);

		assertTrue(thin.contains("fewer samples than were asked for"));
		assertFalse(full.contains("fewer samples than were asked for"));
	}

	@Test
	@DisplayName("A declared but unresolvable model is stated")
	void userMessage_statesTheDeclaredModel() {
		PayloadSampleSet set = new PayloadSampleSet("sensors/a", "http://example.org/nowhere/1.0",
				PayloadIngest.FORMAT_XMI, List.of(sample("<S/>", 1)), CloseReason.TARGET_REACHED, 1, Instant.now(),
				Instant.now());

		assertTrue(InferencePrompt.userMessage(set, "urn:x", 4096).contains("http://example.org/nowhere/1.0"));
	}

	@Test
	@DisplayName("A truncated payload is marked as truncated")
	// An agent that validates against what it thinks is a whole document would model a cut-off one.
	void userMessage_marksTruncation() {
		String message = InferencePrompt.userMessage(set(false, sample("{\"a\":\"" + "x".repeat(200) + "\"}", 1)),
				"urn:x", 50);

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
