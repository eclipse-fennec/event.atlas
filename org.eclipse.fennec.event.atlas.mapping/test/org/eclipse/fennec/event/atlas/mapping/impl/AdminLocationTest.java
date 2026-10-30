/**
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
 *     Data In Motion - initial API and implementation
 */
package org.eclipse.fennec.event.atlas.mapping.impl;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.Instant;
import java.util.List;
import java.util.Map;

import org.eclipse.emf.ecore.EAttribute;
import org.eclipse.emf.ecore.EClass;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.EPackage;
import org.eclipse.emf.ecore.EcoreFactory;
import org.eclipse.emf.ecore.EcorePackage;
import org.eclipse.emf.ecore.util.EcoreUtil;
import org.eclipse.fennec.event.atlas.mapping.ValueMappingException;
import org.eclipse.fennec.event.atlas.model.mapping.AdminMapping;
import org.eclipse.fennec.event.atlas.model.mapping.MappingFactory;
import org.eclipse.fennec.event.atlas.model.mapping.ProviderMapping;
import org.eclipse.sensinact.core.twin.SensinactDigitalTwin;
import org.eclipse.sensinact.core.twin.SensinactProvider;
import org.eclipse.sensinact.core.twin.SensinactResource;
import org.eclipse.sensinact.core.twin.SensinactService;
import org.eclipse.sensinact.gateway.geojson.Point;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

/**
 * The admin location of a provider may belong to someone else - an adapter that knows where the
 * device stands - so a mapping only writes it when it has one, from the instance or statically.
 */
public class AdminLocationTest {

	private final EPackage ePackage = EcoreFactory.eINSTANCE.createEPackage();
	private final EClass uplink = EcoreFactory.eINSTANCE.createEClass();
	private final EAttribute device = attribute("device", EcorePackage.Literals.ESTRING);
	private final EAttribute latitude = attribute("latitude", EcorePackage.Literals.EDOUBLE_OBJECT);
	private final EAttribute longitude = attribute("longitude", EcorePackage.Literals.EDOUBLE_OBJECT);

	private final SensinactDigitalTwin twin = mock(SensinactDigitalTwin.class);
	private final SensinactResource locationResource = mock(SensinactResource.class);

	@BeforeEach
	void modelAndTwin() {
		ePackage.setName("uplink");
		ePackage.setNsPrefix("uplink");
		ePackage.setNsURI("http://example.org/event.atlas/uplink/1.0");
		uplink.setName("Uplink");
		uplink.getEStructuralFeatures().addAll(List.of(device, latitude, longitude));
		ePackage.getEClassifiers().add(uplink);

		SensinactService admin = mock(SensinactService.class);
		doReturn(Map.of("friendlyName", mock(SensinactResource.class), "location", locationResource)).when(admin).getResources();
		SensinactProvider provider = mock(SensinactProvider.class);
		doReturn(Map.of("admin", admin)).when(provider).getServices();
		when(twin.getProvider(anyString(), anyString())).thenReturn(provider);
	}

	@Test
	@DisplayName("A mapping without a location leaves the resource alone")
	void noLocationMapped_nothingWritten() throws ValueMappingException {
		new ValueMapperImpl(twin, mapping(admin())).mapInstance(uplink("dev-1", 50.9, 11.6));

		verify(locationResource, never()).setValue(any(), any(Instant.class));
	}

	@Test
	@DisplayName("latitudeRef/longitudeRef read the point from the instance")
	void referencedLocation_isWritten() throws ValueMappingException {
		AdminMapping admin = admin();
		admin.getLatitudeRef().add(latitude);
		admin.getLongitudeRef().add(longitude);

		new ValueMapperImpl(twin, mapping(admin)).mapInstance(uplink("dev-1", 50.9, 11.6));

		Point point = writtenPoint();
		assertEquals(50.9, point.coordinates().latitude());
		assertEquals(11.6, point.coordinates().longitude());
	}

	@Test
	@DisplayName("A payload without the referenced values leaves the resource alone")
	void referencedLocationAbsent_nothingWritten() throws ValueMappingException {
		AdminMapping admin = admin();
		admin.getLatitudeRef().add(latitude);
		admin.getLongitudeRef().add(longitude);

		new ValueMapperImpl(twin, mapping(admin)).mapInstance(uplink("dev-1", null, null));

		verify(locationResource, never()).setValue(any(), any(Instant.class));
	}

	@Test
	@DisplayName("The static latitude/longitude of the mapping are written when nothing is referenced")
	void staticLocation_isWritten() throws ValueMappingException {
		AdminMapping admin = admin();
		admin.setLatitude(50.9271);
		admin.setLongitude(11.5892);

		new ValueMapperImpl(twin, mapping(admin)).mapInstance(uplink("dev-1", null, null));

		assertEquals(50.9271, writtenPoint().coordinates().latitude());
	}

	private Point writtenPoint() {
		ArgumentCaptor<Object> value = ArgumentCaptor.forClass(Object.class);
		verify(locationResource).setValue(value.capture(), any(Instant.class));
		return (Point) value.getValue();
	}

	private ProviderMapping mapping(AdminMapping admin) {
		ProviderMapping mapping = MappingFactory.eINSTANCE.createProviderMapping();
		mapping.setMid("uplinks");
		mapping.setName(MappingFactory.eINSTANCE.createNameMapping());
		mapping.getName().getFeaturePath().add(device);
		mapping.getProviderClasses().add(uplink);
		mapping.setAdmin(admin);
		return mapping;
	}

	private static AdminMapping admin() {
		AdminMapping admin = MappingFactory.eINSTANCE.createAdminMapping();
		admin.setMid("admin");
		return admin;
	}

	private EObject uplink(String deviceName, Double lat, Double lon) {
		EObject instance = EcoreUtil.create(uplink);
		instance.eSet(device, deviceName);
		if (lat != null) {
			instance.eSet(latitude, lat);
			instance.eSet(longitude, lon);
		}
		return instance;
	}

	private static EAttribute attribute(String name, org.eclipse.emf.ecore.EDataType type) {
		EAttribute attribute = EcoreFactory.eINSTANCE.createEAttribute();
		attribute.setName(name);
		attribute.setEType(type);
		return attribute;
	}
}
