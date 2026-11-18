package com.shopcore.api;

import java.math.BigDecimal;

/** What the API promises to send. Not the database row, not the domain Product. */
public record ProductResponse(String sku, String name, BigDecimal price, int stock) {
}