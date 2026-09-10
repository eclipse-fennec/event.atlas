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
 * A representation of the model object '<em><b>Mqtt Broker</b></em>'.
 * <!-- end-user-doc -->
 *
 * <!-- begin-model-doc -->
 * A southbound MQTT broker connection. The sensinact client is the transport only - connection plus subscription; mapping and ingest happen in event.atlas's own listener, which binds this broker by its id.
 * 
 * 'topics' is what is SUBSCRIBED at the broker. Which of those messages each channel handles is a separate per-channel filter, and the union of the channel filters should cover this list: a topic that is subscribed but matches no channel is received and silently dropped.
 * <!-- end-model-doc -->
 *
 * <p>
 * The following features are supported:
 * </p>
 * <ul>
 *   <li>{@link org.eclipse.fennec.event.atlas.model.deployment.MqttBroker#getId <em>Id</em>}</li>
 *   <li>{@link org.eclipse.fennec.event.atlas.model.deployment.MqttBroker#getProtocol <em>Protocol</em>}</li>
 *   <li>{@link org.eclipse.fennec.event.atlas.model.deployment.MqttBroker#getHost <em>Host</em>}</li>
 *   <li>{@link org.eclipse.fennec.event.atlas.model.deployment.MqttBroker#getPort <em>Port</em>}</li>
 *   <li>{@link org.eclipse.fennec.event.atlas.model.deployment.MqttBroker#getUser <em>User</em>}</li>
 *   <li>{@link org.eclipse.fennec.event.atlas.model.deployment.MqttBroker#getPassword <em>Password</em>}</li>
 *   <li>{@link org.eclipse.fennec.event.atlas.model.deployment.MqttBroker#getTopics <em>Topics</em>}</li>
 * </ul>
 *
 * @see org.eclipse.fennec.event.atlas.model.deployment.DeploymentPackage#getMqttBroker()
 * @model
 * @generated
 */
@ProviderType
public interface MqttBroker extends EObject {
	/**
	 * Returns the value of the '<em><b>Id</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * <!-- begin-model-doc -->
	 * Handler id an ingest channel references through brokerId.
	 * <!-- end-model-doc -->
	 * @return the value of the '<em>Id</em>' attribute.
	 * @see #setId(String)
	 * @see org.eclipse.fennec.event.atlas.model.deployment.DeploymentPackage#getMqttBroker_Id()
	 * @model required="true"
	 * @generated
	 */
	String getId();

	/**
	 * Sets the value of the '{@link org.eclipse.fennec.event.atlas.model.deployment.MqttBroker#getId <em>Id</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @param value the new value of the '<em>Id</em>' attribute.
	 * @see #getId()
	 * @generated
	 */
	void setId(String value);

	/**
	 * Returns the value of the '<em><b>Protocol</b></em>' attribute.
	 * The default value is <code>"tcp"</code>.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the value of the '<em>Protocol</em>' attribute.
	 * @see #setProtocol(String)
	 * @see org.eclipse.fennec.event.atlas.model.deployment.DeploymentPackage#getMqttBroker_Protocol()
	 * @model default="tcp"
	 * @generated
	 */
	String getProtocol();

	/**
	 * Sets the value of the '{@link org.eclipse.fennec.event.atlas.model.deployment.MqttBroker#getProtocol <em>Protocol</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @param value the new value of the '<em>Protocol</em>' attribute.
	 * @see #getProtocol()
	 * @generated
	 */
	void setProtocol(String value);

	/**
	 * Returns the value of the '<em><b>Host</b></em>' attribute.
	 * The default value is <code>"localhost"</code>.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the value of the '<em>Host</em>' attribute.
	 * @see #setHost(String)
	 * @see org.eclipse.fennec.event.atlas.model.deployment.DeploymentPackage#getMqttBroker_Host()
	 * @model default="localhost"
	 * @generated
	 */
	String getHost();

	/**
	 * Sets the value of the '{@link org.eclipse.fennec.event.atlas.model.deployment.MqttBroker#getHost <em>Host</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @param value the new value of the '<em>Host</em>' attribute.
	 * @see #getHost()
	 * @generated
	 */
	void setHost(String value);

	/**
	 * Returns the value of the '<em><b>Port</b></em>' attribute.
	 * The default value is <code>"1883"</code>.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the value of the '<em>Port</em>' attribute.
	 * @see #setPort(int)
	 * @see org.eclipse.fennec.event.atlas.model.deployment.DeploymentPackage#getMqttBroker_Port()
	 * @model default="1883"
	 * @generated
	 */
	int getPort();

	/**
	 * Sets the value of the '{@link org.eclipse.fennec.event.atlas.model.deployment.MqttBroker#getPort <em>Port</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @param value the new value of the '<em>Port</em>' attribute.
	 * @see #getPort()
	 * @generated
	 */
	void setPort(int value);

	/**
	 * Returns the value of the '<em><b>User</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the value of the '<em>User</em>' attribute.
	 * @see #setUser(String)
	 * @see org.eclipse.fennec.event.atlas.model.deployment.DeploymentPackage#getMqttBroker_User()
	 * @model
	 * @generated
	 */
	String getUser();

	/**
	 * Sets the value of the '{@link org.eclipse.fennec.event.atlas.model.deployment.MqttBroker#getUser <em>User</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @param value the new value of the '<em>User</em>' attribute.
	 * @see #getUser()
	 * @generated
	 */
	void setUser(String value);

	/**
	 * Returns the value of the '<em><b>Password</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * <!-- begin-model-doc -->
	 * Written to the private ConfigAdmin property '.password', so it is not readable back through the configuration API. Prefer leaving it blank here and supplying it through the environment.
	 * <!-- end-model-doc -->
	 * @return the value of the '<em>Password</em>' attribute.
	 * @see #setPassword(String)
	 * @see org.eclipse.fennec.event.atlas.model.deployment.DeploymentPackage#getMqttBroker_Password()
	 * @model
	 * @generated
	 */
	String getPassword();

	/**
	 * Sets the value of the '{@link org.eclipse.fennec.event.atlas.model.deployment.MqttBroker#getPassword <em>Password</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @param value the new value of the '<em>Password</em>' attribute.
	 * @see #getPassword()
	 * @generated
	 */
	void setPassword(String value);

	/**
	 * Returns the value of the '<em><b>Topics</b></em>' attribute list.
	 * The list contents are of type {@link java.lang.String}.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the value of the '<em>Topics</em>' attribute list.
	 * @see org.eclipse.fennec.event.atlas.model.deployment.DeploymentPackage#getMqttBroker_Topics()
	 * @model
	 * @generated
	 */
	EList<String> getTopics();

} // MqttBroker
