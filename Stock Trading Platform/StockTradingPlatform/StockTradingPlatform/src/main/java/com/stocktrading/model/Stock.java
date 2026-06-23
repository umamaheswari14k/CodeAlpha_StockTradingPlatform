package com.stocktrading.model;

/**
 * Represents a stock listed on the market.
 * Demonstrates Encapsulation via private fields and public getters/setters.
 */
public class Stock {

    // ── Fields ──────────────────────────────────────────────
    private String stockSymbol;
    private String companyName;
    private double currentPrice;

    // ── Constructors ─────────────────────────────────────────

    /** Full constructor used when loading from DB or creating a new stock. */
    public Stock(String stockSymbol, String companyName, double currentPrice) {
        this.stockSymbol  = stockSymbol;
        this.companyName  = companyName;
        this.currentPrice = currentPrice;
    }

    /** Default constructor required for DAO instantiation. */
    public Stock() {}

    // ── Getters & Setters ────────────────────────────────────

    public String getStockSymbol()  { return stockSymbol; }
    public void   setStockSymbol(String stockSymbol)  { this.stockSymbol = stockSymbol; }

    public String getCompanyName()  { return companyName; }
    public void   setCompanyName(String companyName)  { this.companyName = companyName; }

    public double getCurrentPrice() { return currentPrice; }
    public void   setCurrentPrice(double currentPrice) { this.currentPrice = currentPrice; }

    // ── Business Methods ─────────────────────────────────────

    /**
     * Simulates a random market price fluctuation of ±5%.
     */
    public void simulatePriceChange() {
        double changePercent = (Math.random() * 10) - 5;   // -5% to +5%
        double change        = currentPrice * (changePercent / 100.0);
        currentPrice         = Math.max(1.0, currentPrice + change);
        currentPrice         = Math.round(currentPrice * 100.0) / 100.0;
    }

    // ── Overloaded display helpers (Method Overloading) ──────

    /** Returns a single-line summary string. */
    public String toDisplayString() {
        return String.format("%-10s | %-35s | Rs.%10.2f",
                stockSymbol, companyName, currentPrice);
    }

    /** Returns a formatted string with an optional price-change label. */
    public String toDisplayString(double previousPrice) {
        double diff    = currentPrice - previousPrice;
        String arrow   = diff >= 0 ? "▲" : "▼";
        String diffStr = String.format("%s %.2f", arrow, Math.abs(diff));
        return String.format("%-10s | %-35s | Rs.%10.2f  (%s)",
                stockSymbol, companyName, currentPrice, diffStr);
    }

    @Override
    public String toString() {
        return "Stock{symbol='" + stockSymbol + "', company='" + companyName
                + "', price=" + currentPrice + "}";
    }
}
