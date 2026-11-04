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

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.Instant;
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
import org.eclipse.fennec.event.atlas.mapping.ChangeRuleFilter;
import org.eclipse.fennec.event.atlas.mapping.ExpressionEvaluator;
import org.eclipse.fennec.event.atlas.mapping.ValueMappingException;
import org.eclipse.fennec.event.atlas.model.mapping.MappingFactory;
import org.eclipse.fennec.event.atlas.model.mapping.MetadataMapping;
import org.eclipse.fennec.event.atlas.model.mapping.NameMapping;
import org.eclipse.fennec.event.atlas.model.mapping.ProviderMapping;
import org.eclipse.fennec.event.atlas.model.mapping.ResourceMapping;
import org.eclipse.fennec.event.atlas.model.mapping.ServiceMapping;
import org.eclipse.fennec.event.atlas.model.mapping.ValueMapping;
import org.eclipse.sensinact.core.twin.SensinactDigitalTwin;
import org.eclipse.sensinact.core.twin.SensinactProvider;
import org.eclipse.sensinact.core.twin.SensinactResource;
import org.eclipse.sensinact.core.twin.SensinactService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/**
 * Metadata whose value the payload decides.
 */
public class ResourceMetadataMappingTest {

	private final EPackage ePackage = EcoreFactory.eINSTANCE.createEPackage();
	private final EClass signal = EcoreFactory.eINSTANCE.createEClass();
	private final EAttribute state = attribute("state", EcorePackage.Literals.ESTRING);
	private final EAttribute signalType = attribute("signalType", EcorePackage.Literals.ESTRING);
	private final EAttribute group = attribute("group", EcorePackage.Literals.ESTRING);

	private final SensinactDigitalTwin twin = mock(SensinactDigitalTwin.class);
	private final SensinactResource color = mock(SensinactResource.class);

	@BeforeEach
	void modelAndTwin() {
		ePackage.setName("signals");
		ePackage.setNsPrefix("signals");
		ePackage.setNsURI("http://example.org/event.atlas/signals/1.0");
		signal.setName("Signal");
		signal.getEStructuralFeatures().add(state);
		signal.getEStructuralFeatures().add(signalType);
		signal.getEStructuralFeatures().add(group);
		ePackage.getEClassifiers().add(signal);

		SensinactService service = mock(SensinactService.class);
		doReturn(Map.of("color", color)).when(service).getResources();
		SensinactProvider provider = mock(SensinactProvider.class);
		doReturn(Map.of("signal", service)).when(provider).getServices();
		when(twin.getProvider(anyString(), anyString())).thenReturn(provider);
	}

	@Test
	@DisplayName("Two metadata entries are read from the payload beside the value")
	void metadataFromThePayload_isWritten() throws ValueMappingException {
		map(mapping(metadata("type", signalType), metadata("signalGroup", group)), null);

		verify(color).setMetadataValue(eq("type"), eq("VEHICLE"), any(Instant.class));
		verify(color).setMetadataValue(eq("signalGroup"), eq("SG-3"), any(Instant.class));
		verify(color).setValue(eq("green"), any(Instant.class));
	}

	@Test
	@DisplayName("An entry whose producer yields nothing is left as it is, not unset")
	void producerYieldsNothing_leavesTheEntry() throws ValueMappingException {
		EObject instance = instance();
		instance.eSet(signalType, null);

		new ValueMapperImpl(twin, null, mapping(metadata("type", signalType)), null, null, null, null)
				.mapInstance(instance);

		verify(color, never()).setMetadataValue(eq("type"), any(), any(Instant.class));
		verify(color, never()).unsetMetadataValue(anyString(), any(Instant.class));
	}

	@Test
	@DisplayName("Metadata is written although the change rule drops the value")
	void changeRuleDropsTheValue_metadataIsStillWritten() throws ValueMappingException {
		ChangeRuleFilter filter = mock(ChangeRuleFilter.class);
		when(filter.accept(anyString(), anyString(), anyString(), any(), any(), any())).thenReturn(false);

		map(mapping(metadata("type", signalType)), filter);

		verify(color).setMetadataValue(eq("type"), eq("VEHICLE"), any(Instant.class));
		verify(color, never()).setValue(any(), any(Instant.class));
	}

	@Test
	@DisplayName("An expression can compute a metadata value")
	void expression_computesTheMetadata() throws ValueMappingException {
		ExpressionEvaluator evaluator = (expression, self) -> "self.signalType.toLowerCase()"
				.equals(expression) ? Optional.of("vehicle") : Optional.empty();
		MetadataMapping computed = MappingFactory.eINSTANCE.createMetadataMapping();
		computed.setKey("type");
		computed.setExpression("self.signalType.toLowerCase()");

		new ValueMapperImpl(twin, null, mapping(computed), null, null, null, evaluator)
				.mapInstance(instance());

		verify(color).setMetadataValue(eq("type"), eq("vehicle"), any(Instant.class));
	}

	@Test
	@DisplayName("A failing metadata entry does not cost the reading it describes")
	void metadataFailure_doesNotFailThePush() throws ValueMappingException {
		doThrow(new IllegalStateException("twin says no")).when(color)
				.setMetadataValue(anyString(), any(), any(Instant.class));

		map(mapping(metadata("type", signalType)), null);

		verify(color).setValue(eq("green"), any(Instant.class));
	}

	private void map(ProviderMapping mapping, ChangeRuleFilter filter) throws ValueMappingException {
		new ValueMapperImpl(twin, null, mapping, filter, null, null, null).mapInstance(instance());
	}

	private MetadataMapping metadata(String key, EAttribute path) {
		MetadataMapping metadataMapping = MappingFactory.eINSTANCE.createMetadataMapping();
		metadataMapping.setKey(key);
		metadataMapping.getFeaturePath().add(path);
		return metadataMapping;
	}

	private ProviderMapping mapping(MetadataMapping... metadata) {
		ValueMapping valueMapping = MappingFactory.eINSTANCE.createValueMapping();
		valueMapping.getFeaturePath().add(state);

		ResourceMapping resource = MappingFactory.eINSTANCE.createResourceMapping();
		resource.setMid("color");
		resource.setName("color");
		resource.setEType(EcorePackage.Literals.ESTRING);
		resource.setValueMapping(valueMapping);
		for (MetadataMapping entry : metadata) {
			resource.getMetadata().add(entry);
		}

		ServiceMapping service = MappingFactory.eINSTANCE.createServiceMapping();
		service.setMid("signal");
		service.getResources().add(resource);

		NameMapping providerName = MappingFactory.eINSTANCE.createNameMapping();
		providerName.setName("intersection-1");

		ProviderMapping mapping = MappingFactory.eINSTANCE.createProviderMapping();
		mapping.setMid("signals");
		mapping.setName(providerName);
		mapping.getProviderClasses().add(signal);
		mapping.getServices().add(service);
		return mapping;
	}

	private EObject instance() {
		EObject instance = EcoreUtil.create(signal);
		instance.eSet(state, "green");
		instance.eSet(signalType, "VEHICLE");
		instance.eSet(group, "SG-3");
		return instance;
	}

	private static EAttribute attribute(String name, EDataType type) {
		EAttribute attribute = EcoreFactory.eINSTANCE.createEAttribute();
		attribute.setName(name);
		attribute.setEType(type);
		return attribute;
	}
}
