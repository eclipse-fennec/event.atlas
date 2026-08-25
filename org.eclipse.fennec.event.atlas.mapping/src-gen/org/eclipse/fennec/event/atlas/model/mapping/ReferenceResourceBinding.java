/*
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
 *      Mark Hoffmann - initial API and implementation
 */
package org.eclipse.fennec.event.atlas.model.mapping;

import org.eclipse.emf.common.util.EList;

import org.eclipse.emf.ecore.EAttribute;
import org.eclipse.emf.ecore.EObject;

import org.osgi.annotation.versioning.ProviderType;

/**
 * <!-- begin-user-doc -->
 * A representation of the model object '<em><b>Reference Resource Binding</b></em>'.
 * <!-- end-user-doc -->
 *
 * <!-- begin-model-doc -->
 * Attaches persistence rules and metadata overrides to a subset of the resources that a
 * ReferenceMapping generates.
 * 
 * A reference mapping generates one resource per attribute of the referenced type, so a single
 * rule on the reference itself would have to fit all of them - which it rarely does: wind speed
 * and temperature do not tolerate the same absolute delta. A binding names the attributes it
 * applies to and carries the settings for exactly those.
 * 
 * Every setting resolves independently: for a generated resource, each feature is taken from the
 * most specific binding that sets it, and stays unset when no binding does. Resolution happens
 * when the resources are generated, so the mapping engine only ever reads these settings from the
 * generated ResourceMapping - it never looks at the binding itself.
 * <!-- end-model-doc -->
 *
 * <p>
 * The following features are supported:
 * </p>
 * <ul>
 *   <li>{@link org.eclipse.fennec.event.atlas.model.mapping.ReferenceResourceBinding#getAttributes <em>Attributes</em>}</li>
 *   <li>{@link org.eclipse.fennec.event.atlas.model.mapping.ReferenceResourceBinding#getChangeRule <em>Change Rule</em>}</li>
 *   <li>{@link org.eclipse.fennec.event.atlas.model.mapping.ReferenceResourceBinding#getDeletionRule <em>Deletion Rule</em>}</li>
 *   <li>{@link org.eclipse.fennec.event.atlas.model.mapping.ReferenceResourceBinding#getUnit <em>Unit</em>}</li>
 * </ul>
 *
 * @see org.eclipse.fennec.event.atlas.model.mapping.MappingPackage#getReferenceResourceBinding()
 * @model
 * @generated
 */
@ProviderType
public interface ReferenceResourceBinding extends EObject {
	/**
	 * Returns the value of the '<em><b>Attributes</b></em>' reference list.
	 * The list contents are of type {@link org.eclipse.emf.ecore.EAttribute}.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * <!-- begin-model-doc -->
	 * The attributes of the referenced type this binding applies to, referenced the same way as
	 * ReferenceMapping.filter.
	 * 
	 * An empty list makes the binding apply to every resource generated from the reference mapping,
	 * which is how the default for a whole reference is declared.
	 * 
	 * An attribute named by more than one binding at the same level resolves to the first binding
	 * that names it; the duplicate is ignored and logged.
	 * <!-- end-model-doc -->
	 * @return the value of the '<em>Attributes</em>' reference list.
	 * @see org.eclipse.fennec.event.atlas.model.mapping.MappingPackage#getReferenceResourceBinding_Attributes()
	 * @model
	 * @generated
	 */
	EList<EAttribute> getAttributes();

	/**
	 * Returns the value of the '<em><b>Change Rule</b></em>' containment reference.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * <!-- begin-model-doc -->
	 * Optional change rule for the selected resources, with the same meaning it has on
	 * ResourceMapping.changeRule. Contained here, and copied onto every resource this binding
	 * applies to - the resources get equal rules, not one shared rule.
	 * <!-- end-model-doc -->
	 * @return the value of the '<em>Change Rule</em>' containment reference.
	 * @see #setChangeRule(ChangeRule)
	 * @see org.eclipse.fennec.event.atlas.model.mapping.MappingPackage#getReferenceResourceBinding_ChangeRule()
	 * @model containment="true"
	 * @generated
	 */
	ChangeRule getChangeRule();

	/**
	 * Sets the value of the '{@link org.eclipse.fennec.event.atlas.model.mapping.ReferenceResourceBinding#getChangeRule <em>Change Rule</em>}' containment reference.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @param value the new value of the '<em>Change Rule</em>' containment reference.
	 * @see #getChangeRule()
	 * @generated
	 */
	void setChangeRule(ChangeRule value);

	/**
	 * Returns the value of the '<em><b>Deletion Rule</b></em>' containment reference.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * <!-- begin-model-doc -->
	 * Optional deletion rule for the selected resources, with the same meaning it has on
	 * ResourceMapping.deletionRule. Contained here, and copied onto every resource this binding
	 * applies to.
	 * <!-- end-model-doc -->
	 * @return the value of the '<em>Deletion Rule</em>' containment reference.
	 * @see #setDeletionRule(DeletionRule)
	 * @see org.eclipse.fennec.event.atlas.model.mapping.MappingPackage#getReferenceResourceBinding_DeletionRule()
	 * @model containment="true"
	 * @generated
	 */
	DeletionRule getDeletionRule();

	/**
	 * Sets the value of the '{@link org.eclipse.fennec.event.atlas.model.mapping.ReferenceResourceBinding#getDeletionRule <em>Deletion Rule</em>}' containment reference.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @param value the new value of the '<em>Deletion Rule</em>' containment reference.
	 * @see #getDeletionRule()
	 * @generated
	 */
	void setDeletionRule(DeletionRule value);

	/**
	 * Returns the value of the '<em><b>Unit</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * <!-- begin-model-doc -->
	 * Optional unit for the selected resources, applied as ResourceMapping.unit.
	 * 
	 * A generated resource otherwise takes its unit from the source attribute's 'sensinact.mapping'
	 * annotation, which is copied along with the attribute. Set this when that is not an option -
	 * a source model that cannot be annotated - or when the mapping publishes a different unit than
	 * the model declares, for instance an attribute annotated 'F' exposed as 'C' after conversion.
	 * ResourceMapping.unit takes precedence over the annotation.
	 * <!-- end-model-doc -->
	 * @return the value of the '<em>Unit</em>' attribute.
	 * @see #setUnit(String)
	 * @see org.eclipse.fennec.event.atlas.model.mapping.MappingPackage#getReferenceResourceBinding_Unit()
	 * @model
	 * @generated
	 */
	String getUnit();

	/**
	 * Sets the value of the '{@link org.eclipse.fennec.event.atlas.model.mapping.ReferenceResourceBinding#getUnit <em>Unit</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @param value the new value of the '<em>Unit</em>' attribute.
	 * @see #getUnit()
	 * @generated
	 */
	void setUnit(String value);

} // ReferenceResourceBinding
