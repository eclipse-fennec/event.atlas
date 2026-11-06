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
 *      Mark Hoffmann - initial API and implementation
 */
package org.eclipse.fennec.cloudevents.model.cloudevents.impl;

import org.eclipse.emf.ecore.EAttribute;
import org.eclipse.emf.ecore.EClass;
import org.eclipse.emf.ecore.EPackage;
import org.eclipse.emf.ecore.EReference;

import org.eclipse.emf.ecore.impl.EPackageImpl;

import org.eclipse.fennec.cloudevents.model.cloudevents.CloudEvent;
import org.eclipse.fennec.cloudevents.model.cloudevents.CloudEventsFactory;
import org.eclipse.fennec.cloudevents.model.cloudevents.CloudEventsPackage;
import org.eclipse.fennec.cloudevents.model.cloudevents.MqttSource;
import org.eclipse.fennec.cloudevents.model.cloudevents.Source;

/**
 * <!-- begin-user-doc -->
 * An implementation of the model <b>Package</b>.
 * <!-- end-user-doc -->
 * @generated
 */
public class CloudEventsPackageImpl extends EPackageImpl implements CloudEventsPackage {
	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	private EClass cloudEventEClass = null;

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	private EClass sourceEClass = null;

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	private EClass mqttSourceEClass = null;

	/**
	 * Creates an instance of the model <b>Package</b>, registered with
	 * {@link org.eclipse.emf.ecore.EPackage.Registry EPackage.Registry} by the package
	 * package URI value.
	 * <p>Note: the correct way to create the package is via the static
	 * factory method {@link #init init()}, which also performs
	 * initialization of the package, or returns the registered package,
	 * if one already exists.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see org.eclipse.emf.ecore.EPackage.Registry
	 * @see org.eclipse.fennec.cloudevents.model.cloudevents.CloudEventsPackage#eNS_URI
	 * @see #init()
	 * @generated
	 */
	private CloudEventsPackageImpl() {
		super(eNS_URI, CloudEventsFactory.eINSTANCE);
	}
	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	private static boolean isInited = false;

	/**
	 * Creates, registers, and initializes the <b>Package</b> for this model, and for any others upon which it depends.
	 *
	 * <p>This method is used to initialize {@link CloudEventsPackage#eINSTANCE} when that field is accessed.
	 * Clients should not invoke it directly. Instead, they should simply access that field to obtain the package.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #eNS_URI
	 * @see #createPackageContents()
	 * @see #initializePackageContents()
	 * @generated
	 */
	public static CloudEventsPackage init() {
		if (isInited) return (CloudEventsPackage)EPackage.Registry.INSTANCE.getEPackage(CloudEventsPackage.eNS_URI);

		// Obtain or create and register package
		Object registeredCloudEventsPackage = EPackage.Registry.INSTANCE.get(eNS_URI);
		CloudEventsPackageImpl theCloudEventsPackage = registeredCloudEventsPackage instanceof CloudEventsPackageImpl ? (CloudEventsPackageImpl)registeredCloudEventsPackage : new CloudEventsPackageImpl();

		isInited = true;

		// Create package meta-data objects
		theCloudEventsPackage.createPackageContents();

		// Initialize created meta-data
		theCloudEventsPackage.initializePackageContents();

		// Mark meta-data to indicate it can't be changed
		theCloudEventsPackage.freeze();

		// Update the registry and return the package
		EPackage.Registry.INSTANCE.put(CloudEventsPackage.eNS_URI, theCloudEventsPackage);
		return theCloudEventsPackage;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EClass getCloudEvent() {
		return cloudEventEClass;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EAttribute getCloudEvent_Id() {
		return (EAttribute)cloudEventEClass.getEStructuralFeatures().get(0);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EReference getCloudEvent_Source() {
		return (EReference)cloudEventEClass.getEStructuralFeatures().get(1);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EAttribute getCloudEvent_DataSchema() {
		return (EAttribute)cloudEventEClass.getEStructuralFeatures().get(2);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EAttribute getCloudEvent_Type() {
		return (EAttribute)cloudEventEClass.getEStructuralFeatures().get(3);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EAttribute getCloudEvent_DataContentType() {
		return (EAttribute)cloudEventEClass.getEStructuralFeatures().get(4);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EReference getCloudEvent_Data() {
		return (EReference)cloudEventEClass.getEStructuralFeatures().get(5);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EAttribute getCloudEvent_Time() {
		return (EAttribute)cloudEventEClass.getEStructuralFeatures().get(6);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EClass getSource() {
		return sourceEClass;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EAttribute getSource_SourceURI() {
		return (EAttribute)sourceEClass.getEStructuralFeatures().get(0);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EClass getMqttSource() {
		return mqttSourceEClass;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EAttribute getMqttSource_Broker() {
		return (EAttribute)mqttSourceEClass.getEStructuralFeatures().get(0);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public CloudEventsFactory getCloudEventsFactory() {
		return (CloudEventsFactory)getEFactoryInstance();
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	private boolean isCreated = false;

	/**
	 * Creates the meta-model objects for the package.  This method is
	 * guarded to have no affect on any invocation but its first.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public void createPackageContents() {
		if (isCreated) return;
		isCreated = true;

		// Create classes and their features
		cloudEventEClass = createEClass(CLOUD_EVENT);
		createEAttribute(cloudEventEClass, CLOUD_EVENT__ID);
		createEReference(cloudEventEClass, CLOUD_EVENT__SOURCE);
		createEAttribute(cloudEventEClass, CLOUD_EVENT__DATA_SCHEMA);
		createEAttribute(cloudEventEClass, CLOUD_EVENT__TYPE);
		createEAttribute(cloudEventEClass, CLOUD_EVENT__DATA_CONTENT_TYPE);
		createEReference(cloudEventEClass, CLOUD_EVENT__DATA);
		createEAttribute(cloudEventEClass, CLOUD_EVENT__TIME);

		sourceEClass = createEClass(SOURCE);
		createEAttribute(sourceEClass, SOURCE__SOURCE_URI);

		mqttSourceEClass = createEClass(MQTT_SOURCE);
		createEAttribute(mqttSourceEClass, MQTT_SOURCE__BROKER);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	private boolean isInitialized = false;

	/**
	 * Complete the initialization of the package and its meta-model.  This
	 * method is guarded to have no affect on any invocation but its first.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public void initializePackageContents() {
		if (isInitialized) return;
		isInitialized = true;

		// Initialize package
		setName(eNAME);
		setNsPrefix(eNS_PREFIX);
		setNsURI(eNS_URI);

		// Create type parameters

		// Set bounds for type parameters

		// Add supertypes to classes
		mqttSourceEClass.getESuperTypes().add(this.getSource());

		// Initialize classes, features, and operations; add parameters
		initEClass(cloudEventEClass, CloudEvent.class, "CloudEvent", !IS_ABSTRACT, !IS_INTERFACE, IS_GENERATED_INSTANCE_CLASS);
		initEAttribute(getCloudEvent_Id(), ecorePackage.getEString(), "id", null, 1, 1, CloudEvent.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
		initEReference(getCloudEvent_Source(), this.getSource(), null, "source", null, 1, 1, CloudEvent.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, IS_COMPOSITE, !IS_RESOLVE_PROXIES, !IS_UNSETTABLE, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
		initEAttribute(getCloudEvent_DataSchema(), ecorePackage.getEString(), "dataSchema", null, 1, 1, CloudEvent.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
		initEAttribute(getCloudEvent_Type(), ecorePackage.getEString(), "type", null, 1, 1, CloudEvent.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
		initEAttribute(getCloudEvent_DataContentType(), ecorePackage.getEString(), "dataContentType", null, 0, 1, CloudEvent.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
		initEReference(getCloudEvent_Data(), ecorePackage.getEObject(), null, "data", null, 0, 1, CloudEvent.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, IS_COMPOSITE, !IS_RESOLVE_PROXIES, !IS_UNSETTABLE, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
		initEAttribute(getCloudEvent_Time(), ecorePackage.getEString(), "time", null, 0, 1, CloudEvent.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);

		initEClass(sourceEClass, Source.class, "Source", !IS_ABSTRACT, !IS_INTERFACE, IS_GENERATED_INSTANCE_CLASS);
		initEAttribute(getSource_SourceURI(), ecorePackage.getEString(), "sourceURI", null, 1, 1, Source.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);

		initEClass(mqttSourceEClass, MqttSource.class, "MqttSource", !IS_ABSTRACT, !IS_INTERFACE, IS_GENERATED_INSTANCE_CLASS);
		initEAttribute(getMqttSource_Broker(), ecorePackage.getEString(), "broker", null, 0, 1, MqttSource.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);

		// Create resource
		createResource(eNS_URI);

		// Create annotations
		// Version
		createVersionAnnotations();
		// http://www.eclipse.org/emf/2002/GenModel
		createGenModelAnnotations();
		// http:///org/eclipse/emf/ecore/util/ExtendedMetaData
		createExtendedMetaDataAnnotations();
	}

	/**
	 * Initializes the annotations for <b>Version</b>.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	protected void createVersionAnnotations() {
		String source = "Version";
		addAnnotation
		  (this,
		   source,
		   new String[] {
			   "value", "1.0"
		   });
	}

	/**
	 * Initializes the annotations for <b>http://www.eclipse.org/emf/2002/GenModel</b>.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	protected void createGenModelAnnotations() {
		String source = "http://www.eclipse.org/emf/2002/GenModel";
		addAnnotation
		  (this,
		   source,
		   new String[] {
			   "complianceLevel", "17.0",
			   "oSGiCompatible", "true",
			   "basePackage", "org.eclipse.fennec.cloudevents.model",
			   "resource", "XMI",
			   "copyrightText", "Copyright (c) 2012 - 2026 Data In Motion and others.\nAll rights reserved. \n\nThis program and the accompanying materials are made\navailable under the terms of the Eclipse Public License 2.0\nwhich is available at https://www.eclipse.org/legal/epl-2.0/\n\nSPDX-License-Identifier: EPL-2.0\n\nContributors:\n     Mark Hoffmann - initial API and implementation"
		   });
		addAnnotation
		  (getCloudEvent_DataSchema(),
		   source,
		   new String[] {
			   "documentation", "The class uri of the payload"
		   });
		addAnnotation
		  (getCloudEvent_Type(),
		   source,
		   new String[] {
			   "documentation", "This attribute contains a value describing the type of event related to the \noriginating occurrence. Often this attribute is used for routing, observability, \npolicy enforcement, etc. The format of this is producer defined and might \ninclude information such as the version of the type.\nConstraints:\n\n    REQUIRED\n    MUST be a non-empty string\n    SHOULD be prefixed with a reverse-DNS name. The prefixed domain dictates the organization which defines the semantics of this event type.\n\nExamples\n\n    com.github.pull_request.opened\n    com.example.object.deleted.v2\n\n"
		   });
		addAnnotation
		  (getCloudEvent_DataContentType(),
		   source,
		   new String[] {
			   "documentation", "Content type of data value. "
		   });
		addAnnotation
		  (getCloudEvent_Data(),
		   source,
		   new String[] {
			   "documentation", "The deserialized EObject"
		   });
		addAnnotation
		  (getCloudEvent_Time(),
		   source,
		   new String[] {
			   "documentation", "Time of arrival unless the payload carries one"
		   });
	}

	/**
	 * Initializes the annotations for <b>http:///org/eclipse/emf/ecore/util/ExtendedMetaData</b>.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	protected void createExtendedMetaDataAnnotations() {
		String source = "http:///org/eclipse/emf/ecore/util/ExtendedMetaData";
		addAnnotation
		  (getCloudEvent_DataSchema(),
		   source,
		   new String[] {
			   "name", "dataschema"
		   });
		addAnnotation
		  (getCloudEvent_DataContentType(),
		   source,
		   new String[] {
			   "name", "datacontenttype"
		   });
	}

} //CloudEventsPackageImpl
