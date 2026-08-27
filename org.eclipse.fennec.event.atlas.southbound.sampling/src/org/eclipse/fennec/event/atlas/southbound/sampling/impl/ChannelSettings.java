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
package org.eclipse.fennec.event.atlas.southbound.sampling.impl;

import java.time.Duration;

/**
 * What a collection window of one channel is configured to do: how much evidence to aim for,
 * when to give up waiting for more, and how much to keep.
 * @param targetSamples how many distinctly shaped payloads close the window - N. At least 1
 * @param quietSamples how many consecutive payloads may add no new shape before the window is
 * closed early - K. At least 1
 * @param maxWait how long the window may stay open before it is handed over with whatever it
 * has. Never <code>null</code>, never zero
 * @param ringSize how many samples the window retains. Below {@code targetSamples} the oldest
 * sample is evicted when a new shape arrives, which trades evidence for memory
 * @author Ilenia Salvadori
 * @since 27.08.2026
 */
record ChannelSettings(int targetSamples, int quietSamples, Duration maxWait, int ringSize) {

	/**
	 * Clamps every value into the range that still makes sense, rather than refusing to
	 * activate: these come from an operator's configuration, and a typo in a threshold must
	 * not take the payload sampling - and with it the suppression of the per-payload ingest
	 * warnings - out of the runtime.
	 * @param targetSamples N, clamped to at least 1
	 * @param quietSamples K, clamped to at least 1
	 * @param maxWaitSeconds the maximum window lifetime, clamped to at least 1 second
	 * @param ringSize the retained sample count; 0 or less means {@code targetSamples}
	 * @return the settings. Never <code>null</code>
	 */
	static ChannelSettings of(int targetSamples, int quietSamples, long maxWaitSeconds, int ringSize) {
		int target = Math.max(1, targetSamples);
		return new ChannelSettings(target, Math.max(1, quietSamples),
				Duration.ofSeconds(Math.max(1, maxWaitSeconds)), ringSize <= 0 ? target : Math.min(ringSize, target));
	}

	/*
	 * (non-Javadoc)
	 * @see java.lang.Record#toString()
	 */
	@Override
	public String toString() {
		return String.format("target %s sample(s), quiet after %s, max wait %ss, ring %s", targetSamples, quietSamples,
				maxWait.toSeconds(), ringSize);
	}

}
