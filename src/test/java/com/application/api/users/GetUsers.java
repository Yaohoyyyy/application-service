package com.application.api.users;

import com.application.api.utils.DatabaseHelper;
import io.restassured.http.ContentType;
import org.junit.jupiter.api.Test;

import java.util.List;

import static io.restassured.RestAssured.given;
import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.containsString;

class GetUsers extends UserApiTestBase {

    @Test
    void getAllUsers_shouldReturn200() {
        // Предусловие: пользователя добавляем напрямую в БД, затем проверяем,
        // что GET /api/users (запись должна быть видна в выдаче).
        String email = uniqueEmail();
        DatabaseHelper.insertUser("Ivanov", "Ivan", email);

        List<String> emails = given()
                .contentType(ContentType.JSON)
                .header("Authorization", "Bearer " + token)
                .when()
                .get("/api/users")
                .then()
                .statusCode(200)
                .extract()
                .jsonPath()
                .getList("email", String.class);

        assertThat(emails).contains(email);
    }

    @Test
    void getAllUsers_withoutToken_shouldReturn401() {
        // Проверяет, что GET /api/users без заголовка Authorization отклоняется
        // с 401 и сообщением о отсутствующем/невалидном заголовке.
        given()
                .contentType(ContentType.JSON)
                .when()
                .get("/api/users")
                .then()
                .statusCode(401)
                .body("message", containsString("Authorization"));
    }

    @Test
    void getAllUsers_withInvalidToken_shouldReturn401() {
        // Проверяет, что запрос с несуществующим/битым токеном отклоняется с 401.
        given()
                .contentType(ContentType.JSON)
                .header("Authorization", "Bearer invalid-token")
                .when()
                .get("/api/users")
                .then()
                .statusCode(401)
                .body("message", containsString("Invalid or expired token"));
    }
}