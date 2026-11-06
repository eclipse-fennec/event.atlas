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
package probeadmin.impl;

import org.eclipse.emf.common.notify.Notification;

import org.eclipse.emf.ecore.EClass;
import org.eclipse.emf.ecore.InternalEObject;

import org.eclipse.emf.ecore.impl.ENotificationImpl;

import org.eclipse.sensinact.model.core.provider.impl.DynamicProviderImpl;

import probeadmin.ProbeAdmin;
import probeadmin.ProbeProvider;
import probeadmin.ProbeadminPackage;

/**
 * <!-- begin-user-doc -->
 * An implementation of the model object '<em><b>Probe Provider</b></em>'.
 * <!-- end-user-doc -->
 * <p>
 * The following features are implemented:
 * </p>
 * <ul>
 *   <li>{@link probeadmin.impl.ProbeProviderImpl#getThermal <em>Thermal</em>}</li>
 * </ul>
 *
 * @generated
 */
public class ProbeProviderImpl extends DynamicProviderImpl implements ProbeProvider {
	/**
	 * The cached value of the '{@link #getThermal() <em>Thermal</em>}' reference.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getThermal()
	 * @generated
	 * @ordered
	 */
	protected ProbeAdmin thermal;

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	protected ProbeProviderImpl() {
		super();
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	protected EClass eStaticClass() {
		return ProbeadminPackage.Literals.PROBE_PROVIDER;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public ProbeAdmin getThermal() {
		if (thermal != null && thermal.eIsProxy()) {
			InternalEObject oldThermal = (InternalEObject)thermal;
			thermal = (ProbeAdmin)eResolveProxy(oldThermal);
			if (thermal != oldThermal) {
				if (eNotificationRequired())
					eNotify(new ENotificationImpl(this, Notification.RESOLVE, ProbeadminPackage.PROBE_PROVIDER__THERMAL, oldThermal, thermal));
			}
		}
		return thermal;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public ProbeAdmin basicGetThermal() {
		return thermal;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public void setThermal(ProbeAdmin newThermal) {
		ProbeAdmin oldThermal = thermal;
		thermal = newThermal;
		if (eNotificationRequired())
			eNotify(new ENotificationImpl(this, Notification.SET, ProbeadminPackage.PROBE_PROVIDER__THERMAL, oldThermal, thermal));
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public Object eGet(int featureID, boolean resolve, boolean coreType) {
		switch (featureID) {
			case ProbeadminPackage.PROBE_PROVIDER__THERMAL:
				if (resolve) return getThermal();
				return basicGetThermal();
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
			case ProbeadminPackage.PROBE_PROVIDER__THERMAL:
				setThermal((ProbeAdmin)newValue);
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
			case ProbeadminPackage.PROBE_PROVIDER__THERMAL:
				setThermal((ProbeAdmin)null);
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
			case ProbeadminPackage.PROBE_PROVIDER__THERMAL:
				return thermal != null;
		}
		return super.eIsSet(featureID);
	}

} //ProbeProviderImpl
