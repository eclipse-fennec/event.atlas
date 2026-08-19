# ⚠️ Deprecated — moved to model.atlas

This project has moved to the
[`eclipse-fennec/model.atlas`](https://github.com/eclipse-fennec/model.atlas) repository
(same directory name, same bundle symbolic name and Java packages). All further
development happens there; this copy is frozen and will be removed in a future cleanup.

| | old (here) | new (model.atlas) |
|---|---|---|
| Repository | `eclipse-fennec/event.atlas` | `eclipse-fennec/model.atlas` |
| BSN / packages | `org.eclipse.fennec.model.atlas.eobject.provider` | unchanged |
| Maven coordinates | `org.eclipse.fennec.event.atlas:org.eclipse.fennec.model.atlas.eobject.provider` | `org.eclipse.fennec.model.atlas:org.eclipse.fennec.model.atlas.eobject.provider` |

The bundle never was mapping-specific — it is a generic Model Atlas content source for the
emf.osgi EObject registry, so it belongs next to the rest of the Model Atlas code rather
than in the SensiNact mapping workspace.

Consumers should switch their `central.mvn` / dependency declarations to the new Maven
coordinates; snapshots are published from model.atlas's `snapshot` branch. Inside this
workspace only `…mapping.runtime/launch.bndrun` still wires the provider (component
`AtlasEObjectProvider`, configured in `…mapping.local.config`); it keeps using the frozen
local copy until the model.atlas artifact is indexed in `cnf/ext/central.mvn`.
