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
 *      Mark Hoffmann - initial API and implementation
 */
package org.eclipse.fennec.cloudevents.model.cloudevents.impl;

import org.eclipse.emf.common.notify.Notification;

import org.eclipse.emf.ecore.EClass;

import org.eclipse.emf.ecore.impl.ENotificationImpl;

import org.eclipse.fennec.cloudevents.model.cloudevents.CloudEventsPackage;
import org.eclipse.fennec.cloudevents.model.cloudevents.MqttSource;

/**
 * <!-- begin-user-doc -->
 * An implementation of the model object '<em><b>Mqtt Source</b></em>'.
 * <!-- end-user-doc -->
 * <p>
 * The following features are implemented:
 * </p>
 * <ul>
 *   <li>{@link org.eclipse.fennec.cloudevents.model.cloudevents.impl.MqttSourceImpl#getBroker <em>Broker</em>}</li>
 * </ul>
 *
 * @generated
 */
public class MqttSourceImpl extends SourceImpl implements MqttSource {
	/**
	 * The default value of the '{@link #getBroker() <em>Broker</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getBroker()
	 * @generated
	 * @ordered
	 */
	protected static final String BROKER_EDEFAULT = null;

	/**
	 * The cached value of the '{@link #getBroker() <em>Broker</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getBroker()
	 * @generated
	 * @ordered
	 */
	protected String broker = BROKER_EDEFAULT;

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	protected MqttSourceImpl() {
		super();
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	protected EClass eStaticClass() {
		return CloudEventsPackage.Literals.MQTT_SOURCE;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public String getBroker() {
		return broker;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public void setBroker(String newBroker) {
		String oldBroker = broker;
		broker = newBroker;
		if (eNotificationRequired())
			eNotify(new ENotificationImpl(this, Notification.SET, CloudEventsPackage.MQTT_SOURCE__BROKER, oldBroker, broker));
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public Object eGet(int featureID, boolean resolve, boolean coreType) {
		switch (featureID) {
			case CloudEventsPackage.MQTT_SOURCE__BROKER:
				return getBroker();
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
			case CloudEventsPackage.MQTT_SOURCE__BROKER:
				setBroker((String)newValue);
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
			case CloudEventsPackage.MQTT_SOURCE__BROKER:
				setBroker(BROKER_EDEFAULT);
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
			case CloudEventsPackage.MQTT_SOURCE__BROKER:
				return BROKER_EDEFAULT == null ? broker != null : !BROKER_EDEFAULT.equals(broker);
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
		result.append(" (broker: ");
		result.append(broker);
		result.append(')');
		return result.toString();
	}

} //MqttSourceImpl
