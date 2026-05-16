# AgentGraph & Transitions

When `AgentSquad` isn't enough, `AgentGraph` provides full architectural control over your agentic workflows.

---

## Defining a Graph

A graph consists of nodes (agents) and edges (transitions).

```java
AgentGraph graph = AgentGraph.builder()
    .addNode("fetch", dataFetcher)
    .addNode("process", dataProcessor)
    .addNode("notify", notifier)
    
    // Direct edges
    .addEdge("fetch", "process")
    
    // Conditional edges (Loops)
    .addEdge(Edge.conditional("process", 
        ctx -> !isComplete(ctx), 
        "fetch"))
        
    .addEdge("process", "notify")
    .build();
```

## Transitions

Transitions are the "glue" that moves the context from one agent to another.

### Direct Edges
The most common transition. Once Node A finishes successfully, Node B starts.

### Conditional Edges
Allows for decision branching and loops.
- **Example**: If an LLM's output confidence is too low, loop back to the same node with a different prompt.
- **Example**: If a certain data flag is present, skip to a "Summary" node.

---

## State Management in Graphs
Every node in the graph has access to the **shared state**. 
If Node A saves an `ORDER_ID`, Node B can retrieve it automatically. 

```java
// Node A
return AgentResult.builder()
    .text("Order found")
    .state("order_id", "12345")
    .build();

// Node B
String id = ctx.get("order_id");
```

---

## Error Handling at the Graph Level
You can define an `ErrorPolicy` for the entire graph:
- `RETRY_ONCE`: If any node fails, retry it exactly once before failing the whole graph.
- `FAIL_FAST`: Stop execution immediately on error (default).
