/**
 * Copyright (c) 2012 - 2025 Data In Motion and others.
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
package org.eclipse.fennec.event.atlas.mapping.tests;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNotSame;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.util.Map;

import org.eclipse.emf.common.util.URI;
import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.emf.ecore.resource.ResourceSet;
import org.eclipse.emf.ecore.resource.impl.ResourceSetImpl;
import org.eclipse.emf.ecore.util.EcoreUtil;
import org.eclipse.emf.ecore.xmi.impl.XMIResourceFactoryImpl;
import org.eclipse.fennec.event.atlas.model.mapping.DeletionRule;
import org.eclipse.fennec.event.atlas.model.mapping.DurationUnit;
import org.eclipse.fennec.event.atlas.model.mapping.MappingFactory;
import org.eclipse.fennec.event.atlas.model.mapping.MappingPackage;
import org.eclipse.fennec.event.atlas.model.mapping.PercentageChangeRule;
import org.eclipse.fennec.event.atlas.model.mapping.PersistenceRuleRegistry;
import org.eclipse.fennec.event.atlas.model.mapping.ReferenceMapping;
import org.eclipse.fennec.event.atlas.model.mapping.ReferenceResourceBinding;
import org.eclipse.fennec.event.atlas.model.mapping.ResourceMapping;
import org.eclipse.fennec.event.atlas.model.mapping.ServiceMapping;
import org.eclipse.fennec.event.atlas.model.mapping.TimeThrottleChangeRule;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/**
 * Verifies the persistence-rule model: durations (amount + {@link DurationUnit}) round-trip
 * through XMI with plain EMF serialization (no custom datatype conversion), and rules are
 * owned by the resource mapping or reference binding that uses them - which is why a rule
 * handed to a second owner is relocated rather than shared.
 */
public class PersistenceRuleRoundTripTest {

	private static ResourceSet newResourceSet() {
		ResourceSet rs = new ResourceSetImpl();
		rs.getResourceFactoryRegistry().getExtensionToFactoryMap().put("*", new XMIResourceFactoryImpl());
		rs.getPackageRegistry().put(MappingPackage.eNS_URI, MappingPackage.eINSTANCE);
		return rs;
	}

	@Test
	@DisplayName("Durations (amount + unit) round-trip through XMI")
	void durations_roundTrip() throws Exception {
		MappingFactory f = MappingFactory.eINSTANCE;

		PersistenceRuleRegistry registry = f.createPersistenceRuleRegistry();

		TimeThrottleChangeRule throttle = f.createTimeThrottleChangeRule();
		throttle.setId("throttle-5m");
		throttle.setInterval(5);
		throttle.setIntervalUnit(DurationUnit.MINUTES);
		registry.getChangeRules().add(throttle);

		DeletionRule deletion = f.createDeletionRule();
		deletion.setId("keep-90d");
		deletion.setRetention(90);
		deletion.setRetentionUnit(DurationUnit.DAYS);
		deletion.setCleanupInterval(1);
		deletion.setCleanupIntervalUnit(DurationUnit.DAYS);
		registry.getDeletionRules().add(deletion);

		// Save to XMI
		Resource saved = newResourceSet().createResource(URI.createURI("mem://rules.xmi"));
		saved.getContents().add(registry);
		ByteArrayOutputStream out = new ByteArrayOutputStream();
		saved.save(out, Map.of());
		String xmi = out.toString("UTF-8");

		assertTrue(xmi.contains("interval=\"5\""), () -> "expected interval amount in:\n" + xmi);
		assertTrue(xmi.contains("intervalUnit=\"MINUTES\""), () -> "expected interval unit in:\n" + xmi);
		assertTrue(xmi.contains("retention=\"90\""), () -> "expected retention amount in:\n" + xmi);

		// Load back into a fresh resource set
		Resource loaded = newResourceSet().createResource(URI.createURI("mem://rules.xmi"));
		loaded.load(new ByteArrayInputStream(out.toByteArray()), Map.of());
		PersistenceRuleRegistry reg2 = (PersistenceRuleRegistry) loaded.getContents().get(0);

		TimeThrottleChangeRule throttle2 = (TimeThrottleChangeRule) reg2.getChangeRules().get(0);
		DeletionRule deletion2 = (DeletionRule) reg2.getDeletionRules().get(0);

		assertEquals(Integer.valueOf(5), throttle2.getInterval());
		assertEquals(DurationUnit.MINUTES, throttle2.getIntervalUnit());
		assertEquals(Integer.valueOf(90), deletion2.getRetention());
		assertEquals(DurationUnit.DAYS, deletion2.getRetentionUnit());
		assertEquals(Integer.valueOf(1), deletion2.getCleanupInterval());
		assertEquals(DurationUnit.DAYS, deletion2.getCleanupIntervalUnit());
	}

	@Test
	@DisplayName("A rule is owned by the resource that uses it")
	void rule_isContainedByItsResource() {
		MappingFactory f = MappingFactory.eINSTANCE;

		ResourceMapping temperature = f.createResourceMapping();
		temperature.setMid("temperature");
		DeletionRule keep90 = f.createDeletionRule();
		keep90.setRetention(90);
		keep90.setRetentionUnit(DurationUnit.DAYS);
		temperature.setDeletionRule(keep90);

		assertSame(temperature, keep90.eContainer(), "the resource owns its rule");
	}

	@Test
	@DisplayName("Handing one rule instance to a second resource moves it, so rules must be copied")
	void rule_setOnASecondResource_isMovedNotShared() {
		MappingFactory f = MappingFactory.eINSTANCE;

		DeletionRule keep90 = f.createDeletionRule();
		keep90.setRetention(90);
		keep90.setRetentionUnit(DurationUnit.DAYS);

		ResourceMapping temperature = f.createResourceMapping();
		temperature.setMid("temperature");
		temperature.setDeletionRule(keep90);

		ResourceMapping humidity = f.createResourceMapping();
		humidity.setMid("humidity");
		humidity.setDeletionRule(keep90);

		// Containment allows exactly one container, so the second set relocated the rule
		// instead of sharing it. This is why ProviderModelSensinactMapper copies a binding's
		// rule onto each generated resource rather than assigning it.
		assertNull(temperature.getDeletionRule(), "the rule was moved away from the first resource");
		assertSame(keep90, humidity.getDeletionRule());

		humidity.setDeletionRule(EcoreUtil.copy(keep90));
		temperature.setDeletionRule(EcoreUtil.copy(keep90));
		assertNotNull(temperature.getDeletionRule(), "copies let both resources carry the rule");
		assertNotNull(humidity.getDeletionRule());
		assertNotSame(temperature.getDeletionRule(), humidity.getDeletionRule());
		assertEquals(Integer.valueOf(90), temperature.getDeletionRule().getRetention());
	}

	@Test
	@DisplayName("A rule contained by a reference binding round-trips through XMI")
	void ruleInABinding_roundTrips() throws Exception {
		MappingFactory f = MappingFactory.eINSTANCE;

		PercentageChangeRule percentage = f.createPercentageChangeRule();
		percentage.setPercentage(5.0d);

		ReferenceResourceBinding binding = f.createReferenceResourceBinding();
		binding.setChangeRule(percentage);
		binding.setUnit("kn");

		ReferenceMapping refMapping = f.createReferenceMapping();
		refMapping.getBindings().add(binding);

		ServiceMapping service = f.createServiceMapping();
		service.setMid("weather");
		service.setReferencedResource(refMapping);

		Resource saved = newResourceSet().createResource(URI.createURI("mem://binding.xmi"));
		saved.getContents().add(service);
		ByteArrayOutputStream out = new ByteArrayOutputStream();
		saved.save(out, Map.of());
		String xmi = out.toString("UTF-8");

		assertTrue(xmi.contains("percentage=\"5.0\""), () -> "expected an inline rule in:\n" + xmi);
		assertFalse(xmi.contains("href="), () -> "a contained rule needs no href:\n" + xmi);

		Resource loaded = newResourceSet().createResource(URI.createURI("mem://binding.xmi"));
		loaded.load(new ByteArrayInputStream(out.toByteArray()), Map.of());
		ServiceMapping service2 = (ServiceMapping) loaded.getContents().get(0);
		ReferenceResourceBinding binding2 = service2.getReferencedResource().getBindings().get(0);

		assertEquals("kn", binding2.getUnit());
		assertEquals(5.0d, ((PercentageChangeRule) binding2.getChangeRule()).getPercentage());
	}
}
