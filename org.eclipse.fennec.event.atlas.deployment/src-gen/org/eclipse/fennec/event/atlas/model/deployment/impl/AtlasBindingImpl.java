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
 *      Data In Motion - initial API and implementation
 */
package org.eclipse.fennec.event.atlas.model.deployment.impl;

import java.util.Collection;

import org.eclipse.emf.common.notify.Notification;

import org.eclipse.emf.common.util.EList;

import org.eclipse.emf.ecore.EClass;

import org.eclipse.emf.ecore.impl.ENotificationImpl;
import org.eclipse.emf.ecore.impl.MinimalEObjectImpl;

import org.eclipse.emf.ecore.util.EDataTypeUniqueEList;

import org.eclipse.fennec.event.atlas.model.deployment.AtlasBinding;
import org.eclipse.fennec.event.atlas.model.deployment.DeploymentPackage;
import org.eclipse.fennec.event.atlas.model.deployment.PrefetchMode;

/**
 * <!-- begin-user-doc -->
 * An implementation of the model object '<em><b>Atlas Binding</b></em>'.
 * <!-- end-user-doc -->
 * <p>
 * The following features are implemented:
 * </p>
 * <ul>
 *   <li>{@link org.eclipse.fennec.event.atlas.model.deployment.impl.AtlasBindingImpl#getBaseUri <em>Base Uri</em>}</li>
 *   <li>{@link org.eclipse.fennec.event.atlas.model.deployment.impl.AtlasBindingImpl#getScope <em>Scope</em>}</li>
 *   <li>{@link org.eclipse.fennec.event.atlas.model.deployment.impl.AtlasBindingImpl#getStage <em>Stage</em>}</li>
 *   <li>{@link org.eclipse.fennec.event.atlas.model.deployment.impl.AtlasBindingImpl#getMappingRegistries <em>Mapping Registries</em>}</li>
 *   <li>{@link org.eclipse.fennec.event.atlas.model.deployment.impl.AtlasBindingImpl#getPrefetchMode <em>Prefetch Mode</em>}</li>
 *   <li>{@link org.eclipse.fennec.event.atlas.model.deployment.impl.AtlasBindingImpl#getRequiredNsUris <em>Required Ns Uris</em>}</li>
 *   <li>{@link org.eclipse.fennec.event.atlas.model.deployment.impl.AtlasBindingImpl#getDriftCheckIntervalMs <em>Drift Check Interval Ms</em>}</li>
 *   <li>{@link org.eclipse.fennec.event.atlas.model.deployment.impl.AtlasBindingImpl#getRefreshIntervalMs <em>Refresh Interval Ms</em>}</li>
 * </ul>
 *
 * @generated
 */
public class AtlasBindingImpl extends MinimalEObjectImpl.Container implements AtlasBinding {
	/**
	 * The default value of the '{@link #getBaseUri() <em>Base Uri</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getBaseUri()
	 * @generated
	 * @ordered
	 */
	protected static final String BASE_URI_EDEFAULT = null;

	/**
	 * The cached value of the '{@link #getBaseUri() <em>Base Uri</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getBaseUri()
	 * @generated
	 * @ordered
	 */
	protected String baseUri = BASE_URI_EDEFAULT;

	/**
	 * The default value of the '{@link #getScope() <em>Scope</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getScope()
	 * @generated
	 * @ordered
	 */
	protected static final String SCOPE_EDEFAULT = null;

	/**
	 * The cached value of the '{@link #getScope() <em>Scope</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getScope()
	 * @generated
	 * @ordered
	 */
	protected String scope = SCOPE_EDEFAULT;

	/**
	 * The default value of the '{@link #getStage() <em>Stage</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getStage()
	 * @generated
	 * @ordered
	 */
	protected static final String STAGE_EDEFAULT = "release";

	/**
	 * The cached value of the '{@link #getStage() <em>Stage</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getStage()
	 * @generated
	 * @ordered
	 */
	protected String stage = STAGE_EDEFAULT;

	/**
	 * The cached value of the '{@link #getMappingRegistries() <em>Mapping Registries</em>}' attribute list.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getMappingRegistries()
	 * @generated
	 * @ordered
	 */
	protected EList<String> mappingRegistries;

	/**
	 * The default value of the '{@link #getPrefetchMode() <em>Prefetch Mode</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getPrefetchMode()
	 * @generated
	 * @ordered
	 */
	protected static final PrefetchMode PREFETCH_MODE_EDEFAULT = PrefetchMode.EAGER;

	/**
	 * The cached value of the '{@link #getPrefetchMode() <em>Prefetch Mode</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getPrefetchMode()
	 * @generated
	 * @ordered
	 */
	protected PrefetchMode prefetchMode = PREFETCH_MODE_EDEFAULT;

	/**
	 * The cached value of the '{@link #getRequiredNsUris() <em>Required Ns Uris</em>}' attribute list.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getRequiredNsUris()
	 * @generated
	 * @ordered
	 */
	protected EList<String> requiredNsUris;

	/**
	 * The default value of the '{@link #getDriftCheckIntervalMs() <em>Drift Check Interval Ms</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getDriftCheckIntervalMs()
	 * @generated
	 * @ordered
	 */
	protected static final long DRIFT_CHECK_INTERVAL_MS_EDEFAULT = 10000L;

	/**
	 * The cached value of the '{@link #getDriftCheckIntervalMs() <em>Drift Check Interval Ms</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getDriftCheckIntervalMs()
	 * @generated
	 * @ordered
	 */
	protected long driftCheckIntervalMs = DRIFT_CHECK_INTERVAL_MS_EDEFAULT;

	/**
	 * The default value of the '{@link #getRefreshIntervalMs() <em>Refresh Interval Ms</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getRefreshIntervalMs()
	 * @generated
	 * @ordered
	 */
	protected static final long REFRESH_INTERVAL_MS_EDEFAULT = 60000L;

	/**
	 * The cached value of the '{@link #getRefreshIntervalMs() <em>Refresh Interval Ms</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getRefreshIntervalMs()
	 * @generated
	 * @ordered
	 */
	protected long refreshIntervalMs = REFRESH_INTERVAL_MS_EDEFAULT;

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	protected AtlasBindingImpl() {
		super();
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	protected EClass eStaticClass() {
		return DeploymentPackage.Literals.ATLAS_BINDING;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public String getBaseUri() {
		return baseUri;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public void setBaseUri(String newBaseUri) {
		String oldBaseUri = baseUri;
		baseUri = newBaseUri;
		if (eNotificationRequired())
			eNotify(new ENotificationImpl(this, Notification.SET, DeploymentPackage.ATLAS_BINDING__BASE_URI, oldBaseUri, baseUri));
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public String getScope() {
		return scope;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public void setScope(String newScope) {
		String oldScope = scope;
		scope = newScope;
		if (eNotificationRequired())
			eNotify(new ENotificationImpl(this, Notification.SET, DeploymentPackage.ATLAS_BINDING__SCOPE, oldScope, scope));
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public String getStage() {
		return stage;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public void setStage(String newStage) {
		String oldStage = stage;
		stage = newStage;
		if (eNotificationRequired())
			eNotify(new ENotificationImpl(this, Notification.SET, DeploymentPackage.ATLAS_BINDING__STAGE, oldStage, stage));
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EList<String> getMappingRegistries() {
		if (mappingRegistries == null) {
			mappingRegistries = new EDataTypeUniqueEList<String>(String.class, this, DeploymentPackage.ATLAS_BINDING__MAPPING_REGISTRIES);
		}
		return mappingRegistries;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public PrefetchMode getPrefetchMode() {
		return prefetchMode;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public void setPrefetchMode(PrefetchMode newPrefetchMode) {
		PrefetchMode oldPrefetchMode = prefetchMode;
		prefetchMode = newPrefetchMode == null ? PREFETCH_MODE_EDEFAULT : newPrefetchMode;
		if (eNotificationRequired())
			eNotify(new ENotificationImpl(this, Notification.SET, DeploymentPackage.ATLAS_BINDING__PREFETCH_MODE, oldPrefetchMode, prefetchMode));
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EList<String> getRequiredNsUris() {
		if (requiredNsUris == null) {
			requiredNsUris = new EDataTypeUniqueEList<String>(String.class, this, DeploymentPackage.ATLAS_BINDING__REQUIRED_NS_URIS);
		}
		return requiredNsUris;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public long getDriftCheckIntervalMs() {
		return driftCheckIntervalMs;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public void setDriftCheckIntervalMs(long newDriftCheckIntervalMs) {
		long oldDriftCheckIntervalMs = driftCheckIntervalMs;
		driftCheckIntervalMs = newDriftCheckIntervalMs;
		if (eNotificationRequired())
			eNotify(new ENotificationImpl(this, Notification.SET, DeploymentPackage.ATLAS_BINDING__DRIFT_CHECK_INTERVAL_MS, oldDriftCheckIntervalMs, driftCheckIntervalMs));
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public long getRefreshIntervalMs() {
		return refreshIntervalMs;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public void setRefreshIntervalMs(long newRefreshIntervalMs) {
		long oldRefreshIntervalMs = refreshIntervalMs;
		refreshIntervalMs = newRefreshIntervalMs;
		if (eNotificationRequired())
			eNotify(new ENotificationImpl(this, Notification.SET, DeploymentPackage.ATLAS_BINDING__REFRESH_INTERVAL_MS, oldRefreshIntervalMs, refreshIntervalMs));
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public Object eGet(int featureID, boolean resolve, boolean coreType) {
		switch (featureID) {
			case DeploymentPackage.ATLAS_BINDING__BASE_URI:
				return getBaseUri();
			case DeploymentPackage.ATLAS_BINDING__SCOPE:
				return getScope();
			case DeploymentPackage.ATLAS_BINDING__STAGE:
				return getStage();
			case DeploymentPackage.ATLAS_BINDING__MAPPING_REGISTRIES:
				return getMappingRegistries();
			case DeploymentPackage.ATLAS_BINDING__PREFETCH_MODE:
				return getPrefetchMode();
			case DeploymentPackage.ATLAS_BINDING__REQUIRED_NS_URIS:
				return getRequiredNsUris();
			case DeploymentPackage.ATLAS_BINDING__DRIFT_CHECK_INTERVAL_MS:
				return getDriftCheckIntervalMs();
			case DeploymentPackage.ATLAS_BINDING__REFRESH_INTERVAL_MS:
				return getRefreshIntervalMs();
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
			case DeploymentPackage.ATLAS_BINDING__BASE_URI:
				setBaseUri((String)newValue);
				return;
			case DeploymentPackage.ATLAS_BINDING__SCOPE:
				setScope((String)newValue);
				return;
			case DeploymentPackage.ATLAS_BINDING__STAGE:
				setStage((String)newValue);
				return;
			case DeploymentPackage.ATLAS_BINDING__MAPPING_REGISTRIES:
				getMappingRegistries().clear();
				getMappingRegistries().addAll((Collection<? extends String>)newValue);
				return;
			case DeploymentPackage.ATLAS_BINDING__PREFETCH_MODE:
				setPrefetchMode((PrefetchMode)newValue);
				return;
			case DeploymentPackage.ATLAS_BINDING__REQUIRED_NS_URIS:
				getRequiredNsUris().clear();
				getRequiredNsUris().addAll((Collection<? extends String>)newValue);
				return;
			case DeploymentPackage.ATLAS_BINDING__DRIFT_CHECK_INTERVAL_MS:
				setDriftCheckIntervalMs((Long)newValue);
				return;
			case DeploymentPackage.ATLAS_BINDING__REFRESH_INTERVAL_MS:
				setRefreshIntervalMs((Long)newValue);
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
			case DeploymentPackage.ATLAS_BINDING__BASE_URI:
				setBaseUri(BASE_URI_EDEFAULT);
				return;
			case DeploymentPackage.ATLAS_BINDING__SCOPE:
				setScope(SCOPE_EDEFAULT);
				return;
			case DeploymentPackage.ATLAS_BINDING__STAGE:
				setStage(STAGE_EDEFAULT);
				return;
			case DeploymentPackage.ATLAS_BINDING__MAPPING_REGISTRIES:
				getMappingRegistries().clear();
				return;
			case DeploymentPackage.ATLAS_BINDING__PREFETCH_MODE:
				setPrefetchMode(PREFETCH_MODE_EDEFAULT);
				return;
			case DeploymentPackage.ATLAS_BINDING__REQUIRED_NS_URIS:
				getRequiredNsUris().clear();
				return;
			case DeploymentPackage.ATLAS_BINDING__DRIFT_CHECK_INTERVAL_MS:
				setDriftCheckIntervalMs(DRIFT_CHECK_INTERVAL_MS_EDEFAULT);
				return;
			case DeploymentPackage.ATLAS_BINDING__REFRESH_INTERVAL_MS:
				setRefreshIntervalMs(REFRESH_INTERVAL_MS_EDEFAULT);
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
			case DeploymentPackage.ATLAS_BINDING__BASE_URI:
				return BASE_URI_EDEFAULT == null ? baseUri != null : !BASE_URI_EDEFAULT.equals(baseUri);
			case DeploymentPackage.ATLAS_BINDING__SCOPE:
				return SCOPE_EDEFAULT == null ? scope != null : !SCOPE_EDEFAULT.equals(scope);
			case DeploymentPackage.ATLAS_BINDING__STAGE:
				return STAGE_EDEFAULT == null ? stage != null : !STAGE_EDEFAULT.equals(stage);
			case DeploymentPackage.ATLAS_BINDING__MAPPING_REGISTRIES:
				return mappingRegistries != null && !mappingRegistries.isEmpty();
			case DeploymentPackage.ATLAS_BINDING__PREFETCH_MODE:
				return prefetchMode != PREFETCH_MODE_EDEFAULT;
			case DeploymentPackage.ATLAS_BINDING__REQUIRED_NS_URIS:
				return requiredNsUris != null && !requiredNsUris.isEmpty();
			case DeploymentPackage.ATLAS_BINDING__DRIFT_CHECK_INTERVAL_MS:
				return driftCheckIntervalMs != DRIFT_CHECK_INTERVAL_MS_EDEFAULT;
			case DeploymentPackage.ATLAS_BINDING__REFRESH_INTERVAL_MS:
				return refreshIntervalMs != REFRESH_INTERVAL_MS_EDEFAULT;
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
		result.append(" (baseUri: ");
		result.append(baseUri);
		result.append(", scope: ");
		result.append(scope);
		result.append(", stage: ");
		result.append(stage);
		result.append(", mappingRegistries: ");
		result.append(mappingRegistries);
		result.append(", prefetchMode: ");
		result.append(prefetchMode);
		result.append(", requiredNsUris: ");
		result.append(requiredNsUris);
		result.append(", driftCheckIntervalMs: ");
		result.append(driftCheckIntervalMs);
		result.append(", refreshIntervalMs: ");
		result.append(refreshIntervalMs);
		result.append(')');
		return result.toString();
	}

} //AtlasBindingImpl
