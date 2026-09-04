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
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.IntStream;

import org.eclipse.fennec.event.atlas.southbound.common.IngestResult.Outcome;
import org.eclipse.fennec.event.atlas.southbound.common.PayloadIngest;
import org.eclipse.fennec.event.atlas.southbound.sampling.PayloadSample;
import org.eclipse.fennec.event.atlas.southbound.sampling.PayloadSampleSet;
import org.eclipse.fennec.event.atlas.southbound.sampling.PayloadSampleSet.CloseReason;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/**
 * Unit tests for {@link ShapeDelta}. What is asserted is the evidence the block has to carry:
 * which samples a path was in, which kinds of value it held, and how many payloads stood behind
 * it - plus the guarantee that makes an absence mean anything.
 * @author Ilenia Salvadori
 */
public class ShapeDeltaTest {

	@Test
	@DisplayName("A path missing from a sample is reported as missing from that sample")
	// The whole point: with the bodies alone the agent has to derive this by reading ten
	// documents in parallel, and the run that got it wrong spent its remaining iterations
	// re-modelling what it had already built.
	void describe_separatesTheSharedPathsFromTheVaryingOnes() {
		PayloadSampleSet set = set(4, sample(3, "deviceInfo.devEui:string", "object.temperature:float"),
				sample(1, "deviceInfo.devEui:string", "object.temperature:float", "object.humidity:float"));

		String delta = ShapeDelta.describe(set);

		assertTrue(delta.contains("Paths in all 2 samples:"), delta);
		assertTrue(delta.contains("  deviceInfo.devEui: string"), delta);
		assertTrue(delta.contains("  object.temperature: float"), delta);
		assertTrue(delta.contains("Paths in some samples only, counted against the 4 payload(s) the window saw:"),
				delta);
		assertTrue(delta.contains("  object.humidity: float (sample 2; 1 payload(s))"), delta);
	}

	@Test
	@DisplayName("The block states that an absence is real, not something the collector dropped")
	// Every conclusion the agent can draw from the block rests on this, and it is the one thing
	// it cannot see for itself: a sample is a payload that arrived.
	void describe_statesThatAnAbsenceIsEvidence() {
		String delta = ShapeDelta.describe(set(2, sample(1, "a:int"), sample(1, "a:int", "b:int")));

		assertTrue(delta.contains("genuinely absent"), delta);
	}

	@Test
	@DisplayName("One path with two value kinds names the samples that showed each")
	// int beside float is the whole reason a set of samples can type a field that one payload
	// cannot - ShapeFingerprint keeps them apart for exactly this.
	void describe_reportsDisagreeingValueKinds() {
		String delta = ShapeDelta.describe(set(2, sample(1, "object.temp:int"), sample(1, "object.temp:float")));

		assertTrue(delta.contains("  object.temp: float in sample 2; int in sample 1"), delta);
	}

	@Test
	@DisplayName("The payload count is the window's arrivals, not the sample count")
	// Read against the sample count, '3 payload(s)' out of four samples reads as three quarters
	// of the evidence when it is in fact 0.6% of it - so the population is named once, in the
	// heading, and the per-path number is a plain count.
	void describe_namesThePopulationItCountsAgainst() {
		String delta = ShapeDelta.describe(set(513, sample(509, "a:int"), sample(4, "a:int", "b:int")));

		assertTrue(delta.contains("counted against the 513 payload(s) the window saw"), delta);
		assertTrue(delta.contains("  b: int (sample 2; 4 payload(s))"), delta);
	}

	@Test
	@DisplayName("A varying path is weighted by the payloads behind it, not by the samples")
	// A path missing only from a shape seen once is far weaker evidence than one missing from a
	// shape seen four hundred times, and the sample count alone hides that.
	void describe_weightsAPathByItsPayloads() {
		PayloadSampleSet set = set(405, sample(400, "x:int"), sample(5, "x:int", "y:int"));

		assertTrue(ShapeDelta.describe(set).contains("  y: int (sample 2; 5 payload(s))"),
				ShapeDelta.describe(set));
	}

	@Test
	@DisplayName("An untyped format reports paths and claims no value kinds")
	// An XML or XMI document states no types; for an XMI payload they live in the model this
	// runtime is missing, which is the reason there is an inference at all.
	void describe_reportsXmlPathsWithoutTypes() {
		PayloadSampleSet set = new PayloadSampleSet("sensors/a", "http://example.org/nowhere/1.0",
				PayloadIngest.FORMAT_XMI,
				List.of(sample(1, "Sensor", "Sensor/Reading@value"),
						sample(1, "Sensor", "Sensor/Reading@value", "Sensor/Reading@unit")),
				CloseReason.TARGET_REACHED, 2, Instant.now(), Instant.now());

		String delta = ShapeDelta.describe(set);

		assertTrue(delta.contains("  Sensor/Reading@value"), delta);
		assertTrue(delta.contains("  Sensor/Reading@unit (sample 2; 1 payload(s))"), delta);
		assertFalse(delta.contains("untyped"), "With one kind and no tag there is nothing to say: " + delta);
	}

	@Test
	@DisplayName("A property name containing a colon is not mistaken for a value kind")
	void describe_splitsOnlyOnARealTypeTag() {
		String delta = ShapeDelta.describe(set(2, sample(1, "ns:temp:int"), sample(1, "ns:temp:int", "other:int")));

		assertTrue(delta.contains("  ns:temp: int"), delta);
	}

	@Test
	@DisplayName("Unparseable samples are counted, never listed as a path")
	// They are evidence that something arrived, not evidence about structure - reporting
	// '<unparseable>' among the paths would invite a model of it.
	void describe_countsUnparseableSamplesApart() {
		PayloadSampleSet set = set(2, sample(1, "a:int"), sample(1, "<unparseable>"));

		String delta = ShapeDelta.describe(set);

		assertTrue(delta.contains("1 further sample(s) could not be parsed"), delta);
		assertFalse(delta.contains("<unparseable>"), delta);
		assertTrue(delta.contains("Paths in the only parseable sample:"), delta);
	}

	@Test
	@DisplayName("A set of nothing but unparseable payloads yields no block at all")
	void describe_saysNothingWhenThereIsNoPath() {
		assertEquals("", ShapeDelta.describe(set(9, sample(9, "<unparseable>"))));
	}

	@Test
	@DisplayName("A single sample says that optionality is unproven")
	// Otherwise every path of the one payload reads as required, which is the error a thin
	// window is most likely to produce.
	void describe_flagsThatOneSampleProvesNoOptionality() {
		String delta = ShapeDelta.describe(set(1, sample(1, "a:int", "b:string")));

		assertTrue(delta.contains("One sample cannot show which paths are optional"), delta);
		assertFalse(delta.contains("Paths in all"), delta);
		assertFalse(delta.contains("Paths in some samples only"), delta);
	}

	@Test
	@DisplayName("A document with hundreds of paths is summarised within a bound")
	// An unbounded summary would cost more of the request prefix than the payloads it
	// summarises, which is the opposite of the point.
	void describe_boundsThePathList() {
		String[] shape = IntStream.range(0, 250).mapToObj(index -> String.format("path%03d:int", index))
				.toArray(String[]::new);

		String delta = ShapeDelta.describe(set(1, sample(1, shape)));

		assertTrue(delta.contains("… 50 more path(s), not listed."), delta);
		assertTrue(delta.contains("  path199: int"), delta);
		assertFalse(delta.contains("  path200: int"), "The bound is a bound: " + delta);
	}

	private static PayloadSample sample(int occurrences, String... shape) {
		return new PayloadSample("{}".getBytes(StandardCharsets.UTF_8), PayloadIngest.FORMAT_JSON, List.of(shape),
				Outcome.EMPTY, Instant.now(), occurrences);
	}

	private static PayloadSampleSet set(int payloadsSeen, PayloadSample... samples) {
		List<PayloadSample> all = new ArrayList<>(List.of(samples));
		return new PayloadSampleSet("sensors/a", null, PayloadIngest.FORMAT_JSON, all, CloseReason.TARGET_REACHED,
				payloadsSeen, Instant.now(), Instant.now());
	}

}
