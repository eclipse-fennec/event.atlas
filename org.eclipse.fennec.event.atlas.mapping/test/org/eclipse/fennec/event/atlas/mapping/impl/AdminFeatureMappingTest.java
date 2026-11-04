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
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.eclipse.emf.ecore.EAttribute;
import org.eclipse.emf.ecore.EClass;
import org.eclipse.emf.ecore.EDataType;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.EPackage;
import org.eclipse.emf.ecore.EcoreFactory;
import org.eclipse.emf.ecore.EcorePackage;
import org.eclipse.emf.ecore.util.EcoreUtil;
import org.eclipse.fennec.event.atlas.mapping.ExpressionEvaluator;
import org.eclipse.fennec.event.atlas.mapping.ValueMappingException;
import org.eclipse.fennec.event.atlas.mapping.converters.ValueConverter;
import org.eclipse.fennec.event.atlas.mapping.converters.impl.ValueConverterRegistryImpl;
import org.eclipse.fennec.event.atlas.model.mapping.AdminMapping;
import org.eclipse.fennec.event.atlas.model.mapping.FeatureMapping;
import org.eclipse.fennec.event.atlas.model.mapping.MappingFactory;
import org.eclipse.fennec.event.atlas.model.mapping.NameMapping;
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
 * The admin slots that take a FeatureMapping instead of a bare path.
 * <p>
 * The case they exist for is ILSA: its position is {@code geoJson.bbox[0]} and {@code [1]}, two
 * numbers inside <em>one</em> attribute of datatype {@code Array1D} - a {@code java.lang.Double[]},
 * not an EMF list - so neither a feature path nor a collectionIndex can reach them. A converter
 * on the slot can. The same slot answers {@code 'ILSA ' + intersectionName}, which a path cannot
 * express either.
 */
public class AdminFeatureMappingTest {

	private final EPackage ePackage = EcoreFactory.eINSTANCE.createEPackage();
	private final EClass config = EcoreFactory.eINSTANCE.createEClass();
	private final EAttribute name = attribute("name", EcorePackage.Literals.ESTRING);
	private final EAttribute elevation = attribute("elevation", EcorePackage.Literals.EDOUBLE_OBJECT);
	/** The ILSA shape: one attribute whose value is a Java array. */
	private final EDataType array1D = EcoreFactory.eINSTANCE.createEDataType();
	private final EAttribute bbox = EcoreFactory.eINSTANCE.createEAttribute();

	private final SensinactDigitalTwin twin = mock(SensinactDigitalTwin.class);
	private final SensinactResource locationResource = mock(SensinactResource.class);
	private final SensinactResource friendlyNameResource = mock(SensinactResource.class);

	private final ValueConverterRegistryImpl converters = new ValueConverterRegistryImpl();

	/** Answers the one expression it knows, as a real evaluator answers what parses. */
	private final ExpressionEvaluator evaluator = (expression, self) -> "'ILSA ' + self.name"
			.equals(expression) ? Optional.of("ILSA " + self.eGet(name)) : Optional.empty();

	@BeforeEach
	void modelAndTwin() {
		array1D.setName("Array1D");
		array1D.setInstanceClassName("java.lang.Double[]");
		bbox.setName("bbox");
		bbox.setEType(array1D);
		ePackage.setName("configs");
		ePackage.setNsPrefix("configs");
		ePackage.setNsURI("http://example.org/event.atlas/configs/1.0");
		config.setName("Config");
		config.getEStructuralFeatures().addAll(List.of(name, elevation, bbox));
		ePackage.getEClassifiers().add(array1D);
		ePackage.getEClassifiers().add(config);

		SensinactService admin = mock(SensinactService.class);
		doReturn(Map.of("friendlyName", friendlyNameResource, "location", locationResource))
				.when(admin).getResources();
		SensinactProvider provider = mock(SensinactProvider.class);
		doReturn(Map.of("admin", admin)).when(provider).getServices();
		when(twin.getProvider(anyString(), anyString())).thenReturn(provider);

		// What the UDP side would publish: take one element out of a Double[].
		converters.addConverter(value -> ((Double[]) value)[0], Map.of(ValueConverter.CONVERTER_ID, "first"));
		converters.addConverter(value -> ((Double[]) value)[1], Map.of(ValueConverter.CONVERTER_ID, "second"));
	}

	@Test
	@DisplayName("A converter on the location slots reads two numbers out of one array value")
	void converterOnTheLocation_readsTheBbox() throws ValueMappingException {
		AdminMapping admin = admin();
		admin.setLongitudeMapping(featureMapping(bbox, "first", null));
		admin.setLatitudeMapping(featureMapping(bbox, "second", null));

		map(admin);

		Point point = writtenPoint();
		assertEquals(11.6, point.coordinates().longitude(), "bbox[0]");
		assertEquals(50.9, point.coordinates().latitude(), "bbox[1]");
	}

	@Test
	@DisplayName("An expression on the friendly name combines a literal with a feature")
	void expressionOnTheFriendlyName_isWritten() throws ValueMappingException {
		AdminMapping admin = admin();
		admin.setFriendlyNameMapping(featureMapping(null, null, "'ILSA ' + self.name"));

		map(admin);

		verify(friendlyNameResource).setValue(eq("ILSA Lobeda"), any(Instant.class));
	}

	@Test
	@DisplayName("A mapping replaces the plain reference rather than falling back to it")
	void mapping_replacesTheReference() throws ValueMappingException {
		AdminMapping admin = admin();
		admin.getFriendlyNameFeature().add(name);
		// The producer knows no such expression, so it yields nothing - and the path must not
		// answer in its place. The constant does.
		admin.setFriendlyName("fallback");
		admin.setFriendlyNameMapping(featureMapping(null, null, "self.unknown"));

		map(admin);

		verify(friendlyNameResource).setValue(eq("fallback"), any(Instant.class));
	}

	@Test
	@DisplayName("elevationMapping reaches the third coordinate, which elevationRef never did")
	void elevationMapping_isRead() throws ValueMappingException {
		AdminMapping admin = admin();
		admin.setLongitudeMapping(featureMapping(bbox, "first", null));
		admin.setLatitudeMapping(featureMapping(bbox, "second", null));
		admin.setElevationMapping(featureMapping(elevation, null, null));

		map(admin);

		assertEquals(161.0, writtenPoint().coordinates().elevation());
	}

	@Test
	@DisplayName("An admin that declares neither a mapping nor a reference is untouched")
	void nothingDeclared_nothingWritten() throws ValueMappingException {
		map(admin());

		verify(locationResource, never()).setValue(any(), any(Instant.class));
	}

	private Point writtenPoint() {
		ArgumentCaptor<Object> value = ArgumentCaptor.forClass(Object.class);
		verify(locationResource).setValue(value.capture(), any(Instant.class));
		return (Point) value.getValue();
	}

	private void map(AdminMapping admin) throws ValueMappingException {
		new ValueMapperImpl(twin, null, mapping(admin), null, converters, null, evaluator)
				.mapInstance(instance());
	}

	private FeatureMapping featureMapping(EAttribute path, String converterId, String expression) {
		FeatureMapping featureMapping = MappingFactory.eINSTANCE.createFeatureMapping();
		if (path != null) {
			featureMapping.getFeaturePath().add(path);
		}
		featureMapping.setConverterId(converterId);
		featureMapping.setExpression(expression);
		return featureMapping;
	}

	private AdminMapping admin() {
		NameMapping adminName = MappingFactory.eINSTANCE.createNameMapping();
		adminName.setName("Admin");
		AdminMapping admin = MappingFactory.eINSTANCE.createAdminMapping();
		admin.setMid("admin");
		admin.setName(adminName);
		return admin;
	}

	private ProviderMapping mapping(AdminMapping admin) {
		NameMapping providerName = MappingFactory.eINSTANCE.createNameMapping();
		providerName.setName("lobeda");

		ProviderMapping mapping = MappingFactory.eINSTANCE.createProviderMapping();
		mapping.setMid("configs");
		mapping.setName(providerName);
		mapping.getProviderClasses().add(config);
		mapping.setAdmin(admin);
		return mapping;
	}

	private EObject instance() {
		EObject instance = EcoreUtil.create(config);
		instance.eSet(name, "Lobeda");
		instance.eSet(elevation, 161.0d);
		instance.eSet(bbox, new Double[] { 11.6d, 50.9d });
		return instance;
	}

	private static EAttribute attribute(String attributeName, EDataType type) {
		EAttribute attribute = EcoreFactory.eINSTANCE.createEAttribute();
		attribute.setName(attributeName);
		attribute.setEType(type);
		return attribute;
	}
}
