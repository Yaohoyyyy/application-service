package com.application.api.mongo.categories;

import io.restassured.http.ContentType;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.notNullValue;

class UpdateCategory extends MongoCategoryApiTestBase {

    // Проверяет, что обновление категории без токена возвращает 401
    @Test
    void updateCategory_withoutToken_shouldReturn401() {
        given()
                .contentType(ContentType.JSON)
                .body(Map.of("name", "NewName"))
                .when()
                .put("/api/mongo/categories/123")
                .then()
                .statusCode(401)
                .body("message", equalTo("Missing or invalid Authorization header"));
    }

    // Проверяет обновление несуществующей категории: возвращает 404
    @Test
    void updateCategory_notFound_shouldReturn404() {
        given()
                .header("Authorization", "Bearer " + token)
                .contentType(ContentType.JSON)
                .body(Map.of("name", uniqueName()))
                .when()
                .put("/api/mongo/categories/nonexistent-id")
                .then()
                .statusCode(404);
    }

    // Проверяет валидацию: пустое имя категории при обновлении должно вернуть 400
    @Test
    void updateCategory_withBlankName_shouldReturn400() {
        given()
                .header("Authorization", "Bearer " + token)
                .contentType(ContentType.JSON)
                .body(Map.of("name", ""))
                .when()
                .put("/api/mongo/categories/someid")
                .then()
                .statusCode(400)
                .body("message", notNullValue());
    }
}
