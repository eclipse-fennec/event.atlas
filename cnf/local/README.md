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
| `org.eclipse.fennec.mcp.api` | `eclipse-fennec/emf.osgi-mcp` | a release carries the `MCPEndpoint` split |

`mcp.api` is here rather than from the `fennecMCP` library because the released version predates the
`MCPServer` / `MCPEndpoint` split. `RemoteMCPEndpoint` (the DS component that registers an
`MCPEndpoint` from `server.name` + `server.url` config) lives in this bundle, so no MCP *server*
bundle is needed to address a remote MCP deployment.

After adding or replacing a jar here, regenerate `index.xml` — the `Local` repository is a
`LocalIndexedRepo` and will not see files it has not indexed.
