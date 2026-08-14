# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## What this repository is

`event.atlas` — "Fennec Event Atlas", part of the
[Eclipse Fennec](https://projects.eclipse.org/projects/technology.fennec) project (EPL-2.0).
A **bnd/OSGi + Gradle hybrid workspace** (the shared `fennec-gradle` scaffolding) whose subject
is the **Eclipse SensiNact mapping**: turn instances of a domain-specific EMF model into
SensiNact digital-twin providers declaratively, via a mapping XMI instead of hand-written
transformation code — plus the runtime that hosts it (launch + docker assemblies) and the
southbound adapters that feed payloads in.

Bnd projects (a directory with a `bnd.bnd`; the bnd workspace plugin sweeps each into the
Gradle graph automatically):

| Directory | Contents |
|---|---|
| `…event.atlas.mapping` | the mapping metamodel (`model/event-atlas-mapping.ecore` → `src-gen`) + the mapping engine + its DS components |
| `…event.atlas.mapping.tests` | OSGi integration tests (Felix via the bnd launcher) + the domain test models |
| `…event.atlas.mapping.runtime` | **no code** — carries `launch.bndrun` and `eventatlas.runtime_docker.bndrun`, and the `runtime/{mappings,profiles}` mount-point skeleton |
| `…event.atlas.mapping.local.config` | resource-only configurator bundle for `launch.bndrun` (Model Atlas client + file provider) |
| `…event.atlas.mapping.docker.config` | resource-only configurator bundle baked into the docker image (file providers + northbound REST) |
| `…event.atlas.mapping.test.component` | test-only southbound simulator (`WeatherReportsSimulator`), renders a WeatherReports XMI periodically and pushes it |
| `…event.atlas.mqtt.southbound.adapter` | **empty scaffold** (`bnd.bnd` only) — the work in progress on the `southbound_adapters` branch |
| `…event.atlas.rest.southbound.adapter` | **empty scaffold** (`bnd.bnd` only) — likewise |
| `org.eclipse.fennec.model.atlas.eobject.provider` | note the *different* namespace: a generic **Model Atlas** content source for the emf.osgi EObject registry, not mapping-specific |

Java packages follow the BSNs: `org.eclipse.fennec.event.atlas.mapping` (hand-written) and
`org.eclipse.fennec.event.atlas.model.mapping` (generated, genmodel `basePackage`
`org.eclipse.fennec.event.atlas.model`).

**The nsURI is load-bearing.** Every mapping XMI names
`https://fennec.eclipse.org/event.atlas/mapping/1.0` in its root element, so changing it
invalidates all of them at once (`PackageNotFoundException` on load). If it ever changes,
`grep -rl` the old URI over `**/*.xmi` and the docs and re-run the build — the OSGi tests load
XMIs from `data/` and fail loudly.

## Build & test

Requires **Java 21** (`javac.source/target: 21` in `cnf/ext/fennec.bnd`). bnd toolchain is the
**7.4.0 snapshot** (`gradle.properties`, from the bndtools snapshot repo) — released 7.3.0 has a
`-pom` snapshot regression that breaks bundle jars. Always use the Gradle wrapper.

```bash
./gradlew clean build            # codegen, compile, JUnit, OSGi tests, coverage
./gradlew :org.eclipse.fennec.event.atlas.mapping:test --tests '*ProviderModelMapperTest*'
./gradlew :org.eclipse.fennec.event.atlas.mapping.tests:testOSGi   # OSGi tests alone
./gradlew codeCoverageReport     # aggregate JaCoCo (xml for Sonar + html)
./gradlew perfTest               # @Tag("perf") only — excluded from `build`
./gradlew remoteTest             # @Tag("remote") only — excluded from `build`
```

Baseline as of 2026-08-14: `./gradlew clean build` is green — **54 OSGi tests, 1 `@Disabled`**
(the known admin-service read gap) — plus the plain-JUnit `ProviderModelMapperTest`.

- **`build` already runs `testOSGi`** — the tests project's `check` depends on it, so a plain
  `./gradlew build` launches Felix. No need to add `testOSGi` to the command line.
- **bnd `resolve`/`export` tasks are not parallel-safe in this workspace** — CI passes
  `gradle-parallel: false` for exactly this reason. Don't add `--parallel` locally when
  resolving or exporting.
- Plain-JUnit tests live in each bnd project's **`test/`** folder (JUnit 5 + Mockito + AssertJ,
  pinned in the root `build.gradle`, applied to every subproject). OSGi tests live in
  `…mapping.tests/src/` and run in a real framework via `@ExtendWith(ServiceExtension.class)` +
  `@InjectService`.
- The tests project needs its per-project `build.gradle` pointing `testOSGi` at the **resolved**
  bndrun — without it the raw bndrun is launched and the runpath fails to assemble
  (`NoClassDefFoundError: org/osgi/framework/ServiceListener`):
  ```gradle
  def resolveTask = tasks.named("resolve.test") { outputBndrun = layout.buildDirectory.file("test.bndrun") }
  tasks.named("testOSGi") { bndrun = resolveTask.flatMap { it.outputBndrun } }
  ```
- **`coverageFloorBundles` in `build.gradle` is empty**, so the 30 % JaCoCo tripwire is inert.
  The mapping engine is covered by the *OSGi* tests, whose exec file
  `jacocoTestCoverageVerification` does not read. Add the bundle once its `test/` folder clears
  the floor on its own.
- License headers (EPL-2.0) are enforced by `.licenserc.yaml` (apache/skywalking-eyes) inside the
  shared CI workflow; many file types are exempt (`paths-ignore`, incl. `**/src-gen/**`, `cnf/**`,
  `*.xmi`, `*.ecore`).

## Running the runtime

Two bndruns live in `…mapping.runtime` (`…mapping/launch.bndrun` is an older leftover):

```bash
./gradlew :org.eclipse.fennec.event.atlas.mapping.runtime:run.launch      # dev runtime, Felix
./gradlew :org.eclipse.fennec.event.atlas.mapping.runtime:resolve.launch  # recompute -runbundles
./gradlew :org.eclipse.fennec.event.atlas.mapping.runtime:export.eventatlas.runtime_docker
```

- `launch.bndrun` — dev playground: mapping engine + SensiNact gateway + Gogo shell + the
  **test simulator** + the Model Atlas client (`local.config` points it at
  `http://localhost:8086/atlas/rest`, scope `jena`).
- `eventatlas.runtime_docker.bndrun` — self-contained image runtime: engine + gateway +
  northbound REST on 8080, mappings/profiles read from XMI files under
  `/opt/eventatlas/runtime`, **no** Model Atlas and no simulator.
- Gradle does **not** forward stdin to the launcher; for an interactive Gogo shell use
  `export.launch` and run the resulting `generated/distributions/executable/launch.jar` directly.
- **The bnd resolver only sees package requirements, not DS service references** — that is why
  `org.apache.aries.typedevent.bus` is listed in `-runrequires` by hand; without it the gateway
  resolves but never activates.
- **All three bndruns blacklist `org.apache.aries.spifly.dynamic.framework.extension`.** The
  framework-extension spifly variant breaks the typed-event bus at startup ("Weaving hook
  failed") and the SensiNact `GatewayThread` never activates. Keep the classic
  `spifly.dynamic.bundle`.
- **Docker config is a bundle, not a mounted file.** The Felix configurator's
  `configurator.initial` pass runs before the runtime's JSON provider is wired and fails with
  "Invalid JSON", so the docker wiring is baked into `…mapping.docker.config`. See
  `docker/eventatlas/README.md` for the local image build and the `content/` layout.

## The mapping domain (big picture)

The authoritative user-facing description is **`docs/sensinact-mapping-user-guide.md`** (644
lines, kept in sync with the metamodel — read it before changing mapping semantics).

**Metamodel** — `model/event-atlas-mapping.ecore` + `.genmodel`, EMF package
`org.eclipse.fennec.event.atlas.model.mapping`. Generated into `src-gen` by **bnd `-generate`**
(`generate=fennecEMF`) at build time — never hand-edit `src-gen`, and for `.ecore`/`.genmodel`
changes **ask the user** rather than writing model code.

Core concepts, all expressed as XMI instances of that metamodel:

- **`ProviderMapping`** — the root: `mid` (its registry key), which source `EClass`es it applies
  to (`providerClasses`), the provider `name`, `services` → `resources`, an `admin` service, an
  optional `profile`.
- **Feature paths** — the central mechanism: an ordered list of `EStructuralFeature`s (`href`s
  into the source `.ecore`) navigating from the source object to a value. Because the containing
  feature is the abstract `EStructuralFeature`, `featurePath` / `valueFeature` / `unitFeature` /
  admin `*Ref` entries **must** carry `xsi:type="ecore:EAttribute|EReference"`.
- **`MappingProfile`** — a reusable *target* structure several vendor mappings conform to
  (`providerStrategy` `SEPARATE` vs `UNIFIED`), keyed by `profileId`; the profile registry
  validates conformance against `required` / `expectedType` / `expectedUnit`.
- **Persistence rules** — `PersistenceRuleRegistry` holds reusable `ChangeRule` subtypes
  (percentage / absolute / count / time-throttle) and `DeletionRule`s that `ResourceMapping`s
  reference by id. **Model-only today**: the enforcing notification proxy is a planned separate
  component — design in `docs/WP-SN-2-persistence-rules-plan.md` (it also documents why a
  transparent interceptor on SensiNact's fan-out typed-event bus cannot work).

## Runtime layering

Package `org.eclipse.fennec.event.atlas.mapping`, `@Export`ed via `package-info.java` (not
`Export-Package` in `bnd.bnd`).

**Mappings are content in an EObject registry, not OSGi services.** This is the single most
important thing to know, and the part that changed most recently:

- `ProviderMappingRegistryImpl` is an **`EObjectRegistryListener`** whiteboard service bound to
  the named emf.osgi registry **`sensinact-mappings`** (entry keys = the mapping's `mid`;
  override the registry via the `emf.eobject.registry.name` component property; config pid
  `sensinact.southbound.emf.mapping`). The registry replays its content on bind; the facade
  validates each entry, indexes it by `providerClasses` and builds the provider model in the
  twin. `MappingProfileRegistryImpl` does the same against **`sensinact-profiles`** (keys =
  `profileId`).
- Content reaches those registries through registry *providers*: emf.osgi's
  `FileEObjectProvider` for local XMI directories, `AtlasEObjectProvider` for a Model Atlas, or
  programmatically via `registerModelMapping` (what the OSGi tests do). See the two
  `configs/config.json` files for both wirings side by side.
- **`InstancePusher`** (`pushInstance(EObject)` → number of mappings applied) is the ingress for
  southbound connectors: receive a payload, deserialize it to an EObject, push. Lookup keys on
  **EClass identity**, so incoming instances must come from the same `EPackage` instance the
  mappings resolved their `providerClasses` against.
- `ValueMapper` / `ValueMapperFactory` — the plain-Java engine face: `mapInstance`,
  `mapResourceValues`, `validateInstance`. `ValueMapperImpl` (~1100 lines) does path navigation,
  collection handling, type conversion, timestamp strategies, admin/location mapping.
- `ProviderModelSensinactMapper` — builds the provider model in the twin from a `ProviderMapping`
  (via its inner `Factory`, needs the profile registry).
- `converters/` — `TypeConverter` + `TypeConverterRegistry` for source→resource value coercion.
- All twin interaction happens on the SensiNact **`GatewayThread`** (via
  `AbstractSensinactCommand`); tests follow the same pattern.
- `MappingConfigurator` is a **generated** DS component in `src-gen`, not hand-written code.

The mapping bundle declares `Import-Package: org.eclipse.sensinact.*;resolution:=optional` on
purpose: it doubles as the metamodel carrier, so runtimes that only *read* mapping XMIs (a Model
Atlas content source, say) must resolve it without the SensiNact gateway present. Keep that
optional-import trick intact when adding SensiNact dependencies.

**Where the models live** — `…mapping/model/` holds only the metamodel plus `examples/` and
`profiles/` (example mappings, battery/temperature/weather-station profiles). The *domain*
models being mapped (Dragino, EM310UDL, EcoWitt, DWD weather, LoRaWAN uplink) live in
`…mapping.tests/model/`, with matching payloads in `…mapping.tests/data/`. Whatever domain
EPackages a runtime maps must be registered in that runtime.

## Workspace & OSGi conventions

- **Two build systems, one tree.** Gradle (`build.gradle`, `settings.gradle`) and bnd (`cnf/`)
  operate over the same directories. `cnf/ext/fennec.bnd` is the additive bnd config; `build`,
  `docs`, `docs-site`, `docker` are excluded from bnd's project sweep via `bnd_exclude`.
- **Project coordinates are single-source in `gradle.properties`** (`github_org`,
  `github_repository=event.atlas`, `maven_group_id=org.eclipse.fennec.event.atlas`); bnd
  `-include`s the same file. Never hardcode them.
- **Runtime/OSGi dependencies come from Maven Central via bnd**, listed in `cnf/ext/central.mvn`
  — *not* Gradle. Gradle `dependencies` are test-only. Broader dependency sets are switched on
  through `-library:` in `fennec.bnd` (`fennec`, `fennecTest`, `fennecJacoco`, `fennecEMF`,
  `fennecM2X`, `fennecJPA`, `fennecEMFModels`, `fennecCodec`); a project opts into a setup with
  e.g. `-library: enableEMF` / `enableOSGi-Test` in its own `bnd.bnd`.
- After bumping a library version in `central.mvn`, clear `cnf/cache/<bndversion>/expanded` so
  the new library content is unpacked.
- A resource-only config bundle is `-resourceonly: true` + `-includeresource:
  OSGI-INF/configurator/=configs/` + the configurator `Require-Capability` (copy
  `…local.config/bnd.bnd`).

## Docs site

VitePress site in `docs-site/`, **not** wired into Gradle. `docs/` (hand-written markdown) is the
source of truth; `docs-site/sync-guides.mjs` copies an **allowlist** (`docs-site/guides.mjs`:
`GUIDES`, `EXAMPLES`) into `docs-site/docs/{guides,examples}/` before each build, rewriting links
to non-published docs into GitHub blob URLs. The generated dirs are git-ignored. Build with
`cd docs-site && npm ci && npm run docs:build` (`docs:dev` for live preview).

## Branch flow & CI

PRs merge into **`snapshot`** (snapshot artifacts published from there); releases are cut from
the protected **`main`** branch. Workflows delegate to **SHA-pinned reusable workflows in
`eclipse-fennec/.github`**, except the repo-local container job:

- `build.yml` — `reusable-verify.yml` on PRs and every branch except `main`/`snapshot`; it also
  runs `runtime:resolve.launch` and `runtime:export.eventatlas.runtime_docker` so a bndrun that
  no longer resolves fails the PR.
- `snapshot.yml` / `release.yml` — `verify → release (do-release: false|true) → container + docs`,
  gated by `needs:`. The release job exports the docker runtime jar **in the same build that
  publishes to Maven** and hands it over as the `release-jars` artifact.
- `reusable-container.yml` (repo-local) — does *not* rebuild the jar; it downloads `release-jars`,
  reads `Bundle-Version` out of the mapping jar with the bnd CLI, and pushes multi-arch images to
  `docker.io/eclipsefennec/event.atlas` and `ghcr.io/eclipse-fennec/event.atlas` tagged
  `snapshot`/`latest` plus that version.
- `docs.yml` — `workflow_dispatch` only; plus `scorecard.yml` and `dependency-review.yml`.

Branch protection and secret scanning live in the EF-managed `eclipse-fennec/.eclipsefdn`
otterdog repo, not here.
