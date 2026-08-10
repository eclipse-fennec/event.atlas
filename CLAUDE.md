# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## What this repository is

`event.atlas` — "Fennec Event Atlas", part of the
[Eclipse Fennec](https://projects.eclipse.org/projects/technology.fennec) project (EPL-2.0).
It is a **bnd/OSGi + Gradle hybrid workspace** (same `fennec-gradle` scaffolding as the other
Fennec repos) whose current content is the **Eclipse SensiNact mapping** utility: turn
instances of a domain-specific EMF model into SensiNact digital-twin providers,
declaratively, via an XMI mapping instead of hand-written transformation code.

Two sub-projects (each a bnd project — a directory with a `bnd.bnd`):

| Directory | Contents |
|---|---|
| `org.eclipse.fennec.event.atlas.mapping` | the mapping metamodel (`model/event-atlas-mapping.ecore` → `src-gen`) + the mapping engine + its OSGi components |
| `org.eclipse.fennec.event.atlas.mapping.tests` | OSGi integration tests (Felix via the bnd launcher) + a LoRaWAN test model |

Java packages follow the BSNs: `org.eclipse.fennec.event.atlas.mapping` (hand-written) and
`org.eclipse.fennec.event.atlas.model.mapping` (generated, genmodel `basePackage`
`org.eclipse.fennec.event.atlas.model`). The model is `model/event-atlas-mapping.ecore`,
nsURI `https://fennec.eclipse.org/event.atlas/mapping/1.0`.

**The nsURI is load-bearing.** Every mapping XMI names it in its root element, so changing it
invalidates all of them at once (`PackageNotFoundException` on load) — the SensiNact-era URI
`…/sensinact/core/mapping/1.0` was migrated across all in-repo XMIs on 2026-08-10. If it ever
changes again, `grep -rl` the old URI over `**/*.xmi` and the docs, and re-run the build: the
OSGi tests load the XMIs from `data/` and fail loudly.

## Current state — read before building

This workspace was scaffolded from the **`emf.util`** repository (single `Initial commit`,
everything else untracked), then renamed throughout — directories, BSNs, Java packages,
genmodel, nsURI, bnd descriptors, docs and the docs site. `emf.util` was **only a template**;
this project has no relationship to it, and no reference to it should reappear. The rename is
**complete and verified**: `./gradlew clean build` is green including the 45 OSGi tests
(1 `@Disabled` — the known admin-service read gap), and `launch.bndrun` brings up a Felix in
which the mapping bundle's three DS components (`MappingConfigurator`,
`ProviderMappingRegistryImpl`, `MappingProfileRegistryImpl`) reach `ACTIVE` alongside
SensiNact's `GatewayThreadImpl`.

Two consequences of the template origin worth knowing:

- **`coverageFloorBundles` in `build.gradle` is empty**, so the 30 % JaCoCo tripwire is inert.
  It is not wired to the mapping bundle because that code is covered by the *OSGi* tests, whose
  exec file `jacocoTestCoverageVerification` does not read. Add the bundle once its plain-JUnit
  `test/` folder clears the floor on its own.
- `org.eclipse.fennec.event.atlas.mapping/prompt.txt` (100 kB Claude transcript) and
  `example.txt` are scratch notes, not sources.

## Build & test

Requires **Java 21** (`javac.source/target: 21` in `cnf/ext/fennec.bnd`). bnd toolchain is the
**7.4.0 snapshot** (`gradle.properties`, from the bndtools snapshot repo) — released 7.3.0 has a
`-pom` snapshot regression that breaks bundle jars. Always use the Gradle wrapper.

```bash
./gradlew clean build            # everything: codegen, compile, JUnit, OSGi tests, coverage
./gradlew :org.eclipse.fennec.event.atlas.mapping:test --tests '*ProviderModelMapperTest*'
./gradlew :org.eclipse.fennec.event.atlas.mapping.tests:testOSGi   # OSGi tests alone
./gradlew codeCoverageReport     # aggregate JaCoCo (xml for Sonar + html)
./gradlew perfTest               # @Tag("perf") only — excluded from `build`
./gradlew remoteTest             # @Tag("remote") only — excluded from `build`
```

- **`build` already runs `testOSGi`** — the tests project's `check` depends on it (via the
  `resolve.test` wiring below), so a plain `./gradlew build` launches Felix. There is no need
  to add `testOSGi` to the command line.
- Plain-JUnit tests live in each bnd project's **`test/`** folder (JUnit 5 + Mockito + AssertJ,
  versions pinned in the root `build.gradle` and applied to every subproject).
  `…mapping/test/` currently holds `ProviderModelMapperTest`.
- OSGi tests live in `…mapping.tests/src/` and run in a real framework. That project needs its
  per-project `build.gradle` pointing `testOSGi` at the **resolved** bndrun — without it the raw
  bndrun is launched and the framework runpath fails to assemble
  (`NoClassDefFoundError: org/osgi/framework/ServiceListener`):
  ```gradle
  def resolveTask = tasks.named("resolve.test") { outputBndrun = layout.buildDirectory.file("test.bndrun") }
  tasks.named("testOSGi") { bndrun = resolveTask.flatMap { it.outputBndrun } }
  ```
- **`launch.bndrun`** (core project) is a manual playground, not part of `build`:
  `./gradlew :org.eclipse.fennec.event.atlas.mapping:run.launch` starts Felix with the
  SensiNact gateway impl, the Fennec EMF OSGi component, the mapping bundle and a Gogo shell;
  `resolve.launch` recomputes its `-runbundles`. Note that Gradle does **not** forward stdin to
  the launcher, so for an interactive shell use `export.launch` and run the resulting
  `generated/distributions/executable/launch.jar` directly. **The resolver only sees package
  requirements, not DS service references** — that is why `org.apache.aries.typedevent.bus`
  has to be listed in `-runrequires` by hand; without it the gateway resolves but never
  activates.
- License headers (EPL-2.0) are enforced by `.licenserc.yaml` (apache/skywalking-eyes) inside the
  shared CI workflow; many file types are exempt (`paths-ignore`, incl. `**/src-gen/**`, `cnf/**`,
  `*.xmi`, `*.ecore`).

## The mapping domain (big picture)

The authoritative user-facing description is **`docs/sensinact-mapping-user-guide.md`** (580
lines, kept in sync with the metamodel — read it before changing mapping semantics).
`docs/MAPPING_DOCUMENTATION.md` next to it is only a pointer (an older draft was removed).

**Metamodel** — `model/event-atlas-mapping.ecore` + `.genmodel`, nsURI
`https://fennec.eclipse.org/event.atlas/mapping/1.0`, EMF package
`org.eclipse.fennec.event.atlas.model.mapping`. Generated into `src-gen` by **bnd `-generate`**
(`generate=fennecEMF`) at build time — never hand-edit `src-gen`, and for `.ecore`/`.genmodel`
changes **ask the user** rather than writing model code.

Core concepts, all expressed as XMI instances of that metamodel:

- **`ProviderMapping`** — the root: which source `EClass`es it applies to (`providerClasses`),
  the provider `name`, `services` → `resources`, an `admin` service, an optional `profile`.
- **Feature paths** — the central mechanism: an ordered list of `EStructuralFeature`s
  (`href`s into the source `.ecore`) navigating from the source object to a value. Because the
  containing feature is the abstract `EStructuralFeature`, `featurePath` / `valueFeature` /
  `unitFeature` / admin `*Ref` entries **must** carry `xsi:type="ecore:EAttribute|EReference"`.
- **`MappingProfile`** — a reusable *target* structure several vendor mappings conform to
  (`providerStrategy` `SEPARATE` vs `UNIFIED`); the profile registry validates conformance
  against `required` / `expectedType` / `expectedUnit`.
- **Persistence rules** — `PersistenceRuleRegistry` holds reusable `ChangeRule` subtypes
  (percentage / absolute / count / time-throttle) and `DeletionRule`s that `ResourceMapping`s
  reference by id. **Model-only today**: the enforcing notification proxy is a planned separate
  component — design in `org.eclipse.fennec.event.atlas.mapping/docs/WP-SN-2-persistence-rules-plan.md`
  (it also documents why a transparent interceptor on SensiNact's fan-out typed-event bus
  cannot work).

**Runtime layering** (package `org.eclipse.fennec.event.atlas.mapping`, `@Export`ed via
`package-info.java`, not `Export-Package` in `bnd.bnd`):

- `ValueMapper` / `ValueMapperFactory` — the plain-Java engine face:
  `mapInstance` (update the twin), `mapResourceValues` (extract only), `validateInstance`.
  `ValueMapperImpl` (~1100 lines, package `…mapping.impl`) does path navigation, collection
  handling, type conversion, timestamp strategies, admin/location mapping.
- `ProviderModelSensinactMapper` — builds the *provider model* in the twin from a
  `ProviderMapping` (created through its inner `Factory`, needs the profile registry).
- `ProviderMappingRegistryImpl` — whiteboard `@Component`
  (configuration pid `sensinact.southbound.emf.mapping`) collecting `ProviderMapping`
  **services** dynamically, keyed by `providerClasses`; `MappingProfileRegistryImpl` does the
  same for `MappingProfile` services, keyed by `profileId`.
- `converters/` — `TypeConverter` + `TypeConverterRegistry` for source→resource value coercion.
- All twin interaction happens on the SensiNact **`GatewayThread`** (via
  `AbstractSensinactCommand`); tests follow the same pattern.

The payload `EPackage`s (the domain models being mapped) must be registered in the same runtime
so both the mapping XMI and the incoming instances resolve. `model/` ships several example
domain models (Dragino, EM310UDL, EcoWitt, DWD weather, SensorThings) plus example mappings and
profiles under `model/examples/` and `model/profiles/`; runtime sample data is in `data/`.

## Workspace & OSGi conventions

- **Two build systems, one tree.** Gradle (`build.gradle`, `settings.gradle`) and bnd (`cnf/`)
  operate over the same directories. `cnf/ext/fennec.bnd` is the additive bnd config; `build`,
  `docs`, `docs-site` are excluded from bnd's project sweep via `bnd_exclude`.
- **Project coordinates are single-source in `gradle.properties`** (`github_org`,
  `github_repository=event.atlas`, `maven_group_id=org.eclipse.fennec.event.atlas`); bnd
  `-include`s the same file. Never hardcode them.
- **Runtime/OSGi dependencies come from Maven Central via bnd**, listed in `cnf/ext/central.mvn`
  — *not* Gradle. Gradle `dependencies` are test-only. Broader dependency sets are switched on
  through `-library:` in `fennec.bnd` (`fennec`, `fennecTest`, `fennecJacoco`, `fennecEMF`,
  `fennecM2X`, `fennecJPA`, `fennecEMFModels`, `fennecCodec`); a project then opts into a setup
  with e.g. `-library: enableEMF` / `enableOSGi-Test` in its own `bnd.bnd`.
- SensiNact itself (`org.eclipse.sensinact.gateway.core.*`) comes in through `central.mvn` +
  the projects' `-buildpath`.
- **A new bundle is a new top-level directory with a `bnd.bnd`** — the bnd workspace plugin
  sweeps it into the Gradle graph automatically; the root build applies `java` + `jacoco` to
  every subproject.
- After bumping a library version in `central.mvn`, clear `cnf/cache/<bndversion>/expanded` so
  the new library content is unpacked.

## Docs site

VitePress site in `docs-site/`, **not** wired into Gradle.
`docs/` (hand-written markdown) is the source of truth; `docs-site/sync-guides.mjs` copies an
**allowlist** (`docs-site/guides.mjs`: `GUIDES`, `EXAMPLES`) into `docs-site/docs/{guides,examples}/`
before each build, rewriting links to non-published docs into GitHub blob URLs. The generated
dirs are git-ignored. Build with `cd docs-site && npm ci && npm run docs:build` (`docs:dev` for
live preview).

## Branch flow & CI

PRs merge into **`snapshot`** (snapshot artifacts published from there); releases are cut from
the protected **`main`** branch. All four workflows are thin orchestrators that delegate to
**SHA-pinned reusable workflows in `eclipse-fennec/.github`**:

- `build.yml` — `reusable-verify.yml` on PRs and every branch except `main`/`snapshot`.
- `snapshot.yml` / `release.yml` — `verify → release (do-release: false|true) → docs`, gated by
  `needs:`, so a failed verify stops both the publish and the docs deploy.
- `docs.yml` — `workflow_dispatch` only; plus `scorecard.yml` and `dependency-review.yml`.

Branch protection and secret scanning live in the EF-managed `eclipse-fennec/.eclipsefdn`
otterdog repo, not here.
