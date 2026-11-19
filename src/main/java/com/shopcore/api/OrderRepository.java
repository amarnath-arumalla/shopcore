package com.shopcore.api;

import java.math.BigDecimal;
import java.time.OffsetDateTime;

import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.stereotype.Repository;

@Repository
public class OrderRepository {

    private final JdbcClient jdbc;

    public OrderRepository(JdbcClient jdbc) {
        this.jdbc = jdbc;
    }

    public boolean customerExists(long customerId) {
        return jdbc.sql("SELECT EXISTS (SELECT 1 FROM customers WHERE id = :id)")
                .param("id", customerId)
                .query(Boolean.class)
                .single();
    }

    /** status, discount and created_at come from the column defaults ('PLACED', 0, now()). */
    public CreatedOrder insertOrder(long customerId) {
        return jdbc.sql("""
                        INSERT INTO orders (order_number, customer_id)
                        VALUES ('ORD-' || nextval('order_number_seq'), :customerId)
                        RETURNING id, order_number, status, discount, created_at
                        """)
                .param("customerId", customerId)
                .query((rs, rowNum) -> new CreatedOrder(
                        rs.getLong("id"),
                        rs.getString("order_number"),
                        rs.getString("status"),
                        rs.getBigDecimal("discount"),
                        rs.getObject("created_at", OffsetDateTime.class)))
                .single();
    }

    public void insertLine(long orderId, StockReservation reservation, int quantity) {
        jdbc.sql("""
                        INSERT INTO order_lines (order_id, product_id, product_name, unit_price, quantity)
                        VALUES (:orderId, :productId, :productName, :unitPrice, :quantity)
                        """)
                .param("orderId", orderId)
                .param("productId", reservation.productId())
                .param("productName", reservation.name())     // snapshot
                .param("unitPrice", reservation.unitPrice())  // snapshot
                .param("quantity", quantity)
                .update();
    }

    public record CreatedOrder(long id, String orderNumber, String status,
                               BigDecimal discount, OffsetDateTime createdAt) {
    }
}
