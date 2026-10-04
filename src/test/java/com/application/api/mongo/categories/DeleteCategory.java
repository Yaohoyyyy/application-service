package com.application.api.mongo.categories;

import io.restassured.http.ContentType;
import org.junit.jupiter.api.Test;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.equalTo;

class DeleteCategory extends MongoCategoryApiTestBase {

    // Проверяет, что удаление категории без токена возвращает 401
    @Test
    void deleteCategory_withoutToken_shouldReturn401() {
        given()
                .contentType(ContentType.JSON)
                .when()
                .delete("/api/mongo/categories/123")
                .then()
                .statusCode(401)
                .body("message", equalTo("Missing or invalid Authorization header"));
    }

    // Проверяет удаление несуществующей категории: возвращает 404
    @Test
    void deleteCategory_notFound_shouldReturn404() {
        given()
                .header("Authorization", "Bearer " + token)
                .contentType(ContentType.JSON)
                .when()
                .delete("/api/mongo/categories/nonexistent-id")
                .then()
                .statusCode(404);
    }
}
