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
import static org.junit.jupiter.api.Assertions.assertNotEquals;
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
 * Unit tests for {@link SampleSetFingerprint}. What the fingerprint ignores is what decides how
 * many model calls a fleet of sensors costs.
 * @author Ilenia Salvadori
 */
public class SampleSetFingerprintTest {

	@Test
	@DisplayName("The same shapes fingerprint alike however the samples arrived")
	// A channel whose draft is waiting for review keeps handing over window after window of the
	// same shapes. If those did not fingerprint alike the dedup would do nothing at all.
	void sameShapes_fingerprintAlike() {
		String first = SampleSetFingerprint.of(set("sensors/a", null, sample(List.of("temp:int", "id:string")),
				sample(List.of("temp:float", "id:string"))));
		String second = SampleSetFingerprint.of(set("sensors/a", null,
				// reversed, different bodies, different counts, later timestamps
				sample(List.of("id:string", "temp:float")), sample(List.of("id:string", "temp:int"))));

		assertEquals(first, second);
		assertEquals(64, first.length(), "A SHA-256 as hexadecimal");
		assertTrue(first.matches("[0-9a-f]{64}"));
	}

	@Test
	@DisplayName("The channel is not part of the fingerprint")
	// Twenty identical sensors on twenty topics need one model, not twenty runs at real cost.
	void channel_isNotPartOfIt() {
		List<String> shape = List.of("temp:int");

		assertEquals(SampleSetFingerprint.of(set("sensors/a", null, sample(shape))),
				SampleSetFingerprint.of(set("sensors/b", null, sample(shape))));
	}

	@Test
	@DisplayName("Different shapes fingerprint differently")
	void differentShapes_fingerprintDifferently() {
		assertNotEquals(SampleSetFingerprint.of(set("sensors/a", null, sample(List.of("temp:int")))),
				SampleSetFingerprint.of(set("sensors/a", null, sample(List.of("temp:int", "rssi:int")))));
	}

	@Test
	@DisplayName("A declared model is part of the fingerprint")
	// Two payloads with the same structure that name different models are two models.
	void declaredNamespace_isPartOfIt() {
		List<String> shape = List.of("Station@name");

		assertNotEquals(SampleSetFingerprint.of(set("sensors/a", "http://example.org/a/1.0", sample(shape))),
				SampleSetFingerprint.of(set("sensors/a", "http://example.org/b/1.0", sample(shape))));
	}

	private static PayloadSample sample(List<String> shape) {
		return new PayloadSample("{}".getBytes(StandardCharsets.UTF_8), PayloadIngest.FORMAT_JSON, shape,
				Outcome.EMPTY, Instant.now(), 1);
	}

	private static PayloadSampleSet set(String source, String namespaceUri, PayloadSample... samples) {
		return new PayloadSampleSet(source, namespaceUri, PayloadIngest.FORMAT_JSON, List.of(samples),
				CloseReason.TARGET_REACHED, samples.length, Instant.now(), Instant.now());
	}

}
