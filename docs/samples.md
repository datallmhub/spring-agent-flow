# Samples

The `agentflow4j-samples` module ships ready-to-run examples. All of them work without an API key unless explicitly noted.

## Running

```bash
mvn -pl agentflow4j-samples exec:java
```

This runs the default sample (`MultiAgentCoordination`). To run another, override `exec.mainClass`:

```bash
mvn -pl agentflow4j-samples exec:java \
    -Dexec.mainClass="io.github.datallmhub.agentflow4j.samples.MinimalPipeline"
```

## Included samples

| Example | What it shows | Needs API key? |
|---|---|---|
| `MultiAgentCoordination` | Multi-agent routing with `CoordinatorAgent` | no |
| `MinimalPipeline` | Simple 2-step workflow using `AgentGraph` | no |
| `AdvancedGraphDemo` | Loops, conditions, state, listeners | no |
| `ResearchSquad` | Coordinator + 3 specialists, demonstrating §8 of the spec | no |
| `MistralIntegrationDemo` | End-to-end with a real Mistral LLM | yes (`MISTRAL_API_KEY`) |
| `playground.PlaygroundDemo` | Spring Boot web app exposing `@Bean Agent`s through the playground UI | no (offline agents) / yes (real LLM) |

## Playground

```bash
mvn -pl agentflow4j-samples -am exec:java \
    -Dexec.mainClass=io.github.datallmhub.agentflow4j.samples.playground.PlaygroundDemo
```

Open <http://localhost:8080/playground> — pick an agent in the dropdown, chat, watch tokens stream in.
