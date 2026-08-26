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
import org.eclipse.emf.common.notify.NotificationChain;

import org.eclipse.emf.common.util.EList;

import org.eclipse.emf.ecore.EAttribute;
import org.eclipse.emf.ecore.EClass;
import org.eclipse.emf.ecore.InternalEObject;

import org.eclipse.emf.ecore.impl.ENotificationImpl;
import org.eclipse.emf.ecore.impl.MinimalEObjectImpl;

import org.eclipse.emf.ecore.util.EObjectResolvingEList;

import org.eclipse.fennec.event.atlas.model.mapping.ChangeRule;
import org.eclipse.fennec.event.atlas.model.mapping.DeletionRule;
import org.eclipse.fennec.event.atlas.model.mapping.MappingPackage;
import org.eclipse.fennec.event.atlas.model.mapping.ReferenceResourceBinding;

/**
 * <!-- begin-user-doc -->
 * An implementation of the model object '<em><b>Reference Resource Binding</b></em>'.
 * <!-- end-user-doc -->
 * <p>
 * The following features are implemented:
 * </p>
 * <ul>
 *   <li>{@link org.eclipse.fennec.event.atlas.model.mapping.impl.ReferenceResourceBindingImpl#getAttributes <em>Attributes</em>}</li>
 *   <li>{@link org.eclipse.fennec.event.atlas.model.mapping.impl.ReferenceResourceBindingImpl#getChangeRule <em>Change Rule</em>}</li>
 *   <li>{@link org.eclipse.fennec.event.atlas.model.mapping.impl.ReferenceResourceBindingImpl#getDeletionRule <em>Deletion Rule</em>}</li>
 *   <li>{@link org.eclipse.fennec.event.atlas.model.mapping.impl.ReferenceResourceBindingImpl#getUnit <em>Unit</em>}</li>
 * </ul>
 *
 * @generated
 */
public class ReferenceResourceBindingImpl extends MinimalEObjectImpl.Container implements ReferenceResourceBinding {
	/**
	 * The cached value of the '{@link #getAttributes() <em>Attributes</em>}' reference list.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getAttributes()
	 * @generated
	 * @ordered
	 */
	protected EList<EAttribute> attributes;

	/**
	 * The cached value of the '{@link #getChangeRule() <em>Change Rule</em>}' containment reference.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getChangeRule()
	 * @generated
	 * @ordered
	 */
	protected ChangeRule changeRule;

	/**
	 * The cached value of the '{@link #getDeletionRule() <em>Deletion Rule</em>}' containment reference.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getDeletionRule()
	 * @generated
	 * @ordered
	 */
	protected DeletionRule deletionRule;

	/**
	 * The default value of the '{@link #getUnit() <em>Unit</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getUnit()
	 * @generated
	 * @ordered
	 */
	protected static final String UNIT_EDEFAULT = null;

	/**
	 * The cached value of the '{@link #getUnit() <em>Unit</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getUnit()
	 * @generated
	 * @ordered
	 */
	protected String unit = UNIT_EDEFAULT;

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	protected ReferenceResourceBindingImpl() {
		super();
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	protected EClass eStaticClass() {
		return MappingPackage.Literals.REFERENCE_RESOURCE_BINDING;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EList<EAttribute> getAttributes() {
		if (attributes == null) {
			attributes = new EObjectResolvingEList<EAttribute>(EAttribute.class, this, MappingPackage.REFERENCE_RESOURCE_BINDING__ATTRIBUTES);
		}
		return attributes;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public ChangeRule getChangeRule() {
		return changeRule;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public NotificationChain basicSetChangeRule(ChangeRule newChangeRule, NotificationChain msgs) {
		ChangeRule oldChangeRule = changeRule;
		changeRule = newChangeRule;
		if (eNotificationRequired()) {
			ENotificationImpl notification = new ENotificationImpl(this, Notification.SET, MappingPackage.REFERENCE_RESOURCE_BINDING__CHANGE_RULE, oldChangeRule, newChangeRule);
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
	public void setChangeRule(ChangeRule newChangeRule) {
		if (newChangeRule != changeRule) {
			NotificationChain msgs = null;
			if (changeRule != null)
				msgs = ((InternalEObject)changeRule).eInverseRemove(this, EOPPOSITE_FEATURE_BASE - MappingPackage.REFERENCE_RESOURCE_BINDING__CHANGE_RULE, null, msgs);
			if (newChangeRule != null)
				msgs = ((InternalEObject)newChangeRule).eInverseAdd(this, EOPPOSITE_FEATURE_BASE - MappingPackage.REFERENCE_RESOURCE_BINDING__CHANGE_RULE, null, msgs);
			msgs = basicSetChangeRule(newChangeRule, msgs);
			if (msgs != null) msgs.dispatch();
		}
		else if (eNotificationRequired())
			eNotify(new ENotificationImpl(this, Notification.SET, MappingPackage.REFERENCE_RESOURCE_BINDING__CHANGE_RULE, newChangeRule, newChangeRule));
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public DeletionRule getDeletionRule() {
		return deletionRule;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public NotificationChain basicSetDeletionRule(DeletionRule newDeletionRule, NotificationChain msgs) {
		DeletionRule oldDeletionRule = deletionRule;
		deletionRule = newDeletionRule;
		if (eNotificationRequired()) {
			ENotificationImpl notification = new ENotificationImpl(this, Notification.SET, MappingPackage.REFERENCE_RESOURCE_BINDING__DELETION_RULE, oldDeletionRule, newDeletionRule);
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
	public void setDeletionRule(DeletionRule newDeletionRule) {
		if (newDeletionRule != deletionRule) {
			NotificationChain msgs = null;
			if (deletionRule != null)
				msgs = ((InternalEObject)deletionRule).eInverseRemove(this, EOPPOSITE_FEATURE_BASE - MappingPackage.REFERENCE_RESOURCE_BINDING__DELETION_RULE, null, msgs);
			if (newDeletionRule != null)
				msgs = ((InternalEObject)newDeletionRule).eInverseAdd(this, EOPPOSITE_FEATURE_BASE - MappingPackage.REFERENCE_RESOURCE_BINDING__DELETION_RULE, null, msgs);
			msgs = basicSetDeletionRule(newDeletionRule, msgs);
			if (msgs != null) msgs.dispatch();
		}
		else if (eNotificationRequired())
			eNotify(new ENotificationImpl(this, Notification.SET, MappingPackage.REFERENCE_RESOURCE_BINDING__DELETION_RULE, newDeletionRule, newDeletionRule));
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public String getUnit() {
		return unit;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public void setUnit(String newUnit) {
		String oldUnit = unit;
		unit = newUnit;
		if (eNotificationRequired())
			eNotify(new ENotificationImpl(this, Notification.SET, MappingPackage.REFERENCE_RESOURCE_BINDING__UNIT, oldUnit, unit));
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public NotificationChain eInverseRemove(InternalEObject otherEnd, int featureID, NotificationChain msgs) {
		switch (featureID) {
			case MappingPackage.REFERENCE_RESOURCE_BINDING__CHANGE_RULE:
				return basicSetChangeRule(null, msgs);
			case MappingPackage.REFERENCE_RESOURCE_BINDING__DELETION_RULE:
				return basicSetDeletionRule(null, msgs);
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
			case MappingPackage.REFERENCE_RESOURCE_BINDING__ATTRIBUTES:
				return getAttributes();
			case MappingPackage.REFERENCE_RESOURCE_BINDING__CHANGE_RULE:
				return getChangeRule();
			case MappingPackage.REFERENCE_RESOURCE_BINDING__DELETION_RULE:
				return getDeletionRule();
			case MappingPackage.REFERENCE_RESOURCE_BINDING__UNIT:
				return getUnit();
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
			case MappingPackage.REFERENCE_RESOURCE_BINDING__ATTRIBUTES:
				getAttributes().clear();
				getAttributes().addAll((Collection<? extends EAttribute>)newValue);
				return;
			case MappingPackage.REFERENCE_RESOURCE_BINDING__CHANGE_RULE:
				setChangeRule((ChangeRule)newValue);
				return;
			case MappingPackage.REFERENCE_RESOURCE_BINDING__DELETION_RULE:
				setDeletionRule((DeletionRule)newValue);
				return;
			case MappingPackage.REFERENCE_RESOURCE_BINDING__UNIT:
				setUnit((String)newValue);
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
			case MappingPackage.REFERENCE_RESOURCE_BINDING__ATTRIBUTES:
				getAttributes().clear();
				return;
			case MappingPackage.REFERENCE_RESOURCE_BINDING__CHANGE_RULE:
				setChangeRule((ChangeRule)null);
				return;
			case MappingPackage.REFERENCE_RESOURCE_BINDING__DELETION_RULE:
				setDeletionRule((DeletionRule)null);
				return;
			case MappingPackage.REFERENCE_RESOURCE_BINDING__UNIT:
				setUnit(UNIT_EDEFAULT);
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
			case MappingPackage.REFERENCE_RESOURCE_BINDING__ATTRIBUTES:
				return attributes != null && !attributes.isEmpty();
			case MappingPackage.REFERENCE_RESOURCE_BINDING__CHANGE_RULE:
				return changeRule != null;
			case MappingPackage.REFERENCE_RESOURCE_BINDING__DELETION_RULE:
				return deletionRule != null;
			case MappingPackage.REFERENCE_RESOURCE_BINDING__UNIT:
				return UNIT_EDEFAULT == null ? unit != null : !UNIT_EDEFAULT.equals(unit);
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
		result.append(" (unit: ");
		result.append(unit);
		result.append(')');
		return result.toString();
	}

} //ReferenceResourceBindingImpl
