package com.shopcore;

public class PayPalPayment implements PaymentMethod {

    private final String email;
    private Money balance;
    private int chargeCount = 0;

    public PayPalPayment(String email, Money balance) {
        if (email == null || !email.contains("@")) {
            throw new IllegalArgumentException("A valid email is required");
        }
        if (balance == null) {
            throw new IllegalArgumentException("Balance is required");
        }
        this.email = email;
        this.balance = balance;
    }

    @Override
    public PaymentResult charge(Money amount) {
        if (amount == null || amount.isZero()) {
            throw new IllegalArgumentException("Amount must be greater than zero");
        }
        if (amount.isGreaterThan(balance)) {
            return PaymentResult.declined("PayPal declined: insufficient balance");
        }
        balance = balance.minus(amount);
        chargeCount = chargeCount + 1;
        return PaymentResult.approved("PP-" + chargeCount);
    }

    @Override
    public String getDisplayName() {
        return "PayPal (" + email + ")";
    }
}
