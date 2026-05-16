package io.github.datallmhub.agentflow4j.graph;

import java.util.function.Supplier;

/**
 * Per-node guard that short-circuits calls when an upstream dependency is
 * unhealthy. The graph invokes {@link #execute(String, Supplier)} once per
 * retry attempt, so retry loops iterate through the breaker. Implementations
 * that deem the circuit open must throw so the attempt is recorded as an
 * {@link io.github.datallmhub.agentflow4j.core.AgentError}.
 */
public interface CircuitBreakerPolicy {

    <T> T execute(String nodeName, Supplier<T> call);

    CircuitBreakerPolicy NOOP = new CircuitBreakerPolicy() {
        @Override
        public <T> T execute(String nodeName, Supplier<T> call) {
            return call.get();
        }
    };
}
