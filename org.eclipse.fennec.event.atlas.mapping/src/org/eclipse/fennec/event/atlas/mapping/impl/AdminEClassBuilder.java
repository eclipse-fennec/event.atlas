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

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

import org.eclipse.emf.ecore.EAttribute;
import org.eclipse.emf.ecore.EClass;
import org.eclipse.emf.ecore.EClassifier;
import org.eclipse.emf.ecore.EPackage;
import org.eclipse.emf.ecore.EcoreFactory;
import org.eclipse.emf.ecore.EcorePackage;
import org.eclipse.fennec.event.atlas.model.mapping.AdminMapping;
import org.eclipse.fennec.event.atlas.model.mapping.ResourceMapping;
import org.eclipse.sensinact.model.core.provider.ProviderPackage;

/**
 * Builds the {@code Admin} subclass that carries a mapping's extra admin resources.
 * <p>
 * sensiNact's admin service has a fixed set of resources, because the {@code admin} reference is
 * declared by {@code Provider} and typed {@code Admin}. Nothing in the model layer can widen
 * it - adding a resource to the generated {@code Admin} EClass stops the runtime from creating
 * any provider at all. What does work is an <em>instance</em>: an admin of a subclass, installed
 * with one {@code SensinactEMFProvider.update()}, which the gateway keeps
 * ({@code EMFCompareUtil.copyOldService} retypes the stored admin upward).
 * <p>
 * The subclass needs no {@code .ecore} and no bundle. EMF answers a dynamic EClass that extends
 * a generated one with the <em>generated</em> implementation, which holds the extra features in
 * its settings, so the instance is a real {@code Admin} to the gateway. The same shape is what
 * the gateway's own {@code NexusTest.AdminTests} and {@code SensinactTwinTest} use.
 * <p>
 * EClasses are cached by content, so every provider of one mapping shares one EClass and an
 * edited mapping gets a new one without any invalidation hook.
 */
class AdminEClassBuilder {

	private static final String NS_URI_PREFIX = "https://fennec.eclipse.org/event.atlas/mapping/admin/";

	private static final Map<String, EClass> CACHE = new ConcurrentHashMap<>();

	private AdminEClassBuilder() {
	}

	/**
	 * The resource mappings under {@code <admin>} that sensiNact's admin service does not
	 * already have, i.e. the ones that need a subclass to exist at all.
	 *
	 * @param adminMapping the mapping's admin section, may be <code>null</code>
	 * @return the extra resource mappings, in mapping order, never <code>null</code>
	 */
	static List<ResourceMapping> extraResources(AdminMapping adminMapping) {
		if (adminMapping == null || !adminMapping.isExtendAdmin()) {
			return List.of();
		}
		return adminMapping.getResources().stream().filter(resource -> resource.getMid() != null)
				.filter(resource -> ProviderPackage.Literals.ADMIN.getEStructuralFeature(resource.getMid()) == null)
				.toList();
	}

	/**
	 * The {@code Admin} subclass for these extra resources. One EAttribute per resource, named
	 * by its mid and typed by the resource mapping itself - a {@code ResourceMapping} is an
	 * {@code EAttribute}, so it carries its own {@code eType}; a mapping that names none gets
	 * {@code EString}, which is what the twin reports for an untyped resource anyway.
	 *
	 * @param mappingMid the mid of the owning provider mapping, for the EClass name
	 * @param extras the resource mappings from {@link #extraResources(AdminMapping)}, not empty
	 * @return the EClass, shared by every caller that asks for the same content
	 */
	static EClass adminEClass(String mappingMid, List<ResourceMapping> extras) {
		return CACHE.computeIfAbsent(cacheKey(mappingMid, extras), key -> build(mappingMid, extras));
	}

	private static String cacheKey(String mappingMid, List<ResourceMapping> extras) {
		return mappingMid + "|" + extras.stream()
				.map(resource -> resource.getMid() + ":" + typeOf(resource).getName() + ":" + resource.getUpperBound())
				.collect(Collectors.joining(","));
	}

	private static EClass build(String mappingMid, List<ResourceMapping> extras) {
		EPackage ePackage = EcoreFactory.eINSTANCE.createEPackage();
		ePackage.setName("admin");
		ePackage.setNsPrefix("admin");
		ePackage.setNsURI(NS_URI_PREFIX + mappingMid);

		EClass adminEClass = EcoreFactory.eINSTANCE.createEClass();
		adminEClass.setName(className(mappingMid));
		adminEClass.getESuperTypes().add(ProviderPackage.Literals.ADMIN);
		for (ResourceMapping resource : extras) {
			EAttribute attribute = EcoreFactory.eINSTANCE.createEAttribute();
			attribute.setName(resource.getMid());
			attribute.setEType(typeOf(resource));
			attribute.setUpperBound(resource.getUpperBound());
			adminEClass.getEStructuralFeatures().add(attribute);
		}
		ePackage.getEClassifiers().add(adminEClass);
		return adminEClass;
	}

	private static EClassifier typeOf(ResourceMapping resource) {
		return resource.getEType() == null ? EcorePackage.Literals.ESTRING : resource.getEType();
	}

	/** A readable EClass name, because it is what a log line and a notification carry. */
	private static String className(String mappingMid) {
		String sanitized = mappingMid == null ? "" : mappingMid.replaceAll("[^A-Za-z0-9]", "");
		if (sanitized.isEmpty()) {
			return "MappedAdmin";
		}
		return Character.toUpperCase(sanitized.charAt(0)) + sanitized.substring(1) + "Admin";
	}
}
