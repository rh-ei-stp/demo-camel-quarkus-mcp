package com.redhat.consulting.integration.camel.quarkus.mcp.client;

import org.apache.camel.component.langchain4j.agent.api.Agent;
import org.apache.camel.component.langchain4j.agent.api.AgentConfiguration;
import org.apache.camel.component.langchain4j.agent.api.AgentWithoutMemory;
import dev.langchain4j.mcp.client.McpClient;
import dev.langchain4j.model.chat.ChatModel;
import io.quarkiverse.langchain4j.mcp.runtime.McpClientName;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.inject.Named;

@ApplicationScoped
public class Configuration {

    @Inject ChatModel chatModel;

    @McpClientName("countes")
    McpClient countEsClient;

    @Named("letterCounterAgent")
    Agent letterCounterAgent() {
        // Create agent configuration
        AgentConfiguration configuration = new AgentConfiguration()
                .withChatModel(chatModel)
                .withMcpClient(countEsClient);

        // Create the agent
        Agent agent = new AgentWithoutMemory(configuration);
        return agent;
    }
}
