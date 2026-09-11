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

import org.eclipse.emf.ecore.EFactory;

import org.osgi.annotation.versioning.ProviderType;

/**
 * <!-- begin-user-doc -->
 * The <b>Factory</b> for the model.
 * It provides a create method for each non-abstract class of the model.
 * <!-- end-user-doc -->
 * @see org.eclipse.fennec.event.atlas.model.deployment.DeploymentPackage
 * @generated
 */
@ProviderType
public interface DeploymentFactory extends EFactory {
	/**
	 * The singleton instance of the factory.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	DeploymentFactory eINSTANCE = org.eclipse.fennec.event.atlas.model.deployment.impl.DeploymentFactoryImpl.init();

	/**
	 * Returns a new object of class '<em>Event Atlas Deployment</em>'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return a new object of class '<em>Event Atlas Deployment</em>'.
	 * @generated
	 */
	EventAtlasDeployment createEventAtlasDeployment();

	/**
	 * Returns a new object of class '<em>Http Endpoint</em>'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return a new object of class '<em>Http Endpoint</em>'.
	 * @generated
	 */
	HttpEndpoint createHttpEndpoint();

	/**
	 * Returns a new object of class '<em>Atlas Binding</em>'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return a new object of class '<em>Atlas Binding</em>'.
	 * @generated
	 */
	AtlasBinding createAtlasBinding();

	/**
	 * Returns a new object of class '<em>Mqtt Broker</em>'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return a new object of class '<em>Mqtt Broker</em>'.
	 * @generated
	 */
	MqttBroker createMqttBroker();

	/**
	 * Returns a new object of class '<em>Ingest Channel</em>'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return a new object of class '<em>Ingest Channel</em>'.
	 * @generated
	 */
	IngestChannel createIngestChannel();

	/**
	 * Returns a new object of class '<em>History Config</em>'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return a new object of class '<em>History Config</em>'.
	 * @generated
	 */
	HistoryConfig createHistoryConfig();

	/**
	 * Returns a new object of class '<em>Timescale Storage</em>'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return a new object of class '<em>Timescale Storage</em>'.
	 * @generated
	 */
	TimescaleStorage createTimescaleStorage();

	/**
	 * Returns a new object of class '<em>In Memory Storage</em>'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return a new object of class '<em>In Memory Storage</em>'.
	 * @generated
	 */
	InMemoryStorage createInMemoryStorage();

	/**
	 * Returns a new object of class '<em>Historization Filter</em>'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return a new object of class '<em>Historization Filter</em>'.
	 * @generated
	 */
	HistorizationFilter createHistorizationFilter();

	/**
	 * Returns a new object of class '<em>Housekeeping Policy</em>'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return a new object of class '<em>Housekeeping Policy</em>'.
	 * @generated
	 */
	HousekeepingPolicy createHousekeepingPolicy();

	/**
	 * Returns a new object of class '<em>Inference Config</em>'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return a new object of class '<em>Inference Config</em>'.
	 * @generated
	 */
	InferenceConfig createInferenceConfig();

	/**
	 * Returns the package supported by this factory.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the package supported by this factory.
	 * @generated
	 */
	DeploymentPackage getDeploymentPackage();

} //DeploymentFactory
