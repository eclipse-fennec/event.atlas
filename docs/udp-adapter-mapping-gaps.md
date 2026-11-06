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

## Item 8 — the admin subclass and typed services, 2026-11-05

Measured against the deployed gateway with `EmfReadChannelProbeTest` (OSGi, in the tests
project) and a generated `ProbeAdmin extends Admin`.

### What the gateway does

- **An admin subclass keeps its type, and its own attribute is a readable resource.**
  `SensinactEMFProvider.update(provider)` and `DataUpdate.pushUpdate` store the instance as it
  comes: `EMFCompareUtil.serviceUpdate` copies it and retypes an existing admin upward through
  `copyOldService`. The gateway's own `EMFUpdateServiceTest.admin()` pins the read-back, and the
  ordinary resource API also writes it.
- **Two lookups disagree, and that decides who sees it.**

  | lookup | service EClass it uses | `admin/viewport` |
  |---|---|---|
  | `SensinactDigitalTwin.getResource(provider, service, resource)`, which `ResourceCommand` uses | `Provider.getServiceEClass(name)` - `((EObject) eGet(feature)).eClass()`, the **instance** | **found** |
  | `SensinactProvider.getServices()` and `.getResource(…)` | `ModelNexus.getDefinedServiceForProvider` - `getEReferenceType()`, the **declaration** | null |
  | `twin.snapshotResource(…)`: SensorThings, the northbound provider description, filters | `(EClass) sf.getEType()`, the **declaration** | null |

  ```
  twin.getResource -> pushed provider: found value=a polygon from the push
                      updated provider: found value=a polygon from the EMF channel
  snapshotResource -> null
  setValue through the resource API -> value now=written through the resource API
  ```

  The attribute also travels on the typed-event bus (`notifyServiceAdd` and `notifyResourceChange`
  stream the **instance's** features), so the history engine sees it.
- **A typed service can be created on a live provider, and a frozen model does not stop it.**
  `SensinactEMFProvider.getOrCreateService(name, EClass)` goes to
  `ModelNexus.createServiceInstance`, which puts the service into the `DynamicProvider` services
  map without touching the Model. `SensinactServiceImpl` is then built from
  `serviceInstance.eClass()`, so its resources are visible to the ordinary read paths:

  ```
  getOrCreateService("K1_1", ProbeAdmin) on a frozen model
     -> eClass=ProbeAdmin resources=[viewport, …] | provider services=[thermal, admin, K1_1]
  getResource("K1_1", "viewport") -> found
  ```
- **Never add a resource to a generated service EClass.** One dynamic EAttribute on such an
  EClass makes every later `eIsSet` of it, and every `EcoreUtil.copy` of an instance, end in a
  `StackOverflowError`. It is a gateway bug: sensiNact's custom
  `Service.eIsSet(EStructuralFeature)` calls `super.eIsSet(feature)`, which ping-pongs with
  EMF's base `eIsSet(int)` for a feature no generated switch knows.

### The model-manager route is a dead end, and a destructive one - 2026-11-06

`ModelManagerAdminResourceProbeTest` (OSGi, disabled, run it alone) asks the other obvious
question: can the extra admin resource come from the **model manager** - build a model, give its
admin service one more resource, create a provider of it? No.

```
1 create a model with one ordinary service  -> services=[data, admin] frozen=false dynamic=true
2 whose EClass is that admin?               -> Admin, https://eclipse.org/sensinact/core/provider/1.0
                                               is the generated Admin=true
3 createService("admin")                    -> FAIL IllegalArgumentException: There is an
                                               existing service with name admin in model
4 createResource("viewport") on it          -> accepted, and the GENERATED Admin EClass carries it
5 create a provider of that model           -> FAIL StackOverflowError
8 a provider of an unrelated model          -> FAIL StackOverflowError
```

A model the manager builds is an EClass with `Provider` as its supertype, so its `admin` is the
**inherited** reference and there is no per-model admin EClass to extend. `createService("admin")`
is refused for that reason, and the resource the model's admin service accepts lands on the one
generated `Admin` every provider in the runtime shares. From that moment the runtime can create
no provider at all - step 8 is an unrelated model failing the same way - because
`createAdminServiceForProvider` walks the admin's features and the dynamic one sends the
gateway's `eIsSet` override into an infinite ping-pong with EMF's:

```
AdminImpl.eIsSet(int) -> ServiceImpl.eIsSet(int):182 -> BasicEObjectImpl.eIsSet(EStructuralFeature):1295
  -> ServiceImpl.eIsSet(EStructuralFeature):99 -> BasicEObjectImpl.eIsSet(int):1277 -> eDerivedStructuralFeatureID
```

So it is the same gateway bug as the one above, reached without writing an `.ecore`: the
admin-subclass route plus one `update()` stays the only way to an extra admin attribute.

**Nor does handing the model manager the subclass itself.** The obvious repair is to give the
model an admin of a type that already declares the attribute, through
`EMFModel.createServiceWithEClass("admin", <an EClass extending Admin>)`.
`ModelManagerAdminEClassProbeTest` measures it with the generated `ProbeAdmin`. It is harmless -
and it has no effect (2026-11-06):

```
3 createServiceWithEClass("admin", ProbeAdmin) -> the handle: eClass=ProbeAdmin resources=[viewport, …]
4 what the MODEL says afterwards               -> admin eClass=Admin
                                                  the admin reference is typed Admin (owner Provider)
5 create a provider of that model              -> the stored admin is a Admin, a ProbeAdmin=false
7 read viewport back                           -> all four lookups null
8 getOrCreateService("admin", ProbeAdmin)      -> returned eClass=Admin, the stored admin stays an Admin
```

`ModelImpl.createServiceWithEClass` only builds a `ServiceImpl` **handle** over the EClass it is
given; it writes nothing into the model EClass, which is why step 4 still reports the inherited
`Provider.admin` reference. Its one caller in the whole gateway is `SetValueCommand`, which uses
it to describe a service a **value push** carries (`DataUpdateDto.serviceEClass`) and which the
model does not declare; the instance is then attached by `handleDataUpdate` ->
`createServiceInstance` with that same EClass. Neither condition holds for `admin`: the model
always declares it, so the handle leads nowhere. And `getOrCreateService` cannot repair that either: for a service
name the model **declares**, `ModelNexus.createServiceInstance` instantiates
`serviceFeature.getEType()` and ignores the EClass in its signature. The EClass is honoured only
in the other branch - an undeclared name on a `DynamicProvider` - which is exactly the `K1_1`
case that works above. `admin` is declared by `Provider` for every model there is, so no
EClass-taking API can retype it.

The one thing that does retype an admin is still a pushed or updated *instance*
(`EMFCompareUtil.copyOldService`), which is the route the item 8 design uses.

**Why `twin.getResource` answers in one probe and not in the other.** That lookup resolves the
service EClass from the stored **instance**, so it finds an extra admin attribute only once an
instance of the subclass is actually stored. After `createProvider` the admin is the plain
`Admin` that `ProviderFactory` built, and no model-level call changes that - which is why
`twin.getResource(id, "admin", "viewport")` is null in every pass above. Pass D of
`ModelManagerAdminEClassProbeTest` adds the missing step to a **model-manager** model, not to a
generated provider EClass, and it works there too:

```
D1 a model-manager model and a provider of it -> the stored admin is a Admin
                                                 twin.getResource(admin, viewport)=null
D2 update() with a copy whose admin is a ProbeAdmin
                                              -> the stored admin is now a ProbeAdmin
D3 read it back                               -> provider.getResource=null
                                                 twin.getResource=found value=…
                                                 snapshotResource=null
                                                 EMF admin: ProbeAdmin.viewport=…
D4 write through the ordinary resource API    -> value now=written through the resource API
```

### The gateway's own tests say the same, and one of them widens the design - 2026-11-06

`core/impl/src/test` carries three scenarios for an extended admin, and they agree with every
measurement above:

- **`integration/EMFUpdateServiceTest.admin()`** - the generated case. `testdata.ecore` declares
  `TestAdmin extends Admin` with one extra attribute, and **`TestSensor` does not redeclare
  `admin`**: the subclass arrives only as an instance, `sensor.setAdmin(admin)` through the
  inherited setter. The assertion reads it with a `ResourceCommand`, i.e. the instance-based
  lookup: `assertEquals("blub", getResourceValue("TestSensor", PROVIDER, "admin", "testAdmin"))`.
  That is pass D, pinned upstream.
- **`model/nexus/NexusTest.AdminTests.pushEObjectTestSimpleAttributeChange`** - the retype itself:
  save a provider, then `saved.setAdmin(testAdmin)` and save again, and
  `assertEquals(testAdminEClass, savedAgain.getAdmin().eClass())`. Its package is **loaded
  dynamically from `extended.ecore`**, not generated.
- **`twin/impl/SensinactTwinTest.simpleResourceValueFilterWithDynamicResource`** - the same
  dynamic admin subclass, but the assertions are on **snapshots**, and they only read the dynamic
  *service*. The admin's extra attribute is never asserted there, which is consistent with the
  snapshot path not seeing it.

A fourth one is worth knowing for item 5: **`NexusTest.duplicatedServiceTest`** puts a service
into a `DynamicProvider`'s services map under the name of a *declared* service reference and
expects `nexus.save` to throw `IllegalArgumentException`. A produced service name must therefore
never collide with a declared one.

**The widening: the admin subclass does not have to be generated.** The two dynamic scenarios
cast a dynamically created `TestAdmin` to `Admin`, which only works because EMF answers a dynamic
subclass of a generated EClass with the **generated impl** carrying the extra feature in its
`eSettings`. Pass E of `ModelManagerAdminEClassProbeTest` takes that all the way through the
twin, with an EClass built at run time from `EcoreFactory` and in no bundle at all:

```
E1 what does EMF build for the dynamic subclass? -> impl=AdminImpl | an Admin=true | viewport=a dynamic polygon
E2 a model-manager model and a provider of it    -> the stored admin is a Admin
E3 update() with that dynamic subclass           -> the stored admin is now a DynAdmin
E4 read it back                                  -> twin.getResource=found value=…
                                                    EMF admin: DynAdmin.viewport=…
                                                    provider.getResource=null, snapshotResource=null
E5 write through the ordinary resource API       -> value now=written through the resource API
E6 another provider of the same model            -> admin resources=[the usual five]
```

So `AdminMapping` needs **no `EClass` reference into a deployed `.ecore`**. The engine can build
the admin EClass from the mapping itself - one dynamic EClass extending
`ProviderPackage.Literals.ADMIN`, one `EAttribute` per declared admin feature mapping - and the
deployment stays a mapping XMI with no bundle beside it. The same is not true of a typed
*service*, where the EClass is the point of the feature.

The retype is four lines on the provider the engine already has:

```java
Provider copy = provider.getEMFProvider();          // a copy, by contract
ProbeAdmin admin = ProbeadminFactory.eINSTANCE.createProbeAdmin();
admin.setFriendlyName(id);
copy.setAdmin(admin);
provider.update(copy);
```

**And the package URI of the model changes nothing about the admin - it only decides whether the
model works at all.** The probe runs its ten steps twice. Pass A gives the model a URI of its
own, so `ModelNexus.createModel` builds a new dynamic EPackage and the model EClass and
`ProbeAdmin` are strangers. Pass B gives `ProbeadminPackage.eNS_URI`, so the model EClass is put
into the *generated* package that declares `ProbeAdmin` itself:

```
B 0 which EPackage did the model land in? -> name=probeadmin, is the generated package=true,
                                             its factory=ProbeadminFactoryImpl, it declares ProbeAdmin=true
B 4 what the MODEL says afterwards        -> admin eClass=Admin, typed Admin (owner Provider)
B 5 create a provider of that model       -> FAIL IllegalArgumentException:
                                             The class 'AdminInProbePackage' is not a valid classifier
                                             at ProbeadminFactoryImpl.create -> EcoreUtil.create
                                             -> ModelNexus.doCreateProvider
```

Sharing the package does not bring the model EClass any closer to `ProbeAdmin` - steps 2 to 4
are identical in both passes - and it costs the model its instances: `doCreateProvider` calls
`EcoreUtil.create(modelEClass)`, which goes to `ePackage.getEFactoryInstance()`, and a
**generated** factory is a switch over the classifier ids it was generated from. An EClass added
to that package afterwards is not in the switch, so the factory refuses it. The rule to keep:
**never create a model in the nsURI of a generated EPackage** - a model the manager builds needs
a package with the reflective `EFactoryImpl`.

### What this gives the mapping

The useful capability is **a mapping names the EClass of a service**
(`ServiceMapping.serviceEClass : EClass`), not `AdminMapping.providerPackage : EPackage`. The
engine calls `getOrCreateService(name, eClass)` where it calls `createServiceOn` today, which is
the same place item 5 already grew the model at push time. Three cases follow:

| case | mapping | result |
|---|---|---|
| ILSA `Thermal` | `serviceEClass=Thermal`, constant name `thermal` | a `Thermal`-typed service, its resources from the EClass |
| ILSA one service per signal | `serviceEClass=Signal` plus the produced name of item 5 | one `Signal`-typed service for each `TLSignal`, named from the payload |
| TrafiCam `observedArea` | `serviceEClass=ObservedObjects` | `objects` and `classificationName` carry the types the Jena model declares |

Two rules come with it:

- **Only the resources the EClass declares.** A `ResourceMapping` whose mid is no feature of the
  named EClass must be refused at registration, because adding it would poison the EClass.
- **An extra admin attribute needs two things, and neither is large.** `createProvider` builds a
  plain `Admin`, so the mapping must retype it once - one `SensinactEMFProvider.update()` with a
  copy of `getEMFProvider()` whose admin is an instance of the named subclass. After that every
  value goes through the ordinary resource API, with change rules, timestamps and metadata as
  usual. The second thing is the lookup: the engine resolves admin resources with
  `provider.getServices().get("admin").getResources()`, the declaration-based path, and has to
  use `twin.getResource(providerId, "admin", name)` instead. The metamodel slot is
  `AdminMapping.adminEClass : EClass` plus one `FeatureMapping` per attribute.
  **TrafiCam's `viewport` is then expressible** - the same `udp.traficam.viewport` function the
  `observedArea` resource uses. Know what it buys, though: a direct resource read answers, a
  provider listing and SensorThings do not, because they build from snapshots. The
  `observedArea` service publishes the same geometry and is visible everywhere.

## Item 8 landed - 2026-11-06

Both halves are implemented, and the metamodel grew by two features:
`ServiceMapping.serviceEClass : EClass` and `AdminMapping.extendAdmin : EBoolean` (default
false). The user guide documents both.

**An extra admin resource** (`extendAdmin`). `AdminMapping` already inherited
`resources : ResourceMapping[]` from `ServiceMapping`, and a `ResourceMapping` *is* an
`EAttribute`, so it already carries its own `eType` - nothing else was needed in the model.
`AdminEClassBuilder` builds one dynamic `EClass extends Admin` per mapping, cached by content
so an edited mapping gets a new one with no invalidation hook, and `ValueMapperImpl.extendAdmin`
retypes the provider once with `SensinactEMFProvider.update()`. The guard is the twin's own
instance-based lookup, so a provider that already carries the resources costs one map read per
extra. Reads and writes then go through `twin.getResource(providerId, "admin", mid)`, which
`mapSingleResource` falls back to whenever the declared service does not list a resource.
`ProviderMappingRegistryImpl.unknownAdminResources` still refuses a missing `mid` and
`metadata`.

**A typed service** (`serviceEClass`). `ProviderModelSensinactMapper.mapService` leaves such a
service out of the model, and `ValueMapperImpl.createTypedService` puts it on the provider with
`getOrCreateService(name, eClass)` on the first payload. `refusedTypedServices` refuses a
resource mapping the EClass does not declare and a `referencedResource` beside an EClass;
`unresolvedServiceEClasses` defers the mapping while the EClass is a proxy, as `providerClasses`
already did.

**The one thing that was not in the design, and it matters.**
`ModelNexus.createServiceInstance` honours the EClass only in its `DynamicProvider` branch:

```java
if (serviceFeature.isEmpty() && !(provider instanceof DynamicProvider)) {
    throw new IllegalArgumentException("No Service with name " + serviceName + " exists for provider ...");
}
```

The model manager builds every model on `Provider`, so the first run failed with exactly that
message. `makeDynamicIfTyped` now exchanges the model EClass's supertype for
`DYNAMIC_PROVIDER` when any service of the mapping names an EClass - while the EClass is still
dynamic and before the first provider is instantiated from it. That is also why `K1_1` worked in
the probes: `ProbeProvider extends DynamicProvider`.

Guards: `AdminExtraResourceTest` and `TypedServiceValidationTest` (plain JUnit),
`AdminExtraResourceMappingTest` and `TypedServiceMappingTest` (OSGi, 5 tests). The OSGi ones pin
the two halves end to end, including that the extra admin resource is absent from
`snapshotResource` and from the declared admin service, and that `friendlyName` survives the
exchange of the admin instance. The workspace is at 403 plain-JUnit and 100 OSGi tests, green.

### The three UDP mappings were rewritten on it - 2026-11-06

- `ilsa-configuration-mapping.xmi` and `ilsa-signal-state-mapping.xmi`: both `signal` blocks
  name `serviceEClass=.../sensinact/ilsa/1.0#//Signal`, the EClass the adapter builds. The
  selector `referencedResource` stays - it is also the path that picks each service's source
  element - and `eType` plus `extraMetadata` came out of the resource blocks, because a typed
  service takes both from the EClass. `Signal.color` and `Signal.observedArea` carry their
  `Metadata` annotations already.
- `traficam-config-mapping.xmi`: `observedArea` is typed `ObservedObjects`, and the admin now
  carries `viewport` through `extendAdmin`. Two things are recorded in the file itself: the
  admin value is invisible to the snapshot consumers, so the `observedArea` service remains the
  northbound one; and `ObservedObjects.objects` carries no `Metadata` annotation, unlike
  `TraficamAdmin.viewport`, so `sensorthings.observedArea` needs one line in `traficam.ecore`
  or the service must stay untyped.
- `Ilsa.thermal` is left out on purpose. `Thermal extends Admin` and a typed service may be an
  Admin subclass, but the thermal topic carries a bare number rather than an EObject, so the
  engine never sees that flow.

Every engine gap the four flows named is now closed. What remains is the UNIFIED profile (a
mapping to write), the two transient id attributes and the binary ingress - all outside the
engine.

## The detections became a mapping too - 2026-11-06

They were recorded above as *not planned to move*, for two reasons that both still hold: the
2-second window is state between payloads, and `classificationName` is a lookup into a
`CamConfig` that arrived earlier on another topic. What changed is not the engine but the
**payload**. Jena added `TrafiCamDetections` and `ObservedClass` to `de.jena.traficam.model`,
and `TrafiCamListener.flushWindows` now fills one of them per camera and calls
`InstancePusher.pushInstance`, instead of building a `TrafiCamDto` per class and pushing it
through `DataUpdate`. `DetectionWindow.flush()` hands over `List<TrafiCamObject>` rather than a
finished `FeatureCollection`.

So the division moved to where it belongs: the adapter keeps what holds state, the mapping
shapes the provider. `traficam-detections-mapping.xmi` is that mapping, and it needs no feature
that was not already built:

| what the old `TrafiCamDto` carried | how the mapping says it |
|---|---|
| one push per class | one `<services>` block whose `referencedResource` ends on `classes` and whose `name` is produced - one service per entry (item 5) |
| the service name | `ObservedClass.classId`, the fourth topic segment |
| `ObservedObjects` as the service type | `serviceEClass` (item 8), so `objects` and `classificationName` carry the Jena types |
| the `FeatureCollection` | a `ValueFunction` over `ObservedClass.objects` - `udp.traficam.detections`, still to write, the move `ViewportValueFunction` already made |
| the flush time | the provider timestamp, from `TrafiCamDetections.time` |

`CamConfig.camId` is in place as well - transient, last in the EClass, because TrafiCam reads
binary EMF and `BinaryResourceImpl` identifies a feature by position - so the TrafiCam
configuration mapping has no open gap either.

**What is left for TrafiCam is one function and one profile.** Both TrafiCam mappings name the
provider after the camera, but a mapping's model comes from its own `fqMid`, so the pair needs a
`MappingProfile` with `providerStrategy=UNIFIED`, exactly as the ILSA pair does. That is the
last item of the whole exercise that is a mapping rather than engine work.

## The two profiles - 2026-11-06

`ilsa-profile.xmi` and `traficam-profile.xmi` close the last mapping-level item. Each pair of
mappings now references its own profile by `profileId`
(`href="ilsa-profile.xmi#ilsa"`), and `providerStrategy="UNIFIED"` makes
`determineProviderModel` take the model name from the profile instead of each mapping's
`fqMid`. The provider **id** is untouched - it is still what each mapping's `<name>` produces,
the intersection or the camera - so one profile does not collapse the cameras into one provider.

Three things are worth keeping:

- **A profile is validated against each mapping on its own**, and a failed requirement
  *throws* at registration. The two mappings of a pair split the provider between them - ILSA's
  configuration owns the admin and describes the signals, a state payload carries one colour -
  so anything only one of them has must be optional. ILSA can still require the `signal`
  service and its `color` resource, which both carry; TrafiCam can require only the admin.
  That is the ceiling of a profile as a contract for a split provider.
- **The fragment is the profileId, not `/`.** `ProviderMappingRegistryImpl.resolveProfile`
  looks the fragment up in the profile registry, which is the only route when the document is
  not reachable - a mapping delivered from a Model Atlas. The shipped examples under
  `model/examples` still use `#/`, which works only as a document reference.
- **Two validation bugs surfaced while writing them, and both are fixed.**
  `requiresFriendlyName` and `requiresLocation` asked only for the constant and the bare
  `*Ref` path, so the FeatureMapping slots of item 7 - which is how *both* UDP pairs compute
  their friendly name, and how ILSA reads its location out of a `bbox` - read as missing. And
  `validateResourceAgainstProfile` called `getEAttributeType().getName()` without a null check,
  which an `expectedType` on a resource of a **typed** service would have thrown on, since such
  a resource states no type. Guards: two new cases in `MappingProfileValidationTest`.

`UdpMappingDocumentTest` (plain JUnit, in the tests project) now loads all six UDP documents
and checks that they still parse against the metamodel, that each mapping names its pair's
profile by id, and that every typed service names a Jena EClass. The Jena payload models are
not deployed here, so that is as far as a test can go - but it is what catches a feature
renamed out from under these files.

### A function receives a collection, after a fix - 2026-11-06

`ValueFunctionMappingTest` gained the collection cases it had been missing, and the first one
failed the TrafiCam detections mapping: `ValueMapperImpl.followPath` collapsed **every**
collection it met, by `collectionFilter` or by `collectionIndex` (default 0), so a `functionId`
with a path to `ObservedClass.objects` - which is `upperBound="-1"` - was handed the first
`TrafiCamObject` and rendered one detection per class. Nothing would have failed; the GeoJSON
would just have been thin.

Collapsing is right for a path that **passes** a collection, which has to pick an element to
carry on from. It is wrong for a path that **ends** on one and feeds a function: a function is a
producer, and what its path found is the collection. `getRawValue` now routes the function case
through `followPathForFunction`, which passes the collection whole unless the mapping narrows
it - a `collectionFilter`, or a **non-zero** `collectionIndex`, the same asymmetry
`ForEachServiceTest` pins for a service name, and for the same reason: an explicit
`collectionIndex="0"` is indistinguishable from an unset one. An empty collection is passed on
as an empty list rather than swallowed, because for a producer "the camera saw nothing" is a
statement; only the function can decide whether that is an empty FeatureCollection or no value.

Nothing else in the workspace relied on the old behaviour - the full build is green - and the
detections mapping now names the path, so `udp.traficam.detections` takes a
`List<TrafiCamObject>` and needs to know nothing about `ObservedClass`. `CamConfig.scene` is
single-valued, so the two viewport mappings were never affected either way.
