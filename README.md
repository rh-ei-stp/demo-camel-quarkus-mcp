# Demo of Camel + Quarkus MCP

An example for integrating existing Apache Camel routes with Quarkus MCP, making them available to external AI agent clients. 

## Purpose 

Development teams are being asked to incorporate AI into production systems, but may not know where to begin. This demo shows one way to make an existing Camel Quarkus route available to new AI agents, without requiring any changes to the route.

Future work will show how to do the same with Spring AI for existing Camel Spring Boot applications and a more complex example for a secured and limited access MCP endpoint.

## Walkthrough

Large Language Models (LLMs) on their own are incapable of executing your complex business logic or interacting with external applications, databases, messages brokers, and so on. 
They can't even count the number of specific letters in a word.

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
This method throws a `CamelExecutionException`, so we set the method return type to `ToolResponse` instead of just `String` to clearly indicate any error response to the agent with `ToolResponse.error()` and the happy path response with `ToolResponse.success()`.

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

That's all we need to create the MCP server! We can manually test it by running the Quarkus service in Dev mode
```
mvn -f mcp-service/pom.xml quarkus:dev
```
and opening the MCP Server Tools page in the Dev UI console at http://localhost:8080/q/dev-ui/quarkus-mcp-server-http/tools. 
We can see an entry for the `countEs` tool.

![screenshot of Dev UI with Tool call button](readme/devui_call.png)

When we call it with `splendiferous`, we get the correct response.

![screenshot of Dev UI with success response](readme/devui_success.png)


When we provide the wrong argument or type, we get an error response from the MCP Server. Note `isError: true`.

![screenshot of Dev UI with arg error](readme/devui_argerror.png)

And if we modify the route to throw an error, we see it pass up just the error message for a response with `isError: true`.
```java
from("direct:countEs")
    .routeId("countEs")
    .log("word=${body}")
    .throwException(new CamelExecutionException("UNEXPECTED ERROR", null) {})
    .process(exchange -> {
    ...
```

![screenshot of Dev UI with camel error](readme/devui_camelerror.png)

We can also unit test it with the extension `io.quarkiverse.mcp:quarkus-mcp-server-test` which provides the [McpAssured](https://docs.quarkiverse.io/quarkus-mcp-server/dev/guides-testing.html) library. See the link for many more detailed examples. Here we have a test for the happy path and if the wrong type of argument is provided.

```java
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
```

Now that we have our Camel route wrapped with an MCP Server, let's try it out with a real AI agent.




## Quick Demo Steps

### Start `mcp-service`

```
cd mcp-service
mvn quarkus:dev
```

### Test `mcp-service`

#### REST DSL endpoint

And in another terminal
```
❯ curl -X POST localhost:8080/camel/countEs -H "Content-Type: text/plain"  -d "Splendiferous"
2
```

#### MCP Endpoint

Visit the Dev UI page: http://localhost:8080/q/dev-ui/quarkus-mcp-server-http/tools

### Test `mcp-client`

In a new terminal session,
```
cd mcp-client
mvn quarkus:dev
```

And in yet another new terminal:
```
❯ curl localhost:8081/countEs/splendiferous
There are 2 letter 'e's in the word splendiferous.
```
