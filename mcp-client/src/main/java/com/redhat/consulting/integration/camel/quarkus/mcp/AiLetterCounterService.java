package com.redhat.consulting.integration.camel.quarkus.mcp;

import dev.langchain4j.service.SystemMessage;
import io.quarkiverse.langchain4j.RegisterAiService;
import io.quarkiverse.langchain4j.mcp.runtime.McpToolBox;

@RegisterAiService
public interface AiLetterCounterService {
    @SystemMessage("""
        Count the number of letter 'e's in the provided word.
        Limit your response just the number. 
    """)
    @McpToolBox
    public String countEs(String word);
}
