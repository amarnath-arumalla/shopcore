package com.shopcore.api;

import java.util.List;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

public record PlaceOrderRequest(

        @NotNull(message = "customerId is required")
        @Positive(message = "customerId must be positive")
        Long customerId,

        @NotEmpty(message = "an order needs at least one line")
        @Size(max = 50, message = "an order can have at most 50 lines")
        List<@Valid OrderLineRequest> lines) {     // @Valid here = validate each line too
}