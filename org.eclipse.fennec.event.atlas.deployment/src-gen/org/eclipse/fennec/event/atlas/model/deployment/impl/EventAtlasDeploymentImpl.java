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
import org.eclipse.emf.common.notify.NotificationChain;

import org.eclipse.emf.common.util.EList;

import org.eclipse.emf.ecore.EClass;
import org.eclipse.emf.ecore.InternalEObject;

import org.eclipse.emf.ecore.impl.ENotificationImpl;
import org.eclipse.emf.ecore.impl.MinimalEObjectImpl;

import org.eclipse.emf.ecore.util.EObjectContainmentEList;
import org.eclipse.emf.ecore.util.InternalEList;

import org.eclipse.fennec.event.atlas.model.deployment.AtlasBinding;
import org.eclipse.fennec.event.atlas.model.deployment.DeploymentPackage;
import org.eclipse.fennec.event.atlas.model.deployment.EventAtlasDeployment;
import org.eclipse.fennec.event.atlas.model.deployment.HistoryConfig;
import org.eclipse.fennec.event.atlas.model.deployment.HttpEndpoint;
import org.eclipse.fennec.event.atlas.model.deployment.InferenceConfig;
import org.eclipse.fennec.event.atlas.model.deployment.IngestChannel;
import org.eclipse.fennec.event.atlas.model.deployment.MqttBroker;

/**
 * <!-- begin-user-doc -->
 * An implementation of the model object '<em><b>Event Atlas Deployment</b></em>'.
 * <!-- end-user-doc -->
 * <p>
 * The following features are implemented:
 * </p>
 * <ul>
 *   <li>{@link org.eclipse.fennec.event.atlas.model.deployment.impl.EventAtlasDeploymentImpl#getDeploymentId <em>Deployment Id</em>}</li>
 *   <li>{@link org.eclipse.fennec.event.atlas.model.deployment.impl.EventAtlasDeploymentImpl#getDescription <em>Description</em>}</li>
 *   <li>{@link org.eclipse.fennec.event.atlas.model.deployment.impl.EventAtlasDeploymentImpl#getHttp <em>Http</em>}</li>
 *   <li>{@link org.eclipse.fennec.event.atlas.model.deployment.impl.EventAtlasDeploymentImpl#getAtlas <em>Atlas</em>}</li>
 *   <li>{@link org.eclipse.fennec.event.atlas.model.deployment.impl.EventAtlasDeploymentImpl#getBrokers <em>Brokers</em>}</li>
 *   <li>{@link org.eclipse.fennec.event.atlas.model.deployment.impl.EventAtlasDeploymentImpl#getChannels <em>Channels</em>}</li>
 *   <li>{@link org.eclipse.fennec.event.atlas.model.deployment.impl.EventAtlasDeploymentImpl#getHistory <em>History</em>}</li>
 *   <li>{@link org.eclipse.fennec.event.atlas.model.deployment.impl.EventAtlasDeploymentImpl#getInference <em>Inference</em>}</li>
 * </ul>
 *
 * @generated
 */
public class EventAtlasDeploymentImpl extends MinimalEObjectImpl.Container implements EventAtlasDeployment {
	/**
	 * The default value of the '{@link #getDeploymentId() <em>Deployment Id</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getDeploymentId()
	 * @generated
	 * @ordered
	 */
	protected static final String DEPLOYMENT_ID_EDEFAULT = null;

	/**
	 * The cached value of the '{@link #getDeploymentId() <em>Deployment Id</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getDeploymentId()
	 * @generated
	 * @ordered
	 */
	protected String deploymentId = DEPLOYMENT_ID_EDEFAULT;

	/**
	 * The default value of the '{@link #getDescription() <em>Description</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getDescription()
	 * @generated
	 * @ordered
	 */
	protected static final String DESCRIPTION_EDEFAULT = null;

	/**
	 * The cached value of the '{@link #getDescription() <em>Description</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getDescription()
	 * @generated
	 * @ordered
	 */
	protected String description = DESCRIPTION_EDEFAULT;

	/**
	 * The cached value of the '{@link #getHttp() <em>Http</em>}' containment reference.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getHttp()
	 * @generated
	 * @ordered
	 */
	protected HttpEndpoint http;

	/**
	 * The cached value of the '{@link #getAtlas() <em>Atlas</em>}' containment reference.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getAtlas()
	 * @generated
	 * @ordered
	 */
	protected AtlasBinding atlas;

	/**
	 * The cached value of the '{@link #getBrokers() <em>Brokers</em>}' containment reference list.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getBrokers()
	 * @generated
	 * @ordered
	 */
	protected EList<MqttBroker> brokers;

	/**
	 * The cached value of the '{@link #getChannels() <em>Channels</em>}' containment reference list.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getChannels()
	 * @generated
	 * @ordered
	 */
	protected EList<IngestChannel> channels;

	/**
	 * The cached value of the '{@link #getHistory() <em>History</em>}' containment reference.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getHistory()
	 * @generated
	 * @ordered
	 */
	protected HistoryConfig history;

	/**
	 * The cached value of the '{@link #getInference() <em>Inference</em>}' containment reference.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getInference()
	 * @generated
	 * @ordered
	 */
	protected InferenceConfig inference;

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	protected EventAtlasDeploymentImpl() {
		super();
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	protected EClass eStaticClass() {
		return DeploymentPackage.Literals.EVENT_ATLAS_DEPLOYMENT;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public String getDeploymentId() {
		return deploymentId;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public void setDeploymentId(String newDeploymentId) {
		String oldDeploymentId = deploymentId;
		deploymentId = newDeploymentId;
		if (eNotificationRequired())
			eNotify(new ENotificationImpl(this, Notification.SET, DeploymentPackage.EVENT_ATLAS_DEPLOYMENT__DEPLOYMENT_ID, oldDeploymentId, deploymentId));
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public String getDescription() {
		return description;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public void setDescription(String newDescription) {
		String oldDescription = description;
		description = newDescription;
		if (eNotificationRequired())
			eNotify(new ENotificationImpl(this, Notification.SET, DeploymentPackage.EVENT_ATLAS_DEPLOYMENT__DESCRIPTION, oldDescription, description));
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public HttpEndpoint getHttp() {
		return http;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public NotificationChain basicSetHttp(HttpEndpoint newHttp, NotificationChain msgs) {
		HttpEndpoint oldHttp = http;
		http = newHttp;
		if (eNotificationRequired()) {
			ENotificationImpl notification = new ENotificationImpl(this, Notification.SET, DeploymentPackage.EVENT_ATLAS_DEPLOYMENT__HTTP, oldHttp, newHttp);
			if (msgs == null) msgs = notification; else msgs.add(notification);
		}
		return msgs;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public void setHttp(HttpEndpoint newHttp) {
		if (newHttp != http) {
			NotificationChain msgs = null;
			if (http != null)
				msgs = ((InternalEObject)http).eInverseRemove(this, EOPPOSITE_FEATURE_BASE - DeploymentPackage.EVENT_ATLAS_DEPLOYMENT__HTTP, null, msgs);
			if (newHttp != null)
				msgs = ((InternalEObject)newHttp).eInverseAdd(this, EOPPOSITE_FEATURE_BASE - DeploymentPackage.EVENT_ATLAS_DEPLOYMENT__HTTP, null, msgs);
			msgs = basicSetHttp(newHttp, msgs);
			if (msgs != null) msgs.dispatch();
		}
		else if (eNotificationRequired())
			eNotify(new ENotificationImpl(this, Notification.SET, DeploymentPackage.EVENT_ATLAS_DEPLOYMENT__HTTP, newHttp, newHttp));
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public AtlasBinding getAtlas() {
		return atlas;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public NotificationChain basicSetAtlas(AtlasBinding newAtlas, NotificationChain msgs) {
		AtlasBinding oldAtlas = atlas;
		atlas = newAtlas;
		if (eNotificationRequired()) {
			ENotificationImpl notification = new ENotificationImpl(this, Notification.SET, DeploymentPackage.EVENT_ATLAS_DEPLOYMENT__ATLAS, oldAtlas, newAtlas);
			if (msgs == null) msgs = notification; else msgs.add(notification);
		}
		return msgs;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public void setAtlas(AtlasBinding newAtlas) {
		if (newAtlas != atlas) {
			NotificationChain msgs = null;
			if (atlas != null)
				msgs = ((InternalEObject)atlas).eInverseRemove(this, EOPPOSITE_FEATURE_BASE - DeploymentPackage.EVENT_ATLAS_DEPLOYMENT__ATLAS, null, msgs);
			if (newAtlas != null)
				msgs = ((InternalEObject)newAtlas).eInverseAdd(this, EOPPOSITE_FEATURE_BASE - DeploymentPackage.EVENT_ATLAS_DEPLOYMENT__ATLAS, null, msgs);
			msgs = basicSetAtlas(newAtlas, msgs);
			if (msgs != null) msgs.dispatch();
		}
		else if (eNotificationRequired())
			eNotify(new ENotificationImpl(this, Notification.SET, DeploymentPackage.EVENT_ATLAS_DEPLOYMENT__ATLAS, newAtlas, newAtlas));
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EList<MqttBroker> getBrokers() {
		if (brokers == null) {
			brokers = new EObjectContainmentEList<MqttBroker>(MqttBroker.class, this, DeploymentPackage.EVENT_ATLAS_DEPLOYMENT__BROKERS);
		}
		return brokers;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EList<IngestChannel> getChannels() {
		if (channels == null) {
			channels = new EObjectContainmentEList<IngestChannel>(IngestChannel.class, this, DeploymentPackage.EVENT_ATLAS_DEPLOYMENT__CHANNELS);
		}
		return channels;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public HistoryConfig getHistory() {
		return history;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public NotificationChain basicSetHistory(HistoryConfig newHistory, NotificationChain msgs) {
		HistoryConfig oldHistory = history;
		history = newHistory;
		if (eNotificationRequired()) {
			ENotificationImpl notification = new ENotificationImpl(this, Notification.SET, DeploymentPackage.EVENT_ATLAS_DEPLOYMENT__HISTORY, oldHistory, newHistory);
			if (msgs == null) msgs = notification; else msgs.add(notification);
		}
		return msgs;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public void setHistory(HistoryConfig newHistory) {
		if (newHistory != history) {
			NotificationChain msgs = null;
			if (history != null)
				msgs = ((InternalEObject)history).eInverseRemove(this, EOPPOSITE_FEATURE_BASE - DeploymentPackage.EVENT_ATLAS_DEPLOYMENT__HISTORY, null, msgs);
			if (newHistory != null)
				msgs = ((InternalEObject)newHistory).eInverseAdd(this, EOPPOSITE_FEATURE_BASE - DeploymentPackage.EVENT_ATLAS_DEPLOYMENT__HISTORY, null, msgs);
			msgs = basicSetHistory(newHistory, msgs);
			if (msgs != null) msgs.dispatch();
		}
		else if (eNotificationRequired())
			eNotify(new ENotificationImpl(this, Notification.SET, DeploymentPackage.EVENT_ATLAS_DEPLOYMENT__HISTORY, newHistory, newHistory));
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public InferenceConfig getInference() {
		return inference;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public NotificationChain basicSetInference(InferenceConfig newInference, NotificationChain msgs) {
		InferenceConfig oldInference = inference;
		inference = newInference;
		if (eNotificationRequired()) {
			ENotificationImpl notification = new ENotificationImpl(this, Notification.SET, DeploymentPackage.EVENT_ATLAS_DEPLOYMENT__INFERENCE, oldInference, newInference);
			if (msgs == null) msgs = notification; else msgs.add(notification);
		}
		return msgs;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public void setInference(InferenceConfig newInference) {
		if (newInference != inference) {
			NotificationChain msgs = null;
			if (inference != null)
				msgs = ((InternalEObject)inference).eInverseRemove(this, EOPPOSITE_FEATURE_BASE - DeploymentPackage.EVENT_ATLAS_DEPLOYMENT__INFERENCE, null, msgs);
			if (newInference != null)
				msgs = ((InternalEObject)newInference).eInverseAdd(this, EOPPOSITE_FEATURE_BASE - DeploymentPackage.EVENT_ATLAS_DEPLOYMENT__INFERENCE, null, msgs);
			msgs = basicSetInference(newInference, msgs);
			if (msgs != null) msgs.dispatch();
		}
		else if (eNotificationRequired())
			eNotify(new ENotificationImpl(this, Notification.SET, DeploymentPackage.EVENT_ATLAS_DEPLOYMENT__INFERENCE, newInference, newInference));
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public NotificationChain eInverseRemove(InternalEObject otherEnd, int featureID, NotificationChain msgs) {
		switch (featureID) {
			case DeploymentPackage.EVENT_ATLAS_DEPLOYMENT__HTTP:
				return basicSetHttp(null, msgs);
			case DeploymentPackage.EVENT_ATLAS_DEPLOYMENT__ATLAS:
				return basicSetAtlas(null, msgs);
			case DeploymentPackage.EVENT_ATLAS_DEPLOYMENT__BROKERS:
				return ((InternalEList<?>)getBrokers()).basicRemove(otherEnd, msgs);
			case DeploymentPackage.EVENT_ATLAS_DEPLOYMENT__CHANNELS:
				return ((InternalEList<?>)getChannels()).basicRemove(otherEnd, msgs);
			case DeploymentPackage.EVENT_ATLAS_DEPLOYMENT__HISTORY:
				return basicSetHistory(null, msgs);
			case DeploymentPackage.EVENT_ATLAS_DEPLOYMENT__INFERENCE:
				return basicSetInference(null, msgs);
		}
		return super.eInverseRemove(otherEnd, featureID, msgs);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public Object eGet(int featureID, boolean resolve, boolean coreType) {
		switch (featureID) {
			case DeploymentPackage.EVENT_ATLAS_DEPLOYMENT__DEPLOYMENT_ID:
				return getDeploymentId();
			case DeploymentPackage.EVENT_ATLAS_DEPLOYMENT__DESCRIPTION:
				return getDescription();
			case DeploymentPackage.EVENT_ATLAS_DEPLOYMENT__HTTP:
				return getHttp();
			case DeploymentPackage.EVENT_ATLAS_DEPLOYMENT__ATLAS:
				return getAtlas();
			case DeploymentPackage.EVENT_ATLAS_DEPLOYMENT__BROKERS:
				return getBrokers();
			case DeploymentPackage.EVENT_ATLAS_DEPLOYMENT__CHANNELS:
				return getChannels();
			case DeploymentPackage.EVENT_ATLAS_DEPLOYMENT__HISTORY:
				return getHistory();
			case DeploymentPackage.EVENT_ATLAS_DEPLOYMENT__INFERENCE:
				return getInference();
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
			case DeploymentPackage.EVENT_ATLAS_DEPLOYMENT__DEPLOYMENT_ID:
				setDeploymentId((String)newValue);
				return;
			case DeploymentPackage.EVENT_ATLAS_DEPLOYMENT__DESCRIPTION:
				setDescription((String)newValue);
				return;
			case DeploymentPackage.EVENT_ATLAS_DEPLOYMENT__HTTP:
				setHttp((HttpEndpoint)newValue);
				return;
			case DeploymentPackage.EVENT_ATLAS_DEPLOYMENT__ATLAS:
				setAtlas((AtlasBinding)newValue);
				return;
			case DeploymentPackage.EVENT_ATLAS_DEPLOYMENT__BROKERS:
				getBrokers().clear();
				getBrokers().addAll((Collection<? extends MqttBroker>)newValue);
				return;
			case DeploymentPackage.EVENT_ATLAS_DEPLOYMENT__CHANNELS:
				getChannels().clear();
				getChannels().addAll((Collection<? extends IngestChannel>)newValue);
				return;
			case DeploymentPackage.EVENT_ATLAS_DEPLOYMENT__HISTORY:
				setHistory((HistoryConfig)newValue);
				return;
			case DeploymentPackage.EVENT_ATLAS_DEPLOYMENT__INFERENCE:
				setInference((InferenceConfig)newValue);
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
			case DeploymentPackage.EVENT_ATLAS_DEPLOYMENT__DEPLOYMENT_ID:
				setDeploymentId(DEPLOYMENT_ID_EDEFAULT);
				return;
			case DeploymentPackage.EVENT_ATLAS_DEPLOYMENT__DESCRIPTION:
				setDescription(DESCRIPTION_EDEFAULT);
				return;
			case DeploymentPackage.EVENT_ATLAS_DEPLOYMENT__HTTP:
				setHttp((HttpEndpoint)null);
				return;
			case DeploymentPackage.EVENT_ATLAS_DEPLOYMENT__ATLAS:
				setAtlas((AtlasBinding)null);
				return;
			case DeploymentPackage.EVENT_ATLAS_DEPLOYMENT__BROKERS:
				getBrokers().clear();
				return;
			case DeploymentPackage.EVENT_ATLAS_DEPLOYMENT__CHANNELS:
				getChannels().clear();
				return;
			case DeploymentPackage.EVENT_ATLAS_DEPLOYMENT__HISTORY:
				setHistory((HistoryConfig)null);
				return;
			case DeploymentPackage.EVENT_ATLAS_DEPLOYMENT__INFERENCE:
				setInference((InferenceConfig)null);
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
			case DeploymentPackage.EVENT_ATLAS_DEPLOYMENT__DEPLOYMENT_ID:
				return DEPLOYMENT_ID_EDEFAULT == null ? deploymentId != null : !DEPLOYMENT_ID_EDEFAULT.equals(deploymentId);
			case DeploymentPackage.EVENT_ATLAS_DEPLOYMENT__DESCRIPTION:
				return DESCRIPTION_EDEFAULT == null ? description != null : !DESCRIPTION_EDEFAULT.equals(description);
			case DeploymentPackage.EVENT_ATLAS_DEPLOYMENT__HTTP:
				return http != null;
			case DeploymentPackage.EVENT_ATLAS_DEPLOYMENT__ATLAS:
				return atlas != null;
			case DeploymentPackage.EVENT_ATLAS_DEPLOYMENT__BROKERS:
				return brokers != null && !brokers.isEmpty();
			case DeploymentPackage.EVENT_ATLAS_DEPLOYMENT__CHANNELS:
				return channels != null && !channels.isEmpty();
			case DeploymentPackage.EVENT_ATLAS_DEPLOYMENT__HISTORY:
				return history != null;
			case DeploymentPackage.EVENT_ATLAS_DEPLOYMENT__INFERENCE:
				return inference != null;
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
		result.append(" (deploymentId: ");
		result.append(deploymentId);
		result.append(", description: ");
		result.append(description);
		result.append(')');
		return result.toString();
	}

} //EventAtlasDeploymentImpl
