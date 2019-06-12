package com.revolut.mtt;

import com.revolut.mtt.app.App;
import io.restassured.RestAssured;
import io.restassured.builder.RequestSpecBuilder;
import io.restassured.filter.log.LogDetail;
import io.restassured.http.ContentType;
import org.junit.jupiter.api.extension.AfterAllCallback;
import org.junit.jupiter.api.extension.BeforeAllCallback;
import org.junit.jupiter.api.extension.ExtensionContext;

public class JoobyIntegrationTestExtension implements BeforeAllCallback, AfterAllCallback {

    // todo support as a parameter
    private final App app = new App();

    @Override
    public void beforeAll(final ExtensionContext context) throws Exception {
        app.start("server.join=false");

        RestAssured.port = 8080;
        RestAssured.requestSpecification = new RequestSpecBuilder()
                .setAccept("application/json")
                .setContentType(ContentType.JSON)
                .log(LogDetail.ALL)
                .build();
    }

    @Override
    public void afterAll(final ExtensionContext context) throws Exception {
        app.stop();
    }
}
