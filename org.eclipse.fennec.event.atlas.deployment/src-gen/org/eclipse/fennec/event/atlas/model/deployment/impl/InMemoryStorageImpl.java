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
package org.eclipse.fennec.event.atlas.model.deployment.impl;

import org.eclipse.emf.common.notify.Notification;

import org.eclipse.emf.ecore.EClass;

import org.eclipse.emf.ecore.impl.ENotificationImpl;

import org.eclipse.fennec.event.atlas.model.deployment.DeploymentPackage;
import org.eclipse.fennec.event.atlas.model.deployment.InMemoryStorage;

/**
 * <!-- begin-user-doc -->
 * An implementation of the model object '<em><b>In Memory Storage</b></em>'.
 * <!-- end-user-doc -->
 * <p>
 * The following features are implemented:
 * </p>
 * <ul>
 *   <li>{@link org.eclipse.fennec.event.atlas.model.deployment.impl.InMemoryStorageImpl#getMaxValuesPerResource <em>Max Values Per Resource</em>}</li>
 * </ul>
 *
 * @generated
 */
public class InMemoryStorageImpl extends HistoryStorageImpl implements InMemoryStorage {
	/**
	 * The default value of the '{@link #getMaxValuesPerResource() <em>Max Values Per Resource</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getMaxValuesPerResource()
	 * @generated
	 * @ordered
	 */
	protected static final int MAX_VALUES_PER_RESOURCE_EDEFAULT = 0;

	/**
	 * The cached value of the '{@link #getMaxValuesPerResource() <em>Max Values Per Resource</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getMaxValuesPerResource()
	 * @generated
	 * @ordered
	 */
	protected int maxValuesPerResource = MAX_VALUES_PER_RESOURCE_EDEFAULT;

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	protected InMemoryStorageImpl() {
		super();
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	protected EClass eStaticClass() {
		return DeploymentPackage.Literals.IN_MEMORY_STORAGE;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public int getMaxValuesPerResource() {
		return maxValuesPerResource;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public void setMaxValuesPerResource(int newMaxValuesPerResource) {
		int oldMaxValuesPerResource = maxValuesPerResource;
		maxValuesPerResource = newMaxValuesPerResource;
		if (eNotificationRequired())
			eNotify(new ENotificationImpl(this, Notification.SET, DeploymentPackage.IN_MEMORY_STORAGE__MAX_VALUES_PER_RESOURCE, oldMaxValuesPerResource, maxValuesPerResource));
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public Object eGet(int featureID, boolean resolve, boolean coreType) {
		switch (featureID) {
			case DeploymentPackage.IN_MEMORY_STORAGE__MAX_VALUES_PER_RESOURCE:
				return getMaxValuesPerResource();
		}
		return super.eGet(featureID, resolve, coreType);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public void eSet(int featureID, Object newValue) {
		switch (featureID) {
			case DeploymentPackage.IN_MEMORY_STORAGE__MAX_VALUES_PER_RESOURCE:
				setMaxValuesPerResource((Integer)newValue);
				return;
		}
		super.eSet(featureID, newValue);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public void eUnset(int featureID) {
		switch (featureID) {
			case DeploymentPackage.IN_MEMORY_STORAGE__MAX_VALUES_PER_RESOURCE:
				setMaxValuesPerResource(MAX_VALUES_PER_RESOURCE_EDEFAULT);
				return;
		}
		super.eUnset(featureID);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public boolean eIsSet(int featureID) {
		switch (featureID) {
			case DeploymentPackage.IN_MEMORY_STORAGE__MAX_VALUES_PER_RESOURCE:
				return maxValuesPerResource != MAX_VALUES_PER_RESOURCE_EDEFAULT;
		}
		return super.eIsSet(featureID);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public String toString() {
		if (eIsProxy()) return super.toString();

		StringBuilder result = new StringBuilder(super.toString());
		result.append(" (maxValuesPerResource: ");
		result.append(maxValuesPerResource);
		result.append(')');
		return result.toString();
	}

} //InMemoryStorageImpl
