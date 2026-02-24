package com.redhat.consulting.integration.camel.quarkus.mcp;

import io.quarkiverse.mcp.server.TextContent;
import io.quarkiverse.mcp.server.test.McpAssured;
import io.quarkiverse.mcp.server.test.McpAssured.McpSseTestClient;
import io.quarkus.test.junit.QuarkusTest;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Map;

import org.junit.jupiter.api.Test;

@QuarkusTest
class CountEsMcpToolTest {

    @Test
    void happyPath() {
        McpSseTestClient client = McpAssured.newConnectedSseClient();
        client.when()
            .toolsCall("countEs",
                Map.of("word", "splendiferous"),
                response -> {
                    assertFalse(response.isError());
                    TextContent text = response.content().getFirst().asText();
                    assertTrue(text.text().contains("2"));
            })
            .thenAssertResults();
        client.disconnect();
    }

    @Test
    void wrongArgument() {
        McpSseTestClient client = McpAssured.newConnectedSseClient();
        client.when()
            .toolsCall("countEs",
                Map.of("word", 1),
                response -> {
                    assertTrue(response.isError());
            })
            .thenAssertResults();
        client.disconnect();
    }
}