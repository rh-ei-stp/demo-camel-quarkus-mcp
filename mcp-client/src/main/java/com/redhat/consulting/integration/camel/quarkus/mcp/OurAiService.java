package com.redhat.consulting.integration.camel.quarkus.mcp;

import dev.langchain4j.service.SystemMessage;
import dev.langchain4j.service.UserMessage;
import io.quarkiverse.langchain4j.RegisterAiService;
import io.quarkiverse.langchain4j.mcp.runtime.McpToolBox;

@RegisterAiService
public interface OurAiService {
    @SystemMessage("""
        You are helpful agent. Please provide conside answers to user questions.
    """)
    @McpToolBox
    public String ask(@UserMessage String prompt);
}
