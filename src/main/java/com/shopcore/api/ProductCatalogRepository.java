package com.shopcore.api;

import java.util.List;
import java.util.Optional;

import org.springframework.dao.DuplicateKeyException;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.stereotype.Repository;

@Repository
public class ProductCatalogRepository {

    private static final RowMapper<ProductResponse> PRODUCT_MAPPER = (rs, rowNum) -> new ProductResponse(
            rs.getString("sku"),
            rs.getString("name"),
            ProductCategory.valueOf(rs.getString("category")),
            rs.getBigDecimal("price"),
            rs.getInt("stock"));

    private final JdbcClient jdbc;

    public ProductCatalogRepository(JdbcClient jdbc) {
        this.jdbc = jdbc;
    }

    public List<ProductResponse> findFirst(int limit) {
        return jdbc.sql("""
                        SELECT sku, name, category, price, stock
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
                        SELECT sku, name, category, price, stock
                        FROM products
                        WHERE sku = :sku
                        """)
                .param("sku", sku)
                .query(PRODUCT_MAPPER)
                .optional();
    }

    public ProductResponse insert(CreateProductRequest request) {
        try {
            return jdbc.sql("""
                            INSERT INTO products (sku, name, category, price, stock)
                            VALUES (:sku, :name, :category, :price, :stock)
                            RETURNING sku, name, category, price, stock
                            """)
                    .param("sku", request.sku())
                    .param("name", request.name())
                    .param("category", request.category().name())   // enum -> 'ELECTRONICS'
                    .param("price", request.price())
                    .param("stock", request.stock())
                    .query(PRODUCT_MAPPER)
                    .single();
        } catch (DuplicateKeyException e) {
            throw new DuplicateSkuException(request.sku(), e);
        }
    }
}