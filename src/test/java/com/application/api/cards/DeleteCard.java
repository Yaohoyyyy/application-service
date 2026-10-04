package com.application.api.cards;

import io.restassured.http.ContentType;
import org.junit.jupiter.api.Test;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.equalTo;

class DeleteCard extends CardApiTestBase {

    // Проверяет, что удаление карты без токена возвращает 401
    @Test
    void deleteCard_withoutToken_shouldReturn401() {
        given()
                .contentType(ContentType.JSON)
                .when()
                .delete("/api/cards/1")
                .then()
                .statusCode(401)
                .body("message", equalTo("Missing or invalid Authorization header"));
    }

    // Проверяет удаление несуществующей карты: возвращает 404
    @Test
    void deleteCard_notFound_shouldReturn404() {
        given()
                .header("Authorization", "Bearer " + token)
                .contentType(ContentType.JSON)
                .when()
                .delete("/api/cards/999999999")
                .then()
                .statusCode(404);
    }

    // Проверяет успешное удаление карты
    @Test
    void deleteCard_shouldReturn204() {
        String cardNumber = uniqueCardNumber();

        long id = given()
                .header("Authorization", "Bearer " + token)
                .contentType(ContentType.JSON)
                .body(cardBody(cardNumber, "Ivanov Ivan", 10000))
                .when()
                .post("/api/cards")
                .then()
                .statusCode(201)
                .extract()
                .path("id");

        given()
                .header("Authorization", "Bearer " + token)
                .when()
                .delete("/api/cards/" + id)
                .then()
                .statusCode(204);

        given()
                .header("Authorization", "Bearer " + token)
                .when()
                .get("/api/cards/" + id)
                .then()
                .statusCode(404);
    }
}
