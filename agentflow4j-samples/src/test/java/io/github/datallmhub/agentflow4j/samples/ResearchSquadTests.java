package io.github.datallmhub.agentflow4j.samples;

import java.util.Map;

import io.github.datallmhub.agentflow4j.core.AgentContext;
import io.github.datallmhub.agentflow4j.core.AgentResult;
import io.github.datallmhub.agentflow4j.core.StateKey;
import io.github.datallmhub.agentflow4j.graph.AgentGraph;
import io.github.datallmhub.agentflow4j.test.MockAgent;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class ResearchSquadTests {

    private static final StateKey<String> REPORT = StateKey.of("report", String.class);

    @Test
    void fourStageLinearPipelineProducesFinalReport() {
        MockAgent coordinator = MockAgent.returning("plan: three-section report");
        MockAgent research = MockAgent.returning("findings: A, B, C");
        MockAgent analyze = MockAgent.returning("analysis: X>Y");
        MockAgent write = MockAgent.builder()
                .thenAnswer(ctx -> AgentResult.builder()
                        .text("final report")
                        .stateUpdates(Map.of(REPORT, "final report"))
                        .build())
                .build();

        AgentGraph graph = ResearchSquad.build(coordinator, research, analyze, write);
        AgentResult result = graph.invoke(
                AgentContext.of("Compare Claude 4 and GPT-5"));

        assertThat(result.text()).isEqualTo("final report");
        assertThat(result.stateUpdates().get(REPORT)).isEqualTo("final report");
        assertThat(coordinator.invocations()).isEqualTo(1);
        assertThat(research.invocations()).isEqualTo(1);
        assertThat(analyze.invocations()).isEqualTo(1);
        assertThat(write.invocations()).isEqualTo(1);
    }

    @Test
    void retryOncePolicyRescuesTransientResearchFailure() {
        MockAgent coordinator = MockAgent.returning("plan");
        MockAgent research = MockAgent.builder()
                .thenThrow(new RuntimeException("transient network blip"))
                .thenReturn("findings on retry")
                .build();
        MockAgent analyze = MockAgent.returning("analysis");
        MockAgent write = MockAgent.returning("report");

        AgentGraph graph = ResearchSquad.build(coordinator, research, analyze, write);
        AgentResult result = graph.invoke(AgentContext.of("Research topic X"));

        assertThat(result.text()).isEqualTo("report");
        assertThat(research.invocations()).isEqualTo(2);
    }
}
