package com.stocktrading.model;

/**
 * Represents a single portfolio entry (one user owning shares of one stock).
 * Demonstrates Encapsulation and Composition (references Stock).
 */
public class Portfolio {

    // ── Fields ──────────────────────────────────────────────
    private int    portfolioId;
    private int    userId;
    private String stockSymbol;
    private int    quantity;
    private double purchaseValue;    // total amount spent buying these shares

    // ── Composition: reference to the live Stock object ──────
    private Stock stock;             // injected at runtime for current price lookup

    // ── Constructors ─────────────────────────────────────────

    /** Full constructor used when loading an existing portfolio row. */
    public Portfolio(int portfolioId, int userId, String stockSymbol,
                     int quantity, double purchaseValue) {
        this.portfolioId  = portfolioId;
        this.userId       = userId;
        this.stockSymbol  = stockSymbol;
        this.quantity     = quantity;
        this.purchaseValue = purchaseValue;
    }

    /** Constructor for creating a new portfolio entry (ID assigned by DB). */
    public Portfolio(int userId, String stockSymbol, int quantity, double purchaseValue) {
        this.userId        = userId;
        this.stockSymbol   = stockSymbol;
        this.quantity      = quantity;
        this.purchaseValue = purchaseValue;
    }

    /** Default constructor. */
    public Portfolio() {}

    // ── Getters & Setters ────────────────────────────────────

    public int    getPortfolioId()  { return portfolioId; }
    public void   setPortfolioId(int portfolioId) { this.portfolioId = portfolioId; }

    public int    getUserId()       { return userId; }
    public void   setUserId(int userId) { this.userId = userId; }

    public String getStockSymbol()  { return stockSymbol; }
    public void   setStockSymbol(String stockSymbol) { this.stockSymbol = stockSymbol; }

    public int    getQuantity()     { return quantity; }
    public void   setQuantity(int quantity) { this.quantity = quantity; }

    public double getPurchaseValue() { return purchaseValue; }
    public void   setPurchaseValue(double purchaseValue) { this.purchaseValue = purchaseValue; }

    public Stock  getStock()        { return stock; }
    public void   setStock(Stock stock) { this.stock = stock; }

    // ── Derived / Business Methods ───────────────────────────

    /**
     * Returns the average price paid per share.
     */
    public double getAverageBuyPrice() {
        return quantity > 0 ? purchaseValue / quantity : 0.0;
    }

    /**
     * Returns the current market value of held shares.
     * Requires the stock reference to be set.
     */
    public double getCurrentMarketValue() {
        if (stock == null) return 0.0;
        return stock.getCurrentPrice() * quantity;
    }

    /**
     * Returns profit (positive) or loss (negative) on this holding.
     */
    public double getProfitOrLoss() {
        return getCurrentMarketValue() - purchaseValue;
    }

    /**
     * Returns the profit/loss expressed as a percentage of the purchase value.
     */
    public double getProfitOrLossPercent() {
        if (purchaseValue == 0) return 0.0;
        return (getProfitOrLoss() / purchaseValue) * 100.0;
    }

    // ── Overloaded display helpers (Method Overloading) ──────

    /** Compact single-line summary. */
    public String toDisplayString() {
        double currentValue = getCurrentMarketValue();
        double pnl          = getProfitOrLoss();
        String pnlStr       = String.format("%sRs.%.2f (%.2f%%)",
                pnl >= 0 ? "+" : "", pnl, getProfitOrLossPercent());

        return String.format("%-10s | Qty: %4d | Invested: Rs.%10.2f | Market: Rs.%10.2f | P&L: %s",
                stockSymbol, quantity, purchaseValue, currentValue, pnlStr);
    }

    /** Detailed display including company name from composed Stock. */
    public String toDisplayString(boolean showCompanyName) {
        String company = (showCompanyName && stock != null) ? stock.getCompanyName() : stockSymbol;
        double currentValue = getCurrentMarketValue();
        double pnl          = getProfitOrLoss();
        String pnlStr       = String.format("%sRs.%.2f", pnl >= 0 ? "+" : "", pnl);

        return String.format("%-10s | %-30s | Qty: %4d | Invested: Rs.%10.2f | Market: Rs.%10.2f | P&L: %s",
                stockSymbol, company, quantity, purchaseValue, currentValue, pnlStr);
    }

    @Override
    public String toString() {
        return "Portfolio{userId=" + userId + ", symbol='" + stockSymbol
                + "', qty=" + quantity + ", purchaseValue=" + purchaseValue + "}";
    }
}
