package io.github.datallmhub.agentflow4j.graph;

import java.util.Objects;

import io.github.datallmhub.agentflow4j.core.AgentContext;
import io.github.datallmhub.agentflow4j.core.InterruptRequest;
import org.jspecify.annotations.Nullable;

public record Checkpoint(
        String runId,
        String nextNode,
        AgentContext context,
        int iterations,
        @Nullable InterruptRequest interrupt) {

    public Checkpoint {
        Objects.requireNonNull(runId, "runId");
        Objects.requireNonNull(nextNode, "nextNode");
        Objects.requireNonNull(context, "context");
        if (iterations < 0) {
            throw new IllegalArgumentException("iterations must be >= 0");
        }
    }

    public boolean isInterrupted() {
        return interrupt != null;
    }
}
