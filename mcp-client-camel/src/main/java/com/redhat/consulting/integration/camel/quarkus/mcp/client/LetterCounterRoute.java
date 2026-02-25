package com.redhat.consulting.integration.camel.quarkus.mcp.client;

import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.component.langchain4j.agent.api.Headers;

public class LetterCounterRoute extends RouteBuilder{

    @Override
    public void configure() throws Exception {
        rest("/countEs")
            .get("/{word}")
            .to("direct:agent");

        from("direct:agent")
            .log("word=${header.word}")
            .setHeader(Headers.SYSTEM_MESSAGE).simple("""
                Count the number of letter 'e's in the provided word.
                Limit your response just the number. 
                """)
            .setBody().header("word")
            .to("langchain4j-agent:letterCounterAgent")
            .log("count=${body}")
            ;
    }
    
}
