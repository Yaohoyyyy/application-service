package com.application.api.mongo.categories;

import io.restassured.http.ContentType;
import org.junit.jupiter.api.Test;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.greaterThanOrEqualTo;

class GetCategories extends MongoCategoryApiTestBase {

    // Проверяет, что GET /api/mongo/categories без токена возвращает 401
    @Test
    void getCategories_withoutToken_shouldReturn401() {
        given()
                .contentType(ContentType.JSON)
                .when()
                .get("/api/mongo/categories")
                .then()
                .statusCode(401)
                .body("message", equalTo("Missing or invalid Authorization header"));
    }

    // Проверяет получение списка категорий с валидным токеном
    @Test
    void getCategories_withToken_shouldReturn200() {
        given()
                .header("Authorization", "Bearer " + token)
                .contentType(ContentType.JSON)
                .when()
                .get("/api/mongo/categories")
                .then()
                .statusCode(200)
                .body("size()", greaterThanOrEqualTo(0));
    }
}
