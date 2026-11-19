package com.shopcore.api;

import java.math.BigDecimal;

public record OrderLineResponse(String sku, String productName, BigDecimal unitPrice,
                                int quantity, BigDecimal lineTotal) {
}