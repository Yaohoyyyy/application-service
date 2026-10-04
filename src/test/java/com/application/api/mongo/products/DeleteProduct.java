package com.application.api.mongo.products;

import io.restassured.http.ContentType;
import org.junit.jupiter.api.Test;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.equalTo;

class DeleteProduct extends MongoProductApiTestBase {

    // Проверяет, что удаление продукта без токена возвращает 401
    @Test
    void deleteProduct_withoutToken_shouldReturn401() {
        given()
                .contentType(ContentType.JSON)
                .when()
                .delete("/api/mongo/products/123")
                .then()
                .statusCode(401)
                .body("message", equalTo("Missing or invalid Authorization header"));
    }

    // Проверяет удаление несуществующего продукта: возвращает 404
    @Test
    void deleteProduct_notFound_shouldReturn404() {
        given()
                .header("Authorization", "Bearer " + token)
                .contentType(ContentType.JSON)
                .when()
                .delete("/api/mongo/products/nonexistent-id")
                .then()
                .statusCode(404);
    }
}
