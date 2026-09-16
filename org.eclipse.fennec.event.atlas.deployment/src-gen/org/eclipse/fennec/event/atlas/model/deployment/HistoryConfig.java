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
 * A representation of the model object '<em><b>History Config</b></em>'.
 * <!-- end-user-doc -->
 *
 * <!-- begin-model-doc -->
 * The history store and the engine settings around it: a storage backend holding the values, plus the historization filters and housekeeping policies applied to it.
 * <!-- end-model-doc -->
 *
 * <p>
 * The following features are supported:
 * </p>
 * <ul>
 *   <li>{@link org.eclipse.fennec.event.atlas.model.deployment.HistoryConfig#getProviderName <em>Provider Name</em>}</li>
 *   <li>{@link org.eclipse.fennec.event.atlas.model.deployment.HistoryConfig#getStorage <em>Storage</em>}</li>
 *   <li>{@link org.eclipse.fennec.event.atlas.model.deployment.HistoryConfig#getFilters <em>Filters</em>}</li>
 *   <li>{@link org.eclipse.fennec.event.atlas.model.deployment.HistoryConfig#getHousekeeping <em>Housekeeping</em>}</li>
 * </ul>
 *
 * @see org.eclipse.fennec.event.atlas.model.deployment.DeploymentPackage#getHistoryConfig()
 * @model
 * @generated
 */
@ProviderType
public interface HistoryConfig extends EObject {
	/**
	 * Returns the value of the '<em><b>Provider Name</b></em>' attribute.
	 * The default value is <code>"brokerHistory"</code>.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * <!-- begin-model-doc -->
	 * Name the store is registered under. The SensorThings northbound reads history under this same name.
	 * <!-- end-model-doc -->
	 * @return the value of the '<em>Provider Name</em>' attribute.
	 * @see #setProviderName(String)
	 * @see org.eclipse.fennec.event.atlas.model.deployment.DeploymentPackage#getHistoryConfig_ProviderName()
	 * @model default="brokerHistory"
	 * @generated
	 */
	String getProviderName();

	/**
	 * Sets the value of the '{@link org.eclipse.fennec.event.atlas.model.deployment.HistoryConfig#getProviderName <em>Provider Name</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @param value the new value of the '<em>Provider Name</em>' attribute.
	 * @see #getProviderName()
	 * @generated
	 */
	void setProviderName(String value);

	/**
	 * Returns the value of the '<em><b>Storage</b></em>' containment reference.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * <!-- begin-model-doc -->
	 * The storage backend. Optional: leave it out to declare only the filters and housekeeping policies below and keep the backend configured elsewhere.
	 * <!-- end-model-doc -->
	 * @return the value of the '<em>Storage</em>' containment reference.
	 * @see #setStorage(HistoryStorage)
	 * @see org.eclipse.fennec.event.atlas.model.deployment.DeploymentPackage#getHistoryConfig_Storage()
	 * @model containment="true"
	 * @generated
	 */
	HistoryStorage getStorage();

	/**
	 * Sets the value of the '{@link org.eclipse.fennec.event.atlas.model.deployment.HistoryConfig#getStorage <em>Storage</em>}' containment reference.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @param value the new value of the '<em>Storage</em>' containment reference.
	 * @see #getStorage()
	 * @generated
	 */
	void setStorage(HistoryStorage value);

	/**
	 * Returns the value of the '<em><b>Filters</b></em>' containment reference list.
	 * The list contents are of type {@link org.eclipse.fennec.event.atlas.model.deployment.HistorizationFilter}.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * <!-- begin-model-doc -->
	 * Runtime refinement of what is stored, applied per backend without touching it. A resource update is stored only if at least one active filter includes it and no active filter excludes it.
	 * <!-- end-model-doc -->
	 * @return the value of the '<em>Filters</em>' containment reference list.
	 * @see org.eclipse.fennec.event.atlas.model.deployment.DeploymentPackage#getHistoryConfig_Filters()
	 * @model containment="true"
	 * @generated
	 */
	EList<HistorizationFilter> getFilters();

	/**
	 * Returns the value of the '<em><b>Housekeeping</b></em>' containment reference list.
	 * The list contents are of type {@link org.eclipse.fennec.event.atlas.model.deployment.HousekeepingPolicy}.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the value of the '<em>Housekeeping</em>' containment reference list.
	 * @see org.eclipse.fennec.event.atlas.model.deployment.DeploymentPackage#getHistoryConfig_Housekeeping()
	 * @model containment="true"
	 * @generated
	 */
	EList<HousekeepingPolicy> getHousekeeping();

} // HistoryConfig
