# Replacing the UDP adapters with a mapping

What the Jena urban-data-platform adapters do, and what the mapping engine still needs to
replace them. Written 2026-11-03, from
`backend/de.jena.udp.atlas.ilsa` and `backend/de.jena.udp.atlas.traficam`.

This is the first use case for the issue #91 features. It is not the only final goal.

## The boundary

The engine takes an EObject and changes the twin. What happens before the EObject exists stays
with the southbound adapter.

| stays in the adapter | moves into a mapping |
|---|---|
| the MQTT subscription | `TrafficLightDto`, `TrafiCamDto` |
| the codec configuration (`codec.rootType`, `codec.typeKey`, `codec.dateFormat`) | `ilsa.qvto` |
| the TrafiCam 2-second window, and the class name from an earlier message | the `GenericDto` for the thermal value |
| | `updateConfig`, which builds the TrafiCam provider |

The TrafiCam window holds state between messages. The engine sees one payload and holds no
state. Aggregation is not a mapping task.

## Solved, and not a gap

**The provider id comes from the MQTT topic.** The adapter writes the id into a new `transient`
attribute of the payload model (`CamConfig.camId`, and the same for the ILSA intersection), and
the mapping reads it with a plain `featurePath`. The models belong to Jena, so they can change.

Add the attribute **last** in the EClass. TrafiCam uses binary EMF, and `BinaryResourceImpl`
identifies a feature by its position.

A `source` parameter on `InstancePusher.pushInstance` stays the answer for a third-party model
that cannot change. Nothing needs it now.

## The one capability behind the two large gaps

Two gaps need the same thing: **the engine must extend the twin model at push time**.

1. A service whose name the payload decides. ILSA names a service after `TLSignalState.id`.
2. One service for each element of a collection. The ILSA configuration makes one service for
   each `TLSignal`, from a collection three levels deep.

They are two ways to write one capability in the model, and not two capabilities.

**The gateway permits it.** `LateServiceOnModelTest` proves that a provider which exists already
shows a service that `model.createService(name)` adds later, and that `setValue` on it works.

What the engine must change:

- `InstancePusherImpl` receives the model manager in its command, and gives the twin only to
  `ValueMapperFactory`. The model manager must reach the value mapper.
- `ProviderMappingRegistryImpl` uses `SensinactEMFDigitalTwin` and `SensinactEMFModelManager`
  already, so the pusher can use the same pattern.
- `ProviderModelSensinactMapper.mapProvider` stops when the provider exists
  (`if (isNull(provider))`), so it never extends a model today.
- `mapService` and `mapResource` read `getMid()`, and the push path only **finds** a service and
  a resource. It must be able to make one.

A resource keeps its type, its unit and its metadata in the model. The payload gives the name
only. This is sufficient for ILSA, because every signal has the same shape.

## Smaller gap: metadata from the payload

ILSA writes two metadata entries on the `color` resource, and the payload gives both values:

```java
metadata.getExtra().put("type", metadataValue(signal.getSignalType().getName()));
metadata.getExtra().put("signalGroup", metadataValue(signal.getSignalGroup().getId()));
```

A mapping can write a constant only, because `extraMetadata` is a `StringToStringMap`:

```xml
<resources mid="color">
  <extraMetadata key="type" value="VEHICLE"/>   <!-- wrong: the payload decides this -->
</resources>
```

`MetadataMapping` is the empty EClass that waits for this. Make it extend `FeatureMapping` and
give it a `key`:

```xml
<resources mid="color">
  <metadata key="type">
    <featurePath xsi:type="ecore:EReference" href="...#//TLSignalState/signalType"/>
    <featurePath xsi:type="ecore:EAttribute" href="...#//SignalType/name"/>
  </metadata>
  <metadata key="signalGroup">
    <featurePath xsi:type="ecore:EReference" href="...#//TLSignalState/signalGroup"/>
    <featurePath xsi:type="ecore:EAttribute" href="...#//TLSignalGroup/id"/>
  </metadata>
</resources>
```

`MetadataMapping` then inherits `featurePath`, `collectionFilter`, `converterId` and
`expression` at no cost. `extraMetadata` stays for a constant.

## Smaller gap: a collection index on the admin references

ILSA reads two elements of one array. `Point` takes the longitude first:

```java
return new Point(geoJson.getBbox()[0], geoJson.getBbox()[1]);
```

A mapping must write this today:

```xml
<services xsi:type="sensinactMapping:AdminMapping" mid="admin">
  <longitudeRef xsi:type="ecore:EAttribute" href="...#//GeoJSON/bbox"/>
  <latitudeRef  xsi:type="ecore:EAttribute" href="...#//GeoJSON/bbox"/>
</services>
```

Both paths end on the same multi-valued attribute, and `getRawValue(List)` always takes element
0. So both read `bbox[0]`, and the latitude is wrong. There is no place for an index, because
`latitudeRef` is a list of `EStructuralFeature`, and not a `FeatureMapping`.

The fix follows `ResourceMapping.valueMapping`. Add optional containments beside the old
references, so no mapping that exists needs a change:

```xml
<services xsi:type="sensinactMapping:AdminMapping" mid="admin">
  <longitudeMapping collectionIndex="0">
    <featurePath xsi:type="ecore:EAttribute" href="...#//GeoJSON/bbox"/>
  </longitudeMapping>
  <latitudeMapping collectionIndex="1">
    <featurePath xsi:type="ecore:EAttribute" href="...#//GeoJSON/bbox"/>
  </latitudeMapping>
</services>
```

The same applies to `friendlyNameFeature` and `elevationRef`. `ResourceMapping.valueFeature` has
the same gap, and `valueMapping` already solves it there.

## Smaller gap: a payload that is no EObject

The ILSA thermal topic carries a bare number, and the adapter makes a `GenericDto` from it. The
engine needs an EObject. The adapter can wrap the number in one, so this is small.

## Extra attributes on the admin service

TrafiCam puts a `viewport` on the admin service. It does this with a **subclass**:
`TraficamAdmin extends Admin` in its own provider model.

A mapping cannot do this, and two findings say why.

1. **The engine refuses an unknown admin resource.** `ProviderMappingRegistryImpl` compares each
   mid under `<admin>` with the attributes of sensiNact's `Admin` class, and it **skips the whole
   mapping** when one does not match.
2. **The model cannot grow an admin resource either.** `createResource` on the admin service of a
   model gives a `StackOverflowError`: `AdminImpl.eGet` calls `ServiceImpl.eGet` without end,
   because `Admin` is a *generated* EClass and the new feature is dynamic. The error is on the
   gateway thread, so the gateway stops. Measured 2026-11-03, and the probe took 12 other OSGi
   tests with it. The probe is not in the test suite for that reason.

So the capability that works for a service does **not** work for the admin service. The subclass
is the only route that is proven.

`AdminMapping.providerPackage` is in the metamodel for this, and it is dead: one commented-out
line in `mapProvider`, and two assertions in a test. A mapping would name a package whose `Admin`
subclass carries the extra attributes.

**A dynamic subclass works at the EMF level.** `DynamicAdminSubclassTest` makes a dynamic
EClass with `Admin` as its super type, adds a `viewport` attribute, and reads it back:

```
impl=AdminImpl viewport=a polygon friendlyName=null
```

There is no recursion. EMF gives the generated `AdminImpl` for the dynamic subclass, and the
dynamic attribute still works. So the StackOverflowError does **not** come from EMF handling a
dynamic feature on `Admin`. It comes from the way the model machinery adds a resource to a live
admin service. A subclass that exists before the provider is a different operation.

This is an EMF test with no gateway. It does not show that the twin accepts such an admin, or
that the attribute appears as a resource of the admin service. That step needs the gateway, and
a wrong answer there stops it, so it needs its own framework.

## One question to decide

Profile validation runs at registration. A service that a payload names later does not exist
then. A profile with `required` services cannot check such a mapping. We must decide whether a
dynamic service is outside the profile check, or whether a profile can describe one.

## Order of work

1. The model grows at push time, with a service name from the payload.
2. One service for each element of a collection.
3. Metadata from the payload.
4. The collection index on the admin references and on `valueFeature`.

## ILSA step 1 — the first two mappings, 2026-11-04

Written against the published model (`de.jena.udp.trafficos.trafficlight.model` 1.2.0-SNAPSHOT,
nsURI `.../trafficlight/1.1.1`), not the sources in `jena-upd-models`, which are older and carry
neither `subcircles` nor `TLSignal.observedArea`.

- `org.eclipse.fennec.event.atlas.mapping.tests/data/udp/ilsa-signal-state-mapping.xmi`
- `org.eclipse.fennec.event.atlas.mapping.tests/data/udp/ilsa-configuration-mapping.xmi`

### What the engine can do now

- **One service per signal, from one block.** The configuration reaches its signals through
  `subcircles -> serialPorts -> modules -> signals`, three nested collections. A produced service
  name expands all of them; `ForEachServiceTest.nestedCollections_areFlattened` pins the
  flattening.
- **The service name is the signal id with `/` replaced**, which the adapter does in Java
  (`signalState.getId().replace("/", "_")`). OCL has `replaceAll`, so
  `<name expression="self.id.replaceAll('/', '_')"/>` is the whole of it.
- **The service is created when the first payload names it.** A signal state arrives before any
  service for that signal exists, and the model grows at push time.
- **`observedArea` is a converter, not a gap.** The QVTo black box
  `convertToFeatureCollection()` becomes a `ValueConverter` published by the UDP side under an
  id the mapping names. The whiteboard is in place; only the converter has to be written.
- **The signal-state mapping is complete except for the provider id** - see below.

### What stays in the adapter

| what | why | lands with |
|---|---|---|
| the MQTT subscription, the topic patterns, the codec options (`codec.rootType`, `codec.typeKey`, `codec.dateFormat`) | before the EObject exists, so outside the engine | never - this is the boundary |
| the intersection id for a **signal state** | it is in the topic, not in the payload | a `transient intersectionId` on `TLSignalState`, added **last** in the EClass |
| `type` and `signalGroup` metadata on `color` | `extraMetadata` holds constants; both values come from the signal | item 6, `MetadataMapping extends FeatureMapping` |
| `friendlyName := 'ILSA ' + intersectionName` | `friendlyNameFeature` is a bare feature list - it can name a feature, not prefix it | item 7 |
| `Point(geoJson.bbox[0], geoJson.bbox[1])` | see the correction below | item 7 |
| the `thermal` topic | a bare number, and no EObject | the adapter wraps it, or it keeps its `GenericDto` |

### Correction to issue #91 item 7

The item says "a collection index on the admin references". **`bbox` is not a collection.** It is
a single-valued `EAttribute` of datatype `Array1D`, whose `instanceClassName` is
`java.lang.Double[]`, so `collectionIndex` cannot reach it either - `navigateFeaturePath` tests
for a `java.util.List` and a Java array is not one.

What the admin actually needs is for `longitudeRef`, `latitudeRef`, `friendlyNameFeature` and
`elevationRef` to become **FeatureMappings**, so that a converter or an expression can take the
element - which also answers the `'ILSA ' + name` case with the same change. The index is one way
to use that slot, not the requirement. `ResourceMapping.valueFeature` has the same shape of gap
and `valueMapping` already solved it there, so the fix is a known one.

### One more thing ILSA needs that was filed under TrafiCam

`Ilsa.thermal` is typed `Thermal`, and **`Thermal extends Admin`** in
`de.jena.ilsa.sensinact.model`. So the admin-subclass route of item 8 is not TrafiCam-only: ILSA
carries an extra admin-shaped service too. The same item also covers the fact that a mapping
builds a *dynamic* model named by its `mid` rather than using the hand-written `Ilsa` EPackage -
which is why the `Metadata` annotations of `Signal.color` and `Signal.observedArea` are restated
as `extraMetadata` in the mapping above. `AdminMapping.providerPackage` is the dead slot that
would let a mapping name the package instead.

## TrafiCam step 1 — 2026-11-04

Written against `backend/de.jena.traficam.model` and `backend/de.jena.traficam.sensinact.model`,
both checked out in `urban-data-platform` and both matching the adapter code - unlike ILSA, where
only the published jar does.

- `org.eclipse.fennec.event.atlas.mapping.tests/data/udp/traficam-config-mapping.xmi`

TrafiCam has **three flows** and they fall on different sides of the boundary.

### 1. The configuration — a mapping, with two gaps

| part of `updateConfig` | state |
|---|---|
| `provider.setId(camId)` | gap 1: the camera is in the topic. `CamConfig.camId`, transient, **last** in the EClass |
| `admin.setLocation(point(config.getLocation()))` | **works today** - see below |
| `admin.setFriendlyName("Camera " + camId)` | item 7: `friendlyNameFeature` names a feature, it cannot prefix one |
| `admin.setViewport(viewport(scene))` | item 8: `viewport` belongs to `TraficamAdmin extends Admin` |
| `services.put("observedArea", …)` with `objects := viewport` | **works today**, as a `ValueFunction` over `CamConfig.scene` |

**The location works here and not in ILSA, and the difference is the model.** TrafiCam's is
`CamConfig.location -> GpsCoordinates.latitude / .longitude`, two plain single-valued
attributes, which the admin's `latitudeRef`/`longitudeRef` paths already read. ILSA's is
`geoJson.bbox[0]` and `[1]`, two positions inside **one** attribute of datatype `Array1D`
(`java.lang.Double[]`). So item 7 is not needed for every admin location - only where the
coordinates are packed into one value.

**The viewport is two separate problems, and only one of them is open.** Building a
`GeometryCollection` from the scene's four corners is a computation over a whole EObject, which
is exactly what a `ValueFunction` is for, and the mapping above uses one for the service
resource. What is missing is only the *slot*: an attribute on an `Admin` subclass. The same
function would fill it the moment item 8 lands.

### 2. The detections — these stay in the adapter, and for two different reasons

`flushWindows` pushes one `TrafiCamDto` per camera and object class every 2 seconds. Two kinds
of state are involved, and the engine has neither:

- **Within a window**: `DetectionWindow` keeps the first position of each object id and reduces
  them to one `FeatureCollection`. The engine sees one payload and holds nothing between
  payloads. Aggregation is not a mapping task - this was settled before and TrafiCam is the
  worked case.
- **Across messages**: `className(camId, classId)` reads the `classMap` of a `CamConfig` that
  arrived **earlier, on another topic**. Even with an aggregated payload, a mapping cannot reach
  it. Note the lookup itself would be expressible - `classMap` is a real EMF map
  (`ClassMap` carries `instanceClassName="java.util.Map$Entry"`), so `collectionFilter`
  `key=<classId>` reads it exactly as the iotraffic counter mapping reads its lines. It is the
  *cross-message* part that is out of scope, not the map access.

If the adapter keeps the window and pushes the aggregated result as an EObject, the naming and
the GeoJSON conversion could move into a mapping later. That is a decision for the UDP side, not
a gap here.

### 3. The payload format — binary EMF is a southbound gap

TrafiCam reads its payloads with `BinaryResourceImpl`. `PayloadIngest` picks an EMF resource
factory by file extension and knows XMI and JSON, so a binary-EMF channel would be reported
`FORMAT_UNSUPPORTED` today. This is an ingress gap, not an engine one, and it is the only
format question the two use cases raise.

### Summary: what the two cases ask for, in order

1. **item 7** - a `FeatureMapping` on the admin references. Unblocks ILSA's location *and* both
   friendly names. Two of the five remaining gaps.
2. **item 6** - `MetadataMapping extends FeatureMapping`. ILSA's `type`/`signalGroup` only.
3. **item 8** - the admin subclass, through `AdminMapping.providerPackage`. TrafiCam's viewport,
   and ILSA's `Thermal extends Admin`.
4. The transient id attribute on `TLSignalState` and `CamConfig`. Not an event.atlas change.
5. Binary EMF in the southbound. Only TrafiCam.

## Item 7 landed — 2026-11-04

`AdminMapping` gained `friendlyNameMapping`, `latitudeMapping`, `longitudeMapping` and
`elevationMapping`, each an optional `FeatureMapping` beside the bare `*Ref` path. Both UDP
mappings were updated and two of the five remaining gaps are closed:

- ILSA: `'ILSA ' + self.intersectionName`, and the `bbox` position through a converter.
- TrafiCam: `'Camera ' + self.camId`.

**A converter, not an expression, and that is now measured.** OCL reaches the `Double[]` but
cannot index it: `->at`, `->first` and `->asSequence` all produce nothing, because a Java array
is no OCL collection (`OclExpressionEvaluatorTest.javaArrayDatatype_isNotACollection`). The UDP
side publishes `udp.array.element.<n>`; the converter is three lines.

Remaining, in order: **item 6** (ILSA metadata), **item 8** (TrafiCam `viewport`, ILSA
`Thermal`), the transient id attributes on `TLSignalState`/`CamConfig` (Jena's change), and
binary EMF in the southbound (TrafiCam only).

### Correction: OCL *can* index the bbox, with a contributed operation

The note above said a converter is the only route. That is wrong, and the correction matters
because it removes a converter from the UDP side.

The OCL **standard library** treats a `java.lang.Double[]` as one scalar: `self.bbox->size()` is
1, and `->at`, `->first`, `->flatten`, `->collect`, `->asSequence` and `->asOrderedSet` all leave
it whole or return invalid. But `OclOperation.of(name, ownerType, returnType, impl)` registers an
operation **on a type**, and the type may be that datatype:

```java
OclOperation.of("at", array1D, EcorePackage.Literals.EDOUBLE_OBJECT,
        (owner, args) -> ((Double[]) owner)[((Number) args[0]).intValue() - 1]);
```

With it, `self.geoJson.bbox.at(1)` answers 11.6 and `.at(2)` 50.9 - measured, and pinned by
`ContributedOperationTest` in `…mapping.ocl`.

**One condition applies in a deployed runtime, and it is not the default:**
`ocl.customOperationsEnabled` on PID `DefaultOclEngine` must be set to `true`. One property is
enough - `OclEngineImpl.toDefaultOptions` propagates it into the default evaluation options,
which is what an `evaluate` without explicit options uses.

Every published `OclOperationProvider` binds (`cardinality="0..n"`), so the GeoJSON model's
operations and any other bundle's compose. **Check the engine version before believing
otherwise**: `0.1.0-SNAPSHOT`, still present in `~/.m2`, binds exactly **one** provider and
ships a `NoOpOclOperationProvider` placeholder - the shape m2x replaced in `9e8bfee` ("bind
every operation provider, not one of them", 2026-08-09). The bndruns pin `[0.1.1,0.1.2)`, which
has the fix.

**Decided: the ILSA mapping takes the OCL route.** `self.geoJson.bbox.at(1)` and `.at(2)`, with
the `at` operation published by the model's own bundle. It costs one configuration property and
no converter, and every later expression over that model can use the same operations.

| route | needs |
|---|---|
| `converterId="udp.array.element.0"` | a three-line `ValueConverter` per case; no engine configuration |
| `expression="self.geoJson.bbox.at(1)"` **(chosen)** | an `OclOperationProvider` from the model bundle, plus `ocl.customOperationsEnabled=true` on the engine PID |

## Item 6 landed — 2026-11-04

`MetadataMapping` extends `FeatureMapping` and carries a `key`; `ResourceMapping.metadata` holds
them. The ILSA configuration mapping now writes `type` and `signalGroup` on its `color` resource,
which was the last open gap in that file, and `IlsaListener.describeSignal` can go.

The resource is declared there with **no value**: the configuration describes the signal, the
signal-state flow fills the colour. Declaring it is what gives the metadata somewhere to live,
the way `Signal.color` in the Ilsa package does for the adapter.

### A new open point the exercise surfaced: two mappings, one provider

The adapter pushes **both** ILSA flows into one `Ilsa` provider named after the intersection. A
mapping's provider model comes from its own `fqMid`, unless a `MappingProfile` with
`providerStrategy=UNIFIED` names one — `ValueMapperImpl.determineProviderModel`. So as written,
`ilsa-configuration` and `ilsa-signal-state` build **two** providers, not one.

The pair therefore needs a shared profile before it is deployed. Nothing is missing from the
engine for it; it is a piece of the mapping that has not been written yet, and it applies to
TrafiCam too the day its detections are mapped. The same question decides what the `color`
resource declared in the configuration mapping is for: with a UNIFIED profile it is the very
resource the state flow writes into.

Remaining after this: **item 8** (TrafiCam `viewport`, ILSA `Thermal`), the UNIFIED profile for
the ILSA pair, the transient id attributes (Jena's change) and binary EMF on ingress.
