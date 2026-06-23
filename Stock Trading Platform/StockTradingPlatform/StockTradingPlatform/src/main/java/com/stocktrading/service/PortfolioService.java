package com.stocktrading.service;

import com.stocktrading.dao.PortfolioDAO;
import com.stocktrading.dao.TransactionDAO;
import com.stocktrading.exception.InvalidStockException;
import com.stocktrading.model.Portfolio;
import com.stocktrading.model.Stock;
import com.stocktrading.model.Transaction;
import com.stocktrading.model.User;

import java.util.List;

/**
 * Service layer for portfolio display and performance reporting.
 * Fetches portfolio rows from the DB, enriches them with live
 * stock prices from the Market, and renders formatted summaries.
 *
 * Demonstrates Composition (holds Market, PortfolioDAO, TransactionDAO).
 */
public class PortfolioService {

    // ── Composition ───────────────────────────────────────────
    private final Market         market;
    private final PortfolioDAO   portfolioDAO;
    private final TransactionDAO transactionDAO;

    // ── Constructor ───────────────────────────────────────────

    public PortfolioService(Market market,
                            PortfolioDAO portfolioDAO,
                            TransactionDAO transactionDAO) {
        this.market         = market;
        this.portfolioDAO   = portfolioDAO;
        this.transactionDAO = transactionDAO;
    }

    // ── Portfolio Display ─────────────────────────────────────

    /**
     * Prints a complete portfolio summary for the given user.
     * Enriches each Portfolio entry with the live Stock reference so
     * current market value and P&L can be computed.
     *
     * @param user the user whose portfolio is to be displayed
     */
    public void displayPortfolio(User user) {
        List<Portfolio> entries = portfolioDAO.getPortfolioByUser(user.getUserId());

        String border = "=".repeat(100);
        System.out.println("\n" + border);
        System.out.println("  PORTFOLIO — " + user.getName()
                + "  |  Available Balance: Rs." + String.format("%.2f", user.getBalance()));
        System.out.println(border);

        if (entries.isEmpty()) {
            System.out.println("  No stocks in portfolio. Start by buying stocks from the market!");
            System.out.println(border);
            return;
        }

        // Column header
        System.out.printf("%-10s | %-30s | %6s | %14s | %14s | %s%n",
                "SYMBOL", "COMPANY", "QTY", "INVESTED (Rs.)", "MARKET VAL (Rs.)", "P&L");
        System.out.println("-".repeat(100));

        double totalInvested     = 0;
        double totalMarketValue  = 0;

        for (Portfolio p : entries) {
            // Enrich with live stock data (Composition in action)
            try {
                Stock stock = market.getStock(p.getStockSymbol());
                p.setStock(stock);
            } catch (InvalidStockException e) {
                // Stock was delisted; show with zero current value
            }

            double currentVal = p.getCurrentMarketValue();
            double pnl        = p.getProfitOrLoss();
            String pnlStr     = String.format("%sRs.%.2f (%.1f%%)",
                    pnl >= 0 ? "+" : "", pnl, p.getProfitOrLossPercent());

            String companyName = (p.getStock() != null)
                    ? p.getStock().getCompanyName() : "N/A";

            System.out.printf("%-10s | %-30s | %6d | %14.2f | %14.2f | %s%n",
                    p.getStockSymbol(), companyName,
                    p.getQuantity(), p.getPurchaseValue(),
                    currentVal, pnlStr);

            totalInvested    += p.getPurchaseValue();
            totalMarketValue += currentVal;
        }

        double totalPnL        = totalMarketValue - totalInvested;
        double totalPnLPercent = totalInvested > 0
                ? (totalPnL / totalInvested) * 100.0 : 0.0;
        double netWorth        = user.getBalance() + totalMarketValue;

        System.out.println("=".repeat(100));
        System.out.printf("  %-42s Rs.%14.2f%n", "Total Invested:", totalInvested);
        System.out.printf("  %-42s Rs.%14.2f%n", "Current Portfolio Value:", totalMarketValue);
        System.out.printf("  %-42s %sRs.%.2f  (%.2f%%)%n",
                "Overall P&L:",
                totalPnL >= 0 ? "+" : "", totalPnL, totalPnLPercent);
        System.out.printf("  %-42s Rs.%14.2f%n", "Available Cash Balance:", user.getBalance());
        System.out.printf("  %-42s Rs.%14.2f%n", "Total Net Worth (Cash + Portfolio):", netWorth);
        System.out.println("=".repeat(100));
    }

    // ── Transaction History ───────────────────────────────────

    /**
     * Prints the complete transaction history for a user.
     *
     * @param user the user whose transactions are to be displayed
     */
    public void displayTransactionHistory(User user) {
        List<Transaction> txns = transactionDAO.getTransactionsByUser(user.getUserId());

        String border = "=".repeat(105);
        System.out.println("\n" + border);
        System.out.println("  TRANSACTION HISTORY — " + user.getName());
        System.out.println(border);

        if (txns.isEmpty()) {
            System.out.println("  No transactions found.");
            System.out.println(border);
            return;
        }

        System.out.printf("%-8s | %-4s | %-10s | %5s | %12s | %14s | %s%n",
                "TXN ID", "TYPE", "SYMBOL", "QTY", "PRICE (Rs.)", "TOTAL (Rs.)", "DATE & TIME");
        System.out.println("-".repeat(105));

        for (Transaction t : txns) {
            System.out.println(t.toDisplayString());
        }

        System.out.println(border);
        System.out.println("  Total transactions: " + txns.size());
        System.out.println(border);
    }

    // ── Overloaded Performance Summary (Method Overloading) ──

    /**
     * Returns a quick text snapshot of portfolio performance.
     */
    public String getPerformanceSummary(User user) {
        List<Portfolio> entries = portfolioDAO.getPortfolioByUser(user.getUserId());
        double totalInvested   = 0;
        double totalMarketVal  = 0;

        for (Portfolio p : entries) {
            try {
                p.setStock(market.getStock(p.getStockSymbol()));
            } catch (InvalidStockException ignored) {}
            totalInvested  += p.getPurchaseValue();
            totalMarketVal += p.getCurrentMarketValue();
        }

        double pnl     = totalMarketVal - totalInvested;
        double pnlPct  = totalInvested > 0 ? (pnl / totalInvested) * 100.0 : 0.0;

        return String.format(
                "Holdings: %d stocks | Invested: Rs.%.2f | Market Value: Rs.%.2f | P&L: %sRs.%.2f (%.2f%%)",
                entries.size(), totalInvested, totalMarketVal,
                pnl >= 0 ? "+" : "", pnl, pnlPct);
    }

    /**
     * Overloaded: Returns performance summary with a boolean flag to
     * include cash balance in the printed output.
     */
    public String getPerformanceSummary(User user, boolean includeCash) {
        String base = getPerformanceSummary(user);
        if (includeCash) {
            base += String.format(" | Cash: Rs.%.2f", user.getBalance());
        }
        return base;
    }
}
