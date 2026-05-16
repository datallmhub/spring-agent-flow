package io.github.datallmhub.agentflow4j.samples.playground;

import java.time.Duration;

import io.github.datallmhub.agentflow4j.core.Agent;
import io.github.datallmhub.agentflow4j.core.AgentContext;
import io.github.datallmhub.agentflow4j.core.AgentEvent;
import io.github.datallmhub.agentflow4j.core.AgentResult;
import io.github.datallmhub.agentflow4j.squad.ExecutorAgent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.messages.Message;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.context.annotation.Bean;
import reactor.core.publisher.Flux;

/**
 * Boots a Spring Boot web app that auto-wires the
 * {@code agentflow4j-playground}. Open
 * <a href="http://localhost:8080/playground">http://localhost:8080/playground</a>
 * and chat with the agents below.
 *
 * <p>Run offline:
 * <pre>
 * mvn -pl agentflow4j-samples -am spring-boot:run \
 *     -Dspring-boot.run.mainClass=io.github.datallmhub.agentflow4j.samples.PlaygroundDemo
 * </pre>
 *
 * <p>The {@code mistral} agent only registers when {@code MISTRAL_API_KEY}
 * is exported, so the demo works with zero credentials out of the box.
 */
@SpringBootApplication
public class PlaygroundDemo {

    private static final Logger log = LoggerFactory.getLogger(PlaygroundDemo.class);

    public static void main(String[] args) {
        SpringApplication.run(PlaygroundDemo.class, args);
        log.info("Playground ready: http://localhost:8080/playground");
    }

    /**
     * Pure offline agent — echoes the last user message back in uppercase.
     * Useful to validate the playground works without any credentials.
     */
    @Bean
    Agent echo() {
        return ctx -> AgentResult.ofText("ECHO: " + lastUserMessage(ctx).toUpperCase());
    }

    /**
     * Demonstrates token-by-token streaming. Splits a canned reply into words
     * and emits them at 80ms intervals so the UI shows the typewriter effect.
     */
    @Bean
    Agent streamer() {
        String reply = "Streaming tokens through Server-Sent Events feels much "
                + "snappier than waiting for the full response.";
        String[] words = reply.split(" ");

        return new Agent() {
            @Override
            public AgentResult execute(AgentContext context) {
                return AgentResult.ofText(reply);
            }

            @Override
            public Flux<AgentEvent> executeStream(AgentContext context) {
                Flux<AgentEvent> tokens = Flux.interval(Duration.ofMillis(80))
                        .take(words.length)
                        .map(i -> AgentEvent.token(words[i.intValue()] + " "));
                return tokens.concatWith(Flux.just(AgentEvent.completed(AgentResult.ofText(reply))));
            }
        };
    }

    /**
     * Real LLM-backed agent. Only registered when a {@link ChatModel} bean
     * exists in the context — Spring AI auto-configures one when
     * {@code MISTRAL_API_KEY} (or any other provider's key) is set.
     */
    @Bean
    @ConditionalOnBean(ChatModel.class)
    Agent mistral(ChatModel chatModel) {
        return ExecutorAgent.builder()
                .name("mistral")
                .chatClient(ChatClient.builder(chatModel).build())
                .systemPrompt("You are a concise, friendly assistant. Answer in at most 3 sentences.")
                .build();
    }

    private static String lastUserMessage(AgentContext ctx) {
        return ctx.messages().stream()
                .reduce((a, b) -> b)
                .map(Message::getText)
                .orElse("(no message)");
    }
}
