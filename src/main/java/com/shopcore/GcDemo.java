package com.shopcore;

import java.lang.management.GarbageCollectorMXBean;
import java.lang.management.ManagementFactory;

public class GcDemo {

    private static final int CHECKOUTS = 3_000_000;
    private static final int RECENT_ORDERS_KEPT = 300_000;

    // Long-lived: the recent-order history lives as long as the app (old generation).
    private static OrderHistory history;

    public static void main(String[] args) {
        history = OrderHistory.bounded(RECENT_ORDERS_KEPT);

        ProductCatalog catalog = new ProductCatalog();
        Product mouse = new Product("SKU-1001", "Wireless Mouse", Category.ELECTRONICS,
                Money.of("19.99"), Integer.MAX_VALUE);
        catalog.addProduct(mouse);
        OrderService orderService = new OrderService(catalog);

        long slowerThan1ms = 0;
        long slowerThan10ms = 0;
        long worstNanos = 0;
        long start = System.nanoTime();

        for (int i = 0; i < CHECKOUTS; i++) {
            long before = System.nanoTime();

            // Short-lived objects: the cart and cart item become garbage right after checkout.
            ShoppingCart cart = new ShoppingCart();
            cart.addItem(mouse, 1);
            Order order = orderService.placeOrder(cart);
            history.record(order);

            long took = System.nanoTime() - before;
            worstNanos = Math.max(worstNanos, took);
            if (took > 1_000_000) {
                slowerThan1ms++;
            }
            if (took > 10_000_000) {
                slowerThan10ms++;
            }
        }

        long totalMs = (System.nanoTime() - start) / 1_000_000;
        System.out.println("Checkouts: " + CHECKOUTS + " in " + totalMs + " ms ("
                + (CHECKOUTS * 1000L / Math.max(totalMs, 1)) + " per second)");
        System.out.println("Slowest checkout: " + (worstNanos / 1_000_000) + " ms");
        System.out.println("Checkouts slower than 1 ms: " + slowerThan1ms + ", slower than 10 ms: " + slowerThan10ms);

        for (GarbageCollectorMXBean gc : ManagementFactory.getGarbageCollectorMXBeans()) {
            System.out.println("GC '" + gc.getName() + "': ran " + gc.getCollectionCount()
                    + " times, " + gc.getCollectionTime() + " ms total");
        }
    }
}