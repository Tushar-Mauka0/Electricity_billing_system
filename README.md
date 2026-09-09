# ⚡ Electricity Bill Generation & AI Analytics System

A comprehensive, production-ready desktop application built in **Java (Swing UI + JDBC)** for power distribution utilities (DISCOMs). Tailored specifically for **Indian utility tariffs (INR ₹, kWh units, Electricity Duty taxes, and Govt Subsidies)**, this system features a dynamic tiered slab billing engine, dual database persistence (MySQL with seamless SQLite fallback), AI consumption anomaly detection, linear trend forecasting, and printable HTML/PDF invoice generation.

---

## 🌟 Key Highlights & Features

### 1. 🧮 Dynamic Slab-Wise Billing Engine
- **Tiered Energy Tariffs**: Calculates energy charges across consumption slabs (e.g., 0–100 units @ ₹1.50, 101–300 @ ₹3.50, 301–500 @ ₹6.00, >500 @ ₹8.50).
- **Category Tariffs**: Supports **Residential**, **Commercial**, and **Industrial** consumer tiers.
- **Fixed Charges & Taxes**: Incorporates base meter demand charges (₹) and state electricity duty percentages (9%, 12%, 15%).
- **Government Subsidies**: Automatically credits government electricity subsidies (e.g. ₹100 flat credit for residential consumption ≤ 100 kWh).

### 2. 🗄️ Dual JDBC Database Architecture (MySQL + SQLite Fallback)
- **MySQL Workbench Compatibility**: Includes a standalone, clean database script [`database/schema.sql`](database/schema.sql).
- **Automatic Failover**: Built-in connection manager ([`DBConnection.java`](src/config/DBConnection.java)) automatically attempts connection to MySQL (`localhost:3306`), and seamlessly falls back to a local SQLite database (`electricity_bill.db`) if MySQL is offline or unconfigured.

### 3. 🤖 AI Utility Analytics & Spike Anomaly Detector
- **Spike Anomaly Detector**: Compares incoming meter readings against a moving 3-month baseline to flag unusual jumps (>40% deviation) with severity ratings (`HIGH`, `MEDIUM`, `LOW`).
- **Predictive Metering**: Uses linear trend regression ($\hat{y} = mx + c$) on historical consumption to project next month's energy load (kWh) and bill amount (₹).
- **AI Energy Advisor**: Delivers customized energy conservation advice (e.g. PM Surya Ghar solar rooftop feasibility, APFC panel power factor tuning, Time-of-Day load shifting).

### 4. 📄 Printable Receipts & Direct PDF Export Engine
- **Itemized HTML Receipts**: Renders formatted electricity bills with GSTIN info, consumer account details, itemized slab breakdown tables, payment status badges (`PAID`/`UNPAID`), and simulated UPI payment QR links.
- **Direct PDF Export**: Embedded Apache PDFBox engine exports high-DPI A4 PDF invoices directly via a file save dialog.

### 5. 🎨 Modern Responsive Swing Desktop GUI
- Clean flat styling, dark navy sidebar navigation, KPI dashboard cards, searchable data tables, responsive split views, and smooth window scaling.

---

## 📁 Project Structure

```text
electricity_bill_system/
├── database/
│   └── schema.sql                  # MySQL Workbench creation & seed script
├── lib/                            # JDBC & PDFBox Jar dependencies
│   ├── mysql-connector-j-8.3.0.jar
│   ├── sqlite-jdbc-3.45.1.0.jar
│   ├── pdfbox-2.0.29.jar
│   ├── fontbox-2.0.29.jar
│   ├── slf4j-api-2.0.9.jar
│   ├── slf4j-simple-2.0.9.jar
│   └── commons-logging-1.2.jar
├── src/
│   ├── Main.java                    # Application launch entry point
│   ├── config/
│   │   └── DBConnection.java        # Dynamic MySQL / SQLite JDBC connection manager
│   ├── model/
│   │   ├── Customer.java            # Consumer data model
│   │   ├── MeterReading.java        # Meter reading data model
│   │   ├── SlabRate.java            # Tariff slab configuration model
│   │   ├── Bill.java                # Generated bill invoice model
│   │   └── AIInsight.java           # Anomaly alert & prediction model
│   ├── dao/
│   │   ├── CustomerDAO.java         # Customer data access object
│   │   ├── MeterReadingDAO.java     # Reading data access object
│   │   ├── SlabRateDAO.java         # Tariff slab data access object
│   │   ├── BillDAO.java             # Bill & payment data access object
│   │   └── AIInsightDAO.java        # AI insights data access object
│   ├── service/
│   │   ├── SlabCalculatorService.java # Dynamic slab calculation engine
│   │   ├── AIConsumptionService.java # Anomaly detection & trend forecasting
│   │   ├── BillExportService.java    # HTML receipt renderer
│   │   └── PDFExportService.java     # PDFBox PDF file exporter
│   └── ui/
│       ├── theme/
│       │   └── UITheme.java          # Color palette & button styling
│       ├── MainFrame.java            # Main window & sidebar navigator
│       ├── DashboardPanel.java       # Overview dashboard with KPI cards
│       ├── CustomerPanel.java        # Consumer management panel
│       ├── MeterReadingPanel.java    # Meter reading input & live preview
│       ├── TariffConfigPanel.java    # Slab rates configuration panel
│       ├── BillManagementPanel.java  # Bills ledger & PDF export dialog
│       └── AIAnalyticsPanel.java     # AI anomaly audit & forecasting tab
└── compile_and_run.sh               # Build & launch script
```

---

## ⚡ Default Indian Tariff Slab Rates (INR ₹)

| Category | Min Units (kWh) | Max Units (kWh) | Rate per Unit (₹) | Base Fixed Charge (₹) | Electricity Duty Tax (%) | Subsidy Credit (₹) |
| :--- | :---: | :---: | :---: | :---: | :---: | :---: |
| **Residential** | 0 | 100 | ₹ 1.50 | ₹ 85.00 | 9.0% | ₹ 100.00 (Units ≤ 100) |
| **Residential** | 101 | 300 | ₹ 3.50 | ₹ 85.00 | 9.0% | ₹ 0.00 |
| **Residential** | 301 | 500 | ₹ 6.00 | ₹ 85.00 | 9.0% | ₹ 0.00 |
| **Residential** | 501 | 999,999 | ₹ 8.50 | ₹ 85.00 | 9.0% | ₹ 0.00 |
| **Commercial** | 0 | 200 | ₹ 6.50 | ₹ 250.00 | 12.0% | ₹ 0.00 |
| **Commercial** | 201 | 500 | ₹ 8.50 | ₹ 250.00 | 12.0% | ₹ 0.00 |
| **Commercial** | 501 | 999,999 | ₹ 11.00 | ₹ 250.00 | 12.0% | ₹ 0.00 |
| **Industrial** | 0 | 1000 | ₹ 8.00 | ₹ 750.00 | 15.0% | ₹ 0.00 |
| **Industrial** | 1001 | 999,999 | ₹ 10.50 | ₹ 750.00 | 15.0% | ₹ 0.00 |

---

## 🖥️ System Requirements

- **Java Development Kit (JDK)**: Java 17 or higher (Java 21 / 26 fully supported).
- **Operating System**: macOS, Windows 10/11, or Linux.
- **Database (Optional)**: MySQL 8.0+ (If MySQL is not installed, the application automatically uses local SQLite).

---

## 🚀 How to Build & Run

### Step 1: Open Terminal in Project Folder
```bash
cd electricity_bill_system
```

### Step 2: Run the Build Script
The provided `compile_and_run.sh` script automatically verifies/downloads required JAR dependencies into `./lib/`, compiles all Java source files into `./bin/`, and launches the application:

```bash
chmod +x compile_and_run.sh
./compile_and_run.sh
```

---

## 💻 Modules Overview

1. **Dashboard Panel**: Displays total revenue collected (₹), active consumer count, pending bill count, AI anomaly alerts, and recent billing activity.
2. **Consumers Panel**: Searchable consumer database with DISCOM assignment (`MSEDCL`, `BESCOM`, `TATA Power`, `TPDDL`, `CESC`, `TNEB`, `UPVCL`), meter serial registration, and consumer account number auto-generation (`CA-IND-XXXX`).
3. **Meter Readings Panel**: Entry form for meter readings with a **Real-Time Live Slab Breakdown Preview** pane that calculates energy charges, fixed charges, taxes, and subsidies instantly as you type.
4. **Tariff Slabs Config Panel**: Tariff rule editor to modify category slab boundaries, per-unit rates, fixed charges, tax percentages, and subsidies.
5. **Bills & Invoices Panel**: Filter bills by status (`ALL`, `UNPAID`, `PAID`), record bill payments, view formatted HTML receipts, and **Save as PDF** via file dialog.
6. **AI Analytics Panel**: Grid-wide AI consumption audit tool, anomaly severity flag table (`HIGH`, `MEDIUM`, `LOW`), next-cycle unit/cost forecasting, and tailored AI energy conservation advice.

---

## 📄 Database Setup for MySQL Workbench (Optional)

If you prefer using MySQL Workbench instead of the automatic local SQLite fallback:
1. Open **MySQL Workbench**.
2. Run the script [`database/schema.sql`](database/schema.sql).
3. Update default credentials in [`src/config/DBConnection.java`](src/config/DBConnection.java) if necessary (`MYSQL_USER`, `MYSQL_PASS`).

---

## 📜 License
This project is open-source under the **MIT License**.
