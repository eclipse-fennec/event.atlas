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
package org.eclipse.fennec.event.atlas.rest.southbound.adapter;

import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.ConfigurationPolicy;
import org.osgi.service.jakartars.whiteboard.annotations.RequireJakartarsWhiteboard;
import org.osgi.service.jakartars.whiteboard.propertytypes.JakartarsApplicationBase;
import org.osgi.service.jakartars.whiteboard.propertytypes.JakartarsName;

import jakarta.ws.rs.core.Application;

/**
 * The Jakarta-RS application the {@link PayloadIngestResource} is mounted in, so that the
 * ingest endpoint owns a base URI of its own: {@code <whiteboard base>/ingest}.
 * <p>
 * An explicit application is not decoration, it is what makes the endpoint reachable at all.
 * A resource without an application select filter joins the whiteboard's <em>default</em>
 * application, which sits at the whiteboard root - and in a runtime that also hosts the
 * SensiNact SensorThings gateway that root is already taken: the SensorThings application
 * declares no base of its own (its resources carry {@code @Path("/v1.1/...")} instead), so it
 * occupies the root, shadows the default application and answers every path it does not know
 * with its own 404. A root-mounted ingest resource is therefore never invoked.
 * <p>
 * The application is deliberately empty: it contributes no classes of its own and only
 * collects the whiteboard resources that select it by name. {@code configurationPid} is
 * declared so a deployment can retarget the base URI or bind a specific whiteboard
 * ({@code osgi.jakartars.whiteboard.target}) without a code change.
 * @author Ilenia Salvadori
 */
@RequireJakartarsWhiteboard
@JakartarsName(PayloadIngestApplication.NAME)
@JakartarsApplicationBase(PayloadIngestApplication.BASE)
@Component(service = Application.class, configurationPid = "event.atlas.southbound.rest",
		configurationPolicy = ConfigurationPolicy.OPTIONAL)
public class PayloadIngestApplication extends Application {

	/** Selected by {@link PayloadIngestResource} through {@code osgi.jakartars.application.select}. */
	public static final String NAME = "event-atlas-ingest";

	/** Base URI relative to the Jakarta-RS whiteboard; the resource paths hang below it. */
	public static final String BASE = "ingest";
}
