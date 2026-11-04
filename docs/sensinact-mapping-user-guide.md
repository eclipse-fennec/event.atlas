# SensiNact mapping

> **Status: in development.** The core mapping engine and its OSGi integration are
> implemented and tested; the metamodel and options described here may still evolve.

Turn instances of a **domain-specific EMF model** into **Eclipse SensiNact digital-twin
providers** — declaratively, with an XMI file, instead of hand-written transformation code.
A mapping says *"for objects of this EClass, build a provider with these services and
resources, taking each value from this feature path."* The SensiNact southbound registers
the mapping as a service; every incoming instance is then projected onto the twin
automatically.

- **Input:** any EMF model instance (an `EObject`) plus a mapping XMI.
- **Output:** a SensiNact provider / service / resource structure, updated in the digital twin.
- **No code:** mappings are configuration — hot-reloadable, validated against the EMF
  metamodel, and reusable across device types.

The mapping metamodel lives in the bundle `org.eclipse.fennec.event.atlas.mapping`
(model `model/event-atlas-mapping.ecore`, EMF package
`org.eclipse.fennec.event.atlas.model.mapping`, nsURI
`https://fennec.eclipse.org/event.atlas/mapping/1.0`).

> **Migrating from the SensiNact-era nsURI.** Mappings written against the previous URI
> `https://fennec.eclipse.org/sensinact/core/mapping/1.0` do not load any more — EMF answers
> `PackageNotFoundException`. Replace the namespace URI in the XMI root element; nothing else
> about the file changes.

## Quick start

A minimal mapping: for every `EM310UDLUplink` build a provider named from a device field,
with one `battery/level` resource read from a nested feature path.

```xml
<?xml version="1.0" encoding="UTF-8"?>
<mapping:ProviderMapping
    xmi:version="2.0"
    xmlns:xmi="http://www.omg.org/XMI"
    xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
    xmlns:ecore="http://www.eclipse.org/emf/2002/Ecore"
    xmlns:mapping="https://fennec.eclipse.org/event.atlas/mapping/1.0"
    mid="em310udl-battery-sensor">

  <!-- Provider id (falls back to the static name if the feature is empty) -->
  <name name="EM310UDL Battery Sensor">
    <featurePath xsi:type="ecore:EAttribute"
        href="https://eclipse.org/fennec/lorawan#//UplinkMessage/deduplicationId"/>
  </name>

  <!-- Which source EClass(es) this mapping applies to -->
  <providerClasses href="http://www.example.org/lorawan/specific/em310udl#//EM310UDLUplink"/>

  <services mid="battery">
    <name name="Battery"/>
    <resources name="Battery Level" unit="V" mid="level">
      <eType xsi:type="ecore:EDataType"
          href="http://www.eclipse.org/emf/2002/Ecore#//EDouble"/>
      <valueFeature xsi:type="ecore:EReference"
          href="http://www.example.org/lorawan/specific/em310udl#//EM310UDLUplink/object"/>
      <valueFeature xsi:type="ecore:EAttribute"
          href="http://www.example.org/lorawan/specific/em310udl#//DecodedObject/battery"/>
    </resources>
  </services>
</mapping:ProviderMapping>
```

Load, register, and let the southbound apply it (the payload EPackages must be registered
in the same runtime):

```java
// 1. Load the mapping XMI (any EMF ResourceSet that has the mapping package registered)
Resource r = resourceSet.createResource(URI.createURI("em310udl-battery-mapping.xmi"));
r.load(null);
ProviderMapping mapping = (ProviderMapping) r.getContents().get(0);

// 2. Register it with the mapping registry (an OSGi service)
mappingRegistry.registerModelMapping(mapping);   // creates the provider model in the twin

// 3. Values are projected onto the twin whenever you map an instance
gatewayThread.execute(new AbstractSensinactCommand<Void>() {
    protected Promise<Void> call(SensinactDigitalTwin twin, SensinactModelManager mm, PromiseFactory pf) {
        ValueMapper mapper = ValueMapperFactory.createValueMapper(twin, mapping);
        mapper.mapInstance(uplink);   // uplink is an EM310UDLUplink EObject
        return pf.resolved(null);
    }
});
```

In an OSGi deployment you normally don't call `ValueMapper` yourself — each
`ProviderMapping` (and, optionally, `MappingProfile`) lands in a named EObject registry
(from files, a Model Atlas, or programmatically) and the southbound listeners do the
rest. See [Runtime & OSGi](#runtime--osgi).

## The XMI skeleton

Every mapping file is an instance of one of two root types:

- **`ProviderMapping`** — a mapping (the common case).
- **`MappingProfile`** — a reusable target-structure template (see [Profiles](#mapping-profiles)).

The root element must declare the mapping namespace. The `ecore` namespace is needed
because feature paths point at Ecore elements by URI:

```xml
<mapping:ProviderMapping
    xmlns:xmi="http://www.omg.org/XMI"
    xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
    xmlns:ecore="http://www.eclipse.org/emf/2002/Ecore"
    xmlns:mapping="https://fennec.eclipse.org/event.atlas/mapping/1.0"
    mid="…">
```

> The namespace **prefix** is arbitrary — the test data uses both `mapping:` and
> `sensinactMapping:`. What matters is that the namespace URI is exactly
> `https://fennec.eclipse.org/event.atlas/mapping/1.0`.

### Feature paths — the core mechanism

A **feature path** is an ordered list of EMF structural features that navigates from the
source object to a value. `UplinkMessage → object → battery` becomes two `valueFeature`
entries. Each entry is an `href` into the source `.ecore`:

```xml
<valueFeature xsi:type="ecore:EReference"  href="model.ecore#//EM310UDLUplink/object"/>
<valueFeature xsi:type="ecore:EAttribute" href="model.ecore#//DecodedObject/battery"/>
```

**When is `xsi:type` required?** Only when the containing feature is declared as the
*abstract* `EStructuralFeature` and EMF cannot tell whether you mean an attribute or a
reference. That is the case for `featurePath`, `valueFeature`, `unitFeature`, and the admin
`*Ref` features — always tag those `xsi:type="ecore:EAttribute"` or `"ecore:EReference"`.
References whose declared type is already concrete (`providerClasses`, `targetEClass`,
`filter`, `expectedType`, `providerPackage`, `eType`) don't need it.

All feature paths honour [collection navigation](#collections).

## Provider mapping

`ProviderMapping` is the root of a device mapping. Its attributes and children:

| Element | Meaning |
|---|---|
| `mid` (attr, required) | Mapping id. Also the default provider model name / provider id (see strategy). |
| `providerClasses` (ref, 0..\*) | Source `EClass`es this mapping applies to. The registry keys mappings by these. |
| `providerTimestamp` (attr, default `false`) | If `true`, one timestamp is used for the whole provider (all services/resources update together) instead of per-resource. Use for snapshot-style payloads. |
| `name` (child, required) | The provider name / id — see [Name mapping](#name-mapping). |
| `timestamp` (child) | Provider-level timestamp — see [Timestamps](#timestamps). |
| `services` (child, 0..\*) | The [service mappings](#service-mapping). |
| `admin` (child) | The [admin service](#admin-service). |
| `profile` (ref) | Optional [mapping profile](#mapping-profiles) this mapping conforms to. |

### Name mapping

The `name` (a `NameMapping`) provides the provider name/id. You can give a static `name`,
a feature path, or both — the feature value wins, the static string is the fallback:

```xml
<!-- static only -->
<name name="Weather Station"/>

<!-- from a feature path -->
<name>
  <featurePath xsi:type="ecore:EReference" href="w.ecore#//WeatherReports/reports"/>
  <featurePath xsi:type="ecore:EReference" href="w.ecore#//WeatherReport/weatherStation"/>
  <featurePath xsi:type="ecore:EAttribute" href="w.ecore#//WeatherStation/id"/>
</name>

<!-- feature path with static fallback -->
<name name="Unknown device">
  <featurePath xsi:type="ecore:EAttribute" href="m.ecore#//UplinkMessage/deduplicationId"/>
</name>
```

`name` is also used for service and resource display names — there, only the static `name`
attribute is typically set (`<name name="Battery"/>`).

### Timestamps

A `TimestampMapping` decides the observation time. The engine resolves it in this order, and
the first answer wins:

1. the `featurePath`, if one is set and this payload carries it;
2. the static `timestamp` literal;
3. the current time.

```xml
<!-- from the payload -->
<timestamp>
  <featurePath xsi:type="ecore:EAttribute" href="m.ecore#//UplinkMessage/time"/>
</timestamp>

<!-- gateway clock: no path and no literal -->
<timestamp/>

<!-- parse a non-standard textual timestamp -->
<timestamp hint="yyyy-MM-dd'T'HH:mm:ssX">
  <featurePath xsi:type="ecore:EAttribute" href="m.ecore#//Reading/observedAt"/>
</timestamp>
```

`Instant`, `java.util.Date` and an epoch-millis `Long` are read as they are. A `String` is read
with the `hint` when one is set, and as an ISO-8601 instant otherwise — or as ISO when the hint
does not fit that value.

> **`strategy` does nothing.** The `TimestampStrategy` enum (`NOW`, `FEATURE`, `FUNCTION`) is in
> the metamodel, and the engine never reads it. The order above is implicit. Do not write
> `strategy="NOW"` and expect the current time: a mapping with a feature path reads the path.

> **The `hint` reads a string only.** If the feature is an `EDate`, the codec has already made a
> `java.util.Date` before the mapping runs, and the hint is never used. A date format for a
> payload belongs to the codec, and not here.

The static value is the `timestamp` attribute on the `<timestamp>` element itself, an ISO-8601
instant literal:

```xml
<timestamp timestamp="2026-09-16T10:15:30Z"/>
```

Its datatype `EInstant` is deliberately backed by a `String` rather than `java.time.Instant`, and
that is not a detail you can change back: a mapping stored in a Model Atlas is loaded from the
registered `.ecore` **dynamically**, where no generated factory exists and EMF's reflective
conversion has nothing to build an `Instant` from. Typed as `Instant`, the literal above failed to
load — in the runtime too. The same reasoning fixed `EDuration` in the deployment metamodel
(issue #61), and it applies to any `java.time` type either metamodel might grow.

Timestamps can be set at three levels; the most specific one wins per resource:
provider (`ProviderMapping/timestamp`) → service (`ServiceMapping/timestamp`) →
resource (`ResourceMapping/timestamp`). A resource can also *reference* a timestamp defined
elsewhere in the same file instead of repeating it:

```xml
<timestamp>                                  <!-- provider-level, id //@timestamp -->
  <featurePath xsi:type="ecore:EAttribute" href="m.ecore#//UplinkMessage/time"/>
</timestamp>
…
<resources mid="level" timestamp="//@timestamp"> … </resources>
```

## Service mapping

A `ServiceMapping` (the `services` child) groups resources. It inherits `mid`, `name`, and
`timestamp` from the common mapping type and adds resources:

| Element | Meaning |
|---|---|
| `mid` | Service id (its name in the twin, e.g. `battery`). |
| `name` | Display name. |
| `timestamp` | Service-level timestamp (optional). |
| `resources` (0..\*) | Explicit [resource mappings](#resource-mapping). |
| `referencedResource` | A [ReferenceMapping](#automatic-resource-generation) that auto-generates resources. |

You can mix explicit `resources` and an auto-generating `referencedResource` in the same
service.

### Resource mapping

A `ResourceMapping` (the `resources` child) is one data point. It maps a source value onto
a SensiNact resource:

| Element | Meaning |
|---|---|
| `mid` (required) | Resource id (its name in the twin, e.g. `level`). |
| `name` | Display name → written as the resource's `friendlyName` metadata. |
| `unit` | Unit string → `unit` metadata (falls back to an Ecore annotation, below). |
| `eType` | Target `EDataType` (e.g. `EDouble`). The value is converted to it. |
| `valueFeature` (0..\*) | Feature path to the source value. |
| `valueMapping` | Richer alternative to `valueFeature`: a `ValueMapping` with the path plus `collectionIndex`/`collectionFilter`, an `expression`, a `functionId`, a `converterId` and a constant `value`. Wins when both are set. |
| `unitFeature` (0..\*) | Feature path to a dynamic unit value. |
| `timestamp` | Resource-level timestamp, or a reference to one (`//@timestamp`). |
| `extraMetadata` (0..\*) | Arbitrary `key`/`value` metadata entries. |
| `defaultValueLiteral` (attr) | Initial value before the first reading (parsed against `eType`). |

```xml
<resources name="Battery Level" unit="V" mid="level">
  <eType xsi:type="ecore:EDataType" href="http://www.eclipse.org/emf/2002/Ecore#//EDouble"/>
  <valueFeature xsi:type="ecore:EReference"  href="m.ecore#//EM310UDLUplink/object"/>
  <valueFeature xsi:type="ecore:EAttribute" href="m.ecore#//DecodedObject/battery"/>
  <extraMetadata key="displayFormat" value="%.2f"/>
</resources>
```

### Metadata: where values come from

For each resource the engine assembles default metadata from, in order of precedence:

- **unit** — the `unit` attribute if set, otherwise the source attribute's
  `sensinact.mapping` annotation, key `sensinact.mapping.unit`.
- **friendlyName** — the resource's `name`.
- **description** — the `descriptionMapping` name if set; otherwise the source attribute's
  `sensinact.mapping` annotation (`sensinact.mapping.description`); otherwise the GenModel
  `documentation` annotation.
- **extra metadata** — the `extraMetadata` entries if any; otherwise every detail of the
  source attribute's `sensinact.mapping.metadata` annotation.

This means you can keep most metadata *on the Ecore model* and let it flow through
automatically — especially useful with [auto-generation](#automatic-resource-generation).
Annotate the model attribute:

```xml
<!-- in your domain .ecore -->
<eStructuralFeatures xsi:type="ecore:EAttribute" name="windSpeed" eType="…EFloatObject">
  <eAnnotations source="http://www.eclipse.org/emf/2002/GenModel">
    <details key="documentation" value="Wind speed: m/s (FF)"/>
  </eAnnotations>
  <eAnnotations source="sensinact.mapping">
    <details key="sensinact.mapping.unit" value="m/s"/>
  </eAnnotations>
  <eAnnotations source="sensinact.mapping.metadata">
    <details key="sensorthings.unit.name" value="meters per seconds"/>
  </eAnnotations>
</eStructuralFeatures>
```

The annotation sources and keys are defined in `SensinactMapperConstants`:
`sensinact.mapping` (with `sensinact.mapping.unit` / `sensinact.mapping.description`) and
`sensinact.mapping.metadata`.

## Collections

When a feature path crosses a multi-valued reference, pick the element with
`collectionIndex` (0-based, default `0`). It is available on every feature-path element
(`name`, `timestamp`, `referencedResource`, …):

```xml
<!-- name from reports[0].weatherStation.id -->
<name collectionIndex="0">
  <featurePath xsi:type="ecore:EReference" href="w.ecore#//WeatherReports/reports"/>
  <featurePath xsi:type="ecore:EReference" href="w.ecore#//WeatherReport/weatherStation"/>
  <featurePath xsi:type="ecore:EAttribute" href="w.ecore#//WeatherStation/id"/>
</name>
```

### Selecting by content: `collectionFilter`

A position only works when the payload has one. A JSON object writes its members in whatever
order the sender chose, and a device that splits one reading over several messages sends a
different subset each time — then `collectionIndex="4"` addresses a different thing in every
message, or nothing at all. `collectionFilter` picks the element by what it *is*:

```xml
<referencedResource xsi:type="mapping:ReferenceMapping" collectionFilter="key=line_5">
  <featurePath xsi:type="ecore:EReference" href="c.ecore#//CounterUplink/object"/>
  <featurePath xsi:type="ecore:EReference" href="c.ecore#//CounterData/lines"/>
  <featurePath xsi:type="ecore:EReference" href="c.ecore#//LineEntry/value"/>
</referencedResource>
```

- The expression is `<path>=<literal>`, the path a dot-separated chain of feature **names**
  read from the collection element. An EMF map is a list of entries with a `key` and a `value`
  feature, so a map is addressed as `key=line_5` or `value.line_number=5`.
- Comparison is on the string form of the value: `5` matches an `EInt` 5, an enum matches its
  literal name, and a literal may be quoted (`key="line 5"`).
- It is available wherever `collectionIndex` is — on a service's `referencedResource` and on
  any feature path (`name`, `timestamp`, `valueFeature`, …) — and **takes precedence** over it.
  A malformed expression is logged and ignored, which leaves the index in charge.
- **A path may continue past the collection.** The element the filter picks is navigated on,
  so `lines` selects the entry and `value` steps into it — the three-step path above ends at
  the line itself, and the service's resources read its attributes directly.

**A filter that matches nothing is a skip, not a failure.** The service it selects for is left
untouched for that instance and everything else in the mapping is still applied; the twin keeps
whatever an earlier message put there. That is what makes one mapping able to read a reading
split over several messages — declare a service per element you ever expect, and each message
updates the ones it carries. An **index** out of bounds stays an error, because a mapping that
asks for `[4]` of a three-element list does not fit the payload at all.

> A worked example of both — a camera that reports up to four counting lines per LoRaWAN
> uplink — is in [the iotraffic example](iotraffic-counter-example.md).

### Mapping services onto collection elements

A service's `referencedResource` (a `ReferenceMapping`) does double duty: besides
[automatic resource generation](#automatic-resource-generation) it selects the **source
element for the whole service**. Its `featurePath` + `collectionIndex` decide which element
the service's timestamp, explicit `resources`, and (for the admin service) the `*Ref`
features read from. The classic pattern is two services on different indices of the same
collection — current weather from `reports[0]`, the 3-hour forecast from `reports[1]`:

```xml
<services mid="currentWeather">
  <name name="Current Weather"/>
  <timestamp>  <!-- resolved against reports[0] -->
    <featurePath xsi:type="ecore:EAttribute" href="w.ecore#//WeatherReport/timestamp"/>
  </timestamp>

  <!-- selector only: exclude="false" with no filter generates no resources -->
  <referencedResource collectionIndex="0" exclude="false">
    <featurePath xsi:type="ecore:EReference" href="w.ecore#//WeatherReports/reports"/>
  </referencedResource>

  <!-- explicit resources: valueFeature paths are relative to reports[0] -->
  <resources name="windSpeed" unit="m/s" mid="windSpeed">
    <eType xsi:type="ecore:EDataType" href="http://www.eclipse.org/emf/2002/Ecore#//EFloat"/>
    <valueFeature xsi:type="ecore:EAttribute" href="w.ecore#//MOSMIXSWeatherReport/windSpeed"/>
  </resources>
</services>

<services mid="forecast3H">
  <!-- same shape, collectionIndex="1" -->
</services>
```

Without a `referencedResource` the service reads from the root source object. With one, all
feature paths inside the service are relative to the selected element — so resource paths
start at the element type (`MOSMIXSWeatherReport/windSpeed`), not at the container. If the
selected index does not exist in an incoming instance, the whole instance fails validation,
so a two-index mapping requires at least two elements in the collection. The complete
worked example is the tests' `data/WeatherReportsProviderMapping.xmi` (its admin service
selects `reports[0]` the same way).

## Automatic resource generation

Typing out one `ResourceMapping` per attribute is tedious for rich types. A
**`ReferenceMapping`** (the `referencedResource` of a service) generates a resource for
every attribute of a target EClass automatically.

```xml
<services mid="currentWeather">
  <name name="Current Weather"/>
  <timestamp>
    <featurePath xsi:type="ecore:EAttribute" href="w.ecore#//WeatherReport/timestamp"/>
  </timestamp>

  <referencedResource xsi:type="mapping:ReferenceMapping"
      collectionIndex="0" exclude="true">
    <!-- path to the collection whose element supplies the attributes -->
    <featurePath xsi:type="ecore:EReference" href="w.ecore#//WeatherReports/reports"/>

    <!-- use this concrete subtype's attributes, not the declared base type -->
    <targetEClass href="w.ecore#//MOSMIXSWeatherReport"/>

    <!-- with exclude="true": skip these; map everything else -->
    <filter xsi:type="ecore:EAttribute" href="w.ecore#//WeatherReport/id"/>
    <filter xsi:type="ecore:EAttribute" href="w.ecore#//WeatherReport/timestamp"/>

    <!-- also generate resources for a nested reference, prefixed by its name -->
    <referenceMappings xsi:type="mapping:ReferenceMapping">
      <featurePath xsi:type="ecore:EReference"
          href="w.ecore#//MOSMIXSWeatherReport/significantWeather6Hours"/>
    </referenceMappings>
  </referencedResource>
</services>
```

**`filter` + `exclude`:**

| `exclude` | `filter` | Result |
|---|---|---|
| `true` (default) | empty | map **all** attributes |
| `true` | listed | map all **except** the listed |
| `false` | listed | map **only** the listed |
| `false` | empty | map **nothing** |

Notes:

- **`targetEClass`** matters when the feature is declared with a base type but instances are
  a subtype — without it only the base attributes are generated.
- **`referenceMappings`** (nested) generates resources from a referenced object's
  attributes, named `<referenceName><AttributeName>` (e.g.
  `significantWeather6HoursWeatherCode`).
- Generated resources inherit metadata from the source attribute's annotations (see
  [Metadata](#metadata-where-values-come-from)), so unit/description/extra come for free.
- Auto-generated resources are kept in the service's transient `temporaryResources`; they
  are regenerated on each registration and processed identically to explicit `resources`.
- The same `referencedResource` also selects the service's source element (see
  [Mapping services onto collection elements](#mapping-services-onto-collection-elements));
  with `exclude="false"` and no filter it acts as a pure selector and generates nothing.

## Admin service

The `admin` child (an `AdminMapping`, a specialized service) carries provider metadata:
friendly name, location, and the provider's EPackage. Because the `admin` feature is already
typed as `AdminMapping`, `xsi:type` is optional.

```xml
<admin mid="admin">
  <name name="Admin"/>

  <!-- friendly name from a feature path -->
  <friendlyNameFeature xsi:type="ecore:EReference" href="m.ecore#//UplinkMessage/deviceInfo"/>
  <friendlyNameFeature xsi:type="ecore:EAttribute" href="m.ecore#//DeviceInfo/deviceProfileName"/>

  <!-- the source EPackage that owns the provider type -->
  <providerPackage href="m.ecore#/"/>

  <!-- location: latitude/longitude/elevation, each a feature path -->
  <latitudeRef  xsi:type="ecore:EReference" href="w.ecore#//WeatherReport/station"/>
  <latitudeRef  xsi:type="ecore:EReference" href="w.ecore#//Station/location"/>
  <latitudeRef  xsi:type="ecore:EAttribute" href="w.ecore#//GeoPosition/latitude"/>
  <longitudeRef xsi:type="ecore:EReference" href="w.ecore#//WeatherReport/station"/>
  <longitudeRef xsi:type="ecore:EReference" href="w.ecore#//Station/location"/>
  <longitudeRef xsi:type="ecore:EAttribute" href="w.ecore#//GeoPosition/longitude"/>

  <!-- the Thing description, the one admin resource meant to be fed this way -->
  <resources name="description" mid="description">
    <eType xsi:type="ecore:EDataType" href="http://www.eclipse.org/emf/2002/Ecore#//EString"/>
    <valueFeature xsi:type="ecore:EReference" href="m.ecore#//UplinkMessage/deviceInfo"/>
    <valueFeature xsi:type="ecore:EAttribute" href="m.ecore#//DeviceInfo/deviceProfileName"/>
  </resources>
</admin>
```

Location can also be given as static `latitude` / `longitude` / `elevation` attributes
instead of `*Ref` feature paths. The location is written only when the mapping yields one: a
mapping without `latitude`/`latitudeRef`, or a payload that lacks the referenced values, leaves
`admin/location` as it is, so an adapter that knows the device position can own it.

`resources` under `admin` work like service resources (`valueFeature`, change rules, ...) with
one difference: the admin service's resources are the fixed set sensiNact defines
(`friendlyName`, `description`, `location`, `model`, `modelPackageUri`), so a mapping feeds an
existing one by its `mid` and cannot add new ones. `description` is the one to use here: it is
what the SensorThings northbound shows as the Thing's description (without it a Thing reads
"No description"), while `friendlyName` and `location` have the dedicated features above and
`model`/`modelPackageUri` are the provider's identity. A mapping naming any other `mid` is
refused at registration with a message naming it; one that bypasses the registry has that
resource skipped with a warning.

> **Timestamps on the admin service.** sensiNact keeps the newer value, and the admin
> resources are stamped with the provider's creation instant (the provider timestamp of the
> first mapped payload). An admin update older than that would be dropped silently, with
> `friendlyName` keeping the provider id, which is how a `FEATURE` timestamp on `<admin>` that
> points at the payload's own time used to lose the first payload. The mapper therefore never
> dates an admin update earlier than the provider timestamp of the same payload: the admin
> values describe the provider, not a measurement. `ValueMapperTest#providerMapping_admin`
> covers exactly that case.

## Mapping profiles

A **`MappingProfile`** declares a *target structure* that one or more mappings conform to —
so different device models (Dragino, EM310, …) can all present the same
`battery-sensor` provider shape. A profile is a separate root object, usually its own file:

```xml
<mapping:MappingProfile
    xmlns:xmi="http://www.omg.org/XMI"
    xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
    xmlns:mapping="https://fennec.eclipse.org/event.atlas/mapping/1.0"
    profileId="battery-sensor" name="Battery Sensor Profile"
    providerStrategy="UNIFIED">
  <provider providerId="battery-sensor">
    <services serviceId="battery" serviceName="Battery">
      <resources resourceId="level" resourceName="Battery Level" expectedUnit="V">
        <expectedType href="http://www.eclipse.org/emf/2002/Ecore#//EDouble"/>
      </resources>
    </services>
    <admin serviceId="admin" serviceName="Admin" requiresFriendlyName="true">
      <resources resourceId="friendlyName" resourceName="Device Name" required="false">
        <expectedType href="http://www.eclipse.org/emf/2002/Ecore#//EString"/>
      </resources>
    </admin>
  </provider>
</mapping:MappingProfile>
```

A mapping references the profile by its id (cross-file `href` to the `profileId`):

```xml
<mapping:ProviderMapping mid="em310udl-battery-sensor"> … 
  <profile href="battery-sensor-profile.xmi#battery-sensor"/>
</mapping:ProviderMapping>
```

**`providerStrategy`** governs what happens when several mappings share a profile:

- **`SEPARATE`** (default) — each mapping creates its own provider instance.
- **`UNIFIED`** — all mappings using the profile contribute to a single shared provider.

Profile services and resources carry a `required` flag (default `true`) plus
`expectedType`/`expectedUnit`, which the `MappingProfileRegistry` uses to validate that a
conforming mapping actually provides what the profile demands.

## Converting values

A feature path reads a value. A **converter** changes it on the way into the twin: Fahrenheit to
Celsius, tenths of a degree to degrees, a device's own date format to an instant.

A converter is an OSGi whiteboard service. Register a `ValueConverter` with the service property
`event.atlas.converter.id`, and a mapping selects it by that id:

```java
@Component(property = ValueConverter.CONVERTER_ID + "=my-converter")
public class MyConverter implements ValueConverter {
    public Object convert(Object value) { … }
}
```

```xml
<resources mid="temperature">
  <eType xsi:type="ecore:EDataType" href="http://www.eclipse.org/emf/2002/Ecore#//EDouble"/>
  <valueMapping converterId="f2c">
    <featurePath xsi:type="ecore:EAttribute" href="m.ecore#//Reading/tempF"/>
  </valueMapping>
</resources>
```

`converterId` lives on `FeatureMapping`, so it works wherever one appears: a `ValueMapping`, a
`NameMapping` and a `TimestampMapping`. The converter runs on what the **feature path**
produced, and before the value is read as the resource's `eType` — so a converter may return a
different type than it was handed.

Two outcomes are worth knowing:

- **An unknown id passes the value through unconverted**, and the id is logged once. A typo
  does not empty a resource, but it does publish an unconverted value.
- **A converter that throws, or returns `null`, drops the value.** The resource keeps what it
  had.

### The two converters that ship here

Both are factory components with `configuration-policy=require`, so a runtime that configures
none publishes no converter at all.

| factory pid | properties | what it does |
|---|---|---|
| `event.atlas.converter.linear` | `factor`, `offset` | `value * factor + offset` |
| `event.atlas.converter.instant` | `pattern` | reads a value as an instant |

```json
"event.atlas.converter.linear~f2c": {
    "event.atlas.converter.id": "f2c",
    "factor": 0.5555555555555556,
    "offset": -17.77777777777778
}
```

One configurable component beats a drawer of named arithmetic: millivolts to volts is
`factor = 0.001`, and tenths of a unit is `factor = 0.1`.

`event.atlas.converter.instant` is for a **resource value** that holds a date.
`TimestampMapping.hint` already covers a timestamp, and it needs nothing deployed.

### Converters on generated resources

A `ReferenceMapping` generates one resource for each attribute of the referenced type, so a
converter named there applies to all of them — rarely what you want, because a
Fahrenheit converter would also be handed the humidity. Name the attributes in a binding:

```xml
<referencedResource>
  <featurePath xsi:type="ecore:EReference" href="m.ecore#//Station/reading"/>
  <bindings converterId="f2c">
    <attributes href="m.ecore#//Reading/temperature"/>
  </bindings>
</referencedResource>
```

A binding with an empty `attributes` list is the default for the whole reference. The nearest
declaration wins, as it does for `changeRule`, `deletionRule` and `unit`.

## Constant values

A `valueMapping` may carry a constant in its `value`. The producer answers first — a
[function](#functions), or the feature path — and the constant answers when it yields nothing,
because the resource declares neither, or because this payload does not carry the feature:

```xml
<resources mid="source">
  <eType xsi:type="ecore:EDataType" href="http://www.eclipse.org/emf/2002/Ecore#//EString"/>
  <valueMapping value="field-gateway"/>
</resources>
```

The constant is read as the resource's `eType`, so `value="42"` on an `EInt` resource gives the
integer 42. A constant that does not fit the type is dropped, and the warning names it.

A converter does **not** touch a constant: it is already written in its final form. The same
holds for a `NameMapping`'s `name` and a `TimestampMapping`'s `timestamp`.

## Functions

A converter changes a value. A **function** produces one: the mean of two sensors, a name built
from several fields, anything a feature path cannot reach.

A function is an OSGi whiteboard service. Register a `ValueFunction` with the service property
`event.atlas.function.id`, and a mapping selects it by that id:

```java
@Component(property = ValueFunction.FUNCTION_ID + "=mean-temperature")
public class MeanTemperature implements ValueFunction {
    public Object apply(Object input) {
        EObject reading = (EObject) input;
        return (…);
    }
}
```

**What the function receives depends on the mapping:**

| the mapping has | the function is handed |
|---|---|
| `functionId` only | the whole source EObject, and it reads what it needs |
| `functionId` **and** a `featurePath` | the value that path produced |

```xml
<!-- a producer: it reads the source object itself -->
<valueMapping functionId="mean-temperature"/>

<!-- a transformer: it is handed reading/tempA -->
<valueMapping functionId="double">
  <featurePath xsi:type="ecore:EAttribute" href="m.ecore#//Reading/tempA"/>
</valueMapping>
```

`functionId` lives on `FeatureMapping`, so it works for a `ValueMapping`, a `NameMapping` and a
`TimestampMapping` alike. A provider id computed from several fields is a `NameMapping` with a
`functionId` and no path.

**The order is function, then converter.** A mapping may set both: the function computes the
value and the converter post-processes it.

**A function that produces nothing leaves the mapping's own fallback to answer** — a
`NameMapping`'s literal, a `TimestampMapping`'s literal, a `ValueMapping`'s constant. This
happens when the function returns `null`, when it throws, or when **no function is registered
for the id**.

> That last case differs from a converter on purpose. An unknown *converter* id passes the value
> through, because an unconverted reading is still a reading. An unknown *function* id produces
> nothing, because a function may be handed the whole source object, and there is no sensible
> identity answer for that.

> **`TimestampStrategy.FUNCTION` still does nothing.** The enum is in the metamodel and the
> engine never reads it. A timestamp computed by a function is a `<timestamp functionId="…">`,
> with no `strategy`.

## Expressions (OCL)

A function needs Java code and a deployed bundle. An **expression** needs neither. You write it
in the mapping, in OCL, and it reads the payload.

```xml
<!-- Fahrenheit from a Celsius payload. No converter and no function. -->
<resources mid="fahrenheit" name="fahrenheit" eType="ecore:EDataType …#//EDouble">
  <valueMapping expression="self.temperature * 1.8 + 32"/>
</resources>
```

Put the expression on the `valueMapping`. This is the same element that holds a `converterId` or
a collection selection. A `ResourceMapping` has no `expression` of its own. It has no
`featurePath` of its own either.

**`self` is the payload object.** A `featurePath` starts at the same object. The expression can
read all data in the payload. This is the purpose of an expression:

| Expression | Result |
|---|---|
| `self.temperature * 1.8 + 32` | arithmetic on a feature |
| `self.reports->size()` | a count; a feature path cannot give you one |
| `self.reports->first().windSpeed` | an element, then a feature of that element |
| `self.reports->select(r \| r.windSpeed > 10)->size()` | a count of the elements that agree with a condition |
| `if self.battery < 10 then 'low' else 'ok' endif` | a condition |
| `self.device.toUpperCase()` | the OCL string library |

`expression` is an attribute of `FeatureMapping`. Thus you can use it in a `ValueMapping`, a
`NameMapping` and a `TimestampMapping`. To calculate a provider id from more than one feature,
write `<name expression="…"/>`.

### The sequence of the three producers

**An expression has priority over a `functionId`. Both have priority over a `featurePath`.**

| The mapping has | The value comes from |
|---|---|
| `expression` | the expression. The engine ignores a `featurePath` in the same element. |
| `functionId`, but no expression | the function. The function gets the value of the path, or the source object if there is no path. |
| only `featurePath` | the path |

A `converterId` then changes the value that the first producer gives. Thus an expression and a
converter operate together.

**If a producer gives no value, the engine does not use the next producer.** The literal of the
mapping gives the value: the constant of a `ValueMapping`, the `name` of a `NameMapping`, or the
`timestamp` of a `TimestampMapping`. If there is no literal, the resource reports no value for
this payload. The engine never reads the feature path that is adjacent to an expression. The
expression replaces that path.

### How to deploy it

The mapping engine declares the port and no more. The implementation is the bundle
**`org.eclipse.fennec.event.atlas.mapping.ocl`**. This bundle brings the OCL engine, the parser
and the model bundles with it. The development runtime (`launch.bndrun`) and the docker image
contain it.

**In a runtime without this bundle, each expression gives no value.** There is no error, and the
ingest does not fail. The other parts of the mapping operate as before. The chat adapter for
model inference has the same structure: one bundle is the only bundle that needs the large
stack.

### When an expression gives no value

An expression cannot cause the failure of a payload. For each of these conditions, the
expression gives no value, and the literal of the mapping gives the value:

- **The engine cannot parse the expression.** An expression that names a feature of a different
  type is also a parse error, because OCL is a typed language. The engine writes one log message
  with the position of the error. Then it keeps the result, and it does not parse the same text
  again for the same **model version**.

  The model version is the fingerprint of the EPackage, which `MetadataService` gives. It is a
  digest of the content of the model. Thus:

  | You change | Result |
  |---|---|
  | the mapping | The text is different. The engine parses it with the next payload. |
  | the model, and you register the package again | The fingerprint is different. The engine parses the text again with the next payload. |
  | nothing | The engine does not parse the text again. |

  Two copies of one nsURI with different content are two model versions. They do not share a
  result. Two copies with identical content are one model version, and they share one result.
  If a runtime has no metadata service, the key is the nsURI, and all content under that nsURI
  shares one result.
- **The expression gives the OCL value `invalid` or `null`.** An example is a feature that has
  no value.
- **The expression causes an exception.**

### Two conditions for the data types

- **The OCL type `Real` is a Java `Double`.** If you write `self.temperature * 1.8` for a
  resource with the type `EInt`, the resource gets **no value**. The engine removes the value and
  writes a log message. It does not round the value. Use `EDouble` or `EFloat`. A converter that
  scales a value has the same condition.
- **`tokenize` removes an empty part.** It uses `StringTokenizer`. Thus `'a//b'` gives two parts,
  not three.

### Where the engine does not read an expression

`expression` is an attribute of `FeatureMapping`. Two elements look applicable, but the engine
does not read an expression in them:

- **`referencedResource` (a `ReferenceMapping`)** selects the EObject for the generated service.
  It uses only its `featurePath` with `collectionIndex` or `collectionFilter`. An expression in
  this element would be a more general `collectionFilter`. That is a different function, and it
  is not available.
- **The admin elements `latitudeRef`, `longitudeRef` and `friendlyNameFeature`**, and the other
  `*Ref` lists, are feature paths. They are not `FeatureMapping` elements. Thus they have no
  `expression`, no `functionId` and no `converterId`.

### The available data

`self` is the only variable. An expression cannot read the source of the ingest, such as the
MQTT topic or the REST channel. If a payload must know its source, the payload model must
contain it. `allInstances()` has no model extent, because the engine maps one payload at a time.

## Persistence rules (history control)

> **Status: change rules are enforced on the way *into* the twin, as an interim measure.**
> The rules describe what the SensiNact *history provider* should persist, and the intended
> enforcement is a notification proxy between the twin and the history store (see
> `WP-SN-2-persistence-rules-plan.md`). Until that exists, the mapping engine applies the
> change rules when a payload is mapped: a value its rule rejects never reaches the resource.
>
> **This also freezes the twin's live value at the last accepted one** — under
> `TimeThrottleChangeRule` a northbound read returns a value up to one interval old, and under
> `CountChangeRule` one of every *n* readings. Set `enabled=false` on the configuration pid
> `sensinact.mapping.changerule.filter` to push every value again.
>
> **Deletion rules are still model-only.** Nothing purges history yet; there is no purge API
> to call.

By default the history provider persists *every* value change of *every* resource. Persistence
rules let you control this **per resource**: how often a change is worth storing, and how long
to keep it.

A rule is **contained by the resource that uses it** — written inline, owned by that resource
alone. Two resources that behave alike each carry their own copy, which means changing a
threshold is an edit in every place that uses it. The upside is that a mapping is
self-contained: it carries its rules wherever it travels, including through a Model Atlas,
with nothing to resolve and nothing to deploy alongside it.

### Writing a rule on a resource

```xml
<resources mid="temperature" unit="F">
  <eType xsi:type="ecore:EDataType" href="http://www.eclipse.org/emf/2002/Ecore#//EDouble"/>
  <valueFeature xsi:type="ecore:EAttribute" href="../ecowitt.ecore#//EcoWittWeather/temperaturOutdoor"/>
  <changeRule   xsi:type="mapping:PercentageChangeRule" percentage="5.0"/>
  <deletionRule retention="365" retentionUnit="DAYS" cleanupInterval="7" cleanupIntervalUnit="DAYS"/>
</resources>
```

**`changeRule` needs an `xsi:type`.** Its declared type `ChangeRule` is abstract, so EMF has no
class to instantiate without one and the file fails to load with *"Class 'ChangeRule' is not
found or is abstract"* — the same rule that applies to `featurePath` and `valueFeature`.
`deletionRule` is concrete and needs none.

The optional `id`, `name` and `description` on a rule are documentation — they show up in log
messages. Nothing resolves them: a rule is reached through its container, never by id.

### A catalogue of rule shapes

`PersistenceRuleRegistry` is a plain container for rules, useful for keeping the shapes an
installation has agreed on in one file to copy from:

```xml
<mapping:PersistenceRuleRegistry
    xmlns:xmi="http://www.omg.org/XMI"
    xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
    xmlns:mapping="https://fennec.eclipse.org/event.atlas/mapping/1.0">
  <changeRules xsi:type="mapping:PercentageChangeRule"   id="pct-5"        percentage="5.0"/>
  <changeRules xsi:type="mapping:AbsoluteChangeRule"     id="abs-2"        delta="2.0"/>
  <changeRules xsi:type="mapping:CountChangeRule"        id="every-5"      n="5"/>
  <changeRules xsi:type="mapping:TimeThrottleChangeRule" id="throttle-10m" interval="10" intervalUnit="MINUTES"/>
  <deletionRules id="keep-90d" retention="90"  retentionUnit="DAYS" cleanupInterval="1" cleanupIntervalUnit="DAYS"/>
  <deletionRules id="keep-1y"  retention="365" retentionUnit="DAYS" cleanupInterval="7" cleanupIntervalUnit="DAYS"/>
</mapping:PersistenceRuleRegistry>
```

**Nothing reads that file.** It is not a source the runtime resolves against, and it must not
go into a runtime's mappings directory — the mapping registry will report it as content it
cannot use. `model/examples/EcoWittPersistenceMapping.xmi` shows the same rules written where
they take effect.

An `href` to a rule in another document is what the previous model wanted, and it now leaves an
unresolved proxy behind. That proxy carries the rule's type but none of its parameters, so the
runtime reports it as inapplicable and pushes the resource unfiltered rather than silently
applying a zero threshold — the log message names the mapping, the resource and the href.

### Change rules — "store this change only if…"

Each is a concrete subtype carrying exactly its parameter, written inline on the resource or
binding that uses it. Comparisons are against the **last stored** value, and the first value of
a resource is always stored.

| Type | Parameter | Stores when |
|---|---|---|
| `PercentageChangeRule` | `percentage` | the value moved ≥ *percentage* % vs. the last stored value |
| `AbsoluteChangeRule` | `delta` | the absolute change ≥ *delta* |
| `CountChangeRule` | `n` | one out of every *n* notifications (`n=1` = every change) |
| `TimeThrottleChangeRule` | `interval` + `intervalUnit` | at most once per interval (drops the in-between changes) |

### Rules on a reference mapping (bindings)

A `ReferenceMapping` generates one resource per attribute of the referenced type, so those
resources exist nowhere in the XMI and there is no `ResourceMapping` to hang a rule on. The
reference's **bindings** fill that gap: each binding names the attributes it applies to — the
same way `filter` does — and carries their rule.

```xml
<referencedResource xsi:type="mapping:ReferenceMapping" collectionIndex="0" exclude="true">
  <featurePath xsi:type="ecore:EReference" href="../weather.ecore#//WeatherReports/reports"/>
  <targetEClass href="../weather.ecore#//MOSMIXSWeatherReport"/>

  <!-- named attributes: their own rule, and a unit overriding the source annotation -->
  <bindings unit="kn">
    <attributes href="../weather.ecore#//MOSMIXSWeatherReport/windSpeed"/>
    <changeRule xsi:type="mapping:PercentageChangeRule" percentage="5.0"/>
  </bindings>

  <!-- no attributes named: the default for everything else generated here -->
  <bindings>
    <changeRule xsi:type="mapping:TimeThrottleChangeRule" interval="10" intervalUnit="MINUTES"/>
  </bindings>
</referencedResource>
```

- A binding with an **empty `attributes` list** applies to every resource generated at that
  level — the convention `filter` uses, and the way to declare a default for a whole reference.
- Bindings **cascade into nested `referenceMappings`**, and the nearest declaration wins: a
  binding on the nested mapping overrides one inherited from its parent.
- `changeRule`, `deletionRule` and `unit` resolve **independently**, so a binding can override
  the rule of one attribute and leave its unit to the level above.
- An attribute named by two bindings resolves to the first; the duplicate is logged.

Resolution happens when the resources are generated, so a generated resource is
indistinguishable from a hand-written one by the time the engine reads it. A binding's rule is
**copied** onto each resource it applies to, not shared with them — one binding is still the
single place you edit it, which is what makes a binding worth using for a rule that covers
many resources.

**Pick scale-free rules for a default.** A binding without named attributes covers resources
of mixed units and magnitudes — temperature, pressure, a counter. `PercentageChangeRule`,
`CountChangeRule` and `TimeThrottleChangeRule` generalize over those; an `AbsoluteChangeRule`
almost never does, since `delta="2.0"` means something different for each of them.

`unit` is there for the cases the source model cannot cover: a domain `.ecore` you cannot
annotate, or a mapping that publishes a different unit than the model declares (an attribute
annotated `F` exposed as `C` after conversion). Otherwise a generated resource takes its unit
from the source attribute's `sensinact.mapping` annotation, which is copied along with it.

### Deletion rules — retention & cleanup

`DeletionRule` controls purging of stored data: `retention` (+`retentionUnit`) is the age past
which data is deleted; `cleanupInterval` (+`cleanupIntervalUnit`) is how often cleanup runs;
optional `maxCount` caps the number of samples kept.

### Durations

Durations are an **integer amount + a `DurationUnit`** — one of `MILLISECONDS`, `SECONDS`,
`MINUTES`, `HOURS`, `DAYS` (e.g. `interval="10" intervalUnit="MINUTES"`). There is no custom
duration datatype, so these serialize as plain XMI attributes.

## Runtime & OSGi

The engine is plain Java (`ValueMapper` / `ValueMapperFactory`), but the normal deployment
is service-driven:

- Put each `ProviderMapping` into the named EObject registry **`sensinact-mappings`**
  (emf.osgi `org.eclipse.fennec.emf.osgi.eobject.registry`; entry keys = the mapping's
  `mid`). `ProviderMappingRegistryImpl` (config pid `sensinact.southbound.emf.mapping`)
  is an `EObjectRegistryListener` whiteboard service on that registry: the registry
  replays the current content when it binds, the facade validates every entry (`mid`
  present, provider classes resolved — invalid entries are skipped with a log,
  uniformly for every content source), indexes it by `providerClasses` and builds the
  provider model in the twin on the SensiNact `GatewayThread`. Content reaches the
  registry through its providers — local files via emf.osgi's `FileEObjectProvider`
  (the registry's initial provider), a Model Atlas via the atlas provider — or
  programmatically via `registerModelMapping`.
- **Replacing a mapping under an unchanged `mid` rebuilds its model.** Every content
  change — a re-read file, an edited object syncing in from a Model Atlas — arrives under
  the same key, so the old mapping's model is dropped *before* the new one is registered.
  That is deliberate in both directions: registering first would have deleted the model the
  new mapping had just been mapped onto (both mappings answer the same provider id), and
  dropping first is also what makes a removed service or resource actually disappear, since
  a registration only ever adds to a model it finds. The cost is a brief window in which a
  payload for that provider finds no mapping, and the provider instance itself is re-created
  by the next push. A mapping onto a **shared** provider (`providerStrategy` `UNIFIED`) is
  the exception: its model survives as long as another mapping is still registered onto it.

- `MappingProfile`s flow the same way through the registry **`sensinact-profiles`**
  (entry keys = `profileId`) into `MappingProfileRegistryImpl`, which validates
  conformance.
- Feed data in through the **`InstancePusher`** service: `pushInstance(EObject)` looks up
  all registered mappings for the instance's EClass and applies each on the gateway thread,
  returning the number of mappings applied (`0` = nothing registered for that EClass;
  individual mapping failures are logged and skipped). This is the intended ingress for
  southbound connectors — receive a payload, deserialize it to an EObject, push.
- The payload EPackages (your domain models) must be registered in the runtime so both the
  mapping XMI and the incoming instances resolve. **The registry lookup keys on EClass
  identity**: incoming instances must be built against the same EPackage instance the
  mappings resolved their `providerClasses` from — when in doubt, take it from the mapping
  itself (`mapping.getProviderClasses().get(0).getEPackage()`).

Programmatic use (tests, embedding) goes through the factory, always on the gateway thread:

```java
ValueMapper mapper = ValueMapperFactory.createValueMapper(twin, mapping);

ValueMapper.ValidationResult v = mapper.validateInstance(sourceInstance);
if (!v.isValid()) throw new ValueMappingException(v.getErrors().toString());

mapper.mapInstance(sourceInstance);                 // update the twin
Map<String,Object> values = mapper.mapResourceValues(sourceInstance);  // inspect without updating
```

## Gotchas

- **Namespace URI must match exactly** — `https://fennec.eclipse.org/event.atlas/mapping/1.0`.
  A stale URI in an XMI fails to load with `PackageNotFoundException`.
- **Tag `xsi:type` on `EStructuralFeature` refs** (`featurePath`, `valueFeature`,
  `unitFeature`, admin `*Ref`) — otherwise EMF cannot resolve attribute vs. reference.
- **A `collectionFilter` that matches nothing skips its service** — silently, by design; an
  out-of-bounds `collectionIndex` is still an error. See
  [Selecting by content](#selecting-by-content-collectionfilter).
- **Admin resource reads are incomplete** — see the note under [Admin service](#admin-service).
- **Metadata precedence** — an explicit `unit`/`extraMetadata` on the resource overrides the
  Ecore annotation; leave them off to let model annotations flow through.
- **A converted value needs a floating-point `eType`** — a scaling converter produces a
  `Double`, and a resource typed `EInt` reports **nothing at all** for it. The value is dropped
  and logged, not rounded. Declare `EDouble` or `EFloat` for anything a converter scales.
- **`TimestampStrategy` does nothing** — the enum is in the metamodel and is never read. See
  [Timestamps](#timestamps).
- **An expression needs its bundle** — if you do not deploy `…event.atlas.mapping.ocl`, each
  `expression` gives no value, and there is no error message. See
  [Expressions (OCL)](#expressions-ocl).
- **The engine does not parse a bad expression again for the same model version** — it keeps
  the text with the fingerprint of the EPackage. A correction of the mapping or a new version
  of the model gives a new key. A restart is not necessary for either.
- **An unknown `functionId` produces no value, an unknown `converterId` passes the value
  through** — see [Functions](#functions) for why they differ.

## Reference: metamodel at a glance

| Type | Key features |
|---|---|
| `ProviderMapping` | `mid`, `providerClasses`, `providerTimestamp`, `name`, `timestamp`, `services`, `admin`, `profile` |
| `ServiceMapping` | `mid`, `name`, `timestamp`, `resources`, `referencedResource` |
| `ResourceMapping` | `mid`, `name`, `unit`, `eType`, `valueFeature`, `valueMapping`, `unitFeature`, `timestamp`, `extraMetadata`, `defaultValueLiteral`, `changeRule`, `deletionRule` |
| `AdminMapping` | `friendlyName`/`friendlyNameFeature`, `latitude`/`latitudeRef` (+ longitude, elevation), `providerPackage` |
| `FeatureMapping` (abstract) | `featurePath`, `collectionIndex`, `collectionFilter`, `expression` (OCL), `functionId`, `converterId` |
| `NameMapping` | the above + `name` |
| `ValueMapping` | the above + `value` (a constant) |
| `TimestampMapping` | the above + `hint`, `timestamp`, `strategy` (not read) |
| `ReferenceMapping` | `featurePath`, `targetEClass`, `filter`, `exclude`, `referenceMappings`, `collectionIndex`, `collectionFilter`, `bindings` |
| `ReferenceResourceBinding` | `attributes` (empty = all), `changeRule`, `deletionRule`, `unit`, `converterId` |
| `MappingProfile` | `profileId`, `name`, `version`, `providerStrategy`, `provider` |
| `ProfileProvider` / `ProfileService` / `ProfileResource` / `ProfileAdmin` | `*Id`, `required`, `expectedType`, `expectedUnit`, `requiresLocation`, `requiresFriendlyName` |
| `PersistenceRuleRegistry` | `changeRules` (containment), `deletionRules` (containment) |
| `PercentageChangeRule` / `AbsoluteChangeRule` / `CountChangeRule` / `TimeThrottleChangeRule` | `id`, `name`, `description` + `percentage` / `delta` / `n` / (`interval` + `intervalUnit`) |
| `DeletionRule` | `id`, `retention` + `retentionUnit`, `cleanupInterval` + `cleanupIntervalUnit`, `maxCount` |
