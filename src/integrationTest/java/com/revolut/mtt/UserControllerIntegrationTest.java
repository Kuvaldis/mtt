package com.revolut.mtt;

import io.restassured.response.Response;
import org.apache.http.HttpStatus;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import javax.json.Json;
import javax.json.JsonObject;

import static io.restassured.RestAssured.get;
import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.*;

@ExtendWith(JoobyIntegrationTestExtension.class)
public class UserControllerIntegrationTest {

    @Test
    void user_should_be_created_and_fetched() {
        // create

        // given
        final JsonObject newUser = Json.createObjectBuilder()
                .add("username", "ross")
                .build();

        // when
        final Response createResponse = given()
                .body(newUser.toString())
                .when()
                .post("/users");

        // then
        createResponse.then()
                .statusCode(HttpStatus.SC_CREATED)
                .body("id", not(emptyOrNullString()))
                .body("username", is("ross"));

        // fetch

        // given
        final Long userId = createResponse.getBody()
                .jsonPath()
                .getLong("id");

        // when
        final Response fetchResponse = get("/users/{userId}", userId);

        // then
        fetchResponse.then()
                .statusCode(HttpStatus.SC_OK)
                .body("id", is(userId.intValue()))
                .body("username", is("ross"));
    }
}
