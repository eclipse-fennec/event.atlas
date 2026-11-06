package org.eclipse.fennec.event.atlas.mapping.tests;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.sensinact.core.command.GatewayThread;
import org.eclipse.sensinact.core.emf.command.AbstractSensinactEMFCommand;
import org.eclipse.sensinact.core.emf.model.SensinactEMFModelManager;
import org.eclipse.sensinact.core.emf.twin.SensinactEMFDigitalTwin;
import org.eclipse.sensinact.core.emf.twin.SensinactEMFProvider;
import org.eclipse.sensinact.core.emf.twin.SensinactEMFService;
import org.eclipse.sensinact.model.core.provider.Admin;
import org.eclipse.sensinact.model.core.provider.Provider;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.osgi.test.common.annotation.InjectService;
import org.osgi.test.junit5.context.BundleContextExtension;
import org.osgi.test.junit5.service.ServiceExtension;
import org.osgi.util.promise.Promise;
import org.osgi.util.promise.PromiseFactory;

import probeadmin.ProbeAdmin;
import probeadmin.ProbeProvider;
import probeadmin.ProbeadminFactory;
import probeadmin.ProbeadminPackage;

/**
 * PROBE, not a test: can an extended admin's own attribute be read back through the EMF
 * channel of the twin API? Every step is isolated so a failure names itself.
 */
@ExtendWith(BundleContextExtension.class)
@ExtendWith(ServiceExtension.class)
public class EmfReadChannelProbeTest {

	private static final String ID = "emfprobe1";

	@InjectService
	GatewayThread gatewayThread;

	/** The adapters' own route, and the one the gateway's EMFUpdateServiceTest.admin() uses. */
	@InjectService
	org.eclipse.sensinact.core.push.DataUpdate dataUpdate;

	@Test
	void probe() throws Exception {
		List<String> report = gatewayThread.execute(new AbstractSensinactEMFCommand<List<String>>() {
			@Override
			protected Promise<List<String>> call(SensinactEMFDigitalTwin twin, SensinactEMFModelManager mmgr,
					PromiseFactory pf) {
				List<String> lines = new ArrayList<>();

				step(lines, "1 create the provider from the generated provider EClass", () -> {
					SensinactEMFProvider p = twin.createProvider(ProbeadminPackage.eNS_URI, "ProbeProvider", ID);
					return "services=" + p.getServices().keySet();
				});

				step(lines, "2 getEMFProvider before any admin subclass", () -> {
					Provider emf = twin.getProvider(ID).getEMFProvider();
					Admin admin = emf.getAdmin();
					return "admin eClass=" + (admin == null ? "none" : admin.eClass().getName());
				});

				step(lines, "3 update() with a ProbeAdmin carrying a viewport", () -> {
					ProbeProvider update = ProbeadminFactory.eINSTANCE.createProbeProvider();
					update.setId(ID);
					ProbeAdmin admin = ProbeadminFactory.eINSTANCE.createProbeAdmin();
					admin.setFriendlyName("probe");
					admin.setViewport("a polygon from the EMF channel");
					update.setAdmin(admin);
					twin.getProvider(ID).update(update);
					return "accepted";
				});

				step(lines, "4 getEMFProvider after the update", () -> {
					Provider emf = twin.getProvider(ID).getEMFProvider();
					Admin admin = emf.getAdmin();
					String name = admin == null ? "none" : admin.eClass().getName();
					String viewport = admin instanceof ProbeAdmin probe ? String.valueOf(probe.getViewport())
							: "the admin is no ProbeAdmin";
					return "admin eClass=" + name + " | viewport=" + viewport;
				});

				step(lines, "5 the resource-oriented view of the same admin", () ->
						"admin resources=" + twin.getProvider(ID).getServices().get("admin").getResources().keySet());

				step(lines, "6 getResource(admin, viewport)", () -> {
					Object resource = twin.getProvider(ID).getResource("admin", "viewport");
					return resource == null ? "null" : "found " + resource;
				});

				step(lines, "7 getOrCreateService with a typed EClass, on a frozen model", () -> {
					SensinactEMFService service = twin.getProvider(ID)
							.getOrCreateService("K1_1", ProbeadminPackage.Literals.PROBE_ADMIN);
					return "eClass=" + service.getServiceEClass().getName()
							+ " resources=" + service.getResources().keySet()
							+ " | provider services=" + twin.getProvider(ID).getServices().keySet();
				});

				step(lines, "8 the typed service through the resource view", () -> {
					Object resource = twin.getProvider(ID).getResource("K1_1", "viewport");
					return resource == null ? "null" : "found";
				});

				return pf.resolved(lines);
			}

			private void step(List<String> lines, String what, ThrowingSupplier action) {
				try {
					lines.add("OK   " + what + "  ->  " + action.get());
				} catch (Throwable e) {
					StringBuilder where = new StringBuilder();
					StackTraceElement[] trace = e.getStackTrace();
					for (int i = 0; i < Math.min(8, trace.length); i++) {
						where.append(" | ").append(trace[i]);
					}
					lines.add("FAIL " + what + "  ->  " + e.getClass().getSimpleName() + ": "
							+ String.valueOf(e.getMessage()).replace('\n', ' ') + where);
				}
			}
		}).getValue();

		// The sequence of the gateway's own EMFUpdateServiceTest.admin(): the FIRST touch of
		// the provider is a push of an instance that already carries the admin subclass.
		String pushed;
		ProbeProvider instance = ProbeadminFactory.eINSTANCE.createProbeProvider();
		instance.setId("emfprobe2");
		ProbeAdmin admin = ProbeadminFactory.eINSTANCE.createProbeAdmin();
		admin.setFriendlyName("pushed");
		admin.setViewport("a polygon from the push");
		instance.setAdmin(admin);
		try {
			dataUpdate.pushUpdate(instance).getValue();
			pushed = "accepted";
		} catch (Throwable e) {
			pushed = "FAILED " + e.getClass().getSimpleName() + ": " + e.getMessage();
		}
		System.out.println("EMFPROBE 9 push an instance carrying the subclass -> " + pushed);

		String afterPush = gatewayThread.execute(new AbstractSensinactEMFCommand<String>() {
			@Override
			protected Promise<String> call(SensinactEMFDigitalTwin twin, SensinactEMFModelManager mmgr,
					PromiseFactory pf) {
				try {
					SensinactEMFProvider provider = twin.getProvider("emfprobe2");
					if (provider == null) {
						return pf.resolved("no provider emfprobe2");
					}
					Object resource = provider.getResource("admin", "viewport");
					String value = "n/a";
					if (resource instanceof org.eclipse.sensinact.core.twin.SensinactResource r) {
						value = String.valueOf(r.getValue().getValue().getValue());
					}
					return pf.resolved("admin resources=" + provider.getServices().get("admin").getResources().keySet()
							+ " | getResource(admin,viewport)=" + (resource == null ? "null" : "found")
							+ " | value=" + value);
				} catch (Throwable e) {
					return pf.resolved("FAILED " + e.getClass().getSimpleName() + ": " + e.getMessage());
				}
			}
		}).getValue();
		System.out.println("EMFPROBE 10 read it back as a resource -> " + afterPush);

		// The lookup the gateway's own EMFUpdateServiceTest uses: ResourceCommand ->
		// SensinactDigitalTwin.getResource(provider, service, resource), which resolves the
		// service EClass with Provider.getServiceEClass() - the INSTANCE's EClass.
		String twinLookup = gatewayThread.execute(new AbstractSensinactEMFCommand<String>() {
			@Override
			protected Promise<String> call(SensinactEMFDigitalTwin twin, SensinactEMFModelManager mmgr,
					PromiseFactory pf) {
				StringBuilder out = new StringBuilder();
				for (String id : new String[] { "emfprobe2", "emfprobe1" }) {
					try {
						org.eclipse.sensinact.core.twin.SensinactResource r =
								twin.getResource(id, "admin", "viewport");
						out.append(id).append(": ").append(r == null ? "null"
								: "found value=" + r.getValue().getValue().getValue());
					} catch (Throwable e) {
						out.append(id).append(": FAILED ").append(e.getClass().getSimpleName());
					}
					try {
						Object snap = twin.snapshotResource(id, "admin", "viewport");
						out.append(" | snapshotResource=").append(snap == null ? "null" : "found").append("  ");
					} catch (Throwable e) {
						out.append(" | snapshotResource FAILED ").append(e.getClass().getSimpleName()).append("  ");
					}
				}
				return pf.resolved(out.toString());
			}
		}).getValue();
		System.out.println("EMFPROBE 11 twin.getResource -> " + twinLookup);

		// Can the ordinary resource API WRITE it, once the admin instance is of the subclass?
		String write = gatewayThread.execute(new AbstractSensinactEMFCommand<String>() {
			@Override
			protected Promise<String> call(SensinactEMFDigitalTwin twin, SensinactEMFModelManager mmgr,
					PromiseFactory pf) {
				try {
					org.eclipse.sensinact.core.twin.SensinactResource r =
							twin.getResource("emfprobe1", "admin", "viewport");
					if (r == null) {
						return pf.resolved("no resource");
					}
					r.setValue("written through the resource API", java.time.Instant.now()).getValue();
					Object back = twin.getResource("emfprobe1", "admin", "viewport")
							.getValue().getValue().getValue();
					return pf.resolved("value now=" + back);
				} catch (Throwable e) {
					return pf.resolved("FAILED " + e.getClass().getSimpleName() + ": " + e.getMessage());
				}
			}
		}).getValue();
		System.out.println("EMFPROBE 12 setValue through the resource API -> " + write);

		report.forEach(line -> System.out.println("EMFPROBE " + line));
	}

	@FunctionalInterface
	private interface ThrowingSupplier {
		String get() throws Exception;
	}
}
