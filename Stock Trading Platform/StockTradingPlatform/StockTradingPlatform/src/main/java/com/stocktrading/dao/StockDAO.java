package com.stocktrading.dao;

import com.stocktrading.model.Stock;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Data Access Object for the {@code stocks} table.
 */
public class StockDAO {

    // ── INSERT ────────────────────────────────────────────────

    /**
     * Inserts a brand-new stock into the database.
     *
     * @param stock the stock to insert
     * @return true if the row was inserted successfully
     */
    public boolean insertStock(Stock stock) {
        String sql = "INSERT INTO stocks (stock_symbol, company_name, current_price) VALUES (?, ?, ?)";
        try (PreparedStatement ps = DatabaseConnection.getConnection()
                .prepareStatement(sql)) {

            ps.setString(1, stock.getStockSymbol());
            ps.setString(2, stock.getCompanyName());
            ps.setDouble(3, stock.getCurrentPrice());
            return ps.executeUpdate() == 1;

        } catch (SQLException e) {
            System.err.println("[StockDAO] insertStock error: " + e.getMessage());
        }
        return false;
    }

    // ── SELECT ────────────────────────────────────────────────

    /**
     * Fetches a single stock by its symbol.
     *
     * @param symbol the stock symbol (e.g., "AAPL")
     * @return the {@link Stock} or {@code null} if not found
     */
    public Stock getStockBySymbol(String symbol) {
        String sql = "SELECT * FROM stocks WHERE stock_symbol = ?";
        try (PreparedStatement ps = DatabaseConnection.getConnection()
                .prepareStatement(sql)) {

            ps.setString(1, symbol.toUpperCase());
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return mapRow(rs);
                }
            }
        } catch (SQLException e) {
            System.err.println("[StockDAO] getStockBySymbol error: " + e.getMessage());
        }
        return null;
    }

    /**
     * Returns all stocks listed on the platform.
     */
    public List<Stock> getAllStocks() {
        List<Stock> stocks = new ArrayList<>();
        String sql = "SELECT * FROM stocks ORDER BY stock_symbol";
        try (Statement stmt = DatabaseConnection.getConnection().createStatement();
             ResultSet rs   = stmt.executeQuery(sql)) {

            while (rs.next()) {
                stocks.add(mapRow(rs));
            }
        } catch (SQLException e) {
            System.err.println("[StockDAO] getAllStocks error: " + e.getMessage());
        }
        return stocks;
    }

    // ── UPDATE ────────────────────────────────────────────────

    /**
     * Updates the current price of a stock in the database.
     *
     * @param symbol   the stock symbol
     * @param newPrice the new market price
     * @return true if the row was updated
     */
    public boolean updateStockPrice(String symbol, double newPrice) {
        String sql = "UPDATE stocks SET current_price = ? WHERE stock_symbol = ?";
        try (PreparedStatement ps = DatabaseConnection.getConnection()
                .prepareStatement(sql)) {

            ps.setDouble(1, Math.round(newPrice * 100.0) / 100.0);
            ps.setString(2, symbol.toUpperCase());
            return ps.executeUpdate() == 1;

        } catch (SQLException e) {
            System.err.println("[StockDAO] updateStockPrice error: " + e.getMessage());
        }
        return false;
    }

    /**
     * Saves all stock objects' current prices back to the database.
     * Used after a market-wide price refresh.
     *
     * @param stocks list of stocks with updated prices
     */
    public void updateAllPrices(List<Stock> stocks) {
        for (Stock s : stocks) {
            updateStockPrice(s.getStockSymbol(), s.getCurrentPrice());
        }
    }

    // ── DELETE ────────────────────────────────────────────────

    /**
     * Removes a stock from the platform (admin use).
     */
    public boolean deleteStock(String symbol) {
        String sql = "DELETE FROM stocks WHERE stock_symbol = ?";
        try (PreparedStatement ps = DatabaseConnection.getConnection()
                .prepareStatement(sql)) {

            ps.setString(1, symbol.toUpperCase());
            return ps.executeUpdate() == 1;

        } catch (SQLException e) {
            System.err.println("[StockDAO] deleteStock error: " + e.getMessage());
        }
        return false;
    }

    // ── Helper ───────────────────────────────────────────────

    private Stock mapRow(ResultSet rs) throws SQLException {
        return new Stock(
                rs.getString("stock_symbol"),
                rs.getString("company_name"),
                rs.getDouble("current_price")
        );
    }
}
