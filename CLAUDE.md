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
| `…event.atlas.mapping.local.config` | resource-only configurator bundle for `launch.bndrun` (Model Atlas client + file provider + the MQTT/REST southbound wiring + the timescale history store) |
| `…event.atlas.mapping.docker.config` | resource-only configurator bundle baked into the docker image — four resources: `config.json` (file providers + Model Atlas client + MQTT southbound), `sensinact.json` (session manager, the named HTTP/Jersey whiteboards, northbound REST, SensorThings REST + MQTT broker), `timescale.json` (the history store) and `inference.json` (model inference, **off unless `EVENTATLAS_INFERENCE_ENABLED` and `INFERENCE_NAMESPACE` are set**) |
| `…event.atlas.mapping.test.component` | test-only southbound simulator (`WeatherReportsSimulator`), renders a WeatherReports XMI periodically and pushes it |
| `…event.atlas.southbound.common` | the shared southbound ingress: `PayloadIngest` deserializes a payload (XMI or JSON), pushes it and reports an `IngestResult` (`APPLIED`, `NO_MAPPING`, `MODEL_UNKNOWN`, `PARSE_ERROR`, `FORMAT_UNSUPPORTED`, …), plus the optional `UnknownModelHandler` hook it offers unhandled payloads to |
| `…event.atlas.southbound.sampling` | `PayloadSampleCollector` — the `UnknownModelHandler` implementation that buffers unhandled payloads per channel and hands a closed `PayloadSampleSet` to a `PayloadSampleSetHandler` |
| `…event.atlas.model.inference` | `ModelInferenceService` — the `PayloadSampleSetHandler` that turns a closed sample set into a model draft: fingerprint dedup, run rate limit, one prompt, one agentic completion, then a receipt. Carries **no EMF dependency**, which is what makes "never registers an inferred package locally" structural, and talks to an AI stack only through its own `ChatCompletion` port |
| `…event.atlas.model.inference.chat` | `ChatCompletionAdapter` — the only bundle here that depends on an AI stack: binds the `ChatCompletion` port to a Fennec AI `ChatCompletionService` and reads the agent's answer out of the response's content blocks. Not deploying it is one way a runtime opts out of inference; since 2026-09-04 configuration is the other |
| `…event.atlas.mqtt.southbound.adapter` | `MqttPayloadListener` — binds a SensiNact MQTT handler's topics and feeds each payload through `PayloadIngest` |
| `…event.atlas.rest.southbound.adapter` | `PayloadIngestResource` — `POST <whiteboard base>/ingest/{channel}`; the HTTP status mirrors the `IngestResult` outcome |

`docker/eventatlas/` (not a bnd project, in `bnd_exclude`) holds the Dockerfile and
`docker-compose.example.yml` (the image plus a TimescaleDB, the runnable history example); its
`content/` staging dir is git-ignored (see `docker/eventatlas/README.md`).

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
./gradlew :org.eclipse.fennec.event.atlas.mapping.runtime:export.eventatlas.runtime_docker  # docker runtime jar
```

Baseline as of 2026-08-25: `./gradlew clean build` is green — **68 OSGi tests, 1 `@Disabled`**
(the known admin-service read gap) — plus 39 plain-JUnit tests (`ProviderModelMapperTest`,
`ChangeRuleFilterImplTest`, `BindingResolverTest`, `MappingProfileValidationTest`,
`GeneratedResourceValidationTest`).

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
  Model Atlas client + the history store. It does **not** carry `…mapping.test.component` or any
  domain model any more — add both to `-runrequires` (and re-resolve) if you want the simulator to
  push, otherwise the mapping's `providerClasses` stay unresolved. `local.config` points the client
  at `http://localhost:8080/atlas/rest`, scope `jena` — where the model.atlas jena container
  publishes, per `docker-compose-jena.yml` in `eclipse-fennec/model.atlas`. Because the Atlas owns
  8080, `configs/sensinact.json` puts the runtime's own whiteboard on **8090**, so the local REST
  bases are `http://localhost:8090/event/rest/{sensinact,v1.1,ingest}` — otherwise the same file as
  the docker one. Note
  `sensinact.json` configures `sensinact.northbound.rest`, but `launch.bndrun` carries no
  `…northbound.rest` bundle, so that config is inert and `/event/rest/sensinact/**` answers the
  SensorThings 500 described below, not a provider list.
  It also feeds **two** EObject registries from the Atlas:
  `sensinact-mappings` from the `sensinactmapping` atlas registry (key `mid`) and
  `sensinact-profiles` from `sensinactprofile` (key `profileId`) — the profiles registry is not
  optional, since a mapping whose profile cannot be resolved is skipped entirely.
- `eventatlas.runtime_docker.bndrun` — self-contained image runtime: engine + gateway +
  northbound REST + the SensorThings v1.1 REST gateway and MQTT broker; mappings/profiles read
  from XMI files under `/opt/eventatlas/runtime` **and** optionally from a Model Atlas; no
  simulator. All REST bases live under `/event/rest` (`…/sensinact`, `…/v1.1`) because
  `sensinact.json` configures a named Felix HTTP whiteboard on context path `event/` plus a
  Jersey whiteboard at `rest`; the bndrun sets `org.osgi.service.http.port=-1` so the default
  HTTP service does not compete for 8080. Ports, endpoints and the `$[env:…]` variables are
  documented in `docker/eventatlas/README.md`.
- **The SensorThings REST application owns the Jakarta-RS whiteboard root.** It declares no
  application base (its resources carry `@Path("/v1.1/…")` themselves), so it sits at
  `/event/rest` and every path there that no other application claims becomes a SensorThings
  404 which cannot serialize its own `ErrorResponse` → HTTP 500 "Request failed." rather than a
  404. **A new `@JakartarsResource` therefore needs its own application**: without an
  `osgi.jakartars.application.select` it joins the whiteboard's default application, which the
  SensorThings application shadows, and is never invoked. `PayloadIngestApplication`
  (`…rest.southbound.adapter`, base `ingest`, name `event-atlas-ingest`, config pid
  `event.atlas.southbound.rest`) is the pattern to copy — it is what keeps
  `POST <whiteboard base>/ingest/{channel}` reachable.
- Gradle does **not** forward stdin to the launcher; for an interactive Gogo shell use
  `export.launch` and run the resulting `generated/distributions/executable/launch.jar` directly.
- **The bnd resolver only sees package requirements, not DS service references** — that is why
  `org.apache.aries.typedevent.bus` is listed in `-runrequires` by hand; without it the gateway
  resolves but never activates.
- **All three bndruns blacklist `org.apache.aries.spifly.dynamic.framework.extension`.** The
  framework-extension spifly variant breaks the typed-event bus at startup ("Weaving hook
  failed") and the SensiNact `GatewayThread` never activates. Keep the classic
  `spifly.dynamic.bundle`.
- **JSON payloads need the EMF codec in the runbundles.** `PayloadIngest` picks the EMF
  resource factory by file extension, and EMF answers an unknown extension with its wildcard
  (`*`) factory — XMI in a Fennec runtime — so before the codec was added every JSON payload
  died in a SAX parser with `Content is not allowed in prolog` (issue #17). All three bndruns
  now require `org.eclipse.fennec.codec` by identity (it drags in `…codec.api`,
  `…codec.metadata` and `org.eclipse.fennec.emf.osgi.metadata`), which is what makes a JSON
  channel a *resolve-time* guarantee rather than a runtime surprise. `JsonPayloadIngestTest`
  (OSGi, with `data/dragino-example.json`) is the regression guard; `PayloadIngestImpl` now
  also reports a missing factory as `FORMAT_UNSUPPORTED` (HTTP 501) instead of parsing on.
- **History is the timescale provider, switched on by its config.** Both bndruns require
  `…southbound.history.timescale-provider`, which brings `org.postgresql.jdbc` and the two
  Aries `tx-control` bundles (it reaches the database through the OSGi Transaction Control
  service; those two also export the `org.osgi.service.transaction.control*` API packages, so
  no separate API bundle is needed). All three are third-party artifacts that
  `cnf/ext/sensinact.maven` pins but the Eclipse SensiNact repos do not host — they are
  declared in `central.mvn` for exactly the reason described below. The store's component is
  `configuration-policy=require`, so `configs/timescale.json` in the two config bundles is the
  on/off switch: with a database it creates the `sensinact.history` hypertable and records; with
  none it stays inactive and silent (the failure only reaches the OSGi log service, so `scr:list`
  is where an empty history shows up). Its `provider` name must match `history.provider` in
  `sensinact.json` — both are `brokerHistory`.
- **Docker config is a bundle, not a mounted file.** The Felix configurator's
  `configurator.initial` pass runs before the runtime's JSON provider is wired and fails with
  "Invalid JSON", so the docker wiring is baked into `…mapping.docker.config`. See
  `docker/eventatlas/README.md` for the local image build and the `content/` layout.

## Model inference (optional, off unless configured)

Unknown payloads can be turned into a **reviewed model draft** instead of being dropped. Nothing
about it is mandatory: a runtime without these bundles, or with inference switched off, ingests
exactly as before. The chain, one issue per link (#27 → #30):

```
PayloadIngest ──UnknownModelHandler──▶ PayloadSampleCollector ──PayloadSampleSetHandler──▶
    ModelInferenceService ──ChatCompletion──▶ ChatCompletionAdapter ──▶ ChatCompletionService
                                                                          + remote MCPEndpoint
```

**Two config switches turn it off, and they gate different costs.** Both default to off, so
deploying the bundles is safe — which is what lets the docker image ship them unconditionally
(`configs/inference.json`, `EVENTATLAS_INFERENCE_ENABLED` + `INFERENCE_NAMESPACE`).

- `enabled` on `event.atlas.southbound.sampling` gates **buffering**. Off, `PayloadSampleCollector`
  still registers as the `UnknownModelHandler` and declines each payload, so no window opens and
  no memory is held. `PayloadIngest`'s reference is dynamic, so flipping it takes effect on the
  next payload with no service churn.
- `namespace` on `event.atlas.model.inference` gates the **run**, which is what costs money. Blank
  (the default) means the service activates, logs that it has no namespace, and refuses every
  sample set. This one predates the docker work.

`enabled` true with a blank `namespace` is a deliberate state: payloads are sampled and the sets
dropped, so the log shows what *would* be inferred at no API cost. Note `ConfigurationPolicy` is
**`OPTIONAL`** on both components, so config *absence* is not the switch — the defaults are;
`REQUIRE` would have made the baked-in docker config pin inference permanently on.

**Unknown payloads are separated per channel, but the run budget is not.** The collector's window
key is `(source, namespaceUri, format)` — `source` being the concrete MQTT topic
(`MqttPayloadListener` passes the topic) or `rest/<channel>` — so two southbound adapters infer
two models independently, and `inFlight` is keyed by source so they run concurrently.
`RunRateLimiter`, though, holds **one** counter for the whole runtime: with
`maxRunsPerInterval=1` the first set to close in the interval takes the run and any other
channel's is refused *and discarded* (it releases its fingerprint claim, so that channel can
re-accumulate). Raise it to at least the number of distinct unknown families expected per
interval; the OCD default is 5, and both the docker and inference configs set 1 on purpose.

**There is no topic or channel filter for inference** — it consumes whatever the ingest channels
report as unknown, and only three outcomes reach it (`MODEL_UNKNOWN`, `EMPTY`, `PARSE_ERROR`;
not `NO_MAPPING`, `FORMAT_UNSUPPORTED` or `PUSH_FAILED`). The collector's `channels` property
looks like a filter but only overrides the per-channel close conditions. Since `PARSE_ERROR` is
in that set and the docker runtime serves `POST /event/rest/ingest/{channel}`, malformed input to
an open endpoint is also a trigger, bounded only by the run cap and the fingerprint dedup.

The agent behind the completion **authors and publishes the package itself**, through the
metamodel MCP server's tools; what returns is a receipt line. Nothing in this repository
registers an inferred package into a running framework — a draft is promoted by a human.

**Promoting it is not enough for a *running* runtime, though: the package arrives on the next
restart, not the next payload.** Verified end to end on 2026-09-01 (see
`docs/model-inference-test-log.md`). The Atlas client's `DriftWatcher` only refreshes entries it
already holds — `handleChangedNsUris` skips on `!held.contains(nsUri)`, `handleChangedObjects` on
`!anyHeld` — so a newly published nsURI is filtered out even though the scope's ETag moved and the
check fired. There is no polling counterpart for EPackages either: `EagerPrefetch` runs once at
activation. LAZY mode does not help, because a discriminator lookup
(`FeaturePathTypeResolver.scan`) searches *registered* packages and never asks for a specific
nsURI, so there is nothing for a lazy registry to resolve. Sketched as
`nsc/docs/issue-atlas-drift-ignores-new-packages.md`.

**Mappings are the exception, and that asymmetry is the useful part.**
`AtlasObjectSync.syncRegistry` re-runs `listObjectIds()` on every pass, so with
`refresh.interval.ms > 0` an Atlas-fed `ProviderMapping` added after start-up *is* discovered — no
restart. `inference.bndrun` now carries `org.eclipse.fennec.model.atlas.eobject.provider` and its
`AtlasEObjectProvider~jena` block for exactly this; a `FileEObjectProvider` cannot do it, because
it walks its directory once at activation and never again.

- **The four chat-completion bundles come from a Maven repo now — `cnf/local` is gone.** Since
  2026-09-04 `eclipse-fennec/fennec-ai` publishes a snapshot, so the `LocalIndexedRepo` that used
  to carry local builds of the AI api/impl/models has been deleted along with its
  `-plugin.0.Local` registration in `cnf/build.bnd`. They are declared in **`cnf/ext/nexus.maven`
  at `1.0.0-SNAPSHOT`** (so both bndruns' `-runbundles` name them `[1.0.0,1.0.1)`) and fetched by
  the `-plugin.6.nexus` repo in `cnf/ext/nexus.bnd` (`.6` because `fennec.bnd` already holds
  `.5.Central`).
  **They are published *only* to the DIM nexus** — `devel.data-in-motion.biz/nexus/repository/dim-snapshot`,
  anonymously readable. Not to Maven Central, and not to Sonatype's Central Snapshots either;
  both 404. So `nexus.bnd` is load-bearing rather than a convenience, and CI needs egress to that
  host for the bndrun re-resolve and the docker export. Beware bnd's cache sidecar in `~/.m2`
  when checking provenance: its `"uri"` field records the **first** URL of a comma-separated
  `snapshotUrl` list, not the host the bytes came from, so it can name a URL that 404s — compare
  the recorded `sha_1` against the candidate hosts instead.
  `org.eclipse.fennec.mcp.endpoint` is unaffected: it resolves from
  `org.eclipse.fennec.mcp:org.eclipse.fennec.mcp.endpoint:0.1.0-SNAPSHOT` in `central.mvn`.
  `RemoteMCPEndpoint` (config `server.name` + `server.url`) is what makes a *remote* MCP
  deployment addressable, so no MCP **server** bundle is deployed here — that was the blocker
  recorded in #29.
- **The MCP server is reached by Anthropic, not by this runtime.** `ClaudeHelper` sends each
  endpoint as an `mcp_servers` entry of type `url` (beta `mcp-client-2025-11-20`), so the API
  connects to the MCP server from its own side. `server.url` therefore has to be **publicly
  reachable over HTTPS** — a localhost URL is never dialled from here and fails at request time —
  and `RemoteMCPEndpoint` never probes it, so a wrong URL first surfaces as a failed run. For a
  local test, tunnel the metamodel runtime's servlet (`emf.osgi-mcp`, port 8099) and use the
  tunnel's address.
- **No MCP SDK is deployed, and none is indexed.** Until `emf.osgi-mcp#31` the `MCPEndpoint` API
  shared a bundle with the MCP *server* API, so addressing a remote server dragged in `mcp-core`,
  `reactor-core` and `reactive-streams` — three bundles that did nothing here except carry a
  permanently `UNSATISFIED` `McpJsonDefaults` component. The split moved `MCPEndpoint` +
  `RemoteMCPEndpoint` into `org.eclipse.fennec.mcp.endpoint`, whose `Import-Package` is
  `java.lang` and nothing else. Both bndruns dropped all three (91 bundles in `inference.bndrun`,
  down from 94) and `central.mvn` no longer declares them.
- **Credentials come from the environment**, never from a config file: `api.key` is
  `$[env:ANTHROPIC_API_KEY]`, interpolated at configuration delivery by
  `org.apache.felix.configadmin.plugin.interpolation` (already in the runtime, enabled through
  `felix.cm.config.plugins`). **An unset variable does not stop anything from starting**: the
  metatype's `required` is documentation, not enforcement, so `ClaudeChatCompletionService`
  activates and the first inference run fails at the provider. It surfaces as
  `IllegalStateException: Response object is not of expected type ClaudeResponse` — the client
  reports a non-2xx by failing to deserialize the body — which `ChatCompletionAdapter` rewrites
  to name `api.key` and `base.url`, and which lands as an `UNAVAILABLE` receipt that is not
  retried for `retryAfterUnavailableSeconds`. `download.file.folder` is required by the shared
  OCD even though only the batch service reads it — without it the component does not activate.
  `max.tokens` must be raised well past the component's own 1024 default, which would truncate a
  turn that authors a package.
- **Local credentials go in `…mapping.runtime/secrets.bndrun`, which is gitignored.** It sets
  `-runvm.secrets: -DANTHROPIC_API_KEY=…` (and the endpoints), `launch.bndrun` pulls it in with
  an optional `-include: -secrets.bndrun`, and every value in `config.json` is read as
  `$[env:NAME;default=$[prop:NAME;default=…]]` — exported environment variable first, then the
  system property the bndrun set, then the file's own default. `secrets.bndrun.template` is the
  committed copy to start from; the same pattern (and the same gitignore) is used in
  `eclipse-fennec/nsc`. A checkout without the file resolves, launches and exports exactly as
  before, which is why the include is optional.
- **`namespace` is the only thing `event.atlas.model.inference` tells the agent, and it is a
  prefix.** The prompt names no model family, no annotation source and no tool — a prototype found
  all of those by discovery, and naming them suppressed the discovery that found them. The agent
  extends the prefix with a segment identifying the model it authored and reports the resulting
  nsURI in its receipt; handing the configured value over as the whole nsURI made it one slot for
  one model, so a second device family collided with the first. Nothing here says what the segment
  should be, for the same reason nothing names the tools. Both allow-lists on the MCP server
  (`EMFPackageRegistry.nsuri.allowlist`, `ModelAtlasPublisher.publish.nsuri.allowlist`) are
  prefix-shaped (`…/inferred*`) and need no change. `codec.typeMapId` on
  `event.atlas.southbound.ingest` is an *ingest-side* setting and is deliberately not mirrored
  into the inference configuration.

- **`register.in.global.registry: true` needs an `nsuri.deny.list`, or it breaks the framework.**
  The mirror is an unconditional `EPackage.Registry.INSTANCE.put`
  (`RemoteEPackagePublisher.mirrorToGlobal`), and a scope inherits its parent `atlas` scope — whose
  listing carries the platform's own 17 system packages. An eager sweep therefore replaces
  *generated* EPackages with *dynamic* ones, `Ecore`, the codec and
  `event.atlas/mapping/1.0` included. Generated code then dies on its standard init:
  `ClassCastException: EFactoryImpl cannot be cast to ScopeApiFactory`. It is **order-dependent
  and so latent** — a factory's `<clinit>` runs once, so it only bites when something touches the
  class after the sweep, which is why adding the Atlas EObject provider is what finally exposed
  it. `inference.bndrun`'s config carries the 17-entry deny-list; the quickest check that it is
  live is to count the eager sweep in the Atlas log — **4 domain packages, not 21**. Sketched as
  `nsc/docs/issue-atlas-global-registry-clobber.md`.
- **Reading the twin in `inference.bndrun` means the Gogo shell, and it needs two things.** That
  runtime deploys no northbound REST, no SensorThings and sets `org.osgi.service.http.port=-1`, so
  the twin is write-only over HTTP. Add `org.eclipse.sensinact.gateway.northbound.gogo-shell` *and*
  `"sensinact.session.manager": {"auth.policy": "ALLOW_ALL"}` — both deployed runtimes set that
  policy in their `sensinact.json`, and without it every command answers
  `NotPermittedException: The user <ANONYMOUS> …` **except `providers`, which silently returns
  empty**, because an unreadable provider is filtered out of the listing rather than reported. An
  empty `providers` is therefore not evidence that a mapping failed to apply.

### The MCP tool allow-list is task-scoped, and why

Scoping happens at **both** ends, and neither end is optional.

*Server side*: `server.url` points at `/mcp/inference`, the metamodel runtime's task-scoped
servlet, whose `inference_tool_provider` serves **20 tools** — discovery, authoring, validation,
register, publish (verified against the live server 2026-09-04; it was 21 before
`export_dataset` became `export_package`). The general-purpose `/mcp/emf` servlet on the same
runtime serves all 38.
Each servlet carries its own `server.instructions`, and `/mcp/inference`'s are written for this
task, which matters because the prompt here deliberately names no tool.

*Client side*: `mcp.tools.enabled` in `configs/config.json` repeats those 20 names, and it is
**mandatory, not an optimisation**. `ClaudeHelper` always builds the toolset with
`default_config {enabled:false}` and re-enables only what the array names, and
`ClaudeChatCompletionConfig` declares `String[] mcp_tools_enabled()` with *no default* — so
omitting the key disables every tool on the server and hands the agent nothing to call.

Between them: nothing exposed manages or deletes datasets beyond authoring, replays a recipe, or
unregisters a package, and nothing can promote a draft to a released stage. A name that drifts
out of the server's provider stops being callable rather than silently widening the surface.

**Drift is silent in both directions, so diff the two lists rather than trusting them.** On
2026-09-04 the allow-list still named `export_dataset`, which the server had renamed to
`export_package` — a dead entry that enables nothing and reports no error (the server's own
`create_epackage` description still referred to the old name too). Ask the running server and
compare; `/mcp/inference` needs the bearer token from `secrets.bndrun`, and answers
`401 Missing or invalid bearer token` without it:

```bash
TOK=$(grep -oP '(?<=-DMETAMODEL_MCP_TOKEN=)[^\s,\\]+' …/secrets.bndrun | head -1)
# POST initialize -> read the Mcp-Session-Id response header -> POST tools/list with it
# (responses are SSE: strip the leading `id: …` line before parsing the JSON)
```

The reason is cost, and it was measured on the prototype (same server, same prompt): a server's
tool definitions are re-sent on **every** turn, and a run is ~100 turns.

| exposed tools | request prefix | server's share |
|---|---:|---:|
| 38 (everything) | 47,368 tokens | 19,340 |
| 21 (task-scoped) | 40,344 tokens | 12,316 |

That is 36 % off the server's footprint and 15 % off the whole prefix; the runs also converged
faster (131 → 111 → 104 turns, $2.93 → $2.18 → $2.01).

**Where the saving comes from — measured 2026-08-28, no longer a caveat.** `enabled:false`
does keep a tool's **definition** out of the request prefix, so per-request filtering delivers
the saving on its own. Two otherwise-identical requests against `/mcp/inference` (same prompt,
same server, one `list_registry` call):

| `configs` entries with `enabled:true` | `usage.input_tokens` |
|---|---:|
| 1 (the other 20 left to `default_config {enabled:false}`) | 915 |
| 21 | 14,021 |

13,106 tokens for those 20 definitions, i.e. they are simply absent when disabled — `defer_loading`
turned out not to be the flag that matters. This closes the three-request experiment #30 asked
for. Pointing `server.url` at the task-scoped `/mcp/inference` is still worth doing (the server
cannot serve what it does not expose, and that servlet's `server.instructions` are written for
this task), but the prefix saving no longer depends on it.

**Anthropic rejects an unknown field in a toolset outright**, with a 400 rather than by ignoring
it: `tools.0.mcp_toolset.default_config.deferLoading: Extra inputs are not permitted`. So every
camelCase EAttribute in `claude-chat-completion.ecore`'s MCP types carries an ExtendedMetaData
`"name"` annotation giving its snake_case wire name (`mcpServerName` → `mcp_server_name`,
`defaultConfig` → `default_config`, `deferLoading` → `defer_loading`) — all present and correct.
Worth knowing because a request malformed this way fails as the usual
`IllegalStateException: Response object is not of expected type ClaudeResponse` → `UNAVAILABLE`
receipt, which names `api.key` and `base.url` and so points at the wrong thing entirely.

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
- **Persistence rules** — `ChangeRule` subtypes (percentage / absolute / count / time-throttle)
  and `DeletionRule`, **contained** by the `ResourceMapping` or `ReferenceResourceBinding` that
  uses them. Since 2026-08-25 they are written inline, not shared by reference: a mapping
  carries its rules wherever it travels (a Model Atlas included), at the cost of editing a
  threshold in every copy. `PersistenceRuleRegistry` survives as a *catalogue* container —
  nothing reads it, and `model/examples/persistence-rules.xmi` says so; don't put it in a
  runtime's mappings directory. An old-style `href` to a rule in another document leaves an
  unresolved proxy whose parameters are all null; `ChangeRuleFilterImpl` detects that and pushes
  unfiltered with a warning rather than applying a zero threshold.
  A `ReferenceMapping` gives rules to its auto-generated resources through `bindings`
  (`ReferenceResourceBinding`: `attributes` — empty means all — plus `changeRule`,
  `deletionRule`, `unit`), resolved at registration time, nearest declaration winning over an
  inherited one. **The binding's rule must be `EcoreUtil.copy`-ed onto each generated resource**
  — containment allows one container, so assigning it would move it out of the binding and
  leave a single resource with a rule (guarded by `ChangeRuleFilterTest`).
  **`changeRule` elements need an `xsi:type`** — `ChangeRule` is abstract, so EMF has no class
  to instantiate without one, exactly like `featurePath`.
  **Change rules are enforced at ingest, deletion rules are still model-only.** The rules
  describe what the *history provider* should persist; until it can apply them itself,
  `ChangeRuleFilter` applies the change rules on the way into the twin — which also freezes the
  live value at the last accepted one (see below). The intended enforcement point, and why a
  transparent interceptor on SensiNact's fan-out typed-event bus cannot work, is in
  `docs/WP-SN-2-persistence-rules-plan.md`.

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
- **A mapping's `profile` reference is resolved through the profile registry.** `profile` is
  non-containment, i.e. a reference into another document, which a mapping delivered from a
  Model Atlas does not have (the atlas hands over standalone roots, and its fallback resolves
  *EPackages*, not instance documents). Since `MappingProfile.profileId` is an EMF ID, the
  proxy's URI fragment *is* the profile id, so `ProviderMappingRegistryImpl.validMapping` looks
  it up in `MappingProfileRegistry` and replaces the reference. A profile that cannot be found
  **skips the mapping** rather than registering it profile-less — the profile decides provider
  identity (`providerStrategy` `UNIFIED`), so carrying on would push data to the wrong provider.
  Storing a `MappingProfile` in a Model Atlas additionally needs its own atlas registry: the
  `sensinactmapping` registry pins `root.eclass.uri` to `ProviderMapping` (tracked in
  `eclipse-fennec/model.atlas`).
- **Resources generated from a `ReferenceMapping` are expanded before anything reads them.**
  `ProviderModelSensinactMapper.registerModelMapping` runs `generateReferencedResources` first,
  so profile validation and the twin model both see `temporaryResources`; generation clears
  before regenerating, and is no longer hidden inside `mapService` (which only runs while the
  twin provider does not exist yet).
- **A resource's unit has two sources**, and `MappingAnnotations.effectiveUnit` is the only
  place that decides between them: the `unit` field first, then the `sensinact.mapping`
  annotation — which is how a domain `.ecore` supplies units for generated resources, since
  their annotations are copied off the source attribute. Both the twin metadata and profile
  validation go through it; reading only the field made every annotation-supplied unit look
  like a profile mismatch.
- Content reaches those registries through registry *providers*: emf.osgi's
  `FileEObjectProvider` for local XMI directories, `AtlasEObjectProvider` for a Model Atlas, or
  programmatically via `registerModelMapping` (what the OSGi tests do). See the two
  `configs/config.json` files for both wirings side by side.
- **`InstancePusher`** (`pushInstance(EObject)` → number of mappings applied) is the ingress for
  southbound connectors: receive a payload, deserialize it to an EObject, push. Lookup keys on
  **EClass identity**, so incoming instances must come from the same `EPackage` instance the
  mappings resolved their `providerClasses` against.
- **`ChangeRuleFilter`** (`ChangeRuleFilterImpl`, config pid
  `sensinact.mapping.changerule.filter`, `enabled=true` by default) is consulted by
  `ValueMapperImpl.mapSingleResource` immediately before `resource.setValue`, and drops a value
  its `ResourceMapping`'s `ChangeRule` rejects. It is *stateful* — last accepted value, its
  timestamp and a notification counter per `mappingMid/providerId/serviceMid/resourceMid` —
  because a fresh `ValueMapperImpl` is created per push; comparisons are against the last
  *accepted* value, so a slow drift is not filtered away one step at a time, and the first value
  of a resource is always accepted. Time throttling measures the *payload's* timestamp, not
  arrival time. An inapplicable rule (numeric rule on a non-numeric resource) accepts and warns
  once rather than dropping. Wired optionally and dynamically into `InstancePusherImpl` (which
  passes it to `ValueMapperFactory.createValueMapper`) and into `ProviderMappingRegistryImpl`,
  which calls `reset(mid)` when a mapping is updated or removed so an edited rule starts from a
  clean baseline. Nothing in it touches sensinact types — the bundle must still resolve without
  the gateway. `enabled=false` restores unfiltered pushing.
- `ValueMapper` / `ValueMapperFactory` — the plain-Java engine face: `mapInstance` (update the
  twin), `mapResourceValues` (extract only), `validateInstance`. `ValueMapperImpl` (~1100 lines,
  package `…mapping.impl`) does path navigation, collection handling, type conversion, timestamp
  strategies, admin/location mapping.
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
- SensiNact itself (`org.eclipse.sensinact.gateway.*`) comes in through the dedicated
  `cnf/ext/sensinact.bnd` repo (index `sensinact.maven`, Eclipse sensinact snapshots);
  `central.mvn` additionally carries the Model Atlas client bundles
  (`org.eclipse.fennec.model.atlas:…rest.client.* / scope.api / eobject.provider`) and every
  third-party bundle the northbound chain drags in: Jackson 3 (`tools.jackson.core:jackson-core`
  + `jackson-databind`, `jackson-jakarta-rs-*`, `jackson-module-jakarta-xmlbind-annotations`, all
  on one 3.2.x version — 3.2 is a floor, the Fennec codec imports `tools.jackson.*` as
  `[3.2,4)`), Jackson 2 `jackson-core` for esri.geometry, Jackson 2 `jackson-annotations`
  **2.22** (Jackson 3 left `com.fasterxml.jackson.annotation` at 2.x and it is imported as
  `[2.21,3)` by the sensinact DTO bundles and `[2.22,3)` by the codec, so it does not track
  jackson-core's version), netty, and dropwizard `metrics-core`.
  `…model.atlas.eobject.provider` — the generic Model Atlas
  content source for the emf.osgi EObject registry — used to be a workspace project here; since
  2026-08-19 it lives in `eclipse-fennec/model.atlas` and both bndruns list it with a version
  range (`[0.1.0,0.1.1)`), *not* `version=snapshot`.
- **A new bundle is a new top-level directory with a `bnd.bnd`** — the bnd workspace plugin
  sweeps it into the Gradle graph automatically; the root build applies `java` + `jacoco` to
  every subproject.
- **A `-runbundles` entry only survives CI if some index can actually *fetch* it.** Two ways to
  get this wrong, both of which resolve fine locally (bnd uses `~/.m2` as a cache, and a
  developer machine has half of Maven Central in there) and then fail the CI export with
  "Not found in […]":
  1. the bsn is in **no index at all** — it resolved purely out of `~/.m2` (this is how the
     Jackson 3 core/databind and `jackson-annotations` entries slipped in);
  2. the coordinate is indexed in **`cnf/ext/sensinact.maven`, which pins third-party artifacts
     the Eclipse SensiNact repos do not host** (netty 4.1.9x, `metrics-core`, postgresql,
     tx-control) — `repo.eclipse.org/…/sensinact-{releases,snapshots}` 404s them.

  Either way the fix is the same: declare it in `central.mvn`, at the version the rest of that
  library family already uses. After changing a bndrun's `-runbundles`, cross-check every entry
  against the indexes rather than trusting a green local resolve.
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

- `build.yml` — `reusable-verify.yml` on PRs and every branch except `main`/`snapshot`; its
  `extra-gradle-tasks` re-resolve `launch.bndrun` and export the docker runtime jar, so a bndrun
  that no longer resolves fails the PR (with `gradle-parallel: false` — bnd resolve/export tasks
  are not parallel-safe).
- `snapshot.yml` / `release.yml` — `verify → release (do-release: false|true) → container + docs`,
  gated by `needs:`. The release job exports `eventatlas.runtime_docker.jar` **in the same build
  that publishes to Maven** and uploads it (plus the mapping bundle jar, for the version tag) as
  the `release-jars` artifact.
- `reusable-container.yml` (repo-local) — does *not* rebuild the jar; it downloads `release-jars`,
  reads `Bundle-Version` out of the mapping jar with the bnd CLI, stages `docker/eventatlas/content/`
  and pushes `docker.io/eclipsefennec/event.atlas` and `ghcr.io/eclipse-fennec/event.atlas`
  (amd64 + arm64/v8), tagged `snapshot`/`latest` plus that version. Needs the
  `DOCKER_USERNAME`/`DOCKER_API_TOKEN` secrets and `packages: write` (GHCR).
- `docs.yml` — `workflow_dispatch` only; plus `scorecard.yml` and `dependency-review.yml`.

Branch protection and secret scanning live in the EF-managed `eclipse-fennec/.eclipsefdn`
otterdog repo, not here.
