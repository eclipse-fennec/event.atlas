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
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;

import org.eclipse.fennec.event.atlas.model.mapping.AdminMapping;
import org.eclipse.fennec.event.atlas.model.mapping.MappingFactory;
import org.eclipse.fennec.event.atlas.model.mapping.ProviderMapping;
import org.eclipse.fennec.event.atlas.model.mapping.ResourceMapping;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/**
 * The gate that refuses a mapping naming an admin resource the admin service does not have.
 * What counts as one is sensiNact's business, so the set is read off {@code Admin} rather than
 * written down here - these tests pin that it is read the way the twin reads it.
 */
public class AdminResourceGateTest {

	@Test
	@DisplayName("The admin resources are the attributes of Admin, metadata is not one of them")
	void adminResourcesAreTheAdminAttributes() {
		List<String> names = ProviderMappingRegistryImpl.adminResourceNames();

		assertTrue(names.containsAll(List.of("friendlyName", "description", "location", "model", "modelPackageUri")),
				() -> "the five admin attributes should all be resources: " + names);
		// metadata is inherited from Service as a containment reference and ModelNexus filters it
		// out of a service's resources, so the twin has no 'metadata' resource to feed
		assertEquals(List.of(), names.stream().filter("metadata"::equals).toList(),
				() -> "metadata is not an admin resource: " + names);
	}

	@Test
	@DisplayName("A mid the admin service does not have is reported, a real one is not")
	void unknownMidsAreReported() {
		assertEquals(List.of(), ProviderMappingRegistryImpl.unknownAdminResources(adminMapping("description")),
				"description is an admin resource");
		assertEquals(List.of("icon", "metadata"),
				ProviderMappingRegistryImpl.unknownAdminResources(adminMapping("icon", "description", "metadata")),
				"a mid sensiNact does not define, and one the twin does not expose, are both reported");
		assertEquals(List.of(), ProviderMappingRegistryImpl.unknownAdminResources(MappingFactory.eINSTANCE.createProviderMapping()),
				"a mapping without an admin section has nothing to report");
	}

	private static ProviderMapping adminMapping(String... mids) {
		AdminMapping admin = MappingFactory.eINSTANCE.createAdminMapping();
		admin.setMid("admin");
		for (String mid : mids) {
			ResourceMapping resource = MappingFactory.eINSTANCE.createResourceMapping();
			resource.setMid(mid);
			admin.getResources().add(resource);
		}
		ProviderMapping mapping = MappingFactory.eINSTANCE.createProviderMapping();
		mapping.setMid("test-mapping");
		mapping.setAdmin(admin);
		return mapping;
	}
}
