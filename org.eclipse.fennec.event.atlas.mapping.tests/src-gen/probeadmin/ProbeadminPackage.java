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
package probeadmin;


import org.eclipse.emf.ecore.EAttribute;
import org.eclipse.emf.ecore.EClass;
import org.eclipse.emf.ecore.EReference;

import org.eclipse.fennec.emf.osgi.annotation.provide.EPackage;

import org.eclipse.sensinact.model.core.provider.ProviderPackage;

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
 * @see probeadmin.ProbeadminFactory
 * @model kind="package"
 * @generated
 */
@ProviderType
@EPackage(uri = ProbeadminPackage.eNS_URI, fingerprint = "fp1:805d651443a1030c0fdc5dc5b99decdd7aa0a74a21f789306cf626503824a40b", genModel = "/model/lora-test.genmodel", genModelSourceLocations = {"model/lora-test.genmodel","org.eclipse.fennec.event.atlas.mapping.tests/model/lora-test.genmodel"}, ecore = "/model/probe-admin.ecore", ecoreSourceLocations = "/model/probe-admin.ecore")
public interface ProbeadminPackage extends org.eclipse.emf.ecore.EPackage {
	/**
	 * The package name.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	String eNAME = "probeadmin";

	/**
	 * The package namespace URI.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	String eNS_URI = "http://example.org/event.atlas/probeadmin/1.0";

	/**
	 * The package namespace name.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	String eNS_PREFIX = "probeadmin";

	/**
	 * The singleton instance of the package.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	ProbeadminPackage eINSTANCE = probeadmin.impl.ProbeadminPackageImpl.init();

	/**
	 * The meta object id for the '{@link probeadmin.impl.ProbeAdminImpl <em>Probe Admin</em>}' class.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see probeadmin.impl.ProbeAdminImpl
	 * @see probeadmin.impl.ProbeadminPackageImpl#getProbeAdmin()
	 * @generated
	 */
	int PROBE_ADMIN = 0;

	/**
	 * The feature id for the '<em><b>Metadata</b></em>' map.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int PROBE_ADMIN__METADATA = ProviderPackage.ADMIN__METADATA;

	/**
	 * The feature id for the '<em><b>Friendly Name</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int PROBE_ADMIN__FRIENDLY_NAME = ProviderPackage.ADMIN__FRIENDLY_NAME;

	/**
	 * The feature id for the '<em><b>Description</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int PROBE_ADMIN__DESCRIPTION = ProviderPackage.ADMIN__DESCRIPTION;

	/**
	 * The feature id for the '<em><b>Location</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int PROBE_ADMIN__LOCATION = ProviderPackage.ADMIN__LOCATION;

	/**
	 * The feature id for the '<em><b>Model Package Uri</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int PROBE_ADMIN__MODEL_PACKAGE_URI = ProviderPackage.ADMIN__MODEL_PACKAGE_URI;

	/**
	 * The feature id for the '<em><b>Model</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int PROBE_ADMIN__MODEL = ProviderPackage.ADMIN__MODEL;

	/**
	 * The feature id for the '<em><b>Viewport</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int PROBE_ADMIN__VIEWPORT = ProviderPackage.ADMIN_FEATURE_COUNT + 0;

	/**
	 * The number of structural features of the '<em>Probe Admin</em>' class.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int PROBE_ADMIN_FEATURE_COUNT = ProviderPackage.ADMIN_FEATURE_COUNT + 1;

	/**
	 * The operation id for the '<em>EIs Set</em>' operation.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int PROBE_ADMIN___EIS_SET__ESTRUCTURALFEATURE = ProviderPackage.ADMIN___EIS_SET__ESTRUCTURALFEATURE;

	/**
	 * The number of operations of the '<em>Probe Admin</em>' class.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int PROBE_ADMIN_OPERATION_COUNT = ProviderPackage.ADMIN_OPERATION_COUNT + 0;

	/**
	 * The meta object id for the '{@link probeadmin.impl.ProbeProviderImpl <em>Probe Provider</em>}' class.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see probeadmin.impl.ProbeProviderImpl
	 * @see probeadmin.impl.ProbeadminPackageImpl#getProbeProvider()
	 * @generated
	 */
	int PROBE_PROVIDER = 1;

	/**
	 * The feature id for the '<em><b>Id</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int PROBE_PROVIDER__ID = ProviderPackage.DYNAMIC_PROVIDER__ID;

	/**
	 * The feature id for the '<em><b>Admin</b></em>' containment reference.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int PROBE_PROVIDER__ADMIN = ProviderPackage.DYNAMIC_PROVIDER__ADMIN;

	/**
	 * The feature id for the '<em><b>Linked Providers</b></em>' reference list.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int PROBE_PROVIDER__LINKED_PROVIDERS = ProviderPackage.DYNAMIC_PROVIDER__LINKED_PROVIDERS;

	/**
	 * The feature id for the '<em><b>Services</b></em>' map.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int PROBE_PROVIDER__SERVICES = ProviderPackage.DYNAMIC_PROVIDER__SERVICES;

	/**
	 * The feature id for the '<em><b>Thermal</b></em>' reference.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int PROBE_PROVIDER__THERMAL = ProviderPackage.DYNAMIC_PROVIDER_FEATURE_COUNT + 0;

	/**
	 * The number of structural features of the '<em>Probe Provider</em>' class.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int PROBE_PROVIDER_FEATURE_COUNT = ProviderPackage.DYNAMIC_PROVIDER_FEATURE_COUNT + 1;

	/**
	 * The operation id for the '<em>Get Service</em>' operation.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int PROBE_PROVIDER___GET_SERVICE__STRING = ProviderPackage.DYNAMIC_PROVIDER___GET_SERVICE__STRING;

	/**
	 * The operation id for the '<em>Get Service EClass</em>' operation.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int PROBE_PROVIDER___GET_SERVICE_ECLASS__STRING = ProviderPackage.DYNAMIC_PROVIDER___GET_SERVICE_ECLASS__STRING;

	/**
	 * The number of operations of the '<em>Probe Provider</em>' class.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int PROBE_PROVIDER_OPERATION_COUNT = ProviderPackage.DYNAMIC_PROVIDER_OPERATION_COUNT + 0;


	/**
	 * Returns the meta object for class '{@link probeadmin.ProbeAdmin <em>Probe Admin</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for class '<em>Probe Admin</em>'.
	 * @see probeadmin.ProbeAdmin
	 * @generated
	 */
	EClass getProbeAdmin();

	/**
	 * Returns the meta object for the attribute '{@link probeadmin.ProbeAdmin#getViewport <em>Viewport</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the attribute '<em>Viewport</em>'.
	 * @see probeadmin.ProbeAdmin#getViewport()
	 * @see #getProbeAdmin()
	 * @generated
	 */
	EAttribute getProbeAdmin_Viewport();

	/**
	 * Returns the meta object for class '{@link probeadmin.ProbeProvider <em>Probe Provider</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for class '<em>Probe Provider</em>'.
	 * @see probeadmin.ProbeProvider
	 * @generated
	 */
	EClass getProbeProvider();

	/**
	 * Returns the meta object for the reference '{@link probeadmin.ProbeProvider#getThermal <em>Thermal</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the reference '<em>Thermal</em>'.
	 * @see probeadmin.ProbeProvider#getThermal()
	 * @see #getProbeProvider()
	 * @generated
	 */
	EReference getProbeProvider_Thermal();

	/**
	 * Returns the factory that creates the instances of the model.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the factory that creates the instances of the model.
	 * @generated
	 */
	ProbeadminFactory getProbeadminFactory();

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
		 * The meta object literal for the '{@link probeadmin.impl.ProbeAdminImpl <em>Probe Admin</em>}' class.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @see probeadmin.impl.ProbeAdminImpl
		 * @see probeadmin.impl.ProbeadminPackageImpl#getProbeAdmin()
		 * @generated
		 */
		EClass PROBE_ADMIN = eINSTANCE.getProbeAdmin();

		/**
		 * The meta object literal for the '<em><b>Viewport</b></em>' attribute feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EAttribute PROBE_ADMIN__VIEWPORT = eINSTANCE.getProbeAdmin_Viewport();

		/**
		 * The meta object literal for the '{@link probeadmin.impl.ProbeProviderImpl <em>Probe Provider</em>}' class.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @see probeadmin.impl.ProbeProviderImpl
		 * @see probeadmin.impl.ProbeadminPackageImpl#getProbeProvider()
		 * @generated
		 */
		EClass PROBE_PROVIDER = eINSTANCE.getProbeProvider();

		/**
		 * The meta object literal for the '<em><b>Thermal</b></em>' reference feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EReference PROBE_PROVIDER__THERMAL = eINSTANCE.getProbeProvider_Thermal();

	}

} //ProbeadminPackage
