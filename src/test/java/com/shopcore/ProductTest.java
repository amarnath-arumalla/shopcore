package com.shopcore;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

class ProductTest {

    private Product mouse;

    @BeforeEach
    void setUp() {
        mouse = new Product("SKU-1001", "Wireless Mouse", Category.ELECTRONICS, Money.of("19.99"), 10);
    }

    @Test
    void removeStockReducesStock() {
        mouse.removeStock(3);

        assertEquals(7, mouse.getStock());
    }

    @Test
    void removingTooMuchThrowsAndLeavesStockUnchanged() {
        OutOfStockException e = assertThrows(OutOfStockException.class, () -> mouse.removeStock(50));

        assertAll(
            () -> assertEquals("SKU-1001", e.getSku()),
            () -> assertEquals(10, e.getAvailable()),
            () -> assertEquals(50, e.getRequested()),
            () -> assertEquals(10, mouse.getStock())
        );
    }

    @ParameterizedTest
    @ValueSource(ints = {0, -1, -100})
    void rejectsQuantitiesThatAreNotPositive(int quantity) {
        assertThrows(IllegalArgumentException.class, () -> mouse.addStock(quantity));
        assertThrows(IllegalArgumentException.class, () -> mouse.removeStock(quantity));
    }

    @Test
    void productsWithSameSkuAreEqualEvenIfStockDiffers() {
        Product sameSku = new Product("SKU-1001", "Wireless Mouse", Category.ELECTRONICS, Money.of("19.99"), 99);

        assertEquals(mouse, sameSku);
        assertEquals(mouse.hashCode(), sameSku.hashCode());
    }

    @Test
    void lowStockIsThreeOrFewer() {
        mouse.removeStock(7);
        assertTrue(mouse.isLowStock());

        mouse.addStock(1);
        assertFalse(mouse.isLowStock());
    }
}
