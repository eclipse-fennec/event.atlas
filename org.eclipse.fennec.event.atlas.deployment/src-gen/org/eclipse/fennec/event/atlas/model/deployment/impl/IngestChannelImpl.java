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
package org.eclipse.fennec.event.atlas.model.deployment.impl;

import java.util.Collection;

import org.eclipse.emf.common.notify.Notification;

import org.eclipse.emf.common.util.EList;

import org.eclipse.emf.ecore.EClass;

import org.eclipse.emf.ecore.impl.ENotificationImpl;
import org.eclipse.emf.ecore.impl.MinimalEObjectImpl;

import org.eclipse.emf.ecore.util.EDataTypeUniqueEList;

import org.eclipse.fennec.event.atlas.model.deployment.ChannelTransport;
import org.eclipse.fennec.event.atlas.model.deployment.DeploymentPackage;
import org.eclipse.fennec.event.atlas.model.deployment.IngestChannel;
import org.eclipse.fennec.event.atlas.model.deployment.PayloadFormat;

/**
 * <!-- begin-user-doc -->
 * An implementation of the model object '<em><b>Ingest Channel</b></em>'.
 * <!-- end-user-doc -->
 * <p>
 * The following features are implemented:
 * </p>
 * <ul>
 *   <li>{@link org.eclipse.fennec.event.atlas.model.deployment.impl.IngestChannelImpl#getName <em>Name</em>}</li>
 *   <li>{@link org.eclipse.fennec.event.atlas.model.deployment.impl.IngestChannelImpl#getTransport <em>Transport</em>}</li>
 *   <li>{@link org.eclipse.fennec.event.atlas.model.deployment.impl.IngestChannelImpl#getFormat <em>Format</em>}</li>
 *   <li>{@link org.eclipse.fennec.event.atlas.model.deployment.impl.IngestChannelImpl#getBrokerId <em>Broker Id</em>}</li>
 *   <li>{@link org.eclipse.fennec.event.atlas.model.deployment.impl.IngestChannelImpl#getTopics <em>Topics</em>}</li>
 *   <li>{@link org.eclipse.fennec.event.atlas.model.deployment.impl.IngestChannelImpl#getCodecTypeMapId <em>Codec Type Map Id</em>}</li>
 * </ul>
 *
 * @generated
 */
public class IngestChannelImpl extends MinimalEObjectImpl.Container implements IngestChannel {
	/**
	 * The default value of the '{@link #getName() <em>Name</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getName()
	 * @generated
	 * @ordered
	 */
	protected static final String NAME_EDEFAULT = null;

	/**
	 * The cached value of the '{@link #getName() <em>Name</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getName()
	 * @generated
	 * @ordered
	 */
	protected String name = NAME_EDEFAULT;

	/**
	 * The default value of the '{@link #getTransport() <em>Transport</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getTransport()
	 * @generated
	 * @ordered
	 */
	protected static final ChannelTransport TRANSPORT_EDEFAULT = ChannelTransport.MQTT;

	/**
	 * The cached value of the '{@link #getTransport() <em>Transport</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getTransport()
	 * @generated
	 * @ordered
	 */
	protected ChannelTransport transport = TRANSPORT_EDEFAULT;

	/**
	 * The default value of the '{@link #getFormat() <em>Format</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getFormat()
	 * @generated
	 * @ordered
	 */
	protected static final PayloadFormat FORMAT_EDEFAULT = PayloadFormat.XMI;

	/**
	 * The cached value of the '{@link #getFormat() <em>Format</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getFormat()
	 * @generated
	 * @ordered
	 */
	protected PayloadFormat format = FORMAT_EDEFAULT;

	/**
	 * The default value of the '{@link #getBrokerId() <em>Broker Id</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getBrokerId()
	 * @generated
	 * @ordered
	 */
	protected static final String BROKER_ID_EDEFAULT = null;

	/**
	 * The cached value of the '{@link #getBrokerId() <em>Broker Id</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getBrokerId()
	 * @generated
	 * @ordered
	 */
	protected String brokerId = BROKER_ID_EDEFAULT;

	/**
	 * The cached value of the '{@link #getTopics() <em>Topics</em>}' attribute list.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getTopics()
	 * @generated
	 * @ordered
	 */
	protected EList<String> topics;

	/**
	 * The default value of the '{@link #getCodecTypeMapId() <em>Codec Type Map Id</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getCodecTypeMapId()
	 * @generated
	 * @ordered
	 */
	protected static final String CODEC_TYPE_MAP_ID_EDEFAULT = null;

	/**
	 * The cached value of the '{@link #getCodecTypeMapId() <em>Codec Type Map Id</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getCodecTypeMapId()
	 * @generated
	 * @ordered
	 */
	protected String codecTypeMapId = CODEC_TYPE_MAP_ID_EDEFAULT;

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	protected IngestChannelImpl() {
		super();
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	protected EClass eStaticClass() {
		return DeploymentPackage.Literals.INGEST_CHANNEL;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public String getName() {
		return name;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public void setName(String newName) {
		String oldName = name;
		name = newName;
		if (eNotificationRequired())
			eNotify(new ENotificationImpl(this, Notification.SET, DeploymentPackage.INGEST_CHANNEL__NAME, oldName, name));
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public ChannelTransport getTransport() {
		return transport;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public void setTransport(ChannelTransport newTransport) {
		ChannelTransport oldTransport = transport;
		transport = newTransport == null ? TRANSPORT_EDEFAULT : newTransport;
		if (eNotificationRequired())
			eNotify(new ENotificationImpl(this, Notification.SET, DeploymentPackage.INGEST_CHANNEL__TRANSPORT, oldTransport, transport));
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public PayloadFormat getFormat() {
		return format;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public void setFormat(PayloadFormat newFormat) {
		PayloadFormat oldFormat = format;
		format = newFormat == null ? FORMAT_EDEFAULT : newFormat;
		if (eNotificationRequired())
			eNotify(new ENotificationImpl(this, Notification.SET, DeploymentPackage.INGEST_CHANNEL__FORMAT, oldFormat, format));
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public String getBrokerId() {
		return brokerId;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public void setBrokerId(String newBrokerId) {
		String oldBrokerId = brokerId;
		brokerId = newBrokerId;
		if (eNotificationRequired())
			eNotify(new ENotificationImpl(this, Notification.SET, DeploymentPackage.INGEST_CHANNEL__BROKER_ID, oldBrokerId, brokerId));
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EList<String> getTopics() {
		if (topics == null) {
			topics = new EDataTypeUniqueEList<String>(String.class, this, DeploymentPackage.INGEST_CHANNEL__TOPICS);
		}
		return topics;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public String getCodecTypeMapId() {
		return codecTypeMapId;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public void setCodecTypeMapId(String newCodecTypeMapId) {
		String oldCodecTypeMapId = codecTypeMapId;
		codecTypeMapId = newCodecTypeMapId;
		if (eNotificationRequired())
			eNotify(new ENotificationImpl(this, Notification.SET, DeploymentPackage.INGEST_CHANNEL__CODEC_TYPE_MAP_ID, oldCodecTypeMapId, codecTypeMapId));
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public Object eGet(int featureID, boolean resolve, boolean coreType) {
		switch (featureID) {
			case DeploymentPackage.INGEST_CHANNEL__NAME:
				return getName();
			case DeploymentPackage.INGEST_CHANNEL__TRANSPORT:
				return getTransport();
			case DeploymentPackage.INGEST_CHANNEL__FORMAT:
				return getFormat();
			case DeploymentPackage.INGEST_CHANNEL__BROKER_ID:
				return getBrokerId();
			case DeploymentPackage.INGEST_CHANNEL__TOPICS:
				return getTopics();
			case DeploymentPackage.INGEST_CHANNEL__CODEC_TYPE_MAP_ID:
				return getCodecTypeMapId();
		}
		return super.eGet(featureID, resolve, coreType);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@SuppressWarnings("unchecked")
	@Override
	public void eSet(int featureID, Object newValue) {
		switch (featureID) {
			case DeploymentPackage.INGEST_CHANNEL__NAME:
				setName((String)newValue);
				return;
			case DeploymentPackage.INGEST_CHANNEL__TRANSPORT:
				setTransport((ChannelTransport)newValue);
				return;
			case DeploymentPackage.INGEST_CHANNEL__FORMAT:
				setFormat((PayloadFormat)newValue);
				return;
			case DeploymentPackage.INGEST_CHANNEL__BROKER_ID:
				setBrokerId((String)newValue);
				return;
			case DeploymentPackage.INGEST_CHANNEL__TOPICS:
				getTopics().clear();
				getTopics().addAll((Collection<? extends String>)newValue);
				return;
			case DeploymentPackage.INGEST_CHANNEL__CODEC_TYPE_MAP_ID:
				setCodecTypeMapId((String)newValue);
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
			case DeploymentPackage.INGEST_CHANNEL__NAME:
				setName(NAME_EDEFAULT);
				return;
			case DeploymentPackage.INGEST_CHANNEL__TRANSPORT:
				setTransport(TRANSPORT_EDEFAULT);
				return;
			case DeploymentPackage.INGEST_CHANNEL__FORMAT:
				setFormat(FORMAT_EDEFAULT);
				return;
			case DeploymentPackage.INGEST_CHANNEL__BROKER_ID:
				setBrokerId(BROKER_ID_EDEFAULT);
				return;
			case DeploymentPackage.INGEST_CHANNEL__TOPICS:
				getTopics().clear();
				return;
			case DeploymentPackage.INGEST_CHANNEL__CODEC_TYPE_MAP_ID:
				setCodecTypeMapId(CODEC_TYPE_MAP_ID_EDEFAULT);
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
			case DeploymentPackage.INGEST_CHANNEL__NAME:
				return NAME_EDEFAULT == null ? name != null : !NAME_EDEFAULT.equals(name);
			case DeploymentPackage.INGEST_CHANNEL__TRANSPORT:
				return transport != TRANSPORT_EDEFAULT;
			case DeploymentPackage.INGEST_CHANNEL__FORMAT:
				return format != FORMAT_EDEFAULT;
			case DeploymentPackage.INGEST_CHANNEL__BROKER_ID:
				return BROKER_ID_EDEFAULT == null ? brokerId != null : !BROKER_ID_EDEFAULT.equals(brokerId);
			case DeploymentPackage.INGEST_CHANNEL__TOPICS:
				return topics != null && !topics.isEmpty();
			case DeploymentPackage.INGEST_CHANNEL__CODEC_TYPE_MAP_ID:
				return CODEC_TYPE_MAP_ID_EDEFAULT == null ? codecTypeMapId != null : !CODEC_TYPE_MAP_ID_EDEFAULT.equals(codecTypeMapId);
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
		result.append(" (name: ");
		result.append(name);
		result.append(", transport: ");
		result.append(transport);
		result.append(", format: ");
		result.append(format);
		result.append(", brokerId: ");
		result.append(brokerId);
		result.append(", topics: ");
		result.append(topics);
		result.append(", codecTypeMapId: ");
		result.append(codecTypeMapId);
		result.append(')');
		return result.toString();
	}

} //IngestChannelImpl
