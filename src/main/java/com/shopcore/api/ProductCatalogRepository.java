package com.shopcore.api;

import java.util.List;
import java.util.Optional;

import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.stereotype.Repository;

@Repository
public class ProductCatalogRepository {

    // One mapper shared by every query: row -> ProductResponse
    private static final RowMapper<ProductResponse> PRODUCT_MAPPER = (rs, rowNum) -> new ProductResponse(
            rs.getString("sku"),
            rs.getString("name"),
            rs.getBigDecimal("price"),
            rs.getInt("stock"));

    private final JdbcClient jdbc;

    public ProductCatalogRepository(JdbcClient jdbc) {
        this.jdbc = jdbc;
    }

    public List<ProductResponse> findFirst(int limit) {
        return jdbc.sql("""
                        SELECT sku, name, price, stock
                        FROM products
                        ORDER BY sku
                        LIMIT :limit
                        """)
                .param("limit", limit)
                .query(PRODUCT_MAPPER)
                .list();
    }

    public Optional<ProductResponse> findBySku(String sku) {
        return jdbc.sql("""
                        SELECT sku, name, price, stock
                        FROM products
                        WHERE sku = :sku
                        """)
                .param("sku", sku)
                .query(PRODUCT_MAPPER)
                .optional();      // 0 rows -> Optional.empty(), 1 row -> Optional.of(...)
    }
}