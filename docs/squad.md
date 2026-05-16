# AgentSquad & CoordinatorAgent

The Squad API is the fastest way to get started with multi-agent orchestration. It abstracts away the complexity of graphs while providing powerful dynamic routing.

---

## AgentSquad

`AgentSquad` is a high-level facade designed for 80% of use cases where you need a group of agents to collaborate on a single task.

### Simple Usage
```java
String result = AgentSquad.with(chatClient)
    .agent("Analyze the sentiment of this text")
    .agent("Translate the analysis to French")
    .ask("I love this project, it's so simple!");
```

### How it works
1. **Initialization**: You provide a `ChatClient`.
2. **Registration**: You add agents with simple natural language descriptions.
3. **Execution**: When you call `.ask()`, an internal `CoordinatorAgent` uses the LLM to decide which agent should handle the input first, and how to sequence the follow-up tasks.

---

## CoordinatorAgent

For advanced scenarios, you can use the `CoordinatorAgent` directly. It gives you control over the **Routing Strategy**.

### Custom Routing
By default, we use `RoutingStrategy.llmDriven(chatClient)`, but you can implement your own:

```java
RoutingStrategy myRouter = (context, availableExecutors) -> {
    // Custom logic to pick an agent
    return "specialized-agent";
};

CoordinatorAgent coordinator = CoordinatorAgent.builder()
    .executor("agent-1", executor1)
    .executor("agent-2", executor2)
    .routingStrategy(myRouter)
    .build();
```

### Key Features
- **Dynamic Selection**: The coordinator doesn't follow a fixed path; it evaluates the state after each step.
- **Parallel Execution**: While the current version is sequential, the architecture is designed to support parallel branching in future releases.
- **Resilience**: Every transition through the coordinator is a potential checkpoint.
