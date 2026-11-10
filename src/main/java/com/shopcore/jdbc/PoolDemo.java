package com.shopcore.jdbc;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

public class PoolDemo {

    private static final int LOOKUPS = 200;

    public static void main(String[] args) throws Exception {
        // 1. Same 200 lookups: new connection each time vs. borrowed from a pool
        Database unpooled = Database.fromEnvironment();
        System.out.println("Without pool: " + timeLookups(unpooled) + " ms per lookup");

        try (Database pooled = Database.pooledFromEnvironment(10, 30_000)) {
            timeLookups(pooled);                  // warm-up: the pool opens its connections
            System.out.println("With pool:    " + timeLookups(pooled) + " ms per lookup");
            System.out.println("Pool after lookups: " + pooled.poolStats());
        }

        // 2. Pool exhaustion: 2 connections, 3 slow requests, 2-second wait limit
        System.out.println();
        System.out.println("=== Pool of 2, three requests that each hold a connection for 5 seconds ===");
        try (Database tiny = Database.pooledFromEnvironment(2, 2_000);
             ExecutorService threads = Executors.newVirtualThreadPerTaskExecutor()) {
            List<Future<String>> results = new ArrayList<>();
            for (int i = 1; i <= 3; i++) {
                int request = i;
                results.add(threads.submit(() -> slowRequest(tiny, request)));
                Thread.sleep(100);                // start them a moment apart
            }
            Thread.sleep(500);
            System.out.println("While busy: " + tiny.poolStats());
            for (Future<String> result : results) {
                System.out.println(result.get());
            }
        }
    }

    private static double timeLookups(Database database) throws SQLException {
        ProductRepository products = new ProductRepository(database);
        long start = System.nanoTime();
        for (int i = 0; i < LOOKUPS; i++) {
            products.findBySku("SKU-1001");
        }
        long elapsedMicros = (System.nanoTime() - start) / 1_000;
        return Math.round(elapsedMicros / (double) LOOKUPS / 10.0) / 100.0;   // ms, 2 decimals
    }

    // Holds a connection for 5 seconds, like a slow query or a forgotten transaction.
    private static String slowRequest(Database database, int request) {
        long start = System.currentTimeMillis();
        try (Connection connection = database.connect();
             PreparedStatement slow = connection.prepareStatement("SELECT pg_sleep(5)")) {
            slow.execute();
            return "Request " + request + ": finished after " + (System.currentTimeMillis() - start) + " ms";
        } catch (SQLException e) {
            return "Request " + request + ": FAILED after " + (System.currentTimeMillis() - start)
                    + " ms -> " + e.getClass().getSimpleName() + ": " + e.getMessage();
        }
    }
}