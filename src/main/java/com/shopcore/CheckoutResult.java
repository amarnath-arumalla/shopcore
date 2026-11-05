package com.shopcore;

public record CheckoutResult(String orderId, Money total, String transactionId, String delivery) {
}