package com.shopcore;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.concurrent.atomic.AtomicInteger;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class StoreStatusTest {

    private StoreStatus store;
    private OrderService orderService;
    private Product mouse;

    @BeforeEach
    void setUp() {
        store = new StoreStatus();
        ProductCatalog catalog = new ProductCatalog();
        mouse = new Product("SKU-1001", "Wireless Mouse", Category.ELECTRONICS, Money.of("19.99"), 1_000_000);
        catalog.addProduct(mouse);
        orderService = new OrderService(catalog, store);
    }

    private ShoppingCart cartWithMouse() {
        ShoppingCart cart = new ShoppingCart();
        cart.addItem(mouse, 1);
        return cart;
    }

    @Test
    void closedStoreRejectsOrdersAndLeavesStockAlone() {
        store.close();

        assertThrows(StoreClosedException.class, () -> orderService.placeOrder(cartWithMouse()));
        assertEquals(1_000_000, mouse.getStock());
    }

    @Test
    void reopenedStoreAcceptsOrdersAgain() {
        store.close();
        store.reopen();

        orderService.placeOrder(cartWithMouse());

        assertEquals(999_999, mouse.getStock());
    }

    @Test
    void checkoutThreadSeesTheStoreCloseFromAnotherThread() throws InterruptedException {
        AtomicInteger ordersPlaced = new AtomicInteger(0);
        Thread checkoutWorker = new Thread(() -> {
            try {
                while (true) {
                    orderService.placeOrder(cartWithMouse());
                    ordersPlaced.incrementAndGet();
                }
            } catch (StoreClosedException e) {
                // the worker noticed the store closed and stopped
            }
        });
        checkoutWorker.setDaemon(true);
        checkoutWorker.start();

        Thread.sleep(200);
        store.close();
        checkoutWorker.join(2000);

        assertTrue(!checkoutWorker.isAlive());   // it stopped: the change was visible
        assertTrue(ordersPlaced.get() > 0);      // and it really was working before that
    }
}