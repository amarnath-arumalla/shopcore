package com.shopcore;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicInteger;
import org.junit.jupiter.api.Test;

class OrderServiceConcurrencyTest {

    @Test
    void manyCustomersAtOnceNeverOversellAndNeverShareOrderIds() throws InterruptedException {
        int stock = 1000;
        int customers = 2000;
        ProductCatalog catalog = new ProductCatalog();
        Product mouse = new Product("SKU-1001", "Wireless Mouse", Category.ELECTRONICS, Money.of("19.99"), stock);
        catalog.addProduct(mouse);
        OrderService orderService = new OrderService(catalog);

        Set<String> orderIds = ConcurrentHashMap.newKeySet();
        List<Thread> threads = new ArrayList<>();
        CountDownLatch startingGun = new CountDownLatch(1);
        AtomicInteger sold = new AtomicInteger(0);

        for (int i = 0; i < customers; i++) {
            Thread customer = new Thread(() -> {
                try {
                    ShoppingCart cart = new ShoppingCart();
                    cart.addItem(mouse, 1);
                    startingGun.await();
                    Order order = orderService.placeOrder(cart);
                    orderIds.add(order.getOrderId());
                    sold.incrementAndGet();
                } catch (OutOfStockException e) {
                    // expected for the customers who arrive after it sells out
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            });
            threads.add(customer);
            customer.start();
        }
        startingGun.countDown();
        for (Thread t : threads) {
            t.join();
        }

        assertEquals(stock, sold.get());             // sold exactly what we had
        assertEquals(0, mouse.getStock());           // nothing left, nothing negative
        assertEquals(stock, orderIds.size());        // every order has its own ID
    }
}