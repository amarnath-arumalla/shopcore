package com.shopcore;

public class GiftCardPayment implements PaymentMethod {

    private final String code;
    private Money balance;

    public GiftCardPayment(String code, Money balance) {
        if (code == null || code.isBlank()) {
            throw new IllegalArgumentException("Gift card code is required");
        }
        if (balance == null) {
            throw new IllegalArgumentException("Balance is required");
        }
        this.code = code;
        this.balance = balance;
    }

    @Override
    public PaymentResult charge(Money amount) {
        if (amount == null || amount.isZero()) {
            throw new IllegalArgumentException("Amount must be greater than zero");
        }
        if (amount.isGreaterThan(balance)) {
            return PaymentResult.declined("Gift card has only " + balance + " left");
        }
        balance = balance.minus(amount);
        return PaymentResult.approved("GC-" + code);
    }

    @Override
    public String getDisplayName() {
        return "Gift card " + code;
    }

    public Money getBalance() {
        return balance;
    }
}
