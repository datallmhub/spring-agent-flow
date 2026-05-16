package io.github.datallmhub.agentflow4j.graph;

import java.util.concurrent.atomic.AtomicInteger;

import io.github.datallmhub.agentflow4j.core.Agent;
import io.github.datallmhub.agentflow4j.core.AgentContext;
import io.github.datallmhub.agentflow4j.core.AgentResult;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class AgentGraphBudgetTests {

    @Test
    void runBudgetStopsGraphAcrossNodes() {
        AtomicInteger aCalls = new AtomicInteger();
        AtomicInteger bCalls = new AtomicInteger();
        Agent a = ctx -> { aCalls.incrementAndGet(); return AgentResult.ofText("a"); };
        Agent b = ctx -> { bCalls.incrementAndGet(); return AgentResult.ofText("b"); };

        BudgetPolicy budget = BudgetPolicy.hierarchical(
                BudgetLimits.builder().perRun(1).build(),
                (node, ctx) -> 1.0,
                CostMeter.perCall());

        AgentGraph graph = AgentGraph.builder()
                .addNode("a", a)
                .addNode("b", b)
                .addEdge("a", "b")
                .budgetPolicy(budget)
                .build();

        AgentResult result = graph.invoke(AgentContext.of("go"));

        assertThat(aCalls.get()).isEqualTo(1);
        assertThat(bCalls.get()).isZero();
        assertThat(result.isInterrupted()).isTrue();
        assertThat(result.interrupt().reason()).isEqualTo("budget.exceeded:RUN");
        assertThat(result.interrupt().payload())
                .isInstanceOfSatisfying(BudgetPolicy.Breach.class, breach -> {
                    assertThat(breach.scope()).isEqualTo(BudgetPolicy.Scope.RUN);
                    assertThat(breach.nodeName()).isEqualTo("b");
                });
    }

    @Test
    void nodeBudgetStopsSecondAttemptOfSameNode() {
        AtomicInteger calls = new AtomicInteger();
        Agent loop = ctx -> { calls.incrementAndGet(); return AgentResult.ofText("ok"); };

        BudgetPolicy budget = BudgetPolicy.hierarchical(
                BudgetLimits.builder().perRun(100).perNode(1).build(),
                (node, ctx) -> 1.0,
                CostMeter.perCall());

        AgentGraph graph = AgentGraph.builder()
                .addNode("loop", loop)
                .addEdge(Edge.conditional("loop", ctx -> calls.get() < 5, "loop"))
                .budgetPolicy(budget)
                .build();

        AgentResult result = graph.invoke(AgentContext.of("go"));

        assertThat(calls.get()).isEqualTo(1);
        assertThat(result.isInterrupted()).isTrue();
        assertThat(result.interrupt().reason()).isEqualTo("budget.exceeded:NODE");
        BudgetPolicy.Breach breach = (BudgetPolicy.Breach) result.interrupt().payload();
        assertThat(breach.scope()).isEqualTo(BudgetPolicy.Scope.NODE);
        assertThat(breach.nodeName()).isEqualTo("loop");
    }

    @Test
    void callBudgetFailsFastBeforeNodeIsEverInvoked() {
        AtomicInteger calls = new AtomicInteger();
        Agent expensive = ctx -> { calls.incrementAndGet(); return AgentResult.ofText("nope"); };

        BudgetPolicy budget = BudgetPolicy.hierarchical(
                BudgetLimits.builder().perRun(2).perCall(2).build(),
                (node, ctx) -> 5.0,
                CostMeter.perCall());

        AgentGraph graph = AgentGraph.builder()
                .addNode("e", expensive)
                .budgetPolicy(budget)
                .build();

        AgentResult result = graph.invoke(AgentContext.of("go"));

        assertThat(calls.get()).isZero();
        assertThat(result.isInterrupted()).isTrue();
        assertThat(result.interrupt().reason()).isEqualTo("budget.exceeded:CALL");
    }

    @Test
    void successfulRunRecordsCostAgainstRunAndNodeTiers() {
        Agent a = ctx -> AgentResult.ofText("ok");
        BudgetPolicy budget = BudgetPolicy.hierarchical(
                BudgetLimits.builder().perRun(10).perNode(10).build(),
                CostEstimator.perCall(),
                CostMeter.perCall());

        AgentGraph graph = AgentGraph.builder()
                .addNode("a", a)
                .budgetPolicy(budget)
                .build();

        AgentResult result = graph.invoke(AgentContext.of("go"));

        assertThat(result.completed()).isTrue();
        assertThat(budget.spent(BudgetPolicy.Scope.RUN, "a")).isEqualTo(1.0);
        assertThat(budget.spent(BudgetPolicy.Scope.NODE, "a")).isEqualTo(1.0);
    }
}
