package com.shopcore;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

public class FlashSaleDemo {

    private static final int STOCK = 1000;
    private static final int CUSTOMERS = 3000;

    public static void main(String[] args) throws InterruptedException {
        System.out.println("Fixed pool of 8 threads:");
        try (ExecutorService pool = Executors.newFixedThreadPool(8)) {
            runFlashSale(pool);
        }

        System.out.println("Virtual threads (one per customer):");
        try (ExecutorService virtual = Executors.newVirtualThreadPerTaskExecutor()) {
            runFlashSale(virtual);
        }
    }

    private static void runFlashSale(ExecutorService executor) throws InterruptedException {
        ProductCatalog catalog = new ProductCatalog();
        Product mouse = new Product("SKU-1001", "Wireless Mouse", Category.ELECTRONICS, Money.of("19.99"), STOCK);
        Product book = new Product("SKU-2001", "Clean Code", Category.BOOKS, Money.of("35.50"), STOCK);
        catalog.addProduct(mouse);
        catalog.addProduct(book);
        OrderService orderService = new OrderService(catalog);

        // Half the customers want a mouse, half want a book. Each gets their own task.
        List<Future<Order>> results = new ArrayList<>();
        for (int i = 0; i < CUSTOMERS; i++) {
            Product wanted = (i % 2 == 0) ? mouse : book;
            results.add(executor.submit(() -> {
                ShoppingCart cart = new ShoppingCart();
                cart.addItem(wanted, 1);
                return orderService.placeOrder(cart);
            }));
        }

        int sold = 0;
        int soldOut = 0;
        Set<String> ids = new HashSet<>();
        for (Future<Order> result : results) {
            try {
                Order order = result.get();          // waits for that customer to finish
                sold++;
                ids.add(order.getOrderId());
            } catch (ExecutionException e) {
                if (e.getCause() instanceof OutOfStockException) {
                    soldOut++;
                } else {
                    throw new IllegalStateException("Unexpected failure", e.getCause());
                }
            }
        }

        System.out.println("  sold " + sold + ", turned away " + soldOut
                + ", mouse left " + mouse.getStock() + ", book left " + book.getStock()
                + ", unique IDs " + ids.size());
    }
}