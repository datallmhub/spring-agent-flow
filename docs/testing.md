# Testing without an LLM

The `agentflow4j-test` module ships two helpers that let you exercise graphs entirely offline: `MockAgent` and `TestGraph`.

## MockAgent — scripted responses

```java
MockAgent mock = MockAgent.builder()
        .thenReturn("First response")
        .thenReturn("Second response")
        .build();
```

`MockAgent` returns the queued responses in order. Use `MockAgent.returning("done")` for a single-shot mock.

## TestGraph — trace a graph end-to-end

```java
TestGraph.Trace trace = TestGraph.trace(
        AgentGraph.builder()
            .addNode("a", mock)
            .addNode("b", MockAgent.returning("done"))
            .addEdge("a", "b"));

AgentResult result = trace.invoke(AgentContext.of("test"));

assertThat(trace.visitedInOrder("a", "b")).isTrue();
assertThat(result.text()).isEqualTo("done");
```

`Trace` records visits, transitions and errors so you can assert on the execution path, not just the final result. Useful for regressing routing logic, conditional edges, and retry behaviour.
