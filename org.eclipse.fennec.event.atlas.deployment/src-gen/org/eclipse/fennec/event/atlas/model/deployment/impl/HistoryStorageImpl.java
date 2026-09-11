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

import java.util.Collection;

import org.eclipse.emf.common.notify.Notification;

import org.eclipse.emf.common.util.EList;

import org.eclipse.emf.ecore.EClass;

import org.eclipse.emf.ecore.impl.ENotificationImpl;
import org.eclipse.emf.ecore.impl.MinimalEObjectImpl;

import org.eclipse.emf.ecore.util.EDataTypeUniqueEList;

import org.eclipse.fennec.event.atlas.model.deployment.DeploymentPackage;
import org.eclipse.fennec.event.atlas.model.deployment.HistoryStorage;

/**
 * <!-- begin-user-doc -->
 * An implementation of the model object '<em><b>History Storage</b></em>'.
 * <!-- end-user-doc -->
 * <p>
 * The following features are implemented:
 * </p>
 * <ul>
 *   <li>{@link org.eclipse.fennec.event.atlas.model.deployment.impl.HistoryStorageImpl#getMaxPageSize <em>Max Page Size</em>}</li>
 *   <li>{@link org.eclipse.fennec.event.atlas.model.deployment.impl.HistoryStorageImpl#getIncludeResources <em>Include Resources</em>}</li>
 *   <li>{@link org.eclipse.fennec.event.atlas.model.deployment.impl.HistoryStorageImpl#getExcludeResources <em>Exclude Resources</em>}</li>
 * </ul>
 *
 * @generated
 */
public abstract class HistoryStorageImpl extends MinimalEObjectImpl.Container implements HistoryStorage {
	/**
	 * The default value of the '{@link #getMaxPageSize() <em>Max Page Size</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getMaxPageSize()
	 * @generated
	 * @ordered
	 */
	protected static final int MAX_PAGE_SIZE_EDEFAULT = 10000;

	/**
	 * The cached value of the '{@link #getMaxPageSize() <em>Max Page Size</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getMaxPageSize()
	 * @generated
	 * @ordered
	 */
	protected int maxPageSize = MAX_PAGE_SIZE_EDEFAULT;

	/**
	 * The cached value of the '{@link #getIncludeResources() <em>Include Resources</em>}' attribute list.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getIncludeResources()
	 * @generated
	 * @ordered
	 */
	protected EList<String> includeResources;

	/**
	 * The cached value of the '{@link #getExcludeResources() <em>Exclude Resources</em>}' attribute list.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getExcludeResources()
	 * @generated
	 * @ordered
	 */
	protected EList<String> excludeResources;

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	protected HistoryStorageImpl() {
		super();
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	protected EClass eStaticClass() {
		return DeploymentPackage.Literals.HISTORY_STORAGE;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public int getMaxPageSize() {
		return maxPageSize;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public void setMaxPageSize(int newMaxPageSize) {
		int oldMaxPageSize = maxPageSize;
		maxPageSize = newMaxPageSize;
		if (eNotificationRequired())
			eNotify(new ENotificationImpl(this, Notification.SET, DeploymentPackage.HISTORY_STORAGE__MAX_PAGE_SIZE, oldMaxPageSize, maxPageSize));
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EList<String> getIncludeResources() {
		if (includeResources == null) {
			includeResources = new EDataTypeUniqueEList<String>(String.class, this, DeploymentPackage.HISTORY_STORAGE__INCLUDE_RESOURCES);
		}
		return includeResources;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EList<String> getExcludeResources() {
		if (excludeResources == null) {
			excludeResources = new EDataTypeUniqueEList<String>(String.class, this, DeploymentPackage.HISTORY_STORAGE__EXCLUDE_RESOURCES);
		}
		return excludeResources;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public Object eGet(int featureID, boolean resolve, boolean coreType) {
		switch (featureID) {
			case DeploymentPackage.HISTORY_STORAGE__MAX_PAGE_SIZE:
				return getMaxPageSize();
			case DeploymentPackage.HISTORY_STORAGE__INCLUDE_RESOURCES:
				return getIncludeResources();
			case DeploymentPackage.HISTORY_STORAGE__EXCLUDE_RESOURCES:
				return getExcludeResources();
		}
		return super.eGet(featureID, resolve, coreType);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@SuppressWarnings("unchecked")
	@Override
	public void eSet(int featureID, Object newValue) {
		switch (featureID) {
			case DeploymentPackage.HISTORY_STORAGE__MAX_PAGE_SIZE:
				setMaxPageSize((Integer)newValue);
				return;
			case DeploymentPackage.HISTORY_STORAGE__INCLUDE_RESOURCES:
				getIncludeResources().clear();
				getIncludeResources().addAll((Collection<? extends String>)newValue);
				return;
			case DeploymentPackage.HISTORY_STORAGE__EXCLUDE_RESOURCES:
				getExcludeResources().clear();
				getExcludeResources().addAll((Collection<? extends String>)newValue);
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
			case DeploymentPackage.HISTORY_STORAGE__MAX_PAGE_SIZE:
				setMaxPageSize(MAX_PAGE_SIZE_EDEFAULT);
				return;
			case DeploymentPackage.HISTORY_STORAGE__INCLUDE_RESOURCES:
				getIncludeResources().clear();
				return;
			case DeploymentPackage.HISTORY_STORAGE__EXCLUDE_RESOURCES:
				getExcludeResources().clear();
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
			case DeploymentPackage.HISTORY_STORAGE__MAX_PAGE_SIZE:
				return maxPageSize != MAX_PAGE_SIZE_EDEFAULT;
			case DeploymentPackage.HISTORY_STORAGE__INCLUDE_RESOURCES:
				return includeResources != null && !includeResources.isEmpty();
			case DeploymentPackage.HISTORY_STORAGE__EXCLUDE_RESOURCES:
				return excludeResources != null && !excludeResources.isEmpty();
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
		result.append(" (maxPageSize: ");
		result.append(maxPageSize);
		result.append(", includeResources: ");
		result.append(includeResources);
		result.append(", excludeResources: ");
		result.append(excludeResources);
		result.append(')');
		return result.toString();
	}

} //HistoryStorageImpl
