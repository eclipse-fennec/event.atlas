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
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;

import org.eclipse.emf.ecore.EcorePackage;
import org.eclipse.fennec.event.atlas.model.mapping.MappingFactory;
import org.eclipse.fennec.event.atlas.model.mapping.MappingSelector;
import org.eclipse.fennec.event.atlas.model.mapping.ProviderMapping;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import io.cloudevents.model.ce.CloudEvent;
import io.cloudevents.model.ce.CloudEventsFactory;
import io.cloudevents.model.ce.CloudEventsPackage;

/**
 * What a {@link MappingSelector} says, and what it refuses to say.
 * <p>
 * Every condition it sets must hold; a condition it leaves unset is not a condition. The two
 * rules that are easy to get backwards are pinned here: an exclude beats an include, and an
 * envelope that carries no {@code dataschema} does not satisfy a selector that names one.
 * @author Ilenia Salvadori
 */
public class CompiledSelectorTest {

	private static final String ILSA = "https://model.data.jena.de/sensinact/ilsa/1.0#//TLSignalState";

	@Nested
	@DisplayName("Source")
	class Source {

		@Test
		@DisplayName("An include list means 'only these', read as MQTT topic filters")
		void includes() {
			CompiledSelector selector = compile(selector(List.of("5g/ilsa/#"), List.of(), null, null));

			assertTrue(selector.matches(envelope("5g/ilsa/paradiesbruecke/signalstate")));
			assertTrue(selector.matches(envelope("5g/ilsa")), "'a/#' covers the parent too");
			assertFalse(selector.matches(envelope("5g/traficam/cam1")));
		}

		@Test
		@DisplayName("Any include is enough, so a mapping can serve two unrelated topic trees")
		void includes_areAlternatives() {
			CompiledSelector selector = compile(selector(List.of("5g/ilsa/#", "rest/ilsa"), List.of(), null, null));

			assertTrue(selector.matches(envelope("5g/ilsa/x/signalstate")));
			assertTrue(selector.matches(envelope("rest/ilsa")));
			assertFalse(selector.matches(envelope("rest/traficam")));
		}

		@Test
		@DisplayName("An exclude beats every include - a topic filter cannot say 'except', so the selector does")
		void excludes_beatIncludes() {
			// the case the plan is written around: a general ILSA mapping that must stop
			// applying at the one intersection a specific mapping replaces it for
			CompiledSelector selector = compile(
					selector(List.of("5g/ilsa/#"), List.of("5g/ilsa/lobedaer-platz/#"), null, null));

			assertTrue(selector.matches(envelope("5g/ilsa/paradiesbruecke/signalstate")));
			assertFalse(selector.matches(envelope("5g/ilsa/lobedaer-platz/signalstate")));
		}

		@Test
		@DisplayName("Excludes alone mean 'everything except', which is a selector worth having")
		void excludes_withoutIncludes() {
			CompiledSelector selector = compile(selector(List.of(), List.of("5g/ilsa/lobedaer-platz/#"), null, null));

			assertTrue(selector.matches(envelope("anything/at/all")));
			assertFalse(selector.matches(envelope("5g/ilsa/lobedaer-platz/signalstate")));
		}
	}

	@Nested
	@DisplayName("Type and dataschema")
	class Attributes {

		@Test
		@DisplayName("A type is compared exactly, and says nothing about the source")
		void type() {
			CompiledSelector selector = compile(selector(List.of(), List.of(), "de.jena.ilsa.signal", null));

			assertTrue(selector.matches(envelopeWith("any/topic", "de.jena.ilsa.signal", null)));
			assertFalse(selector.matches(envelopeWith("any/topic", "de.jena.ilsa.config", null)));
		}

		@Test
		@DisplayName("A dataschema is what an envelope-rooted mapping says the payload is")
		void dataschema() {
			CompiledSelector selector = compile(selector(List.of(), List.of(), null, ILSA));

			assertTrue(selector.matches(envelopeWith("any/topic", null, ILSA)));
			assertFalse(selector.matches(envelopeWith("any/topic", null, ILSA + "Config")));
		}

		@Test
		@DisplayName("An absent dataschema is no match, not a wildcard")
		void dataschema_absentOnTheEnvelope_doesNotMatch() {
			// it is optional on a CloudEvent, so a selector naming one has to treat silence as
			// "not this payload" - otherwise every unschema'd message would match every
			// mapping that bothered to be specific
			CompiledSelector selector = compile(selector(List.of(), List.of(), null, ILSA));

			assertFalse(selector.matches(envelopeWith("any/topic", null, null)));
		}

		@Test
		@DisplayName("Every condition set must hold")
		void conditions_areAnded() {
			CompiledSelector selector = compile(
					selector(List.of("5g/ilsa/#"), List.of(), "de.jena.ilsa.signal", ILSA));

			assertTrue(selector.matches(envelopeWith("5g/ilsa/x", "de.jena.ilsa.signal", ILSA)));
			assertFalse(selector.matches(envelopeWith("5g/other/x", "de.jena.ilsa.signal", ILSA)));
			assertFalse(selector.matches(envelopeWith("5g/ilsa/x", "de.jena.other", ILSA)));
			assertFalse(selector.matches(envelopeWith("5g/ilsa/x", "de.jena.ilsa.signal", null)));
		}
	}

	@Test
	@DisplayName("A selector never matches a push that carries no envelope")
	void noEnvelope_neverMatches() {
		// not a special case: there is no source, no type and no schema to compare, so nothing
		// can match. A mapping meant for a programmatic push carries no selector at all
		assertFalse(compile(selector(List.of("5g/ilsa/#"), List.of(), null, null)).matches(null));
		assertFalse(compile(selector(List.of(), List.of(), "de.jena.ilsa.signal", null)).matches(null));
	}

	@Nested
	@DisplayName("Refusals")
	class Refusals {

		@Test
		@DisplayName("A selector that sets no condition is refused rather than meaning 'everything'")
		void emptySelector_isRefused() {
			// it would match every message and still take the mapping out of an envelope-less
			// push - one mapping behaving as two, depending on how it is reached
			IllegalArgumentException e = assertThrows(IllegalArgumentException.class,
					() -> compile(selector(List.of(), List.of(), null, null)));

			assertTrue(e.getMessage().contains("no condition"), e.getMessage());
		}

		@Test
		@DisplayName("A blank attribute is not a condition, so a selector of blanks is still empty")
		void blankAttributes_areNotConditions() {
			assertThrows(IllegalArgumentException.class, () -> compile(selector(List.of(), List.of(), "  ", "")));
		}

		@Test
		@DisplayName("A malformed filter is refused, naming the attribute and the index")
		void malformedFilter_isRefused() {
			IllegalArgumentException e = assertThrows(IllegalArgumentException.class,
					() -> compile(selector(List.of("5g/ilsa/#", "a/#/c"), List.of(), null, null)));

			assertTrue(e.getMessage().contains("source[1]"), e.getMessage());
		}

		@Test
		@DisplayName("A malformed exclude is refused too - one that never matches is the silent double write")
		void malformedExclude_isRefused() {
			IllegalArgumentException e = assertThrows(IllegalArgumentException.class,
					() -> compile(selector(List.of("5g/ilsa/#"), List.of("a+b"), null, null)));

			assertTrue(e.getMessage().contains("sourceExcludes[0]"), e.getMessage());
		}

		@Test
		@DisplayName("An empty filter entry is refused")
		void blankFilter_isRefused() {
			assertThrows(IllegalArgumentException.class, () -> compile(selector(List.of(""), List.of(), null, null)));
		}
	}

	@Nested
	@DisplayName("What registration does with a refusal")
	class Registration {

		@Test
		@DisplayName("A mapping with an unusable selector is refused by name, not registered and silent")
		void unusableSelector_refusesTheMapping() {
			// refused for the whole mapping: a selector that does not compile selects nothing,
			// so registering it anyway would leave the mapping in the registry and out of
			// service at the same time - the failure a selector exists to prevent
			ProviderMapping mapping = MappingFactory.eINSTANCE.createProviderMapping();
			mapping.setMid("ilsa-signal-state");
			mapping.setSelector(selector(List.of("a/#/c"), List.of(), null, null));

			String refusal = ProviderMappingRegistryImpl.refusedSelector(mapping);

			assertNotNull(refusal, "a malformed filter must be refused");
			assertTrue(refusal.contains("source[0]"), refusal);
		}

		@Test
		@DisplayName("A mapping without a selector is never refused - it serves every message of its EClass")
		void noSelector_isFine() {
			ProviderMapping mapping = MappingFactory.eINSTANCE.createProviderMapping();
			mapping.setMid("dwd-weather");

			assertNull(ProviderMappingRegistryImpl.refusedSelector(mapping));
		}

		@Test
		@DisplayName("A mapping naming CloudEvent is envelope-rooted, and is warned about without a selector")
		void envelopeRooted_isRecognised() {
			// saying CloudEvent claims every message in the runtime, of every model, and the
			// payload type is sayable nowhere else - so a selector is what makes it specific.
			// Warned rather than refused: a runtime serving one device family may want it
			ProviderMapping mapping = MappingFactory.eINSTANCE.createProviderMapping();
			mapping.setMid("envelope-rooted");
			mapping.getProviderClasses().add(CloudEventsPackage.Literals.CLOUD_EVENT);

			assertTrue(ProviderMappingRegistryImpl.isEnvelopeRooted(mapping));
			assertNull(ProviderMappingRegistryImpl.refusedSelector(mapping), "it is warned about, never refused");
		}

		@Test
		@DisplayName("A mapping naming a domain EClass is payload-rooted")
		void payloadRooted_isRecognised() {
			ProviderMapping mapping = MappingFactory.eINSTANCE.createProviderMapping();
			mapping.setMid("dwd-weather");
			mapping.getProviderClasses().add(EcorePackage.Literals.EOBJECT);

			assertFalse(ProviderMappingRegistryImpl.isEnvelopeRooted(mapping));
		}

		@Test
		@DisplayName("A usable selector is not refused")
		void usableSelector_isFine() {
			ProviderMapping mapping = MappingFactory.eINSTANCE.createProviderMapping();
			mapping.setMid("ilsa-signal-state");
			mapping.setSelector(selector(List.of("5g/ilsa/#"), List.of("5g/ilsa/lobedaer-platz/#"), null, null));

			assertNull(ProviderMappingRegistryImpl.refusedSelector(mapping));
		}
	}

	private static CompiledSelector compile(MappingSelector selector) {
		return CompiledSelector.compile(selector);
	}

	private static MappingSelector selector(List<String> includes, List<String> excludes, String type,
			String dataschema) {
		MappingSelector selector = MappingFactory.eINSTANCE.createMappingSelector();
		selector.getSource().addAll(includes);
		selector.getSourceExcludes().addAll(excludes);
		selector.setType(type);
		selector.setDataschema(dataschema);
		return selector;
	}

	private static CloudEvent envelope(String source) {
		return envelopeWith(source, "de.jena.test", null);
	}

	private static CloudEvent envelopeWith(String source, String type, String dataschema) {
		CloudEvent envelope = CloudEventsFactory.eINSTANCE.createCloudEvent();
		envelope.setEventId("test-1");
		envelope.setSource(source);
		envelope.setType(type);
		envelope.setDataschema(dataschema);
		return envelope;
	}

}
