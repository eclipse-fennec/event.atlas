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
package org.eclipse.fennec.cloudevents.model.cloudevents.impl;

import org.eclipse.emf.common.notify.Notification;
import org.eclipse.emf.common.notify.NotificationChain;

import org.eclipse.emf.ecore.EClass;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.InternalEObject;

import org.eclipse.emf.ecore.impl.ENotificationImpl;
import org.eclipse.emf.ecore.impl.MinimalEObjectImpl;

import org.eclipse.fennec.cloudevents.model.cloudevents.CloudEvent;
import org.eclipse.fennec.cloudevents.model.cloudevents.CloudEventsPackage;
import org.eclipse.fennec.cloudevents.model.cloudevents.Source;

/**
 * <!-- begin-user-doc -->
 * An implementation of the model object '<em><b>Cloud Event</b></em>'.
 * <!-- end-user-doc -->
 * <p>
 * The following features are implemented:
 * </p>
 * <ul>
 *   <li>{@link org.eclipse.fennec.cloudevents.model.cloudevents.impl.CloudEventImpl#getId <em>Id</em>}</li>
 *   <li>{@link org.eclipse.fennec.cloudevents.model.cloudevents.impl.CloudEventImpl#getSource <em>Source</em>}</li>
 *   <li>{@link org.eclipse.fennec.cloudevents.model.cloudevents.impl.CloudEventImpl#getDataSchema <em>Data Schema</em>}</li>
 *   <li>{@link org.eclipse.fennec.cloudevents.model.cloudevents.impl.CloudEventImpl#getType <em>Type</em>}</li>
 *   <li>{@link org.eclipse.fennec.cloudevents.model.cloudevents.impl.CloudEventImpl#getDataContentType <em>Data Content Type</em>}</li>
 *   <li>{@link org.eclipse.fennec.cloudevents.model.cloudevents.impl.CloudEventImpl#getData <em>Data</em>}</li>
 *   <li>{@link org.eclipse.fennec.cloudevents.model.cloudevents.impl.CloudEventImpl#getTime <em>Time</em>}</li>
 * </ul>
 *
 * @generated
 */
public class CloudEventImpl extends MinimalEObjectImpl.Container implements CloudEvent {
	/**
	 * The default value of the '{@link #getId() <em>Id</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getId()
	 * @generated
	 * @ordered
	 */
	protected static final String ID_EDEFAULT = null;

	/**
	 * The cached value of the '{@link #getId() <em>Id</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getId()
	 * @generated
	 * @ordered
	 */
	protected String id = ID_EDEFAULT;

	/**
	 * The cached value of the '{@link #getSource() <em>Source</em>}' containment reference.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getSource()
	 * @generated
	 * @ordered
	 */
	protected Source source;

	/**
	 * The default value of the '{@link #getDataSchema() <em>Data Schema</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getDataSchema()
	 * @generated
	 * @ordered
	 */
	protected static final String DATA_SCHEMA_EDEFAULT = null;

	/**
	 * The cached value of the '{@link #getDataSchema() <em>Data Schema</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getDataSchema()
	 * @generated
	 * @ordered
	 */
	protected String dataSchema = DATA_SCHEMA_EDEFAULT;

	/**
	 * The default value of the '{@link #getType() <em>Type</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getType()
	 * @generated
	 * @ordered
	 */
	protected static final String TYPE_EDEFAULT = null;

	/**
	 * The cached value of the '{@link #getType() <em>Type</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getType()
	 * @generated
	 * @ordered
	 */
	protected String type = TYPE_EDEFAULT;

	/**
	 * The default value of the '{@link #getDataContentType() <em>Data Content Type</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getDataContentType()
	 * @generated
	 * @ordered
	 */
	protected static final String DATA_CONTENT_TYPE_EDEFAULT = null;

	/**
	 * The cached value of the '{@link #getDataContentType() <em>Data Content Type</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getDataContentType()
	 * @generated
	 * @ordered
	 */
	protected String dataContentType = DATA_CONTENT_TYPE_EDEFAULT;

	/**
	 * The cached value of the '{@link #getData() <em>Data</em>}' containment reference.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getData()
	 * @generated
	 * @ordered
	 */
	protected EObject data;

	/**
	 * The default value of the '{@link #getTime() <em>Time</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getTime()
	 * @generated
	 * @ordered
	 */
	protected static final String TIME_EDEFAULT = null;

	/**
	 * The cached value of the '{@link #getTime() <em>Time</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getTime()
	 * @generated
	 * @ordered
	 */
	protected String time = TIME_EDEFAULT;

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	protected CloudEventImpl() {
		super();
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	protected EClass eStaticClass() {
		return CloudEventsPackage.Literals.CLOUD_EVENT;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public String getId() {
		return id;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public void setId(String newId) {
		String oldId = id;
		id = newId;
		if (eNotificationRequired())
			eNotify(new ENotificationImpl(this, Notification.SET, CloudEventsPackage.CLOUD_EVENT__ID, oldId, id));
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public Source getSource() {
		return source;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public NotificationChain basicSetSource(Source newSource, NotificationChain msgs) {
		Source oldSource = source;
		source = newSource;
		if (eNotificationRequired()) {
			ENotificationImpl notification = new ENotificationImpl(this, Notification.SET, CloudEventsPackage.CLOUD_EVENT__SOURCE, oldSource, newSource);
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
	public void setSource(Source newSource) {
		if (newSource != source) {
			NotificationChain msgs = null;
			if (source != null)
				msgs = ((InternalEObject)source).eInverseRemove(this, EOPPOSITE_FEATURE_BASE - CloudEventsPackage.CLOUD_EVENT__SOURCE, null, msgs);
			if (newSource != null)
				msgs = ((InternalEObject)newSource).eInverseAdd(this, EOPPOSITE_FEATURE_BASE - CloudEventsPackage.CLOUD_EVENT__SOURCE, null, msgs);
			msgs = basicSetSource(newSource, msgs);
			if (msgs != null) msgs.dispatch();
		}
		else if (eNotificationRequired())
			eNotify(new ENotificationImpl(this, Notification.SET, CloudEventsPackage.CLOUD_EVENT__SOURCE, newSource, newSource));
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public String getDataSchema() {
		return dataSchema;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public void setDataSchema(String newDataSchema) {
		String oldDataSchema = dataSchema;
		dataSchema = newDataSchema;
		if (eNotificationRequired())
			eNotify(new ENotificationImpl(this, Notification.SET, CloudEventsPackage.CLOUD_EVENT__DATA_SCHEMA, oldDataSchema, dataSchema));
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public String getType() {
		return type;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public void setType(String newType) {
		String oldType = type;
		type = newType;
		if (eNotificationRequired())
			eNotify(new ENotificationImpl(this, Notification.SET, CloudEventsPackage.CLOUD_EVENT__TYPE, oldType, type));
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public String getDataContentType() {
		return dataContentType;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public void setDataContentType(String newDataContentType) {
		String oldDataContentType = dataContentType;
		dataContentType = newDataContentType;
		if (eNotificationRequired())
			eNotify(new ENotificationImpl(this, Notification.SET, CloudEventsPackage.CLOUD_EVENT__DATA_CONTENT_TYPE, oldDataContentType, dataContentType));
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EObject getData() {
		return data;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public NotificationChain basicSetData(EObject newData, NotificationChain msgs) {
		EObject oldData = data;
		data = newData;
		if (eNotificationRequired()) {
			ENotificationImpl notification = new ENotificationImpl(this, Notification.SET, CloudEventsPackage.CLOUD_EVENT__DATA, oldData, newData);
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
	public void setData(EObject newData) {
		if (newData != data) {
			NotificationChain msgs = null;
			if (data != null)
				msgs = ((InternalEObject)data).eInverseRemove(this, EOPPOSITE_FEATURE_BASE - CloudEventsPackage.CLOUD_EVENT__DATA, null, msgs);
			if (newData != null)
				msgs = ((InternalEObject)newData).eInverseAdd(this, EOPPOSITE_FEATURE_BASE - CloudEventsPackage.CLOUD_EVENT__DATA, null, msgs);
			msgs = basicSetData(newData, msgs);
			if (msgs != null) msgs.dispatch();
		}
		else if (eNotificationRequired())
			eNotify(new ENotificationImpl(this, Notification.SET, CloudEventsPackage.CLOUD_EVENT__DATA, newData, newData));
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public String getTime() {
		return time;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public void setTime(String newTime) {
		String oldTime = time;
		time = newTime;
		if (eNotificationRequired())
			eNotify(new ENotificationImpl(this, Notification.SET, CloudEventsPackage.CLOUD_EVENT__TIME, oldTime, time));
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public NotificationChain eInverseRemove(InternalEObject otherEnd, int featureID, NotificationChain msgs) {
		switch (featureID) {
			case CloudEventsPackage.CLOUD_EVENT__SOURCE:
				return basicSetSource(null, msgs);
			case CloudEventsPackage.CLOUD_EVENT__DATA:
				return basicSetData(null, msgs);
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
			case CloudEventsPackage.CLOUD_EVENT__ID:
				return getId();
			case CloudEventsPackage.CLOUD_EVENT__SOURCE:
				return getSource();
			case CloudEventsPackage.CLOUD_EVENT__DATA_SCHEMA:
				return getDataSchema();
			case CloudEventsPackage.CLOUD_EVENT__TYPE:
				return getType();
			case CloudEventsPackage.CLOUD_EVENT__DATA_CONTENT_TYPE:
				return getDataContentType();
			case CloudEventsPackage.CLOUD_EVENT__DATA:
				return getData();
			case CloudEventsPackage.CLOUD_EVENT__TIME:
				return getTime();
		}
		return super.eGet(featureID, resolve, coreType);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public void eSet(int featureID, Object newValue) {
		switch (featureID) {
			case CloudEventsPackage.CLOUD_EVENT__ID:
				setId((String)newValue);
				return;
			case CloudEventsPackage.CLOUD_EVENT__SOURCE:
				setSource((Source)newValue);
				return;
			case CloudEventsPackage.CLOUD_EVENT__DATA_SCHEMA:
				setDataSchema((String)newValue);
				return;
			case CloudEventsPackage.CLOUD_EVENT__TYPE:
				setType((String)newValue);
				return;
			case CloudEventsPackage.CLOUD_EVENT__DATA_CONTENT_TYPE:
				setDataContentType((String)newValue);
				return;
			case CloudEventsPackage.CLOUD_EVENT__DATA:
				setData((EObject)newValue);
				return;
			case CloudEventsPackage.CLOUD_EVENT__TIME:
				setTime((String)newValue);
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
			case CloudEventsPackage.CLOUD_EVENT__ID:
				setId(ID_EDEFAULT);
				return;
			case CloudEventsPackage.CLOUD_EVENT__SOURCE:
				setSource((Source)null);
				return;
			case CloudEventsPackage.CLOUD_EVENT__DATA_SCHEMA:
				setDataSchema(DATA_SCHEMA_EDEFAULT);
				return;
			case CloudEventsPackage.CLOUD_EVENT__TYPE:
				setType(TYPE_EDEFAULT);
				return;
			case CloudEventsPackage.CLOUD_EVENT__DATA_CONTENT_TYPE:
				setDataContentType(DATA_CONTENT_TYPE_EDEFAULT);
				return;
			case CloudEventsPackage.CLOUD_EVENT__DATA:
				setData((EObject)null);
				return;
			case CloudEventsPackage.CLOUD_EVENT__TIME:
				setTime(TIME_EDEFAULT);
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
			case CloudEventsPackage.CLOUD_EVENT__ID:
				return ID_EDEFAULT == null ? id != null : !ID_EDEFAULT.equals(id);
			case CloudEventsPackage.CLOUD_EVENT__SOURCE:
				return source != null;
			case CloudEventsPackage.CLOUD_EVENT__DATA_SCHEMA:
				return DATA_SCHEMA_EDEFAULT == null ? dataSchema != null : !DATA_SCHEMA_EDEFAULT.equals(dataSchema);
			case CloudEventsPackage.CLOUD_EVENT__TYPE:
				return TYPE_EDEFAULT == null ? type != null : !TYPE_EDEFAULT.equals(type);
			case CloudEventsPackage.CLOUD_EVENT__DATA_CONTENT_TYPE:
				return DATA_CONTENT_TYPE_EDEFAULT == null ? dataContentType != null : !DATA_CONTENT_TYPE_EDEFAULT.equals(dataContentType);
			case CloudEventsPackage.CLOUD_EVENT__DATA:
				return data != null;
			case CloudEventsPackage.CLOUD_EVENT__TIME:
				return TIME_EDEFAULT == null ? time != null : !TIME_EDEFAULT.equals(time);
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
		result.append(" (id: ");
		result.append(id);
		result.append(", dataSchema: ");
		result.append(dataSchema);
		result.append(", type: ");
		result.append(type);
		result.append(", dataContentType: ");
		result.append(dataContentType);
		result.append(", time: ");
		result.append(time);
		result.append(')');
		return result.toString();
	}

} //CloudEventImpl
