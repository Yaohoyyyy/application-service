package com.application.api.mongo.products;

import io.restassured.http.ContentType;
import org.junit.jupiter.api.Test;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.greaterThanOrEqualTo;

class GetProducts extends MongoProductApiTestBase {

    // Проверяет, что GET /api/mongo/products без токена возвращает 401
    @Test
    void getProducts_withoutToken_shouldReturn401() {
        given()
                .contentType(ContentType.JSON)
                .when()
                .get("/api/mongo/products")
                .then()
                .statusCode(401)
                .body("message", equalTo("Missing or invalid Authorization header"));
    }

    // Проверяет получение списка продуктов с валидным токеном
    @Test
    void getProducts_withToken_shouldReturn200() {
        given()
                .header("Authorization", "Bearer " + token)
                .contentType(ContentType.JSON)
                .when()
                .get("/api/mongo/products")
                .then()
                .statusCode(200)
                .body("size()", greaterThanOrEqualTo(0));
    }
}
