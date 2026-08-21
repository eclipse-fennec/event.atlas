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
package org.eclipse.fennec.event.atlas.southbound.common.impl;

import static java.util.Objects.requireNonNull;

import java.io.ByteArrayInputStream;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.atomic.AtomicLong;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;

import org.eclipse.emf.common.util.URI;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.emf.ecore.resource.ResourceSet;
import org.eclipse.emf.ecore.xmi.PackageNotFoundException;
import org.eclipse.fennec.emf.osgi.ResourceSetFactory;
import org.eclipse.fennec.event.atlas.mapping.InstancePusher;
import org.eclipse.fennec.event.atlas.southbound.common.IngestResult;
import org.eclipse.fennec.event.atlas.southbound.common.PayloadIngest;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

/**
 * Default {@link PayloadIngest}: deserializes through the runtime's {@link ResourceSet} and
 * pushes via the {@link InstancePusher}.
 * <p>
 * Every outcome that an operator can act on is logged here at warning level. That is
 * deliberate: {@code InstancePusherImpl} logs the "no mapping registered" case at debug
 * only, because for it a payload without a mapping is a normal, expected result - for a
 * southbound adapter it means data is being dropped, and needs to be visible.
 * @author Ilenia Salvadori
 */
@Component
public class PayloadIngestImpl implements PayloadIngest {

	private static final Logger logger = Logger.getLogger(PayloadIngestImpl.class.getName());

	/** Distinguishes the throw-away resource URIs; only needs to be unique per runtime. */
	private final AtomicLong sequence = new AtomicLong();

	@Reference
	private ResourceSetFactory resourceSetFactory;
	@Reference
	private InstancePusher instancePusher;

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.fennec.event.atlas.southbound.common.PayloadIngest#ingest(byte[], java.lang.String, java.lang.String)
	 */
	@Override
	public IngestResult ingest(byte[] payload, String formatHint, String source) {
		requireNonNull(payload, "Payload must not be null");
		String origin = source == null || source.isBlank() ? "<unknown source>" : source;
		String format = resolveFormat(formatHint, payload);

		List<EObject> roots;
		try {
			roots = deserialize(payload, format, origin);
		} catch (PackageNotFoundException e) {
			// The model is neither deployed nor resolvable through the Model Atlas. Not an
			// error on our side - the payload simply cannot be understood yet.
			logger.warning(String.format(
					"Cannot deserialize payload from '%s': model '%s' is not available "
							+ "(neither deployed nor resolvable via the Model Atlas) - dropping payload",
					origin, e.uri()));
			return IngestResult.modelUnknown(e.uri());
		} catch (Exception e) {
			logger.warning(String.format("Cannot deserialize %s payload from '%s' - dropping payload: %s", format,
					origin, e.getMessage()));
			logger.log(Level.FINE, "Payload deserialization failure for " + origin, e);
			return IngestResult.parseError(e.getMessage());
		}

		if (roots.isEmpty()) {
			logger.warning(String.format("Payload from '%s' was read as %s but contained no objects - dropping payload",
					origin, format));
			return IngestResult.empty();
		}

		int applied = 0;
		try {
			for (EObject root : roots) {
				applied += instancePusher.pushInstance(root);
			}
		} catch (RuntimeException e) {
			// pushInstance fails as a whole when the gateway itself is unavailable. Contain
			// it: an adapter must not lose its broker connection or request thread over it.
			logger.log(Level.SEVERE, "Failed pushing payload from '" + origin + "' into sensinact", e);
			return IngestResult.pushFailed(roots.size(), e.getMessage());
		}

		if (applied == 0) {
			String eClasses = roots.stream().map(r -> r.eClass().getName()).distinct().collect(Collectors.joining(", "));
			logger.warning(String.format(
					"Payload from '%s' deserialized (%s object(s) of type %s) but no provider mapping is "
							+ "registered for it - is the mapping deployed or in the atlas?",
					origin, roots.size(), eClasses));
			return IngestResult.noMapping(roots.size(), eClasses);
		}

		logger.info(String.format("Pushed payload from '%s' - %s object(s), %s mapping(s) applied", origin,
				roots.size(), applied));
		return IngestResult.applied(roots.size(), applied);
	}

	/**
	 * Loads a payload into a fresh {@link ResourceSet} from the runtime's
	 * {@link ResourceSetFactory}. Creating the resource set per payload matters: the atlas
	 * client contributes a ResourceSetConfigurator, so only resource sets created while the
	 * client is active resolve unknown nsURIs remotely (local-first, then fetch-on-miss).
	 * <p>
	 * The resource URI never gets dereferenced - the bytes are handed to
	 * {@link Resource#load(java.io.InputStream, java.util.Map)} directly - it only carries
	 * the file extension that selects the resource factory (XMI, or the codec's for JSON).
	 */
	private List<EObject> deserialize(byte[] payload, String format, String origin) throws Exception {
		ResourceSet resourceSet = resourceSetFactory.createResourceSet();
		Resource resource = resourceSet
				.createResource(URI.createURI("eventatlas-ingest/" + sequence.incrementAndGet() + "." + format));
		try {
			resource.load(new ByteArrayInputStream(payload), Collections.emptyMap());
		} catch (Exception e) {
			PackageNotFoundException notFound = findPackageNotFound(e);
			throw notFound == null ? e : notFound;
		}
		return List.copyOf(resource.getContents());
	}

	/**
	 * EMF reports an unresolvable nsURI as a {@link PackageNotFoundException} buried in the
	 * cause chain of the load failure.
	 */
	private static PackageNotFoundException findPackageNotFound(Throwable e) {
		for (Throwable t = e; t != null; t = t.getCause() == t ? null : t.getCause()) {
			if (t instanceof PackageNotFoundException pnf) {
				return pnf;
			}
		}
		return null;
	}

	/**
	 * Maps a format hint - a bare EMF file extension or a media type - onto the file
	 * extension that selects the resource factory. Unrecognized hints fall back to XMI,
	 * which is the format in which a payload names its own model.
	 */
	private static String resolveFormat(String formatHint, byte[] payload) {
		if (formatHint == null || formatHint.isBlank()) {
			return FORMAT_XMI;
		}
		String hint = formatHint.trim().toLowerCase(Locale.ROOT);
		if (FORMAT_AUTO.equals(hint)) {
			return detectFormat(payload);
		}
		// tolerate a full media type with parameters, e.g. "application/json; charset=utf-8"
		int parameters = hint.indexOf(';');
		if (parameters >= 0) {
			hint = hint.substring(0, parameters).trim();
		}
		if (hint.startsWith(".")) {
			hint = hint.substring(1);
		}
		if (FORMAT_JSON.equals(hint) || hint.endsWith("/json") || hint.endsWith("+json")) {
			return FORMAT_JSON;
		}
		if (FORMAT_XMI.equals(hint) || hint.endsWith("/xml") || hint.endsWith("+xml") || "xml".equals(hint)) {
			return FORMAT_XMI;
		}
		logger.fine(String.format("Unrecognized payload format hint '%s' - falling back to %s", formatHint,
				FORMAT_XMI));
		return FORMAT_XMI;
	}

	/**
	 * Guesses the format from the payload's first non-whitespace byte: <code>&lt;</code> is XMI,
	 * <code>{</code> and <code>[</code> are JSON.
	 * <p>
	 * Only the leading byte is inspected on purpose. Anything deeper would mean parsing the
	 * payload twice, and the codec is about to parse it properly anyway - a wrong guess surfaces
	 * as a parse error naming the format that was tried, which is a better diagnostic than a
	 * silent reinterpretation. Whitespace is skipped because an XML declaration or a pretty-printed
	 * JSON document may be preceded by newlines, and a UTF-8 BOM is skipped because publishers
	 * emit one.
	 * <p>
	 * A payload that begins with neither falls back to XMI, matching the behaviour of an
	 * unrecognized hint.
	 */
	private static String detectFormat(byte[] payload) {
		int i = 0;
		// UTF-8 BOM
		if (payload.length >= 3 && (payload[0] & 0xFF) == 0xEF && (payload[1] & 0xFF) == 0xBB
				&& (payload[2] & 0xFF) == 0xBF) {
			i = 3;
		}
		while (i < payload.length) {
			byte b = payload[i];
			if (b == ' ' || b == '\t' || b == '\n' || b == '\r') {
				i++;
				continue;
			}
			if (b == '{' || b == '[') {
				return FORMAT_JSON;
			}
			if (b == '<') {
				return FORMAT_XMI;
			}
			break;
		}
		logger.fine("Could not detect the payload format from its first byte - falling back to " + FORMAT_XMI);
		return FORMAT_XMI;
	}

}
