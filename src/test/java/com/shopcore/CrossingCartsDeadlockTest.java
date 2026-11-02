package com.shopcore;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import org.junit.jupiter.api.Test;

class CrossingCartsDeadlockTest {

    @Test
    void cartsWithItemsInOppositeOrderNeverDeadlock() throws InterruptedException {
        int rounds = 500;
        ProductCatalog catalog = new ProductCatalog();
        Product mouse = new Product("SKU-1001", "Wireless Mouse", Category.ELECTRONICS, Money.of("19.99"), rounds * 2);
        Product book = new Product("SKU-2001", "Clean Code", Category.BOOKS, Money.of("35.50"), rounds * 2);
        catalog.addProduct(mouse);
        catalog.addProduct(book);
        OrderService orderService = new OrderService(catalog);

        // Daemon threads: if they ever deadlock, they won't stop the test run from exiting.
        ExecutorService pool = Executors.newFixedThreadPool(8, task -> {
            Thread t = new Thread(task);
            t.setDaemon(true);
            return t;
        });
        for (int i = 0; i < rounds; i++) {
            pool.submit(() -> {
                ShoppingCart mouseFirst = new ShoppingCart();
                mouseFirst.addItem(mouse, 1);
                mouseFirst.addItem(book, 1);
                orderService.placeOrder(mouseFirst);
            });
            pool.submit(() -> {
                ShoppingCart bookFirst = new ShoppingCart();
                bookFirst.addItem(book, 1);
                bookFirst.addItem(mouse, 1);
                orderService.placeOrder(bookFirst);
            });
        }
        pool.shutdown();

        boolean finished = pool.awaitTermination(10, TimeUnit.SECONDS);

        assertTrue(finished);              // false here would mean threads are stuck: a deadlock
        assertEquals(0, mouse.getStock());
        assertEquals(0, book.getStock());
    }
}