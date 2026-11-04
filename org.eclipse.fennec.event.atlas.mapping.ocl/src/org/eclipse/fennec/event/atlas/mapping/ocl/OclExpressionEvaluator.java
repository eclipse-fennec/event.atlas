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
package org.eclipse.fennec.event.atlas.mapping.ocl;

import java.util.Optional;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.eclipse.emf.ecore.EClass;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.EPackage;
import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.emf.ecore.resource.ResourceSet;
import org.eclipse.fennec.emf.osgi.metadata.MetadataService;
import org.eclipse.fennec.emf.osgi.model.metadata.PackageMetadata;
import org.eclipse.fennec.event.atlas.mapping.ExpressionEvaluator;
import org.eclipse.fennec.m2x.ocl.api.OclContext;
import org.eclipse.fennec.m2x.ocl.api.OclEngine;
import org.eclipse.fennec.m2x.ocl.api.OclInvalid;
import org.eclipse.fennec.m2x.ocl.api.OclParseException;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

/**
 * The {@link ExpressionEvaluator} the mapping engine asks for, backed by the Fennec OCL engine.
 * <p>
 * The payload is {@code self}, so an expression reads the source object directly:
 * {@code self.temperature * 1.8 + 32}, {@code self.lines->size()},
 * {@code if self.battery &lt; 10 then 'low' else 'ok' endif}. Nothing else is bound - in
 * particular the ingest source is not a variable, because a payload that needs to know where it
 * came from carries that in its own model.
 * <p>
 * <b>Nothing here throws at the mapping.</b> An expression that cannot be parsed, that fails, or
 * that produces the OCL {@code null} or {@code invalid} value yields no value, and the mapping's
 * own fallback answers. One resource with a bad expression must not cost a payload its other
 * resources.
 */
@Component(service = ExpressionEvaluator.class)
public class OclExpressionEvaluator implements ExpressionEvaluator {

	private static final Logger logger = Logger.getLogger(OclExpressionEvaluator.class.getName());

	private final OclEngine engine;

	/** Names the model version each cache entry belongs to, so a changed model is a new key. */
	private final MetadataService metadataService;

	/**
	 * Expressions already known not to parse against a context type.
	 * <p>
	 * The engine caches what it parses, but a failure throws and so is re-attempted on every
	 * payload - which is both wasted work and a log line per payload.
	 * <p>
	 * The key names the model version, not the nsURI: whether a text parses depends on the
	 * <em>content</em> of the package, so the fingerprint is the honest key. Two copies of one
	 * nsURI that differ therefore get their own verdict, two copies that are identical share
	 * one, and a model that is registered again under changed content is parsed again - no
	 * restart.
	 */
	private final Set<String> unparseable = ConcurrentHashMap.newKeySet();

	/** Keys already reported, so a failing expression logs once and not per payload. */
	private final Set<String> reported = ConcurrentHashMap.newKeySet();

	/**
	 * Both references are mandatory. The engine is what this component exists to wrap, and the
	 * metadata service is what makes the parse cache honest; both are services, so with
	 * {@code -resolve.effective: active} a runtime that lacks either fails to resolve rather
	 * than running with a silently weaker cache key.
	 */
	@Activate
	public OclExpressionEvaluator(@Reference OclEngine engine, @Reference MetadataService metadataService) {
		this.engine = engine;
		this.metadataService = metadataService;
	}

	@Override
	public Optional<Object> evaluate(String expression, EObject self) {
		if (expression == null || expression.isBlank()) {
			return Optional.empty();
		}
		if (self == null) {
			return Optional.empty();
		}
		String key = key(expression, self.eClass());
		if (unparseable.contains(key)) {
			return Optional.empty();
		}
		try {
			Object result = engine.evaluate(expression, context(self));
			if (result == null) {
				// OCL void. Not an error: the expression ran and found nothing to report.
				logger.fine(() -> String.format("Expression '%s' produced no value on a %s", expression,
						self.eClass().getName()));
				return Optional.empty();
			}
			if (OclInvalid.INSTANCE.equals(result)) {
				report(key, Level.FINE, String.format(
						"Expression '%s' evaluated to the OCL 'invalid' value on a %s - no value is produced",
						expression, self.eClass().getName()), null);
				return Optional.empty();
			}
			return Optional.of(result);
		} catch (OclParseException e) {
			unparseable.add(key);
			logger.warning(String.format("Expression '%s' does not parse against %s: %s%s - it produces no value"
					+ " until the mapping or the model is corrected", expression, self.eClass().getName(),
					e.getMessage(), diagnostics(e)));
			return Optional.empty();
		} catch (Exception e) {
			// An evaluation failure can depend on the payload, so unlike a parse failure the
			// expression stays in use - only the logging is throttled.
			report(key, Level.WARNING, String.format("Expression '%s' failed on a %s - it produces no value",
					expression, self.eClass().getName()), e);
			return Optional.empty();
		}
	}

	/**
	 * The evaluation context: the payload as {@code self}, plus its resource set where it has
	 * one, so that an expression naming another classifier of the payload's model resolves it
	 * against the same package instances the payload was deserialized with.
	 */
	private static OclContext context(EObject self) {
		Resource resource = self.eResource();
		ResourceSet resourceSet = resource == null ? null : resource.getResourceSet();
		return resourceSet == null ? OclContext.of(self) : OclContext.of(self, resourceSet);
	}

	/**
	 * Keys an expression to the model version it was parsed against.
	 * <p>
	 * The identity of the model version is its fingerprint - a canonical digest of the content
	 * of the package, which {@code MetadataService} keeps memoized for each EPackage instance,
	 * so this costs two map reads and not a walk of the package. The type is named rather than
	 * held, so this map leaves no EClass behind.
	 */
	private String key(String expression, EClass contextType) {
		return modelVersion(contextType.getEPackage()) + '#' + contextType.getName() + '\u0000' + expression;
	}

	/**
	 * The fingerprint of a model version, or the nsURI where none can be read.
	 * <p>
	 * The service is mandatory, but it still cannot describe every package: it may report
	 * nothing for one, or fail on it. The nsURI then keys one verdict for every content under
	 * that URI - weaker, not wrong, and it is what this cache did before the fingerprint.
	 */
	private String modelVersion(EPackage ePackage) {
		if (ePackage == null) {
			return "";
		}
		try {
			String fingerprint = metadataService.getPackageMetadata(ePackage)
					.map(PackageMetadata::getModelFingerprint).orElse(null);
			if (fingerprint != null && !fingerprint.isBlank()) {
				return fingerprint;
			}
		} catch (RuntimeException e) {
			if (reported.add("fingerprint:" + ePackage.getNsURI())) {
				logger.log(Level.FINE, e, () -> String.format(
						"No model fingerprint for %s - keying expressions by nsURI instead",
						ePackage.getNsURI()));
			}
		}
		return String.valueOf(ePackage.getNsURI());
	}

	private void report(String key, Level level, String message, Exception cause) {
		if (reported.add(key)) {
			logger.log(level, message, cause);
		} else {
			logger.log(Level.FINE, message, cause);
		}
	}

	private static String diagnostics(OclParseException e) {
		if (e.getErrors().isEmpty()) {
			return "";
		}
		StringBuilder sb = new StringBuilder();
		e.getErrors().forEach(d -> sb.append(" [").append(d.getMessage()).append(" at ").append(d.getLine())
				.append(':').append(d.getColumn()).append(']'));
		return sb.toString();
	}
}
