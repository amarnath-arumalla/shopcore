package com.shopcore.api;

import java.util.List;

import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.stereotype.Repository;

@Repository
public class ProductCatalogRepository {

    private final JdbcClient jdbc;

    // Spring sees this constructor and passes in the auto-configured JdbcClient
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
                .param("limit", limit)          // bound parameter, same safety as PreparedStatement '?'
                .query((rs, rowNum) -> new ProductResponse(
                        rs.getString("sku"),
                        rs.getString("name"),
                        rs.getBigDecimal("price"),
                        rs.getInt("stock")))
                .list();
    }
}