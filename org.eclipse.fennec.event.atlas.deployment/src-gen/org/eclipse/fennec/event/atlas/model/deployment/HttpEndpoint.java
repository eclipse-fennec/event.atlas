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
 * A representation of the model object '<em><b>Http Endpoint</b></em>'.
 * <!-- end-user-doc -->
 *
 * <!-- begin-model-doc -->
 * The named Felix HTTP whiteboard that serves this runtime, plus the Jersey whiteboard mounted on it. Every REST base of the runtime starts at http://host:port/contextPath/restPath.
 * <!-- end-model-doc -->
 *
 * <p>
 * The following features are supported:
 * </p>
 * <ul>
 *   <li>{@link org.eclipse.fennec.event.atlas.model.deployment.HttpEndpoint#getPort <em>Port</em>}</li>
 *   <li>{@link org.eclipse.fennec.event.atlas.model.deployment.HttpEndpoint#getHost <em>Host</em>}</li>
 *   <li>{@link org.eclipse.fennec.event.atlas.model.deployment.HttpEndpoint#getContextPath <em>Context Path</em>}</li>
 *   <li>{@link org.eclipse.fennec.event.atlas.model.deployment.HttpEndpoint#getRestPath <em>Rest Path</em>}</li>
 *   <li>{@link org.eclipse.fennec.event.atlas.model.deployment.HttpEndpoint#getWhiteboardName <em>Whiteboard Name</em>}</li>
 *   <li>{@link org.eclipse.fennec.event.atlas.model.deployment.HttpEndpoint#isAllowAnonymous <em>Allow Anonymous</em>}</li>
 * </ul>
 *
 * @see org.eclipse.fennec.event.atlas.model.deployment.DeploymentPackage#getHttpEndpoint()
 * @model
 * @generated
 */
@ProviderType
public interface HttpEndpoint extends EObject {
	/**
	 * Returns the value of the '<em><b>Port</b></em>' attribute.
	 * The default value is <code>"8080"</code>.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * <!-- begin-model-doc -->
	 * The port the named whiteboard binds. The framework's default HTTP service is switched off in the bndrun, so this port belongs to the configuration.
	 * <!-- end-model-doc -->
	 * @return the value of the '<em>Port</em>' attribute.
	 * @see #setPort(int)
	 * @see org.eclipse.fennec.event.atlas.model.deployment.DeploymentPackage#getHttpEndpoint_Port()
	 * @model default="8080"
	 * @generated
	 */
	int getPort();

	/**
	 * Sets the value of the '{@link org.eclipse.fennec.event.atlas.model.deployment.HttpEndpoint#getPort <em>Port</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @param value the new value of the '<em>Port</em>' attribute.
	 * @see #getPort()
	 * @generated
	 */
	void setPort(int value);

	/**
	 * Returns the value of the '<em><b>Host</b></em>' attribute.
	 * The default value is <code>"0.0.0.0"</code>.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the value of the '<em>Host</em>' attribute.
	 * @see #setHost(String)
	 * @see org.eclipse.fennec.event.atlas.model.deployment.DeploymentPackage#getHttpEndpoint_Host()
	 * @model default="0.0.0.0"
	 * @generated
	 */
	String getHost();

	/**
	 * Sets the value of the '{@link org.eclipse.fennec.event.atlas.model.deployment.HttpEndpoint#getHost <em>Host</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @param value the new value of the '<em>Host</em>' attribute.
	 * @see #getHost()
	 * @generated
	 */
	void setHost(String value);

	/**
	 * Returns the value of the '<em><b>Context Path</b></em>' attribute.
	 * The default value is <code>"event/"</code>.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the value of the '<em>Context Path</em>' attribute.
	 * @see #setContextPath(String)
	 * @see org.eclipse.fennec.event.atlas.model.deployment.DeploymentPackage#getHttpEndpoint_ContextPath()
	 * @model default="event/"
	 * @generated
	 */
	String getContextPath();

	/**
	 * Sets the value of the '{@link org.eclipse.fennec.event.atlas.model.deployment.HttpEndpoint#getContextPath <em>Context Path</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @param value the new value of the '<em>Context Path</em>' attribute.
	 * @see #getContextPath()
	 * @generated
	 */
	void setContextPath(String value);

	/**
	 * Returns the value of the '<em><b>Rest Path</b></em>' attribute.
	 * The default value is <code>"rest"</code>.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the value of the '<em>Rest Path</em>' attribute.
	 * @see #setRestPath(String)
	 * @see org.eclipse.fennec.event.atlas.model.deployment.DeploymentPackage#getHttpEndpoint_RestPath()
	 * @model default="rest"
	 * @generated
	 */
	String getRestPath();

	/**
	 * Sets the value of the '{@link org.eclipse.fennec.event.atlas.model.deployment.HttpEndpoint#getRestPath <em>Rest Path</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @param value the new value of the '<em>Rest Path</em>' attribute.
	 * @see #getRestPath()
	 * @generated
	 */
	void setRestPath(String value);

	/**
	 * Returns the value of the '<em><b>Whiteboard Name</b></em>' attribute.
	 * The default value is <code>"eventrest"</code>.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * <!-- begin-model-doc -->
	 * Name of the Jersey whiteboard. A Jakarta-RS application targets it through (jersey.jakartars.whiteboard.name=...).
	 * <!-- end-model-doc -->
	 * @return the value of the '<em>Whiteboard Name</em>' attribute.
	 * @see #setWhiteboardName(String)
	 * @see org.eclipse.fennec.event.atlas.model.deployment.DeploymentPackage#getHttpEndpoint_WhiteboardName()
	 * @model default="eventrest"
	 * @generated
	 */
	String getWhiteboardName();

	/**
	 * Sets the value of the '{@link org.eclipse.fennec.event.atlas.model.deployment.HttpEndpoint#getWhiteboardName <em>Whiteboard Name</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @param value the new value of the '<em>Whiteboard Name</em>' attribute.
	 * @see #getWhiteboardName()
	 * @generated
	 */
	void setWhiteboardName(String value);

	/**
	 * Returns the value of the '<em><b>Allow Anonymous</b></em>' attribute.
	 * The default value is <code>"true"</code>.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * <!-- begin-model-doc -->
	 * Whether the northbound REST API answers unauthenticated requests.
	 * <!-- end-model-doc -->
	 * @return the value of the '<em>Allow Anonymous</em>' attribute.
	 * @see #setAllowAnonymous(boolean)
	 * @see org.eclipse.fennec.event.atlas.model.deployment.DeploymentPackage#getHttpEndpoint_AllowAnonymous()
	 * @model default="true"
	 * @generated
	 */
	boolean isAllowAnonymous();

	/**
	 * Sets the value of the '{@link org.eclipse.fennec.event.atlas.model.deployment.HttpEndpoint#isAllowAnonymous <em>Allow Anonymous</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @param value the new value of the '<em>Allow Anonymous</em>' attribute.
	 * @see #isAllowAnonymous()
	 * @generated
	 */
	void setAllowAnonymous(boolean value);

} // HttpEndpoint
