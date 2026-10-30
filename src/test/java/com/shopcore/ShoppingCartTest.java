package com.shopcore;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class ShoppingCartTest {

    private ShoppingCart cart;
    private Product mouse;
    private Product book;

    @BeforeEach
    void setUp() {
        cart = new ShoppingCart();
        mouse = new Product("SKU-1001", "Wireless Mouse", Category.ELECTRONICS, Money.of("19.99"), 10);
        book = new Product("SKU-2001", "Clean Code", Category.BOOKS, Money.of("35.50"), 2);
    }

    @Test
    void addingSameProductTwiceMergesIntoOneLine() {
        cart.addItem(mouse, 2);
        cart.addItem(mouse, 1);

        assertEquals(1, cart.getItems().size());
        assertEquals(3, cart.getItemCount());
    }

    @Test
    void totalAddsUpEveryLine() {
        cart.addItem(mouse, 3);
        cart.addItem(book, 1);

        assertEquals(Money.of("95.47"), cart.getTotal());
    }

    @Test
    void checksCombinedQuantityAgainstStock() {
        cart.addItem(book, 1);

        OutOfStockException e = assertThrows(OutOfStockException.class, () -> cart.addItem(book, 2));
        assertEquals(3, e.getRequested());
    }

    @Test
    void addingToCartDoesNotReduceStock() {
        cart.addItem(mouse, 4);

        assertEquals(10, mouse.getStock());
    }

    @Test
    void itemsListCannotBeModifiedFromOutside() {
        cart.addItem(mouse, 1);

        assertThrows(UnsupportedOperationException.class, () -> cart.getItems().clear());
        assertEquals(1, cart.getItemCount());
    }
}
