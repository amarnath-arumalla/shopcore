package com.shopcore.api;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.stereotype.Repository;

@Repository
public class OrderQueryRepository {

    private static final String ORDER_COLUMNS =
            "id, order_number, customer_id, status, discount, created_at";

    private static final RowMapper<OrderRow> ORDER_MAPPER = (rs, rowNum) -> new OrderRow(
            rs.getLong("id"),
            rs.getString("order_number"),
            rs.getLong("customer_id"),
            rs.getString("status"),
            rs.getBigDecimal("discount"),
            rs.getObject("created_at", OffsetDateTime.class));

    private final JdbcClient jdbc;

    public OrderQueryRepository(JdbcClient jdbc) {
        this.jdbc = jdbc;
    }

    public Optional<OrderRow> findByOrderNumber(String orderNumber) {
        return jdbc.sql("SELECT " + ORDER_COLUMNS + " FROM orders WHERE order_number = :orderNumber")
                .param("orderNumber", orderNumber)
                .query(ORDER_MAPPER)
                .optional();
    }

    /** One page of a customer's orders, newest first. Pass after = null for the first page. */
    public List<OrderRow> findCustomerOrders(long customerId, OrderCursor after, int limit) {
        if (after == null) {
            return jdbc.sql("SELECT " + ORDER_COLUMNS + """
                             FROM orders
                            WHERE customer_id = :customerId
                            ORDER BY created_at DESC, id DESC
                            LIMIT :limit
                            """)
                    .param("customerId", customerId)
                    .param("limit", limit)
                    .query(ORDER_MAPPER)
                    .list();
        }
        return jdbc.sql("SELECT " + ORDER_COLUMNS + """
                         FROM orders
                        WHERE customer_id = :customerId
                          AND (created_at, id) < (:afterCreatedAt, :afterId)
                        ORDER BY created_at DESC, id DESC
                        LIMIT :limit
                        """)
                .param("customerId", customerId)
                .param("afterCreatedAt", after.createdAt())
                .param("afterId", after.id())
                .param("limit", limit)
                .query(ORDER_MAPPER)
                .list();
    }

    /**
     * Lines for MANY orders in ONE query (the N+1 fix).
     * Returns orderId -> lines. Caller must not pass an empty list ("IN ()" is invalid SQL).
     */
    public Map<Long, List<OrderLineResponse>> findLines(List<Long> orderIds) {
        List<LineRow> rows = jdbc.sql("""
                        SELECT l.order_id, p.sku, l.product_name, l.unit_price, l.quantity
                        FROM order_lines l
                        JOIN products p ON p.id = l.product_id
                        WHERE l.order_id IN (:orderIds)
                        ORDER BY l.order_id, p.sku
                        """)
                .param("orderIds", orderIds)            // expands to (?, ?, ?, ...)
                .query((rs, rowNum) -> {
                    BigDecimal unitPrice = rs.getBigDecimal("unit_price");
                    int quantity = rs.getInt("quantity");
                    return new LineRow(rs.getLong("order_id"), new OrderLineResponse(
                            rs.getString("sku"),
                            rs.getString("product_name"),               // snapshot from checkout time
                            unitPrice,
                            quantity,
                            unitPrice.multiply(BigDecimal.valueOf(quantity))));
                })
                .list();

        return rows.stream().collect(Collectors.groupingBy(
                LineRow::orderId,
                LinkedHashMap::new,
                Collectors.mapping(LineRow::line, Collectors.toList())));
    }

    public record OrderRow(long id, String orderNumber, long customerId, String status,
                           BigDecimal discount, OffsetDateTime createdAt) {
    }

    private record LineRow(long orderId, OrderLineResponse line) {
    }
}
