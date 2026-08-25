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

import java.util.Collections;
import java.util.Map;

import org.eclipse.emf.ecore.EAttribute;
import org.eclipse.fennec.event.atlas.mapping.SensinactMapperConstants;
import org.eclipse.fennec.event.atlas.model.mapping.ResourceMapping;

/**
 * Reads the metadata a {@link ResourceMapping} carries, from whichever of its two sources
 * supplies it.
 * <p>
 * A {@code ResourceMapping} extends {@link EAttribute}, so metadata can be written either as
 * a field in the mapping XMI or as an EAnnotation - and a resource generated from a
 * {@code ReferenceMapping} gets the annotations of the source attribute copied onto it, which
 * is how a domain {@code .ecore} supplies units for resources that appear in no XMI. Both
 * sources have to be consulted in the same order everywhere, or the runtime and its validation
 * disagree about what a resource's unit is.
 * @author Ilenia Salvadori
 * @since 25.08.2026
 */
final class MappingAnnotations {

	private MappingAnnotations() {
	}

	/**
	 * Resolves the unit of a resource: the {@code unit} field if it is set, else the
	 * {@code sensinact.mapping} annotation's unit.
	 * @param resourceMapping the resource to read. Parameter must not be <code>null</code>
	 * @return the unit, or <code>null</code> if neither source supplies one
	 */
	static String effectiveUnit(ResourceMapping resourceMapping) {
		if (resourceMapping.getUnit() != null) {
			return resourceMapping.getUnit();
		}
		return annotationValue(resourceMapping, SensinactMapperConstants.SENSINACT_MAPPING_ANNOTATION_SOURCE,
				SensinactMapperConstants.SENSINACT_MAPPING_UNIT);
	}

	/**
	 * @return the annotation detail, or <code>null</code> if the annotation or the key is
	 * absent - so a caller can tell "not specified" from an empty value and leave the metadata
	 * out instead of publishing a blank one
	 */
	static String annotationValue(EAttribute eAttribute, String source, String detailKey) {
		if (eAttribute.getEAnnotation(source) == null) {
			return null;
		}
		return eAttribute.getEAnnotation(source).getDetails().get(detailKey);
	}

	/**
	 * @return every detail of an annotation, or an empty map if it is absent
	 */
	static Map<String, String> annotationDetails(EAttribute eAttribute, String source) {
		if (eAttribute.getEAnnotation(source) == null) {
			return Collections.emptyMap();
		}
		return eAttribute.getEAnnotation(source).getDetails().map();
	}

}
