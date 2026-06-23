# Stock Trading Platform
### Java Console Application 

A fully functional, object-oriented stock trading simulation built with
**Java 17**, **MySQL 8**, and **JDBC**. Users can view live (simulated) market
data, buy and sell stocks, track their portfolio performance, and review
complete transaction history — all persisted in a relational database.

---

## Project Structure

```
StockTradingPlatform/
├── pom.xml
├── sql/
│   └── schema.sql                          ← Database schema + sample data
└── src/main/java/com/stocktrading/
    ├── Main.java                           ← Entry point / menu loop
    ├── model/
    │   ├── Stock.java
    │   ├── User.java
    │   ├── Portfolio.java
    │   └── Transaction.java
    ├── dao/
    │   ├── DatabaseConnection.java
    │   ├── UserDAO.java
    │   ├── StockDAO.java
    │   ├── PortfolioDAO.java
    │   └── TransactionDAO.java
    ├── service/
    │   ├── Market.java
    │   ├── TradingService.java
    │   └── PortfolioService.java
    └── exception/
        ├── InsufficientBalanceException.java
        ├── InsufficientSharesException.java
        └── InvalidStockException.java
```

---

## Prerequisites

| Tool            | Version      |
|-----------------|--------------|
| JDK             | 17 or higher |
| MySQL Server    | 8.0+         |
| Maven           | 3.8+         |
| IntelliJ IDEA   | Any (Community / Ultimate) |

---

## Setup Instructions

### Step 1 — Clone / Download the project

Place the project folder anywhere on your machine.

### Step 2 — Configure the database

1. Start your MySQL server.
2. Open MySQL Workbench or the MySQL CLI.
3. Run the schema file:

```sql
SOURCE /path/to/StockTradingPlatform/sql/schema.sql;
```

This creates the `stock_trading_db` database, all four tables, and inserts
sample stock and user data automatically.

### Step 3 — Update DB credentials

Open `DatabaseConnection.java` and set your MySQL username / password:

```java
private static final String USERNAME = "root";   // your MySQL username
private static final String PASSWORD = "root";   // your MySQL password
```

### Step 4 — Build with Maven

```bash
cd StockTradingPlatform
mvn clean package
```

Maven will download the MySQL connector and compile all source files.

### Step 5 — Run the application

**Option A — via Maven:**
```bash
mvn exec:java -Dexec.mainClass="com.stocktrading.Main"
```

**Option B — run the fat JAR:**
```bash
java -jar target/StockTradingPlatform-1.0-SNAPSHOT-jar-with-dependencies.jar
```

**Option C — via IntelliJ IDEA:**
1. Open the project (File → Open → select `StockTradingPlatform/`).
2. Let IntelliJ import the Maven project.
3. Right-click `Main.java` → Run 'Main.main()'.

---

## Database Schema

```sql
┌──────────────────┐        ┌──────────────────────┐
│      users       │        │       stocks          │
├──────────────────┤        ├──────────────────────┤
│ user_id  (PK)    │        │ stock_symbol  (PK)   │
│ name             │        │ company_name         │
│ balance          │        │ current_price        │
│ created_at       │        └──────────────────────┘
└──────────┬───────┘                  │
           │                          │
           │      ┌───────────────────┤──────────────────┐
           │      │    portfolio      │                  │
           │      ├───────────────────┤                  │
           └─────►│ user_id     (FK)  │◄─────────────────┘
                  │ stock_symbol (FK) │
                  │ quantity          │
                  │ purchase_value    │
                  └───────────────────┘
           │
           │      ┌───────────────────┐
           │      │   transactions    │
           │      ├───────────────────┤
           └─────►│ user_id     (FK)  │◄─ stock_symbol (FK)
                  │ transaction_id PK │
                  │ transaction_type  │
                  │ quantity          │
                  │ price             │
                  │ transaction_date  │
                  └───────────────────┘
```

---

## UML Class Diagram

```
 ┌─────────────────────────────────────────────────────────────────────────────┐
 │                          com.stocktrading                                   │
 └─────────────────────────────────────────────────────────────────────────────┘

 ┌──────────────────┐    ┌──────────────────┐    ┌──────────────────────────┐
 │     Stock        │    │      User        │    │       Transaction        │
 ├──────────────────┤    ├──────────────────┤    ├──────────────────────────┤
 │-stockSymbol:Str  │    │-userId:int       │    │-transactionId:int        │
 │-companyName:Str  │    │-name:String      │    │-userId:int               │
 │-currentPrice:dbl │    │-balance:double   │    │-stockSymbol:String       │
 ├──────────────────┤    ├──────────────────┤    │-transactionType:String   │
 │+simulatePrice()  │    │+canAfford():bool │    │-quantity:int             │
 │+toDisplayString()│    │+deductBalance()  │    │-price:double             │
 │+toDisplayString  │    │+addBalance()     │    │-transactionDate:LDT      │
 │  (double prev)   │    │+toDisplayString()│    ├──────────────────────────┤
 └──────────────────┘    └──────────────────┘    │+getTotalValue():double   │
          ▲ uses                                  │+toDisplayString()        │
          │                                       │+toDisplayString(bool)    │
 ┌────────┴─────────┐                             └──────────────────────────┘
 │    Portfolio     │
 ├──────────────────┤
 │-portfolioId:int  │    «uses / composed»
 │-userId:int       │◄────────────────────────────── Stock (composition)
 │-stockSymbol:Str  │
 │-quantity:int     │
 │-purchaseValue:dbl│
 │-stock:Stock      │
 ├──────────────────┤
 │+getCurrentMktVal │
 │+getProfitOrLoss  │
 │+getPnLPercent    │
 │+getAvgBuyPrice   │
 │+toDisplayString()│
 └──────────────────┘

 ┌──────────────────────┐    ┌───────────────────────┐   ┌──────────────────────┐
 │   DatabaseConnection │    │       UserDAO         │   │       StockDAO       │
 ├──────────────────────┤    ├───────────────────────┤   ├──────────────────────┤
 │-connection:Conn (1)  │    │+insertUser(User)      │   │+insertStock(Stock)   │
 │-URL,USER,PASS        │    │+getUserById(int)      │   │+getStockBySymbol(Str)│
 ├──────────────────────┤    │+getAllUsers()         │   │+getAllStocks()        │
 │+getConnection()      │    │+updateBalance(id,dbl) │   │+updateStockPrice()   │
 │+closeConnection()    │    │+updateUser(User)      │   │+updateAllPrices()    │
 └──────────────────────┘    │+deleteUser(int)       │   │+deleteStock(String)  │
                             └───────────────────────┘   └──────────────────────┘

 ┌──────────────────────┐    ┌────────────────────────────┐
 │    PortfolioDAO      │    │      TransactionDAO         │
 ├──────────────────────┤    ├────────────────────────────┤
 │+insertPortfolio()    │    │+insertTransaction(Txn)     │
 │+getPortfolioEntry()  │    │+getTransactionsByUser(int) │
 │+getPortfolioByUser() │    │+getTransactionsByUserAndStk│
 │+updatePortfolio()    │    │+getAllTransactions()        │
 │+deletePortfolioEntry │    │+getTransactionById(int)    │
 └──────────────────────┘    │+deleteTransactionsByUser() │
                             └────────────────────────────┘

 ┌──────────────────────────┐    ┌──────────────────────────┐
 │         Market           │    │      TradingService      │
 ├──────────────────────────┤    ├──────────────────────────┤
 │-stockMap:HashMap<Str,Stk>│    │-market:Market            │
 │-stockList:ArrayList<Stk> │    │-userDAO:UserDAO          │
 │-stockDAO:StockDAO        │    │-portfolioDAO:PortfolioDAO│
 ├──────────────────────────┤    │-transDAO:TransactionDAO  │
 │+getStock(symbol)         │    ├──────────────────────────┤
 │+getAllStocks()            │    │+buyStock(User,sym,qty)   │
 │+refreshAllPrices()       │    │+sellStock(User,sym,qty)  │
 │+displayMarket()          │    │+buyStock(int,sym,qty)    │ «overloaded»
 │+stockExists(sym):bool    │    │+sellStock(int,sym,qty)   │ «overloaded»
 └──────────────────────────┘    └──────────────────────────┘

 ┌──────────────────────────┐
 │     PortfolioService     │
 ├──────────────────────────┤
 │-market:Market            │
 │-portfolioDAO:PortfolioDAO│
 │-transDAO:TransactionDAO  │
 ├──────────────────────────┤
 │+displayPortfolio(User)   │
 │+displayTransactionHist() │
 │+getPerformanceSummary(U) │ «overloaded»
 │+getPerformanceSummary    │ «overloaded»
 │  (User, boolean)         │
 └──────────────────────────┘

 ┌──────────────────────────────────────────────────┐
 │               Custom Exceptions                  │
 ├──────────────────────────────────────────────────┤
 │  InsufficientBalanceException  extends Exception │
 │  InsufficientSharesException   extends Exception │
 │  InvalidStockException         extends Exception │
 └──────────────────────────────────────────────────┘
```

---

## Sample Console Output

```
╔══════════════════════════════════════════════════════════╗
║          STOCK  TRADING  PLATFORM  v1.0                 ║
║          Java Console Application  |  MySQL + JDBC       ║
╚══════════════════════════════════════════════════════════╝
[DB] Connected to MySQL database: stock_trading_db
[Market] Loaded 10 stocks from database.

╔══════════════════════════════════╗
║        USER LOGIN / REGISTER     ║
╠══════════════════════════════════╣
║  1. Login with existing User ID  ║
║  2. Register as a new user       ║
║  3. List all users               ║
║  0. Exit                         ║
╚══════════════════════════════════╝
Select: 1
Enter your User ID: 1

  ✅  Welcome back, Alice Johnson!
      Available Balance: Rs.100000.00

╔══════════════════════════════════════╗
║  Logged in : Alice Johnson           ║
║  Balance   : Rs.100000.00             ║
╠══════════════════════════════════════╣
║  1. View Market Data                 ║
║  2. Buy Stock                        ║
...
╚══════════════════════════════════════╝
Enter choice: 1

  📈  LIVE MARKET DATA
======================================================================
SYMBOL     | COMPANY                             | PRICE (Rs.)
======================================================================
AAPL       | Apple Inc.                          |     185.50
AMZN       | Amazon.com Inc.                     |     178.90
GOOGL      | Alphabet Inc.                       |     140.25
META       | Meta Platforms Inc.                 |     505.30
MSFT       | Microsoft Corporation               |     415.80
NFLX       | Netflix Inc.                        |     630.20
NVDA       | NVIDIA Corporation                  |     875.40
RELIANCE   | Reliance Industries Ltd.            |    2850.75
TCS        | Tata Consultancy Services           |    3920.50
TSLA       | Tesla Inc.                          |     245.60
======================================================================
  Total stocks listed: 10
======================================================================

Enter choice: 2

  🛒  BUY STOCK
...
  Enter stock symbol to BUY : AAPL
  Enter quantity             : 10

✅  BUY ORDER EXECUTED
    Stock    : AAPL – Apple Inc.
    Shares   : 10  @  Rs.185.50 each
    Total    : Rs.1855.00
    Remaining Balance: Rs.98145.00

Enter choice: 4

====================================================================================================
  PORTFOLIO — Alice Johnson  |  Available Balance: Rs.98145.00
====================================================================================================
SYMBOL     | COMPANY                        |    QTY |   INVESTED (Rs.) | MARKET VAL (Rs.) | P&L
----------------------------------------------------------------------------------------------------
AAPL       | Apple Inc.                     |     10 |        1855.00 |        1862.30 | +Rs.7.30 (0.4%)
====================================================================================================
  Total Invested:                            Rs.      1855.00
  Current Portfolio Value:                   Rs.      1862.30
  Overall P&L:                               +Rs.7.30  (0.39%)
  Available Cash Balance:                    Rs.     98145.00
  Total Net Worth (Cash + Portfolio):        Rs.    100007.30
====================================================================================================

Enter choice: 5

=========================================================================================================
  TRANSACTION HISTORY — Alice Johnson
=========================================================================================================
TXN ID   | TYPE | SYMBOL     |   QTY |   PRICE (Rs.) |     TOTAL (Rs.) | DATE & TIME
---------------------------------------------------------------------------------------------------------
ID: 1      | BUY  | AAPL       |   10  |     185.50  |      1855.00  | 19-06-2026 14:32:07
=========================================================================================================
  Total transactions: 1
=========================================================================================================

Enter choice: 6

  🔄  REFRESHING MARKET PRICES...
[Market] Refreshing all stock prices...
  AAPL        Rs.185.50  →  Rs.188.23
  AMZN        Rs.178.90  →  Rs.175.61
  GOOGL       Rs.140.25  →  Rs.143.10
  ...
[Market] Prices updated.

  ✅  Market prices updated.

Enter choice: 7

  Thank you for using Stock Trading Platform. Goodbye!
[DB] Connection closed.
```

---

## OOP Concepts Demonstrated

| Concept            | Where Used |
|--------------------|-----------|
| Encapsulation      | All model classes (private fields + getters/setters) |
| Constructors       | Multiple constructors in every model class |
| Composition        | `Portfolio` contains `Stock`; services contain DAOs |
| Method Overloading | `toDisplayString()`, `buyStock()`, `sellStock()`, `getPerformanceSummary()` |
| Custom Exceptions  | `InsufficientBalanceException`, `InsufficientSharesException`, `InvalidStockException` |
| ArrayList          | `Market.stockList`, all DAO list returns |
| HashMap            | `Market.stockMap` for O(1) symbol lookup |

---

## Author
CodeAlpha Intern Task 2 — Java + MySQL  
Stack: Java 17 · MySQL 8 · JDBC · Maven
