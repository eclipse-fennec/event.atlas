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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.eclipse.fennec.event.atlas.model.inference.impl.InferenceReceipt.Outcome;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/**
 * Unit tests for {@link InferenceReceipt}. The agent publishes through its own tools, so the
 * receipt line is the only thing that says what a run - a hundred turns and minutes of work -
 * actually did.
 * @author Ilenia Salvadori
 */
public class InferenceReceiptTest {

	@Test
	@DisplayName("A created receipt carries the namespace the draft was published under")
	void created_isReadWithItsNsUri() {
		InferenceReceipt receipt = InferenceReceipt
				.read("I discovered the family, authored the package and published it.\n"
						+ "RECEIPT: created https://example.org/dragino/1.0");

		assertEquals(Outcome.CREATED, receipt.outcome());
		assertEquals("https://example.org/dragino/1.0", receipt.detail());
	}

	@Test
	@DisplayName("A conflict is read as its own outcome, not as a failure")
	// A channel keeps handing over sample sets while its first draft waits for review, so this
	// is the normal steady state of a model that has already been inferred.
	void conflict_isReadAsItsOwnOutcome() {
		InferenceReceipt receipt = InferenceReceipt.read("RECEIPT: conflict https://example.org/dragino/1.0");

		assertEquals(Outcome.CONFLICT, receipt.outcome());
		assertEquals("https://example.org/dragino/1.0", receipt.detail());
	}

	@Test
	@DisplayName("A rejection keeps the agent's reason")
	void rejected_keepsTheReason() {
		InferenceReceipt receipt = InferenceReceipt
				.read("RECEIPT: rejected the samples share no field that could type them");

		assertEquals(Outcome.REJECTED, receipt.outcome());
		assertEquals("the samples share no field that could type them", receipt.detail());
	}

	@Test
	@DisplayName("The marker is found whatever case it is written in and whatever follows it")
	// The receipt is asked for as the last line, but an agent may still add a closing remark.
	void marker_isCaseInsensitiveAndNeedNotBeTheLastLine() {
		InferenceReceipt receipt = InferenceReceipt.read("""
				Done.
				receipt: Created https://example.org/a/1.0
				Let me know if you would like me to revisit the units.""");

		assertEquals(Outcome.CREATED, receipt.outcome());
		assertEquals("https://example.org/a/1.0", receipt.detail());
	}

	@Test
	@DisplayName("The last receipt line wins when the agent restates it")
	void lastReceiptLine_wins() {
		InferenceReceipt receipt = InferenceReceipt.read("""
				RECEIPT: rejected not enough evidence
				On reflection I could model it after all.
				RECEIPT: created https://example.org/b/1.0""");

		assertEquals(Outcome.CREATED, receipt.outcome());
	}

	@Test
	@DisplayName("An answer without a receipt is unreadable, not a rejection")
	// The run happened and was paid for; what it decided is simply unknown, which is a prompt
	// problem rather than a statement about the payloads.
	void answerWithoutAReceipt_isUnreadable() {
		InferenceReceipt receipt = InferenceReceipt.read("I have published the package, all good!");

		assertEquals(Outcome.UNREADABLE, receipt.outcome());
		assertEquals("I have published the package, all good!", receipt.detail());
	}

	@Test
	@DisplayName("A receipt naming something unknown is unreadable")
	void unknownVerb_isUnreadable() {
		assertEquals(Outcome.UNREADABLE, InferenceReceipt.read("RECEIPT: published https://example.org/a").outcome());
	}

	@Test
	@DisplayName("No answer at all is unreadable and says so")
	void noAnswer_isUnreadable() {
		assertEquals(Outcome.UNREADABLE, InferenceReceipt.read(null).outcome());
		assertEquals(Outcome.UNREADABLE, InferenceReceipt.read("   ").outcome());
		assertEquals("the agent answered nothing", InferenceReceipt.read(null).detail());
	}

	@Test
	@DisplayName("A receipt with no detail is still read")
	void receiptWithoutDetail_isStillRead() {
		InferenceReceipt receipt = InferenceReceipt.read("RECEIPT: rejected");

		assertEquals(Outcome.REJECTED, receipt.outcome());
		assertNull(receipt.detail());
	}

	@Test
	@DisplayName("A very long unreadable answer is truncated for the log")
	void unreadableAnswer_isTruncated() {
		String detail = InferenceReceipt.read("x".repeat(500)).detail();

		assertTrue(detail.length() < 250, "A whole answer must not end up in one log line");
		assertTrue(detail.endsWith("…"));
	}

}
