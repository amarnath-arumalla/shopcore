package com.shopcore.jdbc;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import com.shopcore.OutOfStockException;

public class JdbcOrderService {

    private final Database database;

    // The two values the database hands back when an order row is created.
    private record CreatedOrder(long id, String orderNumber) {
    }

    public JdbcOrderService(Database database) {
        this.database = database;
    }

    // Places an order for one product, as ONE database transaction. Returns the order number.
    public String placeOrder(String customerEmail, String sku, int quantity) throws SQLException {
        if (quantity <= 0) {
            throw new IllegalArgumentException("Quantity must be greater than zero");
        }
        try (Connection connection = database.connect()) {
            connection.setAutoCommit(false);          // BEGIN
            try {
                reduceStock(connection, sku, quantity);
                CreatedOrder order = insertOrder(connection, customerEmail);
                insertLine(connection, order.id(), sku, quantity);
                connection.commit();                  // COMMIT
                return order.orderNumber();
            } catch (SQLException | RuntimeException e) {
                connection.rollback();                // ROLLBACK
                throw e;
            }
        }
    }

    // Lesson 21's safe conditional update, now in Java.
    private void reduceStock(Connection connection, String sku, int quantity) throws SQLException {
        String sql = "UPDATE products SET stock = stock - ? WHERE sku = ? AND stock >= ?";
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, quantity);
            statement.setString(2, sku);
            statement.setInt(3, quantity);
            int updated = statement.executeUpdate();
            if (updated == 0) {
                throw new OutOfStockException(sku, currentStock(connection, sku), quantity);
            }
        }
    }

    private int currentStock(Connection connection, String sku) throws SQLException {
        try (PreparedStatement statement = connection.prepareStatement("SELECT stock FROM products WHERE sku = ?")) {
            statement.setString(1, sku);
            try (ResultSet rows = statement.executeQuery()) {
                if (!rows.next()) {
                    throw new IllegalArgumentException("No product with SKU " + sku);
                }
                return rows.getInt("stock");
            }
        }
    }

    private CreatedOrder insertOrder(Connection connection, String customerEmail) throws SQLException {
        String sql = """
                INSERT INTO orders (order_number, customer_id)
                SELECT 'ORD-' || nextval('order_number_seq'), id FROM customers WHERE email = ?
                RETURNING id, order_number
                """;
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, customerEmail);
            try (ResultSet rows = statement.executeQuery()) {
                if (!rows.next()) {
                    throw new IllegalArgumentException("No customer with email " + customerEmail);
                }
                return new CreatedOrder(rows.getLong("id"), rows.getString("order_number"));
            }
        }
    }

    private void insertLine(Connection connection, long orderId, String sku, int quantity) throws SQLException {
        String sql = """
                INSERT INTO order_lines (order_id, product_id, product_name, unit_price, quantity)
                SELECT ?, id, name, price, ? FROM products WHERE sku = ?
                """;
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setLong(1, orderId);
            statement.setInt(2, quantity);
            statement.setString(3, sku);
            statement.executeUpdate();
        }
    }
}