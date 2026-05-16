# Observability

When `agentflow4j-starter` is on the classpath and a `MeterRegistry` bean is present, a `MicrometerAgentListener` is auto-registered. It emits four metrics per graph run.

## Metrics

| Metric | Tags | Description |
|--------|------|-------------|
| `agents.execution.count` | `agent`, `graph`, `status` | Per-node execution count |
| `agents.execution.duration` | `agent`, `graph` | Per-node execution time |
| `agents.graph.transitions` | `graph`, `from`, `to` | Node-to-node transitions |
| `agents.execution.errors` | `agent`, `graph`, `cause` | Error count by type |

## Toggling

```yaml
agentflow4j:
  observability:
    metrics: true      # default; disable to skip the Micrometer listener
    events:  true      # default; controls AgentEvent emission from invokeStream
```

## Custom listeners

`AgentListener` is the underlying SPI. Implement it and register the bean to plug your own observability — logs, traces, audit log, anything.

```java
@Bean
AgentListener auditListener() {
    return new AgentListener() {
        @Override
        public void onNodeExit(String graph, String node, AgentResult r, long ns) {
            audit.record(graph, node, r, Duration.ofNanos(ns));
        }
    };
}
```

Multiple listeners can be registered side-by-side; they are all invoked.
