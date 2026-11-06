package org.eclipse.fennec.event.atlas.mapping.tests;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

import org.eclipse.emf.ecore.EClass;
import org.eclipse.emf.ecore.EStructuralFeature;
import org.eclipse.emf.ecore.util.EcoreUtil;
import org.eclipse.sensinact.core.command.GatewayThread;
import org.eclipse.sensinact.core.emf.command.AbstractSensinactEMFCommand;
import org.eclipse.sensinact.core.emf.model.EMFModel;
import org.eclipse.sensinact.core.emf.model.EMFService;
import org.eclipse.sensinact.core.emf.model.SensinactEMFModelManager;
import org.eclipse.sensinact.core.emf.twin.SensinactEMFDigitalTwin;
import org.eclipse.sensinact.core.emf.twin.SensinactEMFProvider;
import org.eclipse.sensinact.core.model.ResourceType;
import org.eclipse.sensinact.core.snapshot.ResourceSnapshot;
import org.eclipse.sensinact.core.twin.SensinactResource;
import org.eclipse.sensinact.model.core.provider.Admin;
import org.eclipse.sensinact.model.core.provider.Provider;
import org.eclipse.sensinact.model.core.provider.ProviderPackage;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.osgi.test.common.annotation.InjectService;
import org.osgi.test.junit5.context.BundleContextExtension;
import org.osgi.test.junit5.service.ServiceExtension;
import org.osgi.util.promise.Promise;
import org.osgi.util.promise.PromiseFactory;

/**
 * PROBE, not a test (#91 item 8, step 1). The route this one measures is the
 * <em>model manager</em>: build a model, give its admin service one extra resource, create a
 * provider of that model, set a value and try to read it back through the provider.
 * <p>
 * It is the counterpart of {@link EmfReadChannelProbeTest}, which measures the other route - a
 * generated {@code Admin} subclass pushed as an instance.
 * <p>
 * <b>The answer is no, and the attempt is destructive - which is why this probe is
 * {@link Disabled}.</b> Measured 2026-11-06 against the deployed gateway snapshot:
 *
 * <pre>
 * 1 create a model with one ordinary service  -> services=[data, admin] frozen=false dynamic=true
 * 2 whose EClass is that admin?               -> Admin, https://eclipse.org/sensinact/core/provider/1.0
 *                                                is the generated Admin=true
 * 3 createService("admin")                    -> FAIL IllegalArgumentException: There is an
 *                                                existing service with name admin in model
 * 4 createResource("viewport") on it          -> accepted, and the GENERATED Admin EClass carries it
 * 5 create a provider of that model           -> FAIL StackOverflowError
 * 8 a provider of an unrelated model          -> FAIL StackOverflowError
 * </pre>
 *
 * A model built by the model manager inherits its {@code admin} reference from {@code Provider},
 * so there is no per-model admin EClass to extend: the extra resource lands on the
 * <em>generated</em> {@code Admin}, which every provider in the runtime shares. From that moment
 * no provider can be created at all - step 8 shows an unrelated model failing the same way - 
 * because {@code createAdminServiceForProvider} walks the admin's features and the dynamic one
 * sends the gateway's own {@code eIsSet} override into an infinite ping-pong with EMF's:
 *
 * <pre>
 * AdminImpl.eIsSet(int) -> ServiceImpl.eIsSet(int):182 -> BasicEObjectImpl.eIsSet(EStructuralFeature):1295
 *   -> ServiceImpl.eIsSet(EStructuralFeature):99 -> BasicEObjectImpl.eIsSet(int):1277 -> eDerivedStructuralFeatureID
 * </pre>
 *
 * So the route to an extra admin attribute is the other one: an {@code Admin} <em>subclass</em>
 * of its own, installed with one {@code SensinactEMFProvider.update()} and read back with
 * {@code twin.getResource(providerId, "admin", name)} - see {@link EmfReadChannelProbeTest} and
 * the item 8 section of {@code docs/udp-adapter-mapping-gaps.md}.
 * <p>
 * To run it: drop the {@code @Disabled} and run this class <b>alone</b>, because it leaves the
 * framework with no usable admin service for any later test:
 *
 * <pre>
 * ./gradlew :org.eclipse.fennec.event.atlas.mapping.tests:testOSGi \
 *     --tests 'org.eclipse.fennec.event.atlas.mapping.tests.ModelManagerAdminResourceProbeTest'
 * </pre>
 *
 * Every step is guarded, so a failure names itself instead of killing the gateway thread.
 */
@ExtendWith(BundleContextExtension.class)
@ExtendWith(ServiceExtension.class)
public class ModelManagerAdminResourceProbeTest {

	private static final String PKG_URI = "https://fennec.eclipse.org/event.atlas/probe/adminextra/1.0";
	private static final String MODEL = "AdminExtra";
	private static final String OTHER_MODEL = "AdminPlain";
	private static final String ID = "adminextra1";
	private static final String OTHER_ID = "adminplain1";
	private static final String EXTRA = "viewport";
	private static final String VALUE = "a polygon set through the resource API";

	@InjectService
	GatewayThread gatewayThread;

	@Test
	@Disabled("destructive: it adds a dynamic attribute to the shared generated Admin EClass, "
			+ "after which no provider in the framework can be created. Run it alone, never in the suite.")
	void probe() throws Exception {
		List<String> report = gatewayThread.execute(new AbstractSensinactEMFCommand<List<String>>() {
			@Override
			protected Promise<List<String>> call(SensinactEMFDigitalTwin twin, SensinactEMFModelManager mmgr,
					PromiseFactory pf) {
				List<String> lines = new ArrayList<>();

				step(lines, "1 create a model with one ordinary service", () -> {
					EMFModel model = mmgr.getModel(PKG_URI, MODEL);
					if (model == null) {
						model = mmgr.createModel(PKG_URI, MODEL).build();
						model.createService("data").withResource("count")
								.withResourceType(ResourceType.SENSOR).withType(Integer.class).buildAll();
					}
					return "services=" + model.getServices().keySet() + " frozen=" + model.isFrozen()
							+ " dynamic=" + model.isDynamic();
				});

				step(lines, "2 is admin a service of that model, and whose EClass is it?", () -> {
					EMFService admin = mmgr.getModel(PKG_URI, MODEL).getServices().get("admin");
					if (admin == null) {
						return "the model lists no admin service";
					}
					EClass eClass = admin.getServiceEClass();
					return "eClass=" + eClass.getName() + " nsURI=" + eClass.getEPackage().getNsURI()
							+ " | is the generated Admin=" + (eClass == ProviderPackage.Literals.ADMIN)
							+ " | resources=" + admin.getResources().keySet();
				});

				step(lines, "3 ModelBuilder route: createService(\"admin\")", () -> {
					mmgr.getModel(PKG_URI, MODEL).createService("admin").withResource(EXTRA)
							.withResourceType(ResourceType.SENSOR).withType(String.class).build().build();
					return "accepted - the model now declares its own admin";
				});

				step(lines, "4 add the extra resource to the admin service of the model", () -> {
					EMFService admin = mmgr.getModel(PKG_URI, MODEL).getServices().get("admin");
					admin.createResource(EXTRA).withResourceType(ResourceType.SENSOR).withType(String.class).build();
					boolean onGenerated = ProviderPackage.Literals.ADMIN.getEStructuralFeature(EXTRA) != null;
					return "admin resources now=" + admin.getResources().keySet()
							+ " | the GENERATED Admin EClass carries it=" + onGenerated;
				});

				step(lines, "5 create a provider of that model", () -> {
					SensinactEMFProvider provider = twin.createProvider(PKG_URI, MODEL, ID);
					return "services=" + provider.getServices().keySet()
							+ " | admin resources=" + provider.getServices().get("admin").getResources().keySet();
				});

				step(lines, "6 set the value through the provider's own resource lookup", () -> {
					SensinactResource resource = twin.getProvider(ID).getResource("admin", EXTRA);
					if (resource == null) {
						return "getResource(admin, " + EXTRA + ") is null - nothing to set";
					}
					resource.setValue(VALUE, Instant.now()).getValue();
					return "set";
				});

				step(lines, "7 read it back, through all four lookups", () -> {
					SensinactEMFProvider provider = twin.getProvider(ID);
					String viaProvider = describe(provider.getResource("admin", EXTRA));
					String viaTwin = describe(twin.getResource(ID, "admin", EXTRA));
					ResourceSnapshot snapshot = twin.snapshotResource(ID, "admin", EXTRA);
					String viaSnapshot = snapshot == null ? "null"
							: "found value=" + (snapshot.getValue() == null ? "unset"
									: String.valueOf(snapshot.getValue().getValue()));
					Provider emf = provider.getEMFProvider();
					Admin admin = emf.getAdmin();
					EStructuralFeature feature = admin.eClass().getEStructuralFeature(EXTRA);
					String viaEmf = feature == null ? "the stored admin's EClass has no " + EXTRA
							: "eClass=" + admin.eClass().getName() + " value=" + admin.eGet(feature);
					return "provider.getResource=" + viaProvider + " | twin.getResource=" + viaTwin
							+ " | snapshotResource=" + viaSnapshot + " | EMF admin: " + viaEmf;
				});

				step(lines, "8 does it leak to a provider of an unrelated model?", () -> {
					EMFModel other = mmgr.getModel(PKG_URI, OTHER_MODEL);
					if (other == null) {
						other = mmgr.createModel(PKG_URI, OTHER_MODEL).build();
						other.createService("data").withResource("count")
								.withResourceType(ResourceType.SENSOR).withType(Integer.class).buildAll();
					}
					SensinactEMFProvider provider = twin.createProvider(PKG_URI, OTHER_MODEL, OTHER_ID);
					return "admin resources of " + OTHER_ID + "="
							+ provider.getServices().get("admin").getResources().keySet();
				});

				// The two operations a dynamic EAttribute on a GENERATED service EClass is known
				// to break. Both are ordinary gateway traffic, so a StackOverflowError here means
				// the extra resource cost the runtime its admin service.
				step(lines, "9 eIsSet and EcoreUtil.copy of the stored admin", () -> {
					Provider emf = twin.getProvider(ID).getEMFProvider();
					Admin admin = emf.getAdmin();
					EStructuralFeature feature = admin.eClass().getEStructuralFeature(EXTRA);
					String isSet = feature == null ? "no feature" : String.valueOf(admin.eIsSet(feature));
					String copied = EcoreUtil.copy(emf) == null ? "null" : "ok";
					return "eIsSet=" + isSet + " | copy=" + copied;
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

		report.forEach(line -> System.out.println("ADMINPROBE " + line));
	}

	@FunctionalInterface
	private interface ThrowingSupplier {
		String get() throws Exception;
	}
}
