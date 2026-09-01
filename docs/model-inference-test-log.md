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

> **Implemented 2026-08-31**, agent-derived, and confirmed on the wire the same day — see
> *the chain end to end with a structured receipt* below.

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

> **Implemented 2026-08-31**, per channel — see *one run per channel at a time* below.

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

> **Implemented 2026-08-31.** It cost more than this section assumed: the schema never
> reached the wire because of a codec regression, and once it did it stopped the agent
> working. Both are written up below.

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
- ~~**`NO_MAPPING` on the known payload.**~~ Closed 2026-08-31 - see below.
- ~~**One namespace holds one draft.**~~ Closed 2026-08-31: the configured value is a prefix the
  agent extends, and a run has published `…/inferred/dragino-lse01` under it.

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

## 2026-08-31 — why the known payload reported `NO_MAPPING`

Two independent causes, neither of them the `key.feature` hypothesis, which is wrong: neither
registry reads the entry key (`ProviderMappingRegistryImpl` indexes by `mapping.getMid()`,
`MappingProfileRegistryImpl` by `profile.getProfileId()`; `entry.key()` appears only in log
messages), and setting it could only ever *lose* entries, because `FileEObjectProvider` skips an
object whose key feature is absent while the default `<fileName>#<uriFragment>` key always exists.

**Cause 1 — the mapping directory was never read.** `config.json` defaults `locations` to the
cwd-relative `runtime/mappings`, but the skeleton lives in the runtime project. Running the
exported jar from the workspace root:

```
WARNING: Provider mapping-files: location runtime/mappings does not exist - skipping
INFO: Registry sensinact-mappings: initial load complete (0 entries) - services published
```

So the registry came up empty and `NO_MAPPING` was strictly correct — there was no mapping.
`run.inference` hid it, because Gradle's working directory *is* the runtime project.

Fixed by pinning both directories in `inference.bndrun`'s `-runproperties`. Two bnd details,
both established by exporting and reading `launcher.properties`, both easy to get wrong:
`-runvm -D` never reaches an exported jar (there are no JVM arguments left to set once
`java -jar` is running) — only `-runproperties` are baked in; and `${basedir}`/`${project}`
expand to the export's staging copy, so `${.}`, the directory of the file itself, is the macro
that survives. Guarded by `InferenceRuntimeLocationsTest`.

**Cause 2 — the mapping lost a race with the Atlas, permanently.** With the directory fixed the
mapping was found and then rejected, because a file provider loads and validates synchronously at
activation while the Atlas publishes its EPackages after an HTTP round trip:

| line | |
|---:|---|
| 177 | `Registry sensinact-mappings: initial load complete (1 entries)` |
| 191 | `SEVERE: ... has missing or unresolved provider classes [...em310udl#//EM310UDLUplink] — Skipping` |
| 264 | `Published remote EPackage http://www.example.org/lorawan/specific/em310udl` |

73 lines too late, and a file provider never re-loads, so the mapping was gone for the life of the
runtime. Fixed by parking such an entry instead of dropping it: `ProviderMappingRegistryImpl` now
holds entries that are well-formed but not yet resolvable and retries them as EPackages arrive,
bound as a dynamic multiple `EPackage` reference. Malformed entries — not a mapping, no `mid`, no
provider classes at all — are still dropped on the spot.

Two things that fell out of implementing it:

- **`dispose()` is both the `@Deactivate` method and a public interface method** that six tests
  call to reset a *running* registry. Shutting the retry executor down there left the component
  alive but permanently unable to retry. Deactivation is now its own method.
- **Resolving only `providerClasses` is not enough.** The first live run after the fix registered
  the mapping and then failed on the payload with `the feature 'null' is not a valid feature`:
  `valueFeature`, `featurePath` and the admin references are nsURI proxies too, and the file
  provider's resource set cannot see an EPackage published after it was created either. The
  resolution pass therefore sweeps every non-containment reference of the whole mapping, walking
  the `#/`, `#//Name` and `#//Name/feature` fragment forms itself rather than handing them to
  `EcoreUtil.resolve`, which would treat an unknown nsURI as a URL and try to fetch it.

**Verified end to end** against the jena Atlas on 8080 and a mosquitto on 1883, with the exported
`inference.jar`:

```
INFO: Registering provider mapping for 'em310udl-battery-sensor' into registry
INFO: Model for provider 'EM310UDL Battery Sensor' -> 'em310udl-battery-sensor' successfully registered.
INFO: Pushed payload from 'lorawan/known/em310' - 1 object(s), 1 mapping(s) applied
```

`NO_MAPPING` -> `APPLIED`, with no `Error getting raw value`. `LateModelMappingTest` is the
regression guard (69 OSGi tests now, up from 68).

Two smaller things noticed while doing it, neither fixed:

- `runtime/profiles/.keep` is loaded like any other file and logs a SAX warning with a stack trace
  at every start — `FileEObjectProvider.filesOf` walks every regular file with no extension
  filter. `runtime/mappings/.keep` was removed, the directory having a real mapping in it now.
- The paho MQTT client writes its persistence directory relative to the working directory, so
  running the jar from the workspace root leaves a `paho<n>-tcplocalhost1883/` behind — which bnd
  then sweeps into the Gradle build as a project. Run it from a scratch directory.

## 2026-08-31 — a paused turn is resumed instead of reported as a failure

Finding 2 measured that batch raises the provider's server-side iteration ceiling without
removing it — 21, 15 and 22 iterations across three runs, the last coming back `pause_turn` — and
concluded that "continuation is required whatever the transport". `nsc` has since grown the two
pieces that make it possible, `isPaused` and `continueMessageBatch`, and
`BatchChatCompletionAdapter` now uses them.

What it was doing before is worth stating plainly, because it is worse than "missing feature". A
paused result is neither success nor failure: `isSuccessful()` is false and `getErrorMessage()`
is null. The adapter tested success first, so a paused turn came out as

```
Completion batch 'msgbatch_…' failed: null
```

which model inference records as an `UNAVAILABLE` receipt and then refuses to retry for
`retryAfterUnavailableSeconds` — an hour by default. So a run that had done most of the work was
reported as a provider outage, and the fingerprint was locked out afterwards.

The adapter now checks `isPaused` **before** success and, when a turn is paused, submits a fresh
batch carrying the assistant content so far, up to `maxContinuations` times (default 2), joining
what each turn said with a newline. The join is insurance: the receipt is emitted at the end, so
the final turn should carry it, but a pause that lands mid-sentence would otherwise split it.

Two things that fall out of how the API is shaped:

- **The continuation is built from the same prompt, not replayed from the paused batch.** A batch
  result carries the assistant turn but not the request that produced it, so the caller supplies
  the system and user message again. Ours is a pure function of the sample set, so rebuilding
  yields the identical turn — nsc's own plan flagged this as its one open design question, because
  `TREND_ANALYSIS_PROMPT` embeds `LocalDateTime.now()` and cannot be rebuilt faithfully. Everything
  else (model, tools, MCP servers and their authorization) comes from the current configuration on
  purpose: a token has to be minted fresh, not replayed from a batch that may be hours old.
- **The bound is deliberately low.** Each continuation is a whole new batch with its own queueing,
  so `timeoutSeconds` on the inference configuration is usually what stops a long run first, not
  this. `maxContinuations: 0` restores the submit-once behaviour.

Exhaustion gets its own message naming the bound and the configuration to raise, rather than
falling back to the "failed: null" this set out to fix. `BatchChatCompletionAdapterTest` covers
the five cases: finished, paused-then-finished, paused past the bound, continuation disabled, and
a genuine failure that must not be resumed.

## 2026-08-31 — the namespace is a prefix the agent extends

The first of the three refinements, implemented as written up above, with the open question
("who appends the segment") decided in favour of the agent.

The prompt now asks for a namespace *beneath* the configured value rather than handing the value
over as the nsURI, and says why — that the namespace must identify this model and no other,
because a different model published later must not land on it, and that it is the identity a
reviewer promotes and a runtime resolves. What the segment should be is deliberately not stated.
That is the same finding as the codec type map: the agent already derives the family, the sibling
conventions and the discriminator by discovery, and it can see how the namespaces it finds are
built, so naming a scheme here would invite it to skip the discovery that would have found the
better one. `InferencePromptTest` asserts both halves — that the prefix is stated as a prefix, and
that the words "discriminator" and "device family" do not appear.

**Nothing changes on the MCP server.** Both `EMFPackageRegistry.nsuri.allowlist` and
`ModelAtlasPublisher.publish.nsuri.allowlist` already carry
`https://fennec.eclipse.org/event.atlas/inferred*`, and `NsUriPatterns` treats a trailing `*` as a
prefix match, so a derived sub-namespace passes as it stands. Checked rather than assumed.

**What this costs.** The nsURI is no longer known before the run, which promotes the receipt from
informative to load-bearing: it is now the only thing that says where the draft went. That raises
the value of the third refinement (a structured receipt) without changing its shape.

`ModelInferenceService` therefore warns when a `created` or `conflict` receipt names a namespace
outside the configured prefix. A warning, not a failure: the draft is already published by the
time the receipt is read, so there is nothing left to prevent, and since publication is guarded
server-side by that same prefix allow-list, a draft landing outside it means either the allow-list
is wider than the prefix or the agent named an nsURI it did not publish to. Both are an operator's
problem, and both are worth knowing before promoting the draft.

Still to verify against a live run: that the agent actually picks a sensible segment, and that two
different channels no longer collide. The previous four runs all produced
`nsURI="https://fennec.eclipse.org/event.atlas/inferred"` exactly, so any run under this prompt
that produces something longer is the signal.

## 2026-08-31 — one run per channel at a time

The second refinement, narrowed on purpose: while a run for a channel is queued or in progress,
further sets for that channel are dropped rather than queued.

**What "a different fingerprint" actually covers**, since that was the question the design turned
on. `SampleSetFingerprint` hashes the format, the declared nsURI and the sorted union of the
samples' *shapes* — not the channel, not the values, not the counts, not the timestamps. So a
second set differs whenever the *set of shapes* differs, which happens in two ways:

- a genuinely different device on the same topic — real, but rare, since a topic usually carries
  one family;
- far more commonly, the same device in a later window, where one payload carries a field the
  earlier window never saw, or where the later window *misses* a variant the earlier one had.

The second case is the one that matters, and it cuts both ways: the later set is not necessarily
better evidence. A window closing on `quietSamples` or `maxWaitSeconds` sees whatever arrived in
it, so a second run could just as easily be authored from *less* variety than the first. Waiting
for it buys nothing, which is why the first run is simply left to finish.

Note that a pure type-widening difference does **not** produce a new fingerprint: `ShapeFingerprint`
treats int and double as one token, still open from 2026-08-28. So the case this guard fires on is
a field appearing or disappearing, not a value growing.

Implementation notes:

- The key is the **MQTT topic** (`PayloadSampleSet.source()`), not the configured channel name.
- The in-flight claim is taken **before** the fingerprint claim and the rate limiter, so a set
  dropped by this guard has not spent either on the way in.
- Per channel rather than global. Now that each model publishes under its own derived namespace,
  two channels inferring at once is legitimate rather than a collision; the runs are still
  serialized by the single runner thread, but that is a cost decision, not a safeguard, and can be
  relaxed without touching this.

**A pre-existing leak came out with it.** The queue-full rejection handler logged the drop but
released neither the fingerprint claim nor the rate-limiter token, so a set dropped that way was
never inferred again and had still spent a run against the cap. The handler is given the task now
— a named `QueuedRun` rather than a lambda, since the handler is handed the task and nothing else
— and undoes both.

One existing test changed meaning: "a genuinely new shape is inferred again" fired the second set
while the first run was still in flight, which is exactly what is now refused. It asserts the same
thing across the run boundary instead, and two new tests cover the guard itself and its
per-channel scope.

## 2026-08-31 — the chain end to end with a structured receipt

Four batches, of which two cost nothing, one was wasted and one did the job. What they cost is
worth stating plainly, because two of the three failures were free and that changes how cautiously
this is worth iterating on: a request Anthropic rejects at validation is billed for nothing at all.

| # | outcome | tokens in | what it taught |
|---|---|---:|---|
| 1 | 401 at submission | — | the exported jar never gets `secrets.bndrun` |
| 2 | rejected at validation | — | the schema went out as a `$ref` |
| 3 | rejected at validation | — | deploying `codec.jsonschema` does not fix that |
| 4 | discovery only, `end_turn` | 114,625 | a schema leaves the agent no way to narrate |
| 5 | **published** | 370,699 | — |

### The schema never reached the wire, because of a codec regression

`OutputFormat.schema` in `claude-chat-completion.ecore` is annotated
`valueWriterName="eClassToJsonSchema"`, and `ClaudeHelper` sets the matching
`CodecJsonSchemaOptions`. Both ends expect that writer. It was not reachable, so the codec fell
back to writing the `EClass` as an EMF reference:

```json
"schema":{"_type":"…Ecore#//EClass","$ref":"//fennec.eclipse.org/event.atlas/inference/1.0#//InferenceResult"}
```

which Anthropic refuses outright — *"External schema references are not supported"*.

The cause is `fennec-codec` `6147ee4` (2026-08-11, *"public resource factories for non-OSGi use
(#147)"*), which removed `@Component(service = CodecValueWriter.class)` from eight value handlers
across the jsonschema and openapi bundles and made them plain objects, filled into a **copy** of
the shared registry by the factories. They therefore only reach resources those factories create;
a completion request, serialized as ordinary `application/json`, never sees them. Deploying
`org.eclipse.fennec.codec.jsonschema` changes nothing, which run 3 confirmed.

This is not specific to event.atlas — `TrendAnalysis` in `nsc` uses the same annotated feature and
the same `application/json` request resource, so its structured output should be failing the same
way, appearing as a batch that "ended as FAILED" with no reason. Written up as
`nsc/docs/issue-codec-value-handler-services.md`; being fixed in the codec.

Meanwhile `SchemaValueWriter` in `…model.inference.chat` republished the writer as a service — the
same annotation #147 deleted, from the bundle that needs it. The shared registry's own javadoc
documents that whiteboard as the way to contribute a handler, and its `removeValueWriter` gives
exactly the lifecycle safety `registry.copy()` was reaching for.

> **Resolved 2026-09-01.** The codec snapshot restores the components: the
> `0.1.0-SNAPSHOT` jsonschema jar again ships
> `OSGI-INF/…v2.value.EClassValueWriter.xml` providing
> `org.eclipse.fennec.codec.value.CodecValueWriter`, along with the matching reader and the two
> `EPackage` handlers. `SchemaValueWriter` and the bundle's `org.eclipse.fennec.codec.jsonschema`
> buildpath entry are deleted; the chat bundle no longer imports the package at all. Note that
> `inference.bndrun` still needs `bnd.identity;id='org.eclipse.fennec.codec.jsonschema'` in
> `-runrequires` — with the import gone, nothing else would pull the bundle in, and the resolver
> does not see the DS service that now registers the writer.

### A schema leaves the agent no way to think out loud

With the schema finally on the wire, run 4 came back `end_turn` after 13 tool calls — all
discovery, no authoring, nothing published — and said so itself:

```json
{"status":"NOT_INFERRED","nsUri":null,"message":"Discovery complete. Now authoring the Dragino LSE01 package."}
```

It intended to continue. `output_config` constrains **every** text block to the schema, so the
model has no channel for narration: the progress note it wrote *was* a final answer, and the turn
ended on it. Structured output and an agentic loop pull against each other, and nothing but a live
run shows it — the request was well formed and every unit test passed.

The prompt now says there is no way to speak in passing:

> Anything you write is your final answer and ends the run — there is no way to say something in
> passing. So write nothing at all until the work above is done: no plan, no progress note, no
> summary of what you are about to do next. Keep working.

That was the whole fix. Run 5, same payloads, same everything else: 32 tool calls, silent
throughout, one answer at the end. `InferencePromptTest` pins the sentence so it cannot be tidied
away.

The fallback if it had not worked was two-phase — the agentic turn with no schema, then a second
cheap completion converting its final text into `InferenceResult` — which keeps determinism
without constraining the loop. Not needed, but it is the answer if this recurs.

### Run 5

```
stop_reason end_turn · 32 tool calls · 370,699 in / 6,009 out
list_registry → list_annotation_sources → find_classes_by_annotation → list_metamodel →
describe_eclass ×9 → export_package → describe_aspects → find_classes_by_annotation →
export_package → describe_aspects → create_dataset → create_epackage → register_package →
create_from_json ×5 → export_dataset ×5 → post_to_model_atlas
```

```json
{"status":"PUBLISHED",
 "nsUri":"https://fennec.eclipse.org/event.atlas/inferred/dragino-lse01",
 "message":"Package lse01 published as a draft under the Dragino LSE01 soil-sensor namespace; a
            reviewer should promote it to promote the LoRaWAN type-discriminator 'Dragino_LSE01'
            and verify that temp_DS18B20 is correctly modelled as an optional EString (it is
            absent in sample 3 but present as '0.00' in others)."}
```

Three things proven at once:

- **The namespace is the agent's.** `…/inferred/dragino-lse01`, where all four runs of 2026-08-28
  produced the bare `…/inferred` verbatim. No scheme was suggested to it, and the server-side
  prefix allow-list passed the longer nsURI unchanged, as designed.
- **The receipt is structured.** The nsURI arrives in its own field rather than scraped out of
  prose — which matters more than it did this morning, since the agent now chooses that nsURI and
  nothing else records it.
- **The draft is in the Atlas**: `lse01 -> https://fennec.eclipse.org/event.atlas/inferred/dragino-lse01`.

The package reuses what it found rather than re-modelling it: `LSE01Uplink` extends
`lorawan#//UplinkMessage`, and carries `typeDiscriminator: Dragino_LSE01` under
`typeMapping/lorawan`. Neither the supertype nor the annotation source is mentioned anywhere in
the prompt. The string/double distinctions are all correct — `water_SOIL` EString against
`water_SOIL_f` EDouble, `temp_DS18B20` EString because it arrives as `"0.00"`.

Two things it did not do, both worth knowing:

- **No `unsettable="true"` anywhere.** The runs of 2026-08-28 marked the sometimes-absent fields;
  this one did not, though its message shows it *noticed* the absence and told the reviewer
  instead. The same knowledge in a weaker form, and a candidate for the prompt if it recurs.
- **The `uplinkId` overflow trap never reached it.** `uplinkId` lives on `UplinkMessage` in the
  released `lorawan` package, which the agent reused rather than re-modelled, so there was nothing
  to type. The trap was misplaced, not missed — a trap has to sit on a field the new model will
  actually own.

### Two things about running it at all

- **`secrets.bndrun` does not reach an exported jar.** It delivers everything through
  `-runvm -D`, and bnd bakes only `-runproperties` into an exported jar — the same finding as the
  mapping directories earlier the same day. Run 1's 401 was an empty `api.key`, and its
  `mcp_servers[0].url` was empty too. Launch through Gradle/bndtools and it works; run the jar and
  the values have to come from the environment instead, which `config.json` reads first anyway.
  `secrets.bndrun.template` still implies otherwise.
- **A batch that "ended as FAILED" says nothing about why.** `BatchChatCompletionAdapter` reports
  only the status; the reason sat in the results endpoint the whole time and had to be fetched by
  hand with curl. Fetching and logging it would have turned two rounds of guesswork into one line.

---

## 2026-09-01 — promote-and-reingest, all the way to a value in the twin

### Objective

The one link the chain had never been driven through: take a *published draft*, promote it, and
show a runtime ingesting the payload it was inferred from — not just deserializing it, but writing
mapped values into the twin. Everything before this had stopped at the receipt.

Run from a **clean** Atlas: yesterday's `lse01` draft was deleted, not promoted, so the inference
ran from scratch against a `jena` scope holding only `lorawan`, `em310udl` and `Buerger`.

### The run

Same five trap payloads as 2026-08-28 (rebuilt — they live in a scratchpad and do not survive).

| | |
|---|---|
| duration | **1m30s** to a published draft; the batch itself answered in 247s |
| tool calls | **30**, `end_turn` |
| discovery | found `extends lorawan#//UplinkMessage` and `typeDiscriminator: Dragino_LSE01` unaided |
| namespace | `…/inferred/lorawan/dragino-lse01/1.0` — deeper than 08-31's `…/inferred/dragino-lse01`, and again entirely the agent's choice |
| type traps | **7/7 correct** — `conduct_SOIL` EDouble, `pulse_total` ELong, `temp_DS18B20` EString, `hum_SOIL` EDouble, `ec_SOIL` EInt, `s_flag` EInt (control), `water_SOIL` EString |

The receipt was structured and genuinely useful — it flagged `pulse_total as ELong` against "the
observed 32-bit unsigned range seen in sample 4", i.e. it explained the trap back to the reviewer.

**`ShapeFingerprint` now keeps `int` and `float` apart** (`VALUE_NUMBER_INT` vs
`VALUE_NUMBER_FLOAT`), so Finding 3's collapse is fixed: the traps supply their own shape
distinctness and the window closed on `targetSamples`, not on `MAX_WAIT`, with no `LOW EVIDENCE`.
Array indices still collapse, so duplicating an `rxInfo` entry does *not* make a payload distinct.

### The result, end to end

Provider `LST25628782`, model `dragino-lse01-soil`, read back through the Gogo shell:

| service | resource | after p1 | after p4 |
|---|---|---|---|
| `soil` | `temperature` | 13.25 | 13.25 |
| `soil` | `moisture` | 13.74 | 13.74 |
| `soil` | `conductivity` | 35.0 | **40.0** |
| `soil` | `pulseTotal` | 1200 | **3221225472** |
| `battery` | `level` | 3.301 | 3.301 |

**`pulseTotal` = 3221225472 is the headline.** It is `2^31 + 2^30` and does not fit a signed
32-bit int; the agent widened the field to `ELong` on the strength of one sample in five, and that
type survived JSON → inferred ecore → Model Atlas → promotion → mapping → sensinact resource. The
`conductivity` change also confirms the twin is updated per payload, not populated once.

It also settles a worry: every one of those values arrives *through* the inferred `object`
containment reference, whose eType the agent wrote as a **relative** href
(`draginolse01.ecore#//DecodedObject`). It resolves. `admin/friendlyName` would have been the only
populated resource had it not.

### Findings

**20. Promoting a package does not reach a running runtime; promoting a mapping does.** The
central finding, and an asymmetry worth internalising. After `draft → approved → release` the
runtime kept answering `No EClass found for discriminator: Dragino_LSE01`, and the Atlas served
**zero** content requests — while `HEAD /scopes/jena` showed `Last-Modified` at the moment of the
promotion, so the ETag gate opened and the drift check *did* run. The client filtered the package
out: `DriftWatcher.handleChangedNsUris` skips on `!held.contains(nsUri)` and
`handleChangedObjects` on `!anyHeld`. Drift refreshes what you hold; it does not discover. Nor is
there a polling counterpart for EPackages — `EagerPrefetch` runs once at activation. A restart
fixed it instantly (21 packages fetched instead of 20).

LAZY mode is not the missing switch either: `FeaturePathTypeResolver.scan` searches *registered*
packages for a matching discriminator and never asks for an nsURI, so a lazy registry has nothing
to resolve. Discovery has to be pushed by the client.

*Mappings escape this* because `AtlasObjectSync.syncRegistry` re-runs `listObjectIds()` every
pass. A `ProviderMapping` POSTed to `jena/registries/sensinactmapping` went from `NO_MAPPING` at
10:08:16 to `1 mapping(s) applied` at 10:09:14 **with no restart** — the only change in between
was the POST. `inference.bndrun` gained
`org.eclipse.fennec.model.atlas.eobject.provider` and an `AtlasEObjectProvider~jena` block
(`registries: [sensinactmapping]`, `key.feature: mid`, `refresh.interval.ms: 15000`) for this;
`FileEObjectProvider` cannot, as it walks its directory once at activation.
Sketched: `nsc/docs/issue-atlas-drift-ignores-new-packages.md`.

**21. `register.in.global.registry` was silently corrupting the EPackage registry.** Adding the
Atlas EObject provider turned this from dormant to fatal:

```
ClassCastException: EFactoryImpl cannot be cast to ScopeApiFactory
    at ScopeApiFactoryImpl.init / ScopeApiFactory.<clinit>
    at RemoteReadableScopeService.parseScopeInfo … at AtlasObjectSync.syncRegistry
```

`RemoteEPackagePublisher.mirrorToGlobal` does an unconditional
`EPackage.Registry.INSTANCE.put(nsUri, ePackage)`, and the `jena` scope inherits the root `atlas`
scope's **17 system packages** — so the eager sweep had been replacing *generated* EPackages with
*dynamic* ones all along, `Ecore`, the codec and `event.atlas/mapping/1.0` among them. Nothing
complained because a generated factory's `<clinit>` runs **once**: whichever happens first, the
class being touched or the sweep, decides for the life of the framework. `AtlasObjectSync` was
simply the first thing to touch `ScopeApiFactory` *after* the sweep.

Worked around with a 17-entry `nsuri.deny.list` (the gate in
`RemoteEPackageProviderImpl.isPublishable` runs before the cache and on every path, so it covers
the eager sweep and the drift watcher alike). Verify it is live by counting the sweep in the Atlas
log — **4 domain packages, not 21**. A deny-list rather than an allow-list because matching is
exact `contains` with no prefix support, and an inferred nsURI differs on every run.
Sketched: `nsc/docs/issue-atlas-global-registry-clobber.md`.

**22. An empty `providers` is not an empty twin.** With no `sensinact.session.manager` config the
session manager denies everything, and the Gogo commands split two ways: `get` and `services`
raise `NotPermittedException: The user <ANONYMOUS> …`, but **`providers` returns empty**, because
an unreadable provider is filtered out of the listing rather than reported. That reads exactly
like a mapping that never applied. `inference.bndrun` needs both
`org.eclipse.sensinact.gateway.northbound.gogo-shell` and
`"sensinact.session.manager": {"auth.policy": "ALLOW_ALL"}` — the policy both deployed runtimes
already set in their `sensinact.json`.

**23. The Atlas object registry does not rewrite hrefs.** All 22 nsURI hrefs in the uploaded
`ProviderMapping` came back intact from `jena/registries/sensinactmapping/content`. The rewrite
described in `nsc/docs/issue-atlas-href-rewrite.md` affects the *schema* content endpoint, not
object registries.

### Reproducing

As 2026-08-28, plus:

```bash
# promote the inferred package (schema registry: TWO hops)
curl -X POST "$B/jena/schema/stages/draft/actions/transition"    -H 'Content-Type: application/json' -d '{"_type":"…#//StageTransitionRequest","objectId":"<uuid>","targetStage":"approved"}'
curl -X POST "$B/jena/schema/stages/approved/actions/transition" -H 'Content-Type: application/json' -d '{"_type":"…#//StageTransitionRequest","objectId":"<uuid>","targetStage":"release"}'
# then RESTART the runtime - the package will not arrive any other way

# the mapping, by contrast, is picked up live (objectId = the mapping's mid)
curl -X POST "$B/jena/registries/sensinactmapping/stages/release/dragino-lse01-soil?name=dragino-lse01-soil&override=true" \
     -H 'Content-Type: application/xmi' --data-binary @dragino-lse01-soil-mapping.xmi
```

Watching a run without the Eclipse console: **ngrok's local API**
(`http://127.0.0.1:4040/api/requests/http`) lists every MCP tool call with the request body in
`raw`, which is the best progress signal available. The Atlas container log has no access log and
does not log object-registry reads, so silence there proves nothing — but it does log two records
per EPackage served, which is what makes the sweep countable.
