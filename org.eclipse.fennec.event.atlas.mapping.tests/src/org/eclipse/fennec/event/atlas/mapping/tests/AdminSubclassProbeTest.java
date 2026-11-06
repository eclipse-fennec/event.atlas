package org.eclipse.fennec.event.atlas.mapping.tests;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

import org.eclipse.emf.ecore.EAttribute;
import org.eclipse.emf.ecore.EClass;
import org.eclipse.emf.ecore.EPackage;
import org.eclipse.emf.ecore.EcoreFactory;
import org.eclipse.emf.ecore.EcorePackage;
import org.eclipse.sensinact.core.command.GatewayThread;
import org.eclipse.sensinact.core.emf.command.AbstractSensinactEMFCommand;
import org.eclipse.sensinact.core.emf.model.EMFModel;
import org.eclipse.sensinact.core.emf.model.SensinactEMFModelManager;
import org.eclipse.sensinact.core.emf.twin.SensinactEMFDigitalTwin;
import org.eclipse.sensinact.core.emf.twin.SensinactEMFProvider;
import org.eclipse.sensinact.core.emf.twin.SensinactEMFService;
import org.eclipse.sensinact.model.core.provider.Admin;
import org.eclipse.sensinact.model.core.provider.Provider;
import org.eclipse.sensinact.core.model.ResourceType;
import org.eclipse.sensinact.core.twin.SensinactProvider;
import org.eclipse.sensinact.model.core.provider.ProviderPackage;
import probeadmin.ProbeAdmin;
import probeadmin.ProbeProvider;
import probeadmin.ProbeadminFactory;
import probeadmin.ProbeadminPackage;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.osgi.test.common.annotation.InjectService;
import org.osgi.test.junit5.context.BundleContextExtension;
import org.osgi.test.junit5.service.ServiceExtension;
import org.osgi.util.promise.Promise;
import org.osgi.util.promise.PromiseFactory;

/**
 * PROBE, not a test: what the gateway accepts for an Admin subclass (#91 item 8).
 * Every step is guarded so a failure is reported instead of killing the gateway thread.
 */
@ExtendWith(BundleContextExtension.class)
@ExtendWith(ServiceExtension.class)
public class AdminSubclassProbeTest {

	@InjectService
	GatewayThread gatewayThread;

	/** The path the adapters actually use. */
	@InjectService
	org.eclipse.sensinact.core.push.DataUpdate dataUpdate;

	@Test
	void probe() throws Exception {
		Promise<List<String>> result = gatewayThread.execute(new AbstractSensinactEMFCommand<List<String>>() {
			@Override
			protected Promise<List<String>> call(SensinactEMFDigitalTwin twin, SensinactEMFModelManager mmgr,
					PromiseFactory pf) {
				List<String> report = new ArrayList<>();

				EPackage ePackage = EcoreFactory.eINSTANCE.createEPackage();
				ePackage.setName("probe");
				ePackage.setNsPrefix("probe");
				ePackage.setNsURI("http://example.org/event.atlas/probe/1.0");
				EAttribute viewport = EcoreFactory.eINSTANCE.createEAttribute();
				viewport.setName("viewport");
				viewport.setEType(EcorePackage.Literals.ESTRING);
				EClass adminSubclass = EcoreFactory.eINSTANCE.createEClass();
				adminSubclass.setName("ProbeAdmin");
				adminSubclass.getESuperTypes().add(ProviderPackage.Literals.ADMIN);
				adminSubclass.getEStructuralFeatures().add(viewport);
				ePackage.getEClassifiers().add(adminSubclass);

				// 15 - does a model built from a generated provider EClass carry the services
				// its EClass DECLARES? This is the ILSA `Ilsa.thermal : Thermal` shape.
				step(report, "A model from a generated provider EClass", () -> {
					EMFModel model = mmgr.getModel(ProbeadminPackage.Literals.PROBE_PROVIDER);
					if (model == null) {
						model = mmgr.createModel(ProbeadminPackage.Literals.PROBE_PROVIDER).build();
					}
					return "services=" + model.getServices().keySet() + " frozen=" + model.isFrozen()
							+ " dynamic=" + model.isDynamic();
				});

				step(report, "B add a service the EClass does not declare", () -> {
					EMFModel model = mmgr.getModel(ProbeadminPackage.Literals.PROBE_PROVIDER);
					model.createService("extra").build().createResource("count")
							.withResourceType(ResourceType.SENSOR).withType(Integer.class).build();
					return "services now=" + model.getServices().keySet();
				});

				step(report, "C add a resource to a DECLARED service", () -> {
					EMFModel model = mmgr.getModel(ProbeadminPackage.Literals.PROBE_PROVIDER);
					model.getServices().get("thermal").createResource("extraResource")
							.withResourceType(ResourceType.SENSOR).withType(String.class).build();
					return "thermal resources=" + model.getServices().get("thermal").getResources().keySet();
				});

				step(report, "D a provider of that model", () -> {
					var provider = twin.createProvider(ProbeadminPackage.eNS_URI, "ProbeProvider", "p20");
					return "services=" + provider.getServices().keySet();
				});

				return pf.resolved(report);
			}

			private void step(List<String> report, String what, ThrowingSupplier action) {
				try {
					report.add("OK    " + what + "  ->  " + action.get());
				} catch (Throwable e) {
					report.add("FAIL  " + what + "  ->  " + e.getClass().getSimpleName() + ": "
							+ String.valueOf(e.getMessage()).replace('\n', ' '));
				}
			}
		});
		// 12 - the adapter's own route: DataUpdate.pushUpdate with the provider instance
		ProbeProvider pushed = ProbeadminFactory.eINSTANCE.createProbeProvider();
		pushed.setId("p12");
		ProbeAdmin pushedAdmin = ProbeadminFactory.eINSTANCE.createProbeAdmin();
		pushedAdmin.setViewport("a pushed polygon");
		pushedAdmin.setFriendlyName("pushed");
		pushed.setAdmin(pushedAdmin);
		String pushOutcome;
		try {
			dataUpdate.pushUpdate(pushed).getValue();
			pushOutcome = "pushed";
		} catch (Exception e) {
			pushOutcome = "FAILED " + e.getClass().getSimpleName() + ": " + e.getMessage();
		}
		System.out.println("PROBE 12 pushUpdate -> " + pushOutcome);

		String readBack = gatewayThread.execute(new AbstractSensinactEMFCommand<String>() {
			@Override
			protected Promise<String> call(SensinactEMFDigitalTwin twin, SensinactEMFModelManager mmgr,
					PromiseFactory pf) {
				try {
					var provider = twin.getProvider("p12");
					if (provider == null) {
						return pf.resolved("no provider p12");
					}
					var admin = provider.getServices().get("admin");
					return pf.resolved("admin resources=" + admin.getResources().keySet());
				} catch (Throwable e) {
					return pf.resolved("FAILED " + e.getClass().getSimpleName() + ": " + e.getMessage());
				}
			}
		}).getValue();
		System.out.println("PROBE 13 read back -> " + readBack);

		// (kept) 14 - the SERVICE route: a generated service subclass in the services map, which is
		// what the adapters do for ILSA's Signal and TrafiCam's ObservedObjects.
		ProbeProvider withService = ProbeadminFactory.eINSTANCE.createProbeProvider();
		withService.setId("p14");
		ProbeAdmin asService = ProbeadminFactory.eINSTANCE.createProbeAdmin();
		asService.setViewport("a service polygon");
		withService.getServices().put("observedArea", asService);
		String serviceOutcome;
		try {
			dataUpdate.pushUpdate(withService).getValue();
			serviceOutcome = "pushed";
		} catch (Exception e) {
			serviceOutcome = "FAILED " + e.getClass().getSimpleName() + ": " + e.getMessage();
		}
		String serviceRead = gatewayThread.execute(new AbstractSensinactEMFCommand<String>() {
			@Override
			protected Promise<String> call(SensinactEMFDigitalTwin twin, SensinactEMFModelManager mmgr,
					PromiseFactory pf) {
				try {
					var provider = twin.getProvider("p14");
					if (provider == null) {
						return pf.resolved("no provider p14");
					}
					var service = provider.getServices().get("observedArea");
					return pf.resolved(service == null ? "NO observedArea service"
							: "resources=" + service.getResources().keySet());
				} catch (Throwable e) {
					return pf.resolved("FAILED " + e.getClass().getSimpleName() + ": " + e.getMessage());
				}
			}
		}).getValue();
		System.out.println("PROBE 14 service route -> " + serviceOutcome + " | " + serviceRead);

		// 16 - the EMF read channel. The resource-oriented views enumerate the DECLARED
		// Admin EClass, but SensinactEMFProvider.getEMFProvider() hands back the stored
		// instance. Is the pushed ProbeAdmin, with its viewport, still in there?
		String emfRead = gatewayThread.execute(new AbstractSensinactEMFCommand<String>() {
			@Override
			protected Promise<String> call(SensinactEMFDigitalTwin twin, SensinactEMFModelManager mmgr,
					PromiseFactory pf) {
				try {
					SensinactEMFProvider provider = twin.getProvider("p12");
					if (provider == null) {
						return pf.resolved("no provider p12");
					}
					Provider emf = provider.getEMFProvider();
					Admin admin = emf.getAdmin();
					String adminClass = admin == null ? "no admin" : admin.eClass().getName();
					String viewport = admin instanceof ProbeAdmin probeAdmin
							? String.valueOf(probeAdmin.getViewport())
							: "admin is no ProbeAdmin";
					Object resource = provider.getResource("admin", "viewport");
					return pf.resolved("emf admin eClass=" + adminClass + " viewport=" + viewport
							+ " | twin getResource(admin,viewport)=" + (resource == null ? "null" : "found"));
				} catch (Throwable e) {
					return pf.resolved("FAILED " + e.getClass().getSimpleName() + ": " + e.getMessage());
				}
			}
		}).getValue();
		System.out.println("PROBE 16 EMF read channel -> " + emfRead);

		// 17 - getOrCreateService(name, EClass) on a live provider of a FROZEN model. If this
		// works it gives a typed service with a payload-given name through the twin API, which
		// is the combination item 8 is blocked on.
		String typedService = gatewayThread.execute(new AbstractSensinactEMFCommand<String>() {
			@Override
			protected Promise<String> call(SensinactEMFDigitalTwin twin, SensinactEMFModelManager mmgr,
					PromiseFactory pf) {
				try {
					SensinactEMFProvider provider = twin.getProvider("p12");
					if (provider == null) {
						return pf.resolved("no provider p12");
					}
					EMFModel model = mmgr.getModel(ProbeadminPackage.Literals.PROBE_PROVIDER);
					String frozen = model == null ? "no model" : String.valueOf(model.isFrozen());
					SensinactEMFService created = provider.getOrCreateService("K1_1",
							ProbeadminPackage.Literals.PROBE_ADMIN);
					return pf.resolved("model frozen=" + frozen + " | created=" + (created == null ? "null"
							: created.getServiceEClass().getName() + " resources=" + created.getResources().keySet())
							+ " | provider services=" + provider.getServices().keySet());
				} catch (Throwable e) {
					return pf.resolved("FAILED " + e.getClass().getSimpleName() + ": " + e.getMessage());
				}
			}
		}).getValue();
		System.out.println("PROBE 17 getOrCreateService -> " + typedService);

		List<String> report = result.getValue();
		report.forEach(line -> System.out.println("PROBE " + line));
	}

	@FunctionalInterface
	private interface ThrowingSupplier {
		String get() throws Exception;
	}
}
