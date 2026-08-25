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
package org.eclipse.fennec.event.atlas.mapping;

import java.time.Instant;

import org.eclipse.fennec.event.atlas.model.mapping.ChangeRule;
import org.eclipse.fennec.event.atlas.model.mapping.ResourceMapping;
import org.osgi.annotation.versioning.ProviderType;

/**
 * Decides whether a mapped resource value is pushed into the digital twin, by applying the
 * {@link ChangeRule} its {@link ResourceMapping} references.
 * <p>
 * <b>This is an interim enforcement point.</b> A {@link ChangeRule} describes what the
 * <em>history provider</em> should persist, and the intended enforcement is a notification
 * proxy between the twin and the history store (see
 * {@code docs/WP-SN-2-persistence-rules-plan.md}). Until that exists, the rules are applied
 * here, on the way in - which also means the twin's <em>live</em> value stops at the last
 * accepted one: under a time throttle a northbound read returns a value up to one interval
 * old, and under a count rule one of every <em>n</em> readings. Set
 * {@code enabled=false} on the {@code sensinact.mapping.changerule.filter} configuration to
 * turn the filtering off and push every value again.
 * <p>
 * Implementations are stateful: comparisons are made against the last <em>accepted</em>
 * value of a resource, not against its predecessor in the payload stream, so a rejected
 * value does not become the next baseline. The first value seen for a resource is always
 * accepted.
 * @author Ilenia Salvadori
 * @since 25.08.2026
 */
@ProviderType
public interface ChangeRuleFilter {

	/**
	 * Decides whether a single mapped value should be pushed.
	 * <p>
	 * Implementations never throw for an inapplicable rule - a rule the value cannot be
	 * evaluated against (a numeric rule on a non-numeric resource, an unknown rule type)
	 * accepts the value and is logged, because dropping data on a mapping mistake is worse
	 * than storing too much.
	 * @param mappingMid the {@code mid} of the {@code ProviderMapping} being applied, the
	 * grouping key for {@link #reset(String)}. Parameter must not be <code>null</code>
	 * @param providerId the id of the target provider in the twin. Parameter must not be
	 * <code>null</code>
	 * @param serviceMid the {@code mid} of the target service. Parameter must not be
	 * <code>null</code>
	 * @param resourceMapping the resource mapping carrying the rule; a mapping without a
	 * {@link ResourceMapping#getChangeRule() changeRule} always accepts. Parameter must not
	 * be <code>null</code>
	 * @param value the mapped value, already converted to the resource's type
	 * @param timestamp the timestamp the value would be stored with - the same one that
	 * reaches the twin, so a payload that carries its own time throttles on that time rather
	 * than on arrival time. May be <code>null</code>, in which case now is used
	 * @return <code>true</code> if the value should be pushed
	 * @throws NullPointerException if any parameter but the value or the timestamp is
	 * <code>null</code>
	 */
	boolean accept(String mappingMid, String providerId, String serviceMid, ResourceMapping resourceMapping,
			Object value, Instant timestamp);

	/**
	 * Drops the retained state of every resource of a provider mapping, so the next value
	 * of each is treated as its first.
	 * <p>
	 * Called when a mapping is updated or removed: after an edit the retained baseline was
	 * gathered under the previous rule and comparing against it would apply neither rule
	 * faithfully.
	 * @param mappingMid the {@code mid} of the mapping whose state to drop. Parameter must
	 * not be <code>null</code>
	 */
	void reset(String mappingMid);

}
