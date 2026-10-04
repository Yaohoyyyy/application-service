package com.application.api.mongo.categories;

import io.restassured.http.ContentType;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.notNullValue;

class CreateCategory extends MongoCategoryApiTestBase {

    // Проверяет, что создание категории без токена возвращает 401
    @Test
    void createCategory_withoutToken_shouldReturn401() {
        given()
                .contentType(ContentType.JSON)
                .body(Map.of("name", uniqueName()))
                .when()
                .post("/api/mongo/categories")
                .then()
                .statusCode(401)
                .body("message", equalTo("Missing or invalid Authorization header"));
    }

    // Проверяет успешное создание категории
    @Test
    void createCategory_shouldReturn201() {
        String name = uniqueName();
        given()
                .header("Authorization", "Bearer " + token)
                .contentType(ContentType.JSON)
                .body(Map.of("name", name))
                .when()
                .post("/api/mongo/categories")
                .then()
                .statusCode(201)
                .body("id", notNullValue())
                .body("name", equalTo(name));
    }

    // Проверяет валидацию: пустое имя категории должно вернуть 400
    @Test
    void createCategory_withBlankName_shouldReturn400() {
        given()
                .header("Authorization", "Bearer " + token)
                .contentType(ContentType.JSON)
                .body(Map.of("name", ""))
                .when()
                .post("/api/mongo/categories")
                .then()
                .statusCode(400)
                .body("message", notNullValue());
    }
}
