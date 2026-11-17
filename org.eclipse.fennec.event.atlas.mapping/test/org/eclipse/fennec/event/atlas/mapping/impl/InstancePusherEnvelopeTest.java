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
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.lang.reflect.Field;
import java.util.List;

import org.eclipse.emf.ecore.EClass;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.EPackage;
import org.eclipse.emf.ecore.EcoreFactory;
import org.eclipse.emf.ecore.EcorePackage;
import org.eclipse.emf.ecore.util.EcoreUtil;
import org.eclipse.fennec.event.atlas.mapping.ProviderMappingRegistry;
import org.eclipse.sensinact.core.command.GatewayThread;
import org.eclipse.fennec.event.atlas.model.mapping.MappingFactory;
import org.eclipse.fennec.event.atlas.model.mapping.MappingSelector;
import org.eclipse.fennec.event.atlas.model.mapping.ProviderMapping;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import org.osgi.util.promise.PromiseFactory;

import io.cloudevents.model.ce.CloudEvent;
import io.cloudevents.model.ce.CloudEventsFactory;
import io.cloudevents.model.ce.CloudEventsPackage;

/**
 * Which EClass an enveloped push looks mappings up by, and what happens to an envelope that
 * reaches the {@code EObject} entry point.
 * <p>
 * Both answers are decided before the gateway is touched - an EClass nothing is registered
 * for returns 0 straight away - so no sensinact runtime is needed here; the OSGi tests cover
 * the push itself.
 * @author Ilenia Salvadori
 */
@ExtendWith(MockitoExtension.class)
public class InstancePusherEnvelopeTest {

	@Mock
	private ProviderMappingRegistry mappingRegistry;

	@Mock
	private GatewayThread gatewayThread;

	private InstancePusherImpl pusher;
	private EClass sensorEClass;

	@BeforeEach
	void setUp() {
		pusher = new InstancePusherImpl();
		inject(pusher, "mappingRegistry", mappingRegistry);
		inject(pusher, "gatewayThread", gatewayThread);
		sensorEClass = createSensorEClass();
	}

	/**
	 * Has the gateway resolve immediately without running the command: these tests are about
	 * which mappings are handed to it, not about what it does with them.
	 */
	private void whenGatewayExecutes(int applied) {
		when(gatewayThread.execute(any()))
				.thenReturn(new PromiseFactory(PromiseFactory.inlineExecutor()).resolved(applied));
	}

	@Test
	@DisplayName("An enveloped push is looked up by the payload's EClass, not the envelope's")
	void pushInstance_withEnvelope_looksUpThePayloadEClass() {
		CloudEvent event = envelope(EcoreUtil.create(sensorEClass));
		when(mappingRegistry.getProviderMapping(any())).thenReturn(List.of());

		assertEquals(0, pusher.pushInstance(event));

		// the payload-rooted lookup is by the payload's EClass; the envelope's own lookup is
		// the second one, and belongs to envelope-rooted mappings
		verify(mappingRegistry).getProviderMapping(sensorEClass);
	}

	@Test
	@DisplayName("An envelope held in an EObject variable is still treated as an envelope")
	void pushInstance_withEnvelopeAsEObject_delegates() {
		// Java picks the overload from the static type, so without the guard in
		// pushInstance(EObject) the same object would be mapped as a CloudEvent payload -
		// i.e. the mappings registered for ce::CloudEvent rather than for the payload
		EObject event = envelope(EcoreUtil.create(sensorEClass));
		when(mappingRegistry.getProviderMapping(any())).thenReturn(List.of());

		assertEquals(0, pusher.pushInstance(event));

		// without the guard this would take the CloudEvent for a payload of its own EClass and
		// never ask for the payload's mappings at all
		verify(mappingRegistry).getProviderMapping(sensorEClass);
	}

	@Test
	@DisplayName("An envelope without data is nothing to push, not a failure")
	void pushInstance_withEmptyEnvelope_returnsZero() {
		when(mappingRegistry.getProviderMapping(any())).thenReturn(List.of());

		assertEquals(0, pusher.pushInstance(envelope(null)));
	}

	@Test
	@DisplayName("An enveloped push asks for both roots: the payload's EClass and CloudEvent")
	void pushInstance_withEnvelope_looksUpBothRoots() {
		// a mapping declares its root with providerClasses, so one message can feed a
		// payload-rooted and an envelope-rooted mapping at once
		CloudEvent event = envelope(EcoreUtil.create(sensorEClass));
		when(mappingRegistry.getProviderMapping(any())).thenReturn(List.of());

		assertEquals(0, pusher.pushInstance(event));

		verify(mappingRegistry).getProviderMapping(sensorEClass);
		verify(mappingRegistry).getProviderMapping(CloudEventsPackage.Literals.CLOUD_EVENT);
	}

	@Test
	@DisplayName("An envelope-rooted mapping is handed the envelope, not the payload")
	void pushInstance_withEnvelope_rootsTheEnvelopeMappingAtTheEnvelope() {
		ProviderMapping envelopeRooted = MappingFactory.eINSTANCE.createProviderMapping();
		envelopeRooted.setMid("envelope-rooted");
		when(mappingRegistry.getProviderMapping(sensorEClass)).thenReturn(List.of());
		when(mappingRegistry.getProviderMapping(CloudEventsPackage.Literals.CLOUD_EVENT))
				.thenReturn(List.of(envelopeRooted));
		whenGatewayExecutes(1);

		assertEquals(1, pusher.pushInstance(envelope(EcoreUtil.create(sensorEClass))));

		verify(gatewayThread).execute(any());
	}

	@Test
	@DisplayName("An envelope with no payload still offers its envelope-rooted mappings")
	void pushInstance_withoutData_stillLooksUpTheEnvelope() {
		// there is no payload EClass to look up, but an envelope-rooted mapping may still have
		// something to read - the source alone can be the whole mapping
		when(mappingRegistry.getProviderMapping(any())).thenReturn(List.of());

		assertEquals(0, pusher.pushInstance(envelope(null)));

		verify(mappingRegistry).getProviderMapping(CloudEventsPackage.Literals.CLOUD_EVENT);
	}

	@Test
	@DisplayName("A mapping with a selector does not apply to a push that carries no envelope")
	void pushInstance_bareEObject_skipsSelectorMappings() {
		// the rule, and the reason for it: a selector is a statement about the envelope, and a
		// programmatic push has none, so nothing can match. A mapping meant for both carries
		// no selector - which is every mapping shipped here
		when(mappingRegistry.getProviderMapping(any())).thenReturn(List.of(mappingWithSelector("5g/ilsa/#")));

		assertEquals(0, pusher.pushInstance(EcoreUtil.create(sensorEClass)));
	}

	@Test
	@DisplayName("The same mapping applies when the envelope's source matches its selector")
	void pushInstance_withMatchingEnvelope_selectsTheMapping() {
		when(mappingRegistry.getProviderMapping(any())).thenReturn(List.of(mappingWithSelector("5g/ilsa/#")));
		whenGatewayExecutes(1);
		CloudEvent event = envelope(EcoreUtil.create(sensorEClass));
		event.setSource("5g/ilsa/paradiesbruecke/signalstate");

		assertEquals(1, pusher.pushInstance(event));

		// reaching the gateway at all is what "selected" means here; whether the mapping then
		// applies is the ValueMapper's business and the OSGi tests'
		verify(gatewayThread).execute(any());
	}

	@Test
	@DisplayName("And is skipped when the source does not match, without reaching the gateway")
	void pushInstance_withNonMatchingEnvelope_skipsTheMapping() {
		when(mappingRegistry.getProviderMapping(any())).thenReturn(List.of(mappingWithSelector("5g/ilsa/#")));
		CloudEvent event = envelope(EcoreUtil.create(sensorEClass));
		event.setSource("5g/traficam/cam1");

		assertEquals(0, pusher.pushInstance(event));

		verify(gatewayThread, never()).execute(any());
	}

	@Test
	@DisplayName("A mapping whose selector cannot compile selects nothing rather than everything")
	void pushInstance_withUnusableSelector_selectsNothing() {
		// the registry refuses such a mapping by name, so this is the programmatic path - and
		// the safe reading of "I cannot tell whether this message is mine" is "it is not"
		ProviderMapping mapping = MappingFactory.eINSTANCE.createProviderMapping();
		mapping.setMid("broken");
		mapping.setSelector(MappingFactory.eINSTANCE.createMappingSelector());
		when(mappingRegistry.getProviderMapping(any())).thenReturn(List.of(mapping));
		CloudEvent event = envelope(EcoreUtil.create(sensorEClass));

		assertEquals(0, pusher.pushInstance(event));
	}

	@Test
	@DisplayName("A null envelope throws, like a null instance")
	void pushInstance_withNullEnvelope_throws() {
		assertThrows(NullPointerException.class, () -> pusher.pushInstance((CloudEvent) null));
		assertThrows(NullPointerException.class, () -> pusher.pushInstance((EObject) null));
	}

	private static ProviderMapping mappingWithSelector(String source) {
		ProviderMapping mapping = MappingFactory.eINSTANCE.createProviderMapping();
		mapping.setMid("selected");
		MappingSelector selector = MappingFactory.eINSTANCE.createMappingSelector();
		selector.getSource().add(source);
		mapping.setSelector(selector);
		return mapping;
	}

	private static CloudEvent envelope(EObject data) {
		CloudEvent event = CloudEventsFactory.eINSTANCE.createCloudEvent();
		event.setEventId("test-1");
		event.setSource("a/b/c");
		event.setType("test");
		event.setData(data);
		return event;
	}

	private static EClass createSensorEClass() {
		EPackage ePackage = EcoreFactory.eINSTANCE.createEPackage();
		ePackage.setName("test");
		ePackage.setNsPrefix("test");
		ePackage.setNsURI("http://example.org/mapping/envelope/1.0");
		EClass sensor = EcoreFactory.eINSTANCE.createEClass();
		sensor.setName("Sensor");
		var value = EcoreFactory.eINSTANCE.createEAttribute();
		value.setName("value");
		value.setEType(EcorePackage.Literals.EDOUBLE);
		sensor.getEStructuralFeatures().add(value);
		ePackage.getEClassifiers().add(sensor);
		return sensor;
	}

	private static void inject(Object target, String fieldName, Object value) {
		try {
			Field field = target.getClass().getDeclaredField(fieldName);
			field.setAccessible(true);
			field.set(target, value);
		} catch (ReflectiveOperationException e) {
			throw new IllegalStateException("Could not inject '" + fieldName + "'", e);
		}
	}

}
