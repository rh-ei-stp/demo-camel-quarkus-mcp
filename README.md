# Demo of Camel + Quarkus MCP

An example for integrating existing Apache Camel routes with Quarkus MCP, making them available to external AI agent clients. 

## Purpose 

Development teams are being asked to incorporate AI into production systems, but may not know where to begin. This demo shows one way to make an existing Camel Quarkus route available to new AI agents, without requiring any changes to the route.

Future work will show how to do the same with Spring AI for existing Camel Spring Boot applications and a more complex example for a secured and limited access MCP endpoint.

## Walkthrough

Large Language Models (LLMs) are famously bad at counting the number of letters in a word, just like they are incapable of executing your custom business logic on their own.

For example, here we can see [granite4:1b](https://www.ibm.com/granite/docs/models/granite) running on [Ollama](https://ollama.com/) confidently hallucinate four letter 'e's in the word "splendiferous".

```
❯ ollama run granite4:1b
>>> How many 'e's are in the word splendiferous?
The word "splendiferous" contains 4 'e' letters.
```

Fortunately, we have an existing Apache Camel route that can accurately count letter 'e's in any word.

```java
@ApplicationScoped
public class CountEsRoute extends RouteBuilder {

    @Override
    public void configure() throws Exception {

        restConfiguration().inlineRoutes(false);

        rest("/camel/countEs")
            .post()
                .to("direct:countEs");

        from("direct:countEs")
            .routeId("countEs")
            .log("word=${body}")
            .process(exchange -> {
                String body = exchange.getIn().getBody(String.class);
                String result = Long.toString(List.of(body.split("")).stream()
                    .filter(s -> s.equalsIgnoreCase("e"))
                    .count());
                exchange.getIn().setBody(result);
            })
            .log("count=${body}");
    }
}
```

It has a [REST DSL](https://camel.apache.org/manual/rest-dsl.html) interface, so we can call it with `curl` as

```sh
curl -X POST localhost:8080/camel/countEs -H "Content-Type: text/plain"  -d "Splendiferous"
```

and get 
```sh
2
```
for the correct answer.

We will make the `countEs` route available as an [Quarkus MCP Server](https://docs.quarkiverse.io/quarkus-mcp-server/dev/index.html) endpoint. 

To reuse a REST DSL route's `direct` endpoint, we must have `restConfiguration().inlineRoutes(false)` (or set `camel.rest.inline-routes = true` in an `application.properties` file) so that it is not automatically swallowed into the `rest()` route and is accessable outside of it.

To add a Quarkus MCP Server to our application, we just need to include the extension `io.quarkiverse.mcp:quarkus-mcp-server-http` and use some of its annotations. Here we create a new class `CountEsTool` with a method `countEs(String word)`. 
We annotate the method with `@Tool` to make it an MCP tool with the provided name and description, and annotate its parameter with `@ToolArg`.
These will be reported to an AI agent when it requests a list of available tools from the MCP server. The agent will infer from prompts which tool should be used and with what arguements.

To pass the data into the `countEs` route, we simply `@Inject` a `ProducerTemplate` into the class and use `requestBody()` to send a message into the route. 
This method throws a `CamelExecutionException`, so we set the method return type to `ToolResponse` instead of just `String` to clearly indicate any error response to the agent. 

```java 
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
```


## Quick Demo

### Start `mcp-service`

```
cd mcp-service
mvn quarkus:dev
```

### Test `mcp-service`

#### REST DSL endpoint

```
❯ curl -X POST localhost:8080/camel/countEs -H "Content-Type: text/plain"  -d "Splendiferous"
2
```

#### MCP Endpoint

Visit the Dev UI page: http://localhost:8080/q/dev-ui/quarkus-mcp-server-http/tools

### Test `mcp-client`

```
cd mcp-client
mvn quarkus:dev
```

In a new terminal:
```
❯ curl localhost:8081/countEs/splendiferous
There are 2 letter 'e's in the word splendiferous.
```
