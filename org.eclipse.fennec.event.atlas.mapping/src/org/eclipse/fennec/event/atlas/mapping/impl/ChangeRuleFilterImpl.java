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
package org.eclipse.fennec.event.atlas.mapping.impl;

import static java.util.Objects.requireNonNull;

import java.time.Duration;
import java.time.Instant;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;
import java.util.logging.Logger;

import org.eclipse.emf.ecore.InternalEObject;
import org.eclipse.fennec.event.atlas.mapping.ChangeRuleFilter;
import org.eclipse.fennec.event.atlas.model.mapping.AbsoluteChangeRule;
import org.eclipse.fennec.event.atlas.model.mapping.ChangeRule;
import org.eclipse.fennec.event.atlas.model.mapping.CountChangeRule;
import org.eclipse.fennec.event.atlas.model.mapping.PercentageChangeRule;
import org.eclipse.fennec.event.atlas.model.mapping.ResourceMapping;
import org.eclipse.fennec.event.atlas.model.mapping.TimeThrottleChangeRule;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.ConfigurationPolicy;
import org.osgi.service.component.annotations.Modified;

/**
 * Default {@link ChangeRuleFilter}: keeps the last accepted value, its timestamp and a
 * notification counter per resource and evaluates the resource's {@link ChangeRule} against
 * them.
 * <p>
 * State is held per {@code mappingMid} so {@link #reset(String)} is a map removal rather
 * than a scan, and within that per {@code providerId/serviceMid/resourceMid}. Only resources
 * that actually carry a rule ever get an entry, so the footprint is one small record per
 * ruled resource per device - and a resource generated from a {@code ReferenceMapping} keys
 * stably, because its {@code mid} is derived from the source attribute name.
 * <p>
 * Nothing here touches sensinact types: the mapping bundle doubles as the metamodel carrier
 * and must resolve without the gateway.
 * @author Ilenia Salvadori
 * @since 25.08.2026
 */
@Component(configurationPid = ChangeRuleFilterImpl.PID, configurationPolicy = ConfigurationPolicy.OPTIONAL)
public class ChangeRuleFilterImpl implements ChangeRuleFilter {

	/** Configuration pid; {@code enabled=false} pushes every value again. */
	static final String PID = "sensinact.mapping.changerule.filter";

	private static final Logger logger = Logger.getLogger(ChangeRuleFilterImpl.class.getName());

	/** mappingMid -&gt; (providerId/serviceMid/resourceMid -&gt; state) */
	private final Map<String, Map<String, ResourceState>> states = new ConcurrentHashMap<>();

	/** Keys already warned about, so an inapplicable rule is reported once, not per payload. */
	private final Set<String> warned = ConcurrentHashMap.newKeySet();

	private volatile boolean enabled = true;

	/**
	 * The filter's configuration.
	 */
	public @interface Config {
		/**
		 * Whether change rules are enforced on the way into the twin. Turn this off once the
		 * history provider enforces them where they belong.
		 */
		boolean enabled() default true;
	}

	@Activate
	@Modified
	void activate(Config config) {
		boolean wasEnabled = enabled;
		enabled = config.enabled();
		if (!enabled) {
			// Retained baselines are stale the moment they stop being maintained, so drop
			// them rather than compare against them if the filter is switched back on.
			states.clear();
			warned.clear();
		}
		if (wasEnabled != enabled) {
			logger.info("Change rule filtering is now " + (enabled ? "enabled" : "disabled"));
		}
	}

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.fennec.event.atlas.mapping.ChangeRuleFilter#accept(java.lang.String, java.lang.String, java.lang.String, org.eclipse.fennec.event.atlas.model.mapping.ResourceMapping, java.lang.Object, java.time.Instant)
	 */
	@Override
	public boolean accept(String mappingMid, String providerId, String serviceMid, ResourceMapping resourceMapping,
			Object value, Instant timestamp) {
		requireNonNull(mappingMid, "Mapping mid must not be null");
		requireNonNull(providerId, "Provider id must not be null");
		requireNonNull(serviceMid, "Service mid must not be null");
		requireNonNull(resourceMapping, "Resource mapping must not be null");

		ChangeRule rule = resourceMapping.getChangeRule();
		if (!enabled || rule == null) {
			return true;
		}

		String resourceKey = providerId + "/" + serviceMid + "/" + resourceMapping.getMid();
		if (rule.eIsProxy()) {
			// Rules are contained, so this only happens to a mapping written against the older
			// model, where a resource pointed at a rule in another document with an href. EMF
			// accepts such an href on a containment feature and leaves a proxy behind, and that
			// proxy carries the rule's *type* but none of its parameters - evaluating it would
			// silently apply a zero threshold, i.e. accept everything while looking like it
			// filtered. Name the migration instead.
			return acceptInapplicable(rule, mappingMid, resourceKey, String.format(
					"it is an unresolved reference (%s) - rules are now contained, so the rule "
							+ "belongs inline in the mapping rather than in a separate document",
					((InternalEObject) rule).eProxyURI()));
		}
		ResourceState state = states.computeIfAbsent(mappingMid, m -> new ConcurrentHashMap<>())
				.computeIfAbsent(resourceKey, r -> new ResourceState());
		Instant effectiveTimestamp = timestamp == null ? Instant.now() : timestamp;

		// One resource is updated by one payload at a time in practice, but two southbound
		// channels feeding the same provider would race on the baseline.
		synchronized (state) {
			boolean accepted = evaluate(rule, state, value, effectiveTimestamp, mappingMid, resourceKey);
			if (accepted) {
				state.lastValue = value;
				state.lastTimestamp = effectiveTimestamp;
				state.seen = true;
			} else if (logger.isLoggable(java.util.logging.Level.FINE)) {
				logger.fine(String.format("Change rule '%s' dropped %s = %s (last accepted %s at %s)", rule.getId(),
						resourceKey, value, state.lastValue, state.lastTimestamp));
			}
			return accepted;
		}
	}

	/**
	 * Applies one rule. The counter of a {@link CountChangeRule} advances for every value it
	 * sees, accepted or not - that is what "one out of every n" counts.
	 */
	private boolean evaluate(ChangeRule rule, ResourceState state, Object value, Instant timestamp, String mappingMid,
			String resourceKey) {
		if (rule instanceof CountChangeRule countRule) {
			Integer n = countRule.getN();
			state.count++;
			if (n == null || n <= 1) {
				return true;
			}
			return (state.count - 1) % n == 0;
		}
		// Every remaining rule compares against a baseline, so the first value is stored
		// unconditionally - there is nothing to compare it to yet.
		if (!state.seen) {
			return true;
		}
		if (rule instanceof TimeThrottleChangeRule throttle) {
			long intervalMillis = toMillis(throttle);
			if (intervalMillis <= 0) {
				return true;
			}
			// An out-of-order payload yields a negative elapsed time and is dropped: it is
			// older than what the twin already holds.
			return Duration.between(state.lastTimestamp, timestamp).toMillis() >= intervalMillis;
		}
		if (rule instanceof PercentageChangeRule percentageRule) {
			Double last = numeric(state.lastValue);
			Double current = numeric(value);
			if (last == null || current == null) {
				return acceptInapplicable(rule, mappingMid, resourceKey, "it is not numeric");
			}
			if (last == 0.0d) {
				// Relative change is undefined against a zero baseline: any move away from
				// zero is a change, staying at zero is not.
				return current != 0.0d;
			}
			double percentage = percentageRule.getPercentage() == null ? 0.0d : percentageRule.getPercentage();
			return Math.abs(current - last) / Math.abs(last) * 100.0d >= percentage;
		}
		if (rule instanceof AbsoluteChangeRule absoluteRule) {
			Double last = numeric(state.lastValue);
			Double current = numeric(value);
			if (last == null || current == null) {
				return acceptInapplicable(rule, mappingMid, resourceKey, "it is not numeric");
			}
			double delta = absoluteRule.getDelta() == null ? 0.0d : absoluteRule.getDelta();
			return Math.abs(current - last) >= delta;
		}
		return acceptInapplicable(rule, mappingMid, resourceKey,
				"the rule type " + rule.eClass().getName() + " is not implemented");
	}

	/**
	 * A rule that cannot be applied to a value accepts it, and says so once. Silently
	 * dropping data because a mapping binds a numeric rule to a string resource would hide
	 * the mistake in exactly the data it discards.
	 */
	private boolean acceptInapplicable(ChangeRule rule, String mappingMid, String resourceKey, String reason) {
		// A proxy has no id yet, so it is identified by its type for the message and by the
		// resource for the warn-once key.
		String ruleName = rule.eIsProxy() ? rule.eClass().getName() : rule.getId();
		if (warned.add(mappingMid + "/" + resourceKey + "#" + ruleName)) {
			logger.warning(String.format(
					"Change rule '%s' cannot be applied to resource %s of mapping '%s' because %s - "
							+ "every value of this resource is pushed unfiltered",
					ruleName, resourceKey, mappingMid, reason));
		}
		return true;
	}

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.fennec.event.atlas.mapping.ChangeRuleFilter#reset(java.lang.String)
	 */
	@Override
	public void reset(String mappingMid) {
		requireNonNull(mappingMid, "Mapping mid must not be null");
		if (states.remove(mappingMid) != null) {
			logger.fine("Dropped change rule state of mapping " + mappingMid);
		}
		warned.removeIf(k -> k.startsWith(mappingMid + "/"));
	}

	/**
	 * Converts a value to a double for the comparing rules, or <code>null</code> if it is
	 * not a number. Guards {@link ValueMapperImpl#toDouble(Object)}, which reports a
	 * non-numeric value as {@link Double#NaN} but throws on a non-numeric
	 * {@link CharSequence}.
	 */
	private static Double numeric(Object value) {
		try {
			Double converted = ValueMapperImpl.toDouble(value);
			return converted == null || converted.isNaN() ? null : converted;
		} catch (RuntimeException e) {
			return null;
		}
	}

	/**
	 * @return the throttle interval in milliseconds, 0 if it is not fully specified
	 */
	private static long toMillis(TimeThrottleChangeRule rule) {
		if (rule.getInterval() == null || rule.getIntervalUnit() == null) {
			return 0L;
		}
		return TimeUnit.valueOf(rule.getIntervalUnit().getName()).toMillis(rule.getInterval());
	}

	/**
	 * What a rule is evaluated against: the last <em>accepted</em> value, never a rejected
	 * one, so a slow drift is not filtered away one imperceptible step at a time.
	 */
	private static final class ResourceState {
		private boolean seen;
		private Object lastValue;
		private Instant lastTimestamp;
		private long count;
	}

}
