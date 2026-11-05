package com.shopcore;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class SalesReport {

    public record Summary(int orderCount, Money revenue, int duplicateIds) {
    }

    // Slow version: checks for duplicate order IDs by comparing every order with every earlier one.
    public static Summary buildSlow(List<Order> orders) {
        Money revenue = Money.ZERO;
        int duplicates = 0;
        for (int i = 0; i < orders.size(); i++) {
            Order order = orders.get(i);
            revenue = revenue.plus(order.getTotal());
            for (int j = 0; j < i; j++) {
                if (orders.get(j).getOrderId().equals(order.getOrderId())) {
                    duplicates++;
                    break;
                }
            }
        }
        return new Summary(orders.size(), revenue, duplicates);
    }

    // Fast version: remembers the IDs already seen in a HashSet.
    public static Summary buildFast(List<Order> orders) {
        Money revenue = Money.ZERO;
        int duplicates = 0;
        Set<String> seenIds = new HashSet<>();
        for (Order order : orders) {
            revenue = revenue.plus(order.getTotal());
            if (!seenIds.add(order.getOrderId())) {
                duplicates++;
            }
        }
        return new Summary(orders.size(), revenue, duplicates);
    }
}
