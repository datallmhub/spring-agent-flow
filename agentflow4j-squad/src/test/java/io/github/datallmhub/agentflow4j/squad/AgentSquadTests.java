package io.github.datallmhub.agentflow4j.squad;

import org.junit.jupiter.api.Test;
import org.mockito.Answers;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.messages.AssistantMessage;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.ai.chat.model.Generation;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class AgentSquadTests {

    @Test
    void fluentAskWorkflow() {
        ChatClient chatClient = mock(ChatClient.class, Answers.RETURNS_DEEP_STUBS);
        
        when(chatClient.prompt().system(anyString())
                .user(anyString())
                .call()
                .content())
                .thenReturn("researcher");
        
        ChatResponse chatResponse = ChatResponse.builder()
                .generations(List.of(new Generation(new AssistantMessage("quantum qubits are 99.9% stable"))))
                .build();
        
        when(chatClient.prompt().system(anyString())
                .messages(anyList())
                .call()
                .chatResponse())
                .thenReturn(chatResponse);

        String response = AgentSquad.with(chatClient)
                .agent("researcher", "Find quantum facts")
                .agent("writer", "Write a summary")
                .ask("Research quantum computing");
        
        assertThat(response).isEqualTo("quantum qubits are 99.9% stable");
    }
}
