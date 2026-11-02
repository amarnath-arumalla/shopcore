package com.shopcore;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.CountDownLatch;

public class FlashSaleDemo {

    private static final int STOCK = 1000;
    private static final int CUSTOMERS = 2000;

    public static void main(String[] args) throws InterruptedException {
        int runs = 5;
        for (int run = 1; run <= runs; run++) {
            runFlashSale(run);
        }
    }

    private static void runFlashSale(int run) throws InterruptedException {
        ProductCatalog catalog = new ProductCatalog();
        Product mouse = new Product("SKU-1001", "Wireless Mouse", Category.ELECTRONICS, Money.of("19.99"), STOCK);
        catalog.addProduct(mouse);
        OrderService orderService = new OrderService(catalog);

        int customers = CUSTOMERS;
        List<Order> successfulOrders = Collections.synchronizedList(new ArrayList<>());
        CountDownLatch startingGun = new CountDownLatch(1);
        List<Thread> threads = new ArrayList<>();

        for (int i = 0; i < customers; i++) {
            Thread customer = new Thread(() -> {
                try {
                    ShoppingCart cart = new ShoppingCart();
                    cart.addItem(mouse, 1);
                    startingGun.await();              // everyone waits here...
                    Order order = orderService.placeOrder(cart);
                    successfulOrders.add(order);
                } catch (OutOfStockException e) {
                    // sold out: this customer goes home empty-handed
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            });
            threads.add(customer);
            customer.start();
        }

        startingGun.countDown();                      // ...then all go at once
        for (Thread t : threads) {
            t.join();
        }

        Set<String> uniqueIds = new HashSet<>();
        for (Order order : successfulOrders) {
            uniqueIds.add(order.getOrderId());
        }

        System.out.println("Run " + run + ": had " + STOCK + " mice, sold " + successfulOrders.size()
                + ", stock left " + mouse.getStock()
                + ", unique order IDs " + uniqueIds.size());
    }
}
