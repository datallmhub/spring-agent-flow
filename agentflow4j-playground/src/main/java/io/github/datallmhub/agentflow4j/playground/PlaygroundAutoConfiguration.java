package io.github.datallmhub.agentflow4j.playground;

import java.util.Map;

import io.github.datallmhub.agentflow4j.core.Agent;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.web.servlet.DispatcherServlet;

@AutoConfiguration
@ConditionalOnWebApplication(type = ConditionalOnWebApplication.Type.SERVLET)
@ConditionalOnClass(DispatcherServlet.class)
@ConditionalOnProperty(prefix = "agentflow4j.playground", name = "enabled",
        havingValue = "true", matchIfMissing = true)
@EnableConfigurationProperties(PlaygroundProperties.class)
public class PlaygroundAutoConfiguration {

    @Bean
    PlaygroundController playgroundController(Map<String, Agent> agents, PlaygroundProperties properties) {
        return new PlaygroundController(agents, properties);
    }
}
