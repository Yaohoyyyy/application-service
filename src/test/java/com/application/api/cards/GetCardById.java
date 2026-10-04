package com.application.api.cards;

import io.restassured.http.ContentType;
import org.junit.jupiter.api.Test;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.equalTo;

class GetCardById extends CardApiTestBase {

    // Проверяет, что GET /api/cards/{id} без токена возвращает 401
    @Test
    void getCardById_withoutToken_shouldReturn401() {
        given()
                .contentType(ContentType.JSON)
                .when()
                .get("/api/cards/1")
                .then()
                .statusCode(401)
                .body("message", equalTo("Missing or invalid Authorization header"));
    }

    // Проверяет, что несуществующая карта возвращает 404
    @Test
    void getCardById_notFound_shouldReturn404() {
        given()
                .header("Authorization", "Bearer " + token)
                .contentType(ContentType.JSON)
                .when()
                .get("/api/cards/999999999")
                .then()
                .statusCode(404);
    }
}
