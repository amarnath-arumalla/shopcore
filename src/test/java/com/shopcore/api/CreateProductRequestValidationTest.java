package com.shopcore.api;

import static org.assertj.core.api.Assertions.assertThat;

import java.math.BigDecimal;
import java.util.Set;
import java.util.stream.Collectors;

import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

class CreateProductRequestValidationTest {

    private static ValidatorFactory factory;
    private static Validator validator;

    @BeforeAll
    static void setUp() {
        factory = Validation.buildDefaultValidatorFactory();
        validator = factory.getValidator();
    }

    @AfterAll
    static void tearDown() {
        factory.close();
    }

    private Set<String> invalidFields(CreateProductRequest request) {
        return validator.validate(request).stream()
                .map(v -> v.getPropertyPath().toString())
                .collect(Collectors.toSet());
    }

    private static CreateProductRequest valid() {
        return new CreateProductRequest("SKU-9001", "Keyboard", ProductCategory.ELECTRONICS,
                new BigDecimal("1299.00"), 5);
    }

    @Test
    void validRequestHasNoErrors() {
        assertThat(invalidFields(valid())).isEmpty();
    }

    @Test
    void negativePriceAndStockAreRejected() {
        var request = new CreateProductRequest("SKU-9001", "Keyboard", ProductCategory.ELECTRONICS,
                new BigDecimal("-5"), -1);
        assertThat(invalidFields(request)).containsExactlyInAnyOrder("price", "stock");
    }

    @Test
    void missingFieldsAreRejected() {
        var request = new CreateProductRequest(null, " ", null, null, null);
        assertThat(invalidFields(request))
                .containsExactlyInAnyOrder("sku", "name", "category", "price", "stock");
    }

    @Test
    void threeDecimalPlacesIsRejected() {
        var request = new CreateProductRequest("SKU-9001", "Keyboard", ProductCategory.ELECTRONICS,
                new BigDecimal("10.999"), 1);
        assertThat(invalidFields(request)).containsExactly("price");
    }

    @Test
    void priceTooLargeForNumeric10_2IsRejected() {
        var request = new CreateProductRequest("SKU-9001", "Keyboard", ProductCategory.ELECTRONICS,
                new BigDecimal("123456789.00"), 1);                  // 9 digits before the point
        assertThat(invalidFields(request)).containsExactly("price");
    }

    @Test
    void skuLongerThan20IsRejected() {
        var request = new CreateProductRequest("SKU-123456789012345678", "Keyboard",
                ProductCategory.ELECTRONICS, new BigDecimal("10.00"), 1);
        assertThat(invalidFields(request)).containsExactly("sku");
    }
}