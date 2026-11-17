# The UDP mappings — progress

The Jena urban-data-platform adapters `de.jena.udp.atlas.ilsa` and
`de.jena.udp.atlas.traficam`, rewritten as SensiNact mapping XMIs. They are the first use case
for the issue #91 features, and the exercise is run step by step: write the mapping as far as
the engine goes, record what cannot be expressed, implement that, write the mapping again.

The analysis behind each line is in `docs/udp-adapter-mapping-gaps.md`.

## Where the four flows stand, 2026-11-06

| flow | mapping | state |
|---|---|---|
| ILSA signal state | `ilsa-signal-state-mapping.xmi` | **complete**, except the provider id |
| ILSA configuration | `ilsa-configuration-mapping.xmi` | **complete** |
| TrafiCam configuration | `traficam-config-mapping.xmi` | **complete** |
| TrafiCam detections | `traficam-detections-mapping.xmi` | **complete**, except one value function |

All four flows are mappings now. The detections moved last, and they moved because the
payload changed: `TrafiCamDetections` and `ObservedClass` replace the `TrafiCamDto`, so what
the adapter hands over is an EObject with the window's objects in it. The window itself, the
class name and the binary read stay in the adapter, and none of them is an engine gap - the
first is state between payloads, the second a lookup across messages, the third ingress.

Each pair - the two ILSA mappings, the two TrafiCam ones - reaches **one** provider through a
`MappingProfile` with `providerStrategy=UNIFIED`: `ilsa-profile.xmi` and `traficam-profile.xmi`.
Without one, a mapping's model comes from its own `fqMid` and the pair would build two models
for the one provider name its payloads give.

Deploy a profile into the `sensinact-profiles` registry beside the mappings. A mapping whose
profile cannot be resolved is **skipped entirely** - the profile decides provider identity, so
registering it profile-less would push the data to the wrong provider. Each mapping references
its profile by `profileId` (`href="ilsa-profile.xmi#ilsa"`), which is the fragment the registry
looks up when the document itself is not reachable - the normal case for a mapping delivered
from a Model Atlas.

A profile is validated against **each** mapping on its own, and a failed requirement throws at
registration. The two mappings of a pair split the provider between them, so anything only one
of them carries is optional in the profile; what both carry can be required. That is the limit
of a profile as a contract for a split provider, and it is written into both files.

## What has been implemented for them so far

| #91 item | what it gave the mappings |
|---|---|
| 1, converters | ILSA `observedArea`, replacing the QVTo black box |
| 2, OCL | the `'/' -> '_'` service-id rewrite; `'ILSA ' + intersectionName`; `bbox.at(n)` |
| 3, functions | the TrafiCam viewport, a `GeometryCollection` from a whole `Scene` |
| 5, payload-named services | one service for each ILSA signal, from one block instead of nine; and the signal-state service, created on the model when the first state arrives |
| 7, admin feature mappings | both friendly names, and the ILSA location inside a `Double[]` |
| 6, payload metadata | ILSA's `type` and `signalGroup` on the color resource |
| 8, typed services | ILSA's `Signal`, TrafiCam's `ObservedObjects` - the services the adapters build, with the resources, types and metadata their EClasses declare |
| 8, `extendAdmin` | TrafiCam's admin `viewport` |
| 5 + 8 together | one service per TrafiCam object class, each typed `ObservedObjects` |

## What is left

| what | where | kind |
|---|---|---|
| the `udp.traficam.detections` function | beside `ViewportValueFunction`; builds the FeatureCollection the `TrafiCamDto` used to carry | Jena's code, no engine gap |
| the id from the MQTT topic | `TLSignalState.intersectionId` only - `CamConfig.camId` is in place | a transient attribute, Jena's change **or** an envelope-rooted mapping, see below |
| binary EMF on ingress | TrafiCam only | southbound, not the engine |
| a `Metadata` annotation on `ObservedObjects.objects` | `traficam.ecore`, one line | Jena's change, see the mapping's note |
| the ILSA thermal flow | the topic carries a bare number, not an EObject | the adapter wraps it; then `serviceEClass=Thermal` expresses it |

### The topic is readable now, and that changes the id question

Since 2026-11-17 (#96/#97) every southbound message arrives in a CloudEvents envelope carrying
the topic it was published to, and a mapping that declares `ce::CloudEvent` as its root reads it
with one feature path - see *Mapping the envelope itself* in the
[user guide](../../../docs/sensinact-mapping-user-guide.md). So the engine no longer forces a
transient id attribute onto the Jena models.

**Both versions of all four flows are here now**, so each can be tried against the real
adapters:

| flow | payload-rooted | envelope-rooted |
|---|---|---|
| ILSA signal state | `ilsa-signal-state-mapping.xmi` | `ilsa-signal-state-ce-mapping.xmi` |
| ILSA configuration | `ilsa-configuration-mapping.xmi` | `ilsa-configuration-ce-mapping.xmi` |
| TrafiCam configuration | `traficam-config-mapping.xmi` | `traficam-config-ce-mapping.xmi` |
| TrafiCam detections | `traficam-detections-mapping.xmi` | `traficam-detections-ce-mapping.xmi` |

**Deploy one set or the other, never both.** Each pair shares its `UNIFIED` profile, so a twin
and its original answer the same provider id and build the same model. Deployed together both
match every message - one through the payload lookup, one through the envelope lookup - and both
write the same resources, with registration order deciding which value survives. Each twin's
header carries the two-line recipe for running them side by side instead.

The conversion turned out to be mechanical, and two things the plan feared are not true:

- **A feature path crosses `data`.** It is typed `EObject` and declares no payload features, but
  `data` is itself a feature of `CloudEvent` - so a path names it as its first step and navigates
  on, including a `referencedResource` path. One block still makes one service per element.
- **An expression needs no `oclAsType`.** m2x hands a statically unresolvable property to the
  evaluator, which looks it up on the runtime type, so `self.data.intersectionName` works.

So the real edits per mapping are: `providerClasses` → `ce::CloudEvent`, a selector, the provider
name from the topic, a `CloudEvent/data` first step on every path **rooted at the message**, and
a `data.` segment on every expression rooted there. Paths and expressions below a
`referencedResource` are unchanged, because their source is the element, not the root.

Three things still to check before deploying a twin:

- **The topic segment index.** `source` is flat, so the id is `self.source.tokenize('/')->at(n)`
  and `n` differs per flow - the TrafiCam camera is the *fourth* segment of
  `5g/config/traficam/<cam>` and the *third* of `5g/traficam/<cam>/<class>`. Each file states
  which it uses and why.
- **TrafiCam has no envelope yet.** It reads binary EMF and calls `InstancePusher` directly;
  `pushInstance(EObject)` builds no envelope, so those two twins apply to nothing until the
  adapter builds a `CloudEvent` and calls `pushInstance(CloudEvent)`. That adapter change is what
  the twin costs instead of the model change.
- **The attribute names.** The envelope's id is `eventId`, so a carried-over `self.id` stops
  parsing - and `type`, `subject`, `time` and `source` all parse against the *envelope*, which
  are plausible payload attribute names too.

Keeping `TLSignalState.intersectionId` stays a valid answer; it is what the payload-rooted set is
written for. `UdpMappingWellFormedTest` reads every file in this folder, so a typo in either set
fails the build even though nothing here executes them.

### Two things that changed with item 8

- **A typed service takes its metadata from its EClass.** `name`, `unit` and `extraMetadata`
  are written into the *model* resource at registration, and a typed service has no model
  resource. The Jena models carry the `Metadata` annotations already - `Signal.color`'s
  `sensorthings.unit.name`, `Signal.observedArea`'s and `TraficamAdmin.viewport`'s
  `sensorthings.observedArea` - so the mappings stopped restating them. `ObservedObjects.objects`
  is the one that carries none; the mapping says what to add.
- **The admin `viewport` is readable, not visible.** `extendAdmin` puts it on the admin
  instance, where a direct resource read and the history provider see it, and the provider
  description, filters and SensorThings do not. TrafiCam's `observedArea` service carries the
  same geometry for them.

## Two things to know before running any of this

- **The ILSA payload model is not in a source tree.** `TLConfiguration` and `TLSignalState` are
  defined only inside the published bundle
  `de.jena.udp.trafficos.trafficlight.model:1.2.0-SNAPSHOT`
  (`model/tos-trafficlight.ecore`). The checkout in `jena-upd-models` is from 2023 and has
  neither `subcircles` nor `TLSignal.observedArea`, on every branch. The TrafiCam models are
  checked out in `urban-data-platform` and do match their adapter.
- **The ILSA location needs one configuration property.** `bbox` is a `Double[]` inside one
  attribute, which OCL reaches but cannot index until the model bundle contributes an `at`
  operation; the engine then needs `"DefaultOclEngine": {"ocl.customOperationsEnabled": true}`.
  Every published provider binds, so several model bundles can contribute at once.
