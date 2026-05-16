package io.github.datallmhub.agentflow4j.playground;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * Configuration for the agentflow4j web playground.
 */
@ConfigurationProperties(prefix = "agentflow4j.playground")
public class PlaygroundProperties {

    /** Whether the playground UI and SSE endpoint are exposed. Defaults to true. */
    private boolean enabled = true;

    /** Page title shown in the browser tab and header. */
    private String title = "agentflow4j playground";

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }
}
