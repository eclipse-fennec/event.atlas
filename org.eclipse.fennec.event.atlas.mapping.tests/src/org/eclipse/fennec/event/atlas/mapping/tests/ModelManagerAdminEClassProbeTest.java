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

import org.eclipse.emf.ecore.EAttribute;
import org.eclipse.emf.ecore.EClass;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.EPackage;
import org.eclipse.emf.ecore.EcoreFactory;
import org.eclipse.emf.ecore.EcorePackage;
import org.eclipse.emf.ecore.EReference;
import org.eclipse.emf.ecore.EStructuralFeature;
import org.eclipse.emf.ecore.util.EcoreUtil;
import org.eclipse.sensinact.core.command.GatewayThread;
import org.eclipse.sensinact.core.emf.command.AbstractSensinactEMFCommand;
import org.eclipse.sensinact.core.emf.model.EMFModel;
import org.eclipse.sensinact.core.emf.model.EMFService;
import org.eclipse.sensinact.core.emf.model.SensinactEMFModelManager;
import org.eclipse.sensinact.core.emf.twin.SensinactEMFDigitalTwin;
import org.eclipse.sensinact.core.emf.twin.SensinactEMFProvider;
import org.eclipse.sensinact.core.emf.twin.SensinactEMFService;
import org.eclipse.sensinact.core.model.ResourceType;
import org.eclipse.sensinact.core.snapshot.ResourceSnapshot;
import org.eclipse.sensinact.core.twin.SensinactResource;
import org.eclipse.sensinact.model.core.provider.Admin;
import org.eclipse.sensinact.model.core.provider.Provider;
import org.eclipse.sensinact.model.core.provider.ProviderPackage;
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
 * PROBE, not a test (#91 item 8, step 1, second route). Instead of adding a resource to the
 * admin service of a model - which {@link ModelManagerAdminResourceProbeTest} shows to be
 * refused and then destructive - this one hands the model manager an <b>EClass that extends
 * {@code Admin}</b>, through {@code EMFModel.createServiceWithEClass("admin", …)}, and asks
 * whether a provider of that model then carries the subclass.
 * <p>
 * The EClass is the generated {@code ProbeAdmin} of {@code model/probe-admin.ecore}, with one
 * extra attribute {@code viewport}. It has to be generated: a dynamic EClass that extends
 * {@code Admin} is instantiated as a {@code DynamicEObjectImpl}, which is no {@code Admin} to
 * the gateway.
 * <p>
 * The same ten steps run <b>twice</b>, because the model's package URI decides where the model
 * EClass is put:
 * <ul>
 * <li><b>pass A</b> gives a URI of its own, so {@code ModelNexus.createModel} makes a new
 * dynamic EPackage - the model EClass and {@code ProbeAdmin} are then strangers;</li>
 * <li><b>pass B</b> gives {@code ProbeadminPackage.eNS_URI}, so the model EClass is put into the
 * <em>generated</em> package that declares {@code ProbeAdmin} itself.</li>
 * </ul>
 * Step 0 reports which package each pass actually got.
 * <p>
 * Nothing here adds a feature to a generated EClass, so the probe is not destructive. Steps 9
 * and 10 verify that.
 */
@ExtendWith(BundleContextExtension.class)
@ExtendWith(ServiceExtension.class)
public class ModelManagerAdminEClassProbeTest {

	private static final String OWN_PKG_URI = "https://fennec.eclipse.org/event.atlas/probe/admineclass/1.0";
	private static final String EXTRA = "viewport";
	private static final String VALUE = "a polygon through createServiceWithEClass";

	@InjectService
	GatewayThread gatewayThread;

	@Test
	void probe() throws Exception {
		List<String> report = new ArrayList<>();
		report.addAll(run("A a package of its own      ", OWN_PKG_URI, "AdminByEClass", "AdminByEClassPlain",
				"admineclass1", "admineclassplain1"));
		report.addAll(run("B the generated probeadmin  ", ProbeadminPackage.eNS_URI, "AdminInProbePackage",
				"AdminInProbePackagePlain", "admininprobe1", "admininprobeplain1"));
		report.forEach(line -> System.out.println("ECLASSPROBE " + line));
	}

	/**
	 * Pass C, after the {@code .ecore} gained {@code ProbeProvider.admin : ProbeAdmin}: a model
	 * built from the <b>generated provider EClass itself</b>, which redeclares {@code admin} with
	 * the subclass as its type. The question is whether the provider's admin service is then a
	 * {@code ProbeAdmin}.
	 */
	@Test
	void generatedProviderWithItsOwnAdminReference() throws Exception {
		final String id = "probeprovideradmin1";
		List<String> report = gatewayThread.execute(new AbstractSensinactEMFCommand<List<String>>() {
			@Override
			protected Promise<List<String>> call(SensinactEMFDigitalTwin twin, SensinactEMFModelManager mmgr,
					PromiseFactory pf) {
				List<String> lines = new ArrayList<>();

				step(lines, "C1 how many features called admin does ProbeProvider have?", () -> {
					StringBuilder all = new StringBuilder();
					for (EStructuralFeature f : ProbeadminPackage.Literals.PROBE_PROVIDER
							.getEAllStructuralFeatures()) {
						if ("admin".equals(f.getName())) {
							all.append(" [owner=").append(f.getEContainingClass().getName()).append(" type=")
									.append(f.getEType().getName()).append(" id=")
									.append(f.getFeatureID()).append("]");
						}
					}
					EStructuralFeature byName = ProbeadminPackage.Literals.PROBE_PROVIDER
							.getEStructuralFeature("admin");
					return "features named admin:" + all + " | getEStructuralFeature(\"admin\") returns the one of "
							+ byName.getEContainingClass().getName() + " typed " + byName.getEType().getName();
				});

				step(lines, "C2 the model built from the generated EClass", () -> {
					EMFModel m = mmgr.getModel(ProbeadminPackage.Literals.PROBE_PROVIDER);
					if (m == null) {
						m = mmgr.createModel(ProbeadminPackage.Literals.PROBE_PROVIDER).build();
					}
					return "services=" + m.getServices().keySet() + " | admin eClass="
							+ m.getServices().get("admin").getServiceEClass().getName() + " | admin resources="
							+ m.getServices().get("admin").getResources().keySet();
				});

				step(lines, "C3 create a provider of it", () -> {
					SensinactEMFProvider provider = twin.createProvider(ProbeadminPackage.eNS_URI, "ProbeProvider",
							id);
					return "services=" + provider.getServices().keySet();
				});

				step(lines, "C4 which admin did the gateway write, and which does it read?", () -> {
					Provider emf = twin.getProvider(id).getEMFProvider();
					Object inherited = emf.eGet(ProviderPackage.Literals.PROVIDER__ADMIN);
					EStructuralFeature own = null;
					for (EStructuralFeature f : emf.eClass().getEAllStructuralFeatures()) {
						if ("admin".equals(f.getName())
								&& f != ProviderPackage.Literals.PROVIDER__ADMIN) {
							own = f;
						}
					}
					Object redeclared = own == null ? null : emf.eGet(own);
					return "Provider.admin=" + (inherited == null ? "null"
							: ((Admin) inherited).eClass().getName())
							+ " | a second admin feature=" + (own == null ? "none" : own.getEContainingClass()
									.getName() + " typed " + own.getEType().getName())
							+ " | its value=" + (redeclared == null ? "null"
									: ((Admin) redeclared).eClass().getName())
							+ " | getAdmin() gives " + (emf.getAdmin() == null ? "null"
									: emf.getAdmin().eClass().getName() + ", a ProbeAdmin="
											+ (emf.getAdmin() instanceof ProbeAdmin));
				});

				step(lines, "C5 the admin service of the twin", () -> {
					SensinactEMFProvider provider = twin.getProvider(id);
					SensinactEMFService admin = provider.getServices().get("admin");
					return admin == null ? "the provider lists no admin service"
							: "eClass=" + admin.getServiceEClass().getName() + " resources="
									+ admin.getResources().keySet();
				});

				step(lines, "C6 set " + EXTRA + " on the admin", () -> {
					SensinactResource resource = twin.getProvider(id).getResource("admin", EXTRA);
					if (resource == null) {
						resource = twin.getResource(id, "admin", EXTRA);
					}
					if (resource == null) {
						return "no lookup finds admin/" + EXTRA + " - nothing to set";
					}
					resource.setValue(VALUE, Instant.now()).getValue();
					return "set";
				});

				step(lines, "C7 read it back, through all four lookups", () -> {
					SensinactEMFProvider provider = twin.getProvider(id);
					String viaProvider = describe(provider.getResource("admin", EXTRA));
					String viaTwin = describe(twin.getResource(id, "admin", EXTRA));
					ResourceSnapshot snapshot = twin.snapshotResource(id, "admin", EXTRA);
					String viaSnapshot = snapshot == null ? "null"
							: "found value=" + (snapshot.getValue() == null ? "unset"
									: String.valueOf(snapshot.getValue().getValue()));
					Admin admin = provider.getEMFProvider().getAdmin();
					EStructuralFeature feature = admin == null ? null
							: admin.eClass().getEStructuralFeature(EXTRA);
					String viaEmf = admin == null ? "getAdmin() is null"
							: feature == null ? "the stored admin's EClass has no " + EXTRA
									: admin.eClass().getName() + "." + EXTRA + "=" + admin.eGet(feature);
					return "provider.getResource=" + viaProvider + " | twin.getResource=" + viaTwin
							+ " | snapshotResource=" + viaSnapshot + " | EMF admin: " + viaEmf;
				});

				return pf.resolved(lines);
			}

			private String describe(SensinactResource resource) {
				if (resource == null) {
					return "null";
				}
				try {
					return "found value=" + resource.getValue().getValue().getValue();
				} catch (Throwable e) {
					return "found, but reading it failed: " + e.getClass().getSimpleName();
				}
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

		report.forEach(line -> System.out.println("ECLASSPROBE C " + line));
	}

	/**
	 * Pass D: the same model-manager model as pass A, but the admin is retyped once with
	 * {@code SensinactEMFProvider.update()}. This is the route the item 8 design uses, and it is
	 * what makes {@code twin.getResource(id, "admin", viewport)} answer: that lookup resolves the
	 * service EClass from the stored <b>instance</b>, so it finds nothing until an instance of the
	 * subclass is actually stored.
	 */
	@Test
	void retypeTheAdminWithAnUpdate() throws Exception {
		final String pkgUri = "https://fennec.eclipse.org/event.atlas/probe/adminretype/1.0";
		final String model = "AdminRetype";
		final String id = "adminretype1";
		List<String> report = gatewayThread.execute(new AbstractSensinactEMFCommand<List<String>>() {
			@Override
			protected Promise<List<String>> call(SensinactEMFDigitalTwin twin, SensinactEMFModelManager mmgr,
					PromiseFactory pf) {
				List<String> lines = new ArrayList<>();

				step(lines, "D1 a model-manager model and a provider of it", () -> {
					EMFModel m = mmgr.getModel(pkgUri, model);
					if (m == null) {
						m = mmgr.createModel(pkgUri, model).build();
						m.createService("data").withResource("count").withResourceType(ResourceType.SENSOR)
								.withType(Integer.class).buildAll();
					}
					SensinactEMFProvider provider = twin.createProvider(pkgUri, model, id);
					return "the stored admin is a " + provider.getEMFProvider().getAdmin().eClass().getName()
							+ " | twin.getResource(admin, " + EXTRA + ")="
							+ (twin.getResource(id, "admin", EXTRA) == null ? "null" : "found");
				});

				step(lines, "D2 update() with a copy whose admin is a ProbeAdmin", () -> {
					SensinactEMFProvider provider = twin.getProvider(id);
					Provider copy = provider.getEMFProvider();
					ProbeAdmin admin = ProbeadminFactory.eINSTANCE.createProbeAdmin();
					admin.setFriendlyName(id);
					admin.setViewport(VALUE);
					copy.setAdmin(admin);
					provider.update(copy);
					return "the stored admin is now a "
							+ twin.getProvider(id).getEMFProvider().getAdmin().eClass().getName();
				});

				step(lines, "D3 read it back, through all four lookups", () -> {
					SensinactEMFProvider provider = twin.getProvider(id);
					String viaProvider = describe(provider.getResource("admin", EXTRA));
					String viaTwin = describe(twin.getResource(id, "admin", EXTRA));
					ResourceSnapshot snapshot = twin.snapshotResource(id, "admin", EXTRA);
					String viaSnapshot = snapshot == null ? "null"
							: "found value=" + (snapshot.getValue() == null ? "unset"
									: String.valueOf(snapshot.getValue().getValue()));
					Admin admin = provider.getEMFProvider().getAdmin();
					EStructuralFeature feature = admin.eClass().getEStructuralFeature(EXTRA);
					String viaEmf = feature == null ? "the stored admin's EClass has no " + EXTRA
							: admin.eClass().getName() + "." + EXTRA + "=" + admin.eGet(feature);
					return "provider.getResource=" + viaProvider + " | twin.getResource=" + viaTwin
							+ " | snapshotResource=" + viaSnapshot + " | EMF admin: " + viaEmf;
				});

				step(lines, "D4 write a new value through the ordinary resource API", () -> {
					SensinactResource resource = twin.getResource(id, "admin", EXTRA);
					if (resource == null) {
						return "twin.getResource is null - nothing to set";
					}
					resource.setValue("written through the resource API", Instant.now()).getValue();
					return "value now=" + twin.getResource(id, "admin", EXTRA).getValue().getValue().getValue();
				});

				return pf.resolved(lines);
			}

			private String describe(SensinactResource resource) {
				if (resource == null) {
					return "null";
				}
				try {
					return "found value=" + resource.getValue().getValue().getValue();
				} catch (Throwable e) {
					return "found, but reading it failed: " + e.getClass().getSimpleName();
				}
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

		report.forEach(line -> System.out.println("ECLASSPROBE D " + line));
	}

	/**
	 * Pass E: the same retype as pass D, but with a <b>dynamic</b> admin subclass - an EClass
	 * built at run time from {@code EcoreFactory}, not generated and not in any bundle. The
	 * gateway's own {@code NexusTest.AdminTests} and {@code SensinactTwinTest} do this with the
	 * dynamically loaded {@code extended.ecore}, and {@code DynamicAdminSubclassTest} measured
	 * that EMF answers such an EClass with a generated {@code AdminImpl} carrying the extra
	 * feature. If it holds through the twin, the mapping needs no {@code .ecore} for an admin
	 * subclass at all.
	 */
	@Test
	void retypeTheAdminWithADynamicSubclass() throws Exception {
		final String pkgUri = "https://fennec.eclipse.org/event.atlas/probe/admindynamic/1.0";
		final String model = "AdminDynamic";
		final String id = "admindynamic1";
		List<String> report = gatewayThread.execute(new AbstractSensinactEMFCommand<List<String>>() {
			@Override
			protected Promise<List<String>> call(SensinactEMFDigitalTwin twin, SensinactEMFModelManager mmgr,
					PromiseFactory pf) {
				List<String> lines = new ArrayList<>();

				EPackage ePackage = EcoreFactory.eINSTANCE.createEPackage();
				ePackage.setName("dynadmin");
				ePackage.setNsPrefix("dynadmin");
				ePackage.setNsURI("https://fennec.eclipse.org/event.atlas/probe/dynadmin/1.0");
				EClass dynAdmin = EcoreFactory.eINSTANCE.createEClass();
				dynAdmin.setName("DynAdmin");
				dynAdmin.getESuperTypes().add(ProviderPackage.Literals.ADMIN);
				EAttribute viewport = EcoreFactory.eINSTANCE.createEAttribute();
				viewport.setName(EXTRA);
				viewport.setEType(EcorePackage.Literals.ESTRING);
				dynAdmin.getEStructuralFeatures().add(viewport);
				ePackage.getEClassifiers().add(dynAdmin);

				step(lines, "E1 what does EMF build for the dynamic subclass?", () -> {
					EObject instance = EcoreUtil.create(dynAdmin);
					instance.eSet(viewport, "a dynamic polygon");
					return "impl=" + instance.getClass().getSimpleName() + " | an Admin="
							+ (instance instanceof Admin) + " | " + EXTRA + "=" + instance.eGet(viewport);
				});

				step(lines, "E2 a model-manager model and a provider of it", () -> {
					EMFModel m = mmgr.getModel(pkgUri, model);
					if (m == null) {
						m = mmgr.createModel(pkgUri, model).build();
						m.createService("data").withResource("count").withResourceType(ResourceType.SENSOR)
								.withType(Integer.class).buildAll();
					}
					SensinactEMFProvider provider = twin.createProvider(pkgUri, model, id);
					return "the stored admin is a " + provider.getEMFProvider().getAdmin().eClass().getName();
				});

				step(lines, "E3 update() with a copy whose admin is the dynamic subclass", () -> {
					SensinactEMFProvider provider = twin.getProvider(id);
					Provider copy = provider.getEMFProvider();
					Admin admin = (Admin) EcoreUtil.create(dynAdmin);
					admin.setFriendlyName(id);
					admin.eSet(viewport, VALUE);
					copy.setAdmin(admin);
					provider.update(copy);
					return "the stored admin is now a "
							+ twin.getProvider(id).getEMFProvider().getAdmin().eClass().getName();
				});

				step(lines, "E4 read it back, through all four lookups", () -> {
					SensinactEMFProvider provider = twin.getProvider(id);
					String viaProvider = describe(provider.getResource("admin", EXTRA));
					String viaTwin = describe(twin.getResource(id, "admin", EXTRA));
					ResourceSnapshot snapshot = twin.snapshotResource(id, "admin", EXTRA);
					String viaSnapshot = snapshot == null ? "null"
							: "found value=" + (snapshot.getValue() == null ? "unset"
									: String.valueOf(snapshot.getValue().getValue()));
					Admin admin = provider.getEMFProvider().getAdmin();
					EStructuralFeature feature = admin.eClass().getEStructuralFeature(EXTRA);
					String viaEmf = feature == null ? "the stored admin's EClass has no " + EXTRA
							: admin.eClass().getName() + "." + EXTRA + "=" + admin.eGet(feature);
					return "provider.getResource=" + viaProvider + " | twin.getResource=" + viaTwin
							+ " | snapshotResource=" + viaSnapshot + " | EMF admin: " + viaEmf;
				});

				step(lines, "E5 write a new value through the ordinary resource API", () -> {
					SensinactResource resource = twin.getResource(id, "admin", EXTRA);
					if (resource == null) {
						return "twin.getResource is null - nothing to set";
					}
					resource.setValue("written through the resource API", Instant.now()).getValue();
					return "value now=" + twin.getResource(id, "admin", EXTRA).getValue().getValue().getValue();
				});

				step(lines, "E6 is the rest of the runtime still healthy?", () -> {
					SensinactEMFProvider provider = twin.createProvider(pkgUri, model, id + "b");
					return "another provider of the same model: admin resources="
							+ provider.getServices().get("admin").getResources().keySet();
				});

				return pf.resolved(lines);
			}

			private String describe(SensinactResource resource) {
				if (resource == null) {
					return "null";
				}
				try {
					return "found value=" + resource.getValue().getValue().getValue();
				} catch (Throwable e) {
					return "found, but reading it failed: " + e.getClass().getSimpleName();
				}
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

		report.forEach(line -> System.out.println("ECLASSPROBE E " + line));
	}

	private List<String> run(String pass, String pkgUri, String model, String otherModel, String id, String otherId)
			throws Exception {
		List<String> lines = gatewayThread.execute(new AbstractSensinactEMFCommand<List<String>>() {
			@Override
			protected Promise<List<String>> call(SensinactEMFDigitalTwin twin, SensinactEMFModelManager mmgr,
					PromiseFactory pf) {
				List<String> report = new ArrayList<>();

				step(report, "1 create a model with one ordinary service", () -> {
					EMFModel m = mmgr.getModel(pkgUri, model);
					if (m == null) {
						m = mmgr.createModel(pkgUri, model).build();
						m.createService("data").withResource("count").withResourceType(ResourceType.SENSOR)
								.withType(Integer.class).buildAll();
					}
					return "services=" + m.getServices().keySet() + " frozen=" + m.isFrozen();
				});

				step(report, "0 which EPackage did the model land in?", () -> {
					EPackage ePackage = mmgr.getModel(pkgUri, model).getModelEClass().getEPackage();
					return "name=" + ePackage.getName() + " nsURI=" + ePackage.getNsURI()
							+ " | is the generated probeadmin package="
							+ (ePackage == ProbeadminPackage.eINSTANCE)
							+ " | its factory=" + ePackage.getEFactoryInstance().getClass().getSimpleName()
							+ " | it declares ProbeAdmin="
							+ (ePackage.getEClassifier("ProbeAdmin") != null);
				});

				step(report, "2 the admin the model has before we touch it", () -> {
					EMFService admin = mmgr.getModel(pkgUri, model).getServices().get("admin");
					return "eClass=" + admin.getServiceEClass().getName() + " | is the generated Admin="
							+ (admin.getServiceEClass() == ProviderPackage.Literals.ADMIN)
							+ " | resources=" + admin.getResources().keySet();
				});

				step(report, "3 createServiceWithEClass(\"admin\", ProbeAdmin)", () -> {
					EMFService admin = mmgr.getModel(pkgUri, model).createServiceWithEClass("admin",
							ProbeadminPackage.Literals.PROBE_ADMIN);
					return "the handle: name=" + admin.getName() + " eClass=" + admin.getServiceEClass().getName()
							+ " resources=" + admin.getResources().keySet();
				});

				step(report, "4 what the MODEL says afterwards", () -> {
					EMFModel m = mmgr.getModel(pkgUri, model);
					EStructuralFeature feature = m.getModelEClass().getEStructuralFeature("admin");
					String declared = feature instanceof EReference reference
							? reference.getEReferenceType().getName() + " (owner "
									+ reference.getEContainingClass().getName() + ")"
							: String.valueOf(feature);
					return "services=" + m.getServices().keySet() + " | admin eClass="
							+ m.getServices().get("admin").getServiceEClass().getName()
							+ " | the admin reference is typed " + declared;
				});

				step(report, "5 create a provider of that model", () -> {
					SensinactEMFProvider provider = twin.createProvider(pkgUri, model, id);
					Admin admin = provider.getEMFProvider().getAdmin();
					return "services=" + provider.getServices().keySet() + " | admin resources="
							+ provider.getServices().get("admin").getResources().keySet()
							+ " | the stored admin is a " + (admin == null ? "none" : admin.eClass().getName())
							+ ", a ProbeAdmin=" + (admin instanceof ProbeAdmin);
				});

				step(report, "6 set " + EXTRA + " on the admin", () -> {
					SensinactResource resource = twin.getProvider(id).getResource("admin", EXTRA);
					if (resource == null) {
						resource = twin.getResource(id, "admin", EXTRA);
					}
					if (resource == null) {
						return "no lookup finds admin/" + EXTRA + " - nothing to set";
					}
					resource.setValue(VALUE, Instant.now()).getValue();
					return "set";
				});

				step(report, "7 read it back, through all four lookups", () -> read(twin));

				step(report, "8 getOrCreateService(\"admin\", ProbeAdmin) on the live provider", () -> {
					SensinactEMFService created = twin.getProvider(id).getOrCreateService("admin",
							ProbeadminPackage.Literals.PROBE_ADMIN);
					Admin admin = twin.getProvider(id).getEMFProvider().getAdmin();
					return "returned eClass=" + (created == null ? "null" : created.getServiceEClass().getName())
							+ " resources=" + (created == null ? "-" : created.getResources().keySet())
							+ " | the stored admin is now a " + (admin == null ? "none" : admin.eClass().getName())
							+ " | then: " + read(twin);
				});

				step(report, "9 is an unrelated model still usable?", () -> {
					EMFModel other = mmgr.getModel(pkgUri, otherModel);
					if (other == null) {
						other = mmgr.createModel(pkgUri, otherModel).build();
						other.createService("data").withResource("count").withResourceType(ResourceType.SENSOR)
								.withType(Integer.class).buildAll();
					}
					SensinactEMFProvider provider = twin.createProvider(pkgUri, otherModel, otherId);
					return "admin resources of " + otherId + "="
							+ provider.getServices().get("admin").getResources().keySet();
				});

				step(report, "10 eIsSet and EcoreUtil.copy of the stored admin", () -> {
					Provider emf = twin.getProvider(id).getEMFProvider();
					Admin admin = emf.getAdmin();
					EStructuralFeature feature = admin.eClass().getEStructuralFeature(EXTRA);
					String isSet = feature == null ? "no " + EXTRA + " feature"
							: String.valueOf(admin.eIsSet(feature));
					String copied = EcoreUtil.copy(emf) == null ? "null" : "ok";
					return "eIsSet=" + isSet + " | copy=" + copied;
				});

				return pf.resolved(report);
			}

			private String read(SensinactEMFDigitalTwin twin) {
				SensinactEMFProvider provider = twin.getProvider(id);
				String viaProvider = describe(provider.getResource("admin", EXTRA));
				String viaTwin = describe(twin.getResource(id, "admin", EXTRA));
				ResourceSnapshot snapshot = twin.snapshotResource(id, "admin", EXTRA);
				String viaSnapshot = snapshot == null ? "null"
						: "found value=" + (snapshot.getValue() == null ? "unset"
								: String.valueOf(snapshot.getValue().getValue()));
				Admin admin = provider.getEMFProvider().getAdmin();
				EStructuralFeature feature = admin == null ? null : admin.eClass().getEStructuralFeature(EXTRA);
				String viaEmf = feature == null ? "the stored admin's EClass has no " + EXTRA
						: admin.eClass().getName() + "." + EXTRA + "=" + admin.eGet(feature);
				return "provider.getResource=" + viaProvider + " | twin.getResource=" + viaTwin
						+ " | snapshotResource=" + viaSnapshot + " | EMF admin: " + viaEmf;
			}

			private String describe(SensinactResource resource) {
				if (resource == null) {
					return "null";
				}
				try {
					return "found value=" + resource.getValue().getValue().getValue();
				} catch (Throwable e) {
					return "found, but reading it failed: " + e.getClass().getSimpleName();
				}
			}

			private void step(List<String> report, String what, ThrowingSupplier action) {
				try {
					report.add(pass + "OK   " + what + "  ->  " + action.get());
				} catch (Throwable e) {
					StringBuilder where = new StringBuilder();
					StackTraceElement[] trace = e.getStackTrace();
					for (int i = 0; i < Math.min(6, trace.length); i++) {
						where.append(" | ").append(trace[i]);
					}
					report.add(pass + "FAIL " + what + "  ->  " + e.getClass().getSimpleName() + ": "
							+ String.valueOf(e.getMessage()).replace('\n', ' ') + where);
				}
			}
		}).getValue();
		return lines;
	}

	@FunctionalInterface
	private interface ThrowingSupplier {
		String get() throws Exception;
	}
}
