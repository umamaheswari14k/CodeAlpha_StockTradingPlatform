package com.stocktrading;

import com.stocktrading.dao.*;
import com.stocktrading.exception.InsufficientBalanceException;
import com.stocktrading.exception.InsufficientSharesException;
import com.stocktrading.exception.InvalidStockException;
import com.stocktrading.model.User;
import com.stocktrading.service.Market;
import com.stocktrading.service.PortfolioService;
import com.stocktrading.service.TradingService;

import java.util.InputMismatchException;
import java.util.List;
import java.util.Scanner;
/**
 * Entry point for the Stock Trading Platform console application.
 *
 * Menu:
 *   1. View Market Data
 *   2. Buy Stock
 *   3. Sell Stock
 *   4. View Portfolio
 *   5. View Transaction History
 *   6. Refresh Market Prices
 *   7. Exit
 */
public class Main {

    // ── Shared Scanner ────────────────────────────────────────
    private static final Scanner sc = new Scanner(System.in);

    // ── DAO instances ─────────────────────────────────────────
    private static UserDAO        userDAO;
    private static StockDAO       stockDAO;
    private static PortfolioDAO   portfolioDAO;
    private static TransactionDAO transactionDAO;

    // ── Service / domain instances ────────────────────────────
    private static Market          market;
    private static TradingService  tradingService;
    private static PortfolioService portfolioService;

    // ── Currently active user ─────────────────────────────────
    private static User currentUser = null;

    // ─────────────────────────────────────────────────────────
    public static void main(String[] args) {

        printBanner();

        // 1. Initialise DAOs
        userDAO        = new UserDAO();
        stockDAO       = new StockDAO();
        portfolioDAO   = new PortfolioDAO();
        transactionDAO = new TransactionDAO();

        // 2. Initialise market (loads stocks from DB)
        market = new Market(stockDAO);

        // 3. Initialise services
        tradingService   = new TradingService(market, userDAO, portfolioDAO, transactionDAO);
        portfolioService = new PortfolioService(market, portfolioDAO, transactionDAO);

        // 4. User login / registration
        currentUser = loginOrRegister();
        if (currentUser == null) {
            System.out.println("Exiting. Goodbye!");
            DatabaseConnection.closeConnection();
            return;
        }

        // 5. Main menu loop
        boolean running = true;
        while (running) {
            printMainMenu();
            int choice = readInt("Enter choice: ");

            switch (choice) {
                case 1 -> handleViewMarket();
                case 2 -> handleBuyStock();
                case 3 -> handleSellStock();
                case 4 -> handleViewPortfolio();
                case 5 -> handleViewTransactions();
                case 6 -> handleRefreshPrices();
                case 7 -> {
                    running = false;
                    System.out.println("\n  Thank you for using Stock Trading Platform. Goodbye!\n");
                }
                default -> System.out.println("  ⚠  Invalid option. Please choose 1–7.");
            }
        }

        DatabaseConnection.closeConnection();
    }

    // ── Login / Register ──────────────────────────────────────

    private static User loginOrRegister() {
        while (true) {
            System.out.println("\n╔══════════════════════════════════╗");
            System.out.println("║        USER LOGIN / REGISTER     ║");
            System.out.println("╠══════════════════════════════════╣");
            System.out.println("║  1. Login with existing User ID  ║");
            System.out.println("║  2. Register as a new user       ║");
            System.out.println("║  3. List all users               ║");
            System.out.println("║  0. Exit                         ║");
            System.out.println("╚══════════════════════════════════╝");

            int opt = readInt("Select: ");

            switch (opt) {
                case 1 -> {
                    int id   = readInt("Enter your User ID: ");
                    User usr = userDAO.getUserById(id);
                    if (usr == null) {
                        System.out.println("  ⚠  User not found. Try again.");
                    } else {
                        System.out.println("\n  ✅  Welcome back, " + usr.getName() + "!");
                        System.out.printf("      Available Balance: Rs.%.2f%n", usr.getBalance());
                        return usr;
                    }
                }
                case 2 -> {
                    System.out.print("  Enter your name       : ");
                    String name = sc.nextLine().trim();
                    double bal  = readDouble("  Opening balance (Rs.)  : ");
                    if (bal < 0) { System.out.println("  ⚠  Balance cannot be negative."); break; }
                    User newUser = new User(name, bal);
                    int  newId   = userDAO.insertUser(newUser);
                    if (newId > 0) {
                        System.out.println("\n  ✅  Account created!  Your User ID is: " + newId);
                        return newUser;
                    } else {
                        System.out.println("  ⚠  Registration failed. Please try again.");
                    }
                }
                case 3 -> {
                    List<User> all = userDAO.getAllUsers();
                    System.out.println("\n  --- Registered Users ---");
                    if (all.isEmpty()) {
                        System.out.println("  No users registered yet.");
                    } else {
                        all.forEach(u -> System.out.println("  " + u.toDisplayString()));
                    }
                }
                case 0 -> { return null; }
                default -> System.out.println("  ⚠  Invalid option.");
            }
        }
    }

    // ── Menu Handlers ─────────────────────────────────────────

    /** 1. View Market Data */
    private static void handleViewMarket() {
        System.out.println("\n  📈  LIVE MARKET DATA");
        market.displayMarket();
    }

    /** 2. Buy Stock */
    private static void handleBuyStock() {
        System.out.println("\n  🛒  BUY STOCK");
        market.displayMarket();

        String symbol   = readString("  Enter stock symbol to BUY : ").toUpperCase();
        int    quantity = readInt   ("  Enter quantity             : ");

        // Re-fetch latest balance from DB to avoid stale data
        currentUser = userDAO.getUserById(currentUser.getUserId());

        try {
            tradingService.buyStock(currentUser, symbol, quantity);
        } catch (InvalidStockException e) {
            System.out.println("\n  ❌  " + e.getMessage());
        } catch (InsufficientBalanceException e) {
            System.out.println("\n  ❌  " + e.getMessage());
        } catch (IllegalArgumentException e) {
            System.out.println("\n  ❌  " + e.getMessage());
        }
    }

    /** 3. Sell Stock */
    private static void handleSellStock() {
        System.out.println("\n  💰  SELL STOCK");

        // Show current holdings first
        portfolioService.displayPortfolio(currentUser);

        String symbol   = readString("  Enter stock symbol to SELL : ").toUpperCase();
        int    quantity = readInt   ("  Enter quantity              : ");

        // Re-fetch latest balance
        currentUser = userDAO.getUserById(currentUser.getUserId());

        try {
            tradingService.sellStock(currentUser, symbol, quantity);
        } catch (InvalidStockException e) {
            System.out.println("\n  ❌  " + e.getMessage());
        } catch (InsufficientSharesException e) {
            System.out.println("\n  ❌  " + e.getMessage());
        } catch (IllegalArgumentException e) {
            System.out.println("\n  ❌  " + e.getMessage());
        }
    }

    /** 4. View Portfolio */
    private static void handleViewPortfolio() {
        // Always fetch fresh user data so balance is current
        currentUser = userDAO.getUserById(currentUser.getUserId());
        portfolioService.displayPortfolio(currentUser);

        System.out.println("  📊  " + portfolioService.getPerformanceSummary(currentUser, true));
    }

    /** 5. View Transaction History */
    private static void handleViewTransactions() {
        portfolioService.displayTransactionHistory(currentUser);
    }

    /** 6. Refresh Market Prices */
    private static void handleRefreshPrices() {
        System.out.println("\n  🔄  REFRESHING MARKET PRICES...");
        market.refreshAllPrices();
        System.out.println("  ✅  Market prices updated. View Market Data to see latest prices.");
    }

    // ── Utility helpers ───────────────────────────────────────

    private static void printBanner() {
        System.out.println();
        System.out.println("╔══════════════════════════════════════════════════════════╗");
        System.out.println("║          STOCK  TRADING  PLATFORM  v1.0                 ║");
        System.out.println("║          Java Console Application  |  MySQL + JDBC       ║");
        System.out.println("╚══════════════════════════════════════════════════════════╝");
    }

    private static void printMainMenu() {
        System.out.println("\n╔══════════════════════════════════════╗");
        System.out.printf ("║  Logged in : %-24s║%n", currentUser.getName());
        System.out.printf ("║  Balance   : Rs.%-23.2f║%n", currentUser.getBalance());
        System.out.println("╠══════════════════════════════════════╣");
        System.out.println("║  1. View Market Data                 ║");
        System.out.println("║  2. Buy Stock                        ║");
        System.out.println("║  3. Sell Stock                       ║");
        System.out.println("║  4. View Portfolio                   ║");
        System.out.println("║  5. View Transaction History         ║");
        System.out.println("║  6. Refresh Market Prices            ║");
        System.out.println("║  7. Exit                             ║");
        System.out.println("╚══════════════════════════════════════╝");
    }

    /** Safely reads an integer, re-prompting on bad input. */
    private static int readInt(String prompt) {
        while (true) {
            try {
                System.out.print(prompt);
                int val = sc.nextInt();
                sc.nextLine();   // consume newline
                return val;
            } catch (InputMismatchException e) {
                sc.nextLine();   // flush bad token
                System.out.println("  ⚠  Please enter a valid integer.");
            }
        }
    }

    /** Safely reads a double, re-prompting on bad input. */
    private static double readDouble(String prompt) {
        while (true) {
            try {
                System.out.print(prompt);
                double val = sc.nextDouble();
                sc.nextLine();
                return val;
            } catch (InputMismatchException e) {
                sc.nextLine();
                System.out.println("  ⚠  Please enter a valid number.");
            }
        }
    }

    /** Reads a non-empty trimmed string. */
    private static String readString(String prompt) {
        while (true) {
            System.out.print(prompt);
            String val = sc.nextLine().trim();
            if (!val.isEmpty()) return val;
            System.out.println("  ⚠  Input cannot be empty.");
        }
    }
}
