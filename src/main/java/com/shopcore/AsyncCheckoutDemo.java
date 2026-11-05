package com.shopcore;

import java.util.concurrent.CompletionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class AsyncCheckoutDemo {

    public static void main(String[] args) {
        ProductCatalog catalog = new ProductCatalog();
        Product mouse = new Product("SKU-1001", "Wireless Mouse", Category.ELECTRONICS, Money.of("19.99"), 100);
        Product laptop = new Product("SKU-4001", "Laptop", Category.ELECTRONICS, Money.of("1499.00"), 5);
        catalog.addProduct(mouse);
        catalog.addProduct(laptop);
        OrderService orderService = new OrderService(catalog);
        CardPayment card = new CardPayment("4111111111111111", Money.of("10000.00"));
        EmailService email = new EmailService(500);

        try (ExecutorService executor = Executors.newVirtualThreadPerTaskExecutor()) {
            CheckoutService normal = new CheckoutService(orderService, new FraudCheckService(300),
                    new ShippingService(400), email, executor, 1000);

            // 1. Sequential: fraud (300) + shipping (400) + email (500), one after another
            long start = System.currentTimeMillis();
            CheckoutResult r1 = normal.checkoutSequential(cartWith(mouse), card);
            System.out.println("Sequential: " + r1.orderId() + " " + r1.total() + ", " + r1.delivery()
                    + " -> customer waited " + (System.currentTimeMillis() - start) + " ms");

            // 2. Async: fraud and shipping in parallel, email in the background
            start = System.currentTimeMillis();
            CheckoutResult r2 = normal.checkoutAsync(cartWith(mouse), card).join();
            System.out.println("Async:      " + r2.orderId() + " " + r2.total() + ", " + r2.delivery()
                    + " -> customer waited " + (System.currentTimeMillis() - start) + " ms");

            // 3. Shipping provider is very slow (3 s), but we only wait 500 ms for it
            CheckoutService slowShipping = new CheckoutService(orderService, new FraudCheckService(300),
                    new ShippingService(3000), email, executor, 500);
            start = System.currentTimeMillis();
            CheckoutResult r3 = slowShipping.checkoutAsync(cartWith(mouse), card).join();
            System.out.println("Timeout:    " + r3.orderId() + ", " + r3.delivery()
                    + " -> customer waited " + (System.currentTimeMillis() - start) + " ms");

            // 4. Fraud check flags a big order: payment never happens, stock comes back
            System.out.println("Laptop stock before: " + laptop.getStock());
            try {
                normal.checkoutAsync(cartWith(laptop), card).join();
            } catch (CompletionException e) {
                System.out.println("Rejected:   " + e.getCause().getMessage());
            }
            System.out.println("Laptop stock after:  " + laptop.getStock());
        }
        // The executor closed only after every background email finished.
        System.out.println("Emails sent: " + email.getSent().size());
        for (String line : email.getSent()) {
            System.out.println("  " + line);
        }
    }

    private static ShoppingCart cartWith(Product product) {
        ShoppingCart cart = new ShoppingCart();
        cart.addItem(product, 1);
        return cart;
    }
}