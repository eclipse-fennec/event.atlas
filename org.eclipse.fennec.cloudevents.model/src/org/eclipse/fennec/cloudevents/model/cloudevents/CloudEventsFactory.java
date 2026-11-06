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

import org.eclipse.emf.ecore.EFactory;

import org.osgi.annotation.versioning.ProviderType;

/**
 * <!-- begin-user-doc -->
 * The <b>Factory</b> for the model.
 * It provides a create method for each non-abstract class of the model.
 * <!-- end-user-doc -->
 * @see org.eclipse.fennec.cloudevents.model.cloudevents.CloudEventsPackage
 * @generated
 */
@ProviderType
public interface CloudEventsFactory extends EFactory {
	/**
	 * The singleton instance of the factory.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	CloudEventsFactory eINSTANCE = org.eclipse.fennec.cloudevents.model.cloudevents.impl.CloudEventsFactoryImpl.init();

	/**
	 * Returns a new object of class '<em>Cloud Event</em>'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return a new object of class '<em>Cloud Event</em>'.
	 * @generated
	 */
	CloudEvent createCloudEvent();

	/**
	 * Returns a new object of class '<em>Source</em>'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return a new object of class '<em>Source</em>'.
	 * @generated
	 */
	Source createSource();

	/**
	 * Returns a new object of class '<em>Mqtt Source</em>'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return a new object of class '<em>Mqtt Source</em>'.
	 * @generated
	 */
	MqttSource createMqttSource();

	/**
	 * Returns the package supported by this factory.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the package supported by this factory.
	 * @generated
	 */
	CloudEventsPackage getCloudEventsPackage();

} //CloudEventsFactory
