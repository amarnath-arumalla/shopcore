package com.shopcore.jdbc;

import com.shopcore.OutOfStockException;
import com.shopcore.Product;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

public class JdbcDemo {

    public static void main(String[] args) throws Exception {
        Database database = Database.fromEnvironment();
        ProductRepository products = new ProductRepository(database);
        JdbcOrderService orders = new JdbcOrderService(database);

        // 1. Read every product from PostgreSQL into Java objects
        System.out.println("=== Products in the database ===");
        for (Product p : products.findAll()) {
            System.out.println("  " + p + " " + p.getPrice() + ", stock " + p.getStock());
        }

        // 2. Look one up safely
        System.out.println("=== Lookup ===");
        System.out.println("  SKU-2002 -> " + products.findBySku("SKU-2002").map(Product::getName).orElse("not found"));
        System.out.println("  SKU-9999 -> " + products.findBySku("SKU-9999").map(Product::getName).orElse("not found"));

        // 3. SQL injection: the same evil input, unsafe vs safe
        String evilInput = "nothing' OR '1'='1";
        System.out.println("=== SQL injection with input: " + evilInput + " ===");
        System.out.println("  UNSAFE (string concatenation) returned " + products.findBySkuUnsafe(evilInput).size() + " products");
        System.out.println("  SAFE (PreparedStatement) returned " + products.findBySku(evilInput).stream().count() + " products");

        // 4. Place an order in one transaction
        System.out.println("=== Place order ===");
        String orderNumber = orders.placeOrder("carol@example.com", "SKU-3001", 2);
        System.out.println("  Carol ordered 2 mugs -> " + orderNumber + ", mug stock now "
                + products.findBySku("SKU-3001").orElseThrow().getStock());

        // 5. A failing order rolls back completely
        try {
            orders.placeOrder("carol@example.com", "SKU-2001", 50);
        } catch (OutOfStockException e) {
            System.out.println("  Rejected: " + e.getMessage());
        }
        try {
            orders.placeOrder("nobody@example.com", "SKU-3001", 1);
        } catch (IllegalArgumentException e) {
            System.out.println("  Rejected: " + e.getMessage() + " -> mug stock still "
                    + products.findBySku("SKU-3001").orElseThrow().getStock());
        }

        // 6. Flash sale: 20 customers at once, only 3 cables in stock
        System.out.println("=== Flash sale: 20 buyers, 3 USB-C cables ===");
        try (Connection connection = database.connect();
             PreparedStatement reset = connection.prepareStatement("UPDATE products SET stock = 3 WHERE sku = 'SKU-1002'")) {
            reset.executeUpdate();
        }
        int sold = 0;
        int soldOut = 0;
        try (ExecutorService pool = Executors.newVirtualThreadPerTaskExecutor()) {
            List<Future<String>> results = new ArrayList<>();
            for (int i = 0; i < 20; i++) {
                results.add(pool.submit(() -> orders.placeOrder("bob@example.com", "SKU-1002", 1)));
            }
            for (Future<String> result : results) {
                try {
                    result.get();
                    sold++;
                } catch (ExecutionException e) {
                    if (e.getCause() instanceof OutOfStockException) {
                        soldOut++;
                    } else {
                        throw e;
                    }
                }
            }
        }
        System.out.println("  sold " + sold + ", turned away " + soldOut + ", cable stock now "
                + products.findBySku("SKU-1002").orElseThrow().getStock());
    }
}