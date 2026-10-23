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
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;
import java.util.Optional;

import org.eclipse.emf.ecore.EObject;
import org.eclipse.fennec.event.atlas.model.mapping.MappingFactory;
import org.eclipse.fennec.event.atlas.model.mapping.ReferenceMapping;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/**
 * Selecting a collection element by content: what a payload whose members arrive in no fixed
 * order needs, and what a positional index cannot do.
 */
public class CollectionFilterTest {

	private final LineModel model = new LineModel();

	@Test
	@DisplayName("An entry is selected by its map key")
	void mapKey_selectsTheEntry() {
		List<EObject> entries = List.of(model.entry("line_3", 3, 12), model.entry("line_1", 1, 8));

		Optional<Object> selected = filter("key=line_1").get().select(entries);

		assertSame(entries.get(1), selected.orElseThrow());
	}

	@Test
	@DisplayName("A nested path reaches into the entry's value")
	void nestedPath_selectsOnTheValue() {
		List<EObject> entries = List.of(model.entry("line_3", 3, 12), model.entry("line_1", 1, 8));

		Optional<Object> selected = filter("value.line_number=3").get().select(entries);

		assertSame(entries.get(0), selected.orElseThrow());
	}

	@Test
	@DisplayName("A number matches its literal, and a quoted literal its text")
	void literals_areComparedAsText() {
		EObject entry = model.entry("line_7", 7, 23);

		assertTrue(filter("value.line_number=7").get().matches(entry));
		assertTrue(filter("key=\"line_7\"").get().matches(entry));
		assertFalse(filter("value.line_number=70").get().matches(entry));
	}

	@Test
	@DisplayName("A line the payload does not carry selects nothing instead of the wrong one")
	void noMatch_selectsNothing() {
		List<EObject> entries = List.of(model.entry("line_1", 1, 8));

		assertTrue(filter("key=line_5").get().select(entries).isEmpty());
	}

	@Test
	@DisplayName("A path that does not exist on the element matches nothing rather than failing")
	void unknownPath_matchesNothing() {
		assertFalse(filter("value.bicycles=1").get().matches(model.entry("line_1", 1, 8)));
	}

	@Test
	@DisplayName("A malformed expression is ignored, which leaves the collection index in charge")
	void malformedExpression_isIgnored() {
		assertTrue(CollectionFilter.of(referenceMapping("line_1"), "test").isEmpty());
		assertTrue(CollectionFilter.of(referenceMapping("=line_1"), "test").isEmpty());
		assertTrue(CollectionFilter.of(referenceMapping("value..key=1"), "test").isEmpty());
		assertTrue(CollectionFilter.of(referenceMapping("  "), "test").isEmpty());
		assertTrue(CollectionFilter.of(null, "test").isEmpty());
	}

	@Test
	@DisplayName("The expression reads back as written, for the log line that reports no match")
	void toString_readsBackAsWritten() {
		assertEquals("value.line_number=5", filter("value.line_number=5").get().toString());
	}

	private Optional<CollectionFilter> filter(String expression) {
		return CollectionFilter.of(referenceMapping(expression), "test");
	}

	private static ReferenceMapping referenceMapping(String collectionFilter) {
		ReferenceMapping referenceMapping = MappingFactory.eINSTANCE.createReferenceMapping();
		referenceMapping.setCollectionFilter(collectionFilter);
		return referenceMapping;
	}
}
