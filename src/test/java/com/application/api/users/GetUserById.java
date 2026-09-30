package com.application.api.users;

import com.application.api.utils.DatabaseHelper;
import io.restassured.http.ContentType;
import org.junit.jupiter.api.Test;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.notNullValue;

class GetUserById extends UserApiTestBase {

    @Test
    void getUserById_shouldReturn200() {
        // Предусловие: пользователя добавляем напрямую в БД, id получаем из результата
        // вставки и проверяем, что GET /api/users/{id} возвращает 200 и совпадающие с БД поля.
        String email = uniqueEmail();
        long id = DatabaseHelper.insertUser("Ivanov", "Ivan", email);

        given()
                .contentType(ContentType.JSON)
                .header("Authorization", "Bearer " + token)
                .when()
                .get("/api/users/{id}", id)
                .then()
                .statusCode(200)
                .body("id", equalTo((int) id))
                .body("lastName", equalTo("Ivanov"))
                .body("firstName", equalTo("Ivan"))
                .body("email", equalTo(email))
                .body("createdAt", notNullValue())
                .body("updatedAt", notNullValue());
    }

    @Test
    void getUserById_withoutToken_shouldReturn401() {
        // Проверяет, что GET /api/users/{id} без заголовка Authorization
        // отклоняется с 401.
        given()
                .contentType(ContentType.JSON)
                .when()
                .get("/api/users/{id}", 1)
                .then()
                .statusCode(401)
                .body("message", containsString("Authorization"));
    }

    @Test
    void getUserById_withInvalidToken_shouldReturn401() {
        // Проверяет, что запрос с несуществующим/битым токеном отклоняется с 401.
        given()
                .contentType(ContentType.JSON)
                .header("Authorization", "Bearer invalid-token")
                .when()
                .get("/api/users/{id}", 1)
                .then()
                .statusCode(401)
                .body("message", containsString("Invalid or expired token"));
    }

    @Test
    void getUserById_notFound_shouldReturn400() {
        // Проверяет обращение к несуществующему id: записи нет в БД → сервис
        // бросает RuntimeException → обработчик возвращает 400 "user not found".
        given()
                .contentType(ContentType.JSON)
                .header("Authorization", "Bearer " + token)
                .when()
                .get("/api/users/{id}", 999999999)
                .then()
                .statusCode(400)
                .body("message", containsString("user not found"));
    }
}