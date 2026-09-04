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

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.StringJoiner;
import java.util.TreeMap;
import java.util.TreeSet;

import org.eclipse.fennec.event.atlas.southbound.sampling.PayloadSample;
import org.eclipse.fennec.event.atlas.southbound.sampling.PayloadSampleSet;

/**
 * States which paths appear in <em>which</em> samples, so that optionality is evidence the agent
 * is handed rather than something it has to reconstruct by reading ten documents side by side.
 * <p>
 * <b>The comparison is already done - it is why the samples exist.</b> A payload earns a slot in
 * a collection window only when its shape, the sorted {@code path:type} set that
 * {@code ShapeFingerprint} derives, differs from every shape already held. So the set handed
 * over here is by construction a set of <em>distinct</em> shapes, and the difference between any
 * two of them is a set operation over data already in memory. Asking an agent to request those
 * differences one at a time would spend an iteration per request on a dependency chain, which is
 * the one thing measured to exhaust a run's budget (2026-08-28: authoring collapsed from 9
 * iterations to 1 by removing a chain, and that is what brought the run inside the synchronous
 * cap). Computed here it costs none.
 * <p>
 * <b>What this adds that a payload cannot.</b> A single document says which paths it has; it
 * cannot say which of them are optional. Ten documents say so only if the reader also knows that
 * an absence is real - and it is, because a sample is a payload that arrived, not a summary the
 * collector assembled. That guarantee is stated in the rendered text, because it is the premise
 * the whole inference rests on.
 * <p>
 * <b>It does not replace the bodies.</b> The shapes deliberately discard values, so they cannot
 * supply a discriminator's value, a timestamp's format or a unit, and a package cannot be
 * validated against a delta. This is a summary in front of the payloads, never instead of them -
 * see {@link InferencePrompt}, whose class comment explains why the samples travel raw.
 * @author Ilenia Salvadori
 * @since 03.09.2026
 */
final class ShapeDelta {

	/**
	 * The shape a payload no parser could read is given. Mirrors
	 * {@code ShapeFingerprint.UNPARSEABLE}, which is not exported by the sampling bundle -
	 * these samples are reported as a count rather than as a path, since they are evidence
	 * that something arrived, not evidence about structure.
	 */
	private static final String UNPARSEABLE = "<unparseable>";

	/**
	 * How many path lines the block may hold. A large XMI can carry hundreds of paths, and an
	 * unbounded summary would cost more of the prefix than the payloads it summarises.
	 */
	private static final int MAX_PATHS = 200;

	private ShapeDelta() {
		// static use only
	}

	/**
	 * @param sampleSet the set to summarise. Parameter must not be <code>null</code>
	 * @return the summary, or an empty string when the samples yielded no path at all - a set
	 * of unparseable payloads has nothing to compare. Never <code>null</code>
	 */
	static String describe(PayloadSampleSet sampleSet) {
		List<PayloadSample> samples = sampleSet.samples();
		Map<String, PathEvidence> paths = new TreeMap<>();
		int unparseable = 0;
		for (int index = 0; index < samples.size(); index++) {
			PayloadSample sample = samples.get(index);
			if (sample.shape().contains(UNPARSEABLE)) {
				unparseable++;
				continue;
			}
			for (String entry : sample.shape()) {
				String path = pathOf(entry);
				paths.computeIfAbsent(path, key -> new PathEvidence())
						.record(index + 1, typeOf(entry), sample.occurrences());
			}
		}
		if (paths.isEmpty()) {
			return "";
		}
		return render(paths, sampleSet, unparseable);
	}

	/**
	 * Splits the path off a shape entry. Every JSON entry ends in {@code :type}, so the final
	 * colon is the separator; an XML or XMI entry carries no type at all, and one whose own
	 * last segment happens to look like a type tag is read as typed - a cosmetic misgrouping in
	 * a document that states no types anyway.
	 */
	private static String pathOf(String entry) {
		int separator = typeSeparator(entry);
		return separator < 0 ? entry : entry.substring(0, separator);
	}

	/**
	 * @return the entry's type tag, or an empty string when it carries none
	 */
	private static String typeOf(String entry) {
		int separator = typeSeparator(entry);
		return separator < 0 ? "" : entry.substring(separator + 1);
	}

	/**
	 * The index of the colon introducing a type tag, or -1. A tag is the lower-case word
	 * {@code ShapeFingerprint} appends for a scalar, or the {@code {}} / {@code []} it appends
	 * for an empty container; anything else after the final colon is part of the path, since
	 * both JSON property names and XML element names may contain one.
	 */
	private static int typeSeparator(String entry) {
		int colon = entry.lastIndexOf(':');
		if (colon < 0 || colon == entry.length() - 1) {
			return -1;
		}
		return isTypeTag(entry.substring(colon + 1)) ? colon : -1;
	}

	private static boolean isTypeTag(String candidate) {
		if ("{}".equals(candidate) || "[]".equals(candidate)) {
			return true;
		}
		return candidate.chars().allMatch(character -> (character >= 'a' && character <= 'z') || character == '_');
	}

	/**
	 * The block itself: the paths every sample carries, then the paths only some carry, which
	 * is the half that says what is optional.
	 */
	private static String render(Map<String, PathEvidence> paths, PayloadSampleSet sampleSet, int unparseable) {
		int sampleCount = sampleSet.sampleCount();
		int compared = sampleCount - unparseable;
		List<String> everywhere = new ArrayList<>();
		List<String> sometimes = new ArrayList<>();
		for (Map.Entry<String, PathEvidence> path : paths.entrySet()) {
			PathEvidence evidence = path.getValue();
			if (evidence.samples.size() == compared) {
				everywhere.add(String.format("  %s%s", path.getKey(), evidence.types()));
			} else {
				sometimes.add(String.format("  %s%s (%s; %s payload(s))", path.getKey(), evidence.types(),
						samples(evidence.samples), evidence.payloads));
			}
		}

		StringJoiner block = new StringJoiner("\n");
		block.add("Every path the samples below carry, and which of them carry it. An array's elements are not "
				+ "distinguished from each other, so a path through '[]' is that field of every element. "
				+ "After the final ':' is the kind of value the path held, where the format states one at "
				+ "all - '{}' and '[]' mean the container was there and empty, so its contents are unproven "
				+ "rather than absent.");
		block.add("");
		block.add("A sample is a payload that arrived, so a path some samples lack was genuinely absent from "
				+ "those payloads - it is not something the collector dropped. What every sample carries is "
				+ "proven present; what only some carry is proven optional; what one sample carries alone is "
				+ "proven, but thinly.");
		int budget = MAX_PATHS;
		budget = section(block, compared == 1 ? "Paths in the only parseable sample:"
				: String.format("Paths in all %s samples:", compared), everywhere, budget);
		budget = section(block, String.format(
				"Paths in some samples only, counted against the %s payload(s) the window saw:",
				sampleSet.payloadsSeen()), sometimes, budget);
		if (compared == 1) {
			block.add("");
			block.add("One sample cannot show which paths are optional; treat every one of them as unproven.");
		}
		if (unparseable > 0) {
			block.add("");
			block.add(String.format("%s further sample(s) could not be parsed at all and contribute no path. "
					+ "They are still in the payloads below.", unparseable));
		}
		return block.toString();
	}

	/**
	 * One heading and its lines, within what is left of the path budget.
	 * @return the budget remaining
	 */
	private static int section(StringJoiner block, String heading, List<String> lines, int budget) {
		if (lines.isEmpty()) {
			return budget;
		}
		block.add("");
		block.add(heading);
		lines.stream().limit(budget).forEach(block::add);
		if (lines.size() > budget) {
			block.add(String.format("  … %s more path(s), not listed.", lines.size() - budget));
		}
		return Math.max(0, budget - lines.size());
	}

	/**
	 * Which samples carried something, one-based and in the order the payloads arrived, so the
	 * numbers match the sample headers of the prompt.
	 */
	private static String samples(TreeSet<Integer> samples) {
		StringJoiner joined = new StringJoiner(", ");
		samples.forEach(sample -> joined.add(String.valueOf(sample)));
		return String.format(samples.size() == 1 ? "sample %s" : "samples %s", joined);
	}

	/**
	 * What one path was seen to hold, across the samples that carried it.
	 */
	private static final class PathEvidence {

		private final TreeSet<Integer> samples = new TreeSet<>();

		/** Type tag to the samples that showed it, empty key for an entry carrying no tag. */
		private final Map<String, TreeSet<Integer>> types = new TreeMap<>();

		private int payloads;

		void record(int sample, String type, int occurrences) {
			// occurrences sum to the window's payloadsSeen, except where a ringSize below the
			// target evicted a slot - the counts are then a floor, which is the right direction
			if (samples.add(sample)) {
				// occurrences count payloads of this sample's shape, so a path shared by two
				// samples is weighted by both
				payloads += occurrences;
			}
			types.computeIfAbsent(type, key -> new TreeSet<>()).add(sample);
		}

		/**
		 * The path's value kinds. A single kind is stated plainly; several are stated with the
		 * samples that showed each, because that disagreement is the whole reason a set of
		 * samples can type a field that one payload cannot - {@code temp:int} beside
		 * {@code temp:float} is what widens it.
		 * @return the rendered kinds, or an empty string for an untyped format
		 */
		String types() {
			if (types.size() == 1) {
				String only = types.keySet().iterator().next();
				return only.isEmpty() ? "" : ": " + only;
			}
			// '; ' rather than ', ', which the sample lists inside each kind already use
			StringJoiner joined = new StringJoiner("; ");
			for (Map.Entry<String, TreeSet<Integer>> type : types.entrySet()) {
				String kind = type.getKey().isEmpty() ? "untyped" : type.getKey();
				joined.add(String.format("%s in %s", kind, samples(type.getValue())));
			}
			return ": " + joined;
		}
	}

}
