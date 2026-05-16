# Streaming

Every `Agent` exposes `executeStream(...)` returning a `Flux<AgentEvent>`. `AgentGraph.invokeStream(...)` does the same for whole graphs.

## Subscribing

```java
graph.invokeStream(AgentContext.of("hello"))
    .subscribe(event -> {
        switch (event) {
            case AgentEvent.Token t          -> System.out.print(t.chunk());
            case AgentEvent.NodeTransition x -> System.out.println("\n--> " + x.to());
            case AgentEvent.ToolCallStart s  -> System.out.println("[tool] " + s.toolName());
            case AgentEvent.ToolCallEnd e    -> System.out.println("[done] " + e.record().name());
            case AgentEvent.Completed c      -> System.out.println("\n[graph done]");
        }
    });
```

## Event types

`AgentEvent` is a sealed interface with five cases:

| Event | When |
|---|---|
| `Token(chunk)` | Each token (or chunk) streamed by the underlying LLM |
| `ToolCallStart(name, args)` | A tool/function is about to execute |
| `ToolCallEnd(record)` | A tool/function returned a result |
| `NodeTransition(from, to)` | The graph moves from one node to the next |
| `Completed(result)` | Terminal event — carries the final `AgentResult` |

## Use cases

- Server-Sent Events to a browser (see `agentflow4j-playground` for a working example).
- Live logging in CLI agents.
- Cancellation: dispose the `Disposable` returned by `subscribe` to stop the run.
