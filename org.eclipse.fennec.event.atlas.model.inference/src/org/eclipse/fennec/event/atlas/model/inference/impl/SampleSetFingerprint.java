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
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.List;
import java.util.TreeSet;

import org.eclipse.fennec.event.atlas.southbound.sampling.PayloadSample;
import org.eclipse.fennec.event.atlas.southbound.sampling.PayloadSampleSet;

/**
 * Identifies what a sample set is <em>about</em>, so that the same thing is not inferred twice.
 * <p>
 * The fingerprint is a SHA-256 over the sorted union of the samples' shapes plus the declared
 * nsURI - the canonical-string-then-hash approach of the workspace's existing
 * {@code EcoreFingerprintGenerator}, with the shapes standing in for the JSON schema it derived
 * itself.
 * <p>
 * Two things about it matter:
 * <ul>
 * <li><b>The channel is not part of it.</b> A fleet of identical sensors on twenty topics needs
 * one model, not twenty; keying on the channel would spend twenty runs on it - at a per-run
 * cost that is measured in real money.</li>
 * <li><b>Neither are the values, the sample count or the timestamps.</b> A channel that keeps
 * failing hands over window after window of the same shapes while its draft waits for review.
 * Those must all fingerprint alike, or the dedup does nothing.</li>
 * </ul>
 * @author Ilenia Salvadori
 * @since 27.08.2026
 */
final class SampleSetFingerprint {

	private SampleSetFingerprint() {
		// static use only
	}

	/**
	 * @param sampleSet the set. Parameter must not be <code>null</code>
	 * @return the fingerprint as a 64-character hexadecimal string. Never <code>null</code>
	 */
	static String of(PayloadSampleSet sampleSet) {
		return hash(canonical(sampleSet));
	}

	/**
	 * The string that is hashed: the format, the declared nsURI and every distinct shape entry
	 * of every sample, sorted so that the arrival order of the samples cannot change it.
	 */
	private static String canonical(PayloadSampleSet sampleSet) {
		TreeSet<String> entries = new TreeSet<>();
		for (PayloadSample sample : sampleSet.samples()) {
			entries.addAll(sample.shape());
		}
		return String.join("\n", List.of(sampleSet.format(),
				sampleSet.namespaceUri() == null ? "" : sampleSet.namespaceUri(), String.join("\n", entries)));
	}

	private static String hash(String canonical) {
		try {
			byte[] digest = MessageDigest.getInstance("SHA-256").digest(canonical.getBytes(StandardCharsets.UTF_8));
			StringBuilder hex = new StringBuilder(digest.length * 2);
			for (byte b : digest) {
				hex.append(Character.forDigit((b >> 4) & 0xF, 16)).append(Character.forDigit(b & 0xF, 16));
			}
			return hex.toString();
		} catch (NoSuchAlgorithmException e) {
			// SHA-256 is required of every Java platform
			throw new IllegalStateException("SHA-256 is not available", e);
		}
	}

}
