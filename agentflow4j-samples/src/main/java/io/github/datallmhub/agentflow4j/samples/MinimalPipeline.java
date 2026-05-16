package io.github.datallmhub.agentflow4j.samples;

import io.github.datallmhub.agentflow4j.core.Agent;
import io.github.datallmhub.agentflow4j.core.AgentContext;
import io.github.datallmhub.agentflow4j.core.AgentResult;
import io.github.datallmhub.agentflow4j.graph.AgentGraph;

/**
 * 01 — Minimal example.
 *
 * <p>A two-step pipeline: summarize → critique. No LLM required.
 * Run this class directly to see the output.
 *
 * <p>Expected output:
 * <pre>
 * === Minimal Pipeline ===
 * [summarize] Processing...
 * [critique]  Processing...
 *
 * Final output:
 *   Rating: 8/10 — The summary captures the core value proposition.
 *   Suggestion: mention the typed state (StateKey) as a differentiator.
 * </pre>
 */
public final class MinimalPipeline {

    public static void main(String[] args) {
        System.out.println("=== Minimal Pipeline ===");

        // Simple agents that transform text — no LLM needed
        Agent summarizer = ctx -> {
            System.out.println("[summarize] Processing...");
            String input = ctx.messages().get(0).getText();
            return AgentResult.ofText(
                    "Summary: agentflow4j is a multi-agent coordination "
                  + "framework for Spring Boot that eliminates manual orchestration logic.");
        };

        Agent critic = ctx -> {
            System.out.println("[critique]  Processing...");
            return AgentResult.ofText(
                    "Rating: 8/10 — The summary captures the core value proposition.\n"
                  + "  Suggestion: mention the typed state (StateKey) as a differentiator.");
        };

        // Build a two-step graph
        AgentGraph graph = AgentGraph.builder()
                .addNode("summarize", summarizer)
                .addNode("critique", critic)
                .addEdge("summarize", "critique")
                .build();

        // Run
        AgentResult result = graph.invoke(
                AgentContext.of("What is agentflow4j?"));

        System.out.println();
        System.out.println("Final output:");
        System.out.println("  " + result.text());
    }
}
