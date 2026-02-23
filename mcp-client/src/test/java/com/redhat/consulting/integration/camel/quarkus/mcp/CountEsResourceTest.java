package com.redhat.consulting.integration.camel.quarkus.mcp;

import io.quarkus.test.InjectMock;
import io.quarkus.test.junit.QuarkusTest;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import static io.restassured.RestAssured.given;
import static org.hamcrest.CoreMatchers.containsString;

@QuarkusTest
class CountEsResourceTest {

    @InjectMock
    AiLetterCounterService aiService;

    @BeforeAll
    public void setup() {
        Mockito
            .when(aiService.countEs("splendiferous"))
            .thenReturn("There are 2 letter 'e's in the word splendiferous.");
    }

    @Test
    void testCountEndpoint() {
        given()
          .when().get("/countEs/splendiferous")
          .then()
             .statusCode(200)
             .body(containsString("2"));
    }

}