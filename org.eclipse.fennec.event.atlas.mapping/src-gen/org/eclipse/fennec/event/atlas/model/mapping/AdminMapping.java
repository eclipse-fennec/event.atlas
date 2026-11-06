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
package org.eclipse.fennec.event.atlas.model.mapping;

import org.eclipse.emf.common.util.EList;

import org.eclipse.emf.ecore.EPackage;
import org.eclipse.emf.ecore.EStructuralFeature;

import org.osgi.annotation.versioning.ProviderType;

/**
 * <!-- begin-user-doc -->
 * A representation of the model object '<em><b>Admin Mapping</b></em>'.
 * <!-- end-user-doc -->
 *
 * <p>
 * The following features are supported:
 * </p>
 * <ul>
 *   <li>{@link org.eclipse.fennec.event.atlas.model.mapping.AdminMapping#getFriendlyName <em>Friendly Name</em>}</li>
 *   <li>{@link org.eclipse.fennec.event.atlas.model.mapping.AdminMapping#getFriendlyNameFeature <em>Friendly Name Feature</em>}</li>
 *   <li>{@link org.eclipse.fennec.event.atlas.model.mapping.AdminMapping#getProviderPackage <em>Provider Package</em>}</li>
 *   <li>{@link org.eclipse.fennec.event.atlas.model.mapping.AdminMapping#getLatitude <em>Latitude</em>}</li>
 *   <li>{@link org.eclipse.fennec.event.atlas.model.mapping.AdminMapping#getLongitude <em>Longitude</em>}</li>
 *   <li>{@link org.eclipse.fennec.event.atlas.model.mapping.AdminMapping#getElevation <em>Elevation</em>}</li>
 *   <li>{@link org.eclipse.fennec.event.atlas.model.mapping.AdminMapping#getLatitudeRef <em>Latitude Ref</em>}</li>
 *   <li>{@link org.eclipse.fennec.event.atlas.model.mapping.AdminMapping#getLongitudeRef <em>Longitude Ref</em>}</li>
 *   <li>{@link org.eclipse.fennec.event.atlas.model.mapping.AdminMapping#getElevationRef <em>Elevation Ref</em>}</li>
 *   <li>{@link org.eclipse.fennec.event.atlas.model.mapping.AdminMapping#getFriendlyNameMapping <em>Friendly Name Mapping</em>}</li>
 *   <li>{@link org.eclipse.fennec.event.atlas.model.mapping.AdminMapping#getLatitudeMapping <em>Latitude Mapping</em>}</li>
 *   <li>{@link org.eclipse.fennec.event.atlas.model.mapping.AdminMapping#getLongitudeMapping <em>Longitude Mapping</em>}</li>
 *   <li>{@link org.eclipse.fennec.event.atlas.model.mapping.AdminMapping#getElevationMapping <em>Elevation Mapping</em>}</li>
 *   <li>{@link org.eclipse.fennec.event.atlas.model.mapping.AdminMapping#isExtendAdmin <em>Extend Admin</em>}</li>
 * </ul>
 *
 * @see org.eclipse.fennec.event.atlas.model.mapping.MappingPackage#getAdminMapping()
 * @model
 * @generated
 */
@ProviderType
public interface AdminMapping extends ServiceMapping {
	/**
	 * Returns the value of the '<em><b>Friendly Name</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the value of the '<em>Friendly Name</em>' attribute.
	 * @see #setFriendlyName(String)
	 * @see org.eclipse.fennec.event.atlas.model.mapping.MappingPackage#getAdminMapping_FriendlyName()
	 * @model
	 * @generated
	 */
	String getFriendlyName();

	/**
	 * Sets the value of the '{@link org.eclipse.fennec.event.atlas.model.mapping.AdminMapping#getFriendlyName <em>Friendly Name</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @param value the new value of the '<em>Friendly Name</em>' attribute.
	 * @see #getFriendlyName()
	 * @generated
	 */
	void setFriendlyName(String value);

	/**
	 * Returns the value of the '<em><b>Friendly Name Feature</b></em>' reference list.
	 * The list contents are of type {@link org.eclipse.emf.ecore.EStructuralFeature}.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the value of the '<em>Friendly Name Feature</em>' reference list.
	 * @see org.eclipse.fennec.event.atlas.model.mapping.MappingPackage#getAdminMapping_FriendlyNameFeature()
	 * @model
	 * @generated
	 */
	EList<EStructuralFeature> getFriendlyNameFeature();

	/**
	 * Returns the value of the '<em><b>Provider Package</b></em>' reference.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the value of the '<em>Provider Package</em>' reference.
	 * @see #setProviderPackage(EPackage)
	 * @see org.eclipse.fennec.event.atlas.model.mapping.MappingPackage#getAdminMapping_ProviderPackage()
	 * @model
	 * @generated
	 */
	EPackage getProviderPackage();

	/**
	 * Sets the value of the '{@link org.eclipse.fennec.event.atlas.model.mapping.AdminMapping#getProviderPackage <em>Provider Package</em>}' reference.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @param value the new value of the '<em>Provider Package</em>' reference.
	 * @see #getProviderPackage()
	 * @generated
	 */
	void setProviderPackage(EPackage value);

	/**
	 * Returns the value of the '<em><b>Latitude</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the value of the '<em>Latitude</em>' attribute.
	 * @see #setLatitude(Double)
	 * @see org.eclipse.fennec.event.atlas.model.mapping.MappingPackage#getAdminMapping_Latitude()
	 * @model
	 * @generated
	 */
	Double getLatitude();

	/**
	 * Sets the value of the '{@link org.eclipse.fennec.event.atlas.model.mapping.AdminMapping#getLatitude <em>Latitude</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @param value the new value of the '<em>Latitude</em>' attribute.
	 * @see #getLatitude()
	 * @generated
	 */
	void setLatitude(Double value);

	/**
	 * Returns the value of the '<em><b>Longitude</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the value of the '<em>Longitude</em>' attribute.
	 * @see #setLongitude(Double)
	 * @see org.eclipse.fennec.event.atlas.model.mapping.MappingPackage#getAdminMapping_Longitude()
	 * @model
	 * @generated
	 */
	Double getLongitude();

	/**
	 * Sets the value of the '{@link org.eclipse.fennec.event.atlas.model.mapping.AdminMapping#getLongitude <em>Longitude</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @param value the new value of the '<em>Longitude</em>' attribute.
	 * @see #getLongitude()
	 * @generated
	 */
	void setLongitude(Double value);

	/**
	 * Returns the value of the '<em><b>Elevation</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the value of the '<em>Elevation</em>' attribute.
	 * @see #setElevation(Double)
	 * @see org.eclipse.fennec.event.atlas.model.mapping.MappingPackage#getAdminMapping_Elevation()
	 * @model
	 * @generated
	 */
	Double getElevation();

	/**
	 * Sets the value of the '{@link org.eclipse.fennec.event.atlas.model.mapping.AdminMapping#getElevation <em>Elevation</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @param value the new value of the '<em>Elevation</em>' attribute.
	 * @see #getElevation()
	 * @generated
	 */
	void setElevation(Double value);

	/**
	 * Returns the value of the '<em><b>Latitude Ref</b></em>' reference list.
	 * The list contents are of type {@link org.eclipse.emf.ecore.EStructuralFeature}.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the value of the '<em>Latitude Ref</em>' reference list.
	 * @see org.eclipse.fennec.event.atlas.model.mapping.MappingPackage#getAdminMapping_LatitudeRef()
	 * @model
	 * @generated
	 */
	EList<EStructuralFeature> getLatitudeRef();

	/**
	 * Returns the value of the '<em><b>Longitude Ref</b></em>' reference list.
	 * The list contents are of type {@link org.eclipse.emf.ecore.EStructuralFeature}.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the value of the '<em>Longitude Ref</em>' reference list.
	 * @see org.eclipse.fennec.event.atlas.model.mapping.MappingPackage#getAdminMapping_LongitudeRef()
	 * @model
	 * @generated
	 */
	EList<EStructuralFeature> getLongitudeRef();

	/**
	 * Returns the value of the '<em><b>Elevation Ref</b></em>' reference list.
	 * The list contents are of type {@link org.eclipse.emf.ecore.EStructuralFeature}.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the value of the '<em>Elevation Ref</em>' reference list.
	 * @see org.eclipse.fennec.event.atlas.model.mapping.MappingPackage#getAdminMapping_ElevationRef()
	 * @model
	 * @generated
	 */
	EList<EStructuralFeature> getElevationRef();

	/**
	 * Returns the value of the '<em><b>Friendly Name Mapping</b></em>' containment reference.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * <!-- begin-model-doc -->
	 * The richer source of the admin's friendly name, winning over friendlyNameFeature when both are set.
	 * 
	 * Carries the producers a   feature path alone cannot express - an expression, a function, a collection selection - plus a converterId that post-processes the result. "'ILSA ' +   self.intersectionName" is the case it exists for: friendlyNameFeature can name a feature but cannot combine one with anything.
	 * 
	 * The friendlyName attribute stays   the constant: it answers when this mapping produces nothing.
	 * <!-- end-model-doc -->
	 * @return the value of the '<em>Friendly Name Mapping</em>' containment reference.
	 * @see #setFriendlyNameMapping(FeatureMapping)
	 * @see org.eclipse.fennec.event.atlas.model.mapping.MappingPackage#getAdminMapping_FriendlyNameMapping()
	 * @model containment="true"
	 * @generated
	 */
	FeatureMapping getFriendlyNameMapping();

	/**
	 * Sets the value of the '{@link org.eclipse.fennec.event.atlas.model.mapping.AdminMapping#getFriendlyNameMapping <em>Friendly Name Mapping</em>}' containment reference.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @param value the new value of the '<em>Friendly Name Mapping</em>' containment reference.
	 * @see #getFriendlyNameMapping()
	 * @generated
	 */
	void setFriendlyNameMapping(FeatureMapping value);

	/**
	 * Returns the value of the '<em><b>Latitude Mapping</b></em>' containment reference.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * <!-- begin-model-doc -->
	 * The richer source of the admin location's latitude, winning over latitudeRef when both are set.
	 * 
	 * A latitudeRef is a plain path,   so it can only read a value that stands alone in the payload. Where two coordinates share one value - a bbox typed as a Double[] - a converter or an expression takes the   element, and that needs this slot.
	 * 
	 * The latitude attribute stays the constant: it answers when this mapping produces nothing.
	 * <!-- end-model-doc -->
	 * @return the value of the '<em>Latitude Mapping</em>' containment reference.
	 * @see #setLatitudeMapping(FeatureMapping)
	 * @see org.eclipse.fennec.event.atlas.model.mapping.MappingPackage#getAdminMapping_LatitudeMapping()
	 * @model containment="true"
	 * @generated
	 */
	FeatureMapping getLatitudeMapping();

	/**
	 * Sets the value of the '{@link org.eclipse.fennec.event.atlas.model.mapping.AdminMapping#getLatitudeMapping <em>Latitude Mapping</em>}' containment reference.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @param value the new value of the '<em>Latitude Mapping</em>' containment reference.
	 * @see #getLatitudeMapping()
	 * @generated
	 */
	void setLatitudeMapping(FeatureMapping value);

	/**
	 * Returns the value of the '<em><b>Longitude Mapping</b></em>' containment reference.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * <!-- begin-model-doc -->
	 * The richer source of the admin location's longitude, winning over longitudeRef when both are set. See latitudeMapping.
	 * 
	 * The   longitude attribute stays the constant.
	 * <!-- end-model-doc -->
	 * @return the value of the '<em>Longitude Mapping</em>' containment reference.
	 * @see #setLongitudeMapping(FeatureMapping)
	 * @see org.eclipse.fennec.event.atlas.model.mapping.MappingPackage#getAdminMapping_LongitudeMapping()
	 * @model containment="true"
	 * @generated
	 */
	FeatureMapping getLongitudeMapping();

	/**
	 * Sets the value of the '{@link org.eclipse.fennec.event.atlas.model.mapping.AdminMapping#getLongitudeMapping <em>Longitude Mapping</em>}' containment reference.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @param value the new value of the '<em>Longitude Mapping</em>' containment reference.
	 * @see #getLongitudeMapping()
	 * @generated
	 */
	void setLongitudeMapping(FeatureMapping value);

	/**
	 * Returns the value of the '<em><b>Elevation Mapping</b></em>' containment reference.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * <!-- begin-model-doc -->
	 * The richer source of the admin location's elevation, winning over elevationRef when both are set. See latitudeMapping.
	 * 
	 * The   elevation attribute stays the constant.
	 * <!-- end-model-doc -->
	 * @return the value of the '<em>Elevation Mapping</em>' containment reference.
	 * @see #setElevationMapping(FeatureMapping)
	 * @see org.eclipse.fennec.event.atlas.model.mapping.MappingPackage#getAdminMapping_ElevationMapping()
	 * @model containment="true"
	 * @generated
	 */
	FeatureMapping getElevationMapping();

	/**
	 * Sets the value of the '{@link org.eclipse.fennec.event.atlas.model.mapping.AdminMapping#getElevationMapping <em>Elevation Mapping</em>}' containment reference.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @param value the new value of the '<em>Elevation Mapping</em>' containment reference.
	 * @see #getElevationMapping()
	 * @generated
	 */
	void setElevationMapping(FeatureMapping value);

	/**
	 * Returns the value of the '<em><b>Extend Admin</b></em>' attribute.
	 * The default value is <code>"false"</code>.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * <!-- begin-model-doc -->
	 * Allows resource mappings whose mid is no resource of the admin service. The engine then builds an    Admin subclass carrying them and retypes the provider's admin once. Such a value is readable as a resource and travels on the event bus,    but it is invisible to the snapshot consumers - SensorThings, the provider description and filters. Off by default, so a wrong mid stays a    warning instead of becoming a new admin resource.
	 * <!-- end-model-doc -->
	 * @return the value of the '<em>Extend Admin</em>' attribute.
	 * @see #setExtendAdmin(boolean)
	 * @see org.eclipse.fennec.event.atlas.model.mapping.MappingPackage#getAdminMapping_ExtendAdmin()
	 * @model default="false"
	 * @generated
	 */
	boolean isExtendAdmin();

	/**
	 * Sets the value of the '{@link org.eclipse.fennec.event.atlas.model.mapping.AdminMapping#isExtendAdmin <em>Extend Admin</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @param value the new value of the '<em>Extend Admin</em>' attribute.
	 * @see #isExtendAdmin()
	 * @generated
	 */
	void setExtendAdmin(boolean value);

} // AdminMapping
