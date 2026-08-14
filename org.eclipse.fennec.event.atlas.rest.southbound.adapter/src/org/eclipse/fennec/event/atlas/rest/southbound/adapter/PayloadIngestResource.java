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

import java.util.logging.Logger;

import org.eclipse.fennec.event.atlas.southbound.common.IngestResult;
import org.eclipse.fennec.event.atlas.southbound.common.PayloadIngest;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.component.annotations.ServiceScope;
import org.osgi.service.jakartars.whiteboard.annotations.RequireJakartarsWhiteboard;
import org.osgi.service.jakartars.whiteboard.propertytypes.JakartarsName;
import org.osgi.service.jakartars.whiteboard.propertytypes.JakartarsResource;

import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.HeaderParam;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.HttpHeaders;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.Response.Status;

/**
 * REST southbound adapter: an inbound ingest endpoint that pushes posted payloads into the
 * sensinact digital twin through the shared {@link PayloadIngest}.
 *
 * <pre>
 * POST &lt;whiteboard base&gt;/ingest/{channel}
 * </pre>
 *
 * The {@code channel} segment is free-form and used only to identify the sender in log
 * messages - the model comes from the payload itself, not from the path. The request's
 * {@code Content-Type} selects the payload format, so an XMI payload should be posted as
 * {@code application/xml} and a JSON payload as {@code application/json}.
 * <p>
 * The response status carries the outcome: the endpoint deliberately distinguishes "stored"
 * from "understood but nobody is mapping it", so a device operator can tell a wiring problem
 * from a payload problem without reading the gateway log.
 * <table>
 * <caption>Status mapping</caption>
 * <tr><td>200 OK</td><td>pushed into the twin</td></tr>
 * <tr><td>202 Accepted</td><td>understood, but no provider mapping is registered for it</td></tr>
 * <tr><td>400 Bad Request</td><td>unreadable, or read but empty</td></tr>
 * <tr><td>422 Unprocessable Content</td><td>names a model that cannot be resolved</td></tr>
 * <tr><td>503 Service Unavailable</td><td>the twin could not be written - worth retrying</td></tr>
 * </table>
 * @author Ilenia Salvadori
 */
@RequireJakartarsWhiteboard
@JakartarsResource
@JakartarsName("event-atlas-ingest")
@Component(service = PayloadIngestResource.class, scope = ServiceScope.PROTOTYPE)
@Path("ingest")
public class PayloadIngestResource {

	private static final Logger logger = Logger.getLogger(PayloadIngestResource.class.getName());

	/** RFC 9110 422; {@code Status.UNPROCESSABLE_ENTITY} is not in the Jakarta RS 3.1 enum. */
	private static final int UNPROCESSABLE_CONTENT = 422;

	@Reference
	private PayloadIngest payloadIngest;

	/**
	 * Ingests one payload.
	 * @param channel free-form sender identification, used for logging
	 * @param contentType the request content type, used as the payload format hint
	 * @param payload the raw request body
	 * @return a response whose status reflects the {@link IngestResult.Outcome}
	 */
	@POST
	@Path("{channel}")
	@Consumes({ MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
	@Produces(MediaType.TEXT_PLAIN)
	public Response ingest(@PathParam("channel") String channel,
			@HeaderParam(HttpHeaders.CONTENT_TYPE) String contentType, byte[] payload) {

		if (payload == null || payload.length == 0) {
			logger.warning(String.format("Empty REST payload on channel '%s' - nothing to ingest", channel));
			return Response.status(Status.BAD_REQUEST).entity("Empty payload").build();
		}

		IngestResult result = payloadIngest.ingest(payload, contentType, "rest/" + channel);
		// PayloadIngest already logged every drop reason; here we only translate it.
		return Response.status(statusOf(result)).entity(describe(result)).build();
	}

	private static int statusOf(IngestResult result) {
		return switch (result.outcome()) {
			case APPLIED -> Status.OK.getStatusCode();
			case NO_MAPPING -> Status.ACCEPTED.getStatusCode();
			case EMPTY, PARSE_ERROR -> Status.BAD_REQUEST.getStatusCode();
			case MODEL_UNKNOWN -> UNPROCESSABLE_CONTENT;
			case PUSH_FAILED -> Status.SERVICE_UNAVAILABLE.getStatusCode();
		};
	}

	private static String describe(IngestResult result) {
		return switch (result.outcome()) {
			case APPLIED -> "Applied %d mapping(s) to %d object(s)".formatted(result.mappingsApplied(),
					result.roots());
			case NO_MAPPING -> "No provider mapping registered for %s".formatted(result.detail());
			case EMPTY -> "Payload contained no objects";
			case PARSE_ERROR -> "Payload could not be parsed: %s".formatted(result.detail());
			case MODEL_UNKNOWN -> "Model '%s' is not available".formatted(result.detail());
			case PUSH_FAILED -> "Could not write to the digital twin: %s".formatted(result.detail());
		};
	}

}
