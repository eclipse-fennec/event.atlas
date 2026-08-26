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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;

import org.eclipse.emf.ecore.EAttribute;
import org.eclipse.emf.ecore.EcoreFactory;
import org.eclipse.fennec.event.atlas.mapping.impl.ProviderModelSensinactMapper.BindingResolver;
import org.eclipse.fennec.event.atlas.model.mapping.ChangeRule;
import org.eclipse.fennec.event.atlas.model.mapping.DeletionRule;
import org.eclipse.fennec.event.atlas.model.mapping.DurationUnit;
import org.eclipse.fennec.event.atlas.model.mapping.MappingFactory;
import org.eclipse.fennec.event.atlas.model.mapping.PercentageChangeRule;
import org.eclipse.fennec.event.atlas.model.mapping.ReferenceMapping;
import org.eclipse.fennec.event.atlas.model.mapping.ReferenceResourceBinding;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/**
 * Resolution order of a {@code ReferenceMapping}'s bindings: a binding that names the
 * attribute beats the level's default (the binding with an empty attribute list), which beats
 * whatever the enclosing reference mapping declared - per setting, independently.
 */
public class BindingResolverTest {

	private final EAttribute windSpeed = attribute("windSpeed");
	private final EAttribute temperature = attribute("temperature");

	@Test
	@DisplayName("A binding naming the attribute wins over the level's default")
	void namedBinding_beatsDefault() {
		ChangeRule specific = percentage("pct-5");
		ChangeRule fallback = percentage("pct-50");

		ReferenceMapping refMapping = referenceMapping(binding(specific, null, null, windSpeed),
				binding(fallback, null, null));
		BindingResolver resolver = new BindingResolver(null, refMapping);

		assertSame(specific, resolver.changeRule(windSpeed));
		assertSame(fallback, resolver.changeRule(temperature), "not named, so the default applies");
	}

	@Test
	@DisplayName("Without a default, an attribute no binding names has no rule")
	void unnamedAttribute_withoutDefault_hasNoRule() {
		ReferenceMapping refMapping = referenceMapping(binding(percentage("pct-5"), null, null, windSpeed));
		BindingResolver resolver = new BindingResolver(null, refMapping);

		assertNull(resolver.changeRule(temperature));
		assertNull(resolver.unit(temperature));
		assertNull(resolver.deletionRule(temperature));
	}

	@Test
	@DisplayName("A reference mapping without bindings resolves nothing")
	void noBindings_resolvesNothing() {
		BindingResolver resolver = new BindingResolver(null, referenceMapping());

		assertNull(resolver.changeRule(windSpeed));
		assertNull(resolver.deletionRule(windSpeed));
		assertNull(resolver.unit(windSpeed));
	}

	@Test
	@DisplayName("Each setting resolves on its own: a binding may override one and inherit another")
	void settings_resolveIndependently() {
		ChangeRule specific = percentage("pct-5");
		DeletionRule keep90 = deletionRule("keep-90d");

		// The named binding sets only the change rule; the unit and the deletion rule have to
		// come from the default rather than being reset to nothing.
		ReferenceMapping refMapping = referenceMapping(binding(specific, null, null, windSpeed),
				binding(percentage("pct-50"), keep90, "m/s"));
		BindingResolver resolver = new BindingResolver(null, refMapping);

		assertSame(specific, resolver.changeRule(windSpeed));
		assertSame(keep90, resolver.deletionRule(windSpeed));
		assertEquals("m/s", resolver.unit(windSpeed));
	}

	@Test
	@DisplayName("A nested reference mapping inherits what its parent declared")
	void nested_inheritsFromParent() {
		ChangeRule parentSpecific = percentage("pct-5");
		ChangeRule parentDefault = percentage("pct-50");

		ReferenceMapping parent = referenceMapping(binding(parentSpecific, null, null, windSpeed),
				binding(parentDefault, null, null));
		BindingResolver nested = new BindingResolver(new BindingResolver(null, parent), referenceMapping());

		assertSame(parentSpecific, nested.changeRule(windSpeed), "attributes are addressed globally");
		assertSame(parentDefault, nested.changeRule(temperature));
	}

	@Test
	@DisplayName("A nested binding overrides the one it inherits")
	void nested_overridesParent() {
		ReferenceMapping parent = referenceMapping(binding(percentage("parent-default"), null, null));
		ChangeRule nestedRule = percentage("nested");

		ReferenceMapping child = referenceMapping(binding(nestedRule, null, null, temperature));
		BindingResolver resolver = new BindingResolver(new BindingResolver(null, parent), child);

		assertSame(nestedRule, resolver.changeRule(temperature), "the nearest declaration wins");
		assertEquals("parent-default", resolver.changeRule(windSpeed).getId(), "still inherited");
	}

	@Test
	@DisplayName("An attribute named by two bindings resolves to the first of them")
	void duplicateAttribute_firstWins() {
		ChangeRule first = percentage("first");
		ReferenceMapping refMapping = referenceMapping(binding(first, null, null, windSpeed),
				binding(percentage("second"), null, null, windSpeed));
		BindingResolver resolver = new BindingResolver(null, refMapping);

		assertSame(first, resolver.changeRule(windSpeed));
	}

	@Test
	@DisplayName("A second default binding is ignored")
	void duplicateDefault_firstWins() {
		ChangeRule first = percentage("first");
		ReferenceMapping refMapping = referenceMapping(binding(first, null, null),
				binding(percentage("second"), null, null));
		BindingResolver resolver = new BindingResolver(null, refMapping);

		assertSame(first, resolver.changeRule(windSpeed));
	}

	private static EAttribute attribute(String name) {
		EAttribute attribute = EcoreFactory.eINSTANCE.createEAttribute();
		attribute.setName(name);
		return attribute;
	}

	private static ReferenceMapping referenceMapping(ReferenceResourceBinding... bindings) {
		ReferenceMapping refMapping = MappingFactory.eINSTANCE.createReferenceMapping();
		for (ReferenceResourceBinding binding : bindings) {
			refMapping.getBindings().add(binding);
		}
		return refMapping;
	}

	private static ReferenceResourceBinding binding(ChangeRule changeRule, DeletionRule deletionRule, String unit,
			EAttribute... attributes) {
		ReferenceResourceBinding binding = MappingFactory.eINSTANCE.createReferenceResourceBinding();
		binding.setChangeRule(changeRule);
		binding.setDeletionRule(deletionRule);
		binding.setUnit(unit);
		for (EAttribute attribute : attributes) {
			binding.getAttributes().add(attribute);
		}
		return binding;
	}

	private static PercentageChangeRule percentage(String id) {
		PercentageChangeRule rule = MappingFactory.eINSTANCE.createPercentageChangeRule();
		rule.setId(id);
		rule.setPercentage(5.0d);
		return rule;
	}

	private static DeletionRule deletionRule(String id) {
		DeletionRule rule = MappingFactory.eINSTANCE.createDeletionRule();
		rule.setId(id);
		rule.setRetention(90);
		rule.setRetentionUnit(DurationUnit.DAYS);
		return rule;
	}

}
