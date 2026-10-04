package com.application.api.cards;

import io.restassured.http.ContentType;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.notNullValue;

class UpdateCard extends CardApiTestBase {

    // Проверяет, что обновление карты без токена возвращает 401
    @Test
    void updateCard_withoutToken_shouldReturn401() {
        given()
                .contentType(ContentType.JSON)
                .body(cardBody(uniqueCardNumber(), "Ivanov", 5000))
                .when()
                .put("/api/cards/1")
                .then()
                .statusCode(401)
                .body("message", equalTo("Missing or invalid Authorization header"));
    }

    // Проверяет обновление несуществующей карты: возвращает 404
    @Test
    void updateCard_notFound_shouldReturn404() {
        String cardNumber = uniqueCardNumber();
        given()
                .header("Authorization", "Bearer " + token)
                .contentType(ContentType.JSON)
                .body(cardBody(cardNumber, "Ivanov Ivan", 5000))
                .when()
                .put("/api/cards/999999999")
                .then()
                .statusCode(404);
    }

    // Проверяет успешное обновление существующей карты
    @Test
    void updateCard_shouldReturn200() {
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
                .contentType(ContentType.JSON)
                .body(Map.of(
                        "cardNumber", cardNumber,
                        "cardHolder", "Petrov Petr",
                        "limit", 20000))
                .when()
                .put("/api/cards/" + id)
                .then()
                .statusCode(200)
                .body("id", equalTo((int) id))
                .body("cardHolder", equalTo("Petrov Petr"))
                .body("limit", equalTo(20000));
    }
}
