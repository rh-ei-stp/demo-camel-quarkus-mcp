package com.redhat.consulting.integration.camel.quarkus.mcp;

import io.quarkus.test.InjectMock;
import io.quarkus.test.junit.QuarkusTest;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.mockito.Mockito;

import static io.restassured.RestAssured.given;
import static org.hamcrest.CoreMatchers.containsString;

@QuarkusTest
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class AiResourceTest {

    @InjectMock
    OurAiService aiService;

    @BeforeAll
    public void setup() {
        Mockito
            .when(aiService.ask("How many letter 'e's are in splendiferous?"))
            .thenReturn("There are 2 letter 'e's in the word splendiferous.");
    }

    @Test
    void testCountEndpoint() {
        given()
            .body("How many letter 'e's are in splendiferous?")
        .when()
            .post("/ai")
        .then()
            .log().ifValidationFails()
            .statusCode(200)
            .body(containsString("2"));
    }

}