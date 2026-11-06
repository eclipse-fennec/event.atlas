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

import org.eclipse.emf.ecore.EObject;

import org.osgi.annotation.versioning.ProviderType;

/**
 * <!-- begin-user-doc -->
 * A representation of the model object '<em><b>Cloud Event</b></em>'.
 * <!-- end-user-doc -->
 *
 * <p>
 * The following features are supported:
 * </p>
 * <ul>
 *   <li>{@link org.eclipse.fennec.cloudevents.model.cloudevents.CloudEvent#getId <em>Id</em>}</li>
 *   <li>{@link org.eclipse.fennec.cloudevents.model.cloudevents.CloudEvent#getSource <em>Source</em>}</li>
 *   <li>{@link org.eclipse.fennec.cloudevents.model.cloudevents.CloudEvent#getDataSchema <em>Data Schema</em>}</li>
 *   <li>{@link org.eclipse.fennec.cloudevents.model.cloudevents.CloudEvent#getType <em>Type</em>}</li>
 *   <li>{@link org.eclipse.fennec.cloudevents.model.cloudevents.CloudEvent#getDataContentType <em>Data Content Type</em>}</li>
 *   <li>{@link org.eclipse.fennec.cloudevents.model.cloudevents.CloudEvent#getData <em>Data</em>}</li>
 *   <li>{@link org.eclipse.fennec.cloudevents.model.cloudevents.CloudEvent#getTime <em>Time</em>}</li>
 * </ul>
 *
 * @see org.eclipse.fennec.cloudevents.model.cloudevents.CloudEventsPackage#getCloudEvent()
 * @model
 * @generated
 */
@ProviderType
public interface CloudEvent extends EObject {
	/**
	 * Returns the value of the '<em><b>Id</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the value of the '<em>Id</em>' attribute.
	 * @see #setId(String)
	 * @see org.eclipse.fennec.cloudevents.model.cloudevents.CloudEventsPackage#getCloudEvent_Id()
	 * @model id="true" required="true"
	 * @generated
	 */
	String getId();

	/**
	 * Sets the value of the '{@link org.eclipse.fennec.cloudevents.model.cloudevents.CloudEvent#getId <em>Id</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @param value the new value of the '<em>Id</em>' attribute.
	 * @see #getId()
	 * @generated
	 */
	void setId(String value);

	/**
	 * Returns the value of the '<em><b>Source</b></em>' containment reference.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the value of the '<em>Source</em>' containment reference.
	 * @see #setSource(Source)
	 * @see org.eclipse.fennec.cloudevents.model.cloudevents.CloudEventsPackage#getCloudEvent_Source()
	 * @model containment="true" required="true"
	 * @generated
	 */
	Source getSource();

	/**
	 * Sets the value of the '{@link org.eclipse.fennec.cloudevents.model.cloudevents.CloudEvent#getSource <em>Source</em>}' containment reference.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @param value the new value of the '<em>Source</em>' containment reference.
	 * @see #getSource()
	 * @generated
	 */
	void setSource(Source value);

	/**
	 * Returns the value of the '<em><b>Data Schema</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * <!-- begin-model-doc -->
	 * The class uri of the payload
	 * <!-- end-model-doc -->
	 * @return the value of the '<em>Data Schema</em>' attribute.
	 * @see #setDataSchema(String)
	 * @see org.eclipse.fennec.cloudevents.model.cloudevents.CloudEventsPackage#getCloudEvent_DataSchema()
	 * @model required="true"
	 *        extendedMetaData="name='dataschema'"
	 * @generated
	 */
	String getDataSchema();

	/**
	 * Sets the value of the '{@link org.eclipse.fennec.cloudevents.model.cloudevents.CloudEvent#getDataSchema <em>Data Schema</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @param value the new value of the '<em>Data Schema</em>' attribute.
	 * @see #getDataSchema()
	 * @generated
	 */
	void setDataSchema(String value);

	/**
	 * Returns the value of the '<em><b>Type</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * <!-- begin-model-doc -->
	 * This attribute contains a value describing the type of event related to the 
	 * originating occurrence. Often this attribute is used for routing, observability, 
	 * policy enforcement, etc. The format of this is producer defined and might 
	 * include information such as the version of the type.
	 * Constraints:
	 * 
	 *     REQUIRED
	 *     MUST be a non-empty string
	 *     SHOULD be prefixed with a reverse-DNS name. The prefixed domain dictates the organization which defines the semantics of this event type.
	 * 
	 * Examples
	 * 
	 *     com.github.pull_request.opened
	 *     com.example.object.deleted.v2
	 * 
	 * 
	 * <!-- end-model-doc -->
	 * @return the value of the '<em>Type</em>' attribute.
	 * @see #setType(String)
	 * @see org.eclipse.fennec.cloudevents.model.cloudevents.CloudEventsPackage#getCloudEvent_Type()
	 * @model required="true"
	 * @generated
	 */
	String getType();

	/**
	 * Sets the value of the '{@link org.eclipse.fennec.cloudevents.model.cloudevents.CloudEvent#getType <em>Type</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @param value the new value of the '<em>Type</em>' attribute.
	 * @see #getType()
	 * @generated
	 */
	void setType(String value);

	/**
	 * Returns the value of the '<em><b>Data Content Type</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * <!-- begin-model-doc -->
	 * Content type of data value. 
	 * <!-- end-model-doc -->
	 * @return the value of the '<em>Data Content Type</em>' attribute.
	 * @see #setDataContentType(String)
	 * @see org.eclipse.fennec.cloudevents.model.cloudevents.CloudEventsPackage#getCloudEvent_DataContentType()
	 * @model extendedMetaData="name='datacontenttype'"
	 * @generated
	 */
	String getDataContentType();

	/**
	 * Sets the value of the '{@link org.eclipse.fennec.cloudevents.model.cloudevents.CloudEvent#getDataContentType <em>Data Content Type</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @param value the new value of the '<em>Data Content Type</em>' attribute.
	 * @see #getDataContentType()
	 * @generated
	 */
	void setDataContentType(String value);

	/**
	 * Returns the value of the '<em><b>Data</b></em>' containment reference.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * <!-- begin-model-doc -->
	 * The deserialized EObject
	 * <!-- end-model-doc -->
	 * @return the value of the '<em>Data</em>' containment reference.
	 * @see #setData(EObject)
	 * @see org.eclipse.fennec.cloudevents.model.cloudevents.CloudEventsPackage#getCloudEvent_Data()
	 * @model containment="true"
	 * @generated
	 */
	EObject getData();

	/**
	 * Sets the value of the '{@link org.eclipse.fennec.cloudevents.model.cloudevents.CloudEvent#getData <em>Data</em>}' containment reference.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @param value the new value of the '<em>Data</em>' containment reference.
	 * @see #getData()
	 * @generated
	 */
	void setData(EObject value);

	/**
	 * Returns the value of the '<em><b>Time</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * <!-- begin-model-doc -->
	 * Time of arrival unless the payload carries one
	 * <!-- end-model-doc -->
	 * @return the value of the '<em>Time</em>' attribute.
	 * @see #setTime(String)
	 * @see org.eclipse.fennec.cloudevents.model.cloudevents.CloudEventsPackage#getCloudEvent_Time()
	 * @model
	 * @generated
	 */
	String getTime();

	/**
	 * Sets the value of the '{@link org.eclipse.fennec.cloudevents.model.cloudevents.CloudEvent#getTime <em>Time</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @param value the new value of the '<em>Time</em>' attribute.
	 * @see #getTime()
	 * @generated
	 */
	void setTime(String value);

} // CloudEvent
