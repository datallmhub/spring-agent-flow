# Core Concepts

`agentflow4j` is built on a few simple but powerful primitives. Understanding these will help you build complex multi-agent systems with ease.

---

## 1. Agent
An **Agent** is the fundamental unit of work. In this framework, an agent is just a function that takes an `AgentContext` and returns an `AgentResult`.

- **ExecutorAgent**: A specialized agent that uses an LLM (via Spring AI's `ChatClient`) to perform tasks.
- **CoordinatorAgent**: A "manager" agent that routes tasks to other agents.

## 2. AgentContext
The **Context** is the shared memory of your workflow. It carries:
- **Messages**: The conversation history.
- **State**: A typed, key-value store for structured data (e.g., `ORDER_ID`, `USER_PROFILE`).
- **Metadata**: Internal execution data (metrics, trace IDs).

## 3. AgentResult
Every agent execution produces a **Result**. It can contain:
- **Text**: The primary response.
- **Entity**: A structured object (POJO) if the agent was configured for structured output.
- **State Updates**: Changes to be applied to the global context.

## 4. The Graph
The **Graph** defines how agents interact. It consists of:
- **Nodes**: Your agents.
- **Edges**: The paths between agents (can be direct or conditional).
- **Checkpoints**: Moments where the state is persisted, allowing for long-running workflows that can survive restarts.

---

## Why this architecture?
Unlike simple chains, a **Graph** allows for loops, retries, and dynamic routing. By separating the **State** from the **Logic**, we ensure that your agents remain stateless and scalable, while your workflows remain resilient and traceable.
