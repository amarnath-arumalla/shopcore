package com.shopcore.api;

import java.math.BigDecimal;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;

public record CreateProductRequest(

        @NotBlank(message = "sku is required")
        @Pattern(regexp = "^[A-Z0-9-]{3,20}$",                       // varchar(20)
                 message = "sku must be 3-20 chars: capital letters, digits, dashes")
        String sku,

        @NotBlank(message = "name is required")
        @Size(max = 200, message = "name must be at most 200 characters")   // varchar(200)
        String name,

        @NotNull(message = "category is required")
        ProductCategory category,

        @NotNull(message = "price is required")
        @DecimalMin(value = "0.01", message = "price must be at least 0.01")
        @Digits(integer = 8, fraction = 2,                              // numeric(10,2)
                message = "price can have at most 8 digits and 2 decimal places")
        BigDecimal price,

        @NotNull(message = "stock is required")
        @PositiveOrZero(message = "stock cannot be negative")
        Integer stock) {
}