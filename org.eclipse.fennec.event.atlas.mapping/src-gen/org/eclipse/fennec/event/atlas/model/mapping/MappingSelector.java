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
 * A representation of the model object '<em><b>Selector</b></em>'.
 * <!-- end-user-doc -->
 *
 * <!-- begin-model-doc -->
 * A selector based on source, type and dataschema of a CloudEvent. 
 * When the root which is passed to the InstancePusher is a CloudEvent, the ProviderMapping can 
 * be selected using this MappingSelector, based on the attributes of the CloudEvent.
 * <!-- end-model-doc -->
 *
 * <p>
 * The following features are supported:
 * </p>
 * <ul>
 *   <li>{@link org.eclipse.fennec.event.atlas.model.mapping.MappingSelector#getSource <em>Source</em>}</li>
 *   <li>{@link org.eclipse.fennec.event.atlas.model.mapping.MappingSelector#getSourceExcludes <em>Source Excludes</em>}</li>
 *   <li>{@link org.eclipse.fennec.event.atlas.model.mapping.MappingSelector#getType <em>Type</em>}</li>
 *   <li>{@link org.eclipse.fennec.event.atlas.model.mapping.MappingSelector#getDataschema <em>Dataschema</em>}</li>
 * </ul>
 *
 * @see org.eclipse.fennec.event.atlas.model.mapping.MappingPackage#getMappingSelector()
 * @model
 * @generated
 */
@ProviderType
public interface MappingSelector extends EObject {
	/**
	 * Returns the value of the '<em><b>Source</b></em>' attribute list.
	 * The list contents are of type {@link java.lang.String}.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * <!-- begin-model-doc -->
	 * The CloudEvent sources to allow in the selector.
	 * <!-- end-model-doc -->
	 * @return the value of the '<em>Source</em>' attribute list.
	 * @see org.eclipse.fennec.event.atlas.model.mapping.MappingPackage#getMappingSelector_Source()
	 * @model
	 * @generated
	 */
	EList<String> getSource();

	/**
	 * Returns the value of the '<em><b>Source Excludes</b></em>' attribute list.
	 * The list contents are of type {@link java.lang.String}.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * <!-- begin-model-doc -->
	 * The CloudEvent sources to exclude in the selector.
	 * <!-- end-model-doc -->
	 * @return the value of the '<em>Source Excludes</em>' attribute list.
	 * @see org.eclipse.fennec.event.atlas.model.mapping.MappingPackage#getMappingSelector_SourceExcludes()
	 * @model
	 * @generated
	 */
	EList<String> getSourceExcludes();

	/**
	 * Returns the value of the '<em><b>Type</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * <!-- begin-model-doc -->
	 * The CloudEvent type to allow in the selector.
	 * <!-- end-model-doc -->
	 * @return the value of the '<em>Type</em>' attribute.
	 * @see #setType(String)
	 * @see org.eclipse.fennec.event.atlas.model.mapping.MappingPackage#getMappingSelector_Type()
	 * @model
	 * @generated
	 */
	String getType();

	/**
	 * Sets the value of the '{@link org.eclipse.fennec.event.atlas.model.mapping.MappingSelector#getType <em>Type</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @param value the new value of the '<em>Type</em>' attribute.
	 * @see #getType()
	 * @generated
	 */
	void setType(String value);

	/**
	 * Returns the value of the '<em><b>Dataschema</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * <!-- begin-model-doc -->
	 * The CloudEvent dataschema to allow in the selector.
	 * <!-- end-model-doc -->
	 * @return the value of the '<em>Dataschema</em>' attribute.
	 * @see #setDataschema(String)
	 * @see org.eclipse.fennec.event.atlas.model.mapping.MappingPackage#getMappingSelector_Dataschema()
	 * @model
	 * @generated
	 */
	String getDataschema();

	/**
	 * Sets the value of the '{@link org.eclipse.fennec.event.atlas.model.mapping.MappingSelector#getDataschema <em>Dataschema</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @param value the new value of the '<em>Dataschema</em>' attribute.
	 * @see #getDataschema()
	 * @generated
	 */
	void setDataschema(String value);

} // MappingSelector
