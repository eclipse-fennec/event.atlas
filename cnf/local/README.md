# Local repository — TEMPORARY bundles

These jars are **local builds**, copied here so the model-inference work can be developed and tested
before the bundles it needs exist in any published repository. They are not a permanent part of this
workspace and they **shadow** any released artifact with the same bundle symbolic name.

| Bundle | Built from | Remove when |
|---|---|---|
| `org.eclipse.fennec.ai.chat.completion.api` | the AI chat-completion workspace | it publishes a snapshot |
| `org.eclipse.fennec.ai.apis.meta.model` | ” | ” |
| `org.eclipse.fennec.ai.chat.completion.claude.model` | ” | ” |
| `org.eclipse.fennec.ai.chat.completion.impl.claude` | ” | ” |

`mcp.endpoint` **is no longer here** — since 2026-09-01 it resolves from the published
`org.eclipse.fennec.mcp:org.eclipse.fennec.mcp.endpoint:0.1.0-SNAPSHOT` declared in
`cnf/ext/central.mvn`. It was here originally because no release carried it:
`emf.osgi-mcp#31` split `MCPEndpoint` + `RemoteMCPEndpoint` out of `mcp.api` so that a consumer
which only *addresses* a remote MCP server needs none of the MCP SDK. Its `Import-Package` is
`java.lang` and nothing else, which is why this workspace deploys no `mcp-core`, `reactor-core` or
`reactive-streams` — see the note in `cnf/ext/central.mvn`. `RemoteMCPEndpoint` (the DS component
that registers an `MCPEndpoint` from `server.name` + `server.url` config) lives in it, so no MCP
*server* bundle is needed here.

After adding or replacing a jar here, regenerate `index.xml` — the `Local` repository is a
`LocalIndexedRepo` and will not see files it has not indexed.
