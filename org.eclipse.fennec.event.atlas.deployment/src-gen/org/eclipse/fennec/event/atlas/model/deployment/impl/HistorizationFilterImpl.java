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

import org.eclipse.fennec.event.atlas.model.deployment.ChangeMode;
import org.eclipse.fennec.event.atlas.model.deployment.DeploymentPackage;
import org.eclipse.fennec.event.atlas.model.deployment.HistorizationFilter;

/**
 * <!-- begin-user-doc -->
 * An implementation of the model object '<em><b>Historization Filter</b></em>'.
 * <!-- end-user-doc -->
 * <p>
 * The following features are implemented:
 * </p>
 * <ul>
 *   <li>{@link org.eclipse.fennec.event.atlas.model.deployment.impl.HistorizationFilterImpl#getName <em>Name</em>}</li>
 *   <li>{@link org.eclipse.fennec.event.atlas.model.deployment.impl.HistorizationFilterImpl#getTargets <em>Targets</em>}</li>
 *   <li>{@link org.eclipse.fennec.event.atlas.model.deployment.impl.HistorizationFilterImpl#getIncludeResources <em>Include Resources</em>}</li>
 *   <li>{@link org.eclipse.fennec.event.atlas.model.deployment.impl.HistorizationFilterImpl#getExcludeResources <em>Exclude Resources</em>}</li>
 *   <li>{@link org.eclipse.fennec.event.atlas.model.deployment.impl.HistorizationFilterImpl#getChangeMode <em>Change Mode</em>}</li>
 *   <li>{@link org.eclipse.fennec.event.atlas.model.deployment.impl.HistorizationFilterImpl#getChangeThreshold <em>Change Threshold</em>}</li>
 *   <li>{@link org.eclipse.fennec.event.atlas.model.deployment.impl.HistorizationFilterImpl#getChangeThresholdPercent <em>Change Threshold Percent</em>}</li>
 *   <li>{@link org.eclipse.fennec.event.atlas.model.deployment.impl.HistorizationFilterImpl#getChangeMaxInterval <em>Change Max Interval</em>}</li>
 * </ul>
 *
 * @generated
 */
public class HistorizationFilterImpl extends MinimalEObjectImpl.Container implements HistorizationFilter {
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
	 * The default value of the '{@link #getChangeMode() <em>Change Mode</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getChangeMode()
	 * @generated
	 * @ordered
	 */
	protected static final ChangeMode CHANGE_MODE_EDEFAULT = ChangeMode.ALL;

	/**
	 * The cached value of the '{@link #getChangeMode() <em>Change Mode</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getChangeMode()
	 * @generated
	 * @ordered
	 */
	protected ChangeMode changeMode = CHANGE_MODE_EDEFAULT;

	/**
	 * The default value of the '{@link #getChangeThreshold() <em>Change Threshold</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getChangeThreshold()
	 * @generated
	 * @ordered
	 */
	protected static final double CHANGE_THRESHOLD_EDEFAULT = 0.0;

	/**
	 * The cached value of the '{@link #getChangeThreshold() <em>Change Threshold</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getChangeThreshold()
	 * @generated
	 * @ordered
	 */
	protected double changeThreshold = CHANGE_THRESHOLD_EDEFAULT;

	/**
	 * The default value of the '{@link #getChangeThresholdPercent() <em>Change Threshold Percent</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getChangeThresholdPercent()
	 * @generated
	 * @ordered
	 */
	protected static final double CHANGE_THRESHOLD_PERCENT_EDEFAULT = 0.0;

	/**
	 * The cached value of the '{@link #getChangeThresholdPercent() <em>Change Threshold Percent</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getChangeThresholdPercent()
	 * @generated
	 * @ordered
	 */
	protected double changeThresholdPercent = CHANGE_THRESHOLD_PERCENT_EDEFAULT;

	/**
	 * The default value of the '{@link #getChangeMaxInterval() <em>Change Max Interval</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getChangeMaxInterval()
	 * @generated
	 * @ordered
	 */
	protected static final Duration CHANGE_MAX_INTERVAL_EDEFAULT = null;

	/**
	 * The cached value of the '{@link #getChangeMaxInterval() <em>Change Max Interval</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getChangeMaxInterval()
	 * @generated
	 * @ordered
	 */
	protected Duration changeMaxInterval = CHANGE_MAX_INTERVAL_EDEFAULT;

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	protected HistorizationFilterImpl() {
		super();
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	protected EClass eStaticClass() {
		return DeploymentPackage.Literals.HISTORIZATION_FILTER;
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
			eNotify(new ENotificationImpl(this, Notification.SET, DeploymentPackage.HISTORIZATION_FILTER__NAME, oldName, name));
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EList<String> getTargets() {
		if (targets == null) {
			targets = new EDataTypeUniqueEList<String>(String.class, this, DeploymentPackage.HISTORIZATION_FILTER__TARGETS);
		}
		return targets;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EList<String> getIncludeResources() {
		if (includeResources == null) {
			includeResources = new EDataTypeUniqueEList<String>(String.class, this, DeploymentPackage.HISTORIZATION_FILTER__INCLUDE_RESOURCES);
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
			excludeResources = new EDataTypeUniqueEList<String>(String.class, this, DeploymentPackage.HISTORIZATION_FILTER__EXCLUDE_RESOURCES);
		}
		return excludeResources;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public ChangeMode getChangeMode() {
		return changeMode;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public void setChangeMode(ChangeMode newChangeMode) {
		ChangeMode oldChangeMode = changeMode;
		changeMode = newChangeMode == null ? CHANGE_MODE_EDEFAULT : newChangeMode;
		if (eNotificationRequired())
			eNotify(new ENotificationImpl(this, Notification.SET, DeploymentPackage.HISTORIZATION_FILTER__CHANGE_MODE, oldChangeMode, changeMode));
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public double getChangeThreshold() {
		return changeThreshold;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public void setChangeThreshold(double newChangeThreshold) {
		double oldChangeThreshold = changeThreshold;
		changeThreshold = newChangeThreshold;
		if (eNotificationRequired())
			eNotify(new ENotificationImpl(this, Notification.SET, DeploymentPackage.HISTORIZATION_FILTER__CHANGE_THRESHOLD, oldChangeThreshold, changeThreshold));
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public double getChangeThresholdPercent() {
		return changeThresholdPercent;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public void setChangeThresholdPercent(double newChangeThresholdPercent) {
		double oldChangeThresholdPercent = changeThresholdPercent;
		changeThresholdPercent = newChangeThresholdPercent;
		if (eNotificationRequired())
			eNotify(new ENotificationImpl(this, Notification.SET, DeploymentPackage.HISTORIZATION_FILTER__CHANGE_THRESHOLD_PERCENT, oldChangeThresholdPercent, changeThresholdPercent));
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public Duration getChangeMaxInterval() {
		return changeMaxInterval;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public void setChangeMaxInterval(Duration newChangeMaxInterval) {
		Duration oldChangeMaxInterval = changeMaxInterval;
		changeMaxInterval = newChangeMaxInterval;
		if (eNotificationRequired())
			eNotify(new ENotificationImpl(this, Notification.SET, DeploymentPackage.HISTORIZATION_FILTER__CHANGE_MAX_INTERVAL, oldChangeMaxInterval, changeMaxInterval));
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public Object eGet(int featureID, boolean resolve, boolean coreType) {
		switch (featureID) {
			case DeploymentPackage.HISTORIZATION_FILTER__NAME:
				return getName();
			case DeploymentPackage.HISTORIZATION_FILTER__TARGETS:
				return getTargets();
			case DeploymentPackage.HISTORIZATION_FILTER__INCLUDE_RESOURCES:
				return getIncludeResources();
			case DeploymentPackage.HISTORIZATION_FILTER__EXCLUDE_RESOURCES:
				return getExcludeResources();
			case DeploymentPackage.HISTORIZATION_FILTER__CHANGE_MODE:
				return getChangeMode();
			case DeploymentPackage.HISTORIZATION_FILTER__CHANGE_THRESHOLD:
				return getChangeThreshold();
			case DeploymentPackage.HISTORIZATION_FILTER__CHANGE_THRESHOLD_PERCENT:
				return getChangeThresholdPercent();
			case DeploymentPackage.HISTORIZATION_FILTER__CHANGE_MAX_INTERVAL:
				return getChangeMaxInterval();
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
			case DeploymentPackage.HISTORIZATION_FILTER__NAME:
				setName((String)newValue);
				return;
			case DeploymentPackage.HISTORIZATION_FILTER__TARGETS:
				getTargets().clear();
				getTargets().addAll((Collection<? extends String>)newValue);
				return;
			case DeploymentPackage.HISTORIZATION_FILTER__INCLUDE_RESOURCES:
				getIncludeResources().clear();
				getIncludeResources().addAll((Collection<? extends String>)newValue);
				return;
			case DeploymentPackage.HISTORIZATION_FILTER__EXCLUDE_RESOURCES:
				getExcludeResources().clear();
				getExcludeResources().addAll((Collection<? extends String>)newValue);
				return;
			case DeploymentPackage.HISTORIZATION_FILTER__CHANGE_MODE:
				setChangeMode((ChangeMode)newValue);
				return;
			case DeploymentPackage.HISTORIZATION_FILTER__CHANGE_THRESHOLD:
				setChangeThreshold((Double)newValue);
				return;
			case DeploymentPackage.HISTORIZATION_FILTER__CHANGE_THRESHOLD_PERCENT:
				setChangeThresholdPercent((Double)newValue);
				return;
			case DeploymentPackage.HISTORIZATION_FILTER__CHANGE_MAX_INTERVAL:
				setChangeMaxInterval((Duration)newValue);
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
			case DeploymentPackage.HISTORIZATION_FILTER__NAME:
				setName(NAME_EDEFAULT);
				return;
			case DeploymentPackage.HISTORIZATION_FILTER__TARGETS:
				getTargets().clear();
				return;
			case DeploymentPackage.HISTORIZATION_FILTER__INCLUDE_RESOURCES:
				getIncludeResources().clear();
				return;
			case DeploymentPackage.HISTORIZATION_FILTER__EXCLUDE_RESOURCES:
				getExcludeResources().clear();
				return;
			case DeploymentPackage.HISTORIZATION_FILTER__CHANGE_MODE:
				setChangeMode(CHANGE_MODE_EDEFAULT);
				return;
			case DeploymentPackage.HISTORIZATION_FILTER__CHANGE_THRESHOLD:
				setChangeThreshold(CHANGE_THRESHOLD_EDEFAULT);
				return;
			case DeploymentPackage.HISTORIZATION_FILTER__CHANGE_THRESHOLD_PERCENT:
				setChangeThresholdPercent(CHANGE_THRESHOLD_PERCENT_EDEFAULT);
				return;
			case DeploymentPackage.HISTORIZATION_FILTER__CHANGE_MAX_INTERVAL:
				setChangeMaxInterval(CHANGE_MAX_INTERVAL_EDEFAULT);
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
			case DeploymentPackage.HISTORIZATION_FILTER__NAME:
				return NAME_EDEFAULT == null ? name != null : !NAME_EDEFAULT.equals(name);
			case DeploymentPackage.HISTORIZATION_FILTER__TARGETS:
				return targets != null && !targets.isEmpty();
			case DeploymentPackage.HISTORIZATION_FILTER__INCLUDE_RESOURCES:
				return includeResources != null && !includeResources.isEmpty();
			case DeploymentPackage.HISTORIZATION_FILTER__EXCLUDE_RESOURCES:
				return excludeResources != null && !excludeResources.isEmpty();
			case DeploymentPackage.HISTORIZATION_FILTER__CHANGE_MODE:
				return changeMode != CHANGE_MODE_EDEFAULT;
			case DeploymentPackage.HISTORIZATION_FILTER__CHANGE_THRESHOLD:
				return changeThreshold != CHANGE_THRESHOLD_EDEFAULT;
			case DeploymentPackage.HISTORIZATION_FILTER__CHANGE_THRESHOLD_PERCENT:
				return changeThresholdPercent != CHANGE_THRESHOLD_PERCENT_EDEFAULT;
			case DeploymentPackage.HISTORIZATION_FILTER__CHANGE_MAX_INTERVAL:
				return CHANGE_MAX_INTERVAL_EDEFAULT == null ? changeMaxInterval != null : !CHANGE_MAX_INTERVAL_EDEFAULT.equals(changeMaxInterval);
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
		result.append(", includeResources: ");
		result.append(includeResources);
		result.append(", excludeResources: ");
		result.append(excludeResources);
		result.append(", changeMode: ");
		result.append(changeMode);
		result.append(", changeThreshold: ");
		result.append(changeThreshold);
		result.append(", changeThresholdPercent: ");
		result.append(changeThresholdPercent);
		result.append(", changeMaxInterval: ");
		result.append(changeMaxInterval);
		result.append(')');
		return result.toString();
	}

} //HistorizationFilterImpl
