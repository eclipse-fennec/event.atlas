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
package org.eclipse.fennec.event.atlas.southbound.sampling;

import static java.util.Objects.requireNonNull;

import java.time.Duration;
import java.time.Instant;
import java.util.List;

/**
 * The closed collection window of one channel: the distinctly shaped payloads it gathered,
 * and how much evidence that amounts to.
 * <p>
 * This is what model inference reasons over, and it is a <em>set</em> for three reasons that
 * a single payload cannot satisfy:
 * <ol>
 * <li><b>The type discriminator.</b> An untyped JSON payload is typed by the key whose value
 * is constant for this sensor. In one payload every key looks constant; across samples the
 * constant ones stand out and the measurements exclude themselves.</li>
 * <li><b>Type widening.</b> {@code "temp": 0} alone says integer; {@code 0} in one sample and
 * {@code 21.5} in another says double.</li>
 * <li><b>Optionality.</b> Present in every sample means mandatory, present in some means
 * optional. From one payload every field looks mandatory.</li>
 * </ol>
 * @param source the channel the payloads came from - an MQTT topic, or {@code rest/<channel>}.
 * Never <code>null</code>
 * @param namespaceUri the unresolvable nsURI every payload in this set declared, or
 * <code>null</code> when they declared none (which is the normal case: JSON declares nothing)
 * @param format the format every payload in this set was read as. Never <code>null</code>
 * @param samples the distinctly shaped payloads, in arrival order. Never <code>null</code>,
 * never empty
 * @param closeReason why the window was handed over when it was. Never <code>null</code>
 * @param payloadsSeen how many payloads arrived while the window was open, including the ones
 * whose shape was already known. Always at least {@link #sampleCount()}
 * @param openedAt when the window's first payload arrived. Never <code>null</code>
 * @param closedAt when the window was closed. Never <code>null</code>
 * @author Ilenia Salvadori
 * @since 27.08.2026
 */
public record PayloadSampleSet(String source, String namespaceUri, String format, List<PayloadSample> samples,
		CloseReason closeReason, int payloadsSeen, Instant openedAt, Instant closedAt) {

	/**
	 * Why a collection window stopped collecting. Only {@link #MAX_WAIT} means the set is
	 * thinner than was asked for.
	 */
	public enum CloseReason {
		/** The configured number of distinctly shaped samples was reached. */
		TARGET_REACHED,
		/**
		 * The configured number of consecutive payloads added no new shape, so waiting for
		 * more is unlikely to add evidence. The early exit that makes a fast sensor cheap.
		 */
		NO_NEW_SHAPES,
		/**
		 * The window was open for the configured maximum and is handed over with whatever it
		 * has. Ten samples is ten seconds from a 1 Hz sensor and two and a half hours from a
		 * 15-minute one; this is the condition that keeps a slow sensor from never producing
		 * a set at all - at the cost of evidence, see {@link PayloadSampleSet#lowEvidence()}.
		 */
		MAX_WAIT
	}

	/**
	 * @throws NullPointerException if any reference parameter other than {@code namespaceUri}
	 * is <code>null</code>
	 * @throws IllegalArgumentException if the set is empty or claims fewer payloads than
	 * samples
	 */
	public PayloadSampleSet {
		requireNonNull(source, "Source must not be null");
		requireNonNull(format, "Format must not be null");
		requireNonNull(closeReason, "Close reason must not be null");
		requireNonNull(openedAt, "Opened timestamp must not be null");
		requireNonNull(closedAt, "Closed timestamp must not be null");
		samples = List.copyOf(requireNonNull(samples, "Samples must not be null"));
		if (samples.isEmpty()) {
			throw new IllegalArgumentException("An empty sample set is never worth handing over");
		}
		if (payloadsSeen < samples.size()) {
			throw new IllegalArgumentException(
					String.format("Cannot have seen fewer payloads (%s) than samples (%s)", payloadsSeen,
							samples.size()));
		}
	}

	/**
	 * @return how many distinctly shaped payloads the set holds - the amount of evidence
	 * behind it, unlike {@link #payloadsSeen()}
	 */
	public int sampleCount() {
		return samples.size();
	}

	/**
	 * Whether this set was handed over before it had gathered what was asked for.
	 * <p>
	 * A consumer must not refuse a low-evidence set - a 15-minute sensor would then never get
	 * a model at all - but it should record the flag against whatever it produces, so whoever
	 * reviews the result knows how much stood behind it.
	 * @return <code>true</code> if the window was closed by {@link CloseReason#MAX_WAIT}
	 */
	public boolean lowEvidence() {
		return closeReason == CloseReason.MAX_WAIT;
	}

	/**
	 * @return how long the window was open. Never <code>null</code>
	 */
	public Duration duration() {
		return Duration.between(openedAt, closedAt);
	}

	/*
	 * (non-Javadoc)
	 * @see java.lang.Record#toString()
	 */
	@Override
	public String toString() {
		return String.format(
				"PayloadSampleSet[source=%s, format=%s, namespaceUri=%s, samples=%s, payloadsSeen=%s, reason=%s, "
						+ "lowEvidence=%s, duration=%s]",
				source, format, namespaceUri, samples.size(), payloadsSeen, closeReason, lowEvidence(), duration());
	}

}
