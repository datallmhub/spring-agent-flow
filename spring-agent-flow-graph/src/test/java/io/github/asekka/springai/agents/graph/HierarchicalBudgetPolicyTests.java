package io.github.asekka.springai.agents.graph;

import io.github.asekka.springai.agents.core.AgentContext;
import io.github.asekka.springai.agents.core.AgentResult;
import io.github.asekka.springai.agents.core.AgentUsage;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class HierarchicalBudgetPolicyTests {

    private static final AgentContext CTX = AgentContext.of("go");

    @Test
    void allowsCallWhenAllTiersOpen() {
        BudgetPolicy policy = BudgetPolicy.hierarchical(
                BudgetLimits.builder().perRun(10).perNode(10).perCall(10).build(),
                (node, ctx) -> 1.0,
                CostMeter.perCall());

        assertThat(policy.check("n", CTX).allowed()).isTrue();
    }

    @Test
    void deniesAtCallTierWhenSingleCallTooExpensive() {
        BudgetPolicy policy = BudgetPolicy.hierarchical(
                BudgetLimits.builder().perRun(100).perNode(100).perCall(2).build(),
                (node, ctx) -> 5.0,
                CostMeter.perCall());

        BudgetPolicy.Decision decision = policy.check("n", CTX);

        assertThat(decision.denied()).isTrue();
        assertThat(decision.breach().scope()).isEqualTo(BudgetPolicy.Scope.CALL);
        assertThat(decision.breach().limit()).isEqualTo(2.0);
        assertThat(decision.breach().projected()).isEqualTo(5.0);
    }

    @Test
    void deniesAtNodeTierAfterAccumulatedSpending() {
        BudgetPolicy policy = BudgetPolicy.hierarchical(
                BudgetLimits.builder().perRun(100).perNode(2).perCall(5).build(),
                (node, ctx) -> 1.5,
                CostMeter.perCall());

        assertThat(policy.check("n", CTX).allowed()).isTrue();
        policy.record("n", AgentResult.ofText("first"));

        BudgetPolicy.Decision decision = policy.check("n", CTX);
        assertThat(decision.denied()).isTrue();
        assertThat(decision.breach().scope()).isEqualTo(BudgetPolicy.Scope.NODE);
        assertThat(decision.breach().limit()).isEqualTo(2.0);
        assertThat(decision.breach().projected()).isEqualTo(2.5);
    }

    @Test
    void deniesAtRunTierWhenAggregateAcrossNodesExceeds() {
        BudgetPolicy policy = BudgetPolicy.hierarchical(
                BudgetLimits.builder().perRun(3).perNode(10).perCall(10).build(),
                (node, ctx) -> 1.0,
                CostMeter.perCall());

        policy.record("a", AgentResult.ofText("x"));
        policy.record("b", AgentResult.ofText("x"));
        policy.record("a", AgentResult.ofText("x"));

        BudgetPolicy.Decision decision = policy.check("c", CTX);
        assertThat(decision.denied()).isTrue();
        assertThat(decision.breach().scope()).isEqualTo(BudgetPolicy.Scope.RUN);
        assertThat(decision.breach().limit()).isEqualTo(3.0);
        assertThat(decision.breach().projected()).isEqualTo(4.0);
    }

    @Test
    void singleExpensiveCallAgainstSmallBudgetFailsFastAtCallTier() {
        BudgetPolicy policy = BudgetPolicy.hierarchical(
                BudgetLimits.builder().perRun(2).perNode(2).perCall(2).build(),
                (node, ctx) -> 5.0,
                CostMeter.perCall());

        BudgetPolicy.Decision decision = policy.check("n", CTX);
        assertThat(decision.denied()).isTrue();
        assertThat(decision.breach().scope()).isEqualTo(BudgetPolicy.Scope.CALL);
    }

    @Test
    void nodeSpendingIsIsolatedFromOtherNodes() {
        BudgetPolicy policy = BudgetPolicy.hierarchical(
                BudgetLimits.builder().perRun(100).perNode(2).perCall(5).build(),
                (node, ctx) -> 1.0,
                CostMeter.perCall());

        policy.record("a", AgentResult.ofText("x"));
        policy.record("a", AgentResult.ofText("x"));

        assertThat(policy.check("a", CTX).denied()).isTrue();
        assertThat(policy.check("b", CTX).allowed()).isTrue();
        assertThat(policy.spent(BudgetPolicy.Scope.NODE, "a")).isEqualTo(2.0);
        assertThat(policy.spent(BudgetPolicy.Scope.NODE, "b")).isEqualTo(0.0);
        assertThat(policy.spent(BudgetPolicy.Scope.RUN, "a")).isEqualTo(2.0);
    }

    @Test
    void tokenMeterScalesTokensIntoDollars() {
        CostMeter dollarsPerThousand = CostMeter.totalTokens().scaledBy(0.002 / 1000.0);
        AgentResult result = AgentResult.builder()
                .text("hi")
                .usage(AgentUsage.of(500, 500))
                .completed(true)
                .build();
        assertThat(dollarsPerThousand.measure("n", result)).isEqualTo(0.002);
    }

    @Test
    void noopPolicyAlwaysAllows() {
        assertThat(BudgetPolicy.NOOP.check("n", CTX).allowed()).isTrue();
        BudgetPolicy.NOOP.record("n", AgentResult.ofText("x"));
        assertThat(BudgetPolicy.NOOP.spent(BudgetPolicy.Scope.RUN, "n")).isZero();
    }

    @Test
    void negativeLimitsAreRejected() {
        try {
            BudgetLimits.builder().perRun(-1).build();
            assertThat(false).as("expected IllegalArgumentException").isTrue();
        }
        catch (IllegalArgumentException expected) {
            // OK
        }
    }
}
