package com.stocktrading.dao;

import com.stocktrading.model.Transaction;

import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Data Access Object for the {@code transactions} table.
 */
public class TransactionDAO {

    // ── INSERT ────────────────────────────────────────────────

    /**
     * Records a new buy or sell transaction.
     *
     * @param txn the transaction to record
     * @return the generated transaction_id, or -1 on failure
     */
    public int insertTransaction(Transaction txn) {
        String sql = "INSERT INTO transactions "
                   + "(user_id, stock_symbol, transaction_type, quantity, price) "
                   + "VALUES (?, ?, ?, ?, ?)";
        try (PreparedStatement ps = DatabaseConnection.getConnection()
                .prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            ps.setInt(1, txn.getUserId());
            ps.setString(2, txn.getStockSymbol());
            ps.setString(3, txn.getTransactionType());
            ps.setInt(4, txn.getQuantity());
            ps.setDouble(5, txn.getPrice());
            ps.executeUpdate();

            try (ResultSet rs = ps.getGeneratedKeys()) {
                if (rs.next()) {
                    int id = rs.getInt(1);
                    txn.setTransactionId(id);
                    return id;
                }
            }
        } catch (SQLException e) {
            System.err.println("[TransactionDAO] insertTransaction error: " + e.getMessage());
        }
        return -1;
    }

    // ── SELECT ────────────────────────────────────────────────

    /**
     * Returns the complete transaction history for a given user,
     * ordered newest first.
     */
    public List<Transaction> getTransactionsByUser(int userId) {
        List<Transaction> list = new ArrayList<>();
        String sql = "SELECT * FROM transactions WHERE user_id = ? "
                   + "ORDER BY transaction_date DESC";
        try (PreparedStatement ps = DatabaseConnection.getConnection()
                .prepareStatement(sql)) {

            ps.setInt(1, userId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(mapRow(rs));
                }
            }
        } catch (SQLException e) {
            System.err.println("[TransactionDAO] getTransactionsByUser error: " + e.getMessage());
        }
        return list;
    }

    /**
     * Returns all transactions for a user filtered by stock symbol.
     */
    public List<Transaction> getTransactionsByUserAndStock(int userId, String stockSymbol) {
        List<Transaction> list = new ArrayList<>();
        String sql = "SELECT * FROM transactions "
                   + "WHERE user_id = ? AND stock_symbol = ? "
                   + "ORDER BY transaction_date DESC";
        try (PreparedStatement ps = DatabaseConnection.getConnection()
                .prepareStatement(sql)) {

            ps.setInt(1, userId);
            ps.setString(2, stockSymbol.toUpperCase());
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(mapRow(rs));
                }
            }
        } catch (SQLException e) {
            System.err.println("[TransactionDAO] getTransactionsByUserAndStock error: "
                    + e.getMessage());
        }
        return list;
    }

    /**
     * Returns all transactions in the system (admin overview).
     */
    public List<Transaction> getAllTransactions() {
        List<Transaction> list = new ArrayList<>();
        String sql = "SELECT * FROM transactions ORDER BY transaction_date DESC";
        try (Statement stmt = DatabaseConnection.getConnection().createStatement();
             ResultSet rs   = stmt.executeQuery(sql)) {

            while (rs.next()) {
                list.add(mapRow(rs));
            }
        } catch (SQLException e) {
            System.err.println("[TransactionDAO] getAllTransactions error: " + e.getMessage());
        }
        return list;
    }

    /**
     * Fetches a single transaction by its ID.
     */
    public Transaction getTransactionById(int transactionId) {
        String sql = "SELECT * FROM transactions WHERE transaction_id = ?";
        try (PreparedStatement ps = DatabaseConnection.getConnection()
                .prepareStatement(sql)) {

            ps.setInt(1, transactionId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return mapRow(rs);
                }
            }
        } catch (SQLException e) {
            System.err.println("[TransactionDAO] getTransactionById error: " + e.getMessage());
        }
        return null;
    }

    // ── DELETE ────────────────────────────────────────────────

    /**
     * Deletes all transactions for a user (used only in testing / admin cleanup).
     */
    public boolean deleteTransactionsByUser(int userId) {
        String sql = "DELETE FROM transactions WHERE user_id = ?";
        try (PreparedStatement ps = DatabaseConnection.getConnection()
                .prepareStatement(sql)) {

            ps.setInt(1, userId);
            ps.executeUpdate();
            return true;

        } catch (SQLException e) {
            System.err.println("[TransactionDAO] deleteTransactionsByUser error: " + e.getMessage());
        }
        return false;
    }

    // ── Helper ───────────────────────────────────────────────

    private Transaction mapRow(ResultSet rs) throws SQLException {
        Timestamp ts            = rs.getTimestamp("transaction_date");
        LocalDateTime dateTime  = ts != null ? ts.toLocalDateTime() : LocalDateTime.now();
        return new Transaction(
                rs.getInt("transaction_id"),
                rs.getInt("user_id"),
                rs.getString("stock_symbol"),
                rs.getString("transaction_type"),
                rs.getInt("quantity"),
                rs.getDouble("price"),
                dateTime
        );
    }
}
