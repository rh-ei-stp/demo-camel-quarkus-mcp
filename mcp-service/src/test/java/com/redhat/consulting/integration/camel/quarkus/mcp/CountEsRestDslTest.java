package com.redhat.consulting.integration.camel.quarkus.mcp;

import io.quarkus.test.junit.QuarkusTest;
import org.junit.jupiter.api.Test;

import static io.restassured.RestAssured.given;
import static org.hamcrest.CoreMatchers.is;

@QuarkusTest
class CountEsRestDslTest {
    @Test
    void testCountEndpoint() {
        given().body("splendiferous")
          .when().post("/camel/countEs")
          .then()
             .statusCode(200)
             .body(is("2"));
    }
}