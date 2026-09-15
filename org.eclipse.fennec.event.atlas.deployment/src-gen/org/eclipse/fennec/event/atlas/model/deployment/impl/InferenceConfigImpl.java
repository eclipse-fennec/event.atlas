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
import org.eclipse.emf.ecore.impl.MinimalEObjectImpl;

import org.eclipse.fennec.event.atlas.model.deployment.DeploymentPackage;
import org.eclipse.fennec.event.atlas.model.deployment.InferenceConfig;

/**
 * <!-- begin-user-doc -->
 * An implementation of the model object '<em><b>Inference Config</b></em>'.
 * <!-- end-user-doc -->
 * <p>
 * The following features are implemented:
 * </p>
 * <ul>
 *   <li>{@link org.eclipse.fennec.event.atlas.model.deployment.impl.InferenceConfigImpl#isSamplingEnabled <em>Sampling Enabled</em>}</li>
 *   <li>{@link org.eclipse.fennec.event.atlas.model.deployment.impl.InferenceConfigImpl#getNamespace <em>Namespace</em>}</li>
 *   <li>{@link org.eclipse.fennec.event.atlas.model.deployment.impl.InferenceConfigImpl#getMaxRunsPerInterval <em>Max Runs Per Interval</em>}</li>
 * </ul>
 *
 * @generated
 */
public class InferenceConfigImpl extends MinimalEObjectImpl.Container implements InferenceConfig {
	/**
	 * The default value of the '{@link #isSamplingEnabled() <em>Sampling Enabled</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #isSamplingEnabled()
	 * @generated
	 * @ordered
	 */
	protected static final boolean SAMPLING_ENABLED_EDEFAULT = false;

	/**
	 * The cached value of the '{@link #isSamplingEnabled() <em>Sampling Enabled</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #isSamplingEnabled()
	 * @generated
	 * @ordered
	 */
	protected boolean samplingEnabled = SAMPLING_ENABLED_EDEFAULT;

	/**
	 * The default value of the '{@link #getNamespace() <em>Namespace</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getNamespace()
	 * @generated
	 * @ordered
	 */
	protected static final String NAMESPACE_EDEFAULT = null;

	/**
	 * The cached value of the '{@link #getNamespace() <em>Namespace</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getNamespace()
	 * @generated
	 * @ordered
	 */
	protected String namespace = NAMESPACE_EDEFAULT;

	/**
	 * The default value of the '{@link #getMaxRunsPerInterval() <em>Max Runs Per Interval</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getMaxRunsPerInterval()
	 * @generated
	 * @ordered
	 */
	protected static final int MAX_RUNS_PER_INTERVAL_EDEFAULT = 5;

	/**
	 * The cached value of the '{@link #getMaxRunsPerInterval() <em>Max Runs Per Interval</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getMaxRunsPerInterval()
	 * @generated
	 * @ordered
	 */
	protected int maxRunsPerInterval = MAX_RUNS_PER_INTERVAL_EDEFAULT;

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	protected InferenceConfigImpl() {
		super();
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	protected EClass eStaticClass() {
		return DeploymentPackage.Literals.INFERENCE_CONFIG;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public boolean isSamplingEnabled() {
		return samplingEnabled;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public void setSamplingEnabled(boolean newSamplingEnabled) {
		boolean oldSamplingEnabled = samplingEnabled;
		samplingEnabled = newSamplingEnabled;
		if (eNotificationRequired())
			eNotify(new ENotificationImpl(this, Notification.SET, DeploymentPackage.INFERENCE_CONFIG__SAMPLING_ENABLED, oldSamplingEnabled, samplingEnabled));
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public String getNamespace() {
		return namespace;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public void setNamespace(String newNamespace) {
		String oldNamespace = namespace;
		namespace = newNamespace;
		if (eNotificationRequired())
			eNotify(new ENotificationImpl(this, Notification.SET, DeploymentPackage.INFERENCE_CONFIG__NAMESPACE, oldNamespace, namespace));
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public int getMaxRunsPerInterval() {
		return maxRunsPerInterval;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public void setMaxRunsPerInterval(int newMaxRunsPerInterval) {
		int oldMaxRunsPerInterval = maxRunsPerInterval;
		maxRunsPerInterval = newMaxRunsPerInterval;
		if (eNotificationRequired())
			eNotify(new ENotificationImpl(this, Notification.SET, DeploymentPackage.INFERENCE_CONFIG__MAX_RUNS_PER_INTERVAL, oldMaxRunsPerInterval, maxRunsPerInterval));
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public Object eGet(int featureID, boolean resolve, boolean coreType) {
		switch (featureID) {
			case DeploymentPackage.INFERENCE_CONFIG__SAMPLING_ENABLED:
				return isSamplingEnabled();
			case DeploymentPackage.INFERENCE_CONFIG__NAMESPACE:
				return getNamespace();
			case DeploymentPackage.INFERENCE_CONFIG__MAX_RUNS_PER_INTERVAL:
				return getMaxRunsPerInterval();
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
			case DeploymentPackage.INFERENCE_CONFIG__SAMPLING_ENABLED:
				setSamplingEnabled((Boolean)newValue);
				return;
			case DeploymentPackage.INFERENCE_CONFIG__NAMESPACE:
				setNamespace((String)newValue);
				return;
			case DeploymentPackage.INFERENCE_CONFIG__MAX_RUNS_PER_INTERVAL:
				setMaxRunsPerInterval((Integer)newValue);
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
			case DeploymentPackage.INFERENCE_CONFIG__SAMPLING_ENABLED:
				setSamplingEnabled(SAMPLING_ENABLED_EDEFAULT);
				return;
			case DeploymentPackage.INFERENCE_CONFIG__NAMESPACE:
				setNamespace(NAMESPACE_EDEFAULT);
				return;
			case DeploymentPackage.INFERENCE_CONFIG__MAX_RUNS_PER_INTERVAL:
				setMaxRunsPerInterval(MAX_RUNS_PER_INTERVAL_EDEFAULT);
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
			case DeploymentPackage.INFERENCE_CONFIG__SAMPLING_ENABLED:
				return samplingEnabled != SAMPLING_ENABLED_EDEFAULT;
			case DeploymentPackage.INFERENCE_CONFIG__NAMESPACE:
				return NAMESPACE_EDEFAULT == null ? namespace != null : !NAMESPACE_EDEFAULT.equals(namespace);
			case DeploymentPackage.INFERENCE_CONFIG__MAX_RUNS_PER_INTERVAL:
				return maxRunsPerInterval != MAX_RUNS_PER_INTERVAL_EDEFAULT;
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
		result.append(" (samplingEnabled: ");
		result.append(samplingEnabled);
		result.append(", namespace: ");
		result.append(namespace);
		result.append(", maxRunsPerInterval: ");
		result.append(maxRunsPerInterval);
		result.append(')');
		return result.toString();
	}

} //InferenceConfigImpl
