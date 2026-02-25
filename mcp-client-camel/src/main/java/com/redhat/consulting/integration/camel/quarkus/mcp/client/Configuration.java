package com.redhat.consulting.integration.camel.quarkus.mcp.client;

import java.time.Duration;

import org.apache.camel.component.langchain4j.agent.api.Agent;
import org.apache.camel.component.langchain4j.agent.api.AgentConfiguration;
import org.apache.camel.component.langchain4j.agent.api.AgentWithoutMemory;
import org.eclipse.microprofile.config.ConfigProvider;

import dev.langchain4j.mcp.client.DefaultMcpClient;
import dev.langchain4j.mcp.client.McpClient;
import dev.langchain4j.mcp.client.transport.McpTransport;
import dev.langchain4j.mcp.client.transport.http.HttpMcpTransport;
import dev.langchain4j.mcp.client.transport.http.StreamableHttpMcpTransport;
import dev.langchain4j.model.chat.ChatModel;
import dev.langchain4j.model.ollama.OllamaChatModel;
import io.smallrye.common.annotation.Identifier;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.inject.Named;
import jakarta.inject.Qualifier;

@ApplicationScoped
public class Configuration {

    @Inject ChatModel chatModel;

    // @Inject @Identifier("countes")
    // McpClient mcpClient;

    @Named("letterCounterAgent")
    Agent letterCounterAgent() {
        String ollamaEndpoint = ConfigProvider.getConfig()
                .getValue("langchain4j-ollama-dev-service.ollama.endpoint",
                String.class);
        // String ollamaEndpoint = "http://localhost:11434";

        // TODO: externalize this configuration
        // ChatModel ollamaModel = OllamaChatModel.builder()
        //         .baseUrl(ollamaEndpoint)
        //         .temperature(0.1)
        //         .logRequests(false)
        //         .logResponses(false)
        //         .modelName("granite4:1b")
        //         // .modelName("qwen3:1.7b")
        //         .build();

        McpTransport countEsHttpTransport = new StreamableHttpMcpTransport.Builder()
                .url("http://localhost:8080/mcp")
                .logRequests(true)
                .logRequests(true)
                .build();

        McpClient countEsClient = new DefaultMcpClient.Builder()
                .clientName("count_es")
                .transport(countEsHttpTransport)
                .autoHealthCheck(false)
                .autoHealthCheckInterval(Duration.ofMinutes(5L))
                .build();

        // Create agent configuration
        AgentConfiguration configuration = new AgentConfiguration()
                .withChatModel(chatModel)
                .withMcpClient(countEsClient);

        // Create the agent
        Agent agent = new AgentWithoutMemory(configuration);
        return agent;
    }
}
