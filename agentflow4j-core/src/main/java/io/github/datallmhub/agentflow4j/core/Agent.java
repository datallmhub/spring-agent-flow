package io.github.datallmhub.agentflow4j.core;

import reactor.core.publisher.Flux;

public interface Agent {

    AgentResult execute(AgentContext context);

    default Flux<AgentEvent> executeStream(AgentContext context) {
        return Flux.just(AgentEvent.completed(execute(context)));
    }
}
