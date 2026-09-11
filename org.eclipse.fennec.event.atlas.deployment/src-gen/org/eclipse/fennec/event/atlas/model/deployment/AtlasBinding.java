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
package org.eclipse.fennec.event.atlas.model.deployment;

import org.eclipse.emf.common.util.EList;

import org.eclipse.emf.ecore.EObject;

import org.osgi.annotation.versioning.ProviderType;

/**
 * <!-- begin-user-doc -->
 * A representation of the model object '<em><b>Atlas Binding</b></em>'.
 * <!-- end-user-doc -->
 *
 * <!-- begin-model-doc -->
 * The Model Atlas a runtime reads from: the REST client, and the Atlas-backed EObject provider that syncs mappings into the sensinact-mappings registry.
 * 
 * An Atlas-fed mapping added after start-up IS discovered while refreshIntervalMs is greater than zero. A newly published EPackage is not - it arrives on the next restart.
 * <!-- end-model-doc -->
 *
 * <p>
 * The following features are supported:
 * </p>
 * <ul>
 *   <li>{@link org.eclipse.fennec.event.atlas.model.deployment.AtlasBinding#getBaseUri <em>Base Uri</em>}</li>
 *   <li>{@link org.eclipse.fennec.event.atlas.model.deployment.AtlasBinding#getScope <em>Scope</em>}</li>
 *   <li>{@link org.eclipse.fennec.event.atlas.model.deployment.AtlasBinding#getStage <em>Stage</em>}</li>
 *   <li>{@link org.eclipse.fennec.event.atlas.model.deployment.AtlasBinding#getMappingRegistries <em>Mapping Registries</em>}</li>
 *   <li>{@link org.eclipse.fennec.event.atlas.model.deployment.AtlasBinding#getPrefetchMode <em>Prefetch Mode</em>}</li>
 *   <li>{@link org.eclipse.fennec.event.atlas.model.deployment.AtlasBinding#getRequiredNsUris <em>Required Ns Uris</em>}</li>
 *   <li>{@link org.eclipse.fennec.event.atlas.model.deployment.AtlasBinding#getDriftCheckIntervalMs <em>Drift Check Interval Ms</em>}</li>
 *   <li>{@link org.eclipse.fennec.event.atlas.model.deployment.AtlasBinding#getRefreshIntervalMs <em>Refresh Interval Ms</em>}</li>
 * </ul>
 *
 * @see org.eclipse.fennec.event.atlas.model.deployment.DeploymentPackage#getAtlasBinding()
 * @model
 * @generated
 */
@ProviderType
public interface AtlasBinding extends EObject {
	/**
	 * Returns the value of the '<em><b>Base Uri</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * <!-- begin-model-doc -->
	 * REST base of the Model Atlas, e.g. http://localhost:8080/atlas/rest.
	 * <!-- end-model-doc -->
	 * @return the value of the '<em>Base Uri</em>' attribute.
	 * @see #setBaseUri(String)
	 * @see org.eclipse.fennec.event.atlas.model.deployment.DeploymentPackage#getAtlasBinding_BaseUri()
	 * @model required="true"
	 * @generated
	 */
	String getBaseUri();

	/**
	 * Sets the value of the '{@link org.eclipse.fennec.event.atlas.model.deployment.AtlasBinding#getBaseUri <em>Base Uri</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @param value the new value of the '<em>Base Uri</em>' attribute.
	 * @see #getBaseUri()
	 * @generated
	 */
	void setBaseUri(String value);

	/**
	 * Returns the value of the '<em><b>Scope</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * <!-- begin-model-doc -->
	 * Atlas scope name, used both as the client's allowed/default scope and as the provider's scope target.
	 * <!-- end-model-doc -->
	 * @return the value of the '<em>Scope</em>' attribute.
	 * @see #setScope(String)
	 * @see org.eclipse.fennec.event.atlas.model.deployment.DeploymentPackage#getAtlasBinding_Scope()
	 * @model required="true"
	 * @generated
	 */
	String getScope();

	/**
	 * Sets the value of the '{@link org.eclipse.fennec.event.atlas.model.deployment.AtlasBinding#getScope <em>Scope</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @param value the new value of the '<em>Scope</em>' attribute.
	 * @see #getScope()
	 * @generated
	 */
	void setScope(String value);

	/**
	 * Returns the value of the '<em><b>Stage</b></em>' attribute.
	 * The default value is <code>"release"</code>.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * <!-- begin-model-doc -->
	 * The scope's final stage. NOT the client default 'released' - naming the wrong stage yields no EPackages and no error.
	 * <!-- end-model-doc -->
	 * @return the value of the '<em>Stage</em>' attribute.
	 * @see #setStage(String)
	 * @see org.eclipse.fennec.event.atlas.model.deployment.DeploymentPackage#getAtlasBinding_Stage()
	 * @model default="release"
	 * @generated
	 */
	String getStage();

	/**
	 * Sets the value of the '{@link org.eclipse.fennec.event.atlas.model.deployment.AtlasBinding#getStage <em>Stage</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @param value the new value of the '<em>Stage</em>' attribute.
	 * @see #getStage()
	 * @generated
	 */
	void setStage(String value);

	/**
	 * Returns the value of the '<em><b>Mapping Registries</b></em>' attribute list.
	 * The list contents are of type {@link java.lang.String}.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * <!-- begin-model-doc -->
	 * Atlas registries holding the mapping objects, e.g. sensinactmapping.
	 * <!-- end-model-doc -->
	 * @return the value of the '<em>Mapping Registries</em>' attribute list.
	 * @see org.eclipse.fennec.event.atlas.model.deployment.DeploymentPackage#getAtlasBinding_MappingRegistries()
	 * @model
	 * @generated
	 */
	EList<String> getMappingRegistries();

	/**
	 * Returns the value of the '<em><b>Prefetch Mode</b></em>' attribute.
	 * The default value is <code>"EAGER"</code>.
	 * The literals are from the enumeration {@link org.eclipse.fennec.event.atlas.model.deployment.PrefetchMode}.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the value of the '<em>Prefetch Mode</em>' attribute.
	 * @see org.eclipse.fennec.event.atlas.model.deployment.PrefetchMode
	 * @see #setPrefetchMode(PrefetchMode)
	 * @see org.eclipse.fennec.event.atlas.model.deployment.DeploymentPackage#getAtlasBinding_PrefetchMode()
	 * @model default="EAGER"
	 * @generated
	 */
	PrefetchMode getPrefetchMode();

	/**
	 * Sets the value of the '{@link org.eclipse.fennec.event.atlas.model.deployment.AtlasBinding#getPrefetchMode <em>Prefetch Mode</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @param value the new value of the '<em>Prefetch Mode</em>' attribute.
	 * @see org.eclipse.fennec.event.atlas.model.deployment.PrefetchMode
	 * @see #getPrefetchMode()
	 * @generated
	 */
	void setPrefetchMode(PrefetchMode value);

	/**
	 * Returns the value of the '<em><b>Required Ns Uris</b></em>' attribute list.
	 * The list contents are of type {@link java.lang.String}.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * <!-- begin-model-doc -->
	 * A gate, not a loader: the sync pass is postponed until these EPackages are registered. List every domain nsURI that is mapped.
	 * <!-- end-model-doc -->
	 * @return the value of the '<em>Required Ns Uris</em>' attribute list.
	 * @see org.eclipse.fennec.event.atlas.model.deployment.DeploymentPackage#getAtlasBinding_RequiredNsUris()
	 * @model
	 * @generated
	 */
	EList<String> getRequiredNsUris();

	/**
	 * Returns the value of the '<em><b>Drift Check Interval Ms</b></em>' attribute.
	 * The default value is <code>"10000"</code>.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the value of the '<em>Drift Check Interval Ms</em>' attribute.
	 * @see #setDriftCheckIntervalMs(long)
	 * @see org.eclipse.fennec.event.atlas.model.deployment.DeploymentPackage#getAtlasBinding_DriftCheckIntervalMs()
	 * @model default="10000"
	 * @generated
	 */
	long getDriftCheckIntervalMs();

	/**
	 * Sets the value of the '{@link org.eclipse.fennec.event.atlas.model.deployment.AtlasBinding#getDriftCheckIntervalMs <em>Drift Check Interval Ms</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @param value the new value of the '<em>Drift Check Interval Ms</em>' attribute.
	 * @see #getDriftCheckIntervalMs()
	 * @generated
	 */
	void setDriftCheckIntervalMs(long value);

	/**
	 * Returns the value of the '<em><b>Refresh Interval Ms</b></em>' attribute.
	 * The default value is <code>"60000"</code>.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * <!-- begin-model-doc -->
	 * Full re-sync interval. Greater than zero is what makes a mapping added to the Atlas after start-up show up without a restart.
	 * <!-- end-model-doc -->
	 * @return the value of the '<em>Refresh Interval Ms</em>' attribute.
	 * @see #setRefreshIntervalMs(long)
	 * @see org.eclipse.fennec.event.atlas.model.deployment.DeploymentPackage#getAtlasBinding_RefreshIntervalMs()
	 * @model default="60000"
	 * @generated
	 */
	long getRefreshIntervalMs();

	/**
	 * Sets the value of the '{@link org.eclipse.fennec.event.atlas.model.deployment.AtlasBinding#getRefreshIntervalMs <em>Refresh Interval Ms</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @param value the new value of the '<em>Refresh Interval Ms</em>' attribute.
	 * @see #getRefreshIntervalMs()
	 * @generated
	 */
	void setRefreshIntervalMs(long value);

} // AtlasBinding
