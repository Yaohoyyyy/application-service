package com.application.api.mongo.products;

import io.restassured.http.ContentType;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.notNullValue;

class CreateProduct extends MongoProductApiTestBase {

    // Проверяет, что создание продукта без токена возвращает 401
    @Test
    void createProduct_withoutToken_shouldReturn401() {
        given()
                .contentType(ContentType.JSON)
                .body(Map.of("name", uniqueName(), "categoryId", "test", "price", 100))
                .when()
                .post("/api/mongo/products")
                .then()
                .statusCode(401)
                .body("message", equalTo("Missing or invalid Authorization header"));
    }

    // Проверяет, что создание продукта с несуществующей категорией возвращает 400
    @Test
    void createProduct_withInvalidCategory_shouldReturn400() {
        given()
                .header("Authorization", "Bearer " + token)
                .contentType(ContentType.JSON)
                .body(Map.of("name", uniqueName(), "categoryId", "nonexistent-cat", "price", 100))
                .when()
                .post("/api/mongo/products")
                .then()
                .statusCode(400)
                .body("message", notNullValue());
    }

    // Проверяет валидацию: пустое имя продукта должно вернуть 400
    @Test
    void createProduct_withBlankName_shouldReturn400() {
        given()
                .header("Authorization", "Bearer " + token)
                .contentType(ContentType.JSON)
                .body(Map.of("name", "", "categoryId", "cat", "price", 100))
                .when()
                .post("/api/mongo/products")
                .then()
                .statusCode(400)
                .body("message", notNullValue());
    }

    // Проверяет валидацию: пустой categoryId должен вернуть 400
    @Test
    void createProduct_withBlankCategoryId_shouldReturn400() {
        given()
                .header("Authorization", "Bearer " + token)
                .contentType(ContentType.JSON)
                .body(Map.of("name", uniqueName(), "categoryId", "", "price", 100))
                .when()
                .post("/api/mongo/products")
                .then()
                .statusCode(400)
                .body("message", notNullValue());
    }
}
