package io.github.datallmhub.agentflow4j.graph;

import io.github.datallmhub.agentflow4j.core.AgentResult;
import io.github.datallmhub.agentflow4j.core.AgentUsage;

/**
 * Post-call cost meter. Returns the cost actually incurred by an
 * {@link AgentResult}, in the same unit as the matching
 * {@link CostEstimator}. The graph invokes this after each successful node
 * execution and feeds the result to {@link BudgetPolicy#record}.
 */
@FunctionalInterface
public interface CostMeter {

    double measure(String nodeName, AgentResult result);

    /** Charges 1.0 per call regardless of result content. */
    static CostMeter perCall() {
        return (node, result) -> 1.0;
    }

    /**
     * Charges total tokens reported in {@link AgentUsage} when present,
     * 0 otherwise. Use {@code .scaledBy(...)} to convert into dollars.
     */
    static CostMeter totalTokens() {
        return (node, result) -> {
            AgentUsage usage = result.usage();
            return usage == null ? 0.0 : (double) usage.totalTokens();
        };
    }

    /** Returns a meter that multiplies this meter's output by {@code factor}. */
    default CostMeter scaledBy(double factor) {
        return (node, result) -> measure(node, result) * factor;
    }
}
