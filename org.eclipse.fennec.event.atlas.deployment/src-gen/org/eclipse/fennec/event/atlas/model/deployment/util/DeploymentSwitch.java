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

import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.EPackage;

import org.eclipse.emf.ecore.util.Switch;

import org.eclipse.fennec.event.atlas.model.deployment.*;

/**
 * <!-- begin-user-doc -->
 * The <b>Switch</b> for the model's inheritance hierarchy.
 * It supports the call {@link #doSwitch(EObject) doSwitch(object)}
 * to invoke the <code>caseXXX</code> method for each class of the model,
 * starting with the actual class of the object
 * and proceeding up the inheritance hierarchy
 * until a non-null result is returned,
 * which is the result of the switch.
 * <!-- end-user-doc -->
 * @see org.eclipse.fennec.event.atlas.model.deployment.DeploymentPackage
 * @generated
 */
public class DeploymentSwitch<T> extends Switch<T> {
	/**
	 * The cached model package
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	protected static DeploymentPackage modelPackage;

	/**
	 * Creates an instance of the switch.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public DeploymentSwitch() {
		if (modelPackage == null) {
			modelPackage = DeploymentPackage.eINSTANCE;
		}
	}

	/**
	 * Checks whether this is a switch for the given package.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @param ePackage the package in question.
	 * @return whether this is a switch for the given package.
	 * @generated
	 */
	@Override
	protected boolean isSwitchFor(EPackage ePackage) {
		return ePackage == modelPackage;
	}

	/**
	 * Calls <code>caseXXX</code> for each class of the model until one returns a non null result; it yields that result.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the first non-null result returned by a <code>caseXXX</code> call.
	 * @generated
	 */
	@Override
	protected T doSwitch(int classifierID, EObject theEObject) {
		switch (classifierID) {
			case DeploymentPackage.EVENT_ATLAS_DEPLOYMENT: {
				EventAtlasDeployment eventAtlasDeployment = (EventAtlasDeployment)theEObject;
				T result = caseEventAtlasDeployment(eventAtlasDeployment);
				if (result == null) result = defaultCase(theEObject);
				return result;
			}
			case DeploymentPackage.HTTP_ENDPOINT: {
				HttpEndpoint httpEndpoint = (HttpEndpoint)theEObject;
				T result = caseHttpEndpoint(httpEndpoint);
				if (result == null) result = defaultCase(theEObject);
				return result;
			}
			case DeploymentPackage.ATLAS_BINDING: {
				AtlasBinding atlasBinding = (AtlasBinding)theEObject;
				T result = caseAtlasBinding(atlasBinding);
				if (result == null) result = defaultCase(theEObject);
				return result;
			}
			case DeploymentPackage.MQTT_BROKER: {
				MqttBroker mqttBroker = (MqttBroker)theEObject;
				T result = caseMqttBroker(mqttBroker);
				if (result == null) result = defaultCase(theEObject);
				return result;
			}
			case DeploymentPackage.INGEST_CHANNEL: {
				IngestChannel ingestChannel = (IngestChannel)theEObject;
				T result = caseIngestChannel(ingestChannel);
				if (result == null) result = defaultCase(theEObject);
				return result;
			}
			case DeploymentPackage.HISTORY_CONFIG: {
				HistoryConfig historyConfig = (HistoryConfig)theEObject;
				T result = caseHistoryConfig(historyConfig);
				if (result == null) result = defaultCase(theEObject);
				return result;
			}
			case DeploymentPackage.HISTORY_STORAGE: {
				HistoryStorage historyStorage = (HistoryStorage)theEObject;
				T result = caseHistoryStorage(historyStorage);
				if (result == null) result = defaultCase(theEObject);
				return result;
			}
			case DeploymentPackage.TIMESCALE_STORAGE: {
				TimescaleStorage timescaleStorage = (TimescaleStorage)theEObject;
				T result = caseTimescaleStorage(timescaleStorage);
				if (result == null) result = caseHistoryStorage(timescaleStorage);
				if (result == null) result = defaultCase(theEObject);
				return result;
			}
			case DeploymentPackage.IN_MEMORY_STORAGE: {
				InMemoryStorage inMemoryStorage = (InMemoryStorage)theEObject;
				T result = caseInMemoryStorage(inMemoryStorage);
				if (result == null) result = caseHistoryStorage(inMemoryStorage);
				if (result == null) result = defaultCase(theEObject);
				return result;
			}
			case DeploymentPackage.HISTORIZATION_FILTER: {
				HistorizationFilter historizationFilter = (HistorizationFilter)theEObject;
				T result = caseHistorizationFilter(historizationFilter);
				if (result == null) result = defaultCase(theEObject);
				return result;
			}
			case DeploymentPackage.HOUSEKEEPING_POLICY: {
				HousekeepingPolicy housekeepingPolicy = (HousekeepingPolicy)theEObject;
				T result = caseHousekeepingPolicy(housekeepingPolicy);
				if (result == null) result = defaultCase(theEObject);
				return result;
			}
			case DeploymentPackage.INFERENCE_CONFIG: {
				InferenceConfig inferenceConfig = (InferenceConfig)theEObject;
				T result = caseInferenceConfig(inferenceConfig);
				if (result == null) result = defaultCase(theEObject);
				return result;
			}
			default: return defaultCase(theEObject);
		}
	}

	/**
	 * Returns the result of interpreting the object as an instance of '<em>Event Atlas Deployment</em>'.
	 * <!-- begin-user-doc -->
	 * This implementation returns null;
	 * returning a non-null result will terminate the switch.
	 * <!-- end-user-doc -->
	 * @param object the target of the switch.
	 * @return the result of interpreting the object as an instance of '<em>Event Atlas Deployment</em>'.
	 * @see #doSwitch(org.eclipse.emf.ecore.EObject) doSwitch(EObject)
	 * @generated
	 */
	public T caseEventAtlasDeployment(EventAtlasDeployment object) {
		return null;
	}

	/**
	 * Returns the result of interpreting the object as an instance of '<em>Http Endpoint</em>'.
	 * <!-- begin-user-doc -->
	 * This implementation returns null;
	 * returning a non-null result will terminate the switch.
	 * <!-- end-user-doc -->
	 * @param object the target of the switch.
	 * @return the result of interpreting the object as an instance of '<em>Http Endpoint</em>'.
	 * @see #doSwitch(org.eclipse.emf.ecore.EObject) doSwitch(EObject)
	 * @generated
	 */
	public T caseHttpEndpoint(HttpEndpoint object) {
		return null;
	}

	/**
	 * Returns the result of interpreting the object as an instance of '<em>Atlas Binding</em>'.
	 * <!-- begin-user-doc -->
	 * This implementation returns null;
	 * returning a non-null result will terminate the switch.
	 * <!-- end-user-doc -->
	 * @param object the target of the switch.
	 * @return the result of interpreting the object as an instance of '<em>Atlas Binding</em>'.
	 * @see #doSwitch(org.eclipse.emf.ecore.EObject) doSwitch(EObject)
	 * @generated
	 */
	public T caseAtlasBinding(AtlasBinding object) {
		return null;
	}

	/**
	 * Returns the result of interpreting the object as an instance of '<em>Mqtt Broker</em>'.
	 * <!-- begin-user-doc -->
	 * This implementation returns null;
	 * returning a non-null result will terminate the switch.
	 * <!-- end-user-doc -->
	 * @param object the target of the switch.
	 * @return the result of interpreting the object as an instance of '<em>Mqtt Broker</em>'.
	 * @see #doSwitch(org.eclipse.emf.ecore.EObject) doSwitch(EObject)
	 * @generated
	 */
	public T caseMqttBroker(MqttBroker object) {
		return null;
	}

	/**
	 * Returns the result of interpreting the object as an instance of '<em>Ingest Channel</em>'.
	 * <!-- begin-user-doc -->
	 * This implementation returns null;
	 * returning a non-null result will terminate the switch.
	 * <!-- end-user-doc -->
	 * @param object the target of the switch.
	 * @return the result of interpreting the object as an instance of '<em>Ingest Channel</em>'.
	 * @see #doSwitch(org.eclipse.emf.ecore.EObject) doSwitch(EObject)
	 * @generated
	 */
	public T caseIngestChannel(IngestChannel object) {
		return null;
	}

	/**
	 * Returns the result of interpreting the object as an instance of '<em>History Config</em>'.
	 * <!-- begin-user-doc -->
	 * This implementation returns null;
	 * returning a non-null result will terminate the switch.
	 * <!-- end-user-doc -->
	 * @param object the target of the switch.
	 * @return the result of interpreting the object as an instance of '<em>History Config</em>'.
	 * @see #doSwitch(org.eclipse.emf.ecore.EObject) doSwitch(EObject)
	 * @generated
	 */
	public T caseHistoryConfig(HistoryConfig object) {
		return null;
	}

	/**
	 * Returns the result of interpreting the object as an instance of '<em>History Storage</em>'.
	 * <!-- begin-user-doc -->
	 * This implementation returns null;
	 * returning a non-null result will terminate the switch.
	 * <!-- end-user-doc -->
	 * @param object the target of the switch.
	 * @return the result of interpreting the object as an instance of '<em>History Storage</em>'.
	 * @see #doSwitch(org.eclipse.emf.ecore.EObject) doSwitch(EObject)
	 * @generated
	 */
	public T caseHistoryStorage(HistoryStorage object) {
		return null;
	}

	/**
	 * Returns the result of interpreting the object as an instance of '<em>Timescale Storage</em>'.
	 * <!-- begin-user-doc -->
	 * This implementation returns null;
	 * returning a non-null result will terminate the switch.
	 * <!-- end-user-doc -->
	 * @param object the target of the switch.
	 * @return the result of interpreting the object as an instance of '<em>Timescale Storage</em>'.
	 * @see #doSwitch(org.eclipse.emf.ecore.EObject) doSwitch(EObject)
	 * @generated
	 */
	public T caseTimescaleStorage(TimescaleStorage object) {
		return null;
	}

	/**
	 * Returns the result of interpreting the object as an instance of '<em>In Memory Storage</em>'.
	 * <!-- begin-user-doc -->
	 * This implementation returns null;
	 * returning a non-null result will terminate the switch.
	 * <!-- end-user-doc -->
	 * @param object the target of the switch.
	 * @return the result of interpreting the object as an instance of '<em>In Memory Storage</em>'.
	 * @see #doSwitch(org.eclipse.emf.ecore.EObject) doSwitch(EObject)
	 * @generated
	 */
	public T caseInMemoryStorage(InMemoryStorage object) {
		return null;
	}

	/**
	 * Returns the result of interpreting the object as an instance of '<em>Historization Filter</em>'.
	 * <!-- begin-user-doc -->
	 * This implementation returns null;
	 * returning a non-null result will terminate the switch.
	 * <!-- end-user-doc -->
	 * @param object the target of the switch.
	 * @return the result of interpreting the object as an instance of '<em>Historization Filter</em>'.
	 * @see #doSwitch(org.eclipse.emf.ecore.EObject) doSwitch(EObject)
	 * @generated
	 */
	public T caseHistorizationFilter(HistorizationFilter object) {
		return null;
	}

	/**
	 * Returns the result of interpreting the object as an instance of '<em>Housekeeping Policy</em>'.
	 * <!-- begin-user-doc -->
	 * This implementation returns null;
	 * returning a non-null result will terminate the switch.
	 * <!-- end-user-doc -->
	 * @param object the target of the switch.
	 * @return the result of interpreting the object as an instance of '<em>Housekeeping Policy</em>'.
	 * @see #doSwitch(org.eclipse.emf.ecore.EObject) doSwitch(EObject)
	 * @generated
	 */
	public T caseHousekeepingPolicy(HousekeepingPolicy object) {
		return null;
	}

	/**
	 * Returns the result of interpreting the object as an instance of '<em>Inference Config</em>'.
	 * <!-- begin-user-doc -->
	 * This implementation returns null;
	 * returning a non-null result will terminate the switch.
	 * <!-- end-user-doc -->
	 * @param object the target of the switch.
	 * @return the result of interpreting the object as an instance of '<em>Inference Config</em>'.
	 * @see #doSwitch(org.eclipse.emf.ecore.EObject) doSwitch(EObject)
	 * @generated
	 */
	public T caseInferenceConfig(InferenceConfig object) {
		return null;
	}

	/**
	 * Returns the result of interpreting the object as an instance of '<em>EObject</em>'.
	 * <!-- begin-user-doc -->
	 * This implementation returns null;
	 * returning a non-null result will terminate the switch, but this is the last case anyway.
	 * <!-- end-user-doc -->
	 * @param object the target of the switch.
	 * @return the result of interpreting the object as an instance of '<em>EObject</em>'.
	 * @see #doSwitch(org.eclipse.emf.ecore.EObject)
	 * @generated
	 */
	@Override
	public T defaultCase(EObject object) {
		return null;
	}

} //DeploymentSwitch
