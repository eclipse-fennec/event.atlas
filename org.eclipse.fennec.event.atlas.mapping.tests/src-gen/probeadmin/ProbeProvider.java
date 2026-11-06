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
package probeadmin;

import org.eclipse.sensinact.model.core.provider.DynamicProvider;

import org.osgi.annotation.versioning.ProviderType;

/**
 * <!-- begin-user-doc -->
 * A representation of the model object '<em><b>Probe Provider</b></em>'.
 * <!-- end-user-doc -->
 *
 * <p>
 * The following features are supported:
 * </p>
 * <ul>
 *   <li>{@link probeadmin.ProbeProvider#getThermal <em>Thermal</em>}</li>
 * </ul>
 *
 * @see probeadmin.ProbeadminPackage#getProbeProvider()
 * @model
 * @generated
 */
@ProviderType
public interface ProbeProvider extends DynamicProvider {
	/**
	 * Returns the value of the '<em><b>Thermal</b></em>' reference.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the value of the '<em>Thermal</em>' reference.
	 * @see #setThermal(ProbeAdmin)
	 * @see probeadmin.ProbeadminPackage#getProbeProvider_Thermal()
	 * @model
	 * @generated
	 */
	ProbeAdmin getThermal();

	/**
	 * Sets the value of the '{@link probeadmin.ProbeProvider#getThermal <em>Thermal</em>}' reference.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @param value the new value of the '<em>Thermal</em>' reference.
	 * @see #getThermal()
	 * @generated
	 */
	void setThermal(ProbeAdmin value);

} // ProbeProvider
