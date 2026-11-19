package com.shopcore.api;

import java.math.BigDecimal;

/** What we learn when stock is successfully taken: which product row, and its name/price right now. */
public record StockReservation(long productId, String name, BigDecimal unitPrice) {
}
