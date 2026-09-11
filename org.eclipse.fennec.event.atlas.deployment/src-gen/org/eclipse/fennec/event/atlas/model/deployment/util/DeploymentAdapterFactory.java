/**
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
package org.eclipse.fennec.event.atlas.model.deployment.util;

import org.eclipse.emf.common.notify.Adapter;
import org.eclipse.emf.common.notify.Notifier;

import org.eclipse.emf.common.notify.impl.AdapterFactoryImpl;

import org.eclipse.emf.ecore.EObject;

import org.eclipse.fennec.event.atlas.model.deployment.*;

/**
 * <!-- begin-user-doc -->
 * The <b>Adapter Factory</b> for the model.
 * It provides an adapter <code>createXXX</code> method for each class of the model.
 * <!-- end-user-doc -->
 * @see org.eclipse.fennec.event.atlas.model.deployment.DeploymentPackage
 * @generated
 */
public class DeploymentAdapterFactory extends AdapterFactoryImpl {
	/**
	 * The cached model package.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	protected static DeploymentPackage modelPackage;

	/**
	 * Creates an instance of the adapter factory.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public DeploymentAdapterFactory() {
		if (modelPackage == null) {
			modelPackage = DeploymentPackage.eINSTANCE;
		}
	}

	/**
	 * Returns whether this factory is applicable for the type of the object.
	 * <!-- begin-user-doc -->
	 * This implementation returns <code>true</code> if the object is either the model's package or is an instance object of the model.
	 * <!-- end-user-doc -->
	 * @return whether this factory is applicable for the type of the object.
	 * @generated
	 */
	@Override
	public boolean isFactoryForType(Object object) {
		if (object == modelPackage) {
			return true;
		}
		if (object instanceof EObject) {
			return ((EObject)object).eClass().getEPackage() == modelPackage;
		}
		return false;
	}

	/**
	 * The switch that delegates to the <code>createXXX</code> methods.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	protected DeploymentSwitch<Adapter> modelSwitch =
		new DeploymentSwitch<Adapter>() {
			@Override
			public Adapter caseEventAtlasDeployment(EventAtlasDeployment object) {
				return createEventAtlasDeploymentAdapter();
			}
			@Override
			public Adapter caseHttpEndpoint(HttpEndpoint object) {
				return createHttpEndpointAdapter();
			}
			@Override
			public Adapter caseAtlasBinding(AtlasBinding object) {
				return createAtlasBindingAdapter();
			}
			@Override
			public Adapter caseMqttBroker(MqttBroker object) {
				return createMqttBrokerAdapter();
			}
			@Override
			public Adapter caseIngestChannel(IngestChannel object) {
				return createIngestChannelAdapter();
			}
			@Override
			public Adapter caseHistoryConfig(HistoryConfig object) {
				return createHistoryConfigAdapter();
			}
			@Override
			public Adapter caseHistoryStorage(HistoryStorage object) {
				return createHistoryStorageAdapter();
			}
			@Override
			public Adapter caseTimescaleStorage(TimescaleStorage object) {
				return createTimescaleStorageAdapter();
			}
			@Override
			public Adapter caseInMemoryStorage(InMemoryStorage object) {
				return createInMemoryStorageAdapter();
			}
			@Override
			public Adapter caseHistorizationFilter(HistorizationFilter object) {
				return createHistorizationFilterAdapter();
			}
			@Override
			public Adapter caseHousekeepingPolicy(HousekeepingPolicy object) {
				return createHousekeepingPolicyAdapter();
			}
			@Override
			public Adapter caseInferenceConfig(InferenceConfig object) {
				return createInferenceConfigAdapter();
			}
			@Override
			public Adapter defaultCase(EObject object) {
				return createEObjectAdapter();
			}
		};

	/**
	 * Creates an adapter for the <code>target</code>.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @param target the object to adapt.
	 * @return the adapter for the <code>target</code>.
	 * @generated
	 */
	@Override
	public Adapter createAdapter(Notifier target) {
		return modelSwitch.doSwitch((EObject)target);
	}


	/**
	 * Creates a new adapter for an object of class '{@link org.eclipse.fennec.event.atlas.model.deployment.EventAtlasDeployment <em>Event Atlas Deployment</em>}'.
	 * <!-- begin-user-doc -->
	 * This default implementation returns null so that we can easily ignore cases;
	 * it's useful to ignore a case when inheritance will catch all the cases anyway.
	 * <!-- end-user-doc -->
	 * @return the new adapter.
	 * @see org.eclipse.fennec.event.atlas.model.deployment.EventAtlasDeployment
	 * @generated
	 */
	public Adapter createEventAtlasDeploymentAdapter() {
		return null;
	}

	/**
	 * Creates a new adapter for an object of class '{@link org.eclipse.fennec.event.atlas.model.deployment.HttpEndpoint <em>Http Endpoint</em>}'.
	 * <!-- begin-user-doc -->
	 * This default implementation returns null so that we can easily ignore cases;
	 * it's useful to ignore a case when inheritance will catch all the cases anyway.
	 * <!-- end-user-doc -->
	 * @return the new adapter.
	 * @see org.eclipse.fennec.event.atlas.model.deployment.HttpEndpoint
	 * @generated
	 */
	public Adapter createHttpEndpointAdapter() {
		return null;
	}

	/**
	 * Creates a new adapter for an object of class '{@link org.eclipse.fennec.event.atlas.model.deployment.AtlasBinding <em>Atlas Binding</em>}'.
	 * <!-- begin-user-doc -->
	 * This default implementation returns null so that we can easily ignore cases;
	 * it's useful to ignore a case when inheritance will catch all the cases anyway.
	 * <!-- end-user-doc -->
	 * @return the new adapter.
	 * @see org.eclipse.fennec.event.atlas.model.deployment.AtlasBinding
	 * @generated
	 */
	public Adapter createAtlasBindingAdapter() {
		return null;
	}

	/**
	 * Creates a new adapter for an object of class '{@link org.eclipse.fennec.event.atlas.model.deployment.MqttBroker <em>Mqtt Broker</em>}'.
	 * <!-- begin-user-doc -->
	 * This default implementation returns null so that we can easily ignore cases;
	 * it's useful to ignore a case when inheritance will catch all the cases anyway.
	 * <!-- end-user-doc -->
	 * @return the new adapter.
	 * @see org.eclipse.fennec.event.atlas.model.deployment.MqttBroker
	 * @generated
	 */
	public Adapter createMqttBrokerAdapter() {
		return null;
	}

	/**
	 * Creates a new adapter for an object of class '{@link org.eclipse.fennec.event.atlas.model.deployment.IngestChannel <em>Ingest Channel</em>}'.
	 * <!-- begin-user-doc -->
	 * This default implementation returns null so that we can easily ignore cases;
	 * it's useful to ignore a case when inheritance will catch all the cases anyway.
	 * <!-- end-user-doc -->
	 * @return the new adapter.
	 * @see org.eclipse.fennec.event.atlas.model.deployment.IngestChannel
	 * @generated
	 */
	public Adapter createIngestChannelAdapter() {
		return null;
	}

	/**
	 * Creates a new adapter for an object of class '{@link org.eclipse.fennec.event.atlas.model.deployment.HistoryConfig <em>History Config</em>}'.
	 * <!-- begin-user-doc -->
	 * This default implementation returns null so that we can easily ignore cases;
	 * it's useful to ignore a case when inheritance will catch all the cases anyway.
	 * <!-- end-user-doc -->
	 * @return the new adapter.
	 * @see org.eclipse.fennec.event.atlas.model.deployment.HistoryConfig
	 * @generated
	 */
	public Adapter createHistoryConfigAdapter() {
		return null;
	}

	/**
	 * Creates a new adapter for an object of class '{@link org.eclipse.fennec.event.atlas.model.deployment.HistoryStorage <em>History Storage</em>}'.
	 * <!-- begin-user-doc -->
	 * This default implementation returns null so that we can easily ignore cases;
	 * it's useful to ignore a case when inheritance will catch all the cases anyway.
	 * <!-- end-user-doc -->
	 * @return the new adapter.
	 * @see org.eclipse.fennec.event.atlas.model.deployment.HistoryStorage
	 * @generated
	 */
	public Adapter createHistoryStorageAdapter() {
		return null;
	}

	/**
	 * Creates a new adapter for an object of class '{@link org.eclipse.fennec.event.atlas.model.deployment.TimescaleStorage <em>Timescale Storage</em>}'.
	 * <!-- begin-user-doc -->
	 * This default implementation returns null so that we can easily ignore cases;
	 * it's useful to ignore a case when inheritance will catch all the cases anyway.
	 * <!-- end-user-doc -->
	 * @return the new adapter.
	 * @see org.eclipse.fennec.event.atlas.model.deployment.TimescaleStorage
	 * @generated
	 */
	public Adapter createTimescaleStorageAdapter() {
		return null;
	}

	/**
	 * Creates a new adapter for an object of class '{@link org.eclipse.fennec.event.atlas.model.deployment.InMemoryStorage <em>In Memory Storage</em>}'.
	 * <!-- begin-user-doc -->
	 * This default implementation returns null so that we can easily ignore cases;
	 * it's useful to ignore a case when inheritance will catch all the cases anyway.
	 * <!-- end-user-doc -->
	 * @return the new adapter.
	 * @see org.eclipse.fennec.event.atlas.model.deployment.InMemoryStorage
	 * @generated
	 */
	public Adapter createInMemoryStorageAdapter() {
		return null;
	}

	/**
	 * Creates a new adapter for an object of class '{@link org.eclipse.fennec.event.atlas.model.deployment.HistorizationFilter <em>Historization Filter</em>}'.
	 * <!-- begin-user-doc -->
	 * This default implementation returns null so that we can easily ignore cases;
	 * it's useful to ignore a case when inheritance will catch all the cases anyway.
	 * <!-- end-user-doc -->
	 * @return the new adapter.
	 * @see org.eclipse.fennec.event.atlas.model.deployment.HistorizationFilter
	 * @generated
	 */
	public Adapter createHistorizationFilterAdapter() {
		return null;
	}

	/**
	 * Creates a new adapter for an object of class '{@link org.eclipse.fennec.event.atlas.model.deployment.HousekeepingPolicy <em>Housekeeping Policy</em>}'.
	 * <!-- begin-user-doc -->
	 * This default implementation returns null so that we can easily ignore cases;
	 * it's useful to ignore a case when inheritance will catch all the cases anyway.
	 * <!-- end-user-doc -->
	 * @return the new adapter.
	 * @see org.eclipse.fennec.event.atlas.model.deployment.HousekeepingPolicy
	 * @generated
	 */
	public Adapter createHousekeepingPolicyAdapter() {
		return null;
	}

	/**
	 * Creates a new adapter for an object of class '{@link org.eclipse.fennec.event.atlas.model.deployment.InferenceConfig <em>Inference Config</em>}'.
	 * <!-- begin-user-doc -->
	 * This default implementation returns null so that we can easily ignore cases;
	 * it's useful to ignore a case when inheritance will catch all the cases anyway.
	 * <!-- end-user-doc -->
	 * @return the new adapter.
	 * @see org.eclipse.fennec.event.atlas.model.deployment.InferenceConfig
	 * @generated
	 */
	public Adapter createInferenceConfigAdapter() {
		return null;
	}

	/**
	 * Creates a new adapter for the default case.
	 * <!-- begin-user-doc -->
	 * This default implementation returns null.
	 * <!-- end-user-doc -->
	 * @return the new adapter.
	 * @generated
	 */
	public Adapter createEObjectAdapter() {
		return null;
	}

} //DeploymentAdapterFactory
