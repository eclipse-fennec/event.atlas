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

import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.concurrent.atomic.AtomicReference;
import java.util.concurrent.Callable;

import org.eclipse.emf.ecore.EAttribute;
import org.eclipse.emf.ecore.EClass;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.EPackage;
import org.eclipse.emf.ecore.EcoreFactory;
import org.eclipse.emf.ecore.EcorePackage;
import org.eclipse.emf.ecore.util.EcoreUtil;
import org.eclipse.sensinact.model.core.provider.ProviderPackage;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/**
 * A dynamic EClass can extend sensiNact's generated {@code Admin} and carry an extra attribute.
 * <p>
 * This is the route TrafiCam uses for its {@code viewport}: {@code TraficamAdmin extends Admin}.
 * The question was whether a dynamic subclass, from an {@code .ecore} rather than generated
 * code, recurses the way a late resource on a live admin service does. It does not. Measured
 * 2026-11-03:
 *
 * <pre>impl=AdminImpl viewport=a polygon friendlyName=null</pre>
 *
 * EMF gives the <em>generated</em> {@code AdminImpl} for the dynamic subclass, and the dynamic
 * attribute is set and read with no recursion. So the StackOverflowError that
 * {@code createResource} on a live admin service produces does not come from EMF handling a
 * dynamic feature on {@code Admin}. It comes from the way the model machinery adds a resource to
 * that service.
 * <p>
 * <b>What this does not prove.</b> It is an EMF test with no gateway. It does not show that the
 * twin accepts such an admin, or that the extra attribute appears as a resource of the admin
 * service. That needs the gateway, and a wrong answer there stops it.
 * <p>
 * This test touches no gateway and no OSGi framework. It never adds a feature to the generated
 * {@code Admin} class, because that class is shared by every test in this JVM. The work runs in
 * a thread with a small stack, so a recursion ends quickly and stays inside this test.
 */
public class DynamicAdminSubclassTest {

	@Test
	@DisplayName("A dynamic subclass of the generated Admin carries an extra attribute")
	void dynamicAdminSubclass_carriesAnExtraAttribute() {
		String outcome = isolated(() -> {
			EPackage ePackage = EcoreFactory.eINSTANCE.createEPackage();
			ePackage.setName("traficamprovider");
			ePackage.setNsPrefix("traficamprovider");
			ePackage.setNsURI("http://example.org/probe/traficamprovider");

			EClass traficamAdmin = EcoreFactory.eINSTANCE.createEClass();
			traficamAdmin.setName("TraficamAdmin");
			traficamAdmin.getESuperTypes().add(ProviderPackage.Literals.ADMIN);

			EAttribute viewport = EcoreFactory.eINSTANCE.createEAttribute();
			viewport.setName("viewport");
			viewport.setEType(EcorePackage.Literals.ESTRING);
			traficamAdmin.getEStructuralFeatures().add(viewport);
			ePackage.getEClassifiers().add(traficamAdmin);

			EObject instance = EcoreUtil.create(traficamAdmin);
			instance.eSet(viewport, "a polygon");
			Object read = instance.eGet(viewport);
			Object inherited = instance.eGet(ProviderPackage.Literals.ADMIN.getEStructuralFeature("friendlyName"));

			return "impl=" + instance.getClass().getSimpleName()
					+ " viewport=" + read
					+ " friendlyName=" + inherited;
		});

		assertTrue(outcome.contains("viewport=a polygon"),
				() -> "the extra attribute must round-trip, but the probe said: " + outcome);
		assertTrue(outcome.startsWith("impl="),
				() -> "nothing may be thrown, but the probe said: " + outcome);
	}

	/**
	 * Runs the body in a thread with a 256 KB stack. A runaway recursion then ends in
	 * milliseconds, and the Throwable stays here instead of reaching the test runner.
	 */
	private static String isolated(Callable<String> body) {
		AtomicReference<String> outcome = new AtomicReference<>("did not finish");
		Thread thread = new Thread(null, () -> {
			try {
				outcome.set(body.call());
			} catch (Throwable t) {
				outcome.set(t.getClass().getSimpleName() + ": " + t.getMessage());
			}
		}, "admin-subclass-probe", 256 * 1024);
		thread.setDaemon(true);
		thread.start();
		try {
			thread.join(10_000);
		} catch (InterruptedException e) {
			Thread.currentThread().interrupt();
		}
		return outcome.get();
	}
}
