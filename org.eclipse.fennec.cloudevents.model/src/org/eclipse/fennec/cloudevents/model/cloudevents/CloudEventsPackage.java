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


import org.eclipse.emf.ecore.EAttribute;
import org.eclipse.emf.ecore.EClass;
import org.eclipse.emf.ecore.EReference;

import org.eclipse.fennec.emf.osgi.annotation.provide.EPackage;

import org.osgi.annotation.versioning.ProviderType;

/**
 * <!-- begin-user-doc -->
 * The <b>Package</b> for the model.
 * It contains accessors for the meta objects to represent
 * <ul>
 *   <li>each class,</li>
 *   <li>each feature of each class,</li>
 *   <li>each operation of each class,</li>
 *   <li>each enum,</li>
 *   <li>and each data type</li>
 * </ul>
 * <!-- end-user-doc -->
 * @see org.eclipse.fennec.cloudevents.model.cloudevents.CloudEventsFactory
 * @model kind="package"
 *        annotation="Version value='1.0'"
 *        annotation="http://www.eclipse.org/emf/2002/GenModel complianceLevel='17.0' oSGiCompatible='true' basePackage='org.eclipse.fennec.cloudevents.model' resource='XMI' copyrightText='Copyright (c) 2012 - 2026 Data In Motion and others.\nAll rights reserved. \n\nThis program and the accompanying materials are made\navailable under the terms of the Eclipse Public License 2.0\nwhich is available at https://www.eclipse.org/legal/epl-2.0/\n\nSPDX-License-Identifier: EPL-2.0\n\nContributors:\n     Mark Hoffmann - initial API and implementation'"
 * @generated
 */
@ProviderType
@EPackage(uri = CloudEventsPackage.eNS_URI, fingerprint = "fp1:9773e64b523d62531d8c989f71478372cba24e02a9013fd0436d6c8799f3cfe3", genModel = "/model/cloudevents.genmodel", genModelSourceLocations = {"model/cloudevents.genmodel","org.eclipse.fennec.cloudevents.model/model/cloudevents.genmodel"}, ecore = "/model/cloudevents.ecore", ecoreSourceLocations = "/model/cloudevents.ecore")
public interface CloudEventsPackage extends org.eclipse.emf.ecore.EPackage {
	/**
	 * The package name.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	String eNAME = "cloudevents";

	/**
	 * The package namespace URI.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	String eNS_URI = "https://fennec.eclipse.org/cloudevents/1.0";

	/**
	 * The package namespace name.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	String eNS_PREFIX = "cloudevents";

	/**
	 * The singleton instance of the package.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	CloudEventsPackage eINSTANCE = org.eclipse.fennec.cloudevents.model.cloudevents.impl.CloudEventsPackageImpl.init();

	/**
	 * The meta object id for the '{@link org.eclipse.fennec.cloudevents.model.cloudevents.impl.CloudEventImpl <em>Cloud Event</em>}' class.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see org.eclipse.fennec.cloudevents.model.cloudevents.impl.CloudEventImpl
	 * @see org.eclipse.fennec.cloudevents.model.cloudevents.impl.CloudEventsPackageImpl#getCloudEvent()
	 * @generated
	 */
	int CLOUD_EVENT = 0;

	/**
	 * The feature id for the '<em><b>Id</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int CLOUD_EVENT__ID = 0;

	/**
	 * The feature id for the '<em><b>Source</b></em>' containment reference.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int CLOUD_EVENT__SOURCE = 1;

	/**
	 * The feature id for the '<em><b>Data Schema</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int CLOUD_EVENT__DATA_SCHEMA = 2;

	/**
	 * The feature id for the '<em><b>Type</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int CLOUD_EVENT__TYPE = 3;

	/**
	 * The feature id for the '<em><b>Data Content Type</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int CLOUD_EVENT__DATA_CONTENT_TYPE = 4;

	/**
	 * The feature id for the '<em><b>Data</b></em>' containment reference.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int CLOUD_EVENT__DATA = 5;

	/**
	 * The feature id for the '<em><b>Time</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int CLOUD_EVENT__TIME = 6;

	/**
	 * The number of structural features of the '<em>Cloud Event</em>' class.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int CLOUD_EVENT_FEATURE_COUNT = 7;

	/**
	 * The number of operations of the '<em>Cloud Event</em>' class.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int CLOUD_EVENT_OPERATION_COUNT = 0;

	/**
	 * The meta object id for the '{@link org.eclipse.fennec.cloudevents.model.cloudevents.impl.SourceImpl <em>Source</em>}' class.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see org.eclipse.fennec.cloudevents.model.cloudevents.impl.SourceImpl
	 * @see org.eclipse.fennec.cloudevents.model.cloudevents.impl.CloudEventsPackageImpl#getSource()
	 * @generated
	 */
	int SOURCE = 1;

	/**
	 * The feature id for the '<em><b>Source URI</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int SOURCE__SOURCE_URI = 0;

	/**
	 * The number of structural features of the '<em>Source</em>' class.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int SOURCE_FEATURE_COUNT = 1;

	/**
	 * The number of operations of the '<em>Source</em>' class.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int SOURCE_OPERATION_COUNT = 0;

	/**
	 * The meta object id for the '{@link org.eclipse.fennec.cloudevents.model.cloudevents.impl.MqttSourceImpl <em>Mqtt Source</em>}' class.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see org.eclipse.fennec.cloudevents.model.cloudevents.impl.MqttSourceImpl
	 * @see org.eclipse.fennec.cloudevents.model.cloudevents.impl.CloudEventsPackageImpl#getMqttSource()
	 * @generated
	 */
	int MQTT_SOURCE = 2;

	/**
	 * The feature id for the '<em><b>Source URI</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int MQTT_SOURCE__SOURCE_URI = SOURCE__SOURCE_URI;

	/**
	 * The feature id for the '<em><b>Broker</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int MQTT_SOURCE__BROKER = SOURCE_FEATURE_COUNT + 0;

	/**
	 * The number of structural features of the '<em>Mqtt Source</em>' class.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int MQTT_SOURCE_FEATURE_COUNT = SOURCE_FEATURE_COUNT + 1;

	/**
	 * The number of operations of the '<em>Mqtt Source</em>' class.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int MQTT_SOURCE_OPERATION_COUNT = SOURCE_OPERATION_COUNT + 0;


	/**
	 * Returns the meta object for class '{@link org.eclipse.fennec.cloudevents.model.cloudevents.CloudEvent <em>Cloud Event</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for class '<em>Cloud Event</em>'.
	 * @see org.eclipse.fennec.cloudevents.model.cloudevents.CloudEvent
	 * @generated
	 */
	EClass getCloudEvent();

	/**
	 * Returns the meta object for the attribute '{@link org.eclipse.fennec.cloudevents.model.cloudevents.CloudEvent#getId <em>Id</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the attribute '<em>Id</em>'.
	 * @see org.eclipse.fennec.cloudevents.model.cloudevents.CloudEvent#getId()
	 * @see #getCloudEvent()
	 * @generated
	 */
	EAttribute getCloudEvent_Id();

	/**
	 * Returns the meta object for the containment reference '{@link org.eclipse.fennec.cloudevents.model.cloudevents.CloudEvent#getSource <em>Source</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the containment reference '<em>Source</em>'.
	 * @see org.eclipse.fennec.cloudevents.model.cloudevents.CloudEvent#getSource()
	 * @see #getCloudEvent()
	 * @generated
	 */
	EReference getCloudEvent_Source();

	/**
	 * Returns the meta object for the attribute '{@link org.eclipse.fennec.cloudevents.model.cloudevents.CloudEvent#getDataSchema <em>Data Schema</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the attribute '<em>Data Schema</em>'.
	 * @see org.eclipse.fennec.cloudevents.model.cloudevents.CloudEvent#getDataSchema()
	 * @see #getCloudEvent()
	 * @generated
	 */
	EAttribute getCloudEvent_DataSchema();

	/**
	 * Returns the meta object for the attribute '{@link org.eclipse.fennec.cloudevents.model.cloudevents.CloudEvent#getType <em>Type</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the attribute '<em>Type</em>'.
	 * @see org.eclipse.fennec.cloudevents.model.cloudevents.CloudEvent#getType()
	 * @see #getCloudEvent()
	 * @generated
	 */
	EAttribute getCloudEvent_Type();

	/**
	 * Returns the meta object for the attribute '{@link org.eclipse.fennec.cloudevents.model.cloudevents.CloudEvent#getDataContentType <em>Data Content Type</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the attribute '<em>Data Content Type</em>'.
	 * @see org.eclipse.fennec.cloudevents.model.cloudevents.CloudEvent#getDataContentType()
	 * @see #getCloudEvent()
	 * @generated
	 */
	EAttribute getCloudEvent_DataContentType();

	/**
	 * Returns the meta object for the containment reference '{@link org.eclipse.fennec.cloudevents.model.cloudevents.CloudEvent#getData <em>Data</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the containment reference '<em>Data</em>'.
	 * @see org.eclipse.fennec.cloudevents.model.cloudevents.CloudEvent#getData()
	 * @see #getCloudEvent()
	 * @generated
	 */
	EReference getCloudEvent_Data();

	/**
	 * Returns the meta object for the attribute '{@link org.eclipse.fennec.cloudevents.model.cloudevents.CloudEvent#getTime <em>Time</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the attribute '<em>Time</em>'.
	 * @see org.eclipse.fennec.cloudevents.model.cloudevents.CloudEvent#getTime()
	 * @see #getCloudEvent()
	 * @generated
	 */
	EAttribute getCloudEvent_Time();

	/**
	 * Returns the meta object for class '{@link org.eclipse.fennec.cloudevents.model.cloudevents.Source <em>Source</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for class '<em>Source</em>'.
	 * @see org.eclipse.fennec.cloudevents.model.cloudevents.Source
	 * @generated
	 */
	EClass getSource();

	/**
	 * Returns the meta object for the attribute '{@link org.eclipse.fennec.cloudevents.model.cloudevents.Source#getSourceURI <em>Source URI</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the attribute '<em>Source URI</em>'.
	 * @see org.eclipse.fennec.cloudevents.model.cloudevents.Source#getSourceURI()
	 * @see #getSource()
	 * @generated
	 */
	EAttribute getSource_SourceURI();

	/**
	 * Returns the meta object for class '{@link org.eclipse.fennec.cloudevents.model.cloudevents.MqttSource <em>Mqtt Source</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for class '<em>Mqtt Source</em>'.
	 * @see org.eclipse.fennec.cloudevents.model.cloudevents.MqttSource
	 * @generated
	 */
	EClass getMqttSource();

	/**
	 * Returns the meta object for the attribute '{@link org.eclipse.fennec.cloudevents.model.cloudevents.MqttSource#getBroker <em>Broker</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the attribute '<em>Broker</em>'.
	 * @see org.eclipse.fennec.cloudevents.model.cloudevents.MqttSource#getBroker()
	 * @see #getMqttSource()
	 * @generated
	 */
	EAttribute getMqttSource_Broker();

	/**
	 * Returns the factory that creates the instances of the model.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the factory that creates the instances of the model.
	 * @generated
	 */
	CloudEventsFactory getCloudEventsFactory();

	/**
	 * <!-- begin-user-doc -->
	 * Defines literals for the meta objects that represent
	 * <ul>
	 *   <li>each class,</li>
	 *   <li>each feature of each class,</li>
	 *   <li>each operation of each class,</li>
	 *   <li>each enum,</li>
	 *   <li>and each data type</li>
	 * </ul>
	 * <!-- end-user-doc -->
	 * @generated
	 */
	interface Literals {
		/**
		 * The meta object literal for the '{@link org.eclipse.fennec.cloudevents.model.cloudevents.impl.CloudEventImpl <em>Cloud Event</em>}' class.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @see org.eclipse.fennec.cloudevents.model.cloudevents.impl.CloudEventImpl
		 * @see org.eclipse.fennec.cloudevents.model.cloudevents.impl.CloudEventsPackageImpl#getCloudEvent()
		 * @generated
		 */
		EClass CLOUD_EVENT = eINSTANCE.getCloudEvent();

		/**
		 * The meta object literal for the '<em><b>Id</b></em>' attribute feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EAttribute CLOUD_EVENT__ID = eINSTANCE.getCloudEvent_Id();

		/**
		 * The meta object literal for the '<em><b>Source</b></em>' containment reference feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EReference CLOUD_EVENT__SOURCE = eINSTANCE.getCloudEvent_Source();

		/**
		 * The meta object literal for the '<em><b>Data Schema</b></em>' attribute feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EAttribute CLOUD_EVENT__DATA_SCHEMA = eINSTANCE.getCloudEvent_DataSchema();

		/**
		 * The meta object literal for the '<em><b>Type</b></em>' attribute feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EAttribute CLOUD_EVENT__TYPE = eINSTANCE.getCloudEvent_Type();

		/**
		 * The meta object literal for the '<em><b>Data Content Type</b></em>' attribute feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EAttribute CLOUD_EVENT__DATA_CONTENT_TYPE = eINSTANCE.getCloudEvent_DataContentType();

		/**
		 * The meta object literal for the '<em><b>Data</b></em>' containment reference feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EReference CLOUD_EVENT__DATA = eINSTANCE.getCloudEvent_Data();

		/**
		 * The meta object literal for the '<em><b>Time</b></em>' attribute feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EAttribute CLOUD_EVENT__TIME = eINSTANCE.getCloudEvent_Time();

		/**
		 * The meta object literal for the '{@link org.eclipse.fennec.cloudevents.model.cloudevents.impl.SourceImpl <em>Source</em>}' class.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @see org.eclipse.fennec.cloudevents.model.cloudevents.impl.SourceImpl
		 * @see org.eclipse.fennec.cloudevents.model.cloudevents.impl.CloudEventsPackageImpl#getSource()
		 * @generated
		 */
		EClass SOURCE = eINSTANCE.getSource();

		/**
		 * The meta object literal for the '<em><b>Source URI</b></em>' attribute feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EAttribute SOURCE__SOURCE_URI = eINSTANCE.getSource_SourceURI();

		/**
		 * The meta object literal for the '{@link org.eclipse.fennec.cloudevents.model.cloudevents.impl.MqttSourceImpl <em>Mqtt Source</em>}' class.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @see org.eclipse.fennec.cloudevents.model.cloudevents.impl.MqttSourceImpl
		 * @see org.eclipse.fennec.cloudevents.model.cloudevents.impl.CloudEventsPackageImpl#getMqttSource()
		 * @generated
		 */
		EClass MQTT_SOURCE = eINSTANCE.getMqttSource();

		/**
		 * The meta object literal for the '<em><b>Broker</b></em>' attribute feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EAttribute MQTT_SOURCE__BROKER = eINSTANCE.getMqttSource_Broker();

	}

} //CloudEventsPackage
