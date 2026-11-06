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

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.Instant;
import java.lang.reflect.InvocationTargetException;
import java.util.UUID;
import java.util.concurrent.ExecutionException;

import org.eclipse.fennec.emf.osgi.annotation.require.RequireEMF;
import org.eclipse.sensinact.core.command.AbstractSensinactCommand;
import org.eclipse.sensinact.core.command.GatewayThread;
import org.eclipse.sensinact.core.model.Model;
import org.eclipse.sensinact.core.model.ResourceType;
import org.eclipse.sensinact.core.model.SensinactModelManager;
import org.eclipse.sensinact.core.model.Service;
import org.eclipse.sensinact.core.twin.SensinactDigitalTwin;
import org.eclipse.sensinact.core.twin.SensinactProvider;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.osgi.service.typedevent.annotations.RequireTypedEvent;
import org.osgi.test.common.annotation.InjectService;
import org.osgi.test.junit5.context.BundleContextExtension;
import org.osgi.test.junit5.service.ServiceExtension;
import org.osgi.util.promise.Promise;
import org.osgi.util.promise.PromiseFactory;

/**
 * A provider shows a service that its model receives after the provider exists, and a value can
 * be written to that service.
 * <p>
 * This is the assumption under two planned features: a service that a mapping names from the
 * payload, and one service for each element of a collection. Both need the twin model to grow
 * while payloads arrive. The engine builds the model at registration only today, and
 * {@code ProviderModelSensinactMapper.mapProvider} stops when the provider exists, so nothing
 * else proves that the gateway permits this. Verified on 2026-11-03.
 * <p>
 * If a gateway version breaks this, the two features cannot work in this form. Then a mapping
 * must declare every service before the first payload makes the provider.
 */
@RequireEMF
@ExtendWith(BundleContextExtension.class)
@ExtendWith(ServiceExtension.class)
@RequireTypedEvent
public class LateServiceOnModelTest {

	// A fresh name for each run: the test asserts that "second" is absent before it adds it,
	// and a model that another run left behind would make that assertion fail.
	private final String MODEL = "late-service-probe-" + UUID.randomUUID().toString().substring(0, 8);
	private final String PROVIDER = MODEL + "-provider";

	@InjectService
	GatewayThread gatewayThread;

	@Test
	@DisplayName("A provider shows a service its model receives after the provider exists")
	void lateService_isVisibleOnAnExistingProvider()
			throws InterruptedException, ExecutionException, InvocationTargetException {
		Promise<Boolean> result = gatewayThread.execute(new AbstractSensinactCommand<Boolean>() {
			@Override
			protected Promise<Boolean> call(SensinactDigitalTwin twin, SensinactModelManager modelManager,
					PromiseFactory pf) {
				try {
					// 1. a model with one service, as the engine builds it at registration
					Model model = modelManager.getModel(MODEL);
					if (model == null) {
						model = modelManager.createModel(MODEL).build();
					}
					addService(model, "first");

					// 2. the provider, as the first payload creates it
					SensinactProvider provider = twin.getProvider(MODEL, PROVIDER);
					if (provider == null) {
						provider = twin.createProvider(MODEL, PROVIDER, Instant.now());
					}
					assertTrue(provider.getServices().containsKey("first"),
							"the service of the model must be on the new provider");
					assertFalse(provider.getServices().containsKey("second"),
							"the model does not have it yet");

					// 3. a second service, as a later payload would name it
					addService(model, "second");

					// 4. the provider that exists already shows it
					SensinactProvider again = twin.getProvider(MODEL, PROVIDER);
					assertTrue(again.getServices().containsKey("second"),
							"a provider must show a service that its model receives later");

					// 5. and the engine can write a value to it
					again.getServices().get("second").getResources().get("value")
							.setValue("a late value", Instant.now());

					return pf.resolved(true);
				} catch (Exception e) {
					return pf.failed(e);
				}
			}

			private void addService(Model model, String name) {
				if (model.getServices().containsKey(name)) {
					return;
				}
				Service service = model.createService(name).build();
				service.createResource("value").withType(String.class)
						.withResourceType(ResourceType.SENSOR).build();
			}
		});

		assertTrue(result.getValue(), "the gateway no longer permits a late service");
	}

}
