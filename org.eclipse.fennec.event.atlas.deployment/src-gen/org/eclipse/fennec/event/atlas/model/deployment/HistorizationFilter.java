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

import java.time.Duration;

import org.eclipse.emf.common.util.EList;

import org.eclipse.emf.ecore.EObject;

import org.osgi.annotation.versioning.ProviderType;

/**
 * <!-- begin-user-doc -->
 * A representation of the model object '<em><b>Historization Filter</b></em>'.
 * <!-- end-user-doc -->
 *
 * <!-- begin-model-doc -->
 * One historization filter of the history engine. Filters can be added, changed and removed at runtime; stored data is not affected.
 * <!-- end-model-doc -->
 *
 * <p>
 * The following features are supported:
 * </p>
 * <ul>
 *   <li>{@link org.eclipse.fennec.event.atlas.model.deployment.HistorizationFilter#getName <em>Name</em>}</li>
 *   <li>{@link org.eclipse.fennec.event.atlas.model.deployment.HistorizationFilter#getTargets <em>Targets</em>}</li>
 *   <li>{@link org.eclipse.fennec.event.atlas.model.deployment.HistorizationFilter#getIncludeResources <em>Include Resources</em>}</li>
 *   <li>{@link org.eclipse.fennec.event.atlas.model.deployment.HistorizationFilter#getExcludeResources <em>Exclude Resources</em>}</li>
 *   <li>{@link org.eclipse.fennec.event.atlas.model.deployment.HistorizationFilter#getChangeMode <em>Change Mode</em>}</li>
 *   <li>{@link org.eclipse.fennec.event.atlas.model.deployment.HistorizationFilter#getChangeThreshold <em>Change Threshold</em>}</li>
 *   <li>{@link org.eclipse.fennec.event.atlas.model.deployment.HistorizationFilter#getChangeThresholdPercent <em>Change Threshold Percent</em>}</li>
 *   <li>{@link org.eclipse.fennec.event.atlas.model.deployment.HistorizationFilter#getChangeMaxInterval <em>Change Max Interval</em>}</li>
 * </ul>
 *
 * @see org.eclipse.fennec.event.atlas.model.deployment.DeploymentPackage#getHistorizationFilter()
 * @model
 * @generated
 */
@ProviderType
public interface HistorizationFilter extends EObject {
	/**
	 * Returns the value of the '<em><b>Name</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * <!-- begin-model-doc -->
	 * Filter name, and the factory configuration's instance name.
	 * <!-- end-model-doc -->
	 * @return the value of the '<em>Name</em>' attribute.
	 * @see #setName(String)
	 * @see org.eclipse.fennec.event.atlas.model.deployment.DeploymentPackage#getHistorizationFilter_Name()
	 * @model required="true"
	 * @generated
	 */
	String getName();

	/**
	 * Sets the value of the '{@link org.eclipse.fennec.event.atlas.model.deployment.HistorizationFilter#getName <em>Name</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @param value the new value of the '<em>Name</em>' attribute.
	 * @see #getName()
	 * @generated
	 */
	void setName(String value);

	/**
	 * Returns the value of the '<em><b>Targets</b></em>' attribute list.
	 * The list contents are of type {@link java.lang.String}.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * <!-- begin-model-doc -->
	 * History provider names this filter applies to. Empty means all of them.
	 * <!-- end-model-doc -->
	 * @return the value of the '<em>Targets</em>' attribute list.
	 * @see org.eclipse.fennec.event.atlas.model.deployment.DeploymentPackage#getHistorizationFilter_Targets()
	 * @model
	 * @generated
	 */
	EList<String> getTargets();

	/**
	 * Returns the value of the '<em><b>Include Resources</b></em>' attribute list.
	 * The list contents are of type {@link java.lang.String}.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the value of the '<em>Include Resources</em>' attribute list.
	 * @see org.eclipse.fennec.event.atlas.model.deployment.DeploymentPackage#getHistorizationFilter_IncludeResources()
	 * @model
	 * @generated
	 */
	EList<String> getIncludeResources();

	/**
	 * Returns the value of the '<em><b>Exclude Resources</b></em>' attribute list.
	 * The list contents are of type {@link java.lang.String}.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the value of the '<em>Exclude Resources</em>' attribute list.
	 * @see org.eclipse.fennec.event.atlas.model.deployment.DeploymentPackage#getHistorizationFilter_ExcludeResources()
	 * @model
	 * @generated
	 */
	EList<String> getExcludeResources();

	/**
	 * Returns the value of the '<em><b>Change Mode</b></em>' attribute.
	 * The default value is <code>"ALL"</code>.
	 * The literals are from the enumeration {@link org.eclipse.fennec.event.atlas.model.deployment.ChangeMode}.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the value of the '<em>Change Mode</em>' attribute.
	 * @see org.eclipse.fennec.event.atlas.model.deployment.ChangeMode
	 * @see #setChangeMode(ChangeMode)
	 * @see org.eclipse.fennec.event.atlas.model.deployment.DeploymentPackage#getHistorizationFilter_ChangeMode()
	 * @model default="ALL"
	 * @generated
	 */
	ChangeMode getChangeMode();

	/**
	 * Sets the value of the '{@link org.eclipse.fennec.event.atlas.model.deployment.HistorizationFilter#getChangeMode <em>Change Mode</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @param value the new value of the '<em>Change Mode</em>' attribute.
	 * @see org.eclipse.fennec.event.atlas.model.deployment.ChangeMode
	 * @see #getChangeMode()
	 * @generated
	 */
	void setChangeMode(ChangeMode value);

	/**
	 * Returns the value of the '<em><b>Change Threshold</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * <!-- begin-model-doc -->
	 * DEADBAND: absolute difference from the last stored value. Zero means unset.
	 * <!-- end-model-doc -->
	 * @return the value of the '<em>Change Threshold</em>' attribute.
	 * @see #setChangeThreshold(double)
	 * @see org.eclipse.fennec.event.atlas.model.deployment.DeploymentPackage#getHistorizationFilter_ChangeThreshold()
	 * @model
	 * @generated
	 */
	double getChangeThreshold();

	/**
	 * Sets the value of the '{@link org.eclipse.fennec.event.atlas.model.deployment.HistorizationFilter#getChangeThreshold <em>Change Threshold</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @param value the new value of the '<em>Change Threshold</em>' attribute.
	 * @see #getChangeThreshold()
	 * @generated
	 */
	void setChangeThreshold(double value);

	/**
	 * Returns the value of the '<em><b>Change Threshold Percent</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * <!-- begin-model-doc -->
	 * DEADBAND: difference as a percentage of the last stored value. Zero means unset.
	 * <!-- end-model-doc -->
	 * @return the value of the '<em>Change Threshold Percent</em>' attribute.
	 * @see #setChangeThresholdPercent(double)
	 * @see org.eclipse.fennec.event.atlas.model.deployment.DeploymentPackage#getHistorizationFilter_ChangeThresholdPercent()
	 * @model
	 * @generated
	 */
	double getChangeThresholdPercent();

	/**
	 * Sets the value of the '{@link org.eclipse.fennec.event.atlas.model.deployment.HistorizationFilter#getChangeThresholdPercent <em>Change Threshold Percent</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @param value the new value of the '<em>Change Threshold Percent</em>' attribute.
	 * @see #getChangeThresholdPercent()
	 * @generated
	 */
	void setChangeThresholdPercent(double value);

	/**
	 * Returns the value of the '<em><b>Change Max Interval</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * <!-- begin-model-doc -->
	 * Heartbeat: store a value regardless of the deadband once the last stored value is older than this.
	 * <!-- end-model-doc -->
	 * @return the value of the '<em>Change Max Interval</em>' attribute.
	 * @see #setChangeMaxInterval(Duration)
	 * @see org.eclipse.fennec.event.atlas.model.deployment.DeploymentPackage#getHistorizationFilter_ChangeMaxInterval()
	 * @model dataType="org.eclipse.fennec.event.atlas.model.deployment.EDuration"
	 * @generated
	 */
	Duration getChangeMaxInterval();

	/**
	 * Sets the value of the '{@link org.eclipse.fennec.event.atlas.model.deployment.HistorizationFilter#getChangeMaxInterval <em>Change Max Interval</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @param value the new value of the '<em>Change Max Interval</em>' attribute.
	 * @see #getChangeMaxInterval()
	 * @generated
	 */
	void setChangeMaxInterval(Duration value);

} // HistorizationFilter
