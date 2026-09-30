package com.application.api.utils;

import java.io.IOException;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Optional;
import java.util.Properties;

public class DatabaseHelper {

    private static final Properties PROPS = loadProperties();

    private DatabaseHelper() {
    }

    private static Properties loadProperties() {
        Properties props = new Properties();
        try (InputStream in = DatabaseHelper.class.getClassLoader().getResourceAsStream("api.properties")) {
            if (in == null) {
                throw new IllegalStateException("api.properties not found on test classpath");
            }
            props.load(in);
            return props;
        } catch (IOException e) {
            throw new IllegalStateException("Failed to load api.properties", e);
        }
    }

    public record UserRow(String lastName, String firstName, String email, String createdAt, String updatedAt) {
    }

    // Прямая вставка пользователя в таблицу users (предусловие для тестов).
    // Возвращает сгенерированный id. created_at/updated_at выставляются как now().
    public static long insertUser(String lastName, String firstName, String email) {
        String url = PROPS.getProperty("api.db.url");
        String user = PROPS.getProperty("api.db.user");
        String password = PROPS.getProperty("api.db.password");
        try (Connection conn = DriverManager.getConnection(url, user, password);
             PreparedStatement ps = conn.prepareStatement(
                     "INSERT INTO users (last_name, first_name, email, created_at, updated_at) " +
                             "VALUES (?, ?, ?, now(), now())",
                     Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, lastName);
            ps.setString(2, firstName);
            ps.setString(3, email);
            ps.executeUpdate();
            try (ResultSet keys = ps.getGeneratedKeys()) {
                if (!keys.next()) {
                    throw new IllegalStateException("No generated key returned for new user");
                }
                return keys.getLong(1);
            }
        } catch (SQLException e) {
            throw new IllegalStateException("Failed to insert user " + email + " into users table", e);
        }
    }

    // Прямое обращение к БД, чтобы проверить, что в таблицу users действительно
    // добавилась (или не добавилась) запись с заданным email.
    public static Optional<UserRow> findUserByEmail(String email) {
        String url = PROPS.getProperty("api.db.url");
        String user = PROPS.getProperty("api.db.user");
        String password = PROPS.getProperty("api.db.password");
        try (Connection conn = DriverManager.getConnection(url, user, password);
             PreparedStatement ps = conn.prepareStatement(
                     "SELECT last_name, first_name, email, created_at, updated_at FROM users WHERE email = ?")) {
            ps.setString(1, email);
            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) {
                    return Optional.empty();
                }
                return Optional.of(new UserRow(
                        rs.getString("last_name"),
                        rs.getString("first_name"),
                        rs.getString("email"),
                        String.valueOf(rs.getTimestamp("created_at")),
                        String.valueOf(rs.getTimestamp("updated_at"))));
            }
        } catch (SQLException e) {
            throw new IllegalStateException("Failed to query users table", e);
        }
    }
}