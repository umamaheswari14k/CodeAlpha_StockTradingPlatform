package com.stocktrading.exception;

/**
 * Thrown when a user attempts to sell more shares than they own.
 */
public class InsufficientSharesException extends Exception {

    private final int requestedQuantity;
    private final int availableQuantity;

    public InsufficientSharesException(int requestedQuantity, int availableQuantity,
                                       String stockSymbol) {
        super(String.format(
                "Insufficient shares for %s! Requested: %d  Available: %d",
                stockSymbol, requestedQuantity, availableQuantity));
        this.requestedQuantity = requestedQuantity;
        this.availableQuantity = availableQuantity;
    }

    public InsufficientSharesException(String message) {
        super(message);
        this.requestedQuantity = 0;
        this.availableQuantity = 0;
    }

    public int getRequestedQuantity() { return requestedQuantity; }
    public int getAvailableQuantity() { return availableQuantity; }
}
