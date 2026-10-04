package com.application.api.cards;

import io.restassured.http.ContentType;
import org.junit.jupiter.api.Test;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.greaterThanOrEqualTo;
import static org.hamcrest.Matchers.notNullValue;

class GetCards extends CardApiTestBase {

    // Проверяет, что GET /api/cards без токена возвращает 401
    @Test
    void getCards_withoutToken_shouldReturn401() {
        given()
                .contentType(ContentType.JSON)
                .when()
                .get("/api/cards")
                .then()
                .statusCode(401)
                .body("message", equalTo("Missing or invalid Authorization header"));
    }

    // Проверяет получение списка карт с валидным токеном
    @Test
    void getCards_withToken_shouldReturn200() {
        given()
                .header("Authorization", "Bearer " + token)
                .contentType(ContentType.JSON)
                .when()
                .get("/api/cards")
                .then()
                .statusCode(200)
                .body("size()", greaterThanOrEqualTo(0));
    }
}
