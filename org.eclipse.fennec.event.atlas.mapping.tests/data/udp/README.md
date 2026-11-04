# The UDP mappings — progress

The Jena urban-data-platform adapters `de.jena.udp.atlas.ilsa` and
`de.jena.udp.atlas.traficam`, rewritten as SensiNact mapping XMIs. They are the first use case
for the issue #91 features, and the exercise is run step by step: write the mapping as far as
the engine goes, record what cannot be expressed, implement that, write the mapping again.

The analysis behind each line is in `docs/udp-adapter-mapping-gaps.md`.

## Where the four flows stand, 2026-11-04

| flow | mapping | state |
|---|---|---|
| ILSA signal state | `ilsa-signal-state-mapping.xmi` | **complete**, except the provider id |
| ILSA configuration | `ilsa-configuration-mapping.xmi` | **complete** |
| TrafiCam configuration | `traficam-config-mapping.xmi` | two gaps: the provider id, the admin viewport |
| TrafiCam detections | — | **stays in the adapter**, and is not planned to move |

## What has been implemented for them so far

| #91 item | what it gave the mappings |
|---|---|
| 1, converters | ILSA `observedArea`, replacing the QVTo black box |
| 2, OCL | the `'/' -> '_'` service-id rewrite; `'ILSA ' + intersectionName`; `bbox.at(n)` |
| 3, functions | the TrafiCam viewport, a `GeometryCollection` from a whole `Scene` |
| 5, payload-named services | one service for each ILSA signal, from one block instead of nine; and the signal-state service, created on the model when the first state arrives |
| 7, admin feature mappings | both friendly names, and the ILSA location inside a `Double[]` |
| 6, payload metadata | ILSA's `type` and `signalGroup` on the color resource |

## What is left

| what | where | kind |
|---|---|---|
| a shared UNIFIED profile | the two ILSA mappings must reach one provider | a mapping to write, no engine gap |
| an extra admin attribute | TrafiCam `viewport`; also ILSA's `Thermal extends Admin` | #91 item 8 |
| the id from the MQTT topic | `TLSignalState.intersectionId`, `CamConfig.camId` | a transient attribute, Jena's change |
| binary EMF on ingress | TrafiCam only | southbound, not the engine |

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
