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

    @BeforeEach
    void setUp() {
        repository = mock(ProductCatalogRepository.class);   // fake repository, no database
        service = new ProductService(repository);           // constructor injection pays off here
    }

    @Test
    void returnsProductWhenSkuExists() {
        ProductResponse mouse = new ProductResponse("SKU-1001", "Mouse", new BigDecimal("499.00"), 10);
        when(repository.findBySku("SKU-1001")).thenReturn(Optional.of(mouse));

        assertThat(service.getProduct("SKU-1001")).isEqualTo(mouse);
    }

    @Test
    void throwsNotFoundWhenSkuMissing() {
        when(repository.findBySku("NOPE")).thenReturn(Optional.empty());

        ProductNotFoundException ex = assertThrows(ProductNotFoundException.class,
                () -> service.getProduct("NOPE"));

        assertThat(ex.getSku()).isEqualTo("NOPE");
    }
}