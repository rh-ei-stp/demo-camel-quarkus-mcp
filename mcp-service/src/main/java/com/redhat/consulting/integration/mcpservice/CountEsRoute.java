package com.redhat.consulting.integration.mcpservice;

import java.util.List;

import org.apache.camel.builder.RouteBuilder;

import jakarta.enterprise.context.ApplicationScoped;

@ApplicationScoped
public class CountEsRoute extends RouteBuilder {

    @Override
    public void configure() throws Exception {

        restConfiguration().inlineRoutes(false);

        rest("/camel/countEs")
            .post()
                .routeId("post")
                .to("direct:countEs");

        from("direct:countEs")
            .routeId("countEs")
            .log("word=${body}")
            .process(exchange -> {
                String body = exchange.getIn().getBody(String.class);
                String result = Long.toString(List.of(body.split("")).stream().filter(s -> s.equalsIgnoreCase("e")).count());
                exchange.getIn().setBody(result);
            })
            .log("count=${body}");
    }
}
