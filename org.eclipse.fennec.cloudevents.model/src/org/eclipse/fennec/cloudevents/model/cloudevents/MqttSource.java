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
package org.eclipse.fennec.cloudevents.model.cloudevents;

import org.osgi.annotation.versioning.ProviderType;

/**
 * <!-- begin-user-doc -->
 * A representation of the model object '<em><b>Mqtt Source</b></em>'.
 * <!-- end-user-doc -->
 *
 * <p>
 * The following features are supported:
 * </p>
 * <ul>
 *   <li>{@link org.eclipse.fennec.cloudevents.model.cloudevents.MqttSource#getBroker <em>Broker</em>}</li>
 * </ul>
 *
 * @see org.eclipse.fennec.cloudevents.model.cloudevents.CloudEventsPackage#getMqttSource()
 * @model
 * @generated
 */
@ProviderType
public interface MqttSource extends Source {
	/**
	 * Returns the value of the '<em><b>Broker</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the value of the '<em>Broker</em>' attribute.
	 * @see #setBroker(String)
	 * @see org.eclipse.fennec.cloudevents.model.cloudevents.CloudEventsPackage#getMqttSource_Broker()
	 * @model
	 * @generated
	 */
	String getBroker();

	/**
	 * Sets the value of the '{@link org.eclipse.fennec.cloudevents.model.cloudevents.MqttSource#getBroker <em>Broker</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @param value the new value of the '<em>Broker</em>' attribute.
	 * @see #getBroker()
	 * @generated
	 */
	void setBroker(String value);

} // MqttSource
