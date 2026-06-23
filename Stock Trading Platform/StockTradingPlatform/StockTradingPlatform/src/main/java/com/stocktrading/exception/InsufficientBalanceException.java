package com.stocktrading.exception;

/**
 * Thrown when a user attempts to buy stocks without sufficient balance.
 */
public class InsufficientBalanceException extends Exception {

    private final double requiredAmount;
    private final double availableBalance;

    public InsufficientBalanceException(double requiredAmount, double availableBalance) {
        super(String.format(
                "Insufficient balance! Required: Rs.%.2f  Available: Rs.%.2f  Shortfall: Rs.%.2f",
                requiredAmount, availableBalance, (requiredAmount - availableBalance)));
        this.requiredAmount   = requiredAmount;
        this.availableBalance = availableBalance;
    }

    public InsufficientBalanceException(String message) {
        super(message);
        this.requiredAmount   = 0;
        this.availableBalance = 0;
    }

    public double getRequiredAmount()   { return requiredAmount; }
    public double getAvailableBalance() { return availableBalance; }
}
