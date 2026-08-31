/**
 * Copyright (c) 2012 - 2025 Data In Motion and others.
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
package org.eclipse.fennec.event.atlas.model.inference.chat.result.impl;

import org.eclipse.emf.ecore.EAttribute;
import org.eclipse.emf.ecore.EClass;
import org.eclipse.emf.ecore.EEnum;
import org.eclipse.emf.ecore.EPackage;

import org.eclipse.emf.ecore.impl.EPackageImpl;

import org.eclipse.fennec.event.atlas.model.inference.chat.result.InferenceResult;
import org.eclipse.fennec.event.atlas.model.inference.chat.result.InferenceResultFactory;
import org.eclipse.fennec.event.atlas.model.inference.chat.result.InferenceResultPackage;
import org.eclipse.fennec.event.atlas.model.inference.chat.result.InferenceStatus;

/**
 * <!-- begin-user-doc -->
 * An implementation of the model <b>Package</b>.
 * <!-- end-user-doc -->
 * @generated
 */
public class InferenceResultPackageImpl extends EPackageImpl implements InferenceResultPackage {
	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	private EClass inferenceResultEClass = null;

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	private EEnum inferenceStatusEEnum = null;

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
	 * @see org.eclipse.fennec.event.atlas.model.inference.chat.result.InferenceResultPackage#eNS_URI
	 * @see #init()
	 * @generated
	 */
	private InferenceResultPackageImpl() {
		super(eNS_URI, InferenceResultFactory.eINSTANCE);
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
	 * <p>This method is used to initialize {@link InferenceResultPackage#eINSTANCE} when that field is accessed.
	 * Clients should not invoke it directly. Instead, they should simply access that field to obtain the package.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #eNS_URI
	 * @see #createPackageContents()
	 * @see #initializePackageContents()
	 * @generated
	 */
	public static InferenceResultPackage init() {
		if (isInited) return (InferenceResultPackage)EPackage.Registry.INSTANCE.getEPackage(InferenceResultPackage.eNS_URI);

		// Obtain or create and register package
		Object registeredInferenceResultPackage = EPackage.Registry.INSTANCE.get(eNS_URI);
		InferenceResultPackageImpl theInferenceResultPackage = registeredInferenceResultPackage instanceof InferenceResultPackageImpl ? (InferenceResultPackageImpl)registeredInferenceResultPackage : new InferenceResultPackageImpl();

		isInited = true;

		// Create package meta-data objects
		theInferenceResultPackage.createPackageContents();

		// Initialize created meta-data
		theInferenceResultPackage.initializePackageContents();

		// Mark meta-data to indicate it can't be changed
		theInferenceResultPackage.freeze();

		// Update the registry and return the package
		EPackage.Registry.INSTANCE.put(InferenceResultPackage.eNS_URI, theInferenceResultPackage);
		return theInferenceResultPackage;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EClass getInferenceResult() {
		return inferenceResultEClass;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EAttribute getInferenceResult_Status() {
		return (EAttribute)inferenceResultEClass.getEStructuralFeatures().get(0);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EAttribute getInferenceResult_NsUri() {
		return (EAttribute)inferenceResultEClass.getEStructuralFeatures().get(1);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EAttribute getInferenceResult_Message() {
		return (EAttribute)inferenceResultEClass.getEStructuralFeatures().get(2);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EEnum getInferenceStatus() {
		return inferenceStatusEEnum;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public InferenceResultFactory getInferenceResultFactory() {
		return (InferenceResultFactory)getEFactoryInstance();
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
		inferenceResultEClass = createEClass(INFERENCE_RESULT);
		createEAttribute(inferenceResultEClass, INFERENCE_RESULT__STATUS);
		createEAttribute(inferenceResultEClass, INFERENCE_RESULT__NS_URI);
		createEAttribute(inferenceResultEClass, INFERENCE_RESULT__MESSAGE);

		// Create enums
		inferenceStatusEEnum = createEEnum(INFERENCE_STATUS);
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

		// Initialize classes, features, and operations; add parameters
		initEClass(inferenceResultEClass, InferenceResult.class, "InferenceResult", !IS_ABSTRACT, !IS_INTERFACE, IS_GENERATED_INSTANCE_CLASS);
		initEAttribute(getInferenceResult_Status(), this.getInferenceStatus(), "status", null, 1, 1, InferenceResult.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
		initEAttribute(getInferenceResult_NsUri(), ecorePackage.getEString(), "nsUri", null, 0, 1, InferenceResult.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
		initEAttribute(getInferenceResult_Message(), ecorePackage.getEString(), "message", null, 0, 1, InferenceResult.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);

		// Initialize enums and add enum literals
		initEEnum(inferenceStatusEEnum, InferenceStatus.class, "InferenceStatus");
		addEEnumLiteral(inferenceStatusEEnum, InferenceStatus.PUBLISHED);
		addEEnumLiteral(inferenceStatusEEnum, InferenceStatus.ALREADY_EXISTS);
		addEEnumLiteral(inferenceStatusEEnum, InferenceStatus.NOT_PUBLISHED);
		addEEnumLiteral(inferenceStatusEEnum, InferenceStatus.NOT_INFERRED);

		// Create resource
		createResource(eNS_URI);

		// Create annotations
		// http://www.eclipse.org/OCL/Import
		createImportAnnotations();
		// http://www.eclipse.org/emf/2002/ExtendedMetaData
		createExtendedMetaDataAnnotations();
	}

	/**
	 * Initializes the annotations for <b>http://www.eclipse.org/OCL/Import</b>.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	protected void createImportAnnotations() {
		String source = "http://www.eclipse.org/OCL/Import";
		addAnnotation
		  (this,
		   source,
		   new String[] {
			   "ecore", "http://www.eclipse.org/emf/2002/Ecore"
		   });
	}

	/**
	 * Initializes the annotations for <b>http://www.eclipse.org/emf/2002/ExtendedMetaData</b>.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	protected void createExtendedMetaDataAnnotations() {
		String source = "http://www.eclipse.org/emf/2002/ExtendedMetaData";
		addAnnotation
		  (getInferenceResult_NsUri(),
		   source,
		   new String[] {
			   "name", "ns_uri"
		   });
	}

} //InferenceResultPackageImpl
