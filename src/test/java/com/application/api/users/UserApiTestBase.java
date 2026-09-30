package com.application.api.users;

import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import org.junit.jupiter.api.BeforeAll;

import java.io.IOException;
import java.io.InputStream;
import java.util.Map;
import java.util.Properties;
import java.util.UUID;

import static io.restassured.RestAssured.given;

abstract class UserApiTestBase {

    private static final Properties PROPS = loadProperties();

    protected static String token;

    private static Properties loadProperties() {
        Properties props = new Properties();
        try (InputStream in = UserApiTestBase.class.getClassLoader().getResourceAsStream("api.properties")) {
            if (in == null) {
                throw new IllegalStateException("api.properties not found on test classpath");
            }
            props.load(in);
            return props;
        } catch (IOException e) {
            throw new IllegalStateException("Failed to load api.properties", e);
        }
    }

    private static String login() {
        return given()
                .contentType(ContentType.JSON)
                .body(Map.of(
                        "login", PROPS.getProperty("api.login"),
                        "password", PROPS.getProperty("api.password")))
                .when()
                .post("/api/auth/login")
                .then()
                .statusCode(200)
                .extract()
                .path("token");
    }

    private static String loginWithRetry() {
        IllegalStateException last = null;
        for (int attempt = 0; attempt < 3; attempt++) {
            try {
                return login();
            } catch (AssertionError e) {
                // токен в БД уникален, но JWT подписывается с точностью до секунды:
                // повторный логин в ту же секунду даёт дубликат и 400. Ждём следующую секунду.
                last = new IllegalStateException("Failed to login, attempt " + (attempt + 1), e);
                try {
                    Thread.sleep(1100);
                } catch (InterruptedException ie) {
                    Thread.currentThread().interrupt();
                    throw new IllegalStateException("Interrupted while waiting to retry login", ie);
                }
            }
        }
        throw last;
    }

    protected static Map<String, String> userBody(String lastName, String firstName, String email) {
        return Map.of(
                "lastName", lastName,
                "firstName", firstName,
                "email", email);
    }

    protected static String uniqueEmail() {
        return "user-" + UUID.randomUUID() + "@example.com";
    }

    @BeforeAll
    static void setUp() {
        RestAssured.baseURI = PROPS.getProperty("api.base.url");
        token = loginWithRetry();
    }
}