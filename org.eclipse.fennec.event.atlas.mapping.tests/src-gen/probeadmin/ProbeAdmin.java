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

import org.eclipse.sensinact.model.core.provider.Admin;

import org.osgi.annotation.versioning.ProviderType;

/**
 * <!-- begin-user-doc -->
 * A representation of the model object '<em><b>Probe Admin</b></em>'.
 * <!-- end-user-doc -->
 *
 * <p>
 * The following features are supported:
 * </p>
 * <ul>
 *   <li>{@link probeadmin.ProbeAdmin#getViewport <em>Viewport</em>}</li>
 * </ul>
 *
 * @see probeadmin.ProbeadminPackage#getProbeAdmin()
 * @model
 * @generated
 */
@ProviderType
public interface ProbeAdmin extends Admin {
	/**
	 * Returns the value of the '<em><b>Viewport</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the value of the '<em>Viewport</em>' attribute.
	 * @see #setViewport(String)
	 * @see probeadmin.ProbeadminPackage#getProbeAdmin_Viewport()
	 * @model
	 * @generated
	 */
	String getViewport();

	/**
	 * Sets the value of the '{@link probeadmin.ProbeAdmin#getViewport <em>Viewport</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @param value the new value of the '<em>Viewport</em>' attribute.
	 * @see #getViewport()
	 * @generated
	 */
	void setViewport(String value);

} // ProbeAdmin
