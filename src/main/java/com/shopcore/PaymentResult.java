package com.shopcore;

public record PaymentResult(boolean success, String transactionId, String message) {

    public static PaymentResult approved(String transactionId) {
        return new PaymentResult(true, transactionId, "Approved");
    }

    public static PaymentResult declined(String reason) {
        return new PaymentResult(false, null, reason);
    }
}
