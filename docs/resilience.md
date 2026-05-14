# Resilience & Error Handling

LLM-based workflows are inherently non-deterministic. Network failures, rate limits, and "hallucinations" are part of the game. `spring-agent-flow` provides built-in tools to handle these gracefully.

---

## 1. Automatic Retries

You can configure retries at the agent level or the graph level.

```java
ExecutorAgent agent = ExecutorAgent.builder()
    .retryPolicy(RetryPolicy.exponentialBackoff(3)) // Retry 3 times with backoff
    .build();
```

## 2. Structured Error Results

If an agent fails, it returns an `AgentResult` with error metadata. The framework doesn't just throw an exception; it allows you to inspect the failure.

```java
AgentResult result = agent.execute(context);
if (result.hasError()) {
    log.error("Agent failed: {}", result.error().message());
}
```

## 3. Circuit Breaker Support

When using `spring-agent-flow-starter`, we integrate with Spring AI's native retry and circuit breaker advisors. This ensures that if an LLM provider is down, your application remains stable and doesn't waste tokens on guaranteed failures.

## 4. Loop Prevention

Graphs can accidentally create infinite loops. We provide a **max-steps** guardrail to ensure that a workflow eventually terminates.

```java
AgentGraph graph = AgentGraph.builder()
    .maxSteps(10) // Fails if more than 10 transitions occur
    .build();
```

---

## 5. Human-in-the-loop

Sometimes, "resilience" means stopping and asking a human. By using **Checkpoints**, you can halt a workflow on a specific condition, notify a user via email/Slack, and resume once the human has provided the missing data.

---

## 6. Budget Policy (cost gate)

`RetryPolicy` counts attempts and elapsed time, but it is blind to **cost**. A single agent retrying a paid API overnight can quietly burn dollars. `BudgetPolicy` is a pluggable SPI that caps a run by cost — in whatever currency you choose (dollars, tokens, call counts).

### Three hierarchical tiers

A `BudgetLimits` has three caps, enforced in this order on every call:

| Scope  | Meaning                                                      |
| ------ | ------------------------------------------------------------ |
| `CALL` | Max cost of a **single** attempt — a $5 call against a $2 budget fails fast. |
| `NODE` | Max cumulative cost of one **node** over the entire run.     |
| `RUN`  | Max cumulative cost across **all nodes** in the run.         |

Any tier set to `Double.POSITIVE_INFINITY` is disabled. `BudgetLimits.run(double)` is shorthand for "only cap the run".

### Wiring a per-provider cost estimator

You supply two callbacks:

- **`CostEstimator`** — called **before** every attempt with `(nodeName, context)`. Return the worst-case cost of the upcoming call.
- **`CostMeter`** — called **after** a successful attempt with `(nodeName, result)`. Return the actual cost incurred, often derived from `AgentResult.usage()`.

Example: a dollar-denominated budget for an OpenAI-style provider charging $0.002 / 1K total tokens.

```java
double dollarsPerToken = 0.002 / 1000.0;

CostEstimator estimator = (node, ctx) -> {
    int promptTokens = ctx.messages().stream()
            .mapToInt(m -> m.getText().length() / 4)    // rough heuristic
            .sum();
    int worstCaseCompletion = 1024;
    return (promptTokens + worstCaseCompletion) * dollarsPerToken;
};

CostMeter meter = CostMeter.totalTokens().scaledBy(dollarsPerToken);

BudgetPolicy budget = BudgetPolicy.hierarchical(
        BudgetLimits.builder()
                .perRun(2.00)    // $2.00 hard cap per run
                .perNode(0.50)   // no single node may burn more than $0.50
                .perCall(0.10)   // refuse any single call > $0.10
                .build(),
        estimator,
        meter);

AgentGraph graph = AgentGraph.builder()
        .addNode("research", researchAgent)
        .addNode("write", writerAgent)
        .addEdge("research", "write")
        .budgetPolicy(budget)
        .build();
```

### How a breach surfaces

When `check` denies a call, the graph short-circuits the node and returns an `AgentResult` whose `interrupt()` is set:

```java
AgentResult result = graph.invoke(ctx);
if (result.isInterrupted() && result.interrupt().reason().startsWith("budget.exceeded:")) {
    BudgetPolicy.Breach breach = (BudgetPolicy.Breach) result.interrupt().payload();
    log.warn("Halted at scope={} (limit={}, projected={})",
            breach.scope(), breach.limit(), breach.projected());
    // Resume from a Checkpoint after a human approves more budget...
}
```

Because the breach uses the existing `InterruptRequest` mechanism it plugs into the human-in-the-loop / checkpoint flow described above — pause, notify, raise the limit, resume.

### Drop-in cost units

Need something simpler than dollars?

- **Call count**: `CostEstimator.perCall()` + `CostMeter.perCall()` — limits become "max 20 calls per run".
- **Tokens**: `CostMeter.totalTokens()` — read directly from `AgentResult.usage()`; limits are expressed in tokens.

### Gotchas

- The default `BudgetPolicy` is `NOOP`. You only get cost protection after calling `.budgetPolicy(...)` on the builder.
- The policy gates **before every attempt, including retries** — a flaky node will not silently chew through your run budget.
- Counters live on the `BudgetPolicy` instance. Use a fresh instance per run (or per tenant) if you do not want spending to carry over.
