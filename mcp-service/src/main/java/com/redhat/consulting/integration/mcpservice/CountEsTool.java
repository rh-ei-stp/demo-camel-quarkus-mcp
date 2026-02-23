package com.redhat.consulting.integration.mcpservice;

import org.apache.camel.CamelContext;
import org.apache.camel.CamelExecutionException;
import org.apache.camel.ProducerTemplate;

import io.quarkiverse.mcp.server.Tool;
import io.quarkiverse.mcp.server.ToolArg;
import io.quarkiverse.mcp.server.ToolResponse;
import io.quarkus.logging.Log;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.inject.Named;

public class CountEsTool {

    @Inject
    ProducerTemplate producer;

    @Tool(name = "countEs", description = "Count the number of the letter 'e' in a word.")
    ToolResponse countEs(@ToolArg(description = "word") String word) {
        Log.info("MCP request to count 'e's in "+word);
        try {
            String response = producer.requestBody("direct:countEs", word, String.class);
            return ToolResponse.success(response);
        } catch (CamelExecutionException e) {
            Log.error(e);
            e.printStackTrace();
            return ToolResponse.error(e.getMessage());
        }
    }
}
