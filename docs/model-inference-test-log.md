# Model inference — live test log

A record of end-to-end runs of the model-inference chain (#27 → #30) against real infrastructure:
what was deployed, which transport was used, what the agent produced, and what each run taught us.

Each run is appended below. The point of the log is that the interesting findings were almost all
*deployment* findings rather than code findings, and none of them were visible from unit tests.

---

## 2026-08-28 — first end-to-end runs

### Objective

Close the two acceptance criteria of #30 that could not be verified on a developer machine alone:

1. the chat completion actually binding a **remote** MCP endpoint;
2. end to end — feed a sample set, see a draft appear in the Model Atlas.

### Setup

| component | what was deployed |
|---|---|
| Model Atlas | `eclipsefennec/model.atlas:jena-snapshot`, scope `jena`, on `localhost:8080`. Release stage seeded with `lorawan` (8 classes) and `em310udl` (2 classes) |
| Metamodel MCP server | `emf.osgi-mcp`, `mcp.emf.runtime`, `127.0.0.1:8099`, servlet `/mcp/inference` (the task-scoped one — 21 tools) |
| Public exposure | an ngrok tunnel to 8099. **Required**: Anthropic's MCP connector dials the URL from *its* side, so a localhost URL cannot work |
| MQTT broker | `eclipse-mosquitto:2` on 1883 |
| event.atlas runtime | `inference.bndrun` — the minimal runtime built for this test: 94 bundles vs `launch.bndrun`'s 128; no history store, no northbound REST, no SensorThings, no HTTP whiteboard |
| Model | `claude-sonnet-4-6` (see run 1 for why not Opus 5) |

`inference.bndrun` keeps the sensinact gateway core because the chain requires it: `PayloadIngest`
has a mandatory reference to `InstancePusher`, whose implementation mandatorily needs
`GatewayThread`. The twin is therefore written but has nothing northbound reading it — outcomes are
observed in the ingest log.

### The payload set

Five JSON payloads on `lorawan/unknown/dragino`, derived from `dragino-example.json` with `_type`
removed, all sharing the discriminator `deviceInfo.deviceProfileName = "Dragino_LSE01"`. Type traps
were planted deliberately, to test whether the agent reasons over *all* samples rather than
generalising from the first:

| field | values across p1…p5 | correct inference | what it tests |
|---|---|---|---|
| `conduct_SOIL` | `35`, `41`, **`38.6`**, `40`, `37.2` | `EDouble` | must not settle on `EInt` from the first two |
| `pulse_total` | `1200`, `45000`, `987654`, **`3221225472`**, `12` | `ELong` | overflows `EInt` in one sample only |
| `temp_DS18B20` | `"0.00"`, `"0.00"`, **`null`**, `"1.25"`, `"0.00"` | `EString` | a null must not derail the type |
| `hum_SOIL` | absent, `62.4`, absent, `58.1`, absent | `EDouble`, optional | optionality across the window |
| `ec_SOIL` | absent ×3, `1450`, absent | `EInt` | appears once |
| `s_flag` | `1` ×5 | `EInt` | **control** — must not widen everything to double |
| `water_SOIL` | always quoted (`"13.74"` …) | `EString` | respects JSON typing (`EDouble` defensible) |

Payloads 1–5 were also made **structurally** distinct (differing key sets, one null, one with two
`rxInfo` entries) — see Finding 3 for why that was necessary.

### Runs

| # | transport | model | outcome | cost |
|---|---|---|---|---|
| 1 | sync | `claude-opus-5` | `HTTP 400` in 0 s — `temperature is deprecated for this model` | $0 |
| 2 | sync | `claude-sonnet-4-6` | returned after 72 s with only the model's *preamble* text, 1 tool call, no receipt | small |
| 3 | **batch** (hand-submitted via curl) | `claude-sonnet-4-6` | **`end_turn`, 43 tool calls, draft published** | **~$0.71** |
| 4 | sync, `stream: true` | `claude-sonnet-4-6` | `pause_turn` at 11 iterations — streaming makes no difference | ~$0.50 |
| 5 | **batch via `BatchChatCompletionAdapter`** | `claude-sonnet-4-6` | **`end_turn`, 41 tool calls in 15 iterations, `RECEIPT: created`, draft published** — but the runtime could not read the result back, see Finding 10 | ~$0.7 |
| 6 | batch | `claude-sonnet-4-6` | `pause_turn` at **22 iterations** — the agent backtracked after re-modelling inherited fields | ~$0.7 |
| 7 | batch via adapter | `claude-sonnet-4-6` | `HTTP 400` at submission — the codec emitted `_type` discriminators, see Finding 13 | $0 |
| 8 | **batch via adapter** | `claude-sonnet-4-6` | **`end_turn`, 22 tool calls in 11 iterations, `RECEIPT: created`, draft published** — read-back failed on a path bug, see Finding 14 | **~$0.34** |
| 9 | **batch via adapter** | `claude-sonnet-4-6` | `end_turn`, 36 calls in 17 iterations, draft published, result downloaded correctly — but the receipt was not extracted, see Finding 17 | ~$0.68 |

Run 3 detail: submitted 10:24, execution began 10:27, ended 10:47 — ~20 minutes wall clock,
435,227 input / 8,624 output tokens, `service_tier: batch`. Final answer:

```
RECEIPT: created https://fennec.eclipse.org/event.atlas/inferred
```

### What the agent produced

Published to `jena/schema/draft`; the verbatim package is kept at
[`results/inferred-dragino-run1.ecore`](results/inferred-dragino-run1.ecore):

```xml
<ecore:EPackage name="draginolse01" nsURI="https://fennec.eclipse.org/event.atlas/inferred">
  <eClassifiers xsi:type="ecore:EClass" name="DecodedObject">
    <eStructuralFeatures name="batV"         eType="…#//EDouble"/>
    <eStructuralFeatures name="conduct_SOIL" eType="…#//EDouble"/>
    <eStructuralFeatures name="pulse_total"  eType="…#//ELong"/>
    <eStructuralFeatures name="temp_SOIL"    eType="…#//EDouble"/>
    <eStructuralFeatures name="water_SOIL"   eType="…#//EString"/>
    <eStructuralFeatures name="temp_DS18B20" eType="…#//EString"/>
    <eStructuralFeatures name="temp_SOIL_f"  eType="…#//EDouble"/>
    <eStructuralFeatures name="s_flag"       eType="…#//EInt"/>
    <eStructuralFeatures name="ec_SOIL"      eType="…#//EInt"/>
    <eStructuralFeatures name="hum_SOIL"     eType="…#//EDouble"/>
  </eClassifiers>
  <eClassifiers xsi:type="ecore:EClass" name="DraginoLSE01Uplink"
      eSuperTypes="https://eclipse.org/fennec/lorawan#//UplinkMessage">
    <eAnnotations source="http://eclipse.org/fennec/codec/typeMapping/lorawan">
      <details key="typeDiscriminator" value="Dragino_LSE01"/>
    </eAnnotations>
    <eStructuralFeatures xsi:type="ecore:EReference" name="object"
        eType="#//DecodedObject" containment="true"/>
  </eClassifiers>
</ecore:EPackage>
```

**All seven planted traps were inferred correctly**, `pulse_total → ELong` included — which
required reading the fourth sample rather than generalising from the first two.

**The prompt's central bet paid off.** `InferencePrompt` names no model family, no annotation
source, no tool, and (since this test) no codec type map. The agent nonetheless:

- found `lorawan#//UplinkMessage` and subclassed it, rather than modelling the envelope again;
- mirrored `em310udl`'s shape — a vendor `DecodedObject` plus a containment reference named
  `object` — having read it with `export_package`;
- derived the codec type map id **`lorawan`** and wrote
  `typeMapping/lorawan` + `typeDiscriminator = Dragino_LSE01` from the sibling model alone.

That last point is the strongest result of the day: the type map is what makes an inferred model
usable by *this* runtime, and the agent worked it out by discovery. The prompt used to state it
explicitly; that sentence was removed during this test and the run is the evidence it was
unnecessary.

Its 43 calls went discovery-first, as the server instructions ask: `list_registry`,
`list_metamodel`, nine `describe_eclass`, `list_annotation_sources`, `export_package` — and only
then `create_dataset` / `create_epackage` / `add_eclass` / `add_eattribute` / `register_package` /
`post_to_model_atlas`.

### Findings

**1. `temperature` is rejected by every current model, and the client always sent it.**
`ClaudeRequest.temperature` was a primitive `EDouble` with `defaultValueLiteral="0.0"`, and
`ClaudeHelper` enables the codec's `SERIALIZE_DEFAULT`, so `"temperature":0.0` was on every
request. Anthropic removed the sampling parameters on Opus 4.7+/5 and Sonnet 5 and rejects them
with a 400 rather than ignoring them. Invisible until now only because the client's default model
(`claude-sonnet-4-6`) is on the older side of the cut. Fixed in `nsc` the same day by making the
attribute an `EDoubleObject` with no default, so unset stays `null` and is not serialized.

**2. A long synchronous turn is paused by the API; the batch transport runs it to completion.**
Confirmed by measurement, not inference — the same request replayed against
`POST /v1/messages` returns:

```
HTTP 200 after 103s
stop_reason : pause_turn
content     : 8 text blocks, 25 mcp_tool_use, 24 mcp_tool_result
final text  : "Now add the typeDiscriminator annotation to DraginoLSE01Uplink…"
```

i.e. the turn is cut off mid-work. The same prompt and model via
`POST /v1/messages/batches` reached `stop_reason: end_turn` after 43 tool calls. Batch is also
half price.

This fully explains the "answer carried no receipt" warning: a paused turn has not finished, so
there is no receipt in it. `AnswerText` is not at fault — it joins every text block it finds.

**It is the documented server-side sampling-loop limit, and streaming does not lift it.** The API
reference states: *"Returned when the server-side sampling loop reaches its iteration limit while
executing server tools… The default limit is 10 iterations per request."* An *iteration* is one
model generation, which may emit several `tool_use` blocks in parallel — so count iterations, not
tool calls. Three measurements of the same request:

| transport | `mcp_tool_use` blocks | iterations | duration | `stop_reason` |
|---|---:|---:|---:|---|
| sync, `stream: false` | 25 | **11** | 103 s | `pause_turn` |
| sync, `stream: true` | 25 | **11** | 74 s | `pause_turn` |
| batch (run 3) | 43 | **21** | ~20 min | `end_turn` |
| batch (run 5) | 41 | **15** | ~9 min | `end_turn` |
| batch (run 6) | 54 | **22** | ~4 min | **`pause_turn`** |

(iterations = contiguous runs of `tool_use` blocks in the ordered content array; one sync iteration
issued 11 `add_eattribute` calls at once, which is why the raw call count misleads.)

Both synchronous runs stopped at the documented limit regardless of duration, so it is an iteration
budget rather than a timeout — and streaming, the documented remedy for request *timeouts*, makes no
difference. `temperature` (Finding 1) is not the cause either: a run with the field removed paused
identically.

**Batch raises the ceiling; it does not remove it.** Three batch runs went 21, 15 and 22
iterations, the last of which came back `pause_turn` — so the batch budget is roughly double the
synchronous one (~21-22 vs 10) and run 3 finished at exactly 21 by a margin of one. The API
reference describes no Messages-vs-Batches difference at all, so both the higher ceiling and its
existence are measured rather than documented.

**Therefore continuation is required whatever the transport.** Batch alone completed two runs of
three, which is not a foundation to build on. What exhausted run 6 is instructive and will recur:
the agent re-modelled fields that `UplinkMessage` already defines, noticed, and announced *"I need
to start fresh with a clean dataset, removing all the duplicate features I erroneously added"* -
spending its remaining budget on the correction. Any run that backtracks costs iterations, and
backtracking is normal. The documented remedy is to re-send the paused response as-is (no extra
user turn); batch should be read as the thing that makes hitting the ceiling *less likely*, and
continuation as the thing that makes it *survivable*.

The way forward is **both**, in this order: implement `pause_turn` continuation, because no
transport avoids the ceiling, and keep the **batch** service, because it makes the ceiling much
rarer and costs half as much. On the batch service — proven here, half price, and
`ClaudeBatchMessageService` already takes the two strings `ChatCompletion.complete` has; the costs
are that `timeoutSeconds` must grow well past 900 and that a restart discards paid work unless the
batch id is persisted against the fingerprint. Or implement **`pause_turn` continuation** in the
client: re-POST with the assistant content appended and no extra user turn, bounded by a
max-continuations count (Anthropic's own SDK tool runners do not auto-resume either, so this is
expected client work rather than a workaround). Continuation fits the existing client design more
closely, since it is another `sendClaudeRequest` rather than a new transport.

**Streaming was tested and does not help** — see the table above.

Either way, `stop_reason` belongs in `ChatCompletionAdapter`'s no-receipt warning — this took three
runs to pin down only because nothing logged it.

**3. Shape-based sampling can silently discard type evidence.** `ShapeFingerprint` treats an
integer and a double as the same token, so the first payload set collapsed 5 payloads into 4
distinct shapes — and the sample dropped was the one carrying the `EInt` overflow. The window then
closed on `MAX_WAIT` and was flagged `LOW EVIDENCE`. A sample set selected by *structure* does not
necessarily preserve *type* evidence, which is exactly what a model-inference consumer needs. Worked
around here by making each payload structurally distinct; worth addressing properly.

**4. A tunnelled MCP server refuses traffic unless a token is configured.**
`McpAuthenticationFilter` trusts direct loopback callers *only while no token is set*, and
deliberately disqualifies a loopback request carrying a forwarding header — which is precisely what
a tunnel produces. It answers `403 Remote access requires a configured authentication token`.
Set `auth.token` on the servlet's `HttpMCPServerComponent` and the matching
`mcp.authorization.token` on the client. Exposing the endpoint and setting a token are one step,
not two.

**5. `codec.typeMapId` must name a vocabulary the deployed models actually annotate for.** The
runtime was configured `jena-sensors` (the m5airq/waterparc family) while the only type-mapping
annotation present was `typeMapping/lorawan`. An id no model annotates for types nothing at all,
and does so silently. Now overridable per deployment via `EVENTATLAS_CODEC_TYPE_MAP_ID`.

**6. `mcp.tools.enabled` is mandatory, not an optimisation.** `ClaudeHelper` always builds the
toolset with `default_config {enabled:false}` and re-enables only the names in that array, and the
config attribute has no default — so omitting it disables *every* tool on the server and hands the
agent nothing to call.

**7. Disabling a tool does keep its definition out of the request prefix.** Measured on two
otherwise identical requests: 1 tool enabled → **915** input tokens; 21 enabled → **14,021**. This
settles the open question from #30 — the token saving from a task-scoped tool list is real, and
`defer_loading` is not the flag that governs it.

**8. Operational notes.** The MCP runtime needs **Java 21** (on Java 25, aries spifly's ASM fails
with a misleading `ClassFormatError: Weaving hook failed`, four `Caused by` levels above
`Unsupported class file major version 69`) and `-Dgosh.args=--noshutdown` (otherwise the Gogo shell
reaches EOF on a detached stdin and stops the framework, producing a second, unrelated weaving
error). Three deny-all allow-lists on the MCP side had to name the inference namespace before a run
could work: `EMFModelGuard` (both lists), `EMFPackageRegistry.nsuri.allowlist` (gates
`register_package`) and `ModelAtlasPublisher.publish.nsuri.allowlist` (gates
`post_to_model_atlas`).

**9. Quiet logs are not a stalled run.** The metamodel server logs at INFO for only some
operations, so a busy agentic run looks like a ten-minute silence. The ngrok agent's local
inspection API (`http://127.0.0.1:4040/api/requests/http`) carries every request *with its body*,
which is the only practical way to watch such a run live — it revealed a run that looked dead from
both consoles was 140 `tools/call` deep, and allowed the inferred types to be graded from the
`add_eattribute` arguments before the run had finished.

**10. Outbound HTTP resolution is allow-listed, and it blocks the batch path asymmetrically.**
`emf.osgi`'s REST URI handler (pid `org.eclipse.fennec.emf.osgi.urihandler.http`, property
`allowedHosts`) blocks *all* outbound http(s) resolution unless the host is listed — an SSRF guard
against attacker-supplied proxy references in a model. Submitting a batch is a Resource `save()`
and is not gated; every status poll and the result fetch are `load()`s and are. So run 5 submitted
successfully, the batch ran to completion and published its draft, and the runtime then failed on
its first poll with

```
Blocked outbound http(s) resolution of URI 'https://api.anthropic.com/v1/messages/batches/msgbatch_…'
(host 'api.anthropic.com' is not in the configured allow-list)
```

reporting `UNAVAILABLE` for a run that had in fact succeeded — the batch's own result, fetched by
hand afterwards, carried `stop_reason: end_turn` and
`RECEIPT: created https://fennec.eclipse.org/event.atlas/inferred`. With the host allow-listed the
adapter would have returned that line unchanged. The synchronous path never needed
this because a completion is one save-and-read round trip. Fixed by listing the provider host in
`inference.config`; do not use `*`, which disables the guard and makes the handler log a warning
saying so.

**11. The inference is stable across runs.** Runs 3 and 5 were independent and produced the same
package: identical `eType` for every planted trap, the same supertype, and the same
`typeMapping/lorawan` + `Dragino_LSE01` annotation. Run 5 was marginally better — it also modelled
`conduct_SOIL_f`, which run 3 missed, and marked the sometimes-absent `hum_SOIL` / `ec_SOIL`
`unsettable="true"`. The only cosmetic drift was the decoded-object class name (`DecodedObject` vs
`LSE01DecodedObject`). Both packages are kept under `results/`.

**12. A known payload deserializes but does not map.** See Still open.

**13. The codec wrote its own type discriminators into the request, and Anthropic rejects unknown
keys.** After the `Message.content` change (a list of `ContentBlock` rather than a `String`, needed
for `pause_turn` continuation), `TypeStrategy.NONE` was dropped from the request save options — the
reasoning being that `NONE` suppresses *all* type writing, including the `text` / `mcp_tool_use` /
`mcp_tool_result` discriminators the API needs to resume a paused turn. The consequence was a
`_type` key on every object:

```
HTTP 400  messages.0._type: Extra inputs are not permitted
```

Verified against the live API that content-as-array is valid and that `_type` alone is fatal.
The resolution keeps `NONE` globally and re-enables the type **per reference** via
`CodecOptions.CODEC_EREFERENCE_CONFIG` (`typeStrategy=NAME`, `typeKey=type`) on the four
`ContentBlock` containments. Note this had to be done as a *save option*: equivalent
`http://eclipse.org/fennec/codec` annotations on the same references do **not** override the global
config in this codec version, which is filed separately.

**14. `download.file.folder` is joined by string concatenation, twice.**
`ClaudeBatchMessageService` builds the results path as
`config.download_file_folder().concat(batchId)` — no separator — so the file lands next to the
folder rather than in it. `downloadBatchResult` then returns `getFileName()`, which already contains
the folder name, and the reader prepends the folder again:

```
/tmp/eventatlas-inference + eventatlas-inference + msgbatch_…   -> NoSuchFileException
```

The two errors cancel only if the configured folder ends in a separator. Fix is `Path.resolve`
rather than `concat`, and returning a `Path` from `downloadBatchResult` so the folder is never
joined twice. Workaround: give `INFERENCE_DOWNLOAD_FOLDER` a trailing slash (the directory must
already exist — `BodyHandlers.ofFile` will not create it).

**15. Composite authoring tools halved the iteration count.** `emf.osgi-mcp#32` let
`create_epackage` take nested `eClassifiers` and `add_eclass` take nested features. The agent chose
the nested form unprompted — no tool-description hint was needed:

```
create_epackage 'lse01' {eClassifiers: 2}   <- the entire authoring phase, one call
register_package
create_from_json x5                         <- all five samples, one parallel iteration
post_to_model_atlas
```

| | run 6 (before) | run 8 (after) |
|---|---:|---:|
| tool calls | 54 | 22 |
| iterations | 22 (`pause_turn`) | **11 (`end_turn`)** |
| authoring phase | 9 iterations | **1 call** |
| cost | ~$0.7 | ~$0.34 |

This changes Finding 2's conclusion: the task now fits in 11 iterations, and the synchronous path
paused *at* 11. Batch is no longer clearly required — it is marginal — and `pause_turn` continuation
drops from necessary to a prudent backstop. Bulk (array) variants of `add_eattribute` would have
saved nothing here, because parallel calls in one generation already cost one iteration; it was the
*dependency chain* that cost iterations, which is what nesting removes.

**16. The download path fix works.** Run 9 created `/tmp/eventatlas-inference/` (it did not exist
beforehand, so `Files.createDirectories` fired) and wrote the 64 KB results file inside it as
`msgbatch_…`. Watched directly:

```
15:15:06  batch=ended    download_folder=[absent]
15:15:27  batch=ended    download_folder=[msgbatch_01HqhE5vde6papy3KmnQZMJN]
```

So status polling and result download both work, and Finding 14 is closed.

**17. `getResultText()` returns only the first text block, so the receipt is lost.** Run 9 got all
the way to parsing its own downloaded result and still reported:

```
An inference ran for channel 'lorawan/unknown/dragino' (5 sample(s), 308s) but its answer carried
no receipt, so what it did is unknown: I'll work through this systematically: discover existing
models, then author, validate, register, and publish.
```

That string is the agent's **preamble**. The result carries **14 text blocks** and the receipt is in
block **13**; `ClaudeBatchResultResponseImpl.getResultText()` returns on the first one it finds:

```java
for (ContentBlock cb : getResults().get(0).getResult().getMessage().getContent()) {
    if (cb instanceof TextBlock tb) return tb.getText();   // returns block 0
}
```

The synchronous path does not have this bug — `AnswerText.of(...)` in
`…model.inference.chat` joins every text block with `\n`, on the grounds that "several text blocks
in one answer are one message split up". The batch path routes through `getResultText()` instead and
loses that. Fix is to join rather than return early.

Two adjacent points in the same method: `getResults().get(0)` silently ignores every other result in
a multi-request batch (and results arrive in arbitrary order, so "first" is not "first submitted"),
and `isSuccessful()` tests `"end_turn".equals(stopReason)`, so a `pause_turn` result reports
unsuccessful with a null error message rather than as a resumable state.

**18. Run 9 in summary: everything works except receipt extraction.** MQTT ingest, typing failure,
sampling, batch submission, discovery, composite authoring, validation against all five samples,
publish to `draft`, status poll and result download all verified in one run. 9/9 traps correct
again, with `unsettable="true"` on the three sometimes-absent fields, the
`lorawan#//UplinkMessage` supertype and the `Dragino_LSE01` discriminator — four independent runs
now agree on the typing. Only the last hop, turning the downloaded result into a receipt, is
outstanding.

Iteration count varies run to run — 11 (run 8) and 17 (run 9), both `end_turn`, against a batch
ceiling of roughly 21-22. The headroom `#32` created is real but not unlimited, which is why
`pause_turn` continuation stays worth having as a backstop even though it is off the critical path.

**19. One dataset per validated sample, by tool design — and run-to-run validation effort varies a
lot.** Run 9's MCP log shows twelve `Created dataset` lines, eleven of them *after* the package was
registered. That matches the trace exactly:

| call | count |
|---|---:|
| `create_from_json` | 10 |
| `create_dataset` | 2 |
| **dataset-creating calls** | **12** |

`CreateFromJsonTool` creates a fresh dataset on every call (`registry.create(sessionId, seed)`) and
takes no `datasetId` parameter — its schema requires only `eClass` and `data`. So there is no way to
validate several samples into one dataset, and one dataset per validation is unavoidable with the
current tool surface. The datasets are session-scoped, so they go away with the session.

What is worth watching is the **variance**: five samples imply a floor of five
`create_from_json` calls, and run 8 did exactly five. Run 9 did ten — validating each sample about
twice — plus two `create_dataset` calls, meaning it restarted the authoring dataset once. That is
most of the difference between the two runs:

| | run 8 | run 9 |
|---|---:|---:|
| tool calls | 22 | 36 |
| iterations | 11 | 17 |
| `create_from_json` | 5 | 10 |
| input tokens | 191,161 | 411,194 |
| cost | ~$0.34 | ~$0.68 |

Two consequences. **Check the `EMFDatasetRegistry` session caps**: twelve datasets in one session is
not obviously safe, and hitting a cap mid-validation would fail for a reason that looks nothing like
the cause. And the same lesson as Finding 15 applies — the cost is in per-call granularity, not data
volume. An optional `datasetId` on `create_from_json`, letting several samples be validated into one
dataset, would cut both the dataset churn and the iteration count the way nesting did for authoring.

### Refinement: the namespace must be derived per model, not shared

`event.atlas.model.inference`'s `namespace` is handed to the agent as the namespace to publish
under, and the agent uses it **verbatim** as the package nsURI. Every run so far produced:

```xml
<ecore:EPackage name="lse01" nsURI="https://fennec.eclipse.org/event.atlas/inferred" …>
```

The `name` varies between runs (`draginolse01`, `lse01`; `DecodedObject` vs `LSE01DecodedObject`) but
the nsURI never does. So the configured namespace behaves as *one slot for one model*: a second
inference — another channel, another device family, or the same channel after a draft is promoted —
publishes to the same nsURI and collides. That is presumably what `RECEIPT: conflict <nsURI>`
anticipates, but a conflict is the wrong outcome for two genuinely different models.

The fix is to treat the configuration as a **prefix** and have the agent append a segment derived
from the payloads — the discriminator value, the device family, something stable and specific — so
`…/event.atlas/inferred` becomes e.g. `…/event.atlas/inferred/dragino/lse01`. Two things to decide:

- **Who appends.** Leaving it to the agent fits the prompt's philosophy (it already derives the
  family, the conventions and the type map by discovery) but makes the nsURI non-deterministic, which
  matters because the nsURI is the identity a human promotes and a runtime resolves. Deriving it in
  `ModelInferenceService` from the channel is deterministic but assumes one channel carries one
  model.
- **Guarding the prefix.** `ModelAtlasPublisher.publish.nsuri.allowlist` and
  `EMFPackageRegistry.nsuri.allowlist` are prefix-capable (`…/inferred*` is what is already
  configured), so a derived sub-namespace needs no allow-list change — worth keeping that property.

### Refinement: don't start a second inference for a channel already being inferred

Runs are already serialized — `ThreadPoolExecutor(0, 1, …)` with a bounded queue, deliberately, on
the grounds that "two agents authoring into the same namespace at once is how a conflict receipt is
manufactured". Two guards also exist already: `AttemptRegistry` claims a sample-set fingerprint
*before* the run starts, so the same shapes are not inferred twice, and `RunRateLimiter` caps runs
per interval.

What is missing is a **per-channel in-flight guard**. Serialization prevents concurrency but not
sequence: a second sample set from the same channel with a *different* fingerprint — a new shape, or
the same sensor after `maxWaitSeconds` closes another window — is queued and then runs, publishing to
the same nsURI a beat later. The fingerprint claim does not cover it (different shapes, different
fingerprint) and the rate limiter only counts.

So: while a run for channel X is queued or running, drop further sets for X and say so, rather than
queueing them. Two notes for whoever implements it:

- The channel key is the **MQTT topic** (`lorawan/unknown/dragino`), not the configured channel
  `name` — measured, and easy to get wrong.
- It interacts with the namespace refinement above: once each model gets its own derived nsURI, two
  *different* channels inferring at once stops being a collision and becomes legitimate parallelism —
  at which point the serialized executor is the constraint rather than the safeguard. Decide whether
  the guard is per channel (allowing parallel channels later) or global (simpler now).

### Refinement: ask for a structured receipt instead of parsing prose

The receipt is currently a **line of prose** the agent is asked to end with, which the runtime
greps for. Findings 17 and 2 are both symptoms of that: a receipt can be lost in the wrong text
block, or absent because the turn was cut off, and in each case the outcome is "unknown" rather
than reported.

A structured output would make it deterministic — for example a small EClass with an outcome
enum (`CREATED` / `CONFLICT` / `REJECTED` / `FAILED`), the `nsURI`, and a message, so the agent
states whether it managed to publish and what went wrong if not.

The plumbing already exists on both paths:

- `ChatCompletionService.complete(String systemMsg, String userMsg, EClass resultEClass)` returns an
  `EObject`;
- `BatchChatCompletionService.createMessageBatch(id, systemMsg, userMsg, EClass structuredOutputEClass, …)`;
- `ClaudeHelper.createClaudeRequest(…, EClass structuredOutputEClass)`.

One constraint to respect: `…event.atlas.model.inference` deliberately carries **no EMF
dependency** — that is what makes "never registers an inferred package locally" structural. So the
result EClass and the mapping from `EObject` to an outcome belong in
`…model.inference.chat` (which already depends on EMF), with the `ChatCompletion` port either
unchanged (the adapter maps the structured result back to the receipt string inference expects) or
widened to a small outcome record that carries no EMF types.

### Still open

- **Promote and re-ingest.** Promotion `draft → approved → release` is deliberately manual human
  review, so it was not done as part of this test. Afterwards the expected signal is the ingest
  outcome moving from `EMPTY` to `NO_MAPPING`; `APPLIED` additionally needs a Dragino
  `ProviderMapping`.
- **`NO_MAPPING` on the known payload.** A known `EM310-UDL` payload deserialized correctly
  (`1 object(s) of type EM310UDLUplink`) but matched no mapping, despite a mapping file being
  present. Untested hypothesis: the `key.feature: "mid"` property that the docker config carries
  and `inference.config` omits.
- **One namespace holds one draft.** The inferred package took the configured `namespace` verbatim
  as its nsURI, so a second inferred model would collide — which is what
  `RECEIPT: conflict <nsURI>` anticipates, but it means the namespace is per-model, not per-runtime.

### Reproducing

```bash
# 1. Model Atlas (jena scope, with the lorawan + em310udl packages in its release stage)
cd <model.atlas>/docker/dockercompose && docker compose -f docker-compose-jena.yml up -d

# 2. MCP broker for the southbound
docker run -d --rm --name eventatlas-mosquitto -p 1883:1883 \
    eclipse-mosquitto:2 mosquitto -c /mosquitto-no-auth.conf

# 3. Metamodel MCP server — Java 21 and --noshutdown both matter
cd <emf.osgi-mcp> && ./gradlew :org.eclipse.fennec.mcp.emf.runtime:export.launch
/usr/lib/jvm/java-21-openjdk-amd64/bin/java -Dgosh.args=--noshutdown \
    -jar org.eclipse.fennec.mcp.emf.runtime/generated/distributions/executable/launch.jar

# 4. Expose it publicly and set a matching auth.token / mcp.authorization.token

# 5. The runtime
./gradlew :org.eclipse.fennec.event.atlas.mapping.runtime:resolve.inference
./gradlew :org.eclipse.fennec.event.atlas.mapping.runtime:export.inference

# 6. Feed an unknown payload
docker exec -i eventatlas-mosquitto mosquitto_pub -h localhost -p 1883 \
    -i test -t lorawan/unknown/dragino -m "$(cat payload.json)"
```

Credentials and endpoints come from the gitignored `…mapping.runtime/secrets.bndrun`; see
`secrets.bndrun.template`.
