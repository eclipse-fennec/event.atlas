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
package probeadmin.impl;

import dragino.DraginoPackage;

import dragino.impl.DraginoPackageImpl;

import em310udl.Em310udlPackage;

import em310udl.impl.Em310udlPackageImpl;

import lorawan.LorawanPackage;

import lorawan.impl.LorawanPackageImpl;

import org.eclipse.emf.ecore.EAttribute;
import org.eclipse.emf.ecore.EClass;
import org.eclipse.emf.ecore.EPackage;
import org.eclipse.emf.ecore.EReference;

import org.eclipse.emf.ecore.impl.EPackageImpl;

import org.eclipse.sensinact.model.core.provider.ProviderPackage;

import probeadmin.ProbeAdmin;
import probeadmin.ProbeProvider;
import probeadmin.ProbeadminFactory;
import probeadmin.ProbeadminPackage;

/**
 * <!-- begin-user-doc -->
 * An implementation of the model <b>Package</b>.
 * <!-- end-user-doc -->
 * @generated
 */
public class ProbeadminPackageImpl extends EPackageImpl implements ProbeadminPackage {
	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	private EClass probeAdminEClass = null;

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	private EClass probeProviderEClass = null;

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
	 * @see probeadmin.ProbeadminPackage#eNS_URI
	 * @see #init()
	 * @generated
	 */
	private ProbeadminPackageImpl() {
		super(eNS_URI, ProbeadminFactory.eINSTANCE);
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
	 * <p>This method is used to initialize {@link ProbeadminPackage#eINSTANCE} when that field is accessed.
	 * Clients should not invoke it directly. Instead, they should simply access that field to obtain the package.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #eNS_URI
	 * @see #createPackageContents()
	 * @see #initializePackageContents()
	 * @generated
	 */
	public static ProbeadminPackage init() {
		if (isInited) return (ProbeadminPackage)EPackage.Registry.INSTANCE.getEPackage(ProbeadminPackage.eNS_URI);

		// Obtain or create and register package
		Object registeredProbeadminPackage = EPackage.Registry.INSTANCE.get(eNS_URI);
		ProbeadminPackageImpl theProbeadminPackage = registeredProbeadminPackage instanceof ProbeadminPackageImpl ? (ProbeadminPackageImpl)registeredProbeadminPackage : new ProbeadminPackageImpl();

		isInited = true;

		// Initialize simple dependencies
		ProviderPackage.eINSTANCE.eClass();

		// Obtain or create and register interdependencies
		Object registeredPackage = EPackage.Registry.INSTANCE.getEPackage(DraginoPackage.eNS_URI);
		DraginoPackageImpl theDraginoPackage = (DraginoPackageImpl)(registeredPackage instanceof DraginoPackageImpl ? registeredPackage : DraginoPackage.eINSTANCE);
		registeredPackage = EPackage.Registry.INSTANCE.getEPackage(Em310udlPackage.eNS_URI);
		Em310udlPackageImpl theEm310udlPackage = (Em310udlPackageImpl)(registeredPackage instanceof Em310udlPackageImpl ? registeredPackage : Em310udlPackage.eINSTANCE);
		registeredPackage = EPackage.Registry.INSTANCE.getEPackage(LorawanPackage.eNS_URI);
		LorawanPackageImpl theLorawanPackage = (LorawanPackageImpl)(registeredPackage instanceof LorawanPackageImpl ? registeredPackage : LorawanPackage.eINSTANCE);

		// Create package meta-data objects
		theProbeadminPackage.createPackageContents();
		theDraginoPackage.createPackageContents();
		theEm310udlPackage.createPackageContents();
		theLorawanPackage.createPackageContents();

		// Initialize created meta-data
		theProbeadminPackage.initializePackageContents();
		theDraginoPackage.initializePackageContents();
		theEm310udlPackage.initializePackageContents();
		theLorawanPackage.initializePackageContents();

		// Mark meta-data to indicate it can't be changed
		theProbeadminPackage.freeze();

		// Update the registry and return the package
		EPackage.Registry.INSTANCE.put(ProbeadminPackage.eNS_URI, theProbeadminPackage);
		return theProbeadminPackage;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EClass getProbeAdmin() {
		return probeAdminEClass;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EAttribute getProbeAdmin_Viewport() {
		return (EAttribute)probeAdminEClass.getEStructuralFeatures().get(0);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EClass getProbeProvider() {
		return probeProviderEClass;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EReference getProbeProvider_Thermal() {
		return (EReference)probeProviderEClass.getEStructuralFeatures().get(0);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public ProbeadminFactory getProbeadminFactory() {
		return (ProbeadminFactory)getEFactoryInstance();
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
		probeAdminEClass = createEClass(PROBE_ADMIN);
		createEAttribute(probeAdminEClass, PROBE_ADMIN__VIEWPORT);

		probeProviderEClass = createEClass(PROBE_PROVIDER);
		createEReference(probeProviderEClass, PROBE_PROVIDER__THERMAL);
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

		// Obtain other dependent packages
		ProviderPackage theProviderPackage = (ProviderPackage)EPackage.Registry.INSTANCE.getEPackage(ProviderPackage.eNS_URI);

		// Create type parameters

		// Set bounds for type parameters

		// Add supertypes to classes
		probeAdminEClass.getESuperTypes().add(theProviderPackage.getAdmin());
		probeProviderEClass.getESuperTypes().add(theProviderPackage.getDynamicProvider());

		// Initialize classes, features, and operations; add parameters
		initEClass(probeAdminEClass, ProbeAdmin.class, "ProbeAdmin", !IS_ABSTRACT, !IS_INTERFACE, IS_GENERATED_INSTANCE_CLASS);
		initEAttribute(getProbeAdmin_Viewport(), ecorePackage.getEString(), "viewport", null, 0, 1, ProbeAdmin.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);

		initEClass(probeProviderEClass, ProbeProvider.class, "ProbeProvider", !IS_ABSTRACT, !IS_INTERFACE, IS_GENERATED_INSTANCE_CLASS);
		initEReference(getProbeProvider_Thermal(), this.getProbeAdmin(), null, "thermal", null, 0, 1, ProbeProvider.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_COMPOSITE, IS_RESOLVE_PROXIES, !IS_UNSETTABLE, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);

		// Create resource
		createResource(eNS_URI);
	}

} //ProbeadminPackageImpl
