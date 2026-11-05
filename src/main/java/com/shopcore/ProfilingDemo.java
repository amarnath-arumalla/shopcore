package com.shopcore;

import java.util.ArrayList;
import java.util.List;

public class ProfilingDemo {

    public static void main(String[] args) {
        boolean fast = args.length > 0 && args[0].equals("fast");

        ProductCatalog catalog = new ProductCatalog();
        Product mouse = new Product("SKU-1001", "Wireless Mouse", Category.ELECTRONICS,
                Money.of("19.99"), Integer.MAX_VALUE);
        catalog.addProduct(mouse);
        OrderService orderService = new OrderService(catalog);

        long start = System.currentTimeMillis();

        // The "end of day" job: place today's orders, then build the sales report.
        List<Order> todaysOrders = new ArrayList<>();
        for (int i = 0; i < 40_000; i++) {
            ShoppingCart cart = new ShoppingCart();
            cart.addItem(mouse, 1);
            todaysOrders.add(orderService.placeOrder(cart));
        }
        long ordersDone = System.currentTimeMillis();

        SalesReport.Summary report = fast ? SalesReport.buildFast(todaysOrders) : SalesReport.buildSlow(todaysOrders);
        long reportDone = System.currentTimeMillis();

        System.out.println("Mode: " + (fast ? "fast" : "slow"));
        System.out.println("Placing 40000 orders took " + (ordersDone - start) + " ms");
        System.out.println("Building the report took  " + (reportDone - ordersDone) + " ms -> "
                + report.orderCount() + " orders, revenue " + report.revenue()
                + ", duplicate IDs " + report.duplicateIds());
    }
}
