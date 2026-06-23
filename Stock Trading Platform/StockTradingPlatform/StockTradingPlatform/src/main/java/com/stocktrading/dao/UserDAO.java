package com.stocktrading.dao;

import com.stocktrading.model.User;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Data Access Object for the {@code users} table.
 * All CRUD operations for User entities live here.
 */
public class UserDAO {

    // ── INSERT ────────────────────────────────────────────────

    /**
     * Inserts a new user and returns the auto-generated user ID.
     *
     * @param user the user to insert (id field is ignored)
     * @return the generated user_id, or -1 on failure
     */
    public int insertUser(User user) {
        String sql = "INSERT INTO users (name, balance) VALUES (?, ?)";
        try (PreparedStatement ps = DatabaseConnection.getConnection()
                .prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            ps.setString(1, user.getName());
            ps.setDouble(2, user.getBalance());
            ps.executeUpdate();

            try (ResultSet rs = ps.getGeneratedKeys()) {
                if (rs.next()) {
                    int id = rs.getInt(1);
                    user.setUserId(id);
                    return id;
                }
            }
        } catch (SQLException e) {
            System.err.println("[UserDAO] insertUser error: " + e.getMessage());
        }
        return -1;
    }

    // ── SELECT ────────────────────────────────────────────────

    /**
     * Fetches a user by their primary key.
     *
     * @param userId the user_id to look up
     * @return the {@link User} or {@code null} if not found
     */
    public User getUserById(int userId) {
        String sql = "SELECT * FROM users WHERE user_id = ?";
        try (PreparedStatement ps = DatabaseConnection.getConnection()
                .prepareStatement(sql)) {

            ps.setInt(1, userId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return mapRow(rs);
                }
            }
        } catch (SQLException e) {
            System.err.println("[UserDAO] getUserById error: " + e.getMessage());
        }
        return null;
    }

    /**
     * Returns all users in the system.
     */
    public List<User> getAllUsers() {
        List<User> users = new ArrayList<>();
        String sql = "SELECT * FROM users ORDER BY user_id";
        try (Statement stmt = DatabaseConnection.getConnection().createStatement();
             ResultSet rs   = stmt.executeQuery(sql)) {

            while (rs.next()) {
                users.add(mapRow(rs));
            }
        } catch (SQLException e) {
            System.err.println("[UserDAO] getAllUsers error: " + e.getMessage());
        }
        return users;
    }

    // ── UPDATE ────────────────────────────────────────────────

    /**
     * Updates only the balance column for an existing user.
     *
     * @param userId  the user to update
     * @param balance new balance value
     * @return true if exactly one row was updated
     */
    public boolean updateBalance(int userId, double balance) {
        String sql = "UPDATE users SET balance = ? WHERE user_id = ?";
        try (PreparedStatement ps = DatabaseConnection.getConnection()
                .prepareStatement(sql)) {

            ps.setDouble(1, Math.round(balance * 100.0) / 100.0);
            ps.setInt(2, userId);
            return ps.executeUpdate() == 1;

        } catch (SQLException e) {
            System.err.println("[UserDAO] updateBalance error: " + e.getMessage());
        }
        return false;
    }

    /**
     * Updates name and balance for a user.
     */
    public boolean updateUser(User user) {
        String sql = "UPDATE users SET name = ?, balance = ? WHERE user_id = ?";
        try (PreparedStatement ps = DatabaseConnection.getConnection()
                .prepareStatement(sql)) {

            ps.setString(1, user.getName());
            ps.setDouble(2, user.getBalance());
            ps.setInt(3, user.getUserId());
            return ps.executeUpdate() == 1;

        } catch (SQLException e) {
            System.err.println("[UserDAO] updateUser error: " + e.getMessage());
        }
        return false;
    }

    // ── DELETE ────────────────────────────────────────────────

    /**
     * Deletes a user by ID (cascade deletes portfolio & transactions via FK).
     */
    public boolean deleteUser(int userId) {
        String sql = "DELETE FROM users WHERE user_id = ?";
        try (PreparedStatement ps = DatabaseConnection.getConnection()
                .prepareStatement(sql)) {

            ps.setInt(1, userId);
            return ps.executeUpdate() == 1;

        } catch (SQLException e) {
            System.err.println("[UserDAO] deleteUser error: " + e.getMessage());
        }
        return false;
    }

    // ── Helper ───────────────────────────────────────────────

    private User mapRow(ResultSet rs) throws SQLException {
        return new User(
                rs.getInt("user_id"),
                rs.getString("name"),
                rs.getDouble("balance")
        );
    }
}
