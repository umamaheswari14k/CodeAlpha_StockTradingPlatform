package com.stocktrading.model;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * Represents a single buy or sell transaction.
 * Demonstrates Encapsulation.
 */
public class Transaction {

    // ── Constants ────────────────────────────────────────────
    public static final String BUY  = "BUY";
    public static final String SELL = "SELL";

    private static final DateTimeFormatter FORMATTER =
            DateTimeFormatter.ofPattern("dd-MM-yyyy HH:mm:ss");

    // ── Fields ──────────────────────────────────────────────
    private int           transactionId;
    private int           userId;
    private String        stockSymbol;
    private String        transactionType;   // "BUY" or "SELL"
    private int           quantity;
    private double        price;             // price per share at transaction time
    private LocalDateTime transactionDate;

    // ── Constructors ─────────────────────────────────────────

    /** Full constructor used when loading from DB. */
    public Transaction(int transactionId, int userId, String stockSymbol,
                       String transactionType, int quantity,
                       double price, LocalDateTime transactionDate) {
        this.transactionId   = transactionId;
        this.userId          = userId;
        this.stockSymbol     = stockSymbol;
        this.transactionType = transactionType;
        this.quantity        = quantity;
        this.price           = price;
        this.transactionDate = transactionDate;
    }

    /** Constructor for creating a new transaction (ID assigned by DB). */
    public Transaction(int userId, String stockSymbol,
                       String transactionType, int quantity, double price) {
        this.userId          = userId;
        this.stockSymbol     = stockSymbol;
        this.transactionType = transactionType;
        this.quantity        = quantity;
        this.price           = price;
        this.transactionDate = LocalDateTime.now();
    }

    /** Default constructor. */
    public Transaction() {}

    // ── Getters & Setters ────────────────────────────────────

    public int           getTransactionId()   { return transactionId; }
    public void          setTransactionId(int transactionId) { this.transactionId = transactionId; }

    public int           getUserId()          { return userId; }
    public void          setUserId(int userId) { this.userId = userId; }

    public String        getStockSymbol()     { return stockSymbol; }
    public void          setStockSymbol(String stockSymbol) { this.stockSymbol = stockSymbol; }

    public String        getTransactionType() { return transactionType; }
    public void          setTransactionType(String transactionType) { this.transactionType = transactionType; }

    public int           getQuantity()        { return quantity; }
    public void          setQuantity(int quantity) { this.quantity = quantity; }

    public double        getPrice()           { return price; }
    public void          setPrice(double price) { this.price = price; }

    public LocalDateTime getTransactionDate() { return transactionDate; }
    public void          setTransactionDate(LocalDateTime transactionDate) { this.transactionDate = transactionDate; }

    // ── Business Methods ─────────────────────────────────────

    /** Returns the total monetary value of this transaction. */
    public double getTotalValue() {
        return price * quantity;
    }

    // ── Overloaded display helpers (Method Overloading) ──────

    /** Compact one-liner. */
    public String toDisplayString() {
        String dateStr = transactionDate != null ? transactionDate.format(FORMATTER) : "N/A";
        return String.format("ID: %-6d | %-4s | Symbol: %-10s | Qty: %4d | Price: Rs.%10.2f | Total: Rs.%12.2f | %s",
                transactionId, transactionType, stockSymbol,
                quantity, price, getTotalValue(), dateStr);
    }

    /** Display with user ID included (useful for admin views). */
    public String toDisplayString(boolean includeUserId) {
        String base = toDisplayString();
        return includeUserId ? "UserID: " + userId + " | " + base : base;
    }

    @Override
    public String toString() {
        return "Transaction{id=" + transactionId + ", userId=" + userId
                + ", symbol='" + stockSymbol + "', type='" + transactionType
                + "', qty=" + quantity + ", price=" + price + "}";
    }
}
