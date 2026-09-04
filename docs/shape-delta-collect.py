import json, os, subprocess, sys, time
from collections import Counter
from datetime import datetime, timedelta, timezone
S = os.path.dirname(os.path.abspath(__file__))
LABEL, ARM, SINCE = sys.argv[1], sys.argv[2], sys.argv[3]  # SINCE: UTC "HH:MM:SS" of the publish

# SINCE arrives as a bare time of day, so it must be pinned to a DATE before it can be compared.
# Comparing `created_at[11:19] >= SINCE` reintroduces the very bug the wait below exists to
# prevent: a previous run's batch at 12:16:26 yesterday compares as >= 08:12:54 today, so the
# loop locks onto the stale batch on its first pass and reports its numbers as this run's.
# Assume the most recent occurrence of that wall-clock time (yesterday's if it is still ahead of
# now, which is what a just-before-midnight publish collected after midnight looks like).
_now = datetime.now(timezone.utc)
_since = datetime.combine(_now.date(), datetime.strptime(SINCE, "%H:%M:%S").time(), timezone.utc)
if _since > _now + timedelta(minutes=5):
    _since -= timedelta(days=1)

def _created(b):
    return datetime.fromisoformat(b["created_at"].replace("Z", "+00:00"))
KEY = subprocess.run(["bash","-c","grep -oP '(?<=-DANTHROPIC_API_KEY=)[^\\s\\\\]+' "
    "/opt/git/event.atlas/org.eclipse.fennec.event.atlas.mapping.runtime/secrets.bndrun | head -1"],
    capture_output=True, text=True).stdout.strip()
H = ["-H", f"x-api-key: {KEY}", "-H", "anthropic-version: 2023-06-01"]

def api(path):
    out = subprocess.run(["curl","-s",f"https://api.anthropic.com/v1/messages/batches{path}"]+H,
                         capture_output=True, text=True).stdout
    return json.loads(out)

# A batch created BEFORE the publish is a previous run's - reading it silently reported run 1's
# numbers twice. Wait for one created at or after the publish instead.
bid = None
for _ in range(20):
    newest = api("?limit=1")["data"][0]
    if _created(newest) >= _since:
        bid = newest["id"]; break
    time.sleep(10)
if bid is None:
    print(f"{LABEL}: no batch created since {SINCE} UTC - did the window close?"); sys.exit(1)
for _ in range(30):
    b = api(f"/{bid}")
    if b["processing_status"] == "ended":
        break
    time.sleep(15)
else:
    print(f"{LABEL}: batch {bid} still {b['processing_status']}"); sys.exit(1)

raw = subprocess.run(["curl","-s",f"https://api.anthropic.com/v1/messages/batches/{bid}/results"]+H,
                     capture_output=True, text=True).stdout.strip()
r = json.loads(raw)
open(f"{S}/{LABEL}.jsonl","w").write(raw)
if r["result"]["type"] != "succeeded":
    print(f"{LABEL} ({ARM}): {r['result']['type']} -> {json.dumps(r['result'].get('error'))[:200]}"); sys.exit(1)
m = r["result"]["message"]; c = m["content"]; kinds = [x["type"] for x in c]
it = 0; prev = None
for k in kinds:
    if k == "mcp_tool_use" and prev != "mcp_tool_use": it += 1
    prev = k
u = m["usage"]; names = [x["name"] for x in c if x["type"] == "mcp_tool_use"]

def feats(d, out):
    if isinstance(d, dict):
        if "name" in d and ("eType" in d or "type" in d):
            out.append((d.get("name"), str(d.get("eType") or d.get("type")).split("//")[-1]))
        for v in d.values(): feats(v, out)
    elif isinstance(d, list):
        for v in d: feats(v, out)
    return out
first = next((dict(feats(x["input"], [])) for x in c
              if x["type"] == "mcp_tool_use" and x["name"] == "create_epackage"), {})
row = dict(label=LABEL, arm=ARM, batch=bid, stop=m.get("stop_reason"), iters=it, calls=len(names),
           input=u["input_tokens"], out=u["output_tokens"], cread=u["cache_read_input_tokens"],
           ccreate=u["cache_creation_input_tokens"],
           ds18b20=first.get("temp_DS18B20","-"), hum="hum_SOIL" in first, ec="ec_SOIL" in first,
           nfeat=len(first), epkg=names.count("create_epackage"),
           modify=names.count("modify_feature"), reg=names.count("register_package"),
           published=names.count("post_to_model_atlas"))
with open(f"{S}/results.jsonl","a") as f: f.write(json.dumps(row)+"\n")
print(f"{LABEL} ({ARM})  stop={row['stop']}  ITERS={it}  calls={len(names)}  "
      f"input={u['input_tokens']}  out={u['output_tokens']}  cread={row['cread']}  ccreate={row['ccreate']}")
print(f"   temp_DS18B20={row['ds18b20']}  hum_SOIL={row['hum']}  ec_SOIL={row['ec']}  "
      f"features={row['nfeat']}  create_epackage={row['epkg']}  modify_feature={row['modify']}  "
      f"register={row['reg']}  published={row['published']}")
print("   tools:", dict(Counter(names)))
