# Local repository — TEMPORARY bundles

These jars are **local builds**, copied here because the published artifact is missing or broken.
They are not a permanent part of this workspace and they **shadow** any artifact with the same
bundle symbolic name and a lower version: the resolver picks the highest version in range, and a
local build is stamped with the time it was built.

| Bundle | Built from | Remove when |
|---|---|---|
| `org.eclipse.sensinact.gateway.northbound.sensorthings.rest.gateway` `0.0.2.202609240827` | `eclipse-sensinact/org.eclipse.sensinact.gateway` at `8f72d680` (`mvn clean install`) | the Eclipse snapshot deployment publishes a build that carries gateway PR [#769](https://github.com/eclipse-sensinact/org.eclipse.sensinact.gateway/pull/769) |

## Why `rest.gateway` is here

Gateway PR #769 ("Resolve the history provider live instead of from the application snapshot",
merged 2026-09-17) fixes a SensorThings bug that this runtime is exposed to: when the history
provider registers *after* the SensorThings application — the normal order here, because
`configs/timescale.json` has to reach a database first — every Datastream answers with exactly
one Observation, the current value, and nothing is logged. A fully populated history store looks
like a missing one.

The Eclipse snapshot deployment has not published since **2026-09-10** (`0.0.2-20260910.130036`,
built from `9bab89a8` of 2026-09-08), so the fix is unreachable through
`cnf/ext/sensinact.maven`. Compare `Git-SHA` in a candidate jar's manifest against the merge
commit before concluding anything — `maven-metadata.xml` keeps getting a fresh `lastUpdated`
without a new build behind it.

**Only this one bundle is overridden, deliberately.** Its manifest is byte-identical to the
published `-166` build apart from `Bundle-Version`, `Git-SHA`, `Bundle-SCM` and `Build-Jdk-Spec`
— same `Import-Package` ranges — so it drops into the published gateway without dragging the rest
of master in. That matters: master also carries #765, whose `geo-json` imports
`org.jspecify.annotations`, which no index here provides. A whole-gateway override does not
resolve.

## Do not install the gateway into `~/.m2`

`mvn clean install` in a gateway checkout overwrites every `0.0.2-SNAPSHOT` jar that bnd's Maven
repository reads, **without updating bnd's `.jar.json` sidecar** — so the cache silently serves a
different build than it claims, local resolves stop matching CI, and (with master's jspecify
import) they stop resolving at all. Build the one module and copy its jar here instead:

```bash
cd <gateway>/northbound/sensorthings/rest.gateway && mvn -am -pl . package
```

## After adding or replacing a jar

Regenerate the index — `Local` is a `LocalIndexedRepo` and will not serve a file it has not
indexed:

```bash
cd cnf/local
bnd index -d . -n Local -r index.xml <bsn>/<bsn>-<version>.jar ...
```

Then re-resolve every bndrun that carries the bundle and confirm the override actually landed, by
exporting and hashing the bundle *inside* the export rather than trusting a green resolve:

```bash
./gradlew :org.eclipse.fennec.event.atlas.mapping.runtime:export.eventatlas.runtime_docker --rerun-tasks
unzip -p …/eventatlas.runtime_docker.jar | sha1sum   # compare against the jar here
```

## When the upstream build arrives

Delete the bundle directory, regenerate `index.xml`, re-resolve the bndruns and check the
`Git-SHA`/`Bundle-Version` that the export then picks up. When this directory is empty, remove it
together with the `-plugin.0.Local` registration in `cnf/build.bnd`.
