package com.shopcore;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;

public class OrderHistory {

    private final int maxSize;
    private final Map<String, Order> orders;

    private OrderHistory(int maxSize) {
        this.maxSize = maxSize;
        // accessOrder = true: every get() moves that order to the "most recently used" end.
        this.orders = new LinkedHashMap<>(16, 0.75f, true) {
            @Override
            protected boolean removeEldestEntry(Map.Entry<String, Order> eldest) {
                return OrderHistory.this.maxSize > 0 && size() > OrderHistory.this.maxSize;
            }
        };
    }

    // Keeps EVERY order forever. This is the memory leak.
    public static OrderHistory unbounded() {
        return new OrderHistory(0);
    }

    // Keeps only the most recently used orders. Older ones are forgotten.
    public static OrderHistory bounded(int maxSize) {
        if (maxSize < 1) {
            throw new IllegalArgumentException("Max size must be at least 1");
        }
        return new OrderHistory(maxSize);
    }

    public synchronized void record(Order order) {
        orders.put(order.getOrderId(), order);
    }

    public synchronized Optional<Order> find(String orderId) {
        return Optional.ofNullable(orders.get(orderId));
    }

    public synchronized int size() {
        return orders.size();
    }
}
