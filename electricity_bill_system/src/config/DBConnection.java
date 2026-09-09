package config;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.Statement;

public class DBConnection {
    private static final String MYSQL_URL = "jdbc:mysql://localhost:3306/electricity_db?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=UTC";
    private static final String MYSQL_ROOT_URL = "jdbc:mysql://localhost:3306/?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=UTC";
    private static final String MYSQL_USER = "root";
    private static final String MYSQL_PASS = "";

    private static final String SQLITE_URL = "jdbc:sqlite:electricity_bill.db";

    private static String activeDbType = "UNKNOWN";

    public static Connection getConnection() {
        Connection conn = null;
        
        // 1. Try MySQL first
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
            
            // Try creating DB if not exists
            try (Connection rootConn = DriverManager.getConnection(MYSQL_ROOT_URL, MYSQL_USER, MYSQL_PASS);
                 Statement stmt = rootConn.createStatement()) {
                stmt.executeUpdate("CREATE DATABASE IF NOT EXISTS electricity_db");
            } catch (Exception ignored) {}

            conn = DriverManager.getConnection(MYSQL_URL, MYSQL_USER, MYSQL_PASS);
            activeDbType = "MySQL (localhost:3306)";
            initSchemaIfEmpty(conn, false);
            return conn;
        } catch (Throwable t) {
            // MySQL unavailable or credentials mismatched -> Fallback to SQLite
        }

        // 2. SQLite Fallback
        try {
            Class.forName("org.sqlite.JDBC");
            conn = DriverManager.getConnection(SQLITE_URL);
            activeDbType = "SQLite (Local File)";
            initSchemaIfEmpty(conn, true);
            return conn;
        } catch (Throwable t) {
            System.err.println("Database connection failure: " + t.getMessage());
            t.printStackTrace();
        }

        return null;
    }

    public static String getActiveDbType() {
        if ("UNKNOWN".equals(activeDbType)) {
            // Trigger connection attempt to establish type
            Connection c = getConnection();
            if (c != null) {
                try { c.close(); } catch (Exception ignored) {}
            }
        }
        return activeDbType;
    }

    private static void initSchemaIfEmpty(Connection conn, boolean isSqlite) {
        try (Statement stmt = conn.createStatement()) {
            boolean tablesExist = false;
            try {
                ResultSet rs = stmt.executeQuery("SELECT COUNT(*) FROM tariff_slabs");
                if (rs.next()) {
                    tablesExist = true;
                }
            } catch (Exception e) {
                tablesExist = false;
            }

            if (!tablesExist) {
                System.out.println("Initializing DB Schema (" + (isSqlite ? "SQLite" : "MySQL") + ")...");

                if (isSqlite) {
                    stmt.executeUpdate("CREATE TABLE IF NOT EXISTS customers (" +
                            "customer_id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                            "consumer_no TEXT UNIQUE NOT NULL, " +
                            "name TEXT NOT NULL, " +
                            "email TEXT, " +
                            "phone TEXT, " +
                            "address TEXT, " +
                            "category TEXT NOT NULL DEFAULT 'Residential', " +
                            "discom_name TEXT DEFAULT 'MSEDCL', " +
                            "meter_no TEXT UNIQUE NOT NULL, " +
                            "connection_date DATE NOT NULL, " +
                            "created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP)");

                    stmt.executeUpdate("CREATE TABLE IF NOT EXISTS tariff_slabs (" +
                            "slab_id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                            "category TEXT NOT NULL, " +
                            "min_units INTEGER NOT NULL, " +
                            "max_units INTEGER NOT NULL, " +
                            "rate_per_unit REAL NOT NULL, " +
                            "fixed_charge REAL NOT NULL DEFAULT 0.00, " +
                            "tax_percent REAL NOT NULL DEFAULT 0.00, " +
                            "subsidy_amount REAL NOT NULL DEFAULT 0.00)");

                    stmt.executeUpdate("CREATE TABLE IF NOT EXISTS meter_readings (" +
                            "reading_id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                            "customer_id INTEGER NOT NULL, " +
                            "reading_date DATE NOT NULL, " +
                            "prev_reading REAL NOT NULL, " +
                            "curr_reading REAL NOT NULL, " +
                            "units_consumed REAL NOT NULL, " +
                            "reader_name TEXT DEFAULT 'System Auto', " +
                            "created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP, " +
                            "FOREIGN KEY (customer_id) REFERENCES customers(customer_id))");

                    stmt.executeUpdate("CREATE TABLE IF NOT EXISTS bills (" +
                            "bill_id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                            "customer_id INTEGER NOT NULL, " +
                            "reading_id INTEGER NOT NULL, " +
                            "bill_date DATE NOT NULL, " +
                            "due_date DATE NOT NULL, " +
                            "units_consumed REAL NOT NULL, " +
                            "energy_charge REAL NOT NULL, " +
                            "fixed_charge REAL NOT NULL, " +
                            "tax_amount REAL NOT NULL, " +
                            "subsidy_amount REAL NOT NULL, " +
                            "total_amount REAL NOT NULL, " +
                            "payment_status TEXT NOT NULL DEFAULT 'UNPAID', " +
                            "payment_date DATE, " +
                            "created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP, " +
                            "FOREIGN KEY (customer_id) REFERENCES customers(customer_id), " +
                            "FOREIGN KEY (reading_id) REFERENCES meter_readings(reading_id))");

                    stmt.executeUpdate("CREATE TABLE IF NOT EXISTS ai_insights (" +
                            "insight_id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                            "customer_id INTEGER NOT NULL, " +
                            "is_anomaly INTEGER DEFAULT 0, " +
                            "severity TEXT DEFAULT 'NONE', " +
                            "anomaly_reason TEXT, " +
                            "predicted_next_units REAL DEFAULT 0.00, " +
                            "predicted_next_bill REAL DEFAULT 0.00, " +
                            "optimization_tips TEXT, " +
                            "generated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP, " +
                            "FOREIGN KEY (customer_id) REFERENCES customers(customer_id))");
                }

                seedInitialData(stmt);
            }
        } catch (Exception e) {
            System.err.println("Error initializing schema: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private static void seedInitialData(Statement stmt) throws Exception {
        // Tariff Slabs (INR ₹)
        stmt.executeUpdate("INSERT INTO tariff_slabs (category, min_units, max_units, rate_per_unit, fixed_charge, tax_percent, subsidy_amount) VALUES ('Residential', 0, 100, 1.50, 85.00, 9.00, 100.00)");
        stmt.executeUpdate("INSERT INTO tariff_slabs (category, min_units, max_units, rate_per_unit, fixed_charge, tax_percent, subsidy_amount) VALUES ('Residential', 101, 300, 3.50, 85.00, 9.00, 0.00)");
        stmt.executeUpdate("INSERT INTO tariff_slabs (category, min_units, max_units, rate_per_unit, fixed_charge, tax_percent, subsidy_amount) VALUES ('Residential', 301, 500, 6.00, 85.00, 9.00, 0.00)");
        stmt.executeUpdate("INSERT INTO tariff_slabs (category, min_units, max_units, rate_per_unit, fixed_charge, tax_percent, subsidy_amount) VALUES ('Residential', 501, 999999, 8.50, 85.00, 9.00, 0.00)");

        stmt.executeUpdate("INSERT INTO tariff_slabs (category, min_units, max_units, rate_per_unit, fixed_charge, tax_percent, subsidy_amount) VALUES ('Commercial', 0, 200, 6.50, 250.00, 12.00, 0.00)");
        stmt.executeUpdate("INSERT INTO tariff_slabs (category, min_units, max_units, rate_per_unit, fixed_charge, tax_percent, subsidy_amount) VALUES ('Commercial', 201, 500, 8.50, 250.00, 12.00, 0.00)");
        stmt.executeUpdate("INSERT INTO tariff_slabs (category, min_units, max_units, rate_per_unit, fixed_charge, tax_percent, subsidy_amount) VALUES ('Commercial', 501, 999999, 11.00, 250.00, 12.00, 0.00)");

        stmt.executeUpdate("INSERT INTO tariff_slabs (category, min_units, max_units, rate_per_unit, fixed_charge, tax_percent, subsidy_amount) VALUES ('Industrial', 0, 1000, 8.00, 750.00, 15.00, 0.00)");
        stmt.executeUpdate("INSERT INTO tariff_slabs (category, min_units, max_units, rate_per_unit, fixed_charge, tax_percent, subsidy_amount) VALUES ('Industrial', 1001, 999999, 10.50, 750.00, 15.00, 0.00)");

        // Sample Customers
        stmt.executeUpdate("INSERT INTO customers (consumer_no, name, email, phone, address, category, discom_name, meter_no, connection_date) VALUES ('CA-IND-1001', 'Rahul Sharma', 'rahul.sharma@example.in', '+91 98765 43210', 'A-402, Gokuldham Society, Mumbai, MH', 'Residential', 'MSEDCL', 'MTR-88210', '2023-01-15')");
        stmt.executeUpdate("INSERT INTO customers (consumer_no, name, email, phone, address, category, discom_name, meter_no, connection_date) VALUES ('CA-IND-1002', 'Priya Patel', 'priya.p@example.in', '+91 98123 45678', 'Plot 12, Koramangala 4th Block, Bengaluru, KA', 'Residential', 'BESCOM', 'MTR-99412', '2023-03-20')");
        stmt.executeUpdate("INSERT INTO customers (consumer_no, name, email, phone, address, category, discom_name, meter_no, connection_date) VALUES ('CA-IND-1003', 'Sharma Electronics & Cafe', 'contact@sharmacafe.in', '+91 99000 11223', 'Shop 5, Connaught Place, New Delhi, DL', 'Commercial', 'TPDDL', 'MTR-77103', '2022-11-05')");
        stmt.executeUpdate("INSERT INTO customers (consumer_no, name, email, phone, address, category, discom_name, meter_no, connection_date) VALUES ('CA-IND-1004', 'Apex Auto Forge Ltd', 'accounts@apexautoforge.com', '+91 98450 99887', 'Industrial Area Phase 2, Pune, MH', 'Industrial', 'MSEDCL', 'MTR-33901', '2021-08-10')");
        stmt.executeUpdate("INSERT INTO customers (consumer_no, name, email, phone, address, category, discom_name, meter_no, connection_date) VALUES ('CA-IND-1005', 'Dr. Amitav Banerjee', 'dramitav@example.in', '+91 97333 44556', '7/1 Gariahat Road, Kolkata, WB', 'Residential', 'CESC', 'MTR-44519', '2024-02-01')");

        // Seed Readings & Bills
        stmt.executeUpdate("INSERT INTO meter_readings (customer_id, reading_date, prev_reading, curr_reading, units_consumed, reader_name) VALUES (1, '2026-06-01', 1200.00, 1380.00, 180.00, 'Sanjay Kumar')");
        stmt.executeUpdate("INSERT INTO meter_readings (customer_id, reading_date, prev_reading, curr_reading, units_consumed, reader_name) VALUES (1, '2026-07-01', 1380.00, 1575.00, 195.00, 'Sanjay Kumar')");
        stmt.executeUpdate("INSERT INTO meter_readings (customer_id, reading_date, prev_reading, curr_reading, units_consumed, reader_name) VALUES (1, '2026-08-01', 1575.00, 1815.00, 240.00, 'Sanjay Kumar')");

        stmt.executeUpdate("INSERT INTO bills (customer_id, reading_id, bill_date, due_date, units_consumed, energy_charge, fixed_charge, tax_amount, subsidy_amount, total_amount, payment_status, payment_date) VALUES (1, 1, '2026-06-02', '2026-06-20', 180.00, 430.00, 85.00, 46.35, 0.00, 561.35, 'PAID', '2026-06-15')");
        stmt.executeUpdate("INSERT INTO bills (customer_id, reading_id, bill_date, due_date, units_consumed, energy_charge, fixed_charge, tax_amount, subsidy_amount, total_amount, payment_status, payment_date) VALUES (1, 2, '2026-07-02', '2026-07-20', 195.00, 482.50, 85.00, 51.08, 0.00, 618.58, 'PAID', '2026-07-10')");
        stmt.executeUpdate("INSERT INTO bills (customer_id, reading_id, bill_date, due_date, units_consumed, energy_charge, fixed_charge, tax_amount, subsidy_amount, total_amount, payment_status) VALUES (1, 3, '2026-08-02', '2026-08-20', 240.00, 640.00, 85.00, 65.25, 0.00, 790.25, 'UNPAID')");

        stmt.executeUpdate("INSERT INTO meter_readings (customer_id, reading_date, prev_reading, curr_reading, units_consumed, reader_name) VALUES (2, '2026-08-01', 800.00, 890.00, 90.00, 'Ramesh V')");
        stmt.executeUpdate("INSERT INTO bills (customer_id, reading_id, bill_date, due_date, units_consumed, energy_charge, fixed_charge, tax_amount, subsidy_amount, total_amount, payment_status) VALUES (2, 4, '2026-08-02', '2026-08-20', 90.00, 135.00, 85.00, 19.80, 100.00, 139.80, 'UNPAID')");

        stmt.executeUpdate("INSERT INTO meter_readings (customer_id, reading_date, prev_reading, curr_reading, units_consumed, reader_name) VALUES (3, '2026-08-01', 4500.00, 4950.00, 450.00, 'Vikram Singh')");
        stmt.executeUpdate("INSERT INTO bills (customer_id, reading_id, bill_date, due_date, units_consumed, energy_charge, fixed_charge, tax_amount, subsidy_amount, total_amount, payment_status, payment_date) VALUES (3, 5, '2026-08-02', '2026-08-20', 450.00, 3425.00, 250.00, 441.00, 0.00, 4116.00, 'PAID', '2026-08-05')");

        stmt.executeUpdate("INSERT INTO meter_readings (customer_id, reading_date, prev_reading, curr_reading, units_consumed, reader_name) VALUES (4, '2026-08-01', 50000.00, 52800.00, 2800.00, 'Mahesh Shinde')");
        stmt.executeUpdate("INSERT INTO bills (customer_id, reading_id, bill_date, due_date, units_consumed, energy_charge, fixed_charge, tax_amount, subsidy_amount, total_amount, payment_status) VALUES (4, 6, '2026-08-02', '2026-08-20', 2800.00, 26900.00, 750.00, 4147.50, 0.00, 31797.50, 'UNPAID')");

        // Seed AI Insights
        stmt.executeUpdate("INSERT INTO ai_insights (customer_id, is_anomaly, severity, anomaly_reason, predicted_next_units, predicted_next_bill, optimization_tips) VALUES (1, 0, 'NONE', 'Normal consumption pattern consistent with 3-month seasonal trend.', 255.00, 843.50, 'Shift high power appliances like washing machines and EV chargers to off-peak hours (10:00 PM - 06:00 AM).')");
        stmt.executeUpdate("INSERT INTO ai_insights (customer_id, is_anomaly, severity, anomaly_reason, predicted_next_units, predicted_next_bill, optimization_tips) VALUES (2, 0, 'NONE', 'Consumption within subsidised band (<100 kWh). Excellent conservation.', 92.00, 142.00, 'Maintain usage under 100 units to retain ₹100 Government Electricity Subsidy benefit.')");
        stmt.executeUpdate("INSERT INTO ai_insights (customer_id, is_anomaly, severity, anomaly_reason, predicted_next_units, predicted_next_bill, optimization_tips) VALUES (4, 1, 'HIGH', 'Abnormal 65% surge in industrial energy load over baseline. Suspected power factor lag or machine leak.', 3100.00, 35200.00, 'Conduct immediate energy audit of heavy motor compressors. Check APFC panel capacitor bank health.')");

        System.out.println("Seed data successfully inserted!");
    }
}
