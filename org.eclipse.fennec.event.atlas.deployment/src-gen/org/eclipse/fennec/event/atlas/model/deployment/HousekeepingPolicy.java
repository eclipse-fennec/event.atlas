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
 * A representation of the model object '<em><b>Housekeeping Policy</b></em>'.
 * <!-- end-user-doc -->
 *
 * <!-- begin-model-doc -->
 * Periodic pruning of stored history. At least one of retentionPeriod and keepCount is required; a policy with neither is refused rather than written.
 * 
 * Policies only apply to backends that can delete.
 * <!-- end-model-doc -->
 *
 * <p>
 * The following features are supported:
 * </p>
 * <ul>
 *   <li>{@link org.eclipse.fennec.event.atlas.model.deployment.HousekeepingPolicy#getName <em>Name</em>}</li>
 *   <li>{@link org.eclipse.fennec.event.atlas.model.deployment.HousekeepingPolicy#getTargets <em>Targets</em>}</li>
 *   <li>{@link org.eclipse.fennec.event.atlas.model.deployment.HousekeepingPolicy#getRetentionPeriod <em>Retention Period</em>}</li>
 *   <li>{@link org.eclipse.fennec.event.atlas.model.deployment.HousekeepingPolicy#getKeepCount <em>Keep Count</em>}</li>
 *   <li>{@link org.eclipse.fennec.event.atlas.model.deployment.HousekeepingPolicy#getMaxDelete <em>Max Delete</em>}</li>
 *   <li>{@link org.eclipse.fennec.event.atlas.model.deployment.HousekeepingPolicy#getSchedulePeriod <em>Schedule Period</em>}</li>
 * </ul>
 *
 * @see org.eclipse.fennec.event.atlas.model.deployment.DeploymentPackage#getHousekeepingPolicy()
 * @model
 * @generated
 */
@ProviderType
public interface HousekeepingPolicy extends EObject {
	/**
	 * Returns the value of the '<em><b>Name</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the value of the '<em>Name</em>' attribute.
	 * @see #setName(String)
	 * @see org.eclipse.fennec.event.atlas.model.deployment.DeploymentPackage#getHousekeepingPolicy_Name()
	 * @model required="true"
	 * @generated
	 */
	String getName();

	/**
	 * Sets the value of the '{@link org.eclipse.fennec.event.atlas.model.deployment.HousekeepingPolicy#getName <em>Name</em>}' attribute.
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
	 * History provider names this policy applies to. Empty means all of them.
	 * <!-- end-model-doc -->
	 * @return the value of the '<em>Targets</em>' attribute list.
	 * @see org.eclipse.fennec.event.atlas.model.deployment.DeploymentPackage#getHousekeepingPolicy_Targets()
	 * @model
	 * @generated
	 */
	EList<String> getTargets();

	/**
	 * Returns the value of the '<em><b>Retention Period</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * <!-- begin-model-doc -->
	 * Values older than this are deleted.
	 * <!-- end-model-doc -->
	 * @return the value of the '<em>Retention Period</em>' attribute.
	 * @see #setRetentionPeriod(Duration)
	 * @see org.eclipse.fennec.event.atlas.model.deployment.DeploymentPackage#getHousekeepingPolicy_RetentionPeriod()
	 * @model dataType="org.eclipse.fennec.event.atlas.model.deployment.EDuration"
	 * @generated
	 */
	Duration getRetentionPeriod();

	/**
	 * Sets the value of the '{@link org.eclipse.fennec.event.atlas.model.deployment.HousekeepingPolicy#getRetentionPeriod <em>Retention Period</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @param value the new value of the '<em>Retention Period</em>' attribute.
	 * @see #getRetentionPeriod()
	 * @generated
	 */
	void setRetentionPeriod(Duration value);

	/**
	 * Returns the value of the '<em><b>Keep Count</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * <!-- begin-model-doc -->
	 * Keep at most this many newest values per resource. Zero means unset.
	 * <!-- end-model-doc -->
	 * @return the value of the '<em>Keep Count</em>' attribute.
	 * @see #setKeepCount(int)
	 * @see org.eclipse.fennec.event.atlas.model.deployment.DeploymentPackage#getHousekeepingPolicy_KeepCount()
	 * @model
	 * @generated
	 */
	int getKeepCount();

	/**
	 * Sets the value of the '{@link org.eclipse.fennec.event.atlas.model.deployment.HousekeepingPolicy#getKeepCount <em>Keep Count</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @param value the new value of the '<em>Keep Count</em>' attribute.
	 * @see #getKeepCount()
	 * @generated
	 */
	void setKeepCount(int value);

	/**
	 * Returns the value of the '<em><b>Max Delete</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * <!-- begin-model-doc -->
	 * Safety cap on the rows a single run may delete. Zero means unset.
	 * <!-- end-model-doc -->
	 * @return the value of the '<em>Max Delete</em>' attribute.
	 * @see #setMaxDelete(int)
	 * @see org.eclipse.fennec.event.atlas.model.deployment.DeploymentPackage#getHousekeepingPolicy_MaxDelete()
	 * @model
	 * @generated
	 */
	int getMaxDelete();

	/**
	 * Sets the value of the '{@link org.eclipse.fennec.event.atlas.model.deployment.HousekeepingPolicy#getMaxDelete <em>Max Delete</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @param value the new value of the '<em>Max Delete</em>' attribute.
	 * @see #getMaxDelete()
	 * @generated
	 */
	void setMaxDelete(int value);

	/**
	 * Returns the value of the '<em><b>Schedule Period</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * <!-- begin-model-doc -->
	 * Time between runs. The first run happens one full period after activation.
	 * <!-- end-model-doc -->
	 * @return the value of the '<em>Schedule Period</em>' attribute.
	 * @see #setSchedulePeriod(Duration)
	 * @see org.eclipse.fennec.event.atlas.model.deployment.DeploymentPackage#getHousekeepingPolicy_SchedulePeriod()
	 * @model dataType="org.eclipse.fennec.event.atlas.model.deployment.EDuration"
	 * @generated
	 */
	Duration getSchedulePeriod();

	/**
	 * Sets the value of the '{@link org.eclipse.fennec.event.atlas.model.deployment.HousekeepingPolicy#getSchedulePeriod <em>Schedule Period</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @param value the new value of the '<em>Schedule Period</em>' attribute.
	 * @see #getSchedulePeriod()
	 * @generated
	 */
	void setSchedulePeriod(Duration value);

} // HousekeepingPolicy
