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
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;

import java.util.Map;

import org.eclipse.emf.ecore.EObject;
import org.eclipse.fennec.event.atlas.mapping.ValueMapper.ValidationResult;
import org.eclipse.fennec.event.atlas.model.mapping.MappingFactory;
import org.eclipse.fennec.event.atlas.model.mapping.ProviderMapping;
import org.eclipse.fennec.event.atlas.model.mapping.ReferenceMapping;
import org.eclipse.fennec.event.atlas.model.mapping.ServiceMapping;
import org.eclipse.sensinact.core.twin.SensinactDigitalTwin;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/**
 * A device that splits one reporting window over several messages sends a different subset of
 * its lines each time. Each line is a service, selected by a collection filter, and a service
 * whose line is not in this message has to be skipped - not reported as a broken mapping, and
 * above all not allowed to fail the whole instance, which would drop the lines that did
 * arrive.
 * <p>
 * The twin is not needed for any of this: both entry points used here read the instance only.
 */
public class FilteredServiceSourceTest {

	private final LineModel model = new LineModel();

	@Test
	@DisplayName("Only the lines this message carries are read")
	void filteredServices_readTheLinesOfThisMessage() {
		EObject message = model.report("21:36-22:56", model.entry("line_3", 3, 12), model.entry("line_1", 1, 8));

		Map<String, Object> values = mapper(mapping("key=line_1", "key=line_5")).mapResourceValues(message);

		assertEquals(8, values.get("line1.person"));
		assertEquals(1, values.get("line1.line_number"));
		assertFalse(values.keySet().stream().anyMatch(key -> key.startsWith("line5.")),
				"line_5 is in another message, so this one reports nothing for it: " + values);
	}

	@Test
	@DisplayName("A line this message does not carry is no validation finding")
	void absentLine_isNotAnError() {
		EObject message = model.report("21:36-22:56", model.entry("line_1", 1, 8));

		ValidationResult validation = mapper(mapping("key=line_1", "key=line_5")).validateInstance(message);

		assertTrue(validation.isValid(), "validation failed with " + validation.getErrors());
		assertTrue(validation.getErrors().isEmpty());
	}

	@Test
	@DisplayName("Without a filter an index that no element answers is still an error")
	void unselectableIndex_remainsAnError() {
		EObject message = model.report("21:36-22:56", model.entry("line_1", 1, 8));
		ProviderMapping mapping = mapping();
		ReferenceMapping byIndex = referencedLine(null);
		byIndex.setCollectionIndex(4);
		mapping.getServices().add(service("line5", byIndex));

		ValidationResult validation = mapper(mapping).validateInstance(message);

		assertFalse(validation.isValid(), "an index mapping that does not fit the payload is a broken mapping");
	}

	@Test
	@DisplayName("A filter on the entry's value selects the same line as one on its key")
	void filterOnTheValue_selectsTheSameLine() {
		EObject message = model.report("21:36-22:56", model.entry("line_3", 3, 12), model.entry("line_1", 1, 8));

		Map<String, Object> values = mapper(mapping("value.line_number=3")).mapResourceValues(message);

		assertEquals(12, values.get("line1.person"));
	}

	private ValueMapperImpl mapper(ProviderMapping mapping) {
		// Reading an instance never reaches the twin; a mock keeps the constructor happy.
		return new ValueMapperImpl(mock(SensinactDigitalTwin.class), mapping);
	}

	/** A mapping with one service per filter expression, named line1, line2, ... in order. */
	private ProviderMapping mapping(String... collectionFilters) {
		ProviderMapping mapping = MappingFactory.eINSTANCE.createProviderMapping();
		mapping.setMid("lines");
		mapping.setName(MappingFactory.eINSTANCE.createNameMapping());
		mapping.getName().setName("report");
		mapping.getProviderClasses().add(model.report);
		for (int i = 0; i < collectionFilters.length; i++) {
			mapping.getServices().add(service("line" + (i + 1), referencedLine(collectionFilters[i])));
		}
		return mapping;
	}

	private static ServiceMapping service(String mid, ReferenceMapping source) {
		ServiceMapping service = MappingFactory.eINSTANCE.createServiceMapping();
		service.setMid(mid);
		service.setReferencedResource(source);
		return service;
	}

	/** The line of the report's map a service reads: lines[selected].value. */
	private ReferenceMapping referencedLine(String collectionFilter) {
		ReferenceMapping referenced = MappingFactory.eINSTANCE.createReferenceMapping();
		referenced.setCollectionFilter(collectionFilter);
		referenced.getFeaturePath().add(model.lines);
		referenced.getFeaturePath().add(model.value);
		return referenced;
	}
}
