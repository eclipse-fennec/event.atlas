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

import org.eclipse.emf.ecore.impl.ENotificationImpl;

import org.eclipse.sensinact.model.core.provider.impl.AdminImpl;

import probeadmin.ProbeAdmin;
import probeadmin.ProbeadminPackage;

/**
 * <!-- begin-user-doc -->
 * An implementation of the model object '<em><b>Probe Admin</b></em>'.
 * <!-- end-user-doc -->
 * <p>
 * The following features are implemented:
 * </p>
 * <ul>
 *   <li>{@link probeadmin.impl.ProbeAdminImpl#getViewport <em>Viewport</em>}</li>
 * </ul>
 *
 * @generated
 */
public class ProbeAdminImpl extends AdminImpl implements ProbeAdmin {
	/**
	 * The default value of the '{@link #getViewport() <em>Viewport</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getViewport()
	 * @generated
	 * @ordered
	 */
	protected static final String VIEWPORT_EDEFAULT = null;

	/**
	 * The cached value of the '{@link #getViewport() <em>Viewport</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getViewport()
	 * @generated
	 * @ordered
	 */
	protected String viewport = VIEWPORT_EDEFAULT;

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	protected ProbeAdminImpl() {
		super();
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	protected EClass eStaticClass() {
		return ProbeadminPackage.Literals.PROBE_ADMIN;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public String getViewport() {
		return viewport;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public void setViewport(String newViewport) {
		String oldViewport = viewport;
		viewport = newViewport;
		if (eNotificationRequired())
			eNotify(new ENotificationImpl(this, Notification.SET, ProbeadminPackage.PROBE_ADMIN__VIEWPORT, oldViewport, viewport));
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public Object eGet(int featureID, boolean resolve, boolean coreType) {
		switch (featureID) {
			case ProbeadminPackage.PROBE_ADMIN__VIEWPORT:
				return getViewport();
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
			case ProbeadminPackage.PROBE_ADMIN__VIEWPORT:
				setViewport((String)newValue);
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
			case ProbeadminPackage.PROBE_ADMIN__VIEWPORT:
				setViewport(VIEWPORT_EDEFAULT);
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
			case ProbeadminPackage.PROBE_ADMIN__VIEWPORT:
				return VIEWPORT_EDEFAULT == null ? viewport != null : !VIEWPORT_EDEFAULT.equals(viewport);
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
		result.append(" (viewport: ");
		result.append(viewport);
		result.append(')');
		return result.toString();
	}

} //ProbeAdminImpl
