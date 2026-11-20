package com.shopcore.api;

import java.math.BigDecimal;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;

/** Full replacement of the editable fields. SKU comes from the URL; stock is NOT editable here. */
public record UpdateProductRequest(

        @NotBlank(message = "name is required")
        @Size(max = 200, message = "name must be at most 200 characters")
        String name,

        @NotNull(message = "category is required")
        ProductCategory category,

        @NotNull(message = "price is required")
        @DecimalMin(value = "0.01", message = "price must be at least 0.01")
        @Digits(integer = 8, fraction = 2,
                message = "price can have at most 8 digits and 2 decimal places")
        BigDecimal price,

        @NotNull(message = "version is required")
        @PositiveOrZero(message = "version cannot be negative")
        Integer version) {
}