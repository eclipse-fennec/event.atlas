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

import org.eclipse.emf.common.util.EList;

import org.eclipse.emf.ecore.EObject;

import org.osgi.annotation.versioning.ProviderType;

/**
 * <!-- begin-user-doc -->
 * A representation of the model object '<em><b>Event Atlas Deployment</b></em>'.
 * <!-- end-user-doc -->
 *
 * <!-- begin-model-doc -->
 * One event.atlas runtime's deployment configuration: the root of a deployment XMI.
 * 
 * Every section is optional and a section that is ABSENT is not written at all, so a deployment can adopt the model one concern at a time - whatever it leaves out keeps coming from the configurator JSON and its $[env:...] placeholders. Exactly one writer owns each ConfigAdmin PID.
 * <!-- end-model-doc -->
 *
 * <p>
 * The following features are supported:
 * </p>
 * <ul>
 *   <li>{@link org.eclipse.fennec.event.atlas.model.deployment.EventAtlasDeployment#getDeploymentId <em>Deployment Id</em>}</li>
 *   <li>{@link org.eclipse.fennec.event.atlas.model.deployment.EventAtlasDeployment#getDescription <em>Description</em>}</li>
 *   <li>{@link org.eclipse.fennec.event.atlas.model.deployment.EventAtlasDeployment#getHttp <em>Http</em>}</li>
 *   <li>{@link org.eclipse.fennec.event.atlas.model.deployment.EventAtlasDeployment#getAtlas <em>Atlas</em>}</li>
 *   <li>{@link org.eclipse.fennec.event.atlas.model.deployment.EventAtlasDeployment#getBrokers <em>Brokers</em>}</li>
 *   <li>{@link org.eclipse.fennec.event.atlas.model.deployment.EventAtlasDeployment#getChannels <em>Channels</em>}</li>
 *   <li>{@link org.eclipse.fennec.event.atlas.model.deployment.EventAtlasDeployment#getHistory <em>History</em>}</li>
 *   <li>{@link org.eclipse.fennec.event.atlas.model.deployment.EventAtlasDeployment#getInference <em>Inference</em>}</li>
 * </ul>
 *
 * @see org.eclipse.fennec.event.atlas.model.deployment.DeploymentPackage#getEventAtlasDeployment()
 * @model
 * @generated
 */
@ProviderType
public interface EventAtlasDeployment extends EObject {
	/**
	 * Returns the value of the '<em><b>Deployment Id</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * <!-- begin-model-doc -->
	 * Unique identifier of this deployment, and its key in the EObject registry.
	 * <!-- end-model-doc -->
	 * @return the value of the '<em>Deployment Id</em>' attribute.
	 * @see #setDeploymentId(String)
	 * @see org.eclipse.fennec.event.atlas.model.deployment.DeploymentPackage#getEventAtlasDeployment_DeploymentId()
	 * @model id="true" required="true"
	 * @generated
	 */
	String getDeploymentId();

	/**
	 * Sets the value of the '{@link org.eclipse.fennec.event.atlas.model.deployment.EventAtlasDeployment#getDeploymentId <em>Deployment Id</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @param value the new value of the '<em>Deployment Id</em>' attribute.
	 * @see #getDeploymentId()
	 * @generated
	 */
	void setDeploymentId(String value);

	/**
	 * Returns the value of the '<em><b>Description</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * <!-- begin-model-doc -->
	 * What this deployment is for. Documentation only.
	 * <!-- end-model-doc -->
	 * @return the value of the '<em>Description</em>' attribute.
	 * @see #setDescription(String)
	 * @see org.eclipse.fennec.event.atlas.model.deployment.DeploymentPackage#getEventAtlasDeployment_Description()
	 * @model
	 * @generated
	 */
	String getDescription();

	/**
	 * Sets the value of the '{@link org.eclipse.fennec.event.atlas.model.deployment.EventAtlasDeployment#getDescription <em>Description</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @param value the new value of the '<em>Description</em>' attribute.
	 * @see #getDescription()
	 * @generated
	 */
	void setDescription(String value);

	/**
	 * Returns the value of the '<em><b>Http</b></em>' containment reference.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * <!-- begin-model-doc -->
	 * The named Felix HTTP whiteboard and the Jersey whiteboard on it.
	 * <!-- end-model-doc -->
	 * @return the value of the '<em>Http</em>' containment reference.
	 * @see #setHttp(HttpEndpoint)
	 * @see org.eclipse.fennec.event.atlas.model.deployment.DeploymentPackage#getEventAtlasDeployment_Http()
	 * @model containment="true"
	 * @generated
	 */
	HttpEndpoint getHttp();

	/**
	 * Sets the value of the '{@link org.eclipse.fennec.event.atlas.model.deployment.EventAtlasDeployment#getHttp <em>Http</em>}' containment reference.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @param value the new value of the '<em>Http</em>' containment reference.
	 * @see #getHttp()
	 * @generated
	 */
	void setHttp(HttpEndpoint value);

	/**
	 * Returns the value of the '<em><b>Atlas</b></em>' containment reference.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * <!-- begin-model-doc -->
	 * The Model Atlas this runtime reads mappings and domain EPackages from.
	 * <!-- end-model-doc -->
	 * @return the value of the '<em>Atlas</em>' containment reference.
	 * @see #setAtlas(AtlasBinding)
	 * @see org.eclipse.fennec.event.atlas.model.deployment.DeploymentPackage#getEventAtlasDeployment_Atlas()
	 * @model containment="true"
	 * @generated
	 */
	AtlasBinding getAtlas();

	/**
	 * Sets the value of the '{@link org.eclipse.fennec.event.atlas.model.deployment.EventAtlasDeployment#getAtlas <em>Atlas</em>}' containment reference.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @param value the new value of the '<em>Atlas</em>' containment reference.
	 * @see #getAtlas()
	 * @generated
	 */
	void setAtlas(AtlasBinding value);

	/**
	 * Returns the value of the '<em><b>Brokers</b></em>' containment reference list.
	 * The list contents are of type {@link org.eclipse.fennec.event.atlas.model.deployment.MqttBroker}.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * <!-- begin-model-doc -->
	 * The southbound MQTT brokers this runtime CONNECTS TO - not the SensorThings broker it hosts.
	 * <!-- end-model-doc -->
	 * @return the value of the '<em>Brokers</em>' containment reference list.
	 * @see org.eclipse.fennec.event.atlas.model.deployment.DeploymentPackage#getEventAtlasDeployment_Brokers()
	 * @model containment="true"
	 * @generated
	 */
	EList<MqttBroker> getBrokers();

	/**
	 * Returns the value of the '<em><b>Channels</b></em>' containment reference list.
	 * The list contents are of type {@link org.eclipse.fennec.event.atlas.model.deployment.IngestChannel}.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * <!-- begin-model-doc -->
	 * The southbound ingest channels payloads arrive on.
	 * <!-- end-model-doc -->
	 * @return the value of the '<em>Channels</em>' containment reference list.
	 * @see org.eclipse.fennec.event.atlas.model.deployment.DeploymentPackage#getEventAtlasDeployment_Channels()
	 * @model containment="true"
	 * @generated
	 */
	EList<IngestChannel> getChannels();

	/**
	 * Returns the value of the '<em><b>History</b></em>' containment reference.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * <!-- begin-model-doc -->
	 * The history store, its historization filters and its housekeeping policies.
	 * <!-- end-model-doc -->
	 * @return the value of the '<em>History</em>' containment reference.
	 * @see #setHistory(HistoryConfig)
	 * @see org.eclipse.fennec.event.atlas.model.deployment.DeploymentPackage#getEventAtlasDeployment_History()
	 * @model containment="true"
	 * @generated
	 */
	HistoryConfig getHistory();

	/**
	 * Sets the value of the '{@link org.eclipse.fennec.event.atlas.model.deployment.EventAtlasDeployment#getHistory <em>History</em>}' containment reference.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @param value the new value of the '<em>History</em>' containment reference.
	 * @see #getHistory()
	 * @generated
	 */
	void setHistory(HistoryConfig value);

	/**
	 * Returns the value of the '<em><b>Inference</b></em>' containment reference.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * <!-- begin-model-doc -->
	 * Optional model inference for payloads the runtime has no model for.
	 * <!-- end-model-doc -->
	 * @return the value of the '<em>Inference</em>' containment reference.
	 * @see #setInference(InferenceConfig)
	 * @see org.eclipse.fennec.event.atlas.model.deployment.DeploymentPackage#getEventAtlasDeployment_Inference()
	 * @model containment="true"
	 * @generated
	 */
	InferenceConfig getInference();

	/**
	 * Sets the value of the '{@link org.eclipse.fennec.event.atlas.model.deployment.EventAtlasDeployment#getInference <em>Inference</em>}' containment reference.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @param value the new value of the '<em>Inference</em>' containment reference.
	 * @see #getInference()
	 * @generated
	 */
	void setInference(InferenceConfig value);

} // EventAtlasDeployment
