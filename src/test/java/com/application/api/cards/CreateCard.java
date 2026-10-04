package com.application.api.cards;

import io.restassured.http.ContentType;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.notNullValue;

class CreateCard extends CardApiTestBase {

    // Проверяет успешное создание карты с валидным телом запроса
    @Test
    void createCard_shouldReturn201() {
        String cardNumber = uniqueCardNumber();

        given()
                .header("Authorization", "Bearer " + token)
                .contentType(ContentType.JSON)
                .body(cardBody(cardNumber, "Ivanov Ivan", 10000))
                .when()
                .post("/api/cards")
                .then()
                .statusCode(201)
                .body("id", notNullValue())
                .body("cardNumber", equalTo(cardNumber))
                .body("cardHolder", equalTo("Ivanov Ivan"));
    }

    // Проверяет, что создание карты без токена возвращает 401
    @Test
    void createCard_withoutToken_shouldReturn401() {
        String cardNumber = uniqueCardNumber();

        given()
                .contentType(ContentType.JSON)
                .body(cardBody(cardNumber, "Ivanov Ivan", 10000))
                .when()
                .post("/api/cards")
                .then()
                .statusCode(401)
                .body("message", equalTo("Missing or invalid Authorization header"));
    }

    // Проверяет валидацию: пустой cardNumber должен вернуть 400
    @Test
    void createCard_withBlankCardNumber_shouldReturn400() {
        given()
                .header("Authorization", "Bearer " + token)
                .contentType(ContentType.JSON)
                .body(Map.of(
                        "cardHolder", "Ivanov Ivan",
                        "limit", 10000))
                .when()
                .post("/api/cards")
                .then()
                .statusCode(400)
                .body("message", notNullValue());
    }
}
