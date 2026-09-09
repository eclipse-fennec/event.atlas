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

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.eclipse.emf.common.util.Enumerator;

import org.osgi.annotation.versioning.ProviderType;

/**
 * <!-- begin-user-doc -->
 * A representation of the literals of the enumeration '<em><b>Inference Status</b></em>',
 * and utility methods for working with them.
 * <!-- end-user-doc -->
 * <!-- begin-model-doc -->
 * The four things that can become of an inference run, as the agent sees them. A run that never reached the agent at all is not in here - the runtime records that itself.
 * <!-- end-model-doc -->
 * @see org.eclipse.fennec.event.atlas.model.inference.chat.result.InferenceResultPackage#getInferenceStatus()
 * @model
 * @generated
 */
@ProviderType
public enum InferenceStatus implements Enumerator {
	/**
	 * The '<em><b>PUBLISHED</b></em>' literal object.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * <!-- begin-model-doc -->
	 * You authored a package and published it as a draft. Give its nsURI.
	 * <!-- end-model-doc -->
	 * @see #PUBLISHED_VALUE
	 * @generated
	 * @ordered
	 */
	PUBLISHED(0, "PUBLISHED", "PUBLISHED"),

	/**
	 * The '<em><b>ALREADY EXISTS</b></em>' literal object.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * <!-- begin-model-doc -->
	 * A draft for that namespace was already there, so you published nothing. Normal, not a failure: it is waiting for review. Give its nsURI.
	 * <!-- end-model-doc -->
	 * @see #ALREADY_EXISTS_VALUE
	 * @generated
	 * @ordered
	 */
	ALREADY_EXISTS(1, "ALREADY_EXISTS", "ALREADY_EXISTS"),

	/**
	 * The '<em><b>NOT PUBLISHED</b></em>' literal object.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * <!-- begin-model-doc -->
	 * You authored a model but could not publish it - a tool failed, a namespace was refused, something outside your control. Say what in the message: this is the one outcome the runtime will try again.
	 * <!-- end-model-doc -->
	 * @see #NOT_PUBLISHED_VALUE
	 * @generated
	 * @ordered
	 */
	NOT_PUBLISHED(2, "NOT_PUBLISHED", "NOT_PUBLISHED"),

	/**
	 * The '<em><b>NOT INFERRED</b></em>' literal object.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * <!-- begin-model-doc -->
	 * You did not author a model, and that was your judgement rather than an accident - the payloads are not a sensor reading, or they are too thin to model honestly. Say which in the message.
	 * <!-- end-model-doc -->
	 * @see #NOT_INFERRED_VALUE
	 * @generated
	 * @ordered
	 */
	NOT_INFERRED(3, "NOT_INFERRED", "NOT_INFERRED");

	/**
	 * The '<em><b>PUBLISHED</b></em>' literal value.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * <!-- begin-model-doc -->
	 * You authored a package and published it as a draft. Give its nsURI.
	 * <!-- end-model-doc -->
	 * @see #PUBLISHED
	 * @model
	 * @generated
	 * @ordered
	 */
	public static final int PUBLISHED_VALUE = 0;

	/**
	 * The '<em><b>ALREADY EXISTS</b></em>' literal value.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * <!-- begin-model-doc -->
	 * A draft for that namespace was already there, so you published nothing. Normal, not a failure: it is waiting for review. Give its nsURI.
	 * <!-- end-model-doc -->
	 * @see #ALREADY_EXISTS
	 * @model
	 * @generated
	 * @ordered
	 */
	public static final int ALREADY_EXISTS_VALUE = 1;

	/**
	 * The '<em><b>NOT PUBLISHED</b></em>' literal value.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * <!-- begin-model-doc -->
	 * You authored a model but could not publish it - a tool failed, a namespace was refused, something outside your control. Say what in the message: this is the one outcome the runtime will try again.
	 * <!-- end-model-doc -->
	 * @see #NOT_PUBLISHED
	 * @model
	 * @generated
	 * @ordered
	 */
	public static final int NOT_PUBLISHED_VALUE = 2;

	/**
	 * The '<em><b>NOT INFERRED</b></em>' literal value.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * <!-- begin-model-doc -->
	 * You did not author a model, and that was your judgement rather than an accident - the payloads are not a sensor reading, or they are too thin to model honestly. Say which in the message.
	 * <!-- end-model-doc -->
	 * @see #NOT_INFERRED
	 * @model
	 * @generated
	 * @ordered
	 */
	public static final int NOT_INFERRED_VALUE = 3;

	/**
	 * An array of all the '<em><b>Inference Status</b></em>' enumerators.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	private static final InferenceStatus[] VALUES_ARRAY =
		new InferenceStatus[] {
			PUBLISHED,
			ALREADY_EXISTS,
			NOT_PUBLISHED,
			NOT_INFERRED,
		};

	/**
	 * A public read-only list of all the '<em><b>Inference Status</b></em>' enumerators.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public static final List<InferenceStatus> VALUES = Collections.unmodifiableList(Arrays.asList(VALUES_ARRAY));

	/**
	 * Returns the '<em><b>Inference Status</b></em>' literal with the specified literal value.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @param literal the literal.
	 * @return the matching enumerator or <code>null</code>.
	 * @generated
	 */
	public static InferenceStatus get(String literal) {
		for (int i = 0; i < VALUES_ARRAY.length; ++i) {
			InferenceStatus result = VALUES_ARRAY[i];
			if (result.toString().equals(literal)) {
				return result;
			}
		}
		return null;
	}

	/**
	 * Returns the '<em><b>Inference Status</b></em>' literal with the specified name.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @param name the name.
	 * @return the matching enumerator or <code>null</code>.
	 * @generated
	 */
	public static InferenceStatus getByName(String name) {
		for (int i = 0; i < VALUES_ARRAY.length; ++i) {
			InferenceStatus result = VALUES_ARRAY[i];
			if (result.getName().equals(name)) {
				return result;
			}
		}
		return null;
	}

	/**
	 * Returns the '<em><b>Inference Status</b></em>' literal with the specified integer value.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @param value the integer value.
	 * @return the matching enumerator or <code>null</code>.
	 * @generated
	 */
	public static InferenceStatus get(int value) {
		switch (value) {
			case PUBLISHED_VALUE: return PUBLISHED;
			case ALREADY_EXISTS_VALUE: return ALREADY_EXISTS;
			case NOT_PUBLISHED_VALUE: return NOT_PUBLISHED;
			case NOT_INFERRED_VALUE: return NOT_INFERRED;
		}
		return null;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	private final int value;

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	private final String name;

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	private final String literal;

	/**
	 * Only this class can construct instances.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	private InferenceStatus(int value, String name, String literal) {
		this.value = value;
		this.name = name;
		this.literal = literal;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public int getValue() {
	  return value;
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
	public String getLiteral() {
	  return literal;
	}

	/**
	 * Returns the literal value of the enumerator, which is its string representation.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public String toString() {
		return literal;
	}
	
} //InferenceStatus
