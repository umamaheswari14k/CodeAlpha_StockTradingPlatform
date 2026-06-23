package com.stocktrading.model;

/**
 * Represents a registered user of the trading platform.
 * Demonstrates Encapsulation.
 */
public class User {

    // ── Fields ──────────────────────────────────────────────
    private int    userId;
    private String name;
    private double balance;

    // ── Constructors ─────────────────────────────────────────

    /** Constructor used when fetching an existing user from the DB. */
    public User(int userId, String name, double balance) {
        this.userId  = userId;
        this.name    = name;
        this.balance = balance;
    }

    /** Constructor used when creating a new user (ID assigned by DB). */
    public User(String name, double balance) {
        this.name    = name;
        this.balance = balance;
    }

    /** Default constructor. */
    public User() {}

    // ── Getters & Setters ────────────────────────────────────

    public int    getUserId()  { return userId; }
    public void   setUserId(int userId)   { this.userId = userId; }

    public String getName()    { return name; }
    public void   setName(String name)    { this.name = name; }

    public double getBalance() { return balance; }
    public void   setBalance(double balance) { this.balance = balance; }

    // ── Business Methods ─────────────────────────────────────

    /**
     * Deducts amount from the user's balance.
     * @param amount the amount to deduct
     */
    public void deductBalance(double amount) {
        this.balance -= amount;
        this.balance  = Math.round(this.balance * 100.0) / 100.0;
    }

    /**
     * Adds amount to the user's balance.
     * @param amount the amount to add
     */
    public void addBalance(double amount) {
        this.balance += amount;
        this.balance  = Math.round(this.balance * 100.0) / 100.0;
    }

    /**
     * Checks whether the user can afford a given cost.
     * @param cost the total cost to check against balance
     * @return true if balance is sufficient
     */
    public boolean canAfford(double cost) {
        return this.balance >= cost;
    }

    // ── Overloaded display helpers (Method Overloading) ──────

    /** Short summary line. */
    public String toDisplayString() {
        return String.format("ID: %-5d | Name: %-20s | Balance: Rs.%.2f",
                userId, name, balance);
    }

    /** Detailed display with a custom label. */
    public String toDisplayString(String label) {
        return String.format("[%s]  ID: %d  Name: %s  Balance: Rs.%.2f",
                label, userId, name, balance);
    }

    @Override
    public String toString() {
        return "User{id=" + userId + ", name='" + name + "', balance=" + balance + "}";
    }
}
