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

import org.eclipse.emf.ecore.EObject;

import org.osgi.annotation.versioning.ProviderType;

/**
 * <!-- begin-user-doc -->
 * A representation of the model object '<em><b>Inference Result</b></em>'.
 * <!-- end-user-doc -->
 *
 * <!-- begin-model-doc -->
 * What an inference run did. This is the structured answer the agent returns instead of a line of prose, and it is the only record the runtime has of where a draft went.
 * <!-- end-model-doc -->
 *
 * <p>
 * The following features are supported:
 * </p>
 * <ul>
 *   <li>{@link org.eclipse.fennec.event.atlas.model.inference.chat.result.InferenceResult#getStatus <em>Status</em>}</li>
 *   <li>{@link org.eclipse.fennec.event.atlas.model.inference.chat.result.InferenceResult#getNsUri <em>Ns Uri</em>}</li>
 *   <li>{@link org.eclipse.fennec.event.atlas.model.inference.chat.result.InferenceResult#getMessage <em>Message</em>}</li>
 * </ul>
 *
 * @see org.eclipse.fennec.event.atlas.model.inference.chat.result.InferenceResultPackage#getInferenceResult()
 * @model
 * @generated
 */
@ProviderType
public interface InferenceResult extends EObject {
	/**
	 * Returns the value of the '<em><b>Status</b></em>' attribute.
	 * The literals are from the enumeration {@link org.eclipse.fennec.event.atlas.model.inference.chat.result.InferenceStatus}.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * <!-- begin-model-doc -->
	 * What became of the model. Always required, even when the run went badly.
	 * <!-- end-model-doc -->
	 * @return the value of the '<em>Status</em>' attribute.
	 * @see org.eclipse.fennec.event.atlas.model.inference.chat.result.InferenceStatus
	 * @see #setStatus(InferenceStatus)
	 * @see org.eclipse.fennec.event.atlas.model.inference.chat.result.InferenceResultPackage#getInferenceResult_Status()
	 * @model required="true"
	 * @generated
	 */
	InferenceStatus getStatus();

	/**
	 * Sets the value of the '{@link org.eclipse.fennec.event.atlas.model.inference.chat.result.InferenceResult#getStatus <em>Status</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @param value the new value of the '<em>Status</em>' attribute.
	 * @see org.eclipse.fennec.event.atlas.model.inference.chat.result.InferenceStatus
	 * @see #getStatus()
	 * @generated
	 */
	void setStatus(InferenceStatus value);

	/**
	 * Returns the value of the '<em><b>Ns Uri</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * <!-- begin-model-doc -->
	 * The nsURI of the package you authored, exactly as you registered it. Required for PUBLISHED and ALREADY_EXISTS, and give it for NOT_PUBLISHED too if you got as far as choosing one - nothing else tells a reviewer which model this was about.
	 * <!-- end-model-doc -->
	 * @return the value of the '<em>Ns Uri</em>' attribute.
	 * @see #setNsUri(String)
	 * @see org.eclipse.fennec.event.atlas.model.inference.chat.result.InferenceResultPackage#getInferenceResult_NsUri()
	 * @model annotation="http://www.eclipse.org/emf/2002/ExtendedMetaData name='ns_uri'"
	 * @generated
	 */
	String getNsUri();

	/**
	 * Sets the value of the '{@link org.eclipse.fennec.event.atlas.model.inference.chat.result.InferenceResult#getNsUri <em>Ns Uri</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @param value the new value of the '<em>Ns Uri</em>' attribute.
	 * @see #getNsUri()
	 * @generated
	 */
	void setNsUri(String value);

	/**
	 * Returns the value of the '<em><b>Message</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * <!-- begin-model-doc -->
	 * One sentence for a human reading the log. For PUBLISHED, what the model covers; for anything else, what stopped you, specifically enough to act on.
	 * <!-- end-model-doc -->
	 * @return the value of the '<em>Message</em>' attribute.
	 * @see #setMessage(String)
	 * @see org.eclipse.fennec.event.atlas.model.inference.chat.result.InferenceResultPackage#getInferenceResult_Message()
	 * @model
	 * @generated
	 */
	String getMessage();

	/**
	 * Sets the value of the '{@link org.eclipse.fennec.event.atlas.model.inference.chat.result.InferenceResult#getMessage <em>Message</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @param value the new value of the '<em>Message</em>' attribute.
	 * @see #getMessage()
	 * @generated
	 */
	void setMessage(String value);

} // InferenceResult
