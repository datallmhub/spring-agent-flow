# AgentFlow4J

**Build stateful multi-agent workflows in Java — with graphs, retries, and persistence.**

No orchestration code. No glue logic. Just define your agents and run.

[![build](https://github.com/datallmhub/agentflow4j/actions/workflows/build.yml/badge.svg)](https://github.com/datallmhub/agentflow4j/actions)
[![Java 17+](https://img.shields.io/badge/Java-17%2B-blue)](https://adoptium.net/)
[![Spring AI](https://img.shields.io/badge/Spring%20AI-1.0-green)](https://docs.spring.io/spring-ai/reference/)
[![License](https://img.shields.io/badge/License-Apache%202.0-blue.svg)](LICENSE)

---

## 🚀 Live demo of a real-world use case built with AgentFlow4J

🔴 Sample of multi-agent **USE CASE** built with **AgentFlow4J**:

<img width="800" height="400" alt="chrome-capture-2026-05-04" src="https://github.com/user-attachments/assets/8825501e-f1bf-4c27-b734-be348cd83e12" />

👉 https://huggingface.co/spaces/datallmhub/multi-agent-customer-ops

### What it shows

- Multi-agent orchestration (Triage → Lookup → Policy → Writer)
- Hybrid AI + deterministic business logic
- Typed shared state across agents
- End-to-end decision traceability

### Run locally

You can run the demo locally using the full source code:

👉 https://github.com/datallmhub/multi-agent-customer-ops

---

## ⚡ In 60 seconds

```java
ExecutorAgent researcher = ExecutorAgent.builder()
        .chatClient(chatClient)
        .systemPrompt("Find key facts.")
        .build();

ExecutorAgent writer = ExecutorAgent.builder()
        .chatClient(chatClient)
        .systemPrompt("Write a clear report.")
        .build();

CoordinatorAgent coordinator = CoordinatorAgent.builder()
        .executors(Map.of("research", researcher, "writing", writer))
        .routingStrategy(RoutingStrategy.llmDriven(chatClient))
        .build();

AgentResult result = coordinator.execute(
        AgentContext.of("Compare Claude 4 and GPT-5"));

System.out.println(result.text());
```

**Output:**

```
=== Multi-Agent Coordination ===

Request: Compare Claude 4 and GPT-5

[router]   Routing to: research
[research] Gathering facts...

[router]   Routing to: writing
[writing]  Generating report...

Result:
Claude 4 excels in reasoning and long-context tasks.
GPT-5 shows stronger tool integration and instruction following.
```

> **This is a multi-step, stateful workflow with routing, coordination, and resilience — without writing orchestration code.**

⭐ **If this saves you time, consider [starring the repo](https://github.com/datallmhub/agentflow4j).**

---

## 🧠 Why this exists

Real-world AI systems are not one LLM call.

They are:

- **multi-step**
- **stateful**
- **failure-prone**
- **long-running**

Spring AI gives you primitives.
**AgentFlow4J gives you a runtime.**

---

## 📊 How it works

![How it works](docs/images/demo.png)

> A coordinator routes tasks across agents, executing a graph with shared state, retries, and checkpoints.

---

## 🧠 Two levels of control

### Level 1 — Squad API (recommended)

Dynamic routing, minimal setup. A `CoordinatorAgent` routes to `ExecutorAgent`s — you focus on the agents, not the plumbing.

```java
CoordinatorAgent coordinator = CoordinatorAgent.builder()
        .executors(Map.of(
            "research", researchExecutor,
            "analysis", analysisExecutor,
            "writing",  writingExecutor
        ))
        .routingStrategy(RoutingStrategy.llmDriven(chatClient))
        .build();

AgentResult result = coordinator.execute(AgentContext.of("..."));
```

### Level 2 — Graph API

Explicit flows, loops, conditions, full control.

```java
AgentGraph graph = AgentGraph.builder()
        .addNode("research", researcher)
        .addNode("analyze",  analyzer)
        .addNode("write",    writer)
        .addEdge("research", "analyze")
        .addEdge(Edge.conditional("analyze",
                ctx -> ctx.get(CONFIDENCE).doubleValue() < 0.7,
                "research"))                               // loop back
        .addEdge("analyze", "write")                       // fallback: forward
        .errorPolicy(ErrorPolicy.RETRY_ONCE)
        .build();

AgentResult result = graph.invoke(AgentContext.of("..."));
```

---

## 🧭 When should I use this?

**Use it if:**

- your agent needs multiple LLM calls
- your workflow has branches or loops
- failures (retry, resume, rate limits) matter
- multiple agents must coordinate

**Avoid it if:**

- you just call `ChatClient` once

---

## ⚔️ Why not just Spring AI or simple loops?

| Approach | Limitation |
|---|---|
| Spring AI alone | Low-level primitives only — you write the orchestration |
| Manual `while` loops | Don't scale, retries are hard, state becomes fragile |
| LangChain-style flows | Limited execution control, Python-first |

**AgentFlow4J provides:**

- explicit execution graphs
- built-in resilience (retry + circuit breaker)
- durable, typed state

| Spring AI | AgentFlow4J |
|---|---|
| Primitives (`ChatClient`, tools) | Structured runtime (`AgentGraph`, `CoordinatorAgent`) |
| Manual orchestration | Graph-based execution |
| No durable state | Typed shared state + checkpoints |
| Retry logic in user code | Built-in retry + circuit breaker |
| No resume | Interrupt + resume support |

---

## 🚀 Try it in 30 seconds (no API key required)

```bash
git clone https://github.com/datallmhub/agentflow4j.git
cd agentflow4j
mvn install -DskipTests -q
mvn -pl agentflow4j-samples exec:java
```

👉 Runs a real multi-agent workflow with routing, coordination, and state — fully simulated.

## 📦 Samples included

The project ships with ready-to-run examples — no LLM required.

| Example | What it shows | Run |
|--------|--------------|-----|
| `MultiAgentCoordination` | Multi-agent routing with CoordinatorAgent | default |
| `MinimalPipeline` | Simple 2-step workflow using AgentGraph | `-Dexec.mainClass="...MinimalPipeline"` |
| `AdvancedGraphDemo` | Loops, conditions, state, listeners | `-Dexec.mainClass="...AdvancedGraphDemo"` |

👉 Start with `MultiAgentCoordination` — it demonstrates the full power of the framework.

---

## 🧩 What you get

- ⚡ No orchestration code required
- 🧠 Stateful agent workflows
- 🔁 Built-in retries & circuit breakers
- 📊 Graph-based execution
- 💾 Durable checkpoints (JDBC / Redis)
- 🔌 Native Spring AI integration
- 📡 Streaming support
- 📈 Micrometer metrics

---

## 🧱 Architecture

![Modules Architecture](docs/images/modules-architecture.png)

> Layered architecture showing coordination, execution, resilience, and persistence on top of Spring AI.

---

## 🛠 Installation

**Requirements:** Java 17+, Spring Boot 3.x, Spring AI 1.0+

Distributed via [JitPack](https://jitpack.io).

### Maven

```xml
<repositories>
    <repository>
        <id>jitpack.io</id>
        <url>https://jitpack.io</url>
    </repository>
</repositories>

<dependency>
    <groupId>com.github.datallmhub.agentflow4j</groupId>
    <artifactId>agentflow4j-starter</artifactId>
    <version>v0.5.0</version>
</dependency>
```

### Gradle

```groovy
repositories {
    maven { url 'https://jitpack.io' }
}

dependencies {
    implementation 'com.github.datallmhub.agentflow4j:agentflow4j-starter:v0.5.0'
}
```

### Modules

| Module | Use case |
|---|---|
| `agentflow4j-starter` | Spring Boot auto-config, properties, Micrometer listener |
| `agentflow4j-core` | Minimal API (`Agent`, `AgentContext`, `StateKey`, `AgentResult`) |
| `agentflow4j-graph` | `AgentGraph`, `RetryPolicy`, `CircuitBreakerPolicy` SPI, checkpoint contract |
| `agentflow4j-squad` | `CoordinatorAgent`, `ExecutorAgent`, `ReActAgent`, `ParallelAgent`, `RoutingStrategy` |
| `agentflow4j-checkpoint` | `JdbcCheckpointStore`, `RedisCheckpointStore`, Jackson codec |
| `agentflow4j-resilience4j` | `CircuitBreakerPolicy` adapter backed by Resilience4j |
| `agentflow4j-cli-agents` | `CliAgentNode` — runs Claude Code / Codex / Gemini CLI agents as graph nodes |
| `agentflow4j-test` | `MockAgent`, `TestGraph` for unit-testing graphs |

Minimal `application.yml`:

```yaml
spring:
  ai:
    agents:
      enabled: true
      default-error-policy: RETRY_ONCE
      observability:
        metrics: true
```

---

## 📡 Streaming

```java
graph.invokeStream(AgentContext.of("hello"))
    .subscribe(event -> {
        switch (event) {
            case AgentEvent.Token t         -> System.out.print(t.chunk());
            case AgentEvent.NodeTransition x -> System.out.println("\n--> " + x.to());
            case AgentEvent.Completed c     -> System.out.println("\n[done]");
            default -> {}
        }
    });
```

---

## 💾 Typed state — no `Map<String, Object>`

```java
// Declare keys with types — compile-time safety
StateKey<Double> CONFIDENCE = StateKey.of("confidence", Double.class);
StateKey<String> SUMMARY    = StateKey.of("summary",    String.class);

// Use them anywhere
AgentContext ctx = context.with(CONFIDENCE, 0.85);
double score = ctx.get(CONFIDENCE);  // no cast needed
```

---

## 🔁 Resilience

```java
AgentGraph.builder()
    .errorPolicy(ErrorPolicy.FAIL_FAST)          // or RETRY_ONCE / SKIP_NODE
    .retryPolicy(RetryPolicy.exponential(3, Duration.ofMillis(200)))
    .addNode("llm", flakyAgent,
             RetryPolicy.exponential(5, Duration.ofMillis(500)),   // per-node override
             new Resilience4jCircuitBreakerPolicy(registry))        // per-node breaker
    .build();
```

See [resilient-typed-executor.md](docs/recipes/resilient-typed-executor.md) and [circuit-breaker.md](docs/recipes/circuit-breaker.md).

---

## 📈 Observability (Micrometer)

| Metric | Tags | Description |
|--------|------|-------------|
| `agents.execution.count` | `agent`, `graph`, `status` | Per-node execution count |
| `agents.execution.duration` | `agent`, `graph` | Per-node execution time |
| `agents.graph.transitions` | `graph`, `from`, `to` | Node-to-node transitions |
| `agents.execution.errors` | `agent`, `graph`, `cause` | Error count by type |

---

## 🧪 Testing without an LLM

```java
MockAgent mock = MockAgent.builder()
        .thenReturn("First response")
        .thenReturn("Second response")
        .build();

TestGraph.Trace trace = TestGraph.trace(
        AgentGraph.builder()
            .addNode("a", mock)
            .addNode("b", MockAgent.returning("done"))
            .addEdge("a", "b"));

AgentResult result = trace.invoke(AgentContext.of("test"));

assertThat(trace.visitedInOrder("a", "b")).isTrue();
assertThat(result.text()).isEqualTo("done");
```

---

## 📚 Recipes

- [ReAct loop](docs/recipes/react-loop.md) — self-correcting agent with observation/action cycles
- [Supervisor pattern](docs/recipes/supervisor-pattern.md) — coordinator re-routes until done
- [Parallel executors](docs/recipes/parallel-executors.md) — fan-out/fan-in
- [Subgraphs](docs/recipes/subgraphs.md) — plug a graph in as a node
- [Human-in-the-loop](docs/recipes/human-in-the-loop.md) — interrupt, wait for human input, resume
- [Durable runs](docs/recipes/durable-runs.md) — JDBC or Redis checkpoint store, resume after crash
- [Resilient typed executor](docs/recipes/resilient-typed-executor.md) — tool audit + typed output + retry
- [Circuit breaker](docs/recipes/circuit-breaker.md) — trip upstream calls with Resilience4j

---

## 📈 Roadmap

| Version | Focus |
|---------|-------|
| **0.5** (current) | Subgraphs, parallel fan-out, cancellation, typed output, `RetryPolicy`, `CircuitBreakerPolicy`, JDBC/Redis checkpoint store |
| **1.0** | API stabilization, documentation, community feedback |
| **1.1** | Crew roles (CrewAI-inspired), auto-config for checkpoint backends |
| **2.0** | OpenTelemetry tracing, MCP integration, Agent-as-Tool |

---

## 📝 Note on scope

This project is independent and not affiliated with [`spring-ai-community/agent-client`](https://github.com/spring-ai-community/agent-client).

That project focuses on CLI agent integrations (Claude Code, Codex, Gemini).

**AgentFlow4J** focuses on something different:
a graph-based runtime for stateful, multi-step agent workflows on top of Spring AI.

---

## 🤝 Contributing

Contributions welcome! Please see [CONTRIBUTING.md](CONTRIBUTING.md) for guidelines.

This project follows the [Apache 2.0 License](LICENSE).

---

## Inspiration

- [LangGraph](https://github.com/langchain-ai/langgraph) — graph-based orchestration
- [CrewAI](https://github.com/joaomdmoura/crewai) — role-based agent teams
- [AWS Strands](https://github.com/strands-agents/sdk-java) — agent patterns for Java
- [Spring AI](https://github.com/spring-projects/spring-ai) — the foundation we build on
