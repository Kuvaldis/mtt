package com.revolut.mtt;

import io.restassured.RestAssured;
import io.restassured.response.Response;
import org.apache.http.HttpStatus;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import javax.json.Json;
import javax.json.JsonObject;
import java.math.BigDecimal;
import java.util.concurrent.*;

import static io.restassured.RestAssured.given;
import static io.restassured.config.JsonConfig.jsonConfig;
import static io.restassured.path.json.config.JsonPathConfig.NumberReturnType.BIG_DECIMAL;
import static org.junit.jupiter.api.Assertions.assertEquals;

@ExtendWith(JoobyIntegrationTestExtension.class)
public class TransferControllerIntegrationTest {

    private static long chandler;
    private static long joey;
    private static long phoebe;

    @BeforeAll
    static void initUsers() {
        chandler = createUser("chandler");
        joey = createUser("joey");
        phoebe = createUser("phoebe");
    }

    @SuppressWarnings("Duplicates")
    @Test
    void money_should_be_transferred() {
        // given
        final long chandlerAccount = createAccount(chandler, new BigDecimal("7832.12"));
        final long joeyAccount = createAccount(joey, new BigDecimal("12.89"));
        final JsonObject transfer = Json.createObjectBuilder()
                .add("endUserId", chandler)
                .add("sourceAccountId", chandlerAccount)
                .add("destinationAccountId", joeyAccount)
                .add("amount", new BigDecimal("350.00"))
                .build();

        // when
        final Response transferResponse = given().body(transfer.toString())
                .when()
                .post("/transfers");

        // then
        transferResponse.then()
                .statusCode(HttpStatus.SC_OK);
        final BigDecimal chandlerBalance = fetchBalance(chandlerAccount);
        assertEquals(new BigDecimal("7482.12"), chandlerBalance);
        final BigDecimal joeyBalance = fetchBalance(joeyAccount);
        assertEquals(new BigDecimal("362.89"), joeyBalance);
    }

    @SuppressWarnings("Duplicates")
    @Test
    void balance_should_be_not_less_than_transfer_amount() {
        // given
        final long chandlerAccount = createAccount(chandler, new BigDecimal("7832.12"));
        final long joeyAccount = createAccount(joey, new BigDecimal("12.19"));
        final JsonObject transfer = Json.createObjectBuilder()
                .add("endUserId", chandler)
                .add("sourceAccountId", joeyAccount)
                .add("destinationAccountId", chandlerAccount)
                .add("amount", new BigDecimal("350.00"))
                .build();

        // when
        final Response transferResponse = given().body(transfer.toString())
                .when()
                .post("/transfers");

        // then
        transferResponse.then()
                .statusCode(HttpStatus.SC_BAD_REQUEST)
                .body("[0].field", Matchers.is("amount"));
    }

    @Test
    void test_many_small_transfers_correct_summary_balance() throws Exception {
        // given
        final long account1 = createAccount(phoebe, new BigDecimal(100));
        final long account2 = createAccount(phoebe, new BigDecimal(200));
        final long account3 = createAccount(phoebe, new BigDecimal(300));
        final long[] accounts = {account1, account2, account3};
        final ExecutorService executorService = Executors.newFixedThreadPool(20);
        final int numberOfTransfers = 1000;

        // when
        final CompletableFuture[] futures = new CompletableFuture[numberOfTransfers];
        for (int i = 0; i < numberOfTransfers; i++) {
            futures[i] = CompletableFuture.supplyAsync(() -> {
                final int randomShift = ThreadLocalRandom.current().nextBoolean() ? 1 : -1;
                final int randomBase = ThreadLocalRandom.current().nextInt(0, accounts.length);
                final int randomAmount = ThreadLocalRandom.current().nextInt(30);
                final long sourceAccountId = accounts[randomBase];
                final long destinationAccountId = accounts[(randomBase + randomShift + accounts.length) % accounts.length];
                final JsonObject transfer = Json.createObjectBuilder()
                        .add("endUserId", phoebe)
                        .add("sourceAccountId", sourceAccountId)
                        .add("destinationAccountId", destinationAccountId)
                        .add("amount", new BigDecimal(randomAmount))
                        .build();
                given().body(transfer.toString())
                        .post("/transfers");
                return null;
            }, executorService);
        }
        CompletableFuture.allOf(futures)
                .get(20, TimeUnit.SECONDS);

        // then
        final BigDecimal balance1 = fetchBalance(account1);
        final BigDecimal balance2 = fetchBalance(account2);
        final BigDecimal balance3 = fetchBalance(account3);
        assertEquals(new BigDecimal(600), balance1.add(balance2).add(balance3));
    }

    private static long createUser(final String username) {
        // create user
        final JsonObject newUser = Json.createObjectBuilder()
                .add("username", username)
                .build();
        final Response createUserResponse = given()
                .body(newUser.toString())
                .when()
                .post("/users");
        return createUserResponse.getBody()
                .jsonPath()
                .getLong("id");

    }

    private long createAccount(final Long userId,
                               final BigDecimal balance) {
        final JsonObject newAccount = Json.createObjectBuilder()
                .add("userId", userId)
                .add("balance", balance)
                .build();
        final Response createResponse = given()
                .body(newAccount.toString())
                .when()
                .post("/accounts");
        return createResponse.getBody()
                .jsonPath()
                .getLong("id");
    }

    private BigDecimal fetchBalance(final Long accountId) {
        // when
        final Response fetchResponse = given()
                .config(RestAssured.config().jsonConfig(jsonConfig().numberReturnType(BIG_DECIMAL)))
                .when()
                .get("/accounts/{accountId}", accountId);
        return fetchResponse.getBody()
                .jsonPath()
                .getObject("balance", BigDecimal.class);
    }
}
