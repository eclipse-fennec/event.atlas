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

import org.eclipse.emf.ecore.EClass;
import org.eclipse.emf.ecore.EPackage;
import org.eclipse.emf.ecore.EStructuralFeature;
import org.eclipse.emf.ecore.InternalEObject;

import org.eclipse.emf.ecore.impl.ENotificationImpl;

import org.eclipse.emf.ecore.util.EObjectResolvingEList;

import org.eclipse.fennec.event.atlas.model.mapping.AdminMapping;
import org.eclipse.fennec.event.atlas.model.mapping.FeatureMapping;
import org.eclipse.fennec.event.atlas.model.mapping.MappingPackage;

/**
 * <!-- begin-user-doc -->
 * An implementation of the model object '<em><b>Admin Mapping</b></em>'.
 * <!-- end-user-doc -->
 * <p>
 * The following features are implemented:
 * </p>
 * <ul>
 *   <li>{@link org.eclipse.fennec.event.atlas.model.mapping.impl.AdminMappingImpl#getFriendlyName <em>Friendly Name</em>}</li>
 *   <li>{@link org.eclipse.fennec.event.atlas.model.mapping.impl.AdminMappingImpl#getFriendlyNameFeature <em>Friendly Name Feature</em>}</li>
 *   <li>{@link org.eclipse.fennec.event.atlas.model.mapping.impl.AdminMappingImpl#getProviderPackage <em>Provider Package</em>}</li>
 *   <li>{@link org.eclipse.fennec.event.atlas.model.mapping.impl.AdminMappingImpl#getLatitude <em>Latitude</em>}</li>
 *   <li>{@link org.eclipse.fennec.event.atlas.model.mapping.impl.AdminMappingImpl#getLongitude <em>Longitude</em>}</li>
 *   <li>{@link org.eclipse.fennec.event.atlas.model.mapping.impl.AdminMappingImpl#getElevation <em>Elevation</em>}</li>
 *   <li>{@link org.eclipse.fennec.event.atlas.model.mapping.impl.AdminMappingImpl#getLatitudeRef <em>Latitude Ref</em>}</li>
 *   <li>{@link org.eclipse.fennec.event.atlas.model.mapping.impl.AdminMappingImpl#getLongitudeRef <em>Longitude Ref</em>}</li>
 *   <li>{@link org.eclipse.fennec.event.atlas.model.mapping.impl.AdminMappingImpl#getElevationRef <em>Elevation Ref</em>}</li>
 *   <li>{@link org.eclipse.fennec.event.atlas.model.mapping.impl.AdminMappingImpl#getFriendlyNameMapping <em>Friendly Name Mapping</em>}</li>
 *   <li>{@link org.eclipse.fennec.event.atlas.model.mapping.impl.AdminMappingImpl#getLatitudeMapping <em>Latitude Mapping</em>}</li>
 *   <li>{@link org.eclipse.fennec.event.atlas.model.mapping.impl.AdminMappingImpl#getLongitudeMapping <em>Longitude Mapping</em>}</li>
 *   <li>{@link org.eclipse.fennec.event.atlas.model.mapping.impl.AdminMappingImpl#getElevationMapping <em>Elevation Mapping</em>}</li>
 *   <li>{@link org.eclipse.fennec.event.atlas.model.mapping.impl.AdminMappingImpl#isExtendAdmin <em>Extend Admin</em>}</li>
 * </ul>
 *
 * @generated
 */
public class AdminMappingImpl extends ServiceMappingImpl implements AdminMapping {
	/**
	 * The default value of the '{@link #getFriendlyName() <em>Friendly Name</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getFriendlyName()
	 * @generated
	 * @ordered
	 */
	protected static final String FRIENDLY_NAME_EDEFAULT = null;

	/**
	 * The cached value of the '{@link #getFriendlyName() <em>Friendly Name</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getFriendlyName()
	 * @generated
	 * @ordered
	 */
	protected String friendlyName = FRIENDLY_NAME_EDEFAULT;

	/**
	 * The cached value of the '{@link #getFriendlyNameFeature() <em>Friendly Name Feature</em>}' reference list.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getFriendlyNameFeature()
	 * @generated
	 * @ordered
	 */
	protected EList<EStructuralFeature> friendlyNameFeature;

	/**
	 * The cached value of the '{@link #getProviderPackage() <em>Provider Package</em>}' reference.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getProviderPackage()
	 * @generated
	 * @ordered
	 */
	protected EPackage providerPackage;

	/**
	 * The default value of the '{@link #getLatitude() <em>Latitude</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getLatitude()
	 * @generated
	 * @ordered
	 */
	protected static final Double LATITUDE_EDEFAULT = null;

	/**
	 * The cached value of the '{@link #getLatitude() <em>Latitude</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getLatitude()
	 * @generated
	 * @ordered
	 */
	protected Double latitude = LATITUDE_EDEFAULT;

	/**
	 * The default value of the '{@link #getLongitude() <em>Longitude</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getLongitude()
	 * @generated
	 * @ordered
	 */
	protected static final Double LONGITUDE_EDEFAULT = null;

	/**
	 * The cached value of the '{@link #getLongitude() <em>Longitude</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getLongitude()
	 * @generated
	 * @ordered
	 */
	protected Double longitude = LONGITUDE_EDEFAULT;

	/**
	 * The default value of the '{@link #getElevation() <em>Elevation</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getElevation()
	 * @generated
	 * @ordered
	 */
	protected static final Double ELEVATION_EDEFAULT = null;

	/**
	 * The cached value of the '{@link #getElevation() <em>Elevation</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getElevation()
	 * @generated
	 * @ordered
	 */
	protected Double elevation = ELEVATION_EDEFAULT;

	/**
	 * The cached value of the '{@link #getLatitudeRef() <em>Latitude Ref</em>}' reference list.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getLatitudeRef()
	 * @generated
	 * @ordered
	 */
	protected EList<EStructuralFeature> latitudeRef;

	/**
	 * The cached value of the '{@link #getLongitudeRef() <em>Longitude Ref</em>}' reference list.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getLongitudeRef()
	 * @generated
	 * @ordered
	 */
	protected EList<EStructuralFeature> longitudeRef;

	/**
	 * The cached value of the '{@link #getElevationRef() <em>Elevation Ref</em>}' reference list.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getElevationRef()
	 * @generated
	 * @ordered
	 */
	protected EList<EStructuralFeature> elevationRef;

	/**
	 * The cached value of the '{@link #getFriendlyNameMapping() <em>Friendly Name Mapping</em>}' containment reference.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getFriendlyNameMapping()
	 * @generated
	 * @ordered
	 */
	protected FeatureMapping friendlyNameMapping;

	/**
	 * The cached value of the '{@link #getLatitudeMapping() <em>Latitude Mapping</em>}' containment reference.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getLatitudeMapping()
	 * @generated
	 * @ordered
	 */
	protected FeatureMapping latitudeMapping;

	/**
	 * The cached value of the '{@link #getLongitudeMapping() <em>Longitude Mapping</em>}' containment reference.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getLongitudeMapping()
	 * @generated
	 * @ordered
	 */
	protected FeatureMapping longitudeMapping;

	/**
	 * The cached value of the '{@link #getElevationMapping() <em>Elevation Mapping</em>}' containment reference.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getElevationMapping()
	 * @generated
	 * @ordered
	 */
	protected FeatureMapping elevationMapping;

	/**
	 * The default value of the '{@link #isExtendAdmin() <em>Extend Admin</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #isExtendAdmin()
	 * @generated
	 * @ordered
	 */
	protected static final boolean EXTEND_ADMIN_EDEFAULT = false;

	/**
	 * The cached value of the '{@link #isExtendAdmin() <em>Extend Admin</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #isExtendAdmin()
	 * @generated
	 * @ordered
	 */
	protected boolean extendAdmin = EXTEND_ADMIN_EDEFAULT;

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	protected AdminMappingImpl() {
		super();
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	protected EClass eStaticClass() {
		return MappingPackage.Literals.ADMIN_MAPPING;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public String getFriendlyName() {
		return friendlyName;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public void setFriendlyName(String newFriendlyName) {
		String oldFriendlyName = friendlyName;
		friendlyName = newFriendlyName;
		if (eNotificationRequired())
			eNotify(new ENotificationImpl(this, Notification.SET, MappingPackage.ADMIN_MAPPING__FRIENDLY_NAME, oldFriendlyName, friendlyName));
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EList<EStructuralFeature> getFriendlyNameFeature() {
		if (friendlyNameFeature == null) {
			friendlyNameFeature = new EObjectResolvingEList<EStructuralFeature>(EStructuralFeature.class, this, MappingPackage.ADMIN_MAPPING__FRIENDLY_NAME_FEATURE);
		}
		return friendlyNameFeature;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EPackage getProviderPackage() {
		if (providerPackage != null && providerPackage.eIsProxy()) {
			InternalEObject oldProviderPackage = (InternalEObject)providerPackage;
			providerPackage = (EPackage)eResolveProxy(oldProviderPackage);
			if (providerPackage != oldProviderPackage) {
				if (eNotificationRequired())
					eNotify(new ENotificationImpl(this, Notification.RESOLVE, MappingPackage.ADMIN_MAPPING__PROVIDER_PACKAGE, oldProviderPackage, providerPackage));
			}
		}
		return providerPackage;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public EPackage basicGetProviderPackage() {
		return providerPackage;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public void setProviderPackage(EPackage newProviderPackage) {
		EPackage oldProviderPackage = providerPackage;
		providerPackage = newProviderPackage;
		if (eNotificationRequired())
			eNotify(new ENotificationImpl(this, Notification.SET, MappingPackage.ADMIN_MAPPING__PROVIDER_PACKAGE, oldProviderPackage, providerPackage));
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public Double getLatitude() {
		return latitude;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public void setLatitude(Double newLatitude) {
		Double oldLatitude = latitude;
		latitude = newLatitude;
		if (eNotificationRequired())
			eNotify(new ENotificationImpl(this, Notification.SET, MappingPackage.ADMIN_MAPPING__LATITUDE, oldLatitude, latitude));
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public Double getLongitude() {
		return longitude;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public void setLongitude(Double newLongitude) {
		Double oldLongitude = longitude;
		longitude = newLongitude;
		if (eNotificationRequired())
			eNotify(new ENotificationImpl(this, Notification.SET, MappingPackage.ADMIN_MAPPING__LONGITUDE, oldLongitude, longitude));
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public Double getElevation() {
		return elevation;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public void setElevation(Double newElevation) {
		Double oldElevation = elevation;
		elevation = newElevation;
		if (eNotificationRequired())
			eNotify(new ENotificationImpl(this, Notification.SET, MappingPackage.ADMIN_MAPPING__ELEVATION, oldElevation, elevation));
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EList<EStructuralFeature> getLatitudeRef() {
		if (latitudeRef == null) {
			latitudeRef = new EObjectResolvingEList<EStructuralFeature>(EStructuralFeature.class, this, MappingPackage.ADMIN_MAPPING__LATITUDE_REF);
		}
		return latitudeRef;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EList<EStructuralFeature> getLongitudeRef() {
		if (longitudeRef == null) {
			longitudeRef = new EObjectResolvingEList<EStructuralFeature>(EStructuralFeature.class, this, MappingPackage.ADMIN_MAPPING__LONGITUDE_REF);
		}
		return longitudeRef;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EList<EStructuralFeature> getElevationRef() {
		if (elevationRef == null) {
			elevationRef = new EObjectResolvingEList<EStructuralFeature>(EStructuralFeature.class, this, MappingPackage.ADMIN_MAPPING__ELEVATION_REF);
		}
		return elevationRef;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public FeatureMapping getFriendlyNameMapping() {
		return friendlyNameMapping;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public NotificationChain basicSetFriendlyNameMapping(FeatureMapping newFriendlyNameMapping, NotificationChain msgs) {
		FeatureMapping oldFriendlyNameMapping = friendlyNameMapping;
		friendlyNameMapping = newFriendlyNameMapping;
		if (eNotificationRequired()) {
			ENotificationImpl notification = new ENotificationImpl(this, Notification.SET, MappingPackage.ADMIN_MAPPING__FRIENDLY_NAME_MAPPING, oldFriendlyNameMapping, newFriendlyNameMapping);
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
	public void setFriendlyNameMapping(FeatureMapping newFriendlyNameMapping) {
		if (newFriendlyNameMapping != friendlyNameMapping) {
			NotificationChain msgs = null;
			if (friendlyNameMapping != null)
				msgs = ((InternalEObject)friendlyNameMapping).eInverseRemove(this, EOPPOSITE_FEATURE_BASE - MappingPackage.ADMIN_MAPPING__FRIENDLY_NAME_MAPPING, null, msgs);
			if (newFriendlyNameMapping != null)
				msgs = ((InternalEObject)newFriendlyNameMapping).eInverseAdd(this, EOPPOSITE_FEATURE_BASE - MappingPackage.ADMIN_MAPPING__FRIENDLY_NAME_MAPPING, null, msgs);
			msgs = basicSetFriendlyNameMapping(newFriendlyNameMapping, msgs);
			if (msgs != null) msgs.dispatch();
		}
		else if (eNotificationRequired())
			eNotify(new ENotificationImpl(this, Notification.SET, MappingPackage.ADMIN_MAPPING__FRIENDLY_NAME_MAPPING, newFriendlyNameMapping, newFriendlyNameMapping));
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public FeatureMapping getLatitudeMapping() {
		return latitudeMapping;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public NotificationChain basicSetLatitudeMapping(FeatureMapping newLatitudeMapping, NotificationChain msgs) {
		FeatureMapping oldLatitudeMapping = latitudeMapping;
		latitudeMapping = newLatitudeMapping;
		if (eNotificationRequired()) {
			ENotificationImpl notification = new ENotificationImpl(this, Notification.SET, MappingPackage.ADMIN_MAPPING__LATITUDE_MAPPING, oldLatitudeMapping, newLatitudeMapping);
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
	public void setLatitudeMapping(FeatureMapping newLatitudeMapping) {
		if (newLatitudeMapping != latitudeMapping) {
			NotificationChain msgs = null;
			if (latitudeMapping != null)
				msgs = ((InternalEObject)latitudeMapping).eInverseRemove(this, EOPPOSITE_FEATURE_BASE - MappingPackage.ADMIN_MAPPING__LATITUDE_MAPPING, null, msgs);
			if (newLatitudeMapping != null)
				msgs = ((InternalEObject)newLatitudeMapping).eInverseAdd(this, EOPPOSITE_FEATURE_BASE - MappingPackage.ADMIN_MAPPING__LATITUDE_MAPPING, null, msgs);
			msgs = basicSetLatitudeMapping(newLatitudeMapping, msgs);
			if (msgs != null) msgs.dispatch();
		}
		else if (eNotificationRequired())
			eNotify(new ENotificationImpl(this, Notification.SET, MappingPackage.ADMIN_MAPPING__LATITUDE_MAPPING, newLatitudeMapping, newLatitudeMapping));
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public FeatureMapping getLongitudeMapping() {
		return longitudeMapping;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public NotificationChain basicSetLongitudeMapping(FeatureMapping newLongitudeMapping, NotificationChain msgs) {
		FeatureMapping oldLongitudeMapping = longitudeMapping;
		longitudeMapping = newLongitudeMapping;
		if (eNotificationRequired()) {
			ENotificationImpl notification = new ENotificationImpl(this, Notification.SET, MappingPackage.ADMIN_MAPPING__LONGITUDE_MAPPING, oldLongitudeMapping, newLongitudeMapping);
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
	public void setLongitudeMapping(FeatureMapping newLongitudeMapping) {
		if (newLongitudeMapping != longitudeMapping) {
			NotificationChain msgs = null;
			if (longitudeMapping != null)
				msgs = ((InternalEObject)longitudeMapping).eInverseRemove(this, EOPPOSITE_FEATURE_BASE - MappingPackage.ADMIN_MAPPING__LONGITUDE_MAPPING, null, msgs);
			if (newLongitudeMapping != null)
				msgs = ((InternalEObject)newLongitudeMapping).eInverseAdd(this, EOPPOSITE_FEATURE_BASE - MappingPackage.ADMIN_MAPPING__LONGITUDE_MAPPING, null, msgs);
			msgs = basicSetLongitudeMapping(newLongitudeMapping, msgs);
			if (msgs != null) msgs.dispatch();
		}
		else if (eNotificationRequired())
			eNotify(new ENotificationImpl(this, Notification.SET, MappingPackage.ADMIN_MAPPING__LONGITUDE_MAPPING, newLongitudeMapping, newLongitudeMapping));
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public FeatureMapping getElevationMapping() {
		return elevationMapping;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public NotificationChain basicSetElevationMapping(FeatureMapping newElevationMapping, NotificationChain msgs) {
		FeatureMapping oldElevationMapping = elevationMapping;
		elevationMapping = newElevationMapping;
		if (eNotificationRequired()) {
			ENotificationImpl notification = new ENotificationImpl(this, Notification.SET, MappingPackage.ADMIN_MAPPING__ELEVATION_MAPPING, oldElevationMapping, newElevationMapping);
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
	public void setElevationMapping(FeatureMapping newElevationMapping) {
		if (newElevationMapping != elevationMapping) {
			NotificationChain msgs = null;
			if (elevationMapping != null)
				msgs = ((InternalEObject)elevationMapping).eInverseRemove(this, EOPPOSITE_FEATURE_BASE - MappingPackage.ADMIN_MAPPING__ELEVATION_MAPPING, null, msgs);
			if (newElevationMapping != null)
				msgs = ((InternalEObject)newElevationMapping).eInverseAdd(this, EOPPOSITE_FEATURE_BASE - MappingPackage.ADMIN_MAPPING__ELEVATION_MAPPING, null, msgs);
			msgs = basicSetElevationMapping(newElevationMapping, msgs);
			if (msgs != null) msgs.dispatch();
		}
		else if (eNotificationRequired())
			eNotify(new ENotificationImpl(this, Notification.SET, MappingPackage.ADMIN_MAPPING__ELEVATION_MAPPING, newElevationMapping, newElevationMapping));
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public boolean isExtendAdmin() {
		return extendAdmin;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public void setExtendAdmin(boolean newExtendAdmin) {
		boolean oldExtendAdmin = extendAdmin;
		extendAdmin = newExtendAdmin;
		if (eNotificationRequired())
			eNotify(new ENotificationImpl(this, Notification.SET, MappingPackage.ADMIN_MAPPING__EXTEND_ADMIN, oldExtendAdmin, extendAdmin));
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public NotificationChain eInverseRemove(InternalEObject otherEnd, int featureID, NotificationChain msgs) {
		switch (featureID) {
			case MappingPackage.ADMIN_MAPPING__FRIENDLY_NAME_MAPPING:
				return basicSetFriendlyNameMapping(null, msgs);
			case MappingPackage.ADMIN_MAPPING__LATITUDE_MAPPING:
				return basicSetLatitudeMapping(null, msgs);
			case MappingPackage.ADMIN_MAPPING__LONGITUDE_MAPPING:
				return basicSetLongitudeMapping(null, msgs);
			case MappingPackage.ADMIN_MAPPING__ELEVATION_MAPPING:
				return basicSetElevationMapping(null, msgs);
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
			case MappingPackage.ADMIN_MAPPING__FRIENDLY_NAME:
				return getFriendlyName();
			case MappingPackage.ADMIN_MAPPING__FRIENDLY_NAME_FEATURE:
				return getFriendlyNameFeature();
			case MappingPackage.ADMIN_MAPPING__PROVIDER_PACKAGE:
				if (resolve) return getProviderPackage();
				return basicGetProviderPackage();
			case MappingPackage.ADMIN_MAPPING__LATITUDE:
				return getLatitude();
			case MappingPackage.ADMIN_MAPPING__LONGITUDE:
				return getLongitude();
			case MappingPackage.ADMIN_MAPPING__ELEVATION:
				return getElevation();
			case MappingPackage.ADMIN_MAPPING__LATITUDE_REF:
				return getLatitudeRef();
			case MappingPackage.ADMIN_MAPPING__LONGITUDE_REF:
				return getLongitudeRef();
			case MappingPackage.ADMIN_MAPPING__ELEVATION_REF:
				return getElevationRef();
			case MappingPackage.ADMIN_MAPPING__FRIENDLY_NAME_MAPPING:
				return getFriendlyNameMapping();
			case MappingPackage.ADMIN_MAPPING__LATITUDE_MAPPING:
				return getLatitudeMapping();
			case MappingPackage.ADMIN_MAPPING__LONGITUDE_MAPPING:
				return getLongitudeMapping();
			case MappingPackage.ADMIN_MAPPING__ELEVATION_MAPPING:
				return getElevationMapping();
			case MappingPackage.ADMIN_MAPPING__EXTEND_ADMIN:
				return isExtendAdmin();
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
			case MappingPackage.ADMIN_MAPPING__FRIENDLY_NAME:
				setFriendlyName((String)newValue);
				return;
			case MappingPackage.ADMIN_MAPPING__FRIENDLY_NAME_FEATURE:
				getFriendlyNameFeature().clear();
				getFriendlyNameFeature().addAll((Collection<? extends EStructuralFeature>)newValue);
				return;
			case MappingPackage.ADMIN_MAPPING__PROVIDER_PACKAGE:
				setProviderPackage((EPackage)newValue);
				return;
			case MappingPackage.ADMIN_MAPPING__LATITUDE:
				setLatitude((Double)newValue);
				return;
			case MappingPackage.ADMIN_MAPPING__LONGITUDE:
				setLongitude((Double)newValue);
				return;
			case MappingPackage.ADMIN_MAPPING__ELEVATION:
				setElevation((Double)newValue);
				return;
			case MappingPackage.ADMIN_MAPPING__LATITUDE_REF:
				getLatitudeRef().clear();
				getLatitudeRef().addAll((Collection<? extends EStructuralFeature>)newValue);
				return;
			case MappingPackage.ADMIN_MAPPING__LONGITUDE_REF:
				getLongitudeRef().clear();
				getLongitudeRef().addAll((Collection<? extends EStructuralFeature>)newValue);
				return;
			case MappingPackage.ADMIN_MAPPING__ELEVATION_REF:
				getElevationRef().clear();
				getElevationRef().addAll((Collection<? extends EStructuralFeature>)newValue);
				return;
			case MappingPackage.ADMIN_MAPPING__FRIENDLY_NAME_MAPPING:
				setFriendlyNameMapping((FeatureMapping)newValue);
				return;
			case MappingPackage.ADMIN_MAPPING__LATITUDE_MAPPING:
				setLatitudeMapping((FeatureMapping)newValue);
				return;
			case MappingPackage.ADMIN_MAPPING__LONGITUDE_MAPPING:
				setLongitudeMapping((FeatureMapping)newValue);
				return;
			case MappingPackage.ADMIN_MAPPING__ELEVATION_MAPPING:
				setElevationMapping((FeatureMapping)newValue);
				return;
			case MappingPackage.ADMIN_MAPPING__EXTEND_ADMIN:
				setExtendAdmin((Boolean)newValue);
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
			case MappingPackage.ADMIN_MAPPING__FRIENDLY_NAME:
				setFriendlyName(FRIENDLY_NAME_EDEFAULT);
				return;
			case MappingPackage.ADMIN_MAPPING__FRIENDLY_NAME_FEATURE:
				getFriendlyNameFeature().clear();
				return;
			case MappingPackage.ADMIN_MAPPING__PROVIDER_PACKAGE:
				setProviderPackage((EPackage)null);
				return;
			case MappingPackage.ADMIN_MAPPING__LATITUDE:
				setLatitude(LATITUDE_EDEFAULT);
				return;
			case MappingPackage.ADMIN_MAPPING__LONGITUDE:
				setLongitude(LONGITUDE_EDEFAULT);
				return;
			case MappingPackage.ADMIN_MAPPING__ELEVATION:
				setElevation(ELEVATION_EDEFAULT);
				return;
			case MappingPackage.ADMIN_MAPPING__LATITUDE_REF:
				getLatitudeRef().clear();
				return;
			case MappingPackage.ADMIN_MAPPING__LONGITUDE_REF:
				getLongitudeRef().clear();
				return;
			case MappingPackage.ADMIN_MAPPING__ELEVATION_REF:
				getElevationRef().clear();
				return;
			case MappingPackage.ADMIN_MAPPING__FRIENDLY_NAME_MAPPING:
				setFriendlyNameMapping((FeatureMapping)null);
				return;
			case MappingPackage.ADMIN_MAPPING__LATITUDE_MAPPING:
				setLatitudeMapping((FeatureMapping)null);
				return;
			case MappingPackage.ADMIN_MAPPING__LONGITUDE_MAPPING:
				setLongitudeMapping((FeatureMapping)null);
				return;
			case MappingPackage.ADMIN_MAPPING__ELEVATION_MAPPING:
				setElevationMapping((FeatureMapping)null);
				return;
			case MappingPackage.ADMIN_MAPPING__EXTEND_ADMIN:
				setExtendAdmin(EXTEND_ADMIN_EDEFAULT);
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
			case MappingPackage.ADMIN_MAPPING__FRIENDLY_NAME:
				return FRIENDLY_NAME_EDEFAULT == null ? friendlyName != null : !FRIENDLY_NAME_EDEFAULT.equals(friendlyName);
			case MappingPackage.ADMIN_MAPPING__FRIENDLY_NAME_FEATURE:
				return friendlyNameFeature != null && !friendlyNameFeature.isEmpty();
			case MappingPackage.ADMIN_MAPPING__PROVIDER_PACKAGE:
				return providerPackage != null;
			case MappingPackage.ADMIN_MAPPING__LATITUDE:
				return LATITUDE_EDEFAULT == null ? latitude != null : !LATITUDE_EDEFAULT.equals(latitude);
			case MappingPackage.ADMIN_MAPPING__LONGITUDE:
				return LONGITUDE_EDEFAULT == null ? longitude != null : !LONGITUDE_EDEFAULT.equals(longitude);
			case MappingPackage.ADMIN_MAPPING__ELEVATION:
				return ELEVATION_EDEFAULT == null ? elevation != null : !ELEVATION_EDEFAULT.equals(elevation);
			case MappingPackage.ADMIN_MAPPING__LATITUDE_REF:
				return latitudeRef != null && !latitudeRef.isEmpty();
			case MappingPackage.ADMIN_MAPPING__LONGITUDE_REF:
				return longitudeRef != null && !longitudeRef.isEmpty();
			case MappingPackage.ADMIN_MAPPING__ELEVATION_REF:
				return elevationRef != null && !elevationRef.isEmpty();
			case MappingPackage.ADMIN_MAPPING__FRIENDLY_NAME_MAPPING:
				return friendlyNameMapping != null;
			case MappingPackage.ADMIN_MAPPING__LATITUDE_MAPPING:
				return latitudeMapping != null;
			case MappingPackage.ADMIN_MAPPING__LONGITUDE_MAPPING:
				return longitudeMapping != null;
			case MappingPackage.ADMIN_MAPPING__ELEVATION_MAPPING:
				return elevationMapping != null;
			case MappingPackage.ADMIN_MAPPING__EXTEND_ADMIN:
				return extendAdmin != EXTEND_ADMIN_EDEFAULT;
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
		result.append(" (friendlyName: ");
		result.append(friendlyName);
		result.append(", latitude: ");
		result.append(latitude);
		result.append(", longitude: ");
		result.append(longitude);
		result.append(", elevation: ");
		result.append(elevation);
		result.append(", extendAdmin: ");
		result.append(extendAdmin);
		result.append(')');
		return result.toString();
	}

} //AdminMappingImpl
