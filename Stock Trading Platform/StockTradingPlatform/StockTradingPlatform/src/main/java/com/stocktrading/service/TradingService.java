package com.stocktrading.service;

import com.stocktrading.dao.PortfolioDAO;
import com.stocktrading.dao.TransactionDAO;
import com.stocktrading.dao.UserDAO;
import com.stocktrading.exception.InsufficientBalanceException;
import com.stocktrading.exception.InsufficientSharesException;
import com.stocktrading.exception.InvalidStockException;
import com.stocktrading.model.Portfolio;
import com.stocktrading.model.Stock;
import com.stocktrading.model.Transaction;
import com.stocktrading.model.User;

/**
 * Core trading engine.
 * Handles BUY and SELL operations, validates all business rules,
 * records transactions, and keeps the portfolio and user balance
 * in sync both in memory and in the database.
 *
 * Demonstrates Composition (holds DAOs and Market reference).
 */
public class TradingService {

    // ── Composition ───────────────────────────────────────────
    private final Market         market;
    private final UserDAO        userDAO;
    private final PortfolioDAO   portfolioDAO;
    private final TransactionDAO transactionDAO;

    // ── Constructor ───────────────────────────────────────────

    public TradingService(Market market,
                          UserDAO userDAO,
                          PortfolioDAO portfolioDAO,
                          TransactionDAO transactionDAO) {
        this.market         = market;
        this.userDAO        = userDAO;
        this.portfolioDAO   = portfolioDAO;
        this.transactionDAO = transactionDAO;
    }

    // ── BUY ───────────────────────────────────────────────────

    /**
     * Executes a stock purchase for the given user.
     *
     * Steps:
     *  1. Validate stock symbol.
     *  2. Calculate total cost.
     *  3. Check user balance (throws InsufficientBalanceException).
     *  4. Deduct balance, update portfolio, record transaction — all in DB.
     *
     * @param user     the buyer (object is updated in place)
     * @param symbol   the stock symbol to buy
     * @param quantity number of shares to purchase (must be > 0)
     * @throws InvalidStockException        if symbol does not exist
     * @throws InsufficientBalanceException if user cannot afford the purchase
     * @throws IllegalArgumentException     if quantity ≤ 0
     */
    public void buyStock(User user, String symbol, int quantity)
            throws InvalidStockException, InsufficientBalanceException {

        if (quantity <= 0) {
            throw new IllegalArgumentException("Quantity must be a positive integer.");
        }

        // 1. Validate stock
        Stock stock     = market.getStock(symbol);
        double unitPrice = stock.getCurrentPrice();
        double totalCost = unitPrice * quantity;

        // 2. Check balance
        if (!user.canAfford(totalCost)) {
            throw new InsufficientBalanceException(totalCost, user.getBalance());
        }

        // 3. Deduct balance
        user.deductBalance(totalCost);
        userDAO.updateBalance(user.getUserId(), user.getBalance());

        // 4. Update portfolio
        Portfolio existing = portfolioDAO.getPortfolioEntry(user.getUserId(), symbol);
        if (existing == null) {
            // First time buying this stock
            Portfolio newEntry = new Portfolio(
                    user.getUserId(), symbol.toUpperCase(), quantity, totalCost);
            portfolioDAO.insertPortfolio(newEntry);
        } else {
            // Already owns some — add to existing holding
            int    newQty   = existing.getQuantity()      + quantity;
            double newValue = existing.getPurchaseValue() + totalCost;
            portfolioDAO.updatePortfolio(user.getUserId(), symbol, newQty, newValue);
        }

        // 5. Record transaction
        Transaction txn = new Transaction(
                user.getUserId(), symbol.toUpperCase(),
                Transaction.BUY, quantity, unitPrice);
        transactionDAO.insertTransaction(txn);

        // 6. Console feedback
        System.out.println("\n✅  BUY ORDER EXECUTED");
        System.out.println("    Stock    : " + symbol.toUpperCase()
                + " – " + stock.getCompanyName());
        System.out.printf ("    Shares   : %d  @  Rs.%.2f each%n", quantity, unitPrice);
        System.out.printf ("    Total    : Rs.%.2f%n", totalCost);
        System.out.printf ("    Remaining Balance: Rs.%.2f%n", user.getBalance());
    }

    // ── SELL ──────────────────────────────────────────────────

    /**
     * Executes a stock sale for the given user.
     *
     * Steps:
     *  1. Validate stock symbol.
     *  2. Check the user owns enough shares (throws InsufficientSharesException).
     *  3. Credit balance, update or remove portfolio entry, record transaction.
     *
     * @param user     the seller (object is updated in place)
     * @param symbol   the stock symbol to sell
     * @param quantity number of shares to sell (must be > 0)
     * @throws InvalidStockException         if symbol does not exist
     * @throws InsufficientSharesException   if user does not own enough shares
     * @throws IllegalArgumentException      if quantity ≤ 0
     */
    public void sellStock(User user, String symbol, int quantity)
            throws InvalidStockException, InsufficientSharesException {

        if (quantity <= 0) {
            throw new IllegalArgumentException("Quantity must be a positive integer.");
        }

        // 1. Validate stock
        Stock stock     = market.getStock(symbol);
        double unitPrice = stock.getCurrentPrice();
        double proceeds  = unitPrice * quantity;

        // 2. Check ownership
        Portfolio existing = portfolioDAO.getPortfolioEntry(user.getUserId(), symbol);
        if (existing == null || existing.getQuantity() < quantity) {
            int owned = (existing == null) ? 0 : existing.getQuantity();
            throw new InsufficientSharesException(quantity, owned, symbol.toUpperCase());
        }

        // 3. Credit balance
        user.addBalance(proceeds);
        userDAO.updateBalance(user.getUserId(), user.getBalance());

        // 4. Update / remove portfolio entry
        int remainingQty = existing.getQuantity() - quantity;
        if (remainingQty == 0) {
            portfolioDAO.deletePortfolioEntry(user.getUserId(), symbol);
        } else {
            // Proportionally reduce purchase_value
            double soldValueProportion =
                    (existing.getPurchaseValue() / existing.getQuantity()) * quantity;
            double newPurchaseValue = existing.getPurchaseValue() - soldValueProportion;
            portfolioDAO.updatePortfolio(
                    user.getUserId(), symbol, remainingQty, newPurchaseValue);
        }

        // 5. Record transaction
        Transaction txn = new Transaction(
                user.getUserId(), symbol.toUpperCase(),
                Transaction.SELL, quantity, unitPrice);
        transactionDAO.insertTransaction(txn);

        // 6. Calculate realised P&L for display
        double avgBuyPrice   = existing.getPurchaseValue() / existing.getQuantity();
        double realisedPnL   = (unitPrice - avgBuyPrice) * quantity;

        // 7. Console feedback
        System.out.println("\n✅  SELL ORDER EXECUTED");
        System.out.println("    Stock    : " + symbol.toUpperCase()
                + " – " + stock.getCompanyName());
        System.out.printf ("    Shares   : %d  @  Rs.%.2f each%n", quantity, unitPrice);
        System.out.printf ("    Proceeds : Rs.%.2f%n", proceeds);
        System.out.printf ("    Realised P&L: %sRs.%.2f%n",
                realisedPnL >= 0 ? "+" : "", realisedPnL);
        System.out.printf ("    New Balance: Rs.%.2f%n", user.getBalance());
    }

    // ── Overloaded convenience wrappers (Method Overloading) ─

    /**
     * Buy using the user's ID instead of a User object.
     * Fetches the user from the DB, delegates to the main buyStock method.
     */
    public void buyStock(int userId, String symbol, int quantity)
            throws InvalidStockException, InsufficientBalanceException {
        User user = userDAO.getUserById(userId);
        if (user == null) throw new IllegalArgumentException("User not found: " + userId);
        buyStock(user, symbol, quantity);
    }

    /**
     * Sell using the user's ID instead of a User object.
     */
    public void sellStock(int userId, String symbol, int quantity)
            throws InvalidStockException, InsufficientSharesException {
        User user = userDAO.getUserById(userId);
        if (user == null) throw new IllegalArgumentException("User not found: " + userId);
        sellStock(user, symbol, quantity);
    }
}
