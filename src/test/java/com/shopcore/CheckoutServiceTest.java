package com.shopcore;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.concurrent.CompletionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class CheckoutServiceTest {

    private ExecutorService executor;
    private OrderService orderService;
    private Product mouse;
    private Product laptop;
    private CardPayment card;

    @BeforeEach
    void setUp() {
        executor = Executors.newVirtualThreadPerTaskExecutor();
        ProductCatalog catalog = new ProductCatalog();
        mouse = new Product("SKU-1001", "Wireless Mouse", Category.ELECTRONICS, Money.of("19.99"), 10);
        laptop = new Product("SKU-4001", "Laptop", Category.ELECTRONICS, Money.of("1499.00"), 5);
        catalog.addProduct(mouse);
        catalog.addProduct(laptop);
        orderService = new OrderService(catalog);
        card = new CardPayment("4111111111111111", Money.of("10000.00"));
    }

    @AfterEach
    void tearDown() {
        executor.shutdownNow();
    }

    private ShoppingCart cartWith(Product product) {
        ShoppingCart cart = new ShoppingCart();
        cart.addItem(product, 1);
        return cart;
    }

    @Test
    void fraudCheckAndShippingRunAtTheSameTime() {
        CheckoutService checkout = new CheckoutService(orderService, new FraudCheckService(300),
                new ShippingService(300), new EmailService(0), executor, 5000);

        long start = System.currentTimeMillis();
        CheckoutResult result = checkout.checkoutAsync(cartWith(mouse), card).join();
        long took = System.currentTimeMillis() - start;

        assertEquals("Arrives in 2 days", result.delivery());
        assertTrue(took < 550);   // one after another would take at least 600 ms
    }

    @Test
    void slowShippingFallsBackToDefaultAfterTimeout() {
        CheckoutService checkout = new CheckoutService(orderService, new FraudCheckService(0),
                new ShippingService(3000), new EmailService(0), executor, 100);

        long start = System.currentTimeMillis();
        CheckoutResult result = checkout.checkoutAsync(cartWith(mouse), card).join();
        long took = System.currentTimeMillis() - start;

        assertEquals(CheckoutService.NO_ESTIMATE, result.delivery());
        assertTrue(took < 1000);  // we did NOT wait the full 3 seconds
    }

    @Test
    void fraudulentOrderIsRejectedAndStockIsReturned() {
        EmailService email = new EmailService(0);
        CheckoutService checkout = new CheckoutService(orderService, new FraudCheckService(0),
                new ShippingService(0), email, executor, 5000);

        CompletionException e = assertThrows(CompletionException.class,
                () -> checkout.checkoutAsync(cartWith(laptop), card).join());

        assertTrue(e.getCause() instanceof FraudSuspectedException);
        assertEquals(5, laptop.getStock());
        assertEquals(0, email.getSent().size());
    }

    @Test
    void successfulCheckoutTakesPaymentAndReducesStock() {
        CheckoutService checkout = new CheckoutService(orderService, new FraudCheckService(0),
                new ShippingService(0), new EmailService(0), executor, 5000);

        CheckoutResult result = checkout.checkoutAsync(cartWith(mouse), card).join();

        assertTrue(result.transactionId().startsWith("CARD-1111"));
        assertEquals(9, mouse.getStock());
    }
}