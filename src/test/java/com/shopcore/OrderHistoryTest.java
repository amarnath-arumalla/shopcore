package com.shopcore;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;
import org.junit.jupiter.api.Test;

class OrderHistoryTest {

    private Order order(String id) {
        OrderLine line = new OrderLine("SKU-1001", "Wireless Mouse", Money.of("19.99"), 1);
        return new Order(id, List.of(line), Money.ZERO, "No discount");
    }

    @Test
    void boundedHistoryNeverGrowsPastItsLimit() {
        OrderHistory history = OrderHistory.bounded(3);

        for (int i = 1; i <= 10; i++) {
            history.record(order("ORD-" + i));
        }

        assertEquals(3, history.size());
        assertFalse(history.find("ORD-1").isPresent());   // oldest ones were forgotten
        assertTrue(history.find("ORD-10").isPresent());   // newest ones are kept
    }

    @Test
    void recentlyLookedUpOrdersAreKeptLonger() {
        OrderHistory history = OrderHistory.bounded(2);
        history.record(order("ORD-1"));
        history.record(order("ORD-2"));

        history.find("ORD-1");              // ORD-1 is now the most recently used
        history.record(order("ORD-3"));     // so ORD-2 is the one forgotten

        assertTrue(history.find("ORD-1").isPresent());
        assertFalse(history.find("ORD-2").isPresent());
    }

    @Test
    void unboundedHistoryKeepsEverything() {
        OrderHistory history = OrderHistory.unbounded();

        for (int i = 1; i <= 1000; i++) {
            history.record(order("ORD-" + i));
        }

        assertEquals(1000, history.size());
    }
}