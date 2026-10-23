# Worked example: a line-counting camera that splits its report

The iotec **iotraffic** camera counts objects crossing configured lines — people, bicycles,
cars, buses, trucks, motorbikes, trains — and reports the counts of a time window over
LoRaWAN. Two properties of that payload are what this example is about, because neither can be
mapped by position:

- **The lines are a JSON object, not a list.** They are keyed `line_1`, `line_2`, … and the
  decoder writes them in no fixed order — one uplink carries `line_1, line_3, line_4, line_2`.
- **A window is split over several uplinks.** A LoRaWAN payload holds at most four lines, so
  nine lines arrive as three uplinks, each with a different subset.

The same camera also reports the health of its AI module (CPU and GPU temperature, load,
uptime, supply voltage) as a second message type on a second port, through the same topic.

Everything below is in the repository: the model is
`org.eclipse.fennec.event.atlas.mapping.tests/model/iotraffic.ecore`, the mappings and the
recorded payloads are in `…mapping.tests/data/iotraffic/`, and `IotrafficMappingTest` is the
end-to-end test.

## The model: lines are a map

```xml
<eClassifiers xsi:type="ecore:EClass" name="CounterData">
  …
  <eStructuralFeatures xsi:type="ecore:EReference" name="lines" upperBound="-1"
      eType="#//LineEntry" containment="true"/>
</eClassifiers>

<eClassifiers xsi:type="ecore:EClass" name="LineEntry" instanceClassName="java.util.Map$Entry">
  <eStructuralFeatures xsi:type="ecore:EAttribute" name="key" eType="…#//EString"/>
  <eStructuralFeatures xsi:type="ecore:EReference" name="value" eType="#//TrafficLine"
      containment="true"/>
</eClassifiers>
```

`instanceClassName="java.util.Map$Entry"` is what makes this an **EMF map**: the Fennec codec
reads a JSON object into it, each member name becoming an entry key. Without it the codec would
expect a JSON array. The entries are ordinary `EObject`s with a `key` and a `value` feature,
whether the package is generated or — as here and as a Model Atlas delivers one — loaded
dynamically from the `.ecore`.

The two message types are separate root classes, each with its own discriminator:

```xml
<eClassifiers xsi:type="ecore:EClass" name="CounterUplink" eSuperTypes="#//IotrafficUplink">
  <eAnnotations source="http://eclipse.org/fennec/codec/typeMapping/iotraffic">
    <details key="typeDiscriminator" value="counter_data"/>
    <details key="typeDiscriminatorPath" value="object.message_type"/>
    <details key="fallbackStrategy" value="SKIP"/>
  </eAnnotations>
  …
```

## The mapping: one service per line

Each line becomes a service of the camera's provider, and its `collectionFilter` says which
line, by key:

```xml
<services mid="line5">
  <name name="Line 5"/>
  <referencedResource xsi:type="mapping:ReferenceMapping" collectionFilter="key=line_5">
    <featurePath xsi:type="ecore:EReference" href="…iotraffic#//CounterUplink/object"/>
    <featurePath xsi:type="ecore:EReference" href="…iotraffic#//CounterData/lines"/>
    <featurePath xsi:type="ecore:EReference" href="…iotraffic#//LineEntry/value"/>
  </referencedResource>
</services>
```

Three things are doing work here:

1. **The filter selects the entry** — `value.line_number=5` would do the same, reading the
   camera's own line number instead of the key the decoder derives from it.
2. **The path continues past the collection**: `value` steps from the selected entry into the
   line, so the service's source *is* the line and its resources are the line's attributes.
3. **No `filter` and no `exclude`** means every attribute of `TrafficLine` becomes a resource —
   `person`, `bicycle`, `car`, `bus`, `truck`, `motorbike`, `train`, `total`, plus
   `line_number` — carrying the units and descriptions annotated on the model.

The twin that comes out:

```
iotraffic-camera / 37a151dd238fb2e7
├─ admin   friendlyName "iotraffic Camera Test", location from the receiving gateway
├─ window  from "21:36", to "22:56"
├─ line1   person 5, bicycle 2, bus 1, total 8
├─ …
└─ line9   person 20, car 2, bus 1, total 23
```

**The split is handled by the filter matching nothing.** The uplink carrying lines 5 to 8 has
no `line_1`, so `line1`'s filter selects nothing, that service is skipped, and the values the
previous uplink put there stay. Declare a service per line the camera is configured for; every
uplink updates the ones it brought.

## Two message types, one provider

The health report is its own root class and its own mapping — and lands on the **same**
provider, because both mappings reference a profile with `providerStrategy="UNIFIED"`:

```xml
<mapping:MappingProfile profileId="iotraffic-camera" providerStrategy="UNIFIED" …>
  <provider providerId="iotraffic-camera">
    <admin serviceId="admin" serviceName="Admin" required="true"
           requiresLocation="true" requiresFriendlyName="true"/>
  </provider>
</mapping:MappingProfile>
```

The provider *id* comes from each mapping's `name` (the `devEui`, the same for both), the
provider *model* from the profile — so one camera is one thing in the twin, with the counting
services and a `status` service beside them. Without the profile the two mappings would answer
the same provider id under two different models, which the twin cannot hold.

## Getting the payload typed

A ChirpStack uplink carries a fresh UUID in `deduplicationId`, so the runtime-wide discriminator
path of a shared type-map registry (see [JSON southbound typing](json-southbound-typing.md))
can never place it. The channel declares what it receives instead:

```json
"event.atlas.southbound.ingest": {
  "codec.rootType": [
    "iotraffic/#=https://eclipse.org/fennec/lorawan/iotraffic#//CounterUplink"
  ]
}
```

That is a **fallback**, which is exactly what this camera needs: both message types arrive on
one topic, the `object.message_type` discriminator tells them apart, and the configured root
type only catches what the discriminator cannot place.

## What it does not do

- **Counts are per window, not cumulative.** A resource holds what the last uplink reported for
  that line, and the `window` service says for which window. Consumers aggregating over time
  read the history, not the live value.
- **The camera reports no position**, so `admin/location` is the position of the receiving
  gateway. With several gateways in range that is the first one in `rxInfo`, which can move
  between uplinks; set a fixed `latitude`/`longitude` on the admin mapping if that matters.
- **A line the camera is not configured for is simply never filled.** Its service exists in the
  twin and stays empty, so trim the mapping to the lines in use.
