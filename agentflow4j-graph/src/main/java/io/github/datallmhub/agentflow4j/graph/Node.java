package io.github.datallmhub.agentflow4j.graph;

import io.github.datallmhub.agentflow4j.core.Agent;
import io.github.datallmhub.agentflow4j.core.AgentContext;
import io.github.datallmhub.agentflow4j.core.AgentEvent;
import io.github.datallmhub.agentflow4j.core.AgentResult;
import org.jspecify.annotations.Nullable;
import reactor.core.publisher.Flux;

public interface Node {

    String name();

    AgentResult execute(AgentContext context);

    default Flux<AgentEvent> executeStream(AgentContext context) {
        return Flux.just(AgentEvent.completed(execute(context)));
    }

    @Nullable
    default RetryPolicy retryPolicy() {
        return null;
    }

    @Nullable
    default CircuitBreakerPolicy circuitBreaker() {
        return null;
    }

    static Node of(String name, Agent agent) {
        return new AgentNode(name, agent, null, null);
    }

    static Node of(String name, Agent agent, RetryPolicy retryPolicy) {
        return new AgentNode(name, agent, retryPolicy, null);
    }

    static Node of(String name, Agent agent, RetryPolicy retryPolicy, CircuitBreakerPolicy circuitBreaker) {
        return new AgentNode(name, agent, retryPolicy, circuitBreaker);
    }

    record AgentNode(
            String name,
            Agent agent,
            @Nullable RetryPolicy retryPolicy,
            @Nullable CircuitBreakerPolicy circuitBreaker) implements Node {
        @Override
        public AgentResult execute(AgentContext context) {
            return agent.execute(context);
        }

        @Override
        public Flux<AgentEvent> executeStream(AgentContext context) {
            return agent.executeStream(context);
        }
    }
}
