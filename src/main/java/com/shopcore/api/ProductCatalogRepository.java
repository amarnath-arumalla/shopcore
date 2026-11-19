package com.shopcore.api;

import java.util.List;
import java.util.Optional;

import org.springframework.dao.DuplicateKeyException;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.stereotype.Repository;

@Repository
public class ProductCatalogRepository {

    private static final String COLUMNS = "sku, name, category, price, stock, version";

    private static final RowMapper<ProductResponse> PRODUCT_MAPPER = (rs, rowNum) -> new ProductResponse(
            rs.getString("sku"),
            rs.getString("name"),
            ProductCategory.valueOf(rs.getString("category")),
            rs.getBigDecimal("price"),
            rs.getInt("stock"),
            rs.getInt("version"));

    private final JdbcClient jdbc;

    public ProductCatalogRepository(JdbcClient jdbc) {
        this.jdbc = jdbc;
    }

    public List<ProductResponse> findFirst(int limit) {
        return jdbc.sql("SELECT " + COLUMNS + " FROM products ORDER BY sku LIMIT :limit")
                .param("limit", limit)
                .query(PRODUCT_MAPPER)
                .list();
    }

    public Optional<ProductResponse> findBySku(String sku) {
        return jdbc.sql("SELECT " + COLUMNS + " FROM products WHERE sku = :sku")
                .param("sku", sku)
                .query(PRODUCT_MAPPER)
                .optional();
    }

    public ProductResponse insert(CreateProductRequest request) {
        try {
            return jdbc.sql("""
                            INSERT INTO products (sku, name, category, price, stock)
                            VALUES (:sku, :name, :category, :price, :stock)
                            RETURNING\s""" + COLUMNS)
                    .param("sku", request.sku())
                    .param("name", request.name())
                    .param("category", request.category().name())
                    .param("price", request.price())
                    .param("stock", request.stock())
                    .query(PRODUCT_MAPPER)
                    .single();
        } catch (DuplicateKeyException e) {
            throw new DuplicateSkuException(request.sku(), e);
        }
    }

    /**
     * Optimistic update: succeeds only if the row still has the version the client read.
     * Empty result = product missing OR version changed (caller decides which).
     */
    public Optional<ProductResponse> update(String sku, UpdateProductRequest request) {
        return jdbc.sql("""
                        UPDATE products
                        SET name     = :name,
                            category = :category,
                            price    = :price,
                            version  = version + 1
                        WHERE sku = :sku
                          AND version = :version
                        RETURNING\s""" + COLUMNS)
                .param("name", request.name())
                .param("category", request.category().name())
                .param("price", request.price())
                .param("sku", sku)
                .param("version", request.version())
                .query(PRODUCT_MAPPER)
                .optional();
    }

    public Optional<StockReservation> reserveStock(String sku, int quantity) {
        return jdbc.sql("""
                        UPDATE products
                        SET stock = stock - :quantity
                        WHERE sku = :sku
                          AND stock >= :quantity
                        RETURNING id, name, price
                        """)
                .param("quantity", quantity)
                .param("sku", sku)
                .query((rs, rowNum) -> new StockReservation(
                        rs.getLong("id"),
                        rs.getString("name"),
                        rs.getBigDecimal("price")))
                .optional();
    }
}