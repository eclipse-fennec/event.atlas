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
package org.eclipse.fennec.event.atlas.model.mapping.impl;

import java.util.Collection;

import org.eclipse.emf.common.notify.Notification;

import org.eclipse.emf.common.util.EList;

import org.eclipse.emf.ecore.EClass;

import org.eclipse.emf.ecore.impl.ENotificationImpl;
import org.eclipse.emf.ecore.impl.MinimalEObjectImpl;

import org.eclipse.emf.ecore.util.EDataTypeUniqueEList;

import org.eclipse.fennec.event.atlas.model.mapping.MappingPackage;
import org.eclipse.fennec.event.atlas.model.mapping.MappingSelector;

/**
 * <!-- begin-user-doc -->
 * An implementation of the model object '<em><b>Selector</b></em>'.
 * <!-- end-user-doc -->
 * <p>
 * The following features are implemented:
 * </p>
 * <ul>
 *   <li>{@link org.eclipse.fennec.event.atlas.model.mapping.impl.MappingSelectorImpl#getSource <em>Source</em>}</li>
 *   <li>{@link org.eclipse.fennec.event.atlas.model.mapping.impl.MappingSelectorImpl#getSourceExcludes <em>Source Excludes</em>}</li>
 *   <li>{@link org.eclipse.fennec.event.atlas.model.mapping.impl.MappingSelectorImpl#getType <em>Type</em>}</li>
 *   <li>{@link org.eclipse.fennec.event.atlas.model.mapping.impl.MappingSelectorImpl#getDataschema <em>Dataschema</em>}</li>
 * </ul>
 *
 * @generated
 */
public class MappingSelectorImpl extends MinimalEObjectImpl.Container implements MappingSelector {
	/**
	 * The cached value of the '{@link #getSource() <em>Source</em>}' attribute list.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getSource()
	 * @generated
	 * @ordered
	 */
	protected EList<String> source;

	/**
	 * The cached value of the '{@link #getSourceExcludes() <em>Source Excludes</em>}' attribute list.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getSourceExcludes()
	 * @generated
	 * @ordered
	 */
	protected EList<String> sourceExcludes;

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
	 * The default value of the '{@link #getDataschema() <em>Dataschema</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getDataschema()
	 * @generated
	 * @ordered
	 */
	protected static final String DATASCHEMA_EDEFAULT = null;

	/**
	 * The cached value of the '{@link #getDataschema() <em>Dataschema</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getDataschema()
	 * @generated
	 * @ordered
	 */
	protected String dataschema = DATASCHEMA_EDEFAULT;

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	protected MappingSelectorImpl() {
		super();
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	protected EClass eStaticClass() {
		return MappingPackage.Literals.MAPPING_SELECTOR;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EList<String> getSource() {
		if (source == null) {
			source = new EDataTypeUniqueEList<String>(String.class, this, MappingPackage.MAPPING_SELECTOR__SOURCE);
		}
		return source;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EList<String> getSourceExcludes() {
		if (sourceExcludes == null) {
			sourceExcludes = new EDataTypeUniqueEList<String>(String.class, this, MappingPackage.MAPPING_SELECTOR__SOURCE_EXCLUDES);
		}
		return sourceExcludes;
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
			eNotify(new ENotificationImpl(this, Notification.SET, MappingPackage.MAPPING_SELECTOR__TYPE, oldType, type));
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public String getDataschema() {
		return dataschema;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public void setDataschema(String newDataschema) {
		String oldDataschema = dataschema;
		dataschema = newDataschema;
		if (eNotificationRequired())
			eNotify(new ENotificationImpl(this, Notification.SET, MappingPackage.MAPPING_SELECTOR__DATASCHEMA, oldDataschema, dataschema));
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public Object eGet(int featureID, boolean resolve, boolean coreType) {
		switch (featureID) {
			case MappingPackage.MAPPING_SELECTOR__SOURCE:
				return getSource();
			case MappingPackage.MAPPING_SELECTOR__SOURCE_EXCLUDES:
				return getSourceExcludes();
			case MappingPackage.MAPPING_SELECTOR__TYPE:
				return getType();
			case MappingPackage.MAPPING_SELECTOR__DATASCHEMA:
				return getDataschema();
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
			case MappingPackage.MAPPING_SELECTOR__SOURCE:
				getSource().clear();
				getSource().addAll((Collection<? extends String>)newValue);
				return;
			case MappingPackage.MAPPING_SELECTOR__SOURCE_EXCLUDES:
				getSourceExcludes().clear();
				getSourceExcludes().addAll((Collection<? extends String>)newValue);
				return;
			case MappingPackage.MAPPING_SELECTOR__TYPE:
				setType((String)newValue);
				return;
			case MappingPackage.MAPPING_SELECTOR__DATASCHEMA:
				setDataschema((String)newValue);
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
			case MappingPackage.MAPPING_SELECTOR__SOURCE:
				getSource().clear();
				return;
			case MappingPackage.MAPPING_SELECTOR__SOURCE_EXCLUDES:
				getSourceExcludes().clear();
				return;
			case MappingPackage.MAPPING_SELECTOR__TYPE:
				setType(TYPE_EDEFAULT);
				return;
			case MappingPackage.MAPPING_SELECTOR__DATASCHEMA:
				setDataschema(DATASCHEMA_EDEFAULT);
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
			case MappingPackage.MAPPING_SELECTOR__SOURCE:
				return source != null && !source.isEmpty();
			case MappingPackage.MAPPING_SELECTOR__SOURCE_EXCLUDES:
				return sourceExcludes != null && !sourceExcludes.isEmpty();
			case MappingPackage.MAPPING_SELECTOR__TYPE:
				return TYPE_EDEFAULT == null ? type != null : !TYPE_EDEFAULT.equals(type);
			case MappingPackage.MAPPING_SELECTOR__DATASCHEMA:
				return DATASCHEMA_EDEFAULT == null ? dataschema != null : !DATASCHEMA_EDEFAULT.equals(dataschema);
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
		result.append(" (source: ");
		result.append(source);
		result.append(", sourceExcludes: ");
		result.append(sourceExcludes);
		result.append(", type: ");
		result.append(type);
		result.append(", dataschema: ");
		result.append(dataschema);
		result.append(')');
		return result.toString();
	}

} //MappingSelectorImpl
