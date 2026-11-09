package com.shopcore.jdbc;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class Database {

    private final String url;
    private final String user;
    private final String password;

    public Database(String url, String user, String password) {
        if (password == null || password.isBlank()) {
            throw new IllegalStateException("Database password is missing. Set the SHOPCORE_DB_PASSWORD environment variable.");
        }
        this.url = url;
        this.user = user;
        this.password = password;
    }

    // Reads connection settings from environment variables, never from code.
    public static Database fromEnvironment() {
        String url = System.getenv().getOrDefault("SHOPCORE_DB_URL", "jdbc:postgresql://localhost:5432/shopcore");
        String user = System.getenv().getOrDefault("SHOPCORE_DB_USER", "shopcore_app");
        String password = System.getenv("SHOPCORE_DB_PASSWORD");
        return new Database(url, user, password);
    }

    public Connection connect() throws SQLException {
        return DriverManager.getConnection(url, user, password);
    }
}
