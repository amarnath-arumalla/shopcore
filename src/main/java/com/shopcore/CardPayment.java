package com.shopcore;

public class CardPayment implements PaymentMethod {

    private final String lastFourDigits;
    private Money availableCredit;
    private int chargeCount = 0;

    public CardPayment(String cardNumber, Money creditLimit) {
        if (cardNumber == null || !cardNumber.matches("\\d{12,19}")) {
            throw new IllegalArgumentException("Card number must be 12 to 19 digits");
        }
        if (creditLimit == null || creditLimit.isZero()) {
            throw new IllegalArgumentException("Credit limit must be greater than zero");
        }
        this.lastFourDigits = cardNumber.substring(cardNumber.length() - 4);
        this.availableCredit = creditLimit;
    }

    @Override
    public PaymentResult charge(Money amount) {
        if (amount == null || amount.isZero()) {
            throw new IllegalArgumentException("Amount must be greater than zero");
        }
        if (amount.isGreaterThan(availableCredit)) {
            return PaymentResult.declined("Card declined: over credit limit");
        }
        availableCredit = availableCredit.minus(amount);
        chargeCount = chargeCount + 1;
        return PaymentResult.approved("CARD-" + lastFourDigits + "-" + chargeCount);
    }

    @Override
    public String getDisplayName() {
        return "Card ending " + lastFourDigits;
    }
}
