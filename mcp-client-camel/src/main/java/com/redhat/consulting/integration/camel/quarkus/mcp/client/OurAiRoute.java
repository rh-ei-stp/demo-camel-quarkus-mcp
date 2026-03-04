package com.redhat.consulting.integration.camel.quarkus.mcp.client;

import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.component.langchain4j.agent.api.Headers;

import jakarta.ws.rs.core.MediaType;

public class OurAiRoute extends RouteBuilder{

    @Override
    public void configure() throws Exception {
        rest("/camel/ai")
            .post()
                .consumes(MediaType.TEXT_PLAIN)
                .produces(MediaType.TEXT_PLAIN)
            .to("direct:agent");

        from("direct:agent")
            .log("prompt=${body}")
            .setHeader(Headers.SYSTEM_MESSAGE).simple("""
                You are a helpful agent. 
                Please provide consise answers to user questions.
                """)
            .convertBodyTo(String.class)
            .to("langchain4j-agent:letterCounterAgent")
            .log("response=${body}")
            ;
    }
    
}
