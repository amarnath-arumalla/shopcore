package com.shopcore.jdbc;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import com.shopcore.Category;
import com.shopcore.Money;
import com.shopcore.Product;

public class ProductRepository {

    private static final String COLUMNS = "sku, name, category, price, stock";

    private final Database database;

    public ProductRepository(Database database) {
        this.database = database;
    }

    public List<Product> findAll() throws SQLException {
        String sql = "SELECT " + COLUMNS + " FROM products ORDER BY sku";
        try (Connection connection = database.connect();
             PreparedStatement statement = connection.prepareStatement(sql);
             ResultSet rows = statement.executeQuery()) {
            List<Product> products = new ArrayList<>();
            while (rows.next()) {
                products.add(toProduct(rows));
            }
            return products;
        }
    }

    // SAFE: the SKU is sent as a parameter, never glued into the SQL text.
    public Optional<Product> findBySku(String sku) throws SQLException {
        String sql = "SELECT " + COLUMNS + " FROM products WHERE sku = ?";
        try (Connection connection = database.connect();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, sku);
            try (ResultSet rows = statement.executeQuery()) {
                return rows.next() ? Optional.of(toProduct(rows)) : Optional.empty();
            }
        }
    }

    // UNSAFE: for the SQL injection demo ONLY. Never write code like this.
    public List<Product> findBySkuUnsafe(String sku) throws SQLException {
        String sql = "SELECT " + COLUMNS + " FROM products WHERE sku = '" + sku + "'";
        try (Connection connection = database.connect();
             Statement statement = connection.createStatement();
             ResultSet rows = statement.executeQuery(sql)) {
            List<Product> products = new ArrayList<>();
            while (rows.next()) {
                products.add(toProduct(rows));
            }
            return products;
        }
    }

    // Turns the current database row into a Product object.
    private Product toProduct(ResultSet row) throws SQLException {
        return new Product(
                row.getString("sku"),
                row.getString("name"),
                Category.valueOf(row.getString("category")),
                new Money(row.getBigDecimal("price")),
                row.getInt("stock"));
    }
}