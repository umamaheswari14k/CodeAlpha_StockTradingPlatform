package com.stocktrading.exception;

/**
 * Thrown when a given stock symbol does not exist in the market or database.
 */
public class InvalidStockException extends Exception {

    private final String stockSymbol;

    public InvalidStockException(String stockSymbol) {
        super("Invalid stock symbol: '" + stockSymbol
                + "'. Please check the market listing and try again.");
        this.stockSymbol = stockSymbol;
    }

    public InvalidStockException(String stockSymbol, String message) {
        super(message);
        this.stockSymbol = stockSymbol;
    }

    public String getStockSymbol() { return stockSymbol; }
}
