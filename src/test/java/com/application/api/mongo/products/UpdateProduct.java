package com.application.api.mongo.products;

import io.restassured.http.ContentType;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.notNullValue;

class UpdateProduct extends MongoProductApiTestBase {

    // Проверяет, что обновление продукта без токена возвращает 401
    @Test
    void updateProduct_withoutToken_shouldReturn401() {
        given()
                .contentType(ContentType.JSON)
                .body(Map.of("name", uniqueName(), "categoryId", "cat", "price", 200))
                .when()
                .put("/api/mongo/products/123")
                .then()
                .statusCode(401)
                .body("message", equalTo("Missing or invalid Authorization header"));
    }

    // Проверяет обновление несуществующего продукта: возвращает 404
    @Test
    void updateProduct_notFound_shouldReturn404() {
        given()
                .header("Authorization", "Bearer " + token)
                .contentType(ContentType.JSON)
                .body(Map.of("name", uniqueName(), "categoryId", "somecat", "price", 200))
                .when()
                .put("/api/mongo/products/nonexistent-id")
                .then()
                .statusCode(404);
    }

    // Проверяет, что обновление с несуществующей категорией возвращает 400
    @Test
    void updateProduct_withInvalidCategory_shouldReturn400() {
        given()
                .header("Authorization", "Bearer " + token)
                .contentType(ContentType.JSON)
                .body(Map.of("name", uniqueName(), "categoryId", "nonexistent-cat", "price", 200))
                .when()
                .put("/api/mongo/products/nonexistent-id")
                .then()
                .statusCode(400)
                .body("message", notNullValue());
    }
}
