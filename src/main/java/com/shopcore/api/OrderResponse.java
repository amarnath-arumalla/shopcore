package com.shopcore.api;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.List;

public record OrderResponse(String orderNumber, long customerId, String status,
                            BigDecimal subtotal, BigDecimal discount, BigDecimal total,
                            OffsetDateTime createdAt, List<OrderLineResponse> lines) {
}