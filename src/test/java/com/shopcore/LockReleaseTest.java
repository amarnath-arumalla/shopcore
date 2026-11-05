package com.shopcore;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import org.junit.jupiter.api.Test;

class LockReleaseTest {

    @Test
    void failedOrderReleasesItsLockForOtherCustomers() throws Exception {
        ProductCatalog catalog = new ProductCatalog();
        Product mouse = new Product("SKU-1001", "Wireless Mouse", Category.ELECTRONICS, Money.of("19.99"), 1);
        catalog.addProduct(mouse);
        OrderService orderService = new OrderService(catalog);

        // 1. An order fails INSIDE the locked section (stock vanished after it was added to the cart).
        ShoppingCart unlucky = new ShoppingCart();
        unlucky.addItem(mouse, 1);
        mouse.removeStock(1);
        assertThrows(OutOfStockException.class, () -> orderService.placeOrder(unlucky));

        // 2. Restock, then a DIFFERENT thread tries to buy. It must not hang.
        mouse.addStock(10);
        ExecutorService otherCustomer = Executors.newSingleThreadExecutor(task -> {
            Thread t = new Thread(task);
            t.setDaemon(true);
            return t;
        });
        Future<Order> next = otherCustomer.submit(() -> {
            ShoppingCart cart = new ShoppingCart();
            cart.addItem(mouse, 1);
            return orderService.placeOrder(cart);
        });

        Order order = next.get(5, TimeUnit.SECONDS);   // TimeoutException here = lock was never released
        assertTrue(order.getOrderId().startsWith("ORD-"));
        assertEquals(9, mouse.getStock());
        otherCustomer.shutdownNow();
    }
}