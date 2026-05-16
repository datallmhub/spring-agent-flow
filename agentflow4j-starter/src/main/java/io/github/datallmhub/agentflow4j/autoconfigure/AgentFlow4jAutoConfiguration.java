package io.github.datallmhub.agentflow4j.autoconfigure;

import io.github.datallmhub.agentflow4j.graph.AgentListener;
import io.github.datallmhub.agentflow4j.squad.RoutingStrategy;
import io.micrometer.core.instrument.MeterRegistry;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;

@AutoConfiguration
@ConditionalOnProperty(prefix = "agentflow4j", name = "enabled", havingValue = "true",
        matchIfMissing = true)
@EnableConfigurationProperties(AgentFlow4jProperties.class)
public class AgentFlow4jAutoConfiguration {

    @Bean
    @ConditionalOnMissingBean
    RoutingStrategy defaultRoutingStrategy(AgentFlow4jProperties properties,
                                           org.springframework.beans.factory.ObjectProvider<ChatClient> chatClientProvider) {
        AgentFlow4jProperties.Squad.DefaultRoutingStrategy choice =
                properties.getSquad().getDefaultRoutingStrategy();
        if (choice == AgentFlow4jProperties.Squad.DefaultRoutingStrategy.LLM_DRIVEN) {
            ChatClient client = chatClientProvider.getIfAvailable();
            if (client != null) {
                return RoutingStrategy.llmDriven(client);
            }
        }
        return RoutingStrategy.first();
    }

    @Bean
    @ConditionalOnClass(MeterRegistry.class)
    @ConditionalOnBean(MeterRegistry.class)
    @ConditionalOnMissingBean
    @ConditionalOnProperty(prefix = "agentflow4j.observability", name = "metrics",
            havingValue = "true", matchIfMissing = true)
    AgentListener micrometerAgentListener(MeterRegistry registry) {
        return new MicrometerAgentListener(registry);
    }
}
