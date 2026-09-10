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

import java.time.Duration;

import java.util.Collection;

import org.eclipse.emf.common.notify.Notification;

import org.eclipse.emf.common.util.EList;

import org.eclipse.emf.ecore.EClass;

import org.eclipse.emf.ecore.impl.ENotificationImpl;
import org.eclipse.emf.ecore.impl.MinimalEObjectImpl;

import org.eclipse.emf.ecore.util.EDataTypeUniqueEList;

import org.eclipse.fennec.event.atlas.model.deployment.DeploymentPackage;
import org.eclipse.fennec.event.atlas.model.deployment.HousekeepingPolicy;

/**
 * <!-- begin-user-doc -->
 * An implementation of the model object '<em><b>Housekeeping Policy</b></em>'.
 * <!-- end-user-doc -->
 * <p>
 * The following features are implemented:
 * </p>
 * <ul>
 *   <li>{@link org.eclipse.fennec.event.atlas.model.deployment.impl.HousekeepingPolicyImpl#getName <em>Name</em>}</li>
 *   <li>{@link org.eclipse.fennec.event.atlas.model.deployment.impl.HousekeepingPolicyImpl#getTargets <em>Targets</em>}</li>
 *   <li>{@link org.eclipse.fennec.event.atlas.model.deployment.impl.HousekeepingPolicyImpl#getRetentionPeriod <em>Retention Period</em>}</li>
 *   <li>{@link org.eclipse.fennec.event.atlas.model.deployment.impl.HousekeepingPolicyImpl#getKeepCount <em>Keep Count</em>}</li>
 *   <li>{@link org.eclipse.fennec.event.atlas.model.deployment.impl.HousekeepingPolicyImpl#getMaxDelete <em>Max Delete</em>}</li>
 *   <li>{@link org.eclipse.fennec.event.atlas.model.deployment.impl.HousekeepingPolicyImpl#getSchedulePeriod <em>Schedule Period</em>}</li>
 * </ul>
 *
 * @generated
 */
public class HousekeepingPolicyImpl extends MinimalEObjectImpl.Container implements HousekeepingPolicy {
	/**
	 * The default value of the '{@link #getName() <em>Name</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getName()
	 * @generated
	 * @ordered
	 */
	protected static final String NAME_EDEFAULT = null;

	/**
	 * The cached value of the '{@link #getName() <em>Name</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getName()
	 * @generated
	 * @ordered
	 */
	protected String name = NAME_EDEFAULT;

	/**
	 * The cached value of the '{@link #getTargets() <em>Targets</em>}' attribute list.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getTargets()
	 * @generated
	 * @ordered
	 */
	protected EList<String> targets;

	/**
	 * The default value of the '{@link #getRetentionPeriod() <em>Retention Period</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getRetentionPeriod()
	 * @generated
	 * @ordered
	 */
	protected static final Duration RETENTION_PERIOD_EDEFAULT = null;

	/**
	 * The cached value of the '{@link #getRetentionPeriod() <em>Retention Period</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getRetentionPeriod()
	 * @generated
	 * @ordered
	 */
	protected Duration retentionPeriod = RETENTION_PERIOD_EDEFAULT;

	/**
	 * The default value of the '{@link #getKeepCount() <em>Keep Count</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getKeepCount()
	 * @generated
	 * @ordered
	 */
	protected static final int KEEP_COUNT_EDEFAULT = 0;

	/**
	 * The cached value of the '{@link #getKeepCount() <em>Keep Count</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getKeepCount()
	 * @generated
	 * @ordered
	 */
	protected int keepCount = KEEP_COUNT_EDEFAULT;

	/**
	 * The default value of the '{@link #getMaxDelete() <em>Max Delete</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getMaxDelete()
	 * @generated
	 * @ordered
	 */
	protected static final int MAX_DELETE_EDEFAULT = 0;

	/**
	 * The cached value of the '{@link #getMaxDelete() <em>Max Delete</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getMaxDelete()
	 * @generated
	 * @ordered
	 */
	protected int maxDelete = MAX_DELETE_EDEFAULT;

	/**
	 * The default value of the '{@link #getSchedulePeriod() <em>Schedule Period</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getSchedulePeriod()
	 * @generated
	 * @ordered
	 */
	protected static final Duration SCHEDULE_PERIOD_EDEFAULT = null;

	/**
	 * The cached value of the '{@link #getSchedulePeriod() <em>Schedule Period</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getSchedulePeriod()
	 * @generated
	 * @ordered
	 */
	protected Duration schedulePeriod = SCHEDULE_PERIOD_EDEFAULT;

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	protected HousekeepingPolicyImpl() {
		super();
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	protected EClass eStaticClass() {
		return DeploymentPackage.Literals.HOUSEKEEPING_POLICY;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public String getName() {
		return name;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public void setName(String newName) {
		String oldName = name;
		name = newName;
		if (eNotificationRequired())
			eNotify(new ENotificationImpl(this, Notification.SET, DeploymentPackage.HOUSEKEEPING_POLICY__NAME, oldName, name));
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EList<String> getTargets() {
		if (targets == null) {
			targets = new EDataTypeUniqueEList<String>(String.class, this, DeploymentPackage.HOUSEKEEPING_POLICY__TARGETS);
		}
		return targets;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public Duration getRetentionPeriod() {
		return retentionPeriod;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public void setRetentionPeriod(Duration newRetentionPeriod) {
		Duration oldRetentionPeriod = retentionPeriod;
		retentionPeriod = newRetentionPeriod;
		if (eNotificationRequired())
			eNotify(new ENotificationImpl(this, Notification.SET, DeploymentPackage.HOUSEKEEPING_POLICY__RETENTION_PERIOD, oldRetentionPeriod, retentionPeriod));
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public int getKeepCount() {
		return keepCount;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public void setKeepCount(int newKeepCount) {
		int oldKeepCount = keepCount;
		keepCount = newKeepCount;
		if (eNotificationRequired())
			eNotify(new ENotificationImpl(this, Notification.SET, DeploymentPackage.HOUSEKEEPING_POLICY__KEEP_COUNT, oldKeepCount, keepCount));
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public int getMaxDelete() {
		return maxDelete;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public void setMaxDelete(int newMaxDelete) {
		int oldMaxDelete = maxDelete;
		maxDelete = newMaxDelete;
		if (eNotificationRequired())
			eNotify(new ENotificationImpl(this, Notification.SET, DeploymentPackage.HOUSEKEEPING_POLICY__MAX_DELETE, oldMaxDelete, maxDelete));
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public Duration getSchedulePeriod() {
		return schedulePeriod;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public void setSchedulePeriod(Duration newSchedulePeriod) {
		Duration oldSchedulePeriod = schedulePeriod;
		schedulePeriod = newSchedulePeriod;
		if (eNotificationRequired())
			eNotify(new ENotificationImpl(this, Notification.SET, DeploymentPackage.HOUSEKEEPING_POLICY__SCHEDULE_PERIOD, oldSchedulePeriod, schedulePeriod));
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public Object eGet(int featureID, boolean resolve, boolean coreType) {
		switch (featureID) {
			case DeploymentPackage.HOUSEKEEPING_POLICY__NAME:
				return getName();
			case DeploymentPackage.HOUSEKEEPING_POLICY__TARGETS:
				return getTargets();
			case DeploymentPackage.HOUSEKEEPING_POLICY__RETENTION_PERIOD:
				return getRetentionPeriod();
			case DeploymentPackage.HOUSEKEEPING_POLICY__KEEP_COUNT:
				return getKeepCount();
			case DeploymentPackage.HOUSEKEEPING_POLICY__MAX_DELETE:
				return getMaxDelete();
			case DeploymentPackage.HOUSEKEEPING_POLICY__SCHEDULE_PERIOD:
				return getSchedulePeriod();
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
			case DeploymentPackage.HOUSEKEEPING_POLICY__NAME:
				setName((String)newValue);
				return;
			case DeploymentPackage.HOUSEKEEPING_POLICY__TARGETS:
				getTargets().clear();
				getTargets().addAll((Collection<? extends String>)newValue);
				return;
			case DeploymentPackage.HOUSEKEEPING_POLICY__RETENTION_PERIOD:
				setRetentionPeriod((Duration)newValue);
				return;
			case DeploymentPackage.HOUSEKEEPING_POLICY__KEEP_COUNT:
				setKeepCount((Integer)newValue);
				return;
			case DeploymentPackage.HOUSEKEEPING_POLICY__MAX_DELETE:
				setMaxDelete((Integer)newValue);
				return;
			case DeploymentPackage.HOUSEKEEPING_POLICY__SCHEDULE_PERIOD:
				setSchedulePeriod((Duration)newValue);
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
			case DeploymentPackage.HOUSEKEEPING_POLICY__NAME:
				setName(NAME_EDEFAULT);
				return;
			case DeploymentPackage.HOUSEKEEPING_POLICY__TARGETS:
				getTargets().clear();
				return;
			case DeploymentPackage.HOUSEKEEPING_POLICY__RETENTION_PERIOD:
				setRetentionPeriod(RETENTION_PERIOD_EDEFAULT);
				return;
			case DeploymentPackage.HOUSEKEEPING_POLICY__KEEP_COUNT:
				setKeepCount(KEEP_COUNT_EDEFAULT);
				return;
			case DeploymentPackage.HOUSEKEEPING_POLICY__MAX_DELETE:
				setMaxDelete(MAX_DELETE_EDEFAULT);
				return;
			case DeploymentPackage.HOUSEKEEPING_POLICY__SCHEDULE_PERIOD:
				setSchedulePeriod(SCHEDULE_PERIOD_EDEFAULT);
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
			case DeploymentPackage.HOUSEKEEPING_POLICY__NAME:
				return NAME_EDEFAULT == null ? name != null : !NAME_EDEFAULT.equals(name);
			case DeploymentPackage.HOUSEKEEPING_POLICY__TARGETS:
				return targets != null && !targets.isEmpty();
			case DeploymentPackage.HOUSEKEEPING_POLICY__RETENTION_PERIOD:
				return RETENTION_PERIOD_EDEFAULT == null ? retentionPeriod != null : !RETENTION_PERIOD_EDEFAULT.equals(retentionPeriod);
			case DeploymentPackage.HOUSEKEEPING_POLICY__KEEP_COUNT:
				return keepCount != KEEP_COUNT_EDEFAULT;
			case DeploymentPackage.HOUSEKEEPING_POLICY__MAX_DELETE:
				return maxDelete != MAX_DELETE_EDEFAULT;
			case DeploymentPackage.HOUSEKEEPING_POLICY__SCHEDULE_PERIOD:
				return SCHEDULE_PERIOD_EDEFAULT == null ? schedulePeriod != null : !SCHEDULE_PERIOD_EDEFAULT.equals(schedulePeriod);
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
		result.append(" (name: ");
		result.append(name);
		result.append(", targets: ");
		result.append(targets);
		result.append(", retentionPeriod: ");
		result.append(retentionPeriod);
		result.append(", keepCount: ");
		result.append(keepCount);
		result.append(", maxDelete: ");
		result.append(maxDelete);
		result.append(", schedulePeriod: ");
		result.append(schedulePeriod);
		result.append(')');
		return result.toString();
	}

} //HousekeepingPolicyImpl
