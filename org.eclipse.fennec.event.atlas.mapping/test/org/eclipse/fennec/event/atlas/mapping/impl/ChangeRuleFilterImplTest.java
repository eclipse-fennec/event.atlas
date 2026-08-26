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

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.lang.annotation.Annotation;
import java.time.Duration;
import java.time.Instant;

import org.eclipse.emf.common.util.URI;
import org.eclipse.emf.ecore.InternalEObject;
import org.eclipse.fennec.event.atlas.model.mapping.AbsoluteChangeRule;
import org.eclipse.fennec.event.atlas.model.mapping.ChangeRule;
import org.eclipse.fennec.event.atlas.model.mapping.CountChangeRule;
import org.eclipse.fennec.event.atlas.model.mapping.DurationUnit;
import org.eclipse.fennec.event.atlas.model.mapping.MappingFactory;
import org.eclipse.fennec.event.atlas.model.mapping.PercentageChangeRule;
import org.eclipse.fennec.event.atlas.model.mapping.ResourceMapping;
import org.eclipse.fennec.event.atlas.model.mapping.TimeThrottleChangeRule;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/**
 * Rule semantics of {@link ChangeRuleFilterImpl}, exercised without a twin: what the filter
 * decides is a pure function of the rule, the retained baseline and the incoming value.
 */
public class ChangeRuleFilterImplTest {

	private static final String MAPPING = "mapping-1";
	private static final String PROVIDER = "provider-1";
	private static final String SERVICE = "service-1";

	private final Instant base = Instant.parse("2026-08-25T10:00:00Z");

	@Test
	@DisplayName("A resource without a change rule accepts every value")
	void noRule_acceptsEverything() {
		ChangeRuleFilterImpl filter = filter(true);
		ResourceMapping resource = resource("temperature", null);

		assertTrue(accept(filter, resource, 20.0d, base));
		assertTrue(accept(filter, resource, 20.0d, base.plusSeconds(1)));
		assertTrue(accept(filter, resource, 20.001d, base.plusSeconds(2)));
	}

	@Test
	@DisplayName("A disabled filter accepts every value")
	void disabled_acceptsEverything() {
		ChangeRuleFilterImpl filter = filter(false);
		ResourceMapping resource = resource("temperature", percentage(50.0d));

		assertTrue(accept(filter, resource, 20.0d, base));
		assertTrue(accept(filter, resource, 20.001d, base.plusSeconds(1)));
	}

	@Test
	@DisplayName("A percentage rule stores the first value, then only moves beyond the threshold")
	void percentageRule_thresholdOnLastAcceptedValue() {
		ChangeRuleFilterImpl filter = filter(true);
		ResourceMapping resource = resource("temperature", percentage(10.0d));

		assertTrue(accept(filter, resource, 100.0d, base), "the first value has nothing to compare to");
		assertFalse(accept(filter, resource, 105.0d, base.plusSeconds(1)), "5%");
		assertFalse(accept(filter, resource, 109.0d, base.plusSeconds(2)), "9% - still against 100");
		assertTrue(accept(filter, resource, 110.0d, base.plusSeconds(3)), "10% reaches the threshold");
		assertFalse(accept(filter, resource, 115.0d, base.plusSeconds(4)), "now measured against 110");
	}

	@Test
	@DisplayName("A percentage rule treats any move away from a zero baseline as a change")
	void percentageRule_zeroBaseline() {
		ChangeRuleFilterImpl filter = filter(true);
		ResourceMapping resource = resource("rain", percentage(10.0d));

		assertTrue(accept(filter, resource, 0.0d, base));
		assertFalse(accept(filter, resource, 0.0d, base.plusSeconds(1)), "still zero, still no change");
		assertTrue(accept(filter, resource, 0.1d, base.plusSeconds(2)), "relative change is undefined here");
	}

	@Test
	@DisplayName("A percentage rule measures the size of the change, not its direction")
	void percentageRule_worksDownwards() {
		ChangeRuleFilterImpl filter = filter(true);
		ResourceMapping resource = resource("temperature", percentage(10.0d));

		assertTrue(accept(filter, resource, 100.0d, base));
		assertTrue(accept(filter, resource, 80.0d, base.plusSeconds(1)), "-20%");
	}

	@Test
	@DisplayName("An absolute rule compares the absolute change against its delta")
	void absoluteRule_delta() {
		ChangeRuleFilterImpl filter = filter(true);
		ResourceMapping resource = resource("humidity", absolute(2.0d));

		assertTrue(accept(filter, resource, 50, base));
		assertFalse(accept(filter, resource, 51, base.plusSeconds(1)));
		assertTrue(accept(filter, resource, 52, base.plusSeconds(2)));
		assertFalse(accept(filter, resource, 51, base.plusSeconds(3)), "1 away from the stored 52");
	}

	@Test
	@DisplayName("A count rule accepts one value out of every n, however much they move")
	void countRule_everyNth() {
		ChangeRuleFilterImpl filter = filter(true);
		ResourceMapping resource = resource("pulse", count(3));

		assertTrue(accept(filter, resource, 1.0d, base));
		assertFalse(accept(filter, resource, 500.0d, base.plusSeconds(1)));
		assertFalse(accept(filter, resource, 900.0d, base.plusSeconds(2)));
		assertTrue(accept(filter, resource, 2.0d, base.plusSeconds(3)));
		assertFalse(accept(filter, resource, 3.0d, base.plusSeconds(4)));
	}

	@Test
	@DisplayName("A count rule of 1 or an unset n accepts every value")
	void countRule_degenerate() {
		ChangeRuleFilterImpl filter = filter(true);
		ResourceMapping everyOne = resource("every-one", count(1));
		ResourceMapping unset = resource("unset", count(null));

		assertTrue(accept(filter, everyOne, 1.0d, base));
		assertTrue(accept(filter, everyOne, 1.0d, base.plusSeconds(1)));
		assertTrue(accept(filter, unset, 1.0d, base));
		assertTrue(accept(filter, unset, 1.0d, base.plusSeconds(1)));
	}

	@Test
	@DisplayName("A time throttle measures the value's timestamp against the last accepted one")
	void timeThrottle_interval() {
		ChangeRuleFilterImpl filter = filter(true);
		ResourceMapping resource = resource("temperature", throttle(10, DurationUnit.MINUTES));

		assertTrue(accept(filter, resource, 1.0d, base));
		assertFalse(accept(filter, resource, 2.0d, base.plus(Duration.ofMinutes(9))));
		assertTrue(accept(filter, resource, 3.0d, base.plus(Duration.ofMinutes(10))));
		assertFalse(accept(filter, resource, 4.0d, base.plus(Duration.ofMinutes(19))),
				"the window restarts at the accepted value");
	}

	@Test
	@DisplayName("A time throttle drops a value older than the one already stored")
	void timeThrottle_outOfOrder() {
		ChangeRuleFilterImpl filter = filter(true);
		ResourceMapping resource = resource("temperature", throttle(10, DurationUnit.MINUTES));

		assertTrue(accept(filter, resource, 1.0d, base.plus(Duration.ofMinutes(30))));
		assertFalse(accept(filter, resource, 2.0d, base), "it is older than what the twin holds");
	}

	@Test
	@DisplayName("A numeric rule on a non-numeric value accepts rather than drops")
	void numericRule_onNonNumericValue_accepts() {
		ChangeRuleFilterImpl filter = filter(true);
		ResourceMapping resource = resource("state", percentage(10.0d));

		assertTrue(accept(filter, resource, "OPEN", base));
		assertTrue(accept(filter, resource, "OPEN", base.plusSeconds(1)), "an unapplicable rule never drops");
		assertTrue(accept(filter, resource, "CLOSED", base.plusSeconds(2)));
	}

	@Test
	@DisplayName("An unresolved rule reference accepts rather than applying a zero threshold")
	void proxyRule_acceptsEverything() {
		ChangeRuleFilterImpl filter = filter(true);

		// What an href that never resolved leaves behind: the right type, no parameters. Read
		// naively, percentage == null would mean a 0% threshold - filtering nothing while
		// looking like it filtered something.
		PercentageChangeRule proxy = MappingFactory.eINSTANCE.createPercentageChangeRule();
		((InternalEObject) proxy).eSetProxyURI(URI.createURI("persistence-rules.xmi#pct-5"));
		assertTrue(proxy.eIsProxy(), "precondition: the rule stands in for an unresolved reference");

		ResourceMapping resource = resource("temperature", proxy);

		assertTrue(accept(filter, resource, 100.0d, base));
		assertTrue(accept(filter, resource, 100.0d, base.plusSeconds(1)), "unchanged, and still accepted");
	}

	@Test
	@DisplayName("State is kept per resource, not per rule")
	void state_isPerResource() {
		ChangeRuleFilterImpl filter = filter(true);
		// Equal rules, not one shared instance: a rule is contained by its resource, so
		// handing the same instance to both would move it out of the first.
		ResourceMapping first = resource("first", percentage(10.0d));
		ResourceMapping second = resource("second", percentage(10.0d));

		assertTrue(accept(filter, first, 100.0d, base));
		assertTrue(accept(filter, second, 5000.0d, base), "the second resource is on its own first value");
		assertFalse(accept(filter, first, 101.0d, base.plusSeconds(1)));
		assertFalse(accept(filter, second, 5050.0d, base.plusSeconds(1)));
	}

	@Test
	@DisplayName("State is kept per provider, so two devices do not share a baseline")
	void state_isPerProvider() {
		ChangeRuleFilterImpl filter = filter(true);
		ResourceMapping resource = resource("temperature", percentage(10.0d));

		assertTrue(filter.accept(MAPPING, "device-a", SERVICE, resource, 100.0d, base));
		assertTrue(filter.accept(MAPPING, "device-b", SERVICE, resource, 100.0d, base));
		assertFalse(filter.accept(MAPPING, "device-a", SERVICE, resource, 101.0d, base.plusSeconds(1)));
	}

	@Test
	@DisplayName("reset() makes the next value of a mapping's resources count as the first")
	void reset_dropsTheBaseline() {
		ChangeRuleFilterImpl filter = filter(true);
		ResourceMapping resource = resource("temperature", percentage(10.0d));

		assertTrue(accept(filter, resource, 100.0d, base));
		assertFalse(accept(filter, resource, 101.0d, base.plusSeconds(1)));

		filter.reset(MAPPING);

		assertTrue(accept(filter, resource, 101.0d, base.plusSeconds(2)), "no baseline to compare against");
	}

	@Test
	@DisplayName("reset() only drops the named mapping")
	void reset_isPerMapping() {
		ChangeRuleFilterImpl filter = filter(true);
		ResourceMapping resource = resource("temperature", percentage(10.0d));

		assertTrue(accept(filter, resource, 100.0d, base));
		assertTrue(filter.accept("other-mapping", PROVIDER, SERVICE, resource, 100.0d, base));

		filter.reset("other-mapping");

		assertFalse(accept(filter, resource, 101.0d, base.plusSeconds(1)), "this mapping kept its baseline");
		assertTrue(filter.accept("other-mapping", PROVIDER, SERVICE, resource, 101.0d, base.plusSeconds(1)));
	}

	@Test
	@DisplayName("A null timestamp falls back to now instead of failing")
	void nullTimestamp_isAccepted() {
		ChangeRuleFilterImpl filter = filter(true);
		ResourceMapping resource = resource("temperature", throttle(10, DurationUnit.MINUTES));

		assertTrue(filter.accept(MAPPING, PROVIDER, SERVICE, resource, 1.0d, null));
		assertFalse(filter.accept(MAPPING, PROVIDER, SERVICE, resource, 2.0d, null),
				"both fall back to now, which is inside the window");
	}

	@Test
	@DisplayName("The identifying parameters are required")
	void nullArguments_areRejected() {
		ChangeRuleFilterImpl filter = filter(true);
		ResourceMapping resource = resource("temperature", percentage(10.0d));

		assertThrows(NullPointerException.class, () -> filter.accept(null, PROVIDER, SERVICE, resource, 1.0d, base));
		assertThrows(NullPointerException.class, () -> filter.accept(MAPPING, null, SERVICE, resource, 1.0d, base));
		assertThrows(NullPointerException.class, () -> filter.accept(MAPPING, PROVIDER, null, resource, 1.0d, base));
		assertThrows(NullPointerException.class, () -> filter.accept(MAPPING, PROVIDER, SERVICE, null, 1.0d, base));
		assertThrows(NullPointerException.class, () -> filter.reset(null));
	}

	private boolean accept(ChangeRuleFilterImpl filter, ResourceMapping resource, Object value, Instant timestamp) {
		return filter.accept(MAPPING, PROVIDER, SERVICE, resource, value, timestamp);
	}

	private static ChangeRuleFilterImpl filter(boolean enabled) {
		ChangeRuleFilterImpl filter = new ChangeRuleFilterImpl();
		filter.activate(new ChangeRuleFilterImpl.Config() {

			@Override
			public Class<? extends Annotation> annotationType() {
				return ChangeRuleFilterImpl.Config.class;
			}

			@Override
			public boolean enabled() {
				return enabled;
			}
		});
		return filter;
	}

	private static ResourceMapping resource(String mid, ChangeRule rule) {
		ResourceMapping resource = MappingFactory.eINSTANCE.createResourceMapping();
		resource.setMid(mid);
		resource.setChangeRule(rule);
		return resource;
	}

	private static PercentageChangeRule percentage(Double percentage) {
		PercentageChangeRule rule = MappingFactory.eINSTANCE.createPercentageChangeRule();
		rule.setId("pct");
		rule.setPercentage(percentage);
		return rule;
	}

	private static AbsoluteChangeRule absolute(Double delta) {
		AbsoluteChangeRule rule = MappingFactory.eINSTANCE.createAbsoluteChangeRule();
		rule.setId("abs");
		rule.setDelta(delta);
		return rule;
	}

	private static CountChangeRule count(Integer n) {
		CountChangeRule rule = MappingFactory.eINSTANCE.createCountChangeRule();
		rule.setId("count");
		rule.setN(n);
		return rule;
	}

	private static TimeThrottleChangeRule throttle(Integer interval, DurationUnit unit) {
		TimeThrottleChangeRule rule = MappingFactory.eINSTANCE.createTimeThrottleChangeRule();
		rule.setId("throttle");
		rule.setInterval(interval);
		rule.setIntervalUnit(unit);
		return rule;
	}

}
