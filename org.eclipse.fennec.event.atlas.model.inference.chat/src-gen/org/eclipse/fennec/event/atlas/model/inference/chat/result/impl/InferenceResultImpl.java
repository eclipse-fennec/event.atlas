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
package org.eclipse.fennec.event.atlas.model.inference.chat.result.impl;

import org.eclipse.emf.common.notify.Notification;

import org.eclipse.emf.ecore.EClass;

import org.eclipse.emf.ecore.impl.ENotificationImpl;
import org.eclipse.emf.ecore.impl.MinimalEObjectImpl;

import org.eclipse.fennec.event.atlas.model.inference.chat.result.InferenceResult;
import org.eclipse.fennec.event.atlas.model.inference.chat.result.InferenceResultPackage;
import org.eclipse.fennec.event.atlas.model.inference.chat.result.InferenceStatus;

/**
 * <!-- begin-user-doc -->
 * An implementation of the model object '<em><b>Inference Result</b></em>'.
 * <!-- end-user-doc -->
 * <p>
 * The following features are implemented:
 * </p>
 * <ul>
 *   <li>{@link org.eclipse.fennec.event.atlas.model.inference.chat.result.impl.InferenceResultImpl#getStatus <em>Status</em>}</li>
 *   <li>{@link org.eclipse.fennec.event.atlas.model.inference.chat.result.impl.InferenceResultImpl#getNsUri <em>Ns Uri</em>}</li>
 *   <li>{@link org.eclipse.fennec.event.atlas.model.inference.chat.result.impl.InferenceResultImpl#getMessage <em>Message</em>}</li>
 * </ul>
 *
 * @generated
 */
public class InferenceResultImpl extends MinimalEObjectImpl.Container implements InferenceResult {
	/**
	 * The default value of the '{@link #getStatus() <em>Status</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getStatus()
	 * @generated
	 * @ordered
	 */
	protected static final InferenceStatus STATUS_EDEFAULT = InferenceStatus.PUBLISHED;

	/**
	 * The cached value of the '{@link #getStatus() <em>Status</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getStatus()
	 * @generated
	 * @ordered
	 */
	protected InferenceStatus status = STATUS_EDEFAULT;

	/**
	 * The default value of the '{@link #getNsUri() <em>Ns Uri</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getNsUri()
	 * @generated
	 * @ordered
	 */
	protected static final String NS_URI_EDEFAULT = null;

	/**
	 * The cached value of the '{@link #getNsUri() <em>Ns Uri</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getNsUri()
	 * @generated
	 * @ordered
	 */
	protected String nsUri = NS_URI_EDEFAULT;

	/**
	 * The default value of the '{@link #getMessage() <em>Message</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getMessage()
	 * @generated
	 * @ordered
	 */
	protected static final String MESSAGE_EDEFAULT = null;

	/**
	 * The cached value of the '{@link #getMessage() <em>Message</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getMessage()
	 * @generated
	 * @ordered
	 */
	protected String message = MESSAGE_EDEFAULT;

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	protected InferenceResultImpl() {
		super();
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	protected EClass eStaticClass() {
		return InferenceResultPackage.Literals.INFERENCE_RESULT;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public InferenceStatus getStatus() {
		return status;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public void setStatus(InferenceStatus newStatus) {
		InferenceStatus oldStatus = status;
		status = newStatus == null ? STATUS_EDEFAULT : newStatus;
		if (eNotificationRequired())
			eNotify(new ENotificationImpl(this, Notification.SET, InferenceResultPackage.INFERENCE_RESULT__STATUS, oldStatus, status));
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public String getNsUri() {
		return nsUri;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public void setNsUri(String newNsUri) {
		String oldNsUri = nsUri;
		nsUri = newNsUri;
		if (eNotificationRequired())
			eNotify(new ENotificationImpl(this, Notification.SET, InferenceResultPackage.INFERENCE_RESULT__NS_URI, oldNsUri, nsUri));
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public String getMessage() {
		return message;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public void setMessage(String newMessage) {
		String oldMessage = message;
		message = newMessage;
		if (eNotificationRequired())
			eNotify(new ENotificationImpl(this, Notification.SET, InferenceResultPackage.INFERENCE_RESULT__MESSAGE, oldMessage, message));
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public Object eGet(int featureID, boolean resolve, boolean coreType) {
		switch (featureID) {
			case InferenceResultPackage.INFERENCE_RESULT__STATUS:
				return getStatus();
			case InferenceResultPackage.INFERENCE_RESULT__NS_URI:
				return getNsUri();
			case InferenceResultPackage.INFERENCE_RESULT__MESSAGE:
				return getMessage();
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
			case InferenceResultPackage.INFERENCE_RESULT__STATUS:
				setStatus((InferenceStatus)newValue);
				return;
			case InferenceResultPackage.INFERENCE_RESULT__NS_URI:
				setNsUri((String)newValue);
				return;
			case InferenceResultPackage.INFERENCE_RESULT__MESSAGE:
				setMessage((String)newValue);
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
			case InferenceResultPackage.INFERENCE_RESULT__STATUS:
				setStatus(STATUS_EDEFAULT);
				return;
			case InferenceResultPackage.INFERENCE_RESULT__NS_URI:
				setNsUri(NS_URI_EDEFAULT);
				return;
			case InferenceResultPackage.INFERENCE_RESULT__MESSAGE:
				setMessage(MESSAGE_EDEFAULT);
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
			case InferenceResultPackage.INFERENCE_RESULT__STATUS:
				return status != STATUS_EDEFAULT;
			case InferenceResultPackage.INFERENCE_RESULT__NS_URI:
				return NS_URI_EDEFAULT == null ? nsUri != null : !NS_URI_EDEFAULT.equals(nsUri);
			case InferenceResultPackage.INFERENCE_RESULT__MESSAGE:
				return MESSAGE_EDEFAULT == null ? message != null : !MESSAGE_EDEFAULT.equals(message);
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
		result.append(" (status: ");
		result.append(status);
		result.append(", nsUri: ");
		result.append(nsUri);
		result.append(", message: ");
		result.append(message);
		result.append(')');
		return result.toString();
	}

} //InferenceResultImpl
