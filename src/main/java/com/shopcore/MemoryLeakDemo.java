package com.shopcore;

import java.util.Scanner;

public class MemoryLeakDemo {

    private static final int TOTAL_ORDERS = 500_000;

    // Long-lived, like a service object that lives as long as the app does.
    private static OrderHistory history;

    public static void main(String[] args) {
        String mode = (args.length > 0) ? args[0] : "leaky";
        history = mode.equals("bounded")
                ? OrderHistory.bounded(1_000)
                : OrderHistory.unbounded();

        Runtime runtime = Runtime.getRuntime();
        System.out.println("Mode: " + mode + ", max heap " + mb(runtime.maxMemory()) + " MB, PID "
                + ProcessHandle.current().pid());

        ProductCatalog catalog = new ProductCatalog();
        Product mouse = new Product("SKU-1001", "Wireless Mouse", Category.ELECTRONICS,
                Money.of("19.99"), Integer.MAX_VALUE);
        catalog.addProduct(mouse);
        OrderService orderService = new OrderService(catalog);

        for (int i = 1; i <= TOTAL_ORDERS; i++) {
            ShoppingCart cart = new ShoppingCart();
            cart.addItem(mouse, 1);
            Order order = orderService.placeOrder(cart);
            history.record(order);

            if (i % 100_000 == 0) {
                System.gc();   // ask the JVM to clean up so we see what is REALLY still in use
                long used = runtime.totalMemory() - runtime.freeMemory();
                System.out.println("  " + i + " orders placed, history holds " + history.size()
                        + ", heap in use " + mb(used) + " MB");
            }
        }

        System.out.println("Done. Press Enter to exit (use this pause to run jcmd).");
        new Scanner(System.in).nextLine();
    }

    private static long mb(long bytes) {
        return bytes / (1024 * 1024);
    }
}