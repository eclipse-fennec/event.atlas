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

import org.eclipse.emf.ecore.EObject;

import org.osgi.annotation.versioning.ProviderType;

/**
 * <!-- begin-user-doc -->
 * A representation of the model object '<em><b>Inference Config</b></em>'.
 * <!-- end-user-doc -->
 *
 * <!-- begin-model-doc -->
 * Optional model inference for payloads with no resolvable model.
 * 
 * Two switches gate different costs, and both default to off: samplingEnabled gates BUFFERING, namespace gates the RUN, which is what costs money. Sampling on with a blank namespace is a deliberate state - the log shows what would be inferred at no API cost.
 * <!-- end-model-doc -->
 *
 * <p>
 * The following features are supported:
 * </p>
 * <ul>
 *   <li>{@link org.eclipse.fennec.event.atlas.model.deployment.InferenceConfig#isSamplingEnabled <em>Sampling Enabled</em>}</li>
 *   <li>{@link org.eclipse.fennec.event.atlas.model.deployment.InferenceConfig#getNamespace <em>Namespace</em>}</li>
 *   <li>{@link org.eclipse.fennec.event.atlas.model.deployment.InferenceConfig#getMaxRunsPerInterval <em>Max Runs Per Interval</em>}</li>
 * </ul>
 *
 * @see org.eclipse.fennec.event.atlas.model.deployment.DeploymentPackage#getInferenceConfig()
 * @model
 * @generated
 */
@ProviderType
public interface InferenceConfig extends EObject {
	/**
	 * Returns the value of the '<em><b>Sampling Enabled</b></em>' attribute.
	 * The default value is <code>"false"</code>.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the value of the '<em>Sampling Enabled</em>' attribute.
	 * @see #setSamplingEnabled(boolean)
	 * @see org.eclipse.fennec.event.atlas.model.deployment.DeploymentPackage#getInferenceConfig_SamplingEnabled()
	 * @model default="false"
	 * @generated
	 */
	boolean isSamplingEnabled();

	/**
	 * Sets the value of the '{@link org.eclipse.fennec.event.atlas.model.deployment.InferenceConfig#isSamplingEnabled <em>Sampling Enabled</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @param value the new value of the '<em>Sampling Enabled</em>' attribute.
	 * @see #isSamplingEnabled()
	 * @generated
	 */
	void setSamplingEnabled(boolean value);

	/**
	 * Returns the value of the '<em><b>Namespace</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * <!-- begin-model-doc -->
	 * Namespace PREFIX drafts are published beneath - not a whole nsURI: the agent extends it with a segment naming the model it authored. Blank is the off position for the run.
	 * <!-- end-model-doc -->
	 * @return the value of the '<em>Namespace</em>' attribute.
	 * @see #setNamespace(String)
	 * @see org.eclipse.fennec.event.atlas.model.deployment.DeploymentPackage#getInferenceConfig_Namespace()
	 * @model
	 * @generated
	 */
	String getNamespace();

	/**
	 * Sets the value of the '{@link org.eclipse.fennec.event.atlas.model.deployment.InferenceConfig#getNamespace <em>Namespace</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @param value the new value of the '<em>Namespace</em>' attribute.
	 * @see #getNamespace()
	 * @generated
	 */
	void setNamespace(String value);

	/**
	 * Returns the value of the '<em><b>Max Runs Per Interval</b></em>' attribute.
	 * The default value is <code>"5"</code>.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * <!-- begin-model-doc -->
	 * Run cap per interval. It is ONE counter for the whole runtime, not per channel: with a cap of 1 the first sample set to close takes the run and any other channel's is refused and discarded. Raise it to at least the number of distinct unknown families expected per interval.
	 * <!-- end-model-doc -->
	 * @return the value of the '<em>Max Runs Per Interval</em>' attribute.
	 * @see #setMaxRunsPerInterval(int)
	 * @see org.eclipse.fennec.event.atlas.model.deployment.DeploymentPackage#getInferenceConfig_MaxRunsPerInterval()
	 * @model default="5"
	 * @generated
	 */
	int getMaxRunsPerInterval();

	/**
	 * Sets the value of the '{@link org.eclipse.fennec.event.atlas.model.deployment.InferenceConfig#getMaxRunsPerInterval <em>Max Runs Per Interval</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @param value the new value of the '<em>Max Runs Per Interval</em>' attribute.
	 * @see #getMaxRunsPerInterval()
	 * @generated
	 */
	void setMaxRunsPerInterval(int value);

} // InferenceConfig
