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

import org.eclipse.emf.ecore.EObject;

import org.osgi.annotation.versioning.ProviderType;

/**
 * <!-- begin-user-doc -->
 * A representation of the model object '<em><b>Persistence Rule Registry</b></em>'.
 * <!-- end-user-doc -->
 *
 * <!-- begin-model-doc -->
 * A standalone container for persistence rules.
 * Nothing in the runtime reads it: rules are contained by the ResourceMapping or the
 * ReferenceResourceBinding that uses them, so a registry file is a catalogue to copy rules out
 * of - a place to keep the shapes an installation has agreed on - not a source the engine
 * resolves against. It is kept because a shared rule source may be wanted again later.
 * <!-- end-model-doc -->
 *
 * <p>
 * The following features are supported:
 * </p>
 * <ul>
 *   <li>{@link org.eclipse.fennec.event.atlas.model.mapping.PersistenceRuleRegistry#getChangeRules <em>Change Rules</em>}</li>
 *   <li>{@link org.eclipse.fennec.event.atlas.model.mapping.PersistenceRuleRegistry#getDeletionRules <em>Deletion Rules</em>}</li>
 * </ul>
 *
 * @see org.eclipse.fennec.event.atlas.model.mapping.MappingPackage#getPersistenceRuleRegistry()
 * @model
 * @generated
 */
@ProviderType
public interface PersistenceRuleRegistry extends EObject {
	/**
	 * Returns the value of the '<em><b>Change Rules</b></em>' containment reference list.
	 * The list contents are of type {@link org.eclipse.fennec.event.atlas.model.mapping.ChangeRule}.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the value of the '<em>Change Rules</em>' containment reference list.
	 * @see org.eclipse.fennec.event.atlas.model.mapping.MappingPackage#getPersistenceRuleRegistry_ChangeRules()
	 * @model containment="true"
	 * @generated
	 */
	EList<ChangeRule> getChangeRules();

	/**
	 * Returns the value of the '<em><b>Deletion Rules</b></em>' containment reference list.
	 * The list contents are of type {@link org.eclipse.fennec.event.atlas.model.mapping.DeletionRule}.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the value of the '<em>Deletion Rules</em>' containment reference list.
	 * @see org.eclipse.fennec.event.atlas.model.mapping.MappingPackage#getPersistenceRuleRegistry_DeletionRules()
	 * @model containment="true"
	 * @generated
	 */
	EList<DeletionRule> getDeletionRules();

} // PersistenceRuleRegistry
