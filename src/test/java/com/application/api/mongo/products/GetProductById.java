package com.application.api.mongo.products;

import io.restassured.http.ContentType;
import org.junit.jupiter.api.Test;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.equalTo;

class GetProductById extends MongoProductApiTestBase {

    // Проверяет, что GET /api/mongo/products/{id} без токена возвращает 401
    @Test
    void getProductById_withoutToken_shouldReturn401() {
        given()
                .contentType(ContentType.JSON)
                .when()
                .get("/api/mongo/products/123")
                .then()
                .statusCode(401)
                .body("message", equalTo("Missing or invalid Authorization header"));
    }

    // Проверяет, что несуществующий продукт возвращает 404
    @Test
    void getProductById_notFound_shouldReturn404() {
        given()
                .header("Authorization", "Bearer " + token)
                .contentType(ContentType.JSON)
                .when()
                .get("/api/mongo/products/nonexistent-id")
                .then()
                .statusCode(404);
    }
}
