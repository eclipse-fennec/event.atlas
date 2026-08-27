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
import java.time.Instant;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
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
import org.eclipse.fennec.event.atlas.southbound.common.UnknownModelHandler;
import org.eclipse.fennec.event.atlas.southbound.common.UnknownPayload;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.metatype.annotations.ObjectClassDefinition;
import org.osgi.service.component.annotations.Deactivate;
import org.osgi.service.component.annotations.Modified;
import org.osgi.service.component.annotations.ConfigurationPolicy;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.component.annotations.ReferenceCardinality;
import org.osgi.service.component.annotations.ReferencePolicy;
import org.osgi.service.component.annotations.ReferencePolicyOption;

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
@Component(configurationPid = "event.atlas.southbound.ingest", configurationPolicy = ConfigurationPolicy.OPTIONAL)
public class PayloadIngestImpl implements PayloadIngest {

	/**
	 * Configuration of this component.
	 */
	@ObjectClassDefinition
	public @interface Config {
		/**
		 * The codec's type-mapping registry id (mapId). Payload formats that do not name
		 * their own model - JSON, unlike XMI - are typed by the codec's discriminator:
		 * the model annotates each EClass with
		 * {@code http://eclipse.org/fennec/codec/typeMapping/<mapId>} carrying
		 * {@code typeDiscriminator} and {@code typeDiscriminatorPath}. Empty leaves the
		 * codec to its own defaults, which means untyped JSON yields no objects.
		 */
		String codec_typeMapId() default "";

		/**
		 * Whether the codec reads JSON property names from the model's
		 * {@code ExtendedMetaData} annotations instead of the plain feature names. Device
		 * payloads usually carry the wire names ({@code object}, {@code voc_index}), which
		 * is exactly what those annotations record; the codec's own default is the feature
		 * name.
		 */
		boolean codec_useNamesFromExtendedMetadata() default true;
	}

	/** Codec load option selecting the type-mapping registry; see {@code CodecOptions}. */
	private static final String OPTION_TYPE_MAP_ID = "codec.typeMapId";

	/** Codec load option honouring ExtendedMetaData names; see {@code ConfigProperty}. */
	private static final String OPTION_USE_EMD_NAMES = "codec.useNamesFromExtendedMetadata";

	private static final Logger logger = Logger.getLogger(PayloadIngestImpl.class.getName());

	/**
	 * How many unhandled payloads may wait for the {@link UnknownModelHandler}. Bounded on
	 * purpose: a handler doing network I/O is slower than a sensor farm publishing, and
	 * discarding a sample is exactly what happens without a handler anyway - growing a queue
	 * until the runtime runs out of memory is not.
	 */
	private static final int HANDOFF_QUEUE_CAPACITY = 64;

	/** Warn on the first discarded payload and every {@code n}th after it, not on each. */
	private static final long HANDOFF_DISCARD_LOG_INTERVAL = 100;

	/** Empty unless a mapId is configured; passed to every JSON load. */
	private volatile Map<String, Object> loadOptions = Collections.emptyMap();

	/** Distinguishes the throw-away resource URIs; only needs to be unique per runtime. */
	private final AtomicLong sequence = new AtomicLong();

	/** Payloads discarded because the hand-off queue was full; for the throttled warning. */
	private final AtomicLong handoffDiscarded = new AtomicLong();

	/**
	 * Takes the payloads offered to the {@link UnknownModelHandler} off the ingest thread.
	 * <p>
	 * One daemon thread, created on the first unhandled payload and reclaimed after 30s idle,
	 * so a runtime without a handler - or one that never sees an unknown payload - pays
	 * nothing for this. A single thread also means a handler sees the payloads of a channel in
	 * arrival order, which a sample collector reasoning about optionality across a window
	 * needs. Created in the field initializer rather than in {@link #activate(Config)},
	 * because that method is also the {@link Modified} handler and must not replace it.
	 */
	private final ExecutorService handoff = new ThreadPoolExecutor(0, 1, 30L, TimeUnit.SECONDS,
			new ArrayBlockingQueue<>(HANDOFF_QUEUE_CAPACITY), PayloadIngestImpl::newHandoffThread,
			(task, executor) -> rejectedHandoff(executor));

	@Reference
	private ResourceSetFactory resourceSetFactory;
	@Reference
	private InstancePusher instancePusher;
	/**
	 * Optional so a runtime without one behaves exactly as it did before the hook existed;
	 * dynamic so deploying or reconfiguring a handler does not tear ingest down and interrupt
	 * the southbound adapters bound to it.
	 */
	@Reference(cardinality = ReferenceCardinality.OPTIONAL, policy = ReferencePolicy.DYNAMIC, //
			policyOption = ReferencePolicyOption.GREEDY)
	private volatile UnknownModelHandler unknownModelHandler;

	@Activate
	@Modified
	void activate(Config config) {
		Map<String, Object> options = new LinkedHashMap<>();
		String mapId = config.codec_typeMapId();
		if (mapId == null || mapId.isBlank()) {
			logger.fine("No codec type map configured - payloads must name their own model");
		} else {
			options.put(OPTION_TYPE_MAP_ID, mapId);
		}
		if (config.codec_useNamesFromExtendedMetadata()) {
			options.put(OPTION_USE_EMD_NAMES, Boolean.TRUE);
		}
		loadOptions = options.isEmpty() ? Collections.emptyMap() : Map.copyOf(options);
		logger.info(String.format("Payload typing: map '%s', ExtendedMetaData names %s",
				mapId == null || mapId.isBlank() ? "<none>" : mapId,
				config.codec_useNamesFromExtendedMetadata() ? "on" : "off"));
	}

	@Deactivate
	void deactivate() {
		// Drop whatever is still queued instead of waiting: the payloads are best-effort
		// samples, and a handler blocked on network I/O must not delay the shutdown.
		handoff.shutdownNow();
	}

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.fennec.event.atlas.southbound.common.PayloadIngest#ingest(byte[], java.lang.String, java.lang.String)
	 */
	@Override
	public IngestResult ingest(byte[] payload, String formatHint, String source) {
		requireNonNull(payload, "Payload must not be null");
		String origin = source == null || source.isBlank() ? "<unknown source>" : source;
		String format = resolveFormat(formatHint, payload);

		Resource resource;
		try {
			resource = deserialize(payload, format);
		} catch (PackageNotFoundException e) {
			// The model is neither deployed nor resolvable through the Model Atlas. Not an
			// error on our side - the payload simply cannot be understood yet.
			logDrop(origin, String.format(
					"Cannot deserialize payload from '%s': model '%s' is not available "
							+ "(neither deployed nor resolvable via the Model Atlas) - dropping payload",
					origin, e.uri()));
			return offerUnknown(IngestResult.modelUnknown(e.uri()), payload, format, origin);
		} catch (UnsupportedFormatException e) {
			// A deployment gap, not a payload problem: without this the payload would be
			// handed to the wildcard XMI factory and fail as an XML parse error, which reads
			// like malformed data instead of a missing bundle.
			logger.severe(String.format("Cannot deserialize %s payload from '%s' - dropping payload: %s", format,
					origin, e.getMessage()));
			return IngestResult.formatUnsupported(format);
		} catch (Exception e) {
			logDrop(origin, String.format("Cannot deserialize %s payload from '%s' - dropping payload: %s", format,
					origin, describe(e)));
			logger.log(Level.FINE, "Payload deserialization failure for " + origin, e);
			return offerUnknown(IngestResult.parseError(describe(e)), payload, format, origin);
		}

		List<EObject> roots = List.copyOf(resource.getContents());
		if (roots.isEmpty()) {
			// The JSON codec does not throw on a payload it cannot make objects from - it
			// records a diagnostic and returns an empty resource. Passing that on is the
			// difference between "contained no objects" and knowing why.
			String reason = firstError(resource);
			logDrop(origin, String.format(
					"Payload from '%s' was read as %s but contained no objects - dropping payload%s", origin, format,
					reason == null ? "" : ": " + reason));
			return offerUnknown(IngestResult.empty(reason), payload, format, origin);
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
	 * Reports a payload that could not be turned into objects: at warning level, unless the
	 * {@link UnknownModelHandler} says it is currently collecting that channel's payloads on
	 * purpose.
	 * <p>
	 * A drop normally means data is being lost and has to be visible. While a handler gathers a
	 * channel's payloads to infer a model from, though, every one of them is an expected drop,
	 * and one warning per payload would make commissioning a new sensor look like an outage.
	 * The handler logs its own line when it starts collecting and when it stops; in between,
	 * this drops to {@code FINE}.
	 */
	private void logDrop(String origin, String message) {
		if (isCollecting(origin)) {
			logger.fine(message);
		} else {
			logger.warning(message);
		}
	}

	/**
	 * @return whether the deployed handler is collecting <code>origin</code>'s payloads. A
	 * handler that misbehaves here must not cost the log message, let alone the ingest
	 */
	private boolean isCollecting(String origin) {
		UnknownModelHandler handler = unknownModelHandler;
		if (handler == null) {
			return false;
		}
		try {
			return handler.isCollecting(origin);
		} catch (Throwable e) {
			logger.log(Level.FINE, "The unknown-model handler failed to answer isCollecting", e);
			return false;
		}
	}

	/**
	 * Offers a payload this runtime has no model for to the {@link UnknownModelHandler}, if
	 * one is deployed, and returns <code>result</code> unchanged.
	 * <p>
	 * Called from the three outcomes that can be a missing model - {@code MODEL_UNKNOWN},
	 * {@code EMPTY} and {@code PARSE_ERROR} - and from those only. A missing codec bundle
	 * ({@code FORMAT_UNSUPPORTED}), a model without a mapping ({@code NO_MAPPING}) and an
	 * unavailable twin ({@code PUSH_FAILED}) are not model problems; the reasoning is in
	 * {@link UnknownModelHandler}.
	 * <p>
	 * The result is returned untouched on every path, including a rejected hand-off and a
	 * handler that throws: the southbound adapters map outcomes onto transport responses, and
	 * this is a side channel, not a second result.
	 */
	private IngestResult offerUnknown(IngestResult result, byte[] payload, String format, String origin) {
		UnknownModelHandler handler = unknownModelHandler;
		if (handler == null) {
			return result;
		}
		// The nsURI only exists for MODEL_UNKNOWN, where it is what the outcome reports.
		String nsUri = result.outcome() == IngestResult.Outcome.MODEL_UNKNOWN ? result.detail() : null;
		UnknownPayload unknown = new UnknownPayload(payload, format, origin, nsUri, result.outcome(), Instant.now());
		// The handler is captured here rather than read again in the task: a dynamic reference
		// may be unbound while the payload waits, and calling a handler that has just gone away
		// is contained by notifyHandler - dropping the sample silently because of a race is
		// worse. A full queue or a shutdown pool goes to rejectedHandoff and never throws here.
		handoff.execute(() -> notifyHandler(handler, unknown));
		return result;
	}

	/**
	 * Runs the handler on the hand-off thread. A handler is third-party code doing network
	 * I/O: anything it throws - including an {@link Error} - stays here, because the thread it
	 * would kill is the one every later payload needs.
	 */
	private static void notifyHandler(UnknownModelHandler handler, UnknownPayload unknown) {
		try {
			handler.onUnknownModel(unknown);
		} catch (Throwable e) {
			logger.log(Level.WARNING,
					String.format("The unknown-model handler failed on %s - the payload is dropped", unknown), e);
		}
	}

	/**
	 * The hand-off pool would not take this payload, so it is dropped. Called on the ingest
	 * thread, so it must stay cheap and must not throw - the whole point of a rejection
	 * handler here rather than the default {@code AbortPolicy} is that
	 * {@link ExecutorService#execute(Runnable)} never fails an ingest.
	 * <p>
	 * A shutdown pool is deactivation and unremarkable. A full queue means the handler cannot
	 * keep up, which is worth a warning - but on the first occurrence and every
	 * {@link #HANDOFF_DISCARD_LOG_INTERVAL}th after it, since a sensor farm would otherwise
	 * turn one warning per payload into the log flood this bounded queue exists to prevent.
	 */
	private void rejectedHandoff(ThreadPoolExecutor executor) {
		if (executor.isShutdown()) {
			logger.fine("Ingest is shutting down - the unknown-model handler is no longer offered payloads");
			return;
		}
		long discarded = handoffDiscarded.incrementAndGet();
		if (discarded == 1 || discarded % HANDOFF_DISCARD_LOG_INTERVAL == 0) {
			logger.warning(String.format(
					"The unknown-model handler is not keeping up - %s payload(s) discarded without being offered "
							+ "(queue capacity %s)",
					discarded, HANDOFF_QUEUE_CAPACITY));
		}
	}

	private static Thread newHandoffThread(Runnable task) {
		Thread thread = new Thread(task, "event.atlas-unknown-model-handoff");
		// A handler blocked on I/O must not keep the JVM alive
		thread.setDaemon(true);
		return thread;
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
	private Resource deserialize(byte[] payload, String format) throws Exception {
		ResourceSet resourceSet = resourceSetFactory.createResourceSet();
		requireResourceFactory(resourceSet, format);
		Resource resource = resourceSet
				.createResource(URI.createURI("eventatlas-ingest/" + sequence.incrementAndGet() + "." + format));
		try {
			resource.load(new ByteArrayInputStream(payload), loadOptions);
		} catch (Exception e) {
			PackageNotFoundException notFound = findPackageNotFound(e);
			throw notFound == null ? e : notFound;
		}
		return resource;
	}

	/**
	 * @return the first load diagnostic the codec recorded, or <code>null</code> if it
	 * recorded none
	 */
	private static String firstError(Resource resource) {
		return resource.getErrors().stream().map(Resource.Diagnostic::getMessage).findFirst().orElse(null);
	}

	/**
	 * A codec failure does not always carry a message - EMF wraps whatever the parser threw,
	 * and some of those (an {@code ArrayStoreException} from a value that does not fit its
	 * feature, say) have none at all. Naming the exception type then beats reporting "null".
	 */
	private static String describe(Throwable e) {
		return e.getMessage() == null ? e.getClass().getSimpleName() : e.getMessage();
	}

	/**
	 * Asserts that the resource set really has a resource factory for <code>format</code>.
	 * <p>
	 * EMF's factory registry answers an unknown file extension with the wildcard
	 * (<code>"*"</code>) entry, which in a Fennec runtime is the XMI factory. A JSON payload
	 * in a runtime without the EMF JSON codec would therefore be handed to a SAX parser and
	 * die with "Content is not allowed in prolog" - a message that blames the payload for a
	 * missing bundle. Checking the extension map first lets us name the real cause.
	 */
	private static void requireResourceFactory(ResourceSet resourceSet, String format)
			throws UnsupportedFormatException {
		if (!resourceSet.getResourceFactoryRegistry().getExtensionToFactoryMap().containsKey(format)) {
			throw new UnsupportedFormatException(format);
		}
	}

	/**
	 * No {@link Resource.Factory} is registered for a payload format the runtime was asked
	 * to read.
	 */
	private static class UnsupportedFormatException extends Exception {

		private static final long serialVersionUID = 1L;

		UnsupportedFormatException(String format) {
			super(String.format(
					"no EMF resource factory is registered for extension '%s' - the runtime is missing the "
							+ "codec bundle for that format (JSON needs org.eclipse.fennec.codec)",
					format));
		}
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
