package com.stocktrading.service;

import com.stocktrading.dao.StockDAO;
import com.stocktrading.exception.InvalidStockException;
import com.stocktrading.model.Stock;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Represents the stock market.
 * Holds all available stocks in memory (HashMap for O(1) lookup)
 * and coordinates with StockDAO to persist price changes.
 *
 * Demonstrates Composition (contains StockDAO) and use of
 * ArrayList + HashMap from the Collections Framework.
 */
public class Market {

    // ── Collections ───────────────────────────────────────────
    /** Fast symbol-based lookup. */
    private final Map<String, Stock>  stockMap  = new HashMap<>();
    /** Ordered list for display purposes. */
    private final List<Stock>         stockList = new ArrayList<>();

    // ── Composition ───────────────────────────────────────────
    private final StockDAO stockDAO;

    // ── Constructor ───────────────────────────────────────────

    public Market(StockDAO stockDAO) {
        this.stockDAO = stockDAO;
        loadStocksFromDB();
    }

    // ── Initialisation ────────────────────────────────────────

    /**
     * Loads all stocks from the database into the in-memory collections.
     */
    private void loadStocksFromDB() {
        stockMap.clear();
        stockList.clear();
        List<Stock> dbStocks = stockDAO.getAllStocks();
        for (Stock s : dbStocks) {
            stockMap.put(s.getStockSymbol(), s);
            stockList.add(s);
        }
        System.out.println("[Market] Loaded " + stockList.size() + " stocks from database.");
    }

    // ── Public API ────────────────────────────────────────────

    /**
     * Returns a stock by its symbol.
     *
     * @param symbol the stock symbol (case-insensitive)
     * @throws InvalidStockException if the symbol is not listed
     */
    public Stock getStock(String symbol) throws InvalidStockException {
        Stock s = stockMap.get(symbol.toUpperCase());
        if (s == null) {
            throw new InvalidStockException(symbol.toUpperCase());
        }
        return s;
    }

    /**
     * Checks whether a symbol is listed without throwing an exception.
     */
    public boolean stockExists(String symbol) {
        return stockMap.containsKey(symbol.toUpperCase());
    }

    /**
     * Returns an unmodifiable view of all stocks (for display).
     */
    public List<Stock> getAllStocks() {
        return new ArrayList<>(stockList);
    }

    /**
     * Simulates market-wide random price changes (±5% per stock),
     * then persists all updated prices to the database.
     */
    public void refreshAllPrices() {
        System.out.println("\n[Market] Refreshing all stock prices...");
        for (Stock s : stockList) {
            double oldPrice = s.getCurrentPrice();
            s.simulatePriceChange();
            stockDAO.updateStockPrice(s.getStockSymbol(), s.getCurrentPrice());
            System.out.printf("  %-10s  Rs.%.2f  →  Rs.%.2f%n",
                    s.getStockSymbol(), oldPrice, s.getCurrentPrice());
        }
        System.out.println("[Market] Prices updated.\n");
    }

    /**
     * Refreshes the price of a single stock and persists it.
     */
    public void refreshPrice(String symbol) throws InvalidStockException {
        Stock s = getStock(symbol);
        s.simulatePriceChange();
        stockDAO.updateStockPrice(s.getStockSymbol(), s.getCurrentPrice());
    }

    /**
     * Displays the full market listing to the console.
     */
    public void displayMarket() {
        String line = "=".repeat(70);
        System.out.println(line);
        System.out.printf("%-10s | %-35s | %s%n", "SYMBOL", "COMPANY", "PRICE (Rs.)");
        System.out.println(line);
        for (Stock s : stockList) {
            System.out.println(s.toDisplayString());
        }
        System.out.println(line);
        System.out.println("  Total stocks listed: " + stockList.size());
        System.out.println(line);
    }

    /** Returns how many stocks are listed. */
    public int getStockCount() {
        return stockList.size();
    }
}
