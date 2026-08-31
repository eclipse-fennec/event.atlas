/*
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
package org.eclipse.fennec.event.atlas.model.inference.chat.result;


import org.eclipse.emf.ecore.EAttribute;
import org.eclipse.emf.ecore.EClass;
import org.eclipse.emf.ecore.EEnum;

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
 * @see org.eclipse.fennec.event.atlas.model.inference.chat.result.InferenceResultFactory
 * @model kind="package"
 *        annotation="http://www.eclipse.org/OCL/Import ecore='http://www.eclipse.org/emf/2002/Ecore'"
 * @generated
 */
@ProviderType
@EPackage(uri = InferenceResultPackage.eNS_URI, fingerprint = "fp1:2cc07b5af547c49046dfa513b97c602833497290e126964498e014e3c6d69fd9", genModel = "/model/inference.genmodel", genModelSourceLocations = {"model/inference.genmodel","org.eclipse.fennec.event.atlas.model.inference.chat/model/inference.genmodel"}, ecore = "/model/inference.ecore", ecoreSourceLocations = "/model/inference.ecore")
public interface InferenceResultPackage extends org.eclipse.emf.ecore.EPackage {
	/**
	 * The package name.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	String eNAME = "result";

	/**
	 * The package namespace URI.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	String eNS_URI = "https://fennec.eclipse.org/event.atlas/inference/1.0";

	/**
	 * The package namespace name.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	String eNS_PREFIX = "inference";

	/**
	 * The singleton instance of the package.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	InferenceResultPackage eINSTANCE = org.eclipse.fennec.event.atlas.model.inference.chat.result.impl.InferenceResultPackageImpl.init();

	/**
	 * The meta object id for the '{@link org.eclipse.fennec.event.atlas.model.inference.chat.result.impl.InferenceResultImpl <em>Inference Result</em>}' class.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see org.eclipse.fennec.event.atlas.model.inference.chat.result.impl.InferenceResultImpl
	 * @see org.eclipse.fennec.event.atlas.model.inference.chat.result.impl.InferenceResultPackageImpl#getInferenceResult()
	 * @generated
	 */
	int INFERENCE_RESULT = 0;

	/**
	 * The feature id for the '<em><b>Status</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int INFERENCE_RESULT__STATUS = 0;

	/**
	 * The feature id for the '<em><b>Ns Uri</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int INFERENCE_RESULT__NS_URI = 1;

	/**
	 * The feature id for the '<em><b>Message</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int INFERENCE_RESULT__MESSAGE = 2;

	/**
	 * The number of structural features of the '<em>Inference Result</em>' class.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int INFERENCE_RESULT_FEATURE_COUNT = 3;

	/**
	 * The number of operations of the '<em>Inference Result</em>' class.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int INFERENCE_RESULT_OPERATION_COUNT = 0;

	/**
	 * The meta object id for the '{@link org.eclipse.fennec.event.atlas.model.inference.chat.result.InferenceStatus <em>Inference Status</em>}' enum.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see org.eclipse.fennec.event.atlas.model.inference.chat.result.InferenceStatus
	 * @see org.eclipse.fennec.event.atlas.model.inference.chat.result.impl.InferenceResultPackageImpl#getInferenceStatus()
	 * @generated
	 */
	int INFERENCE_STATUS = 1;


	/**
	 * Returns the meta object for class '{@link org.eclipse.fennec.event.atlas.model.inference.chat.result.InferenceResult <em>Inference Result</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for class '<em>Inference Result</em>'.
	 * @see org.eclipse.fennec.event.atlas.model.inference.chat.result.InferenceResult
	 * @generated
	 */
	EClass getInferenceResult();

	/**
	 * Returns the meta object for the attribute '{@link org.eclipse.fennec.event.atlas.model.inference.chat.result.InferenceResult#getStatus <em>Status</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the attribute '<em>Status</em>'.
	 * @see org.eclipse.fennec.event.atlas.model.inference.chat.result.InferenceResult#getStatus()
	 * @see #getInferenceResult()
	 * @generated
	 */
	EAttribute getInferenceResult_Status();

	/**
	 * Returns the meta object for the attribute '{@link org.eclipse.fennec.event.atlas.model.inference.chat.result.InferenceResult#getNsUri <em>Ns Uri</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the attribute '<em>Ns Uri</em>'.
	 * @see org.eclipse.fennec.event.atlas.model.inference.chat.result.InferenceResult#getNsUri()
	 * @see #getInferenceResult()
	 * @generated
	 */
	EAttribute getInferenceResult_NsUri();

	/**
	 * Returns the meta object for the attribute '{@link org.eclipse.fennec.event.atlas.model.inference.chat.result.InferenceResult#getMessage <em>Message</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the attribute '<em>Message</em>'.
	 * @see org.eclipse.fennec.event.atlas.model.inference.chat.result.InferenceResult#getMessage()
	 * @see #getInferenceResult()
	 * @generated
	 */
	EAttribute getInferenceResult_Message();

	/**
	 * Returns the meta object for enum '{@link org.eclipse.fennec.event.atlas.model.inference.chat.result.InferenceStatus <em>Inference Status</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for enum '<em>Inference Status</em>'.
	 * @see org.eclipse.fennec.event.atlas.model.inference.chat.result.InferenceStatus
	 * @generated
	 */
	EEnum getInferenceStatus();

	/**
	 * Returns the factory that creates the instances of the model.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the factory that creates the instances of the model.
	 * @generated
	 */
	InferenceResultFactory getInferenceResultFactory();

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
		 * The meta object literal for the '{@link org.eclipse.fennec.event.atlas.model.inference.chat.result.impl.InferenceResultImpl <em>Inference Result</em>}' class.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @see org.eclipse.fennec.event.atlas.model.inference.chat.result.impl.InferenceResultImpl
		 * @see org.eclipse.fennec.event.atlas.model.inference.chat.result.impl.InferenceResultPackageImpl#getInferenceResult()
		 * @generated
		 */
		EClass INFERENCE_RESULT = eINSTANCE.getInferenceResult();

		/**
		 * The meta object literal for the '<em><b>Status</b></em>' attribute feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EAttribute INFERENCE_RESULT__STATUS = eINSTANCE.getInferenceResult_Status();

		/**
		 * The meta object literal for the '<em><b>Ns Uri</b></em>' attribute feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EAttribute INFERENCE_RESULT__NS_URI = eINSTANCE.getInferenceResult_NsUri();

		/**
		 * The meta object literal for the '<em><b>Message</b></em>' attribute feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EAttribute INFERENCE_RESULT__MESSAGE = eINSTANCE.getInferenceResult_Message();

		/**
		 * The meta object literal for the '{@link org.eclipse.fennec.event.atlas.model.inference.chat.result.InferenceStatus <em>Inference Status</em>}' enum.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @see org.eclipse.fennec.event.atlas.model.inference.chat.result.InferenceStatus
		 * @see org.eclipse.fennec.event.atlas.model.inference.chat.result.impl.InferenceResultPackageImpl#getInferenceStatus()
		 * @generated
		 */
		EEnum INFERENCE_STATUS = eINSTANCE.getInferenceStatus();

	}

} //InferenceResultPackage
