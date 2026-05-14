package io.github.asekka.springai.agents.graph;

import io.github.asekka.springai.agents.core.AgentContext;
import io.github.asekka.springai.agents.core.AgentResult;
import org.jspecify.annotations.Nullable;

/**
 * Pluggable cost gate that stops a graph run when a configured budget is
 * exceeded. Implementations track spending across three hierarchical scopes
 * ({@link Scope#RUN} &gt; {@link Scope#NODE} &gt; {@link Scope#CALL}) so that
 * a single node cannot drain the global run budget.
 *
 * <p>The graph invokes {@link #check} <em>before every attempt</em>, including
 * retries, passing the cost reported by a caller-supplied
 * {@link CostEstimator}. A denied decision surfaces as an
 * {@link io.github.asekka.springai.agents.core.InterruptRequest} so it plugs
 * into the existing human-in-the-loop machinery. On a successful attempt the
 * graph calls {@link #record} with the cost reported by a caller-supplied
 * {@link CostMeter}.
 *
 * <p>This SPI is intentionally currency-agnostic; the caller decides whether
 * "cost" is dollars, tokens, or call counts.
 */
public interface BudgetPolicy {

    /**
     * Returns {@link Decision#allow()} when the estimated call cost fits
     * within all three tiers, otherwise a {@link Decision} carrying the
     * tier that would have been breached.
     */
    Decision check(String nodeName, AgentContext context);

    /**
     * Accumulates the actual cost of a completed call against the running
     * counters used by future {@link #check} calls. Idempotent if called
     * with zero cost.
     */
    void record(String nodeName, AgentResult result);

    /**
     * Returns the total cost recorded against {@code scope} so far, in the
     * meter's unit. {@code nodeName} is only consulted for
     * {@link Scope#NODE}. Returns 0 if nothing has been recorded.
     */
    double spent(Scope scope, String nodeName);

    /**
     * No-op policy. Every call is allowed and no counters are kept.
     * Useful as the default when no budget has been wired.
     */
    BudgetPolicy NOOP = new BudgetPolicy() {
        @Override
        public Decision check(String nodeName, AgentContext context) {
            return Decision.allow();
        }
        @Override
        public void record(String nodeName, AgentResult result) {
        }
        @Override
        public double spent(Scope scope, String nodeName) {
            return 0.0;
        }
    };

    /**
     * Creates a {@link HierarchicalBudgetPolicy} with the supplied limits and
     * cost functions.
     */
    static BudgetPolicy hierarchical(BudgetLimits limits,
                                     CostEstimator estimator,
                                     CostMeter meter) {
        return new HierarchicalBudgetPolicy(limits, estimator, meter);
    }

    /** The tier at which a budget is enforced. */
    enum Scope { RUN, NODE, CALL }

    /** Outcome of a {@link #check} call. */
    record Decision(boolean allowed, @Nullable Breach breach) {

        public static Decision allow() {
            return new Decision(true, null);
        }

        public static Decision deny(Scope scope, String nodeName, double limit, double projected) {
            return new Decision(false, new Breach(scope, nodeName, limit, projected));
        }

        public boolean denied() {
            return !allowed;
        }
    }

    /**
     * Details of a budget breach. {@code projected} is the spend that
     * <em>would</em> result from accepting the call.
     */
    record Breach(Scope scope, String nodeName, double limit, double projected) {

        public String describe() {
            return String.format(
                    "Budget exceeded at scope=%s node='%s': limit=%.6f, projected=%.6f",
                    scope, nodeName, limit, projected);
        }
    }
}
