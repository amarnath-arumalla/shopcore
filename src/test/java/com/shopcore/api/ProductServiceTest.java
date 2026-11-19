package com.shopcore.api;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class ProductServiceTest {

    private ProductCatalogRepository repository;
    private ProductService service;

    private static ProductResponse keyboard(int version, String price) {
        return new ProductResponse("SKU-9001", "Keyboard", ProductCategory.ELECTRONICS,
                new BigDecimal(price), 5, version);
    }

    @BeforeEach
    void setUp() {
        repository = mock(ProductCatalogRepository.class);
        service = new ProductService(repository);
    }

    @Test
    void returnsProductWhenSkuExists() {
        when(repository.findBySku("SKU-9001")).thenReturn(Optional.of(keyboard(0, "1299.00")));
        assertThat(service.getProduct("SKU-9001").price()).isEqualByComparingTo("1299.00");
    }

    @Test
    void throwsNotFoundWhenSkuMissing() {
        when(repository.findBySku("NOPE")).thenReturn(Optional.empty());
        ProductNotFoundException ex = assertThrows(ProductNotFoundException.class,
                () -> service.getProduct("NOPE"));
        assertThat(ex.getSku()).isEqualTo("NOPE");
    }

    @Test
    void updateSucceedsWhenVersionMatches() {
        var request = new UpdateProductRequest("Keyboard", ProductCategory.ELECTRONICS,
                new BigDecimal("1199.00"), 0);
        when(repository.update("SKU-9001", request)).thenReturn(Optional.of(keyboard(1, "1199.00")));

        ProductResponse result = service.updateProduct("SKU-9001", request);

        assertThat(result.version()).isEqualTo(1);
        assertThat(result.price()).isEqualByComparingTo("1199.00");
    }

    @Test
    void updateWithOldVersionThrowsStale() {
        var request = new UpdateProductRequest("RGB Keyboard", ProductCategory.ELECTRONICS,
                new BigDecimal("1299.00"), 0);
        when(repository.update("SKU-9001", request)).thenReturn(Optional.empty());     // 0 rows
        when(repository.findBySku("SKU-9001")).thenReturn(Optional.of(keyboard(1, "1199.00")));

        StaleProductVersionException ex = assertThrows(StaleProductVersionException.class,
                () -> service.updateProduct("SKU-9001", request));

        assertThat(ex.getExpectedVersion()).isEqualTo(0);
        assertThat(ex.getCurrentVersion()).isEqualTo(1);
    }

    @Test
    void updateOfMissingProductThrowsNotFound() {
        var request = new UpdateProductRequest("X", ProductCategory.BOOKS, new BigDecimal("1.00"), 0);
        when(repository.update("NOPE", request)).thenReturn(Optional.empty());
        when(repository.findBySku("NOPE")).thenReturn(Optional.empty());

        assertThrows(ProductNotFoundException.class, () -> service.updateProduct("NOPE", request));
    }
}