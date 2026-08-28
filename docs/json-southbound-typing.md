# JSON southbound typing — configuration changes (2026-08-24)

This documents the configuration changes that switched the waterpark southbound
channel from XMI to JSON, and the codec-typing scheme that every JSON device
family in this runtime now shares. The corresponding producer-side changes live
in the waterpark repository (`DataInMotion/waterparc`, PR #2: JSON sink,
`typeMapping` annotations and the `deduplicationId` attribute in
`waterpark_domain.ecore`).

## Why JSON needs typing configuration at all

XMI payloads name their model themselves (nsURI in the root element), so the
ingest can deserialize them with no extra configuration. JSON does not — the
Fennec codec has to resolve the root EClass from a **discriminator value**
found somewhere in the payload. Three constraints of the codec shape everything
below (verified against `org.eclipse.fennec.codec` 0.1.0-SNAPSHOT sources):

1. **One type-map registry per ingest.** `PayloadIngestImpl` passes a single
   `codec.typeMapId` load option to every JSON load
   (pid `event.atlas.southbound.ingest`); there is no per-channel override.
2. **One discriminator path per registry.** `TypeDiscriminatorRegistry` stores
   a single `discriminatorPath` for all classes of a mapId — registering a
   second path logs a warning and overwrites the first.
3. **No `_type` fallback once discriminator models exist.** With any
   discriminator-annotated class registered, the root deserializer scans
   *only* the discriminator path (`FeaturePathTypeResolver`); if the value is
   missing or unknown, the payload is dropped
   (`featurePath resolution failed … no concrete fallback available`).

Consequence: **every JSON device family in one runtime must share the same
mapId and the same discriminator path.** The real M5AirQ device dictates the
path — its payloads carry `deduplicationId` at the root — so the waterpark
simulator injects the same field.

## The shared registry: `jena-sensors`

The registry id was renamed from the historical `m5airq` to the neutral
`jena-sensors`, since it is deployment-wide, not device-specific. It must
match in three places:

| Where | What |
|---|---|
| `…mapping.local.config/configs/config.json` | `"event.atlas.southbound.ingest": { "codec.typeMapId": "jena-sensors" }` |
| `…mapping.tests/model/m5airq.ecore` | `eAnnotations source="http://eclipse.org/fennec/codec/typeMapping/jena-sensors"` on `AirQualityMessage` and `StatusMessage` |
| `waterpark_domain.ecore` (waterpark repo) | same annotation source on `WaterTemperature` and `WaterQuality` |

Registered discriminator values (path `deduplicationId`, `fallbackStrategy`
`SKIP` everywhere):

| Value | EClass |
|---|---|
| `airquality-sensor-data` | `m5airq#//AirQualityMessage` |
| `airquality-status` | `m5airq#//StatusMessage` |
| `waterpark-water-temperature` | `waterpark_domain#//WaterTemperature` |
| `waterpark-water-quality` | `waterpark_domain#//WaterQuality` |

## config.json changes (local.config)

```json
"event.atlas.southbound.mqtt~waterpark": {
  "mqttTopics": ["waterpark/sensors/#"],
  "mqtt.handler.id": "waterpark-broker",
  "format": "json",          // was: "xmi"
  "name": "waterpark-mqtt"
},

"event.atlas.southbound.ingest": {
  "codec.typeMapId": "jena-sensors"   // was: "m5airq"
}
```

`codec.useNamesFromExtendedMetadata` stays at its default (`true`) — the
waterpark feature names are the wire names already, the m5airq model maps its
wire keys (`object`, `voc_index`, `motion_detected`, …) via ExtendedMetaData.

After editing the config, rebuild the resource-only bundle so the dev runtime
picks it up:

```bash
./gradlew :org.eclipse.fennec.event.atlas.mapping.local.config:assemble
```

## Model Atlas prerequisites

The runtime resolves both domain EPackages from the Model Atlas
(scope `jena`, stage `release`, EAGER prefetch). Two operational rules learned
the hard way:

- **Annotation changes require a re-upload.** The codec reads the
  `typeMapping` annotations from the EPackage *the runtime registered* — a
  stale Atlas copy silently breaks typing (`No EClass found for
  discriminator: …` although the value was found in the payload).
  Schema uploads go draft → approved → release:

  ```bash
  curl -X POST ".../jena/schema/stages/draft?nsUri=<enc>&name=<n>&version=<v>" \
       -H "Content-Type: application/xmi" --data-binary @model.ecore
  curl -X POST .../jena/schema/stages/draft/actions/transition \
       -H "Content-Type: application/json" \
       -d '{"objectId": "<uuid>", "targetStage": "approved"}'
  # then approved -> release the same way
  ```

- **Never leave two release objects with the same nsUri.** The schema registry
  allows it, and both `DELETE ?nsUri=…` and the client's prefetch then pick an
  arbitrary one (`matches.get(0)`). If duplicates exist, delete by nsUri until
  none remain and upload the desired version fresh.

The provider mappings live in the `sensinactmapping` registry (keyed by `mid`,
so a draft → release re-upload *replaces* the entry — no duplicate problem
there).

## Related fix: enum-typed resources break SensorThings

`WaterQualityProviderMapping.xmi` (waterpark repo, uploaded to the Atlas as
1.1.0) now types the `status` resource as `EString` instead of the
`WaterQualityStatus` EEnum. With a *dynamic* domain EPackage, an enum-typed
resource stores the raw `EEnumLiteralImpl` in the twin — sensinact warns
(`EEnumLiteralImpl has no matching EClassifier`) and the SensorThings
Observation serializes as endlessly recursive, invalid JSON. The engine's
`ValueMapperImpl.convertValue()` converts an `Enumerator` to its name, so the
string-typed resource reports plain `"OK"/"WARN"/"CRIT"`. Rule of thumb:
**mapping resources for dynamic-EMF enums should declare `EString`.**

## Verified end to end

Sim → MQTT `waterpark/sensors/#` (JSON with `deduplicationId`) →
`PayloadIngest` (`Pushed payload … 1 mapping(s) applied`) → twin →
SensorThings v1.1: 8 Things (4 pools × temperature + quality), live
observations for `ph`, `freeChlorine`, `redox`, `temperature`, and `status`
as a proper string. The airquality channel (`format: json` all along) keeps
working through the same shared registry.

Caveat when stopping the dev runtime: killing the Gradle process orphans the
bnd launcher, which keeps port 8080 — a restarted runtime then fails its
`eventHttp` connector (`Failed to bind`) while the *old* instance keeps
answering. Kill the launcher process (`pgrep -f biz.aQute.launch`) before
restarting.
