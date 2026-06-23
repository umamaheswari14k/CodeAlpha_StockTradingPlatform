-- ============================================================
-- Stock Trading Platform - MySQL Database Schema
-- Database: stock_trading_db
-- ============================================================

CREATE DATABASE IF NOT EXISTS stock_trading_db;
USE stock_trading_db;

-- ============================================================
-- TABLE: users
-- ============================================================
CREATE TABLE IF NOT EXISTS users (
    user_id      INT AUTO_INCREMENT PRIMARY KEY,
    name         VARCHAR(100) NOT NULL,
    balance      DOUBLE NOT NULL DEFAULT 0.0,
    created_at   TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- ============================================================
-- TABLE: stocks
-- ============================================================
CREATE TABLE IF NOT EXISTS stocks (
    stock_symbol  VARCHAR(10) PRIMARY KEY,
    company_name  VARCHAR(150) NOT NULL,
    current_price DOUBLE NOT NULL
);

-- ============================================================
-- TABLE: portfolio
-- ============================================================
CREATE TABLE IF NOT EXISTS portfolio (
    portfolio_id     INT AUTO_INCREMENT PRIMARY KEY,
    user_id          INT NOT NULL,
    stock_symbol     VARCHAR(10) NOT NULL,
    quantity         INT NOT NULL DEFAULT 0,
    purchase_value   DOUBLE NOT NULL DEFAULT 0.0,
    FOREIGN KEY (user_id)      REFERENCES users(user_id)  ON DELETE CASCADE,
    FOREIGN KEY (stock_symbol) REFERENCES stocks(stock_symbol) ON DELETE CASCADE,
    UNIQUE KEY uq_user_stock (user_id, stock_symbol)
);

-- ============================================================
-- TABLE: transactions
-- ============================================================
CREATE TABLE IF NOT EXISTS transactions (
    transaction_id   INT AUTO_INCREMENT PRIMARY KEY,
    user_id          INT NOT NULL,
    stock_symbol     VARCHAR(10) NOT NULL,
    transaction_type ENUM('BUY','SELL') NOT NULL,
    quantity         INT NOT NULL,
    price            DOUBLE NOT NULL,
    transaction_date TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (user_id)      REFERENCES users(user_id)  ON DELETE CASCADE,
    FOREIGN KEY (stock_symbol) REFERENCES stocks(stock_symbol) ON DELETE CASCADE
);

-- ============================================================
-- SAMPLE DATA: stocks
-- ============================================================
INSERT INTO stocks (stock_symbol, company_name, current_price) VALUES
('AAPL',  'Apple Inc.',                   185.50),
('GOOGL', 'Alphabet Inc.',                140.25),
('MSFT',  'Microsoft Corporation',        415.80),
('AMZN',  'Amazon.com Inc.',              178.90),
('TSLA',  'Tesla Inc.',                   245.60),
('META',  'Meta Platforms Inc.',          505.30),
('NVDA',  'NVIDIA Corporation',           875.40),
('NFLX',  'Netflix Inc.',                 630.20),
('RELIANCE', 'Reliance Industries Ltd.',  2850.75),
('TCS',   'Tata Consultancy Services',    3920.50);

-- ============================================================
-- SAMPLE DATA: users
-- ============================================================
INSERT INTO users (name, balance) VALUES
('Alice Johnson', 100000.00),
('Bob Smith',      50000.00),
('Charlie Kumar',  75000.00);

-- ============================================================
-- Verify tables
-- ============================================================
SHOW TABLES;
SELECT * FROM stocks;
SELECT * FROM users;
