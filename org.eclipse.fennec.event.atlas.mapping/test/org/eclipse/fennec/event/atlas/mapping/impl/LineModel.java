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

import java.util.List;

import org.eclipse.emf.ecore.EAttribute;
import org.eclipse.emf.ecore.EClass;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.EPackage;
import org.eclipse.emf.ecore.EReference;
import org.eclipse.emf.ecore.EcoreFactory;
import org.eclipse.emf.ecore.EcorePackage;
import org.eclipse.emf.ecore.util.EcoreUtil;

/**
 * A dynamic stand-in for the shape a line-crossing counter sends: a report carrying its lines
 * as a map, keyed {@code line_1}, {@code line_2}, ... and filled with a different subset per
 * message.
 * <p>
 * Dynamic on purpose. A model that reaches a runtime from a Model Atlas is loaded from its
 * {@code .ecore} and never from generated code, so the map entries are
 * {@code DynamicEObjectImpl.BasicEMapEntry} instances - which is the form the engine has to
 * navigate.
 */
class LineModel {

	private static final String NS_URI = "http://example.org/event.atlas/lines/1.0";

	final EPackage ePackage = EcoreFactory.eINSTANCE.createEPackage();
	final EClass line = EcoreFactory.eINSTANCE.createEClass();
	final EClass lineEntry = EcoreFactory.eINSTANCE.createEClass();
	final EClass report = EcoreFactory.eINSTANCE.createEClass();

	final EAttribute lineNumber = attribute("line_number", EcorePackage.Literals.EINT);
	final EAttribute person = attribute("person", EcorePackage.Literals.EINT);
	final EAttribute key = attribute("key", EcorePackage.Literals.ESTRING);
	final EAttribute window = attribute("window", EcorePackage.Literals.ESTRING);
	final EReference value = reference("value", null);
	final EReference lines = reference("lines", null);

	LineModel() {
		ePackage.setName("lines");
		ePackage.setNsPrefix("lines");
		ePackage.setNsURI(NS_URI);

		line.setName("Line");
		line.getEStructuralFeatures().addAll(List.of(lineNumber, person));

		lineEntry.setName("LineEntry");
		lineEntry.setInstanceClassName("java.util.Map$Entry");
		value.setEType(line);
		value.setContainment(true);
		lineEntry.getEStructuralFeatures().addAll(List.of(key, value));

		report.setName("Report");
		lines.setEType(lineEntry);
		lines.setContainment(true);
		lines.setUpperBound(-1);
		report.getEStructuralFeatures().addAll(List.of(window, lines));

		ePackage.getEClassifiers().addAll(List.of(line, lineEntry, report));
	}

	EObject entry(String name, int number, int people) {
		EObject entry = ePackage.getEFactoryInstance().create(lineEntry);
		EObject counts = EcoreUtil.create(line);
		counts.eSet(lineNumber, number);
		counts.eSet(person, people);
		entry.eSet(key, name);
		entry.eSet(value, counts);
		return entry;
	}

	@SuppressWarnings("unchecked")
	EObject report(String reportingWindow, EObject... entries) {
		EObject report = EcoreUtil.create(this.report);
		report.eSet(window, reportingWindow);
		((List<EObject>) report.eGet(lines)).addAll(List.of(entries));
		return report;
	}

	private static EAttribute attribute(String name, org.eclipse.emf.ecore.EDataType type) {
		EAttribute attribute = EcoreFactory.eINSTANCE.createEAttribute();
		attribute.setName(name);
		attribute.setEType(type);
		return attribute;
	}

	private static EReference reference(String name, EClass type) {
		EReference reference = EcoreFactory.eINSTANCE.createEReference();
		reference.setName(name);
		reference.setEType(type);
		return reference;
	}
}
