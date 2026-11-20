package com.shopcore.api;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record OrderLineRequest(

        @NotBlank(message = "sku is required")
        String sku,

        @NotNull(message = "quantity is required")
        @Min(value = 1, message = "quantity must be at least 1")
        @Max(value = 100, message = "quantity cannot exceed 100 per line")
        Integer quantity) {
}
