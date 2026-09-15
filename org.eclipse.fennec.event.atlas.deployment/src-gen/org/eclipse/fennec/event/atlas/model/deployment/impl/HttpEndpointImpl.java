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
import org.eclipse.fennec.event.atlas.model.deployment.HttpEndpoint;

/**
 * <!-- begin-user-doc -->
 * An implementation of the model object '<em><b>Http Endpoint</b></em>'.
 * <!-- end-user-doc -->
 * <p>
 * The following features are implemented:
 * </p>
 * <ul>
 *   <li>{@link org.eclipse.fennec.event.atlas.model.deployment.impl.HttpEndpointImpl#getPort <em>Port</em>}</li>
 *   <li>{@link org.eclipse.fennec.event.atlas.model.deployment.impl.HttpEndpointImpl#getHost <em>Host</em>}</li>
 *   <li>{@link org.eclipse.fennec.event.atlas.model.deployment.impl.HttpEndpointImpl#getContextPath <em>Context Path</em>}</li>
 *   <li>{@link org.eclipse.fennec.event.atlas.model.deployment.impl.HttpEndpointImpl#getRestPath <em>Rest Path</em>}</li>
 *   <li>{@link org.eclipse.fennec.event.atlas.model.deployment.impl.HttpEndpointImpl#getWhiteboardName <em>Whiteboard Name</em>}</li>
 *   <li>{@link org.eclipse.fennec.event.atlas.model.deployment.impl.HttpEndpointImpl#isAllowAnonymous <em>Allow Anonymous</em>}</li>
 * </ul>
 *
 * @generated
 */
public class HttpEndpointImpl extends MinimalEObjectImpl.Container implements HttpEndpoint {
	/**
	 * The default value of the '{@link #getPort() <em>Port</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getPort()
	 * @generated
	 * @ordered
	 */
	protected static final int PORT_EDEFAULT = 8080;

	/**
	 * The cached value of the '{@link #getPort() <em>Port</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getPort()
	 * @generated
	 * @ordered
	 */
	protected int port = PORT_EDEFAULT;

	/**
	 * The default value of the '{@link #getHost() <em>Host</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getHost()
	 * @generated
	 * @ordered
	 */
	protected static final String HOST_EDEFAULT = "0.0.0.0";

	/**
	 * The cached value of the '{@link #getHost() <em>Host</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getHost()
	 * @generated
	 * @ordered
	 */
	protected String host = HOST_EDEFAULT;

	/**
	 * The default value of the '{@link #getContextPath() <em>Context Path</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getContextPath()
	 * @generated
	 * @ordered
	 */
	protected static final String CONTEXT_PATH_EDEFAULT = "event/";

	/**
	 * The cached value of the '{@link #getContextPath() <em>Context Path</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getContextPath()
	 * @generated
	 * @ordered
	 */
	protected String contextPath = CONTEXT_PATH_EDEFAULT;

	/**
	 * The default value of the '{@link #getRestPath() <em>Rest Path</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getRestPath()
	 * @generated
	 * @ordered
	 */
	protected static final String REST_PATH_EDEFAULT = "rest";

	/**
	 * The cached value of the '{@link #getRestPath() <em>Rest Path</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getRestPath()
	 * @generated
	 * @ordered
	 */
	protected String restPath = REST_PATH_EDEFAULT;

	/**
	 * The default value of the '{@link #getWhiteboardName() <em>Whiteboard Name</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getWhiteboardName()
	 * @generated
	 * @ordered
	 */
	protected static final String WHITEBOARD_NAME_EDEFAULT = "eventrest";

	/**
	 * The cached value of the '{@link #getWhiteboardName() <em>Whiteboard Name</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getWhiteboardName()
	 * @generated
	 * @ordered
	 */
	protected String whiteboardName = WHITEBOARD_NAME_EDEFAULT;

	/**
	 * The default value of the '{@link #isAllowAnonymous() <em>Allow Anonymous</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #isAllowAnonymous()
	 * @generated
	 * @ordered
	 */
	protected static final boolean ALLOW_ANONYMOUS_EDEFAULT = true;

	/**
	 * The cached value of the '{@link #isAllowAnonymous() <em>Allow Anonymous</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #isAllowAnonymous()
	 * @generated
	 * @ordered
	 */
	protected boolean allowAnonymous = ALLOW_ANONYMOUS_EDEFAULT;

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	protected HttpEndpointImpl() {
		super();
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	protected EClass eStaticClass() {
		return DeploymentPackage.Literals.HTTP_ENDPOINT;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public int getPort() {
		return port;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public void setPort(int newPort) {
		int oldPort = port;
		port = newPort;
		if (eNotificationRequired())
			eNotify(new ENotificationImpl(this, Notification.SET, DeploymentPackage.HTTP_ENDPOINT__PORT, oldPort, port));
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public String getHost() {
		return host;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public void setHost(String newHost) {
		String oldHost = host;
		host = newHost;
		if (eNotificationRequired())
			eNotify(new ENotificationImpl(this, Notification.SET, DeploymentPackage.HTTP_ENDPOINT__HOST, oldHost, host));
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public String getContextPath() {
		return contextPath;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public void setContextPath(String newContextPath) {
		String oldContextPath = contextPath;
		contextPath = newContextPath;
		if (eNotificationRequired())
			eNotify(new ENotificationImpl(this, Notification.SET, DeploymentPackage.HTTP_ENDPOINT__CONTEXT_PATH, oldContextPath, contextPath));
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public String getRestPath() {
		return restPath;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public void setRestPath(String newRestPath) {
		String oldRestPath = restPath;
		restPath = newRestPath;
		if (eNotificationRequired())
			eNotify(new ENotificationImpl(this, Notification.SET, DeploymentPackage.HTTP_ENDPOINT__REST_PATH, oldRestPath, restPath));
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public String getWhiteboardName() {
		return whiteboardName;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public void setWhiteboardName(String newWhiteboardName) {
		String oldWhiteboardName = whiteboardName;
		whiteboardName = newWhiteboardName;
		if (eNotificationRequired())
			eNotify(new ENotificationImpl(this, Notification.SET, DeploymentPackage.HTTP_ENDPOINT__WHITEBOARD_NAME, oldWhiteboardName, whiteboardName));
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public boolean isAllowAnonymous() {
		return allowAnonymous;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public void setAllowAnonymous(boolean newAllowAnonymous) {
		boolean oldAllowAnonymous = allowAnonymous;
		allowAnonymous = newAllowAnonymous;
		if (eNotificationRequired())
			eNotify(new ENotificationImpl(this, Notification.SET, DeploymentPackage.HTTP_ENDPOINT__ALLOW_ANONYMOUS, oldAllowAnonymous, allowAnonymous));
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public Object eGet(int featureID, boolean resolve, boolean coreType) {
		switch (featureID) {
			case DeploymentPackage.HTTP_ENDPOINT__PORT:
				return getPort();
			case DeploymentPackage.HTTP_ENDPOINT__HOST:
				return getHost();
			case DeploymentPackage.HTTP_ENDPOINT__CONTEXT_PATH:
				return getContextPath();
			case DeploymentPackage.HTTP_ENDPOINT__REST_PATH:
				return getRestPath();
			case DeploymentPackage.HTTP_ENDPOINT__WHITEBOARD_NAME:
				return getWhiteboardName();
			case DeploymentPackage.HTTP_ENDPOINT__ALLOW_ANONYMOUS:
				return isAllowAnonymous();
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
			case DeploymentPackage.HTTP_ENDPOINT__PORT:
				setPort((Integer)newValue);
				return;
			case DeploymentPackage.HTTP_ENDPOINT__HOST:
				setHost((String)newValue);
				return;
			case DeploymentPackage.HTTP_ENDPOINT__CONTEXT_PATH:
				setContextPath((String)newValue);
				return;
			case DeploymentPackage.HTTP_ENDPOINT__REST_PATH:
				setRestPath((String)newValue);
				return;
			case DeploymentPackage.HTTP_ENDPOINT__WHITEBOARD_NAME:
				setWhiteboardName((String)newValue);
				return;
			case DeploymentPackage.HTTP_ENDPOINT__ALLOW_ANONYMOUS:
				setAllowAnonymous((Boolean)newValue);
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
			case DeploymentPackage.HTTP_ENDPOINT__PORT:
				setPort(PORT_EDEFAULT);
				return;
			case DeploymentPackage.HTTP_ENDPOINT__HOST:
				setHost(HOST_EDEFAULT);
				return;
			case DeploymentPackage.HTTP_ENDPOINT__CONTEXT_PATH:
				setContextPath(CONTEXT_PATH_EDEFAULT);
				return;
			case DeploymentPackage.HTTP_ENDPOINT__REST_PATH:
				setRestPath(REST_PATH_EDEFAULT);
				return;
			case DeploymentPackage.HTTP_ENDPOINT__WHITEBOARD_NAME:
				setWhiteboardName(WHITEBOARD_NAME_EDEFAULT);
				return;
			case DeploymentPackage.HTTP_ENDPOINT__ALLOW_ANONYMOUS:
				setAllowAnonymous(ALLOW_ANONYMOUS_EDEFAULT);
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
			case DeploymentPackage.HTTP_ENDPOINT__PORT:
				return port != PORT_EDEFAULT;
			case DeploymentPackage.HTTP_ENDPOINT__HOST:
				return HOST_EDEFAULT == null ? host != null : !HOST_EDEFAULT.equals(host);
			case DeploymentPackage.HTTP_ENDPOINT__CONTEXT_PATH:
				return CONTEXT_PATH_EDEFAULT == null ? contextPath != null : !CONTEXT_PATH_EDEFAULT.equals(contextPath);
			case DeploymentPackage.HTTP_ENDPOINT__REST_PATH:
				return REST_PATH_EDEFAULT == null ? restPath != null : !REST_PATH_EDEFAULT.equals(restPath);
			case DeploymentPackage.HTTP_ENDPOINT__WHITEBOARD_NAME:
				return WHITEBOARD_NAME_EDEFAULT == null ? whiteboardName != null : !WHITEBOARD_NAME_EDEFAULT.equals(whiteboardName);
			case DeploymentPackage.HTTP_ENDPOINT__ALLOW_ANONYMOUS:
				return allowAnonymous != ALLOW_ANONYMOUS_EDEFAULT;
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
		result.append(" (port: ");
		result.append(port);
		result.append(", host: ");
		result.append(host);
		result.append(", contextPath: ");
		result.append(contextPath);
		result.append(", restPath: ");
		result.append(restPath);
		result.append(", whiteboardName: ");
		result.append(whiteboardName);
		result.append(", allowAnonymous: ");
		result.append(allowAnonymous);
		result.append(')');
		return result.toString();
	}

} //HttpEndpointImpl
