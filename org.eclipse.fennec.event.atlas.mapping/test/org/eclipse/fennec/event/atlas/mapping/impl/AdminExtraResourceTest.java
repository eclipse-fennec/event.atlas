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
 *      Data In Motion - initial API and implementation
 */
package org.eclipse.fennec.event.atlas.mapping.impl;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.Instant;
import java.util.Map;

import org.eclipse.emf.ecore.EAttribute;
import org.eclipse.emf.ecore.EClass;
import org.eclipse.emf.ecore.EDataType;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.EPackage;
import org.eclipse.emf.ecore.EcoreFactory;
import org.eclipse.emf.ecore.EcorePackage;
import org.eclipse.emf.ecore.util.EcoreUtil;
import org.eclipse.fennec.event.atlas.mapping.ValueMappingException;
import org.eclipse.fennec.event.atlas.model.mapping.AdminMapping;
import org.eclipse.fennec.event.atlas.model.mapping.MappingFactory;
import org.eclipse.fennec.event.atlas.model.mapping.NameMapping;
import org.eclipse.fennec.event.atlas.model.mapping.ProviderMapping;
import org.eclipse.fennec.event.atlas.model.mapping.ResourceMapping;
import org.eclipse.sensinact.core.emf.twin.SensinactEMFProvider;
import org.eclipse.sensinact.core.twin.SensinactDigitalTwin;
import org.eclipse.sensinact.core.twin.SensinactResource;
import org.eclipse.sensinact.core.twin.SensinactService;
import org.eclipse.sensinact.model.core.provider.Admin;
import org.eclipse.sensinact.model.core.provider.Provider;
import org.eclipse.sensinact.model.core.provider.ProviderFactory;
import org.eclipse.sensinact.model.core.provider.ProviderPackage;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

/**
 * An admin resource that sensiNact's admin service does not have (#91 item 8).
 * <p>
 * The admin's resources are fixed, because {@code Provider} declares {@code admin} and types it
 * {@code Admin}. The engine answers that with an instance: when {@code extendAdmin} is set it
 * builds an {@code Admin} subclass carrying the extra resources and retypes the provider once.
 * The value then goes through the ordinary resource API, found by the twin's instance-based
 * lookup rather than by the declared service.
 * <p>
 * The case is TrafiCam's {@code viewport}.
 */
public class AdminExtraResourceTest {

	private static final String PROVIDER_ID = "lobeda";
	private static final String EXTRA = "viewport";

	private final EPackage ePackage = EcoreFactory.eINSTANCE.createEPackage();
	private final EClass config = EcoreFactory.eINSTANCE.createEClass();
	private final EAttribute area = attribute("area", EcorePackage.Literals.ESTRING);

	private final SensinactDigitalTwin twin = mock(SensinactDigitalTwin.class);
	private final SensinactEMFProvider provider = mock(SensinactEMFProvider.class);
	private final SensinactResource extraResource = mock(SensinactResource.class);

	/** What the twin holds: a provider with the plain admin the gateway builds. */
	private final Provider storedProvider = ProviderFactory.eINSTANCE.createProvider();

	@BeforeEach
	void modelAndTwin() {
		ePackage.setName("configs");
		ePackage.setNsPrefix("configs");
		ePackage.setNsURI("http://example.org/event.atlas/configs/1.0");
		config.setName("Config");
		config.getEStructuralFeatures().add(area);
		ePackage.getEClassifiers().add(config);

		storedProvider.setId(PROVIDER_ID);
		Admin admin = ProviderFactory.eINSTANCE.createAdmin();
		admin.setFriendlyName("Lobeda");
		storedProvider.setAdmin(admin);

		SensinactService adminService = mock(SensinactService.class);
		doReturn(Map.of("friendlyName", mock(SensinactResource.class), "location", mock(SensinactResource.class)))
				.when(adminService).getResources();
		doReturn(Map.of("admin", adminService)).when(provider).getServices();
		doReturn(storedProvider).when(provider).getEMFProvider();
		when(twin.getProvider(anyString(), anyString())).thenReturn(provider);
	}

	@Test
	@DisplayName("Without extendAdmin an extra admin resource is skipped, and nothing is retyped")
	void withoutExtendAdmin_nothingHappens() throws ValueMappingException {
		map(admin(false));

		verify(provider, never()).update(any());
		verify(extraResource, never()).setValue(any(), any(Instant.class));
	}

	@Test
	@DisplayName("With extendAdmin the provider's admin is replaced by a subclass carrying the resource")
	void withExtendAdmin_theAdminIsRetyped() throws ValueMappingException {
		map(admin(true));

		ArgumentCaptor<Provider> updated = ArgumentCaptor.forClass(Provider.class);
		verify(provider).update(updated.capture());

		Admin extended = updated.getValue().getAdmin();
		assertTrue(ProviderPackage.Literals.ADMIN.isSuperTypeOf(extended.eClass()),
				"the new admin must be an Admin subclass, but it is a " + extended.eClass().getName());
		EAttribute viewport = (EAttribute) extended.eClass().getEStructuralFeature(EXTRA);
		assertNotNull(viewport, "the subclass must declare " + EXTRA);
		assertEquals(EcorePackage.Literals.ESTRING, viewport.getEType(), "the type comes from the mapping");
		assertEquals("Lobeda", extended.getFriendlyName(), "the values of the old admin must survive the exchange");
	}

	@Test
	@DisplayName("An admin that already carries the resource is not retyped again, and takes the value")
	void adminAlreadyExtended_takesTheValue() throws ValueMappingException {
		// What the twin answers once the retype has happened: the instance-based lookup finds it.
		when(twin.getResource(eq(PROVIDER_ID), eq("admin"), eq(EXTRA))).thenReturn(extraResource);

		map(admin(true));

		verify(provider, never()).update(any());
		verify(extraResource).setValue(eq("a polygon"), any(Instant.class));
	}

	@Test
	@DisplayName("The EClass is shared by every provider of one mapping, and follows its content")
	void theEClassIsCachedByContent() {
		AdminMapping first = admin(true);
		AdminMapping second = admin(true);
		assertSame(AdminEClassBuilder.adminEClass("configs", AdminEClassBuilder.extraResources(first)),
				AdminEClassBuilder.adminEClass("configs", AdminEClassBuilder.extraResources(second)),
				"the same content must give the same EClass");

		AdminMapping typed = admin(true);
		((ResourceMapping) typed.getResources().get(0)).setEType(EcorePackage.Literals.EDOUBLE);
		assertEquals(EcorePackage.Literals.EDOUBLE,
				AdminEClassBuilder.adminEClass("configs", AdminEClassBuilder.extraResources(typed))
						.getEStructuralFeature(EXTRA).getEType(),
				"a changed type must give a new EClass");
	}

	private void map(AdminMapping admin) throws ValueMappingException {
		new ValueMapperImpl(twin, null, mapping(admin), null, null, null, null).mapInstance(instance());
	}

	private AdminMapping admin(boolean extend) {
		NameMapping adminName = MappingFactory.eINSTANCE.createNameMapping();
		adminName.setName("Admin");
		AdminMapping admin = MappingFactory.eINSTANCE.createAdminMapping();
		admin.setMid("admin");
		admin.setName(adminName);
		admin.setExtendAdmin(extend);
		admin.getResources().add(viewportMapping());
		return admin;
	}

	/** The extra resource: a string the payload carries in {@code area}. */
	private ResourceMapping viewportMapping() {
		ResourceMapping resource = MappingFactory.eINSTANCE.createResourceMapping();
		resource.setMid(EXTRA);
		resource.setEType(EcorePackage.Literals.ESTRING);
		resource.getValueFeature().add(area);
		resource.setTimestamp(MappingFactory.eINSTANCE.createTimestampMapping());
		return resource;
	}

	private ProviderMapping mapping(AdminMapping admin) {
		NameMapping providerName = MappingFactory.eINSTANCE.createNameMapping();
		providerName.setName(PROVIDER_ID);

		ProviderMapping mapping = MappingFactory.eINSTANCE.createProviderMapping();
		mapping.setMid("configs");
		mapping.setName(providerName);
		mapping.getProviderClasses().add(config);
		mapping.setAdmin(admin);
		return mapping;
	}

	private EObject instance() {
		EObject instance = EcoreUtil.create(config);
		instance.eSet(area, "a polygon");
		return instance;
	}

	private static EAttribute attribute(String attributeName, EDataType type) {
		EAttribute attribute = EcoreFactory.eINSTANCE.createEAttribute();
		attribute.setName(attributeName);
		attribute.setEType(type);
		return attribute;
	}
}
