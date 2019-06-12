package com.revolut.mtt;

import io.restassured.RestAssured;
import io.restassured.response.Response;
import org.apache.http.HttpStatus;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import javax.json.Json;
import javax.json.JsonObject;
import java.math.BigDecimal;

import static io.restassured.RestAssured.given;
import static io.restassured.config.JsonConfig.jsonConfig;
import static io.restassured.path.json.config.JsonPathConfig.NumberReturnType.BIG_DECIMAL;
import static org.hamcrest.Matchers.*;

@ExtendWith(JoobyIntegrationTestExtension.class)
public class AccountControllerIntegrationTest {

    @Test
    void account_should_be_created() {
        // create user

        // given
        final JsonObject newUser = Json.createObjectBuilder()
                .add("username", "monica")
                .build();

        // when
        final Response createUserResponse = given()
                .body(newUser.toString())
                .when()
                .post("/users");

        // then
        final Long userId = createUserResponse.getBody()
                .jsonPath()
                .getLong("id");

        // create account

        // given
        final JsonObject newAccount = Json.createObjectBuilder()
                .add("userId", userId)
                .add("balance", "21.89")
                .build();

        // when
        final Response createResponse = given()
                .config(RestAssured.config().jsonConfig(jsonConfig().numberReturnType(BIG_DECIMAL)))
                .body(newAccount.toString())
                .when()
                .post("/accounts");

        // then
        createResponse.then()
                .statusCode(HttpStatus.SC_CREATED).body("id", not(emptyOrNullString()))
                .body("userId", is(userId.intValue()))
                .body("balance", is(new BigDecimal("21.89")));

        // fetch

        // given
        final Long accountId = createResponse.getBody()
                .jsonPath()
                .getLong("id");

        // when
        final Response fetchResponse = given()
                .config(RestAssured.config().jsonConfig(jsonConfig().numberReturnType(BIG_DECIMAL)))
                .when()
                .get("/accounts/{accountId}", accountId);

        // then
        fetchResponse.then()
                .statusCode(HttpStatus.SC_OK)
                .body("id", is(accountId.intValue()))
                .body("userId", is(userId.intValue()))
                .body("balance", is(new BigDecimal("21.89")));
    }
}
