package com.shopcore;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.List;
import org.junit.jupiter.api.Test;

class SalesReportTest {

    private Order order(String id, String price) {
        OrderLine line = new OrderLine("SKU-1001", "Wireless Mouse", Money.of(price), 1);
        return new Order(id, List.of(line), Money.ZERO, "No discount");
    }

    private final List<Order> orders = List.of(
            order("ORD-1", "10.00"),
            order("ORD-2", "20.00"),
            order("ORD-1", "5.50"));    // same ID twice: a duplicate

    @Test
    void slowVersionCountsRevenueAndDuplicates() {
        SalesReport.Summary summary = SalesReport.buildSlow(orders);

        assertEquals(3, summary.orderCount());
        assertEquals(Money.of("35.50"), summary.revenue());
        assertEquals(1, summary.duplicateIds());
    }

    @Test
    void fastVersionGivesExactlyTheSameAnswer() {
        assertEquals(SalesReport.buildSlow(orders), SalesReport.buildFast(orders));
    }
}