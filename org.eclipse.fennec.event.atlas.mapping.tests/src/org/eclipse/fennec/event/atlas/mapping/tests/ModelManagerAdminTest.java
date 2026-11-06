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
package org.eclipse.fennec.event.atlas.mapping.tests;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

import org.eclipse.sensinact.core.command.GatewayThread;
import org.eclipse.sensinact.core.emf.command.AbstractSensinactEMFCommand;
import org.eclipse.sensinact.core.emf.model.EMFModel;
import org.eclipse.sensinact.core.emf.model.SensinactEMFModelManager;
import org.eclipse.sensinact.core.emf.twin.SensinactEMFDigitalTwin;
import org.eclipse.sensinact.core.emf.twin.SensinactEMFProvider;
import org.eclipse.sensinact.core.model.ResourceType;
import org.eclipse.sensinact.core.snapshot.ResourceSnapshot;
import org.eclipse.sensinact.core.twin.SensinactResource;
import org.eclipse.sensinact.model.core.provider.Provider;
import org.eclipse.sensinact.model.core.provider.Service;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.osgi.test.common.annotation.InjectService;
import org.osgi.test.junit5.context.BundleContextExtension;
import org.osgi.test.junit5.service.ServiceExtension;
import org.osgi.util.promise.Promise;
import org.osgi.util.promise.PromiseFactory;

import probeadmin.ProbeAdmin;
import probeadmin.ProbeadminFactory;
import probeadmin.ProbeadminPackage;

/**
 * Use the Sensinact modelmanager to create create a Model, with an Admin service with an additional resource, 
 * create a provider of that model and set the necessary data. Use the provider manager to retrieve the instance 
 * and see if the resource is set in the admin service.
 */
@ExtendWith(BundleContextExtension.class)
@ExtendWith(ServiceExtension.class)
public class ModelManagerAdminTest {

	private static final String PKG_URI = "https://fennec.eclipse.org/event.atlas/probe/adminextra/1.0";
	private static final String MODEL = "AdminExtra";
	private static final String ID = "adminextra1";
	private static final String EXTRA = "viewport";
	private static final String VALUE = "a polygon set through the resource API";

	@InjectService
	GatewayThread gatewayThread;

	@Test
	void probe() throws Exception {
		List<String> report = gatewayThread.execute(new AbstractSensinactEMFCommand<List<String>>() {
			@Override
			protected Promise<List<String>> call(SensinactEMFDigitalTwin twin, SensinactEMFModelManager mmgr,
					PromiseFactory pf) {
				List<String> lines = new ArrayList<>();

				step(lines, "1 create a model with an admin service with an additional resource", () -> {
					EMFModel model = mmgr.getModel(PKG_URI, MODEL);
					if (model == null) {
						model = mmgr.createModel(PKG_URI, MODEL).build();
						
//						model.createService("admin").withResource(EXTRA)
//								.withResourceType(ResourceType.SENSOR).withType(String.class).buildAll();
						model.createServiceWithEClass("admin", ProbeadminPackage.Literals.PROBE_ADMIN);
					}
					return "services=" + model.getServices().keySet() + " frozen=" + model.isFrozen()
							+ " dynamic=" + model.isDynamic();
				});

				step(lines, "2 create a provider of that model", () -> {
					SensinactEMFProvider provider = twin.createProvider(PKG_URI, MODEL, ID);
					
//					This does not work
//					SensinactResource resource = provider.getServices().get("admin").getResources().getOrDefault(EXTRA, null);
					
//					Only after an update it works because createServiceWithEClass does not really do anything for admin
					Provider copy = provider.getEMFProvider();          // a copy, by contract
					ProbeAdmin admin = ProbeadminFactory.eINSTANCE.createProbeAdmin();
					admin.setFriendlyName(ID);
					copy.setAdmin(admin);
					provider.update(copy);   					
					SensinactResource resource = twin.getResource(ID, "admin", EXTRA);
					if (resource == null) {
						return "getResource(admin, " + EXTRA + ") is null - nothing to set";
					}
					resource.setValue(VALUE, Instant.now()).getValue();
					return "set";
				});

				step(lines, "3 use the provider manager to retieve the instance and see if the resource is set", () -> {					
					ResourceSnapshot snapshot = twin.snapshotResource(ID, "admin", EXTRA);
					String viaSnapshot = snapshot == null ? "null"
							: "found value=" + (snapshot.getValue() == null ? "unset"
									: String.valueOf(snapshot.getValue().getValue()));
					
					SensinactEMFProvider provider = twin.getProvider(ID);
					SensinactResource resource = provider.getResource("admin", EXTRA);
					String viaProvider =  resource == null ? "null"
							: "found value=" + (resource.getValue() == null ? "unset"
									: String.valueOf(resource.getValue().getValue()));
					
					SensinactResource viaTwin = twin.getResource(ID, "admin", EXTRA);
					String value = viaTwin == null ? "null"
					          : String.valueOf(viaTwin.getValue().getValue().getValue());  
					return "Via Snapshot " + viaSnapshot +  " - Via Provider " + viaProvider + " - Via Twin " + value;
				});

				

				return pf.resolved(lines);
			}

			

			private void step(List<String> lines, String what, ThrowingSupplier action) {
				try {
					lines.add("OK   " + what + "  ->  " + action.get());
				} catch (Throwable e) {
					StringBuilder where = new StringBuilder();
					StackTraceElement[] trace = e.getStackTrace();
					for (int i = 0; i < Math.min(6, trace.length); i++) {
						where.append(" | ").append(trace[i]);
					}
					lines.add("FAIL " + what + "  ->  " + e.getClass().getSimpleName() + ": "
							+ String.valueOf(e.getMessage()).replace('\n', ' ') + where);
				}
			}
		}).getValue();

		report.forEach(line -> System.out.println("ADMINPROBE " + line));
	}

	@FunctionalInterface
	private interface ThrowingSupplier {
		String get() throws Exception;
	}
}
