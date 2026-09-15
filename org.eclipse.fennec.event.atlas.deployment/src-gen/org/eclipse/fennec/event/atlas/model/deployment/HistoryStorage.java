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

import org.eclipse.emf.common.util.EList;

import org.eclipse.emf.ecore.EObject;

import org.osgi.annotation.versioning.ProviderType;

/**
 * <!-- begin-user-doc -->
 * A representation of the model object '<em><b>History Storage</b></em>'.
 * <!-- end-user-doc -->
 *
 * <!-- begin-model-doc -->
 * A history storage backend. The include/exclude selectors decide which resources reach the backend at all; change-based suppression belongs to a HistorizationFilter.
 * <!-- end-model-doc -->
 *
 * <p>
 * The following features are supported:
 * </p>
 * <ul>
 *   <li>{@link org.eclipse.fennec.event.atlas.model.deployment.HistoryStorage#getMaxPageSize <em>Max Page Size</em>}</li>
 *   <li>{@link org.eclipse.fennec.event.atlas.model.deployment.HistoryStorage#getIncludeResources <em>Include Resources</em>}</li>
 *   <li>{@link org.eclipse.fennec.event.atlas.model.deployment.HistoryStorage#getExcludeResources <em>Exclude Resources</em>}</li>
 * </ul>
 *
 * @see org.eclipse.fennec.event.atlas.model.deployment.DeploymentPackage#getHistoryStorage()
 * @model abstract="true"
 * @generated
 */
@ProviderType
public interface HistoryStorage extends EObject {
	/**
	 * Returns the value of the '<em><b>Max Page Size</b></em>' attribute.
	 * The default value is <code>"10000"</code>.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * <!-- begin-model-doc -->
	 * Largest page a single range query returns. It caps memory per request; it does not cap the reachable dataset.
	 * <!-- end-model-doc -->
	 * @return the value of the '<em>Max Page Size</em>' attribute.
	 * @see #setMaxPageSize(int)
	 * @see org.eclipse.fennec.event.atlas.model.deployment.DeploymentPackage#getHistoryStorage_MaxPageSize()
	 * @model default="10000"
	 * @generated
	 */
	int getMaxPageSize();

	/**
	 * Sets the value of the '{@link org.eclipse.fennec.event.atlas.model.deployment.HistoryStorage#getMaxPageSize <em>Max Page Size</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @param value the new value of the '<em>Max Page Size</em>' attribute.
	 * @see #getMaxPageSize()
	 * @generated
	 */
	void setMaxPageSize(int value);

	/**
	 * Returns the value of the '<em><b>Include Resources</b></em>' attribute list.
	 * The list contents are of type {@link java.lang.String}.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * <!-- begin-model-doc -->
	 * JSON-encoded resource selectors choosing what to historize. Empty means everything.
	 * <!-- end-model-doc -->
	 * @return the value of the '<em>Include Resources</em>' attribute list.
	 * @see org.eclipse.fennec.event.atlas.model.deployment.DeploymentPackage#getHistoryStorage_IncludeResources()
	 * @model
	 * @generated
	 */
	EList<String> getIncludeResources();

	/**
	 * Returns the value of the '<em><b>Exclude Resources</b></em>' attribute list.
	 * The list contents are of type {@link java.lang.String}.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * <!-- begin-model-doc -->
	 * JSON-encoded resource selectors excluded after the include selection.
	 * <!-- end-model-doc -->
	 * @return the value of the '<em>Exclude Resources</em>' attribute list.
	 * @see org.eclipse.fennec.event.atlas.model.deployment.DeploymentPackage#getHistoryStorage_ExcludeResources()
	 * @model
	 * @generated
	 */
	EList<String> getExcludeResources();

} // HistoryStorage
