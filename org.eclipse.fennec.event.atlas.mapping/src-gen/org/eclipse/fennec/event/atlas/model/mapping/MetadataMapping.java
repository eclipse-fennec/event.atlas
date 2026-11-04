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

import org.osgi.annotation.versioning.ProviderType;

/**
 * <!-- begin-user-doc -->
 * A representation of the model object '<em><b>Metadata Mapping</b></em>'.
 * <!-- end-user-doc -->
 *
 * <!-- begin-model-doc -->
 * One metadata entry of a resource, whose value the payload decides.
 * 
 * extraMetadata holds the entries that are the same for every   payload. This one is read per push: it inherits featurePath, collectionFilter, functionId, expression and converterId from FeatureMapping, so the value can be read from a   sibling feature of the mapped value, or computed.
 * 
 * A producer that yields nothing leaves the entry as it is. A payload that does not carry it is not a reason to delete   what an earlier payload wrote.
 * <!-- end-model-doc -->
 *
 * <p>
 * The following features are supported:
 * </p>
 * <ul>
 *   <li>{@link org.eclipse.fennec.event.atlas.model.mapping.MetadataMapping#getKey <em>Key</em>}</li>
 * </ul>
 *
 * @see org.eclipse.fennec.event.atlas.model.mapping.MappingPackage#getMetadataMapping()
 * @model
 * @generated
 */
@ProviderType
public interface MetadataMapping extends FeatureMapping {
	/**
	 * Returns the value of the '<em><b>Key</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * <!-- begin-model-doc -->
	 * The name of the metadata entry, as a northbound consumer sees it - the same namespace extraMetadata writes into. Where both name one    key, the produced value overwrites the constant on every push that yields one.
	 * <!-- end-model-doc -->
	 * @return the value of the '<em>Key</em>' attribute.
	 * @see #setKey(String)
	 * @see org.eclipse.fennec.event.atlas.model.mapping.MappingPackage#getMetadataMapping_Key()
	 * @model required="true"
	 * @generated
	 */
	String getKey();

	/**
	 * Sets the value of the '{@link org.eclipse.fennec.event.atlas.model.mapping.MetadataMapping#getKey <em>Key</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @param value the new value of the '<em>Key</em>' attribute.
	 * @see #getKey()
	 * @generated
	 */
	void setKey(String value);

} // MetadataMapping
