# The event.atlas deployment model

An event.atlas runtime is configured through OSGi ConfigAdmin: a dozen PIDs spread over four
configurator JSON files, each value an `$[env:…]` placeholder, and roughly forty environment
variables to remember. That works, and for a single container it is hard to beat. It does not
scale to *describing* a deployment — you cannot diff two of them, validate one before it starts,
store one in a Model Atlas, or tell from the outside which knobs a given runtime actually uses.

The deployment model is the declarative alternative. One XMI instance of
`EventAtlasDeployment` describes the runtime — HTTP endpoint, Model Atlas binding, southbound
brokers and ingest channels, history storage with its filters and housekeeping policies, optional
model inference — and the `DeploymentConfigurator` turns it into exactly those ConfigAdmin
configurations.

- **Metamodel**: `org.eclipse.fennec.event.atlas.deployment/model/event-atlas-deployment.ecore`,
  nsURI `https://fennec.eclipse.org/event.atlas/deployment/1.0`, generated into `src-gen` as
  `org.eclipse.fennec.event.atlas.model.deployment`.
- **Examples**: `…deployment/model/examples/` — `deployment-docker.xmi` (a complete deployment)
  and `deployment-history-tuning.xmi` (the smallest useful step).

## It is additive, and that is the whole design

Nothing is forced. A runtime with no deployment model behaves exactly as before, and a runtime
*with* one keeps every setting the model does not mention.

Two rules make that safe:

**An absent section emits nothing.** The model has no notion of "unset means default" that would
overwrite a JSON value with a model default. If the XMI carries no `<http>` element, no HTTP PID is
written at all, and the whiteboard keeps coming from `configs/sensinact.json`. So a deployment can
be migrated one concern at a time.

**A PID has exactly one writer.** Every configuration the configurator writes is stamped with
`event.atlas.deployment.owner = <deploymentId>`. Before writing, it looks:

| what it finds | what it does |
|---|---|
| no configuration | creates it, stamped |
| a configuration it stamped for this deployment | updates it |
| a configuration stamped for a *different* deployment | leaves it alone, warns |
| a configuration with no stamp | leaves it alone, warns |

The last row is the important one. A block still present in a configurator JSON bundle *wins*, and
you get a log line naming the PID:

```
Deployment 'eventatlas-docker': sensinact.history.timescale already exists and was not written
from a deployment model - left untouched. Remove it from the configurator JSON to let the model
own it.
```

That is not a failure — it is the model telling you which JSON block to delete next. It also means
applying `deployment-docker.xmi` to the shipped image changes nothing until you start removing
blocks, which makes the migration reversible at every step.

A model that shrinks cleans up after itself: PIDs a previous version of the same deployment wrote
and this one no longer asks for are deleted. Removing the model from the registry deletes
everything it wrote. Deactivating the *bundle* deletes nothing — ConfigAdmin is persistent by
design, and a bundle refresh must not tear down a running runtime.

## How the model reaches the runtime

Exactly the way mappings and profiles do: as content of a named EObject registry,
`event-atlas-deployment`, keyed by `deploymentId`. Both config bundles already declare the registry
and a `FileEObjectProvider` for it, so there is nothing to wire:

- **docker image** — the provider reads `/opt/eventatlas/runtime/deployment`. Mount a directory
  there, drop XMIs in, done.
- **local playground** — `…local.config/configs/config.json` declares the provider with no
  `locations`; add one pointing at a directory of deployment XMIs.
- **Model Atlas** — because it is registry content, an `AtlasEObjectProvider` can feed it instead,
  which puts deployments under the same versioning and drift handling as mappings.

The registry replays its content when the configurator binds, so the model may arrive before or
after the component starts. Registry callbacks run under the registry's lock, so the configurator
hands the work to a single-threaded executor: ConfigAdmin updates reactivate components and must
not run under that lock.

## Migrating: start with history tuning

`deployment-history-tuning.xmi` is the recommended first step because it collides with nothing. The
history rework introduced two factory PIDs — `sensinact.history.filter` and
`sensinact.history.housekeeping` — that **no configurator JSON file in this repository writes**.
Declaring a `<history>` section *without* a `<storage>` child claims only those:

```xml
<history providerName="brokerHistory">
  <filters name="temperature-deadband" changeMode="DEADBAND" changeThreshold="0.5"
      changeMaxInterval="PT30M">
    <targets>brokerHistory</targets>
    <includeResources>{"resource": {"value": "temperature", "type": "EXACT"}}</includeResources>
  </filters>
  <housekeeping name="ninety-days" retentionPeriod="P90D" maxDelete="50000"/>
</history>
```

`storage` is optional for exactly this reason. Drop that file into the deployment directory of a
running runtime and the filters and policies appear; the store itself stays where it is.

From there, take over one section at a time: add `<storage>` to the model *and* delete
`configs/timescale.json` in the same change, add `<http>` and delete the whiteboard blocks from
`configs/sensinact.json`, and so on.

## What each section writes

| model section | ConfigAdmin PIDs |
|---|---|
| `http` | `org.apache.felix.http~eventHttp`, `JakartarsServletWhiteboardRuntimeComponent~<whiteboardName>`, `sensinact.northbound.rest` |
| `atlas` | `org.eclipse.fennec.model.atlas.rest.client~atlas`, `AtlasEObjectProvider~atlas` |
| `brokers` | `sensinact.southbound.mqtt~<id>`, one per broker |
| `channels` (MQTT) | `event.atlas.southbound.mqtt~<name>`, one per channel |
| `channels` (REST) | `event.atlas.southbound.rest`, once — one application serves every REST channel |
| `channels` (any `codecTypeMapId`) | `event.atlas.southbound.ingest` |
| `history.storage` | `sensinact.history.timescale` or `sensinact.history.inmemory`, plus `sensinact.sensorthings.northbound.rest` |
| `history.filters` | `sensinact.history.filter~<name>` |
| `history.housekeeping` | `sensinact.history.housekeeping~<name>` |
| `inference` | `event.atlas.southbound.sampling`, `event.atlas.model.inference` |

Three of those are worth spelling out.

**`history.providerName` is written twice on purpose.** It is the name the store registers under
*and* the name the SensorThings northbound asks for (`history.provider`). Those two have always had
to agree, and getting them out of step yields Observations with a single row and no error. In the
model it is one attribute, so they cannot disagree.

**The mapping metamodel's nsURI is added to `required.nsuris` automatically.** That gate postpones
the Atlas sync until the listed EPackages are registered, and omitting
`https://fennec.eclipse.org/event.atlas/mapping/1.0` postpones it forever — silently. List only your
domain models; the metamodel is added for you.

**A REST channel writes no per-channel configuration.** The channel is a path segment of
`POST <whiteboard base>/ingest/{channel}` and one Jakarta-RS application serves them all. What the
model does write is that application's whiteboard target — without an application of its own it
would join the whiteboard's default application, which the SensorThings application shadows, and
never be invoked. Declaring a REST channel therefore needs an `<http>` section; without one it is
refused with a logged problem.

## Refusals

The configurator collects problems rather than throwing: a section that is wrong does not stop the
others. Each refusal is logged, and the configuration behind it is not written.

| refused | why |
|---|---|
| `atlas` without `baseUri` or `scope` | neither has a sensible default; a wrong scope yields no EPackages and no error |
| an MQTT channel with no `topics` | the adapter refuses to activate rather than consume a whole shared broker, so writing it would only produce a failed component |
| a JSON channel with no `codecTypeMapId` anywhere | JSON payloads do not name their model; untyped, every one of them is dropped by the deserializer |
| a REST channel with no `http` section | there is no whiteboard to mount the ingest application on |
| `changeMode="DEADBAND"` with no threshold | a deadband of zero is not a deadband |
| a housekeeping policy with neither `retentionPeriod` nor `keepCount` | it would delete nothing, or everything |

## Two details that bite

**`keepCount` and `maxDelete` of `0` mean "unset", and are omitted rather than written.** The
history engine uses `-1` as its unset sentinel for both. A literal `0` would therefore not mean
"no limit" but "keep no values" and "delete no rows" — the first of which deletes the whole table
on the next run. The planner omits any value that is not greater than zero, and a test pins that.

**Durations are ISO-8601 and typed.** `changeMaxInterval`, `retentionPeriod` and `schedulePeriod`
are `java.time.Duration`, written as `PT30M`, `P90D`, `PT24H`. The `EDuration` datatype carries
`create`/`convert` bodies in the `.ecore`'s GenModel annotation, because EMF's default reflective
conversion cannot build a `Duration` from a literal — without them every deployment XMI carrying a
duration fails to load with `The value 'P30D' is invalid`. If you add another `java.time` datatype
to this metamodel, give it the same treatment.

## Changing the metamodel

`src-gen` is generated by bnd `-generate` from the `.genmodel` at build time — never hand-edit it.
One trap: **the generate task's up-to-date check does not notice an edit to the `.ecore` alone.**
An ecore-only change appears to regenerate and silently does not. Force it:

```bash
./gradlew :org.eclipse.fennec.event.atlas.deployment:generate --rerun-tasks
```

The `.genmodel` is mechanically derived from the `.ecore` (every classifier and feature listed), so
after adding a classifier, add the matching `genClasses` / `genFeatures` entry too.

## Testing

`DeploymentPlannerTest` covers the translation — every section, every refusal, and the two traps
above — against `DeploymentPlanner`, which is pure and needs no framework.
`ExampleDeploymentTest` loads the shipped example XMIs the way a runtime would and plans them,
so an example that stops parsing fails the build rather than misleading a reader.
