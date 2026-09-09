/**
 * Copyright (c) 2012 - 2025 Data In Motion and others.
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

import static java.util.Objects.nonNull;
import static java.util.Objects.requireNonNull;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.stream.Collectors;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.eclipse.emf.common.util.URI;
import org.eclipse.emf.ecore.EClass;
import org.eclipse.emf.ecore.EClassifier;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.EPackage;
import org.eclipse.emf.ecore.EReference;
import org.eclipse.emf.ecore.InternalEObject;
import org.eclipse.emf.ecore.util.EcoreUtil;
import org.eclipse.fennec.emf.osgi.eobject.registry.EObjectRegistryConstants;
import org.eclipse.fennec.emf.osgi.eobject.registry.EObjectRegistryEntry;
import org.eclipse.fennec.emf.osgi.eobject.registry.EObjectRegistryListener;
import org.eclipse.fennec.event.atlas.mapping.ChangeRuleFilter;
import org.eclipse.fennec.event.atlas.mapping.MappingProfileRegistry;
import org.eclipse.fennec.event.atlas.mapping.ProviderMappingRegistry;
import org.eclipse.fennec.event.atlas.model.mapping.MappingProfile;
import org.eclipse.fennec.event.atlas.model.mapping.ProviderMapping;
import org.eclipse.sensinact.core.command.GatewayThread;
import org.eclipse.sensinact.core.emf.command.AbstractSensinactEMFCommand;
import org.eclipse.sensinact.core.emf.model.SensinactEMFModelManager;
import org.eclipse.sensinact.core.emf.twin.SensinactEMFDigitalTwin;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.ConfigurationPolicy;
import org.osgi.service.component.annotations.Deactivate;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.component.annotations.ReferenceCardinality;
import org.osgi.service.component.annotations.ReferencePolicy;
import org.osgi.service.component.annotations.ReferencePolicyOption;
import org.osgi.util.promise.Promise;
import org.osgi.util.promise.PromiseFactory;

/**
 * Collects {@link ProviderMapping}s from the named EObject registry
 * {@code sensinact-mappings} (override via the {@code emf.eobject.registry.name}
 * component property): published as an {@link EObjectRegistryListener} whiteboard
 * service, the registry binds it and replays the current content, so late binding is
 * indistinguishable from early binding. Entries are validated here - uniformly for
 * every content source (files, model atlas, ...) - and the outcome is one of three:
 * registered; dropped with a log, for content that can never become a mapping
 * (non-ProviderMapping content, a blank {@code mid}, no provider classes at all); or
 * <em>parked</em>, for a well-formed mapping whose model or profile has not arrived yet,
 * which is retried as EPackages are registered.
 *
 * @author Mark Hoffmann
 * @since 04.07.2025
 */
@Component(immediate = true, configurationPid = "sensinact.southbound.emf.mapping", configurationPolicy = ConfigurationPolicy.OPTIONAL, //
		property = EObjectRegistryConstants.EMF_EOBJECT_REGISTRY_NAME + "=sensinact-mappings")
public class ProviderMappingRegistryImpl implements ProviderMappingRegistry, EObjectRegistryListener {

	private static final Logger logger = Logger.getLogger(ProviderMappingRegistryImpl.class.getName());

	private final Map<EClass, List<ProviderMapping>> registry = new ConcurrentHashMap<>();
	/**
	 * Entries that are well-formed but not registrable yet, keyed by registry key. A mapping
	 * routinely arrives before the model it maps: a file provider loads and validates
	 * synchronously at activation, while a Model Atlas publishes its EPackages after an HTTP
	 * round trip. Dropping such an entry makes it unrecoverable, because a file provider never
	 * re-loads - so it is parked here and retried as EPackages arrive.
	 */
	private final Map<String, EObjectRegistryEntry> deferred = new LinkedHashMap<>();
	/** Every EPackage in the framework, by nsURI: what a parked entry's proxies resolve against. */
	private final Map<String, EPackage> boundPackages = new ConcurrentHashMap<>();
	/**
	 * Retries run here rather than on the DS bind thread, which must not block - and
	 * {@link #registerModelMapping(ProviderMapping)} waits on the gateway. Single-threaded, so
	 * two retries never register the same parked entry twice.
	 */
	private volatile ExecutorService retries;
	private ProviderModelSensinactMapper.Factory mapperFactory;
	@Reference
	private GatewayThread gatewayThread;
	@Reference
	private MappingProfileRegistry profileRegistry;
	/** Optional: only present when change rules are enforced on the way into the twin. */
	@Reference(cardinality = ReferenceCardinality.OPTIONAL, policy = ReferencePolicy.DYNAMIC, //
			policyOption = ReferencePolicyOption.GREEDY)
	private volatile ChangeRuleFilter changeRuleFilter;

	@Activate
	public void activate() {
		mapperFactory = new ProviderModelSensinactMapper.Factory(profileRegistry);
		retries = Executors.newSingleThreadExecutor(runnable -> {
			Thread thread = new Thread(runnable, "event-atlas-mapping-retry");
			thread.setDaemon(true);
			return thread;
		});
		// EPackages bound before activation found no executor to retry on.
		retryDeferred();
	}

	/**
	 * Component shutdown, which {@link #dispose()} deliberately is not: dispose is also the
	 * interface method a caller uses to reset a <em>running</em> registry, so tearing the retry
	 * executor down there would leave the component alive but unable to ever retry again.
	 */
	@Deactivate
	void deactivate() {
		ExecutorService executor = retries;
		retries = null;
		if (executor != null) {
			executor.shutdownNow();
		}
		dispose();
	}

	@Override
	public void dispose() {
		synchronized (deferred) {
			deferred.clear();
		}
		synchronized (registry) {
			registry.values().stream().flatMap(List::stream).collect(Collectors.toSet()).forEach(this::unregisterModelMapping);
			registry.clear();
		}
	}

	/**
	 * Bound for the side effect: every EPackage that appears is a chance for a parked mapping to
	 * become registrable. Unfiltered on purpose - a mapping may name any model in the framework,
	 * and the retry is a no-op while nothing is parked.
	 */
	@Reference(cardinality = ReferenceCardinality.MULTIPLE, policy = ReferencePolicy.DYNAMIC, //
			policyOption = ReferencePolicyOption.GREEDY)
	void bindEPackage(EPackage ePackage) {
		if (ePackage.getNsURI() == null) {
			return;
		}
		boundPackages.put(ePackage.getNsURI(), ePackage);
		retryDeferred();
	}

	void unbindEPackage(EPackage ePackage) {
		if (ePackage.getNsURI() != null) {
			boundPackages.remove(ePackage.getNsURI(), ePackage);
		}
	}

	/**
	 * Re-validates every parked entry. Entries that are still not registrable stay parked and,
	 * having already been reported once, stay silent - this runs on every EPackage in the
	 * framework, which at startup is dozens of times.
	 */
	private void retryDeferred() {
		ExecutorService executor = retries;
		if (executor == null) {
			return;
		}
		synchronized (deferred) {
			if (deferred.isEmpty()) {
				return;
			}
		}
		executor.execute(() -> {
			List<EObjectRegistryEntry> parked;
			synchronized (deferred) {
				parked = new ArrayList<>(deferred.values());
			}
			parked.forEach(entry -> validMapping(entry, false).ifPresent(this::registerModelMapping));
		});
	}

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.fennec.emf.osgi.eobject.registry.EObjectRegistryListener#entryAdded(org.eclipse.fennec.emf.osgi.eobject.registry.EObjectRegistryEntry)
	 */
	@Override
	public void entryAdded(EObjectRegistryEntry entry) {
		validMapping(entry, false).ifPresent(this::registerModelMapping);
	}

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.fennec.emf.osgi.eobject.registry.EObjectRegistryListener#entryUpdated(org.eclipse.fennec.emf.osgi.eobject.registry.EObjectRegistryEntry, org.eclipse.fennec.emf.osgi.eobject.registry.EObjectRegistryEntry)
	 */
	@Override
	public void entryUpdated(EObjectRegistryEntry entry, EObjectRegistryEntry oldEntry) {
		// the new mapping is registered before the old one is dropped, so lookups never
		// see a gap - the remove-after-add ordering of the former service whiteboard
		validMapping(entry, false).ifPresent(this::registerModelMapping);
		validMapping(oldEntry, true).ifPresent(this::unregisterModelMapping);
		// The rules themselves may have changed with the entry, and a baseline gathered under
		// the previous rule applies neither faithfully.
		validMapping(entry, true).ifPresent(m -> resetChangeRuleState(m.getMid()));
	}

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.fennec.emf.osgi.eobject.registry.EObjectRegistryListener#entryRemoved(org.eclipse.fennec.emf.osgi.eobject.registry.EObjectRegistryEntry)
	 */
	@Override
	public void entryRemoved(EObjectRegistryEntry entry) {
		// Also while parked: an entry withdrawn by its source must not be registered by a
		// later retry.
		undefer(entry);
		validMapping(entry, true).ifPresent(this::unregisterModelMapping);
	}

	/**
	 * The validation that guarded the former per-object service registration, now
	 * applied uniformly to every registry source. Quiet mode is used for old/removed
	 * entries: an entry that never passed validation was never registered, so its
	 * removal must neither log again nor reach the gateway.
	 */
	private Optional<ProviderMapping> validMapping(EObjectRegistryEntry entry, boolean quiet) {
		if (!(entry.object() instanceof ProviderMapping mapping)) {
			if (!quiet) {
				logger.warning(String.format("Registry entry '%s' is a %s - expected ProviderMapping, skipping", entry.key(), entry.object().eClass().getName()));
			}
			return Optional.empty();
		}
		if (mapping.getMid() == null || mapping.getMid().isBlank()) {
			if (!quiet) {
				logger.severe(String.format("ProviderMapping '%s' has no mid - skipping", entry.key()));
			}
			return Optional.empty();
		}
		if (mapping.getProviderClasses().isEmpty()) {
			if (!quiet) {
				logger.severe(String.format("ProviderMapping '%s' (%s) names no provider classes - skipping",
						entry.key(), mapping.getMid()));
			}
			return Optional.empty();
		}
		// From here every failure is recoverable: the mapping is well-formed and only the model
		// or the profile it points at is missing, both of which may still arrive. Reported once,
		// on the first park - a retry runs per EPackage bind and must not repeat itself.
		boolean report = !quiet && !isDeferred(entry);
		List<EClass> unresolved = resolveProviderClasses(mapping);
		if (!unresolved.isEmpty()) {
			if (!quiet) {
				defer(entry, report, String.format("its provider classes are not resolvable yet %s - is the sensor "
						+ "model deployed?", unresolved.stream().map(ProviderMappingRegistryImpl::proxyUriOf).toList()));
			}
			return Optional.empty();
		}
		if (!resolveProfile(mapping, !report)) {
			if (!quiet) {
				defer(entry, report, "its profile is neither reachable as a document nor registered yet");
			}
			return Optional.empty();
		}
		if (!quiet) {
			undefer(entry);
		}
		return Optional.of(mapping);
	}

	/**
	 * Resolves every nsURI proxy the mapping carries against the bound EPackages, and answers
	 * the provider classes that are still proxies afterwards.
	 * <p>
	 * EMF's own resolution is tried first, by reading each value - enough whenever the mapping's
	 * resource set can see the model. It cannot when the model was published as an EPackage
	 * service <em>after</em> that resource set was created, which is the case this exists for,
	 * and the consequence reaches further than validation: a mapping whose provider class was
	 * resolved by hand would still register and then fail on every payload, because
	 * {@code valueFeature}, {@code featurePath} and the admin references are proxies too and
	 * {@link ValueMapperImpl} reports them as "the feature 'null' is not a valid feature". So
	 * the whole mapping is swept, not just the reference validation happens to look at.
	 * <p>
	 * Only {@code providerClasses} gates registration, exactly as before: a reference that
	 * cannot be resolved here is left as the proxy it was, and {@code profile} keeps its own
	 * treatment in {@link #resolveProfile(ProviderMapping, boolean)}.
	 */
	private List<EClass> resolveProviderClasses(ProviderMapping mapping) {
		if (!boundPackages.isEmpty()) {
			resolveAgainstBoundPackages(mapping);
		}
		return mapping.getProviderClasses().stream().filter(EObject::eIsProxy).toList();
	}

	/** Replaces resolvable proxies in every non-containment reference of the whole mapping tree. */
	private void resolveAgainstBoundPackages(ProviderMapping mapping) {
		Iterator<EObject> objects = EcoreUtil.getAllContents(List.of(mapping), false);
		while (objects.hasNext()) {
			EObject object = objects.next();
			for (EReference reference : object.eClass().getEAllReferences()) {
				if (reference.isContainment() || reference.isDerived() || !object.eIsSet(reference)) {
					continue;
				}
				if (reference.isMany()) {
					@SuppressWarnings("unchecked")
					List<EObject> values = (List<EObject>) object.eGet(reference, false);
					for (int i = 0; i < values.size(); i++) {
						EObject resolved = resolved(values.get(i));
						if (resolved != null) {
							values.set(i, resolved);
						}
					}
				} else {
					EObject resolved = resolved((EObject) object.eGet(reference, false));
					if (resolved != null) {
						object.eSet(reference, resolved);
					}
				}
			}
		}
	}

	/** The object a proxy stands for, or <code>null</code> when it is not one or cannot be resolved. */
	private EObject resolved(EObject value) {
		if (value == null || !value.eIsProxy()) {
			return null;
		}
		return fromBoundPackages(((InternalEObject) value).eProxyURI());
	}

	/**
	 * The {@code <nsURI>#<fragment>} form an nsURI href leaves behind, resolved against the
	 * bound EPackages.
	 * <p>
	 * The fragment is walked here rather than handed to EMF, because
	 * {@code EcoreUtil.resolve(proxy, resourceSet)} would treat an unknown nsURI as a URL and
	 * try to fetch it. Three forms occur in a mapping and no others: {@code /} for the package
	 * itself (an admin's {@code providerPackage}), {@code //Name} for a classifier (a provider
	 * class), and {@code //Name/feature} for a structural feature (every feature path).
	 */
	private EObject fromBoundPackages(URI proxyUri) {
		if (proxyUri == null) {
			return null;
		}
		EPackage ePackage = boundPackages.get(proxyUri.trimFragment().toString());
		String fragment = proxyUri.fragment();
		if (ePackage == null || fragment == null) {
			return null;
		}
		if ("/".equals(fragment)) {
			return ePackage;
		}
		if (!fragment.startsWith("//")) {
			return null;
		}
		String[] path = fragment.substring(2).split("/");
		EClassifier classifier = ePackage.getEClassifier(path[0]);
		if (path.length == 1) {
			return classifier;
		}
		if (path.length != 2 || !(classifier instanceof EClass eClass)) {
			return null;
		}
		return eClass.getEStructuralFeature(path[1]);
	}

	private static String proxyUriOf(EClass proxy) {
		URI proxyUri = ((InternalEObject) proxy).eProxyURI();
		return proxyUri == null ? proxy.toString() : proxyUri.toString();
	}

	/** Parks an entry for a later retry, reporting it the first time only. */
	private void defer(EObjectRegistryEntry entry, boolean report, String reason) {
		synchronized (deferred) {
			deferred.put(entry.key(), entry);
		}
		if (report) {
			logger.warning(String.format("ProviderMapping '%s' cannot be registered yet: %s. Parked - it will be "
					+ "retried as further EPackages are registered.", entry.key(), reason));
		}
	}

	private void undefer(EObjectRegistryEntry entry) {
		synchronized (deferred) {
			deferred.remove(entry.key());
		}
	}

	private boolean isDeferred(EObjectRegistryEntry entry) {
		synchronized (deferred) {
			return deferred.containsKey(entry.key());
		}
	}

	/**
	 * Resolves a mapping's {@code profile} reference through the profile registry when the
	 * document it points at is not available.
	 * <p>
	 * {@code profile} is a non-containment reference to another document, so a mapping that
	 * arrives without its profile file - every mapping delivered from a Model Atlas, which
	 * hands over standalone root objects - carries an unresolved proxy. Since
	 * {@code MappingProfile.profileId} is an EMF ID, the proxy's URI fragment <em>is</em> the
	 * profile id, and the profile registry is already indexed by exactly that, fed by whatever
	 * providers the runtime configures. So the profile is looked up there and the reference is
	 * replaced with the registered instance - which is what EMF's own proxy resolution would
	 * have done, had the document been reachable.
	 * <p>
	 * A profile that cannot be found makes the mapping <em>invalid</em> rather than
	 * profile-less: the profile decides the provider identity
	 * ({@code providerStrategy == UNIFIED} maps every conforming mapping onto one shared
	 * provider), so carrying on without it would silently push data to a different provider
	 * than the operator asked for. Registration is skipped instead, the same way an unresolved
	 * provider class skips it - a later entry update or atlas refresh tries again.
	 * @return <code>true</code> if the mapping may be registered
	 */
	private boolean resolveProfile(ProviderMapping mapping, boolean quiet) {
		MappingProfile profile = mapping.getProfile();
		if (profile == null || !profile.eIsProxy()) {
			return true;
		}
		URI proxyUri = ((InternalEObject) profile).eProxyURI();
		String profileId = proxyUri == null ? null : proxyUri.fragment();
		Optional<MappingProfile> registered = profileId == null ? Optional.empty()
				: profileRegistry.getProfile(profileId);
		if (registered.isEmpty()) {
			if (!quiet) {
				logger.severe(String.format(
						"ProviderMapping '%s' references profile '%s' (%s), which is neither reachable as a "
								+ "document nor registered in the profile registry - is the profile deployed? Skipping",
						mapping.getMid(), profileId, proxyUri));
			}
			return false;
		}
		mapping.setProfile(registered.get());
		logger.fine(String.format("Resolved profile '%s' of ProviderMapping '%s' through the profile registry",
				profileId, mapping.getMid()));
		return true;
	}

	/*
	 * (non-Javadoc)
	 * @see org.gecko.emf.sensinact.model.ProviderMappingRegistry#registerModelMapping(org.eclipse.sensinact.mapping.ProviderMapping)
	 */
	@Override
	public void registerModelMapping(ProviderMapping mapping) {
		mapping.getProviderClasses().forEach(ec->{
			logger.info(String.format("Registering provider mapping for '%s' into registry", mapping.getMid()));
			registry.computeIfAbsent(ec, e->new ArrayList<>()).add(mapping);
		});
		Promise<Boolean> execute = gatewayThread.execute(new AbstractSensinactEMFCommand<Boolean>() {

			@Override
			protected Promise<Boolean> call(SensinactEMFDigitalTwin twin, SensinactEMFModelManager mmgr,
					PromiseFactory pf) {
				try {
					logger.fine(String.format("Mapping provider '%s' into sensinact", mapping.getMid()));
					mapperFactory.createMapper(twin, mmgr).registerModelMapping(mapping);
					return pf.resolved(Boolean.TRUE);
				} catch (Throwable e) {
					// Log the throwable, not getMessage(): an exception without a message - an
					// NPE, say - would otherwise be reported as "with error null".
					logger.log(Level.WARNING,
							String.format("Failed registering provider '%s' into sensinact", mapping.getMid()), e);
					return pf.failed(e);
				}
			}
		});
		try {
			if (nonNull(execute.getFailure())) {
				throw new IllegalStateException(String.format("Error registering provider '%s'", mapping.getMid()), execute.getFailure());
			}
		} catch (InterruptedException e) {
			throw new IllegalStateException(String.format("Error registering provider '%s' while waiting for promise to resolve", mapping.getMid()));
		}
	}

	/*
	 * (non-Javadoc)
	 * @see org.gecko.emf.sensinact.model.ProviderMappingRegistry#unregisterModelMapping(org.eclipse.sensinact.mapping.ProviderMapping)
	 */
	@Override
	public void unregisterModelMapping(ProviderMapping mapping) {
		resetChangeRuleState(mapping.getMid());
		mapping.getProviderClasses().forEach(ec->{
			logger.fine(String.format("Un-registering provider mapping for '%s' into registry", mapping.getMid()));
			registry.getOrDefault(ec, Collections.emptyList()).remove(mapping);
		});
		gatewayThread.execute(new AbstractSensinactEMFCommand<Boolean>() {

			@Override
			protected Promise<Boolean> call(SensinactEMFDigitalTwin twin, SensinactEMFModelManager mmgr,
					PromiseFactory pf) {
				try {
					logger.fine(String.format("Un-registering provider '%s' from sensinact", mapping.getMid()));
					mapperFactory.createMapper(twin, mmgr).unregisterModelMapping(mapping);
					return pf.resolved(Boolean.TRUE);
				} catch (Throwable e) {
					logger.log(Level.FINE,
							String.format("Failed un-registering provider '%s' from sensinact", mapping.getMid()), e);
					return pf.failed(e);
				}
			}
		});
	}

	/*
	 * (non-Javadoc)
	 * @see org.gecko.emf.sensinact.model.ProviderMappingRegistry#getPoviderMapping(org.eclipse.emf.ecore.EClass)
	 */
	@Override
	public List<ProviderMapping> getProviderMapping(EClass eclass) {
		requireNonNull(eclass);
		return registry.getOrDefault(eclass, Collections.emptyList());
	}

	/**
	 * Drops what the change rule filter retained for a mapping, so the next value of each of
	 * its resources counts as the first one again. A no-op when nothing enforces the rules.
	 */
	private void resetChangeRuleState(String mid) {
		ChangeRuleFilter filter = changeRuleFilter;
		if (filter != null && mid != null) {
			filter.reset(mid);
		}
	}

}
