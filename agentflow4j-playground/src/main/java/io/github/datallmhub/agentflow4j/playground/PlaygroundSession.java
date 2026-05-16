package io.github.datallmhub.agentflow4j.playground;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;

import io.github.datallmhub.agentflow4j.core.AgentContext;

/**
 * Per-HTTP-session conversation memory. Holds one {@link AgentContext}
 * per agent name so successive turns against the same agent accumulate
 * the message history.
 */
final class PlaygroundSession {

    static final String SESSION_KEY = "agentflow4j.playground.session";

    private final Map<String, AgentContext> conversations = new LinkedHashMap<>();

    AgentContext contextFor(String agent) {
        Objects.requireNonNull(agent, "agent");
        return conversations.computeIfAbsent(agent, k -> AgentContext.empty());
    }

    void update(String agent, AgentContext context) {
        Objects.requireNonNull(agent, "agent");
        Objects.requireNonNull(context, "context");
        conversations.put(agent, context);
    }

    void reset(String agent) {
        conversations.remove(agent);
    }

    void resetAll() {
        conversations.clear();
    }
}
