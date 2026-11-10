package com.shopcore.jdbc;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import com.zaxxer.hikari.HikariPoolMXBean;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class Database implements AutoCloseable {

    private final String url;
    private final String user;
    private final String password;
    private final HikariDataSource pool;   // null means "no pool: open a new connection every time"

    private Database(String url, String user, String password, HikariDataSource pool) {
        if (password == null || password.isBlank()) {
            throw new IllegalStateException("Database password is missing. Set the SHOPCORE_DB_PASSWORD environment variable.");
        }
        this.url = url;
        this.user = user;
        this.password = password;
        this.pool = pool;
    }

    // No pool: every connect() opens a brand-new connection (Lesson 24 behaviour).
    public static Database fromEnvironment() {
        return new Database(envUrl(), envUser(), envPassword(), null);
    }

    // With a pool: connections are opened once and reused.
    public static Database pooledFromEnvironment(int maxPoolSize, long connectionTimeoutMillis) {
        HikariConfig config = new HikariConfig();
        config.setPoolName("shopcore-pool");
        config.setJdbcUrl(envUrl());
        config.setUsername(envUser());
        config.setPassword(envPassword());
        config.setMaximumPoolSize(maxPoolSize);
        config.setConnectionTimeout(connectionTimeoutMillis);
        return new Database(envUrl(), envUser(), envPassword(), new HikariDataSource(config));
    }

    public Connection connect() throws SQLException {
        if (pool != null) {
            return pool.getConnection();          // borrow from the pool
        }
        return DriverManager.getConnection(url, user, password);
    }

    public String poolStats() {
        if (pool == null) {
            return "no pool";
        }
        HikariPoolMXBean stats = pool.getHikariPoolMXBean();
        return "active=" + stats.getActiveConnections()
                + ", idle=" + stats.getIdleConnections()
                + ", total=" + stats.getTotalConnections()
                + ", waiting=" + stats.getThreadsAwaitingConnection();
    }

    @Override
    public void close() {
        if (pool != null) {
            pool.close();                         // really closes every pooled connection
        }
    }

    private static String envUrl() {
        return System.getenv().getOrDefault("SHOPCORE_DB_URL", "jdbc:postgresql://localhost:5432/shopcore");
    }

    private static String envUser() {
        return System.getenv().getOrDefault("SHOPCORE_DB_USER", "shopcore_app");
    }

    private static String envPassword() {
        return System.getenv("SHOPCORE_DB_PASSWORD");
    }
}