package com.stocktrading.dao;

import com.stocktrading.model.Portfolio;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Data Access Object for the {@code portfolio} table.
 */
public class PortfolioDAO {

    // ── INSERT ────────────────────────────────────────────────

    /**
     * Inserts a new portfolio record (first purchase of a stock for a user).
     *
     * @param portfolio the portfolio entry to insert
     * @return the generated portfolio_id, or -1 on failure
     */
    public int insertPortfolio(Portfolio portfolio) {
        String sql = "INSERT INTO portfolio (user_id, stock_symbol, quantity, purchase_value) "
                   + "VALUES (?, ?, ?, ?)";
        try (PreparedStatement ps = DatabaseConnection.getConnection()
                .prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            ps.setInt(1, portfolio.getUserId());
            ps.setString(2, portfolio.getStockSymbol());
            ps.setInt(3, portfolio.getQuantity());
            ps.setDouble(4, portfolio.getPurchaseValue());
            ps.executeUpdate();

            try (ResultSet rs = ps.getGeneratedKeys()) {
                if (rs.next()) {
                    int id = rs.getInt(1);
                    portfolio.setPortfolioId(id);
                    return id;
                }
            }
        } catch (SQLException e) {
            System.err.println("[PortfolioDAO] insertPortfolio error: " + e.getMessage());
        }
        return -1;
    }

    // ── SELECT ────────────────────────────────────────────────

    /**
     * Finds a portfolio entry for a specific user and stock.
     *
     * @return the {@link Portfolio} or {@code null} if the user does not own this stock
     */
    public Portfolio getPortfolioEntry(int userId, String stockSymbol) {
        String sql = "SELECT * FROM portfolio WHERE user_id = ? AND stock_symbol = ?";
        try (PreparedStatement ps = DatabaseConnection.getConnection()
                .prepareStatement(sql)) {

            ps.setInt(1, userId);
            ps.setString(2, stockSymbol.toUpperCase());
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return mapRow(rs);
                }
            }
        } catch (SQLException e) {
            System.err.println("[PortfolioDAO] getPortfolioEntry error: " + e.getMessage());
        }
        return null;
    }

    /**
     * Returns all portfolio entries for a given user.
     */
    public List<Portfolio> getPortfolioByUser(int userId) {
        List<Portfolio> list = new ArrayList<>();
        String sql = "SELECT * FROM portfolio WHERE user_id = ? ORDER BY stock_symbol";
        try (PreparedStatement ps = DatabaseConnection.getConnection()
                .prepareStatement(sql)) {

            ps.setInt(1, userId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(mapRow(rs));
                }
            }
        } catch (SQLException e) {
            System.err.println("[PortfolioDAO] getPortfolioByUser error: " + e.getMessage());
        }
        return list;
    }

    // ── UPDATE ────────────────────────────────────────────────

    /**
     * Updates the quantity and purchase_value for an existing portfolio entry.
     * Called on every buy or partial sell.
     *
     * @param userId      the owner
     * @param stockSymbol the stock
     * @param newQty      new total quantity
     * @param newValue    new total purchase value
     * @return true if row was updated
     */
    public boolean updatePortfolio(int userId, String stockSymbol,
                                   int newQty, double newValue) {
        String sql = "UPDATE portfolio SET quantity = ?, purchase_value = ? "
                   + "WHERE user_id = ? AND stock_symbol = ?";
        try (PreparedStatement ps = DatabaseConnection.getConnection()
                .prepareStatement(sql)) {

            ps.setInt(1, newQty);
            ps.setDouble(2, Math.round(newValue * 100.0) / 100.0);
            ps.setInt(3, userId);
            ps.setString(4, stockSymbol.toUpperCase());
            return ps.executeUpdate() == 1;

        } catch (SQLException e) {
            System.err.println("[PortfolioDAO] updatePortfolio error: " + e.getMessage());
        }
        return false;
    }

    // ── DELETE ────────────────────────────────────────────────

    /**
     * Removes a portfolio entry when a user sells all their shares of a stock.
     */
    public boolean deletePortfolioEntry(int userId, String stockSymbol) {
        String sql = "DELETE FROM portfolio WHERE user_id = ? AND stock_symbol = ?";
        try (PreparedStatement ps = DatabaseConnection.getConnection()
                .prepareStatement(sql)) {

            ps.setInt(1, userId);
            ps.setString(2, stockSymbol.toUpperCase());
            return ps.executeUpdate() == 1;

        } catch (SQLException e) {
            System.err.println("[PortfolioDAO] deletePortfolioEntry error: " + e.getMessage());
        }
        return false;
    }

    // ── Helper ───────────────────────────────────────────────

    private Portfolio mapRow(ResultSet rs) throws SQLException {
        return new Portfolio(
                rs.getInt("portfolio_id"),
                rs.getInt("user_id"),
                rs.getString("stock_symbol"),
                rs.getInt("quantity"),
                rs.getDouble("purchase_value")
        );
    }
}
