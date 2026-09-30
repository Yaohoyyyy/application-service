package com.application.api.users;

import com.application.api.utils.DatabaseHelper;
import io.restassured.http.ContentType;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static io.restassured.RestAssured.given;
import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.notNullValue;

class CreateUser extends UserApiTestBase {

    @Test
    void createUser_shouldReturn201() {
        // Проверяет успешное создание пользователя с валидным телом запроса:
        // эндпоинт должен вернуть 201 Created и тело с заполненными полями
        // (id, lastName, firstName, email). Дополнительно проверяем, что строка
        // с этими значениями реально появилась в таблице users.
        String email = uniqueEmail();

        given()
                .header("Authorization", "Bearer " + token)
                .contentType(ContentType.JSON)
                .body(userBody("Ivanov", "Ivan", email))
                .when()
                .post("/api/users")
                .then()
                .statusCode(201)
                .body("id", notNullValue())
                .body("lastName", equalTo("Ivanov"))
                .body("firstName", equalTo("Ivan"))
                .body("email", equalTo(email));

        DatabaseHelper.UserRow user = DatabaseHelper.findUserByEmail(email).orElseThrow(() ->
                new AssertionError("User with email " + email + " was not saved in the database"));
        assertThat(user.lastName()).isEqualTo("Ivanov");
        assertThat(user.firstName()).isEqualTo("Ivan");
        assertThat(user.email()).isEqualTo(email);
        assertThat(user.createdAt()).isNotEqualTo("null");
        assertThat(user.updatedAt()).isNotEqualTo("null");
    }

    @Test
    void createUser_withoutToken_shouldReturn401() {
        // Проверяет, что POST /api/users без заголовка Authorization отклоняется:
        // JWT-фильтр должен вернуть 401 с сообщением о отсутствующем/невалидном заголовке.
        // В БД никакой записи создано быть не должно.
        String email = uniqueEmail();

        given()
                .contentType(ContentType.JSON)
                .body(userBody("Ivanov", "Ivan", email))
                .when()
                .post("/api/users")
                .then()
                .statusCode(401)
                .body("message", containsString("Authorization"));

        assertThat(DatabaseHelper.findUserByEmail(email)).isEmpty();
    }

    @Test
    void createUser_withInvalidToken_shouldReturn401() {
        // Проверяет, что запрос с несуществующим/битым токеном (Bearer invalid-token)
        // отклоняется: фильтр должен вернуть 401 "Invalid or expired token".
        // В БД никакой записи создано быть не должно.
        String email = uniqueEmail();

        given()
                .header("Authorization", "Bearer invalid-token")
                .contentType(ContentType.JSON)
                .body(userBody("Ivanov", "Ivan", email))
                .when()
                .post("/api/users")
                .then()
                .statusCode(401)
                .body("message", containsString("Invalid or expired token"));

        assertThat(DatabaseHelper.findUserByEmail(email)).isEmpty();
    }

    @Test
    void createUser_withBlankLastName_shouldReturn400() {
        // Проверяет валидацию @NotBlank: если в теле отсутствует обязательное поле
        // lastName, сервер должен ответить 400 с упоминанием этого поля в message.
        // В БД никакой записи создано быть не должно.
        String email = uniqueEmail();

        given()
                .header("Authorization", "Bearer " + token)
                .contentType(ContentType.JSON)
                .body(Map.of(
                        "firstName", "Ivan",
                        "email", email))
                .when()
                .post("/api/users")
                .then()
                .statusCode(400)
                .body("message", containsString("lastName"));

        assertThat(DatabaseHelper.findUserByEmail(email)).isEmpty();
    }

    @Test
    void createUser_withInvalidEmail_shouldReturn400() {
        // Проверяет валидацию @Email: некорректный email ("not-an-email")
        // должен быть отклонён с 400 и упоминанием поля email in message.
        // В БД никакой записи создано быть не должно.
        given()
                .header("Authorization", "Bearer " + token)
                .contentType(ContentType.JSON)
                .body(userBody("Ivanov", "Ivan", "not-an-email"))
                .when()
                .post("/api/users")
                .then()
                .statusCode(400)
                .body("message", containsString("email"));

        assertThat(DatabaseHelper.findUserByEmail("not-an-email")).isEmpty();
    }

    @Test
    void createUser_withDuplicateEmail_shouldReturn400() {
        // Проверяет бизнес-правило об уникальности email: создаём пользователя,
        // затем пытаемся создать второго с тем же email —
        // сервер должен ответить 400 с сообщением "already exists".
        // В БД остаётся только одна запись — созданная первой.
        String email = uniqueEmail();

        given()
                .header("Authorization", "Bearer " + token)
                .contentType(ContentType.JSON)
                .body(userBody("Ivanov", "Ivan", email))
                .when()
                .post("/api/users")
                .then()
                .statusCode(201);

        given()
                .header("Authorization", "Bearer " + token)
                .contentType(ContentType.JSON)
                .body(userBody("Petrov", "Petr", email))
                .when()
                .post("/api/users")
                .then()
                .statusCode(400)
                .body("message", containsString("already exists"));

        DatabaseHelper.UserRow user = DatabaseHelper.findUserByEmail(email).orElseThrow(() ->
                new AssertionError("User with email " + email + " was not saved in the database"));
        assertThat(user.lastName()).isEqualTo("Ivanov");
        assertThat(user.firstName()).isEqualTo("Ivan");
    }
}