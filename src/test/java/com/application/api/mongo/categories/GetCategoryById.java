package com.application.api.mongo.categories;

import io.restassured.http.ContentType;
import org.junit.jupiter.api.Test;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.equalTo;

class GetCategoryById extends MongoCategoryApiTestBase {

    // Проверяет, что GET /api/mongo/categories/{id} без токена возвращает 401
    @Test
    void getCategoryById_withoutToken_shouldReturn401() {
        given()
                .contentType(ContentType.JSON)
                .when()
                .get("/api/mongo/categories/123")
                .then()
                .statusCode(401)
                .body("message", equalTo("Missing or invalid Authorization header"));
    }

    // Проверяет, что несуществующая категория возвращает 404
    @Test
    void getCategoryById_notFound_shouldReturn404() {
        given()
                .header("Authorization", "Bearer " + token)
                .contentType(ContentType.JSON)
                .when()
                .get("/api/mongo/categories/nonexistent-id")
                .then()
                .statusCode(404);
    }
}
