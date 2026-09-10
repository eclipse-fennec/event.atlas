/*
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
 *      Data In Motion - initial API and implementation
 */
package org.eclipse.fennec.event.atlas.model.deployment;

import org.osgi.annotation.versioning.ProviderType;

/**
 * <!-- begin-user-doc -->
 * A representation of the model object '<em><b>In Memory Storage</b></em>'.
 * <!-- end-user-doc -->
 *
 * <!-- begin-model-doc -->
 * The non-persistent backend, for tests and demos: history without a database, lost on restart.
 * <!-- end-model-doc -->
 *
 * <p>
 * The following features are supported:
 * </p>
 * <ul>
 *   <li>{@link org.eclipse.fennec.event.atlas.model.deployment.InMemoryStorage#getMaxValuesPerResource <em>Max Values Per Resource</em>}</li>
 * </ul>
 *
 * @see org.eclipse.fennec.event.atlas.model.deployment.DeploymentPackage#getInMemoryStorage()
 * @model
 * @generated
 */
@ProviderType
public interface InMemoryStorage extends HistoryStorage {
	/**
	 * Returns the value of the '<em><b>Max Values Per Resource</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * <!-- begin-model-doc -->
	 * Bound on the values kept per resource. Zero or unset leaves the backend's own default in place.
	 * <!-- end-model-doc -->
	 * @return the value of the '<em>Max Values Per Resource</em>' attribute.
	 * @see #setMaxValuesPerResource(int)
	 * @see org.eclipse.fennec.event.atlas.model.deployment.DeploymentPackage#getInMemoryStorage_MaxValuesPerResource()
	 * @model
	 * @generated
	 */
	int getMaxValuesPerResource();

	/**
	 * Sets the value of the '{@link org.eclipse.fennec.event.atlas.model.deployment.InMemoryStorage#getMaxValuesPerResource <em>Max Values Per Resource</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @param value the new value of the '<em>Max Values Per Resource</em>' attribute.
	 * @see #getMaxValuesPerResource()
	 * @generated
	 */
	void setMaxValuesPerResource(int value);

} // InMemoryStorage
