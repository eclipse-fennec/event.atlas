# Event Atlas mapping runtime

Assembly project — it carries no code of its own, only the two runtime definitions:

| File | Purpose |
|---|---|
| `launch.bndrun` | local development runtime: mapping engine + SensiNact gateway + Model Atlas client + both southbound adapters + the history store + a Gogo shell |
| `eventatlas.runtime_docker.bndrun` | self-contained image runtime: mappings and profiles read from XMI files under `/opt/eventatlas/runtime`, no Model Atlas |

Configuration comes from the sibling config bundles — `…mapping.local.config` for `launch.bndrun`,
`…mapping.docker.config` for the image.

```bash
./gradlew :org.eclipse.fennec.event.atlas.mapping.runtime:run.launch      # start it
./gradlew :org.eclipse.fennec.event.atlas.mapping.runtime:resolve.launch  # after changing -runrequires
```

Gradle does not forward stdin, so for an interactive Gogo shell use `export.launch` and run
`generated/distributions/executable/launch.jar` directly.

## Before you send anything

A payload only reaches the digital twin if **both** halves are resolvable:

1. **The domain model** (here: the DWD weather model, nsURI `http://cdc.dwd.de/common/weather`).
   The payload names it in its root element; the runtime resolves it locally first, then
   fetch-on-miss through the Model Atlas.
2. **A `ProviderMapping`** for the payload's `EClass`, reaching the `sensinact-mappings`
   EObject registry — from the Model Atlas (registry `sensinactmapping`, scope `jena`,
   re-synced every `refresh.interval.ms`, 60 s by default) or from local XMI files.
3. **A codec for the payload format.** XMI needs nothing; JSON is read by the EMF resource
   factory that `org.eclipse.fennec.codec` contributes for the `json` file extension. Both
   bndruns require that bundle by identity, so a runtime that could not honour a JSON channel
   fails to resolve. Without it an ingest reports `FORMAT_UNSUPPORTED` — EMF would otherwise
   answer the unknown extension with its wildcard factory, XMI, and the payload would die in
   a SAX parser (`Content is not allowed in prolog`).

> **The mapping's own nsURI is load-bearing.** A `ProviderMapping` XMI must declare
> `https://fennec.eclipse.org/event.atlas/mapping/1.0`. An XMI still carrying the pre-rename
> `…/sensinact/core/mapping/1.0` fails to load and is silently skipped — the symptom is
> "no provider mapping is registered" on every payload.

Watch the log for `Registering provider mapping for '<mid>' into registry` at startup; that
confirms the mapping arrived. `sna:providers` in the Gogo shell lists the resulting provider
**before any payload is sent**, because the provider model is built at registration time.

## A payload

Both examples below use the same file. The timestamps are generated at write time, so the
values are current; SensiNact keeps the newest value per resource, so a stale timestamp can
look like "nothing happened".

```bash
cat > /tmp/weather.xmi <<EOF
<?xml version="1.0" encoding="UTF-8"?>
<xmi:XMI xmi:version="2.0" xmlns:xmi="http://www.omg.org/XMI" xmlns:dwdweather="http://cdc.dwd.de/common/weather">
  <dwdweather:WeatherReports xmi:id="wr" id="manual-10567" reports="r0 r1"/>
  <dwdweather:MOSMIXSWeatherReport xmi:id="r0" id="report-current"
      timestamp="$(date -u +%Y-%m-%dT%H:%M:%S.000+0000)" station="st0" weatherStation="ws0"
      windDirection="180.0" windSpeed="5.0" cloudCoverTotal="50.0"
      surfacePressure="101000.0" tempAboveSurface5="290.0"/>
  <dwdweather:MOSMIXSWeatherReport xmi:id="r1" id="report-forecast-3h"
      timestamp="$(date -u -d '+3 hours' +%Y-%m-%dT%H:%M:%S.000+0000)" station="st0" weatherStation="ws0"
      windDirection="190.0" windSpeed="7.5" cloudCoverTotal="60.0"
      surfacePressure="101200.0" tempAboveSurface5="291.0"/>
  <dwdweather:WeatherStation xmi:id="ws0" name="GERA" id="10567">
    <location latitude="50.88" longitude="12.13" elevation="311"/>
  </dwdweather:WeatherStation>
  <dwdweather:Station xmi:id="st0" name="GERA">
    <location latitude="50.88" longitude="12.13" elevation="311"/>
  </dwdweather:Station>
</xmi:XMI>
EOF
```

The timestamp format is EMF's `EDate` serialization (`yyyy-MM-dd'T'HH:mm:ss.SSSZ`, RFC822
zone without a colon). Note also that the numbers must use a decimal **point** — rendering
this file with a tool that honours a comma-decimal locale produces `windSpeed="5,0"`, which
EMF rejects.

## Testing the MQTT adapter

Needs a broker:

```bash
docker run --rm -p 1883:1883 eclipse-mosquitto:2 mosquitto -c /mosquitto-no-auth.conf
```

`local.config` already wires both halves — they are two *independent* topic lists and both
must match, or messages arrive at the broker and are never delivered to the adapter:

```json
"sensinact.southbound.mqtt~local": {
  "id": "local-broker", "protocol": "tcp", "host": "localhost", "port": 1883,
  "topics": ["eventatlas/#"]                       // what is SUBSCRIBED on the broker
},
"event.atlas.southbound.mqtt~weather": {
  "mqttTopics": ["eventatlas/weather"],            // which of those THIS adapter handles
  "mqtt.handler.id": "local-broker",
  "format": "xmi",
  "name": "weather-mqtt"
},
"event.atlas.southbound.mqtt~json": {
  "mqttTopics": ["eventatlas/json/#"],             // the JSON half of the same subscription
  "mqtt.handler.id": "local-broker",
  "format": "json",
  "name": "local-mqtt-json"
}
```

### List properties under the interpolation plugin

`topics` and `mqttTopics` are OSGi `String[]` properties. Written literally in a cm.json file they
are JSON arrays, as above. Written as an interpolated environment variable they need **both**
directives:

```json
"topics": "$[env:MY_TOPICS;default=eventatlas/#;delimiter=,;type=String[]]"
```

`delimiter` alone does nothing — the plugin converts only when `type` is present — and the property
then reaches paho as a single comma-joined string, which fails activation with
`IllegalArgumentException: Invalid usage of multi-level wildcard in topic string`. (The `]` inside
`String[]` is safe: the placeholder parser counts brackets.)

### Mixed-format topic trees

`event.atlas.southbound.mqtt` is a **factory** pid, and sensiNact dispatches to each registered
listener only for the topics matching that listener's own filters (`MqttTopic.isMatched`). So when
one broker carries several payload formats, add one adapter instance **per format** over the same
`mqtt.handler.id` rather than making a single adapter guess:

```json
"event.atlas.southbound.mqtt~xmi":  { "mqttTopics": ["plant/sensors/#"],
                                      "mqtt.handler.id": "local-broker", "format": "xmi" },
"event.atlas.southbound.mqtt~json": { "mqttTopics": ["plant/dashboard/#"],
                                      "mqtt.handler.id": "local-broker", "format": "json" }
```

Each channel then states its format, so a payload in the wrong format is a `PARSE_ERROR` naming
the channel instead of being silently routed to the other codec. The filters are subsets of the
broker's `topics` and are not derived from them: a subscribed topic matching **no** filter is
received and dropped — every channel logs its filters at activation, which is how you catch it.

`format` also accepts **`auto`**, which picks per message from the payload's first non-whitespace
byte (`<` → XMI, `{`/`[` → JSON, a leading BOM skipped, otherwise XMI). Reach for it only when a
*single topic* genuinely carries both formats — where the topic tree separates them, the two-channel
split above is the better answer.

Send it:

```bash
mosquitto_pub -h localhost -p 1883 -t eventatlas/weather -f /tmp/weather.xmi
```

Use `-f` (or `-m`), never `-l` — `-l` publishes every *line* of the XMI as a separate
message. Add `-r` to publish retained: SensiNact replays the last retained message per topic
to a listener as soon as it binds, so the payload is re-ingested on every runtime restart.

At startup you should see:

```
INFO: MQTT southbound adapter 'weather-mqtt' listening on [eventatlas/weather] (format xmi, broker 'local-broker')
```

and on delivery:

```
INFO: Pushed payload from 'eventatlas/weather' - 5 object(s), 1 mapping(s) applied
```

## Testing the REST adapter

The endpoint is served by the Jakarta-RS whiteboard, which is `configuration-policy=require`
— **without this config nothing is published at all** and every request 404s:

```json
"JakartarsServletWhiteboardRuntimeComponent": {
  "osgi.jakartars.name": "eventatlas.rest"
}
```

The resource declares no application, so it binds to the default application at `/` —
unlike SensiNact's northbound REST, which lives under its own `/sensinact` base.

```bash
curl -i -X POST http://localhost:8090/event/rest/ingest/weather \
     -H 'Content-Type: application/xml' \
     --data-binary @/tmp/weather.xmi
```

```
HTTP/1.1 200 OK
Content-Type: text/plain

Applied 1 mapping(s) to 5 object(s)
```

The `{channel}` path segment (`weather` here) is free-form and only identifies the sender in
log messages — the model comes from the payload, not the path. The `Content-Type` selects the
format: `application/xml` for XMI, `application/json` for JSON.

## Reading the result

```
sna:providers          # in the Gogo shell
sna:describe 10567
```

The docker runtime additionally exposes the northbound REST API
(`curl http://localhost:8080/event/rest/sensinact/providers`); `launch.bndrun` does not include it.

## Outcomes

Both adapters share one ingest, so the outcomes are the same; only the reporting differs.

| Log | HTTP | Meaning |
|---|---|---|
| `Pushed payload … N mapping(s) applied` | 200 | in the twin |
| `no provider mapping is registered for it` | 202 | model resolved, no mapping for that `EClass` — check the mapping's nsURI and that it reached the registry |
| `model '<nsURI>' is not available` | 422 | neither deployed nor resolvable via the Model Atlas |
| `Cannot deserialize … dropping payload` | 400 | malformed payload |
| `was read as <format> but contained no objects` | 400 | read, but nothing came out — the message carries the codec's own diagnostic; for JSON an unresolvable `_type` lands here rather than on 422, because the codec records a diagnostic where the XMI parser throws |
| `no EMF resource factory is registered for extension …` | 501 | this runtime has no codec for that format — for `json`, `org.eclipse.fennec.codec` is missing from the runbundles |
| `Failed pushing payload … into sensinact` | 503 | gateway unavailable — retryable |
| — | 404 | Jakarta-RS whiteboard not configured, or wrong path |

## History

The twin holds only the current value of a resource. Both runtimes carry the SensiNact
**timescale** history provider, which records every update into a TimescaleDB and serves the
recorded values back under the provider name `brokerHistory` - which is what
`sensinact.json`'s `history.provider` already points the SensorThings gateway at.

`…local.config`'s `configs/timescale.json` is the switch: the store's component is
`configuration-policy=require`, so with that file present it activates, and without a database
to talk to it just stays inactive (nothing retries, nothing logs to stdout - the activation
failure only reaches the OSGi log service, so `scr:list` in the Gogo shell is where an
unexpectedly empty history shows up).

One to try against:

```bash
docker run --rm -p 5432:5432 \
  -e POSTGRES_DB=sensinactHistory -e POSTGRES_USER=snaHistory -e POSTGRES_PASSWORD=test.password \
  timescale/timescaledb-ha:pg16
```

The defaults in `timescale.json` match that container except for the password, so:

```bash
TIMESCALE_PWD=test.password ./gradlew :org.eclipse.fennec.event.atlas.mapping.runtime:run.launch
```

`History schema ready (TimescaleDB enabled)` and `Timescale history storage brokerHistory
registered` in the log mean it is recording. From then on every payload that reaches the twin
also lands in `sensinact.history`:

```bash
psql -h localhost -U snaHistory -d sensinactHistory \
  -c 'select time, provider, service, resource, value_num from sensinact.history order by time desc limit 20;'
```

`docker/eventatlas/docker-compose.example.yml` is the same thing for the image, with the
database wired in for you.

## Housekeeping

The Paho MQTT client writes its persistence store into the working directory, so running the
launch runtime leaves `paho*-tcplocalhost1883/` directories here. They are throw-away.
