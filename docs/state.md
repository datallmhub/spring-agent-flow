# State Management & Checkpoints

One of the biggest challenges in multi-agent systems is maintaining state over long or complex conversations. `agentflow4j` treats state as a first-class citizen.

---

## Shared State

The `AgentContext` contains a `State` object which is a typed map. 

### Best Practices: StateKeys
To avoid "stringly-typed" code, use `StateKey`:

```java
public static final StateKey<Long> USER_ID = StateKey.of("user_id", Long.class);

// Writing
context.set(USER_ID, 42L);

// Reading
Long id = context.get(USER_ID);
```

---

## Checkpoints

A **Checkpoint** is a snapshot of the `AgentContext` (messages + state) saved at a specific point in time.

### Why use Checkpoints?
1. **Persistence**: Save progress to a database (Redis, JDBC, etc.).
2. **Interrupt & Resume**: If an agent requires human intervention, you can save the checkpoint, wait for the user, and resume exactly where you left off.
3. **Time Travel**: Debugging is easier when you can inspect the exact state of the context between any two agent steps.

### Implementation
Checkpoints are handled by the `CheckpointRepository`. You can provide your own implementation to persist state anywhere.

```java
AgentGraph graph = AgentGraph.builder()
    .checkpointRepository(new JdbcCheckpointRepository(dataSource))
    .build();
```

---

## Serialization
By default, we use JSON serialization for the state. Ensure that any custom objects you store in the context are Jackson-compatible.
