package com.shopcore.api;

import java.math.BigDecimal;

public record ProductResponse(String sku, String name, ProductCategory category,
                              BigDecimal price, int stock) {
}