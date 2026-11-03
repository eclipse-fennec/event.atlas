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
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.Instant;
import java.util.Map;

import org.eclipse.emf.ecore.EAttribute;
import org.eclipse.emf.ecore.EClass;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.EPackage;
import org.eclipse.emf.ecore.EcoreFactory;
import org.eclipse.emf.ecore.EcorePackage;
import org.eclipse.emf.ecore.util.EcoreUtil;
import org.eclipse.fennec.event.atlas.mapping.ValueMappingException;
import org.eclipse.fennec.event.atlas.mapping.converters.ValueConverter;
import org.eclipse.fennec.event.atlas.mapping.converters.impl.ValueConverterRegistryImpl;
import org.eclipse.fennec.event.atlas.model.mapping.MappingFactory;
import org.eclipse.fennec.event.atlas.model.mapping.NameMapping;
import org.eclipse.fennec.event.atlas.model.mapping.ProviderMapping;
import org.eclipse.fennec.event.atlas.model.mapping.ResourceMapping;
import org.eclipse.fennec.event.atlas.model.mapping.ServiceMapping;
import org.eclipse.fennec.event.atlas.model.mapping.TimestampMapping;
import org.eclipse.sensinact.core.twin.SensinactDigitalTwin;
import org.eclipse.sensinact.core.twin.SensinactProvider;
import org.eclipse.sensinact.core.twin.SensinactResource;
import org.eclipse.sensinact.core.twin.SensinactService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

/**
 * converterId is inherited from FeatureMapping, so a NameMapping and a TimestampMapping carry
 * one too. These pin that both are actually consulted - they were silently ignored when the
 * converter was first wired only into the resource value path.
 */
public class ConverterOnNameAndTimestampTest {

	/** 2025-11-03T08:00:00Z, as epoch seconds - what the device sends. */
	private static final long EPOCH_SECONDS = 1762156800L;

	private final EPackage ePackage = EcoreFactory.eINSTANCE.createEPackage();
	private final EClass uplink = EcoreFactory.eINSTANCE.createEClass();
	private final EAttribute device = attribute("device", EcorePackage.Literals.ESTRING);
	private final EAttribute seconds = attribute("seconds", EcorePackage.Literals.ELONG_OBJECT);
	private final EAttribute reading = attribute("reading", EcorePackage.Literals.EINT);

	private final SensinactDigitalTwin twin = mock(SensinactDigitalTwin.class);
	private final SensinactResource readingResource = mock(SensinactResource.class);
	private final ValueConverterRegistryImpl registry = new ValueConverterRegistryImpl();

	@BeforeEach
	void setUp() {
		uplink.setName("Uplink");
		// EcoreUtil.create needs the class to have an owning package with a factory.
		ePackage.setName("uplinks");
		ePackage.setNsPrefix("uplinks");
		ePackage.setNsURI("http://example.org/uplinks");
		ePackage.getEClassifiers().add(uplink);
		uplink.getEStructuralFeatures().add(device);
		uplink.getEStructuralFeatures().add(seconds);
		uplink.getEStructuralFeatures().add(reading);

		SensinactService data = mock(SensinactService.class);
		doReturn(Map.of("reading", readingResource)).when(data).getResources();
		SensinactProvider provider = mock(SensinactProvider.class);
		doReturn(Map.of("data", data)).when(provider).getServices();
		when(twin.getProvider(anyString(), anyString())).thenReturn(provider);

		registry.addConverter(value -> "converted:" + value, Map.of(ValueConverter.CONVERTER_ID, "mark"));
		registry.addConverter(value -> ((Number) value).longValue() * 1000L,
				Map.of(ValueConverter.CONVERTER_ID, "sec2ms"));
	}

	@Test
	@DisplayName("A name mapping's converter decides the provider id")
	void nameConverter_isApplied() throws ValueMappingException {
		map(mapping(name("mark", null), timestamp(null)));

		assertEquals("converted:dev-1", providerId());
	}

	@Test
	@DisplayName("Without a converter the name is the raw feature value")
	void nameWithoutConverter_isTheRawValue() throws ValueMappingException {
		map(mapping(name(null, null), timestamp(null)));

		assertEquals("dev-1", providerId());
	}

	@Test
	@DisplayName("A dropped name falls back to the mapping's static name")
	void droppedName_fallsBackToTheLiteral() throws ValueMappingException {
		registry.addConverter(value -> null, Map.of(ValueConverter.CONVERTER_ID, "swallow"));

		map(mapping(name("swallow", "fallback-name"), timestamp(null)));

		assertEquals("fallback-name", providerId());
	}

	@Test
	@DisplayName("A timestamp mapping's converter decides the instant a value is stamped with")
	void timestampConverter_isApplied() throws ValueMappingException {
		map(mapping(name(null, null), timestamp("sec2ms")));

		assertEquals(Instant.ofEpochSecond(EPOCH_SECONDS), writtenTimestamp(),
				"epoch seconds became epoch millis, which is what the engine can read");
	}

	@Test
	@DisplayName("Without a converter the same payload is read as epoch millis and lands elsewhere")
	void timestampWithoutConverter_isNotTheSameInstant() throws ValueMappingException {
		map(mapping(name(null, null), timestamp(null)));

		assertNotEquals(Instant.ofEpochSecond(EPOCH_SECONDS), writtenTimestamp(),
				"the raw value is read as milliseconds - this is the gap the converter closes");
	}

	@Test
	@DisplayName("An unknown converter leaves the timestamp as it was read")
	void unknownTimestampConverter_passesThrough() throws ValueMappingException {
		map(mapping(name(null, null), timestamp("not-deployed")));

		assertEquals(Instant.ofEpochMilli(EPOCH_SECONDS), writtenTimestamp());
	}

	private void map(ProviderMapping mapping) throws ValueMappingException {
		EObject instance = EcoreUtil.create(uplink);
		instance.eSet(device, "dev-1");
		instance.eSet(seconds, EPOCH_SECONDS);
		instance.eSet(reading, 42);
		new ValueMapperImpl(twin, mapping, null, registry).mapInstance(instance);
	}

	private String providerId() {
		ArgumentCaptor<String> id = ArgumentCaptor.forClass(String.class);
		verify(twin).getProvider(anyString(), id.capture());
		return id.getValue();
	}

	private Instant writtenTimestamp() {
		ArgumentCaptor<Object> value = ArgumentCaptor.forClass(Object.class);
		ArgumentCaptor<Instant> stamp = ArgumentCaptor.forClass(Instant.class);
		verify(readingResource).setValue(value.capture(), stamp.capture());
		return stamp.getValue();
	}

	private NameMapping name(String converterId, String literal) {
		NameMapping name = MappingFactory.eINSTANCE.createNameMapping();
		name.getFeaturePath().add(device);
		name.setConverterId(converterId);
		name.setName(literal);
		return name;
	}

	private TimestampMapping timestamp(String converterId) {
		TimestampMapping stamp = MappingFactory.eINSTANCE.createTimestampMapping();
		stamp.getFeaturePath().add(seconds);
		stamp.setConverterId(converterId);
		return stamp;
	}

	private ProviderMapping mapping(NameMapping name, TimestampMapping timestamp) {
		MappingFactory f = MappingFactory.eINSTANCE;

		ResourceMapping resource = f.createResourceMapping();
		resource.setMid("reading");
		resource.setName("reading");
		resource.setEType(EcorePackage.Literals.EINT);
		resource.getValueFeature().add(reading);

		ServiceMapping service = f.createServiceMapping();
		service.setMid("data");
		service.setTimestamp(timestamp);
		service.getResources().add(resource);

		ProviderMapping mapping = f.createProviderMapping();
		mapping.setMid("uplinks");
		mapping.setName(name);
		mapping.getProviderClasses().add(uplink);
		mapping.getServices().add(service);
		return mapping;
	}

	private static EAttribute attribute(String name, org.eclipse.emf.ecore.EDataType type) {
		EAttribute attribute = EcoreFactory.eINSTANCE.createEAttribute();
		attribute.setName(name);
		attribute.setEType(type);
		return attribute;
	}
}
