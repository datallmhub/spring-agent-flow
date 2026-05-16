package io.github.datallmhub.agentflow4j.graph;

import io.github.datallmhub.agentflow4j.core.AgentContext;

/**
 * Pre-call cost estimator. Returns a currency-agnostic positive number
 * (dollars, tokens, call counts, etc.) representing the worst-case cost
 * of executing {@code nodeName} against {@code context}.
 *
 * <p>The graph invokes this <em>before every attempt</em> (including
 * retries) and feeds the value to {@link BudgetPolicy#check}.
 */
@FunctionalInterface
public interface CostEstimator {

    double estimate(String nodeName, AgentContext context);

    /** Treats every call as costing 1.0 — handy for call-count budgets. */
    static CostEstimator perCall() {
        return (node, ctx) -> 1.0;
    }

    /** Always reports zero. Useful when the caller only relies on {@link CostMeter}. */
    static CostEstimator zero() {
        return (node, ctx) -> 0.0;
    }
}
