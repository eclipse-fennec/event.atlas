# Event Atlas mapping runtime image

Self-contained SensiNact mapping runtime: the mapping engine, the SensiNact gateway, its
northbound REST API, the SensorThings v1.1 REST gateway and the SensorThings MQTT broker.
Provider mappings and mapping profiles are read as XMI files from directories inside the
container; a Model Atlas can additionally feed mappings in over REST (off by default —
the client is wired but points at a placeholder URI, see the environment table).

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

## Ports

| Port | What |
|---|---|
| 8080 | HTTP — everything REST, under the `event/` context path |
| 1883 | SensorThings MQTT broker, plain TCP |
| 8885 | SensorThings MQTT broker, WebSocket |
| 8883 / 8886 | MQTT over TLS / WebSocket over TLS — **only bound when a keystore is configured** |

The MQTT broker is Moquette, embedded in the `…northbound.sensorthings.mqtt` bundle (that
is what pulls netty into the runtime). It writes a `.moquette_uuid` marker into the process
working directory.

## HTTP endpoints

One Felix HTTP whiteboard instance serves context path `event/`, and one Jersey
(Jakarta-RS) whiteboard is mounted at `rest` inside it, so every REST base starts with
`/event/rest`:

| URL | What |
|---|---|
| `/event/rest/sensinact` | SensiNact northbound REST API (anonymous access allowed) |
| `/event/rest/v1.1` | SensorThings v1.1 REST gateway |
| `/event/rest/ingest/{channel}` | `POST` a payload into the twin (the REST southbound adapter) |
| `/event/rest` | the whiteboard root — **not** an endpoint; answers 500, see below |

See [Payload formats](#payload-formats) for what a channel may post.

The framework's default HTTP service is switched off (`org.osgi.service.http.port=-1` in
the bndrun) so that the named whiteboard from `sensinact.json` is the only one binding
8080. Changing the context path, the port or the Jersey path means editing that file, not
the bndrun.

> **The SensorThings gateway owns the whiteboard root.** Its Jakarta-RS application
> declares no application base, so it is mounted at `/event/rest` and its resources carry
> `@Path("/v1.1/…")` themselves. Consequence: any path under `/event/rest` that no other
> application claims — the bare root included — becomes a SensorThings 404 whose
> `ErrorResponse` has no `MessageBodyWriter`, and surfaces as **HTTP 500 "Request failed."**
> instead of a 404. Nothing is broken when you see that; you are off-endpoint.
>
> This is also why the ingest endpoint has an application of its own
> (`PayloadIngestApplication`, base `ingest`, config pid `event.atlas.southbound.rest`): a
> Jakarta-RS resource with no `osgi.jakartars.application.select` joins the whiteboard's
> default application, which the SensorThings application shadows — so it would never be
> invoked. Any further REST resource added to this runtime needs the same treatment.

## Payload formats

Both southbound adapters accept **XMI** and **JSON**, and both formats go through the same
`PayloadIngest`: the format selects an EMF resource factory by file extension, the payload is
deserialized into EObjects and pushed into the twin. The MQTT side picks the format per channel
(`_XMI_TOPICS` / `_JSON_TOPICS`), the REST side from the request's `Content-Type`.

**JSON needs a codec bundle.** The runtime carries `org.eclipse.fennec.codec` (plus
`…codec.api`, `…codec.metadata` and `org.eclipse.fennec.emf.osgi.metadata`) for exactly this:
it registers the resource factory for the `json` extension. It is pinned in the bndrun's
`-runrequires`, so a runtime that could not honour a JSON channel fails to resolve rather than
starting and dropping every message. If the codec is ever missing anyway, an ingest answers
`FORMAT_UNSUPPORTED` (HTTP 501) and logs

```
no EMF resource factory is registered for extension 'json' - the runtime is missing the codec
bundle for that format (JSON needs org.eclipse.fennec.codec)
```

rather than handing the payload to EMF's wildcard factory — which is XMI, and used to fail with
the misleading `Content is not allowed in prolog` (a SAX error) instead.

A payload names its own model: the root object carries `_type` with the nsURI of its EClass
(`{"_type": "https://eclipse.org/fennec/lorawan/dragino#//DraginoLSE01Uplink", …}`), the XMI
equivalent being the root element's namespace. That model has to be resolvable in the runtime -
deployed as a bundle or reachable through the Model Atlas - or the ingest reports
`MODEL_UNKNOWN`.

## Configuration

The runtime wiring is baked into the
`org.eclipse.fennec.event.atlas.mapping.docker.config` configurator bundle — deliberately
not a mounted file: the Felix configurator's `configurator.initial` pass runs before the
runtime's JSON provider is wired and fails with "Invalid JSON", so file-based bootstrap
config does not work here. The bundle ships two configurator resources
(`-includeresource: OSGI-INF/configurator/=configs/` picks up everything in `configs/`):

| Resource | Contents |
|---|---|
| `config.json` | the event.atlas side: file providers → EObject registries `sensinact-mappings` / `sensinact-profiles`, the Model Atlas REST client + `AtlasEObjectProvider`, and the MQTT southbound (SensiNact MQTT client + `MqttPayloadListener`) |
| `sensinact.json` | the SensiNact side: session manager `ALLOW_ALL`, the named Felix HTTP whiteboard + Jersey whiteboard, northbound REST (anonymous), SensorThings REST (`history.provider`) and the SensorThings MQTT broker ports/keystore |
| `timescale.json` | the history store: where the twin's value updates are written, and under which provider name they are served back — see [History](#history) |

Deployment-specific values are `$[env:…]` placeholders resolved at configuration-delivery
time by `org.apache.felix.configadmin.plugin.interpolation`, so one published image serves
every environment. Every placeholder has a default — the image starts standalone.

| Variable | Default | What |
|---|---|---|
| `EVENTATLAS_ATLAS_BASE_URI` | `http://localhost:8086/atlas/rest` | Model Atlas REST base; unreachable by default, which logs a `TransportException` per drift check and is otherwise harmless |
| `EVENTATLAS_ATLAS_SCOPE` | `jena` | Atlas scope name (also used as the provider's scope target) |
| `EVENTATLAS_ATLAS_REGISTRY` | `sensinactmapping` | Atlas registry holding the mapping objects |
| `EVENTATLAS_ATLAS_DRIFT_INTERVAL_MS` | `10000` | drift check interval |
| `EVENTATLAS_ATLAS_REFRESH_INTERVAL_MS` | `60000` | full re-sync interval |
| `EVENTATLAS_MQTT_PROTOCOL` / `_HOST` / `_PORT` | `tcp` / `localhost` / `1883` | southbound broker the runtime *connects to* (not the SensorThings broker it hosts) |
| `EVENTATLAS_MQTT_USER` / `_PASSWORD` | empty | southbound broker credentials |
| `EVENTATLAS_MQTT_TOPICS` | `eventatlas/#` | comma-separated topics **subscribed at the broker** |
| `EVENTATLAS_MQTT_XMI_TOPICS` | `eventatlas/#` | which of those the **XMI** ingest channel handles |
| `EVENTATLAS_MQTT_JSON_TOPICS` | `eventatlas/json/#` | which of those the **JSON** ingest channel handles |
| `TIMESCALE_HOST` / `_PORT` / `_DB` | `localhost` / `5432` / `sensinactHistory` | the history database (see [History](#history)); with nothing listening there the history store simply stays inactive |
| `TIMESCALE_USER` / `_PWD` | `snaHistory` / empty | history database credentials |
| `TIMESCALE_PROVIDER` | `brokerHistory` | the name the history store is registered under - must match `history.provider` in `sensinact.json` |
| `SENSORTHINGS_MQTT_PORT` / `_SECURE_PORT` | `1883` / `8883` | hosted SensorThings broker, TCP / TLS |
| `SENSORTHINGS_MQTT_WS_PORT` / `_WSS_PORT` | `8885` / `8886` | hosted SensorThings broker, WebSocket / WSS |
| `SENSORTHINGS_MQTT_KEYSTORE_FILE` / `_TYPE` | empty / `jks` | keystore for the TLS listeners; without a file the TLS ports stay closed |
| `SENSORTHINGS_MQTT_KEYSTORE_PASSWORD` / `_KEYMANAGER_PASSWORD` | empty | keystore secrets (`.`-prefixed properties, so ConfigAdmin treats them as private) |

### List-valued variables

`EVENTATLAS_MQTT_TOPICS`, `_XMI_TOPICS` and `_JSON_TOPICS` are comma-separated. In the baked
`config.json` each is declared `$[env:NAME;default=...;delimiter=,;type=String[]]`.

**`type=String[]` is mandatory, not decoration.** The interpolation plugin only converts a value
when the `type` directive is present, so `delimiter=,` on its own is silently ignored and the
property arrives as one comma-joined String. sensiNact passes that straight to paho, which fails
activation with:

```
java.lang.IllegalArgumentException: Invalid usage of multi-level wildcard in topic string: eventatlas/#,waterpark/#
```

Add both directives to any new list property.

### Working directory

The container's `WORKDIR` is `/opt/eventatlas/work` — a directory owned by the runtime user
(65532), deliberately **not** `/opt/eventatlas`, whose contents stay root-owned and read-only.
It has to be writable: libraries that take a default path resolve it against `user.dir`, and
paho's `MqttDefaultFilePersistence` — which sensiNact's `MqttClientHandler` gets from
`new MqttClient(uri, id)`, with no way to configure a directory — throws
`MqttException (0)` from `open()` if it cannot write there, killing the southbound MQTT client at
activation. If you override the working directory when running the image, point it at a writable
path.

## History

Without a history store the twin only ever holds each resource's *current* value: SensorThings
`Observations` return one row, and there is nothing to plot. This image therefore carries the
SensiNact **timescale** history provider (`…southbound.history.timescale-provider`, plus the
postgres driver and the two Aries tx-control bundles it reaches the database through). It
subscribes to the twin's update notifications and appends them to a TimescaleDB hypertable.

Two names have to agree, and both default to `brokerHistory`: the `provider` in
`timescale.json` (what the store registers itself as) and `history.provider` in
`sensinact.json` (what the SensorThings gateway asks for). Change one, change the other -
`TIMESCALE_PROVIDER` exists so that a deployment can do it in one place.

The store's component is `configuration-policy=require`, so `timescale.json` is the on/off
switch. It ships in the config bundle, which means:

- **with a reachable database** the store connects on activation, creates its schema if it is
  not there yet (`sensinact.history`, a hypertable keyed by provider/service/resource and time)
  and starts recording. `TimescaleDB enabled` in the log is the confirmation.
- **without one** the component fails to activate and nothing else happens - no retry storm, no
  stack traces on stdout. The runtime serves everything else exactly as before, and history
  queries stay empty. The activation failure is reported through the OSGi log service only, so
  if history stays empty when you expect data, that is the first thing to check (`scr:list` in
  the Gogo shell shows the component as unsatisfied or failed).

`exclude.resources` keeps the per-provider location resources (`observedArea`, `viewport`) out
of the table - they are state, not measurements. It is a list of resource-selector filters, so a
deployment can exclude more.

A database to try it against, and the image wired to it:

```bash
docker compose -f docker/eventatlas/docker-compose.example.yml up
```

That compose file is the whole example: a `timescale/timescaledb-ha:pg16` service with the
default database/user this config expects, and the runtime pointed at it. What lands in the
table needs a mapping and its domain model, exactly as for the current value - history is
downstream of the same push.

```bash
# what has been recorded
docker compose -f docker/eventatlas/docker-compose.example.yml exec timescaledb \
    psql -U snaHistory -d sensinactHistory \
    -c 'select time, provider, service, resource, value_num from sensinact.history order by time desc limit 20;'

# and through the northbound: the Observations of one datastream
curl 'http://localhost:8080/event/rest/v1.1/Datastreams'
```

## Building locally

```bash
./gradlew :org.eclipse.fennec.event.atlas.mapping.runtime:export.eventatlas.runtime_docker
mkdir -p docker/eventatlas/content
cp org.eclipse.fennec.event.atlas.mapping.runtime/generated/distributions/executable/eventatlas.runtime_docker.jar \
   docker/eventatlas/content/
cp -r org.eclipse.fennec.event.atlas.mapping.runtime/runtime docker/eventatlas/content/runtime
docker build -t eventatlas:local docker/eventatlas/
```

The exported jar also runs outside docker, which is the quickest way to check a config
change (the mount points below `/opt/eventatlas/runtime` are simply missing then):

```bash
java -Dgosh.args=--nointeractive -jar \
  org.eclipse.fennec.event.atlas.mapping.runtime/generated/distributions/executable/eventatlas.runtime_docker.jar
```

## Running

```bash
docker run --rm -p 8080:8080 -p 1883:1883 -p 8885:8885 \
  -v $(pwd)/my-mappings:/opt/eventatlas/runtime/mappings \
  -v $(pwd)/my-profiles:/opt/eventatlas/runtime/profiles \
  eventatlas:local
```

Smoke tests:

```bash
curl http://localhost:8080/event/rest/sensinact/providers   # SensiNact provider list
curl http://localhost:8080/event/rest/v1.1                 # SensorThings serverSettings
curl http://localhost:8080/event/rest/v1.1/Things          # {"value":[…]}

# ingest: 200 pushed, 202 no mapping, 400 unreadable, 422 unknown model,
#         501 no codec for that format, 503 twin down
curl -i -X POST -H 'Content-Type: application/xml' --data-binary @payload.xmi \
     http://localhost:8080/event/rest/ingest/my-device
curl -i -X POST -H 'Content-Type: application/json' --data-binary @payload.json \
     http://localhost:8080/event/rest/ingest/my-device
```

Mappings must reference domain models resolvable in the runtime; add the domain model
bundles in a derived image (or extend the docker bndrun) for the sources you map. Until a
mapping produces a twin, `Things` and the provider list show only the built-in `sensiNact`
provider.
