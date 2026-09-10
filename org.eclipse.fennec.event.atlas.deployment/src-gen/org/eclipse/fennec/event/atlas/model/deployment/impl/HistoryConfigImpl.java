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
import org.eclipse.emf.common.notify.NotificationChain;

import org.eclipse.emf.common.util.EList;

import org.eclipse.emf.ecore.EClass;
import org.eclipse.emf.ecore.InternalEObject;

import org.eclipse.emf.ecore.impl.ENotificationImpl;
import org.eclipse.emf.ecore.impl.MinimalEObjectImpl;

import org.eclipse.emf.ecore.util.EObjectContainmentEList;
import org.eclipse.emf.ecore.util.InternalEList;

import org.eclipse.fennec.event.atlas.model.deployment.DeploymentPackage;
import org.eclipse.fennec.event.atlas.model.deployment.HistorizationFilter;
import org.eclipse.fennec.event.atlas.model.deployment.HistoryConfig;
import org.eclipse.fennec.event.atlas.model.deployment.HistoryStorage;
import org.eclipse.fennec.event.atlas.model.deployment.HousekeepingPolicy;

/**
 * <!-- begin-user-doc -->
 * An implementation of the model object '<em><b>History Config</b></em>'.
 * <!-- end-user-doc -->
 * <p>
 * The following features are implemented:
 * </p>
 * <ul>
 *   <li>{@link org.eclipse.fennec.event.atlas.model.deployment.impl.HistoryConfigImpl#getProviderName <em>Provider Name</em>}</li>
 *   <li>{@link org.eclipse.fennec.event.atlas.model.deployment.impl.HistoryConfigImpl#getStorage <em>Storage</em>}</li>
 *   <li>{@link org.eclipse.fennec.event.atlas.model.deployment.impl.HistoryConfigImpl#getFilters <em>Filters</em>}</li>
 *   <li>{@link org.eclipse.fennec.event.atlas.model.deployment.impl.HistoryConfigImpl#getHousekeeping <em>Housekeeping</em>}</li>
 * </ul>
 *
 * @generated
 */
public class HistoryConfigImpl extends MinimalEObjectImpl.Container implements HistoryConfig {
	/**
	 * The default value of the '{@link #getProviderName() <em>Provider Name</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getProviderName()
	 * @generated
	 * @ordered
	 */
	protected static final String PROVIDER_NAME_EDEFAULT = "brokerHistory";

	/**
	 * The cached value of the '{@link #getProviderName() <em>Provider Name</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getProviderName()
	 * @generated
	 * @ordered
	 */
	protected String providerName = PROVIDER_NAME_EDEFAULT;

	/**
	 * The cached value of the '{@link #getStorage() <em>Storage</em>}' containment reference.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getStorage()
	 * @generated
	 * @ordered
	 */
	protected HistoryStorage storage;

	/**
	 * The cached value of the '{@link #getFilters() <em>Filters</em>}' containment reference list.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getFilters()
	 * @generated
	 * @ordered
	 */
	protected EList<HistorizationFilter> filters;

	/**
	 * The cached value of the '{@link #getHousekeeping() <em>Housekeeping</em>}' containment reference list.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getHousekeeping()
	 * @generated
	 * @ordered
	 */
	protected EList<HousekeepingPolicy> housekeeping;

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	protected HistoryConfigImpl() {
		super();
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	protected EClass eStaticClass() {
		return DeploymentPackage.Literals.HISTORY_CONFIG;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public String getProviderName() {
		return providerName;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public void setProviderName(String newProviderName) {
		String oldProviderName = providerName;
		providerName = newProviderName;
		if (eNotificationRequired())
			eNotify(new ENotificationImpl(this, Notification.SET, DeploymentPackage.HISTORY_CONFIG__PROVIDER_NAME, oldProviderName, providerName));
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public HistoryStorage getStorage() {
		return storage;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public NotificationChain basicSetStorage(HistoryStorage newStorage, NotificationChain msgs) {
		HistoryStorage oldStorage = storage;
		storage = newStorage;
		if (eNotificationRequired()) {
			ENotificationImpl notification = new ENotificationImpl(this, Notification.SET, DeploymentPackage.HISTORY_CONFIG__STORAGE, oldStorage, newStorage);
			if (msgs == null) msgs = notification; else msgs.add(notification);
		}
		return msgs;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public void setStorage(HistoryStorage newStorage) {
		if (newStorage != storage) {
			NotificationChain msgs = null;
			if (storage != null)
				msgs = ((InternalEObject)storage).eInverseRemove(this, EOPPOSITE_FEATURE_BASE - DeploymentPackage.HISTORY_CONFIG__STORAGE, null, msgs);
			if (newStorage != null)
				msgs = ((InternalEObject)newStorage).eInverseAdd(this, EOPPOSITE_FEATURE_BASE - DeploymentPackage.HISTORY_CONFIG__STORAGE, null, msgs);
			msgs = basicSetStorage(newStorage, msgs);
			if (msgs != null) msgs.dispatch();
		}
		else if (eNotificationRequired())
			eNotify(new ENotificationImpl(this, Notification.SET, DeploymentPackage.HISTORY_CONFIG__STORAGE, newStorage, newStorage));
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EList<HistorizationFilter> getFilters() {
		if (filters == null) {
			filters = new EObjectContainmentEList<HistorizationFilter>(HistorizationFilter.class, this, DeploymentPackage.HISTORY_CONFIG__FILTERS);
		}
		return filters;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EList<HousekeepingPolicy> getHousekeeping() {
		if (housekeeping == null) {
			housekeeping = new EObjectContainmentEList<HousekeepingPolicy>(HousekeepingPolicy.class, this, DeploymentPackage.HISTORY_CONFIG__HOUSEKEEPING);
		}
		return housekeeping;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public NotificationChain eInverseRemove(InternalEObject otherEnd, int featureID, NotificationChain msgs) {
		switch (featureID) {
			case DeploymentPackage.HISTORY_CONFIG__STORAGE:
				return basicSetStorage(null, msgs);
			case DeploymentPackage.HISTORY_CONFIG__FILTERS:
				return ((InternalEList<?>)getFilters()).basicRemove(otherEnd, msgs);
			case DeploymentPackage.HISTORY_CONFIG__HOUSEKEEPING:
				return ((InternalEList<?>)getHousekeeping()).basicRemove(otherEnd, msgs);
		}
		return super.eInverseRemove(otherEnd, featureID, msgs);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public Object eGet(int featureID, boolean resolve, boolean coreType) {
		switch (featureID) {
			case DeploymentPackage.HISTORY_CONFIG__PROVIDER_NAME:
				return getProviderName();
			case DeploymentPackage.HISTORY_CONFIG__STORAGE:
				return getStorage();
			case DeploymentPackage.HISTORY_CONFIG__FILTERS:
				return getFilters();
			case DeploymentPackage.HISTORY_CONFIG__HOUSEKEEPING:
				return getHousekeeping();
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
			case DeploymentPackage.HISTORY_CONFIG__PROVIDER_NAME:
				setProviderName((String)newValue);
				return;
			case DeploymentPackage.HISTORY_CONFIG__STORAGE:
				setStorage((HistoryStorage)newValue);
				return;
			case DeploymentPackage.HISTORY_CONFIG__FILTERS:
				getFilters().clear();
				getFilters().addAll((Collection<? extends HistorizationFilter>)newValue);
				return;
			case DeploymentPackage.HISTORY_CONFIG__HOUSEKEEPING:
				getHousekeeping().clear();
				getHousekeeping().addAll((Collection<? extends HousekeepingPolicy>)newValue);
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
			case DeploymentPackage.HISTORY_CONFIG__PROVIDER_NAME:
				setProviderName(PROVIDER_NAME_EDEFAULT);
				return;
			case DeploymentPackage.HISTORY_CONFIG__STORAGE:
				setStorage((HistoryStorage)null);
				return;
			case DeploymentPackage.HISTORY_CONFIG__FILTERS:
				getFilters().clear();
				return;
			case DeploymentPackage.HISTORY_CONFIG__HOUSEKEEPING:
				getHousekeeping().clear();
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
			case DeploymentPackage.HISTORY_CONFIG__PROVIDER_NAME:
				return PROVIDER_NAME_EDEFAULT == null ? providerName != null : !PROVIDER_NAME_EDEFAULT.equals(providerName);
			case DeploymentPackage.HISTORY_CONFIG__STORAGE:
				return storage != null;
			case DeploymentPackage.HISTORY_CONFIG__FILTERS:
				return filters != null && !filters.isEmpty();
			case DeploymentPackage.HISTORY_CONFIG__HOUSEKEEPING:
				return housekeeping != null && !housekeeping.isEmpty();
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
		result.append(" (providerName: ");
		result.append(providerName);
		result.append(')');
		return result.toString();
	}

} //HistoryConfigImpl
