# AgentFlow4J

**Build stateful multi-agent workflows in Java — with graphs, retries, and persistence.**

No orchestration code. No glue logic. Just define your agents and run.

[![build](https://github.com/datallmhub/agentflow4j/actions/workflows/build.yml/badge.svg)](https://github.com/datallmhub/agentflow4j/actions)
[![Java 17+](https://img.shields.io/badge/Java-17%2B-blue)](https://adoptium.net/)
[![Spring AI](https://img.shields.io/badge/Spring%20AI-1.0-green)](https://docs.spring.io/spring-ai/reference/)
[![License](https://img.shields.io/badge/License-Apache%202.0-blue.svg)](LICENSE)

---

## 🚀 Live demo

🔴 Multi-agent **B2B use case** built with **AgentFlow4J**:

<img width="800" height="400" alt="Live demo" src="https://github.com/user-attachments/assets/8825501e-f1bf-4c27-b734-be348cd83e12" />

👉 https://huggingface.co/spaces/datallmhub/multi-agent-customer-ops &nbsp;·&nbsp; Source: [multi-agent-customer-ops](https://github.com/datallmhub/multi-agent-customer-ops)

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
```

A multi-step, stateful workflow with routing, coordination, and resilience — without writing orchestration code.

⭐ **If this saves you time, consider [starring the repo](https://github.com/datallmhub/agentflow4j).**

---

## 🧠 Why AgentFlow4J?

Real-world AI systems are **multi-step**, **stateful**, **failure-prone**, and **long-running**.

Spring AI gives you primitives. **AgentFlow4J gives you a runtime.**

| Spring AI | AgentFlow4J |
|---|---|
| Primitives (`ChatClient`, tools) | Structured runtime (`AgentGraph`, `CoordinatorAgent`) |
| Manual orchestration | Graph-based execution |
| No durable state | Typed shared state + checkpoints |
| Retry logic in user code | Built-in retry + circuit breaker |
| No resume | Interrupt + resume support |

**Use it if** your agent needs multiple LLM calls, your workflow has branches or loops, failures matter, or multiple agents must coordinate.
**Skip it if** you just call `ChatClient` once.

---

## 🧩 Two levels of control

- **Squad API** — dynamic routing, minimal setup. A `CoordinatorAgent` dispatches to `ExecutorAgent`s.
- **Graph API** — explicit flows, loops, conditions, full control.

Both are covered in the [docs](#-documentation).

---

## 🚀 Try it in 30 seconds (no API key)

```bash
git clone https://github.com/datallmhub/agentflow4j.git
cd agentflow4j
mvn install -DskipTests -q
mvn -pl agentflow4j-samples exec:java
```

Runs `MultiAgentCoordination` — a fully simulated multi-agent workflow with routing, coordination and state.

---

## 🛠 Installation

**Requirements:** Java 17+, Spring Boot 3.x, Spring AI 1.0+.
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
repositories { maven { url 'https://jitpack.io' } }
dependencies { implementation 'com.github.datallmhub.agentflow4j:agentflow4j-starter:v0.5.0' }
```

### Modules

| Module | Purpose |
|---|---|
| `agentflow4j-starter` | Spring Boot auto-config, properties, Micrometer listener |
| `agentflow4j-core` | Minimal API (`Agent`, `AgentContext`, `StateKey`, `AgentResult`) |
| `agentflow4j-graph` | `AgentGraph`, `RetryPolicy`, `CircuitBreakerPolicy`, `BudgetPolicy`, checkpoint contract |
| `agentflow4j-squad` | `CoordinatorAgent`, `ExecutorAgent`, `ReActAgent`, `ParallelAgent` |
| `agentflow4j-checkpoint` | `JdbcCheckpointStore`, `RedisCheckpointStore`, Jackson codec |
| `agentflow4j-resilience4j` | `CircuitBreakerPolicy` adapter backed by Resilience4j |
| `agentflow4j-playground` | Drop-in web UI to chat with your `Agent` beans |
| `agentflow4j-cli-agents` | `CliAgentNode` — Claude Code / Codex / Gemini CLI as graph nodes |
| `agentflow4j-test` | `MockAgent`, `TestGraph` for LLM-free unit tests |

---

## 📚 Documentation

- [Two API levels (Squad + Graph)](docs/two-api-levels.md) — when to use which, with code
- [Typed state](docs/state.md) — `StateKey<T>` instead of `Map<String, Object>`
- [Resilience & error handling](docs/resilience.md) — retries, circuit breaker, budget policy
- [Observability](docs/observability.md) — Micrometer metrics, tags, listeners
- [Streaming](docs/streaming.md) — `Flux<AgentEvent>` tokens, transitions, tool calls
- [Testing without an LLM](docs/testing.md) — `MockAgent` + `TestGraph`
- [Samples](docs/samples.md) — runnable examples shipped with the repo

---

## 📈 Roadmap

| Version | Focus |
|---------|-------|
| **0.5** (current) | Subgraphs, parallel fan-out, cancellation, typed output, retry/circuit-breaker/budget policies, JDBC/Redis checkpoint store, web playground |
| **1.0** | API stabilization, documentation, community feedback |
| **1.1** | Crew roles (CrewAI-inspired), auto-config for checkpoint backends |
| **2.0** | OpenTelemetry tracing, MCP integration, Agent-as-Tool |

---

## 📝 Note on scope

It is not an official Spring project.

---

## 🤝 Contributing & License

Contributions welcome — see [CONTRIBUTING.md](CONTRIBUTING.md).
Released under the [Apache 2.0 License](LICENSE).

---

## Inspiration

- [LangGraph](https://github.com/langchain-ai/langgraph) — graph-based orchestration
- [CrewAI](https://github.com/joaomdmoura/crewai) — role-based agent teams
- [AWS Strands](https://github.com/strands-agents/sdk-java) — agent patterns for Java
- [Spring AI](https://github.com/spring-projects/spring-ai) — the foundation we build on
