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
 * A representation of the model object '<em><b>Ingest Channel</b></em>'.
 * <!-- end-user-doc -->
 *
 * <!-- begin-model-doc -->
 * One southbound ingest channel: a transport, a payload format and the topics or path it accepts.
 * <!-- end-model-doc -->
 *
 * <p>
 * The following features are supported:
 * </p>
 * <ul>
 *   <li>{@link org.eclipse.fennec.event.atlas.model.deployment.IngestChannel#getName <em>Name</em>}</li>
 *   <li>{@link org.eclipse.fennec.event.atlas.model.deployment.IngestChannel#getTransport <em>Transport</em>}</li>
 *   <li>{@link org.eclipse.fennec.event.atlas.model.deployment.IngestChannel#getFormat <em>Format</em>}</li>
 *   <li>{@link org.eclipse.fennec.event.atlas.model.deployment.IngestChannel#getBrokerId <em>Broker Id</em>}</li>
 *   <li>{@link org.eclipse.fennec.event.atlas.model.deployment.IngestChannel#getTopics <em>Topics</em>}</li>
 *   <li>{@link org.eclipse.fennec.event.atlas.model.deployment.IngestChannel#getCodecTypeMapId <em>Codec Type Map Id</em>}</li>
 * </ul>
 *
 * @see org.eclipse.fennec.event.atlas.model.deployment.DeploymentPackage#getIngestChannel()
 * @model
 * @generated
 */
@ProviderType
public interface IngestChannel extends EObject {
	/**
	 * Returns the value of the '<em><b>Name</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * <!-- begin-model-doc -->
	 * Channel name. Also the factory configuration's instance name, so it must be unique.
	 * <!-- end-model-doc -->
	 * @return the value of the '<em>Name</em>' attribute.
	 * @see #setName(String)
	 * @see org.eclipse.fennec.event.atlas.model.deployment.DeploymentPackage#getIngestChannel_Name()
	 * @model required="true"
	 * @generated
	 */
	String getName();

	/**
	 * Sets the value of the '{@link org.eclipse.fennec.event.atlas.model.deployment.IngestChannel#getName <em>Name</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @param value the new value of the '<em>Name</em>' attribute.
	 * @see #getName()
	 * @generated
	 */
	void setName(String value);

	/**
	 * Returns the value of the '<em><b>Transport</b></em>' attribute.
	 * The default value is <code>"MQTT"</code>.
	 * The literals are from the enumeration {@link org.eclipse.fennec.event.atlas.model.deployment.ChannelTransport}.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the value of the '<em>Transport</em>' attribute.
	 * @see org.eclipse.fennec.event.atlas.model.deployment.ChannelTransport
	 * @see #setTransport(ChannelTransport)
	 * @see org.eclipse.fennec.event.atlas.model.deployment.DeploymentPackage#getIngestChannel_Transport()
	 * @model default="MQTT"
	 * @generated
	 */
	ChannelTransport getTransport();

	/**
	 * Sets the value of the '{@link org.eclipse.fennec.event.atlas.model.deployment.IngestChannel#getTransport <em>Transport</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @param value the new value of the '<em>Transport</em>' attribute.
	 * @see org.eclipse.fennec.event.atlas.model.deployment.ChannelTransport
	 * @see #getTransport()
	 * @generated
	 */
	void setTransport(ChannelTransport value);

	/**
	 * Returns the value of the '<em><b>Format</b></em>' attribute.
	 * The default value is <code>"XMI"</code>.
	 * The literals are from the enumeration {@link org.eclipse.fennec.event.atlas.model.deployment.PayloadFormat}.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the value of the '<em>Format</em>' attribute.
	 * @see org.eclipse.fennec.event.atlas.model.deployment.PayloadFormat
	 * @see #setFormat(PayloadFormat)
	 * @see org.eclipse.fennec.event.atlas.model.deployment.DeploymentPackage#getIngestChannel_Format()
	 * @model default="XMI"
	 * @generated
	 */
	PayloadFormat getFormat();

	/**
	 * Sets the value of the '{@link org.eclipse.fennec.event.atlas.model.deployment.IngestChannel#getFormat <em>Format</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @param value the new value of the '<em>Format</em>' attribute.
	 * @see org.eclipse.fennec.event.atlas.model.deployment.PayloadFormat
	 * @see #getFormat()
	 * @generated
	 */
	void setFormat(PayloadFormat value);

	/**
	 * Returns the value of the '<em><b>Broker Id</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * <!-- begin-model-doc -->
	 * MQTT only: the id of the MqttBroker this channel listens on.
	 * <!-- end-model-doc -->
	 * @return the value of the '<em>Broker Id</em>' attribute.
	 * @see #setBrokerId(String)
	 * @see org.eclipse.fennec.event.atlas.model.deployment.DeploymentPackage#getIngestChannel_BrokerId()
	 * @model
	 * @generated
	 */
	String getBrokerId();

	/**
	 * Sets the value of the '{@link org.eclipse.fennec.event.atlas.model.deployment.IngestChannel#getBrokerId <em>Broker Id</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @param value the new value of the '<em>Broker Id</em>' attribute.
	 * @see #getBrokerId()
	 * @generated
	 */
	void setBrokerId(String value);

	/**
	 * Returns the value of the '<em><b>Topics</b></em>' attribute list.
	 * The list contents are of type {@link java.lang.String}.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * <!-- begin-model-doc -->
	 * MQTT only: the subset of the broker's subscribed topics this channel handles. A filter matching nothing is harmless; an EMPTY topic list is an activation failure by design.
	 * <!-- end-model-doc -->
	 * @return the value of the '<em>Topics</em>' attribute list.
	 * @see org.eclipse.fennec.event.atlas.model.deployment.DeploymentPackage#getIngestChannel_Topics()
	 * @model
	 * @generated
	 */
	EList<String> getTopics();

	/**
	 * Returns the value of the '<em><b>Codec Type Map Id</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * <!-- begin-model-doc -->
	 * Discriminator registry that types JSON payloads, which do not name their model. One registry per runtime, so the LAST non-blank value across all channels wins and is written once.
	 * 
	 * NOT OPTIONAL for a JSON deployment: an empty map id passes no load option, the deserializer finds no type information and every JSON payload is dropped.
	 * <!-- end-model-doc -->
	 * @return the value of the '<em>Codec Type Map Id</em>' attribute.
	 * @see #setCodecTypeMapId(String)
	 * @see org.eclipse.fennec.event.atlas.model.deployment.DeploymentPackage#getIngestChannel_CodecTypeMapId()
	 * @model
	 * @generated
	 */
	String getCodecTypeMapId();

	/**
	 * Sets the value of the '{@link org.eclipse.fennec.event.atlas.model.deployment.IngestChannel#getCodecTypeMapId <em>Codec Type Map Id</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @param value the new value of the '<em>Codec Type Map Id</em>' attribute.
	 * @see #getCodecTypeMapId()
	 * @generated
	 */
	void setCodecTypeMapId(String value);

} // IngestChannel
