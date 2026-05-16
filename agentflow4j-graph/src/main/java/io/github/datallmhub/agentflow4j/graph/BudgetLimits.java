package io.github.datallmhub.agentflow4j.graph;

/**
 * Three-tier budget caps used by {@link HierarchicalBudgetPolicy}. Each limit
 * is currency-agnostic — units are whatever the paired {@link CostEstimator} /
 * {@link CostMeter} produce (dollars, tokens, call counts, ...).
 *
 * <p>{@link Double#POSITIVE_INFINITY} disables a given tier.
 *
 * @param perRun  total cost ceiling for one graph run
 * @param perNode cost ceiling for a single node across all attempts in one run
 * @param perCall cost ceiling for a single attempt
 */
public record BudgetLimits(double perRun, double perNode, double perCall) {

    public BudgetLimits {
        if (perRun < 0 || perNode < 0 || perCall < 0) {
            throw new IllegalArgumentException("limits must be >= 0");
        }
        if (Double.isNaN(perRun) || Double.isNaN(perNode) || Double.isNaN(perCall)) {
            throw new IllegalArgumentException("limits must not be NaN");
        }
    }

    /** Cap the run; leave per-node / per-call open. */
    public static BudgetLimits run(double perRun) {
        return new BudgetLimits(perRun, Double.POSITIVE_INFINITY, Double.POSITIVE_INFINITY);
    }

    /** Convenience builder. */
    public static Builder builder() {
        return new Builder();
    }

    public static final class Builder {
        private double perRun = Double.POSITIVE_INFINITY;
        private double perNode = Double.POSITIVE_INFINITY;
        private double perCall = Double.POSITIVE_INFINITY;

        public Builder perRun(double v) { this.perRun = v; return this; }
        public Builder perNode(double v) { this.perNode = v; return this; }
        public Builder perCall(double v) { this.perCall = v; return this; }

        public BudgetLimits build() {
            return new BudgetLimits(perRun, perNode, perCall);
        }
    }
}
