# Event Atlas mapping runtime image

Self-contained SensiNact mapping runtime: the mapping engine, the SensiNact gateway and
its northbound REST API on port 8080. Provider mappings and mapping profiles are read as
XMI files from directories inside the container — there is no Model Atlas connection.

Published by CI as `docker.io/eclipsefennec/event.atlas` and
`ghcr.io/eclipse-fennec/event.atlas` (tags: `snapshot` / `latest` plus the bundle
version); see `.github/workflows/reusable-container.yml`.

## Content layout

The build context expects a `content/` directory (git-ignored, staged by CI or by the
manual steps below):

```
content/eventatlas.runtime_docker.jar   bnd-exported executable runtime
content/runtime/mappings/               ProviderMapping XMIs (key.feature: mid)
content/runtime/profiles/               MappingProfile XMIs (key.feature: profileId)
```

The runtime wiring (file providers → EObject registries `sensinact-mappings` /
`sensinact-profiles`, session manager `ALLOW_ALL`, Jakarta-RS whiteboard, anonymous REST
access) is baked in as the `org.eclipse.fennec.event.atlas.mapping.docker.config`
configurator bundle — deliberately not a mounted file: the Felix configurator's
`configurator.initial` pass runs before the runtime's JSON provider is wired and fails
with "Invalid JSON", so file-based bootstrap config does not work here.

## Building locally

```bash
./gradlew :org.eclipse.fennec.event.atlas.mapping.runtime:export.eventatlas.runtime_docker
mkdir -p docker/eventatlas/content
cp org.eclipse.fennec.event.atlas.mapping.runtime/generated/distributions/executable/eventatlas.runtime_docker.jar \
   docker/eventatlas/content/
cp -r org.eclipse.fennec.event.atlas.mapping.runtime/runtime docker/eventatlas/content/runtime
docker build -t eventatlas:local docker/eventatlas/
```

## Running

```bash
docker run --rm -p 8080:8080 \
  -v $(pwd)/my-mappings:/opt/eventatlas/runtime/mappings \
  -v $(pwd)/my-profiles:/opt/eventatlas/runtime/profiles \
  eventatlas:local
```

Smoke test: `curl http://localhost:8080/sensinact/providers` answers with the providers
list. Mappings must reference domain models resolvable in the runtime; add the domain
model bundles in a derived image (or extend the docker bndrun) for the sources you map.
