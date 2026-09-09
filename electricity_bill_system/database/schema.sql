-- Electricity Bill Generation System Schema (MySQL Workbench Compatible)
-- Created for Indian Utility Tariffs (INR ₹, kWh Units)

CREATE DATABASE IF NOT EXISTS electricity_db;
USE electricity_db;

-- 1. Customers Table
CREATE TABLE IF NOT EXISTS customers (
    customer_id INT AUTO_INCREMENT PRIMARY KEY,
    consumer_no VARCHAR(50) UNIQUE NOT NULL,
    name VARCHAR(100) NOT NULL,
    email VARCHAR(100),
    phone VARCHAR(20),
    address TEXT,
    category VARCHAR(30) NOT NULL DEFAULT 'Residential', -- Residential, Commercial, Industrial
    discom_name VARCHAR(100) DEFAULT 'MSEDCL',         -- MSEDCL, BESCOM, TATA Power, TNEB, UPVCL
    meter_no VARCHAR(50) UNIQUE NOT NULL,
    connection_date DATE NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- 2. Tariff Slabs Table
CREATE TABLE IF NOT EXISTS tariff_slabs (
    slab_id INT AUTO_INCREMENT PRIMARY KEY,
    category VARCHAR(30) NOT NULL,
    min_units INT NOT NULL,
    max_units INT NOT NULL, -- 999999 for infinity
    rate_per_unit DECIMAL(10,2) NOT NULL,
    fixed_charge DECIMAL(10,2) NOT NULL DEFAULT 0.00,
    tax_percent DECIMAL(5,2) NOT NULL DEFAULT 0.00,
    subsidy_amount DECIMAL(10,2) NOT NULL DEFAULT 0.00
);

-- 3. Meter Readings Table
CREATE TABLE IF NOT EXISTS meter_readings (
    reading_id INT AUTO_INCREMENT PRIMARY KEY,
    customer_id INT NOT NULL,
    reading_date DATE NOT NULL,
    prev_reading DECIMAL(10,2) NOT NULL,
    curr_reading DECIMAL(10,2) NOT NULL,
    units_consumed DECIMAL(10,2) NOT NULL,
    reader_name VARCHAR(100) DEFAULT 'System Auto',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (customer_id) REFERENCES customers(customer_id) ON DELETE CASCADE
);

-- 4. Bills Table
CREATE TABLE IF NOT EXISTS bills (
    bill_id INT AUTO_INCREMENT PRIMARY KEY,
    customer_id INT NOT NULL,
    reading_id INT NOT NULL,
    bill_date DATE NOT NULL,
    due_date DATE NOT NULL,
    units_consumed DECIMAL(10,2) NOT NULL,
    energy_charge DECIMAL(10,2) NOT NULL,
    fixed_charge DECIMAL(10,2) NOT NULL,
    tax_amount DECIMAL(10,2) NOT NULL,
    subsidy_amount DECIMAL(10,2) NOT NULL,
    total_amount DECIMAL(10,2) NOT NULL,
    payment_status VARCHAR(20) NOT NULL DEFAULT 'UNPAID', -- UNPAID, PAID, OVERDUE
    payment_date DATE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (customer_id) REFERENCES customers(customer_id) ON DELETE CASCADE,
    FOREIGN KEY (reading_id) REFERENCES meter_readings(reading_id) ON DELETE CASCADE
);

-- 5. AI Insights & Anomalies Table
CREATE TABLE IF NOT EXISTS ai_insights (
    insight_id INT AUTO_INCREMENT PRIMARY KEY,
    customer_id INT NOT NULL,
    is_anomaly BOOLEAN DEFAULT FALSE,
    severity VARCHAR(20) DEFAULT 'NONE', -- NONE, LOW, MEDIUM, HIGH
    anomaly_reason TEXT,
    predicted_next_units DECIMAL(10,2) DEFAULT 0.00,
    predicted_next_bill DECIMAL(10,2) DEFAULT 0.00,
    optimization_tips TEXT,
    generated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (customer_id) REFERENCES customers(customer_id) ON DELETE CASCADE
);

-- ========================================================
-- SEED DATA (Indian Tariff Rates & Sample Consumers)
-- ========================================================

-- Clear existing data if re-running
DELETE FROM ai_insights;
DELETE FROM bills;
DELETE FROM meter_readings;
DELETE FROM customers;
DELETE FROM tariff_slabs;

-- Seed Tariff Slabs for Residential (INR ₹)
INSERT INTO tariff_slabs (category, min_units, max_units, rate_per_unit, fixed_charge, tax_percent, subsidy_amount) VALUES
('Residential', 0, 100, 1.50, 85.00, 9.00, 100.00),
('Residential', 101, 300, 3.50, 85.00, 9.00, 0.00),
('Residential', 301, 500, 6.00, 85.00, 9.00, 0.00),
('Residential', 501, 999999, 8.50, 85.00, 9.00, 0.00);

-- Seed Tariff Slabs for Commercial (INR ₹)
INSERT INTO tariff_slabs (category, min_units, max_units, rate_per_unit, fixed_charge, tax_percent, subsidy_amount) VALUES
('Commercial', 0, 200, 6.50, 250.00, 12.00, 0.00),
('Commercial', 201, 500, 8.50, 250.00, 12.00, 0.00),
('Commercial', 501, 999999, 11.00, 250.00, 12.00, 0.00);

-- Seed Tariff Slabs for Industrial (INR ₹)
INSERT INTO tariff_slabs (category, min_units, max_units, rate_per_unit, fixed_charge, tax_percent, subsidy_amount) VALUES
('Industrial', 0, 1000, 8.00, 750.00, 15.00, 0.00),
('Industrial', 1001, 999999, 10.50, 750.00, 15.00, 0.00);

-- Seed Sample Customers
INSERT INTO customers (customer_id, consumer_no, name, email, phone, address, category, discom_name, meter_no, connection_date) VALUES
(1, 'CA-IND-1001', 'Rahul Sharma', 'rahul.sharma@example.in', '+91 98765 43210', 'A-402, Gokuldham Society, Mumbai, MH', 'Residential', 'MSEDCL', 'MTR-88210', '2023-01-15'),
(2, 'CA-IND-1002', 'Priya Patel', 'priya.p@example.in', '+91 98123 45678', 'Plot 12, Koramangala 4th Block, Bengaluru, KA', 'Residential', 'BESCOM', 'MTR-99412', '2023-03-20'),
(3, 'CA-IND-1003', 'Sharma Electronics & Cafe', 'contact@sharmacafe.in', '+91 99000 11223', 'Shop 5, Connaught Place, New Delhi, DL', 'Commercial', 'TPDDL', 'MTR-77103', '2022-11-05'),
(4, 'CA-IND-1004', 'Apex Auto Forge Ltd', 'accounts@apexautoforge.com', '+91 98450 99887', 'Industrial Area Phase 2, Pune, MH', 'Industrial', 'MSEDCL', 'MTR-33901', '2021-08-10'),
(5, 'CA-IND-1005', 'Dr. Amitav Banerjee', 'dramitav@example.in', '+91 97333 44556', '7/1 Gariahat Road, Kolkata, WB', 'Residential', 'CESC', 'MTR-44519', '2024-02-01');

-- Seed Historical Readings & Bills for Rahul Sharma (CA-IND-1001)
INSERT INTO meter_readings (reading_id, customer_id, reading_date, prev_reading, curr_reading, units_consumed, reader_name) VALUES
(1, 1, '2026-06-01', 1200.00, 1380.00, 180.00, 'Sanjay Kumar'),
(2, 1, '2026-07-01', 1380.00, 1575.00, 195.00, 'Sanjay Kumar'),
(3, 1, '2026-08-01', 1575.00, 1815.00, 240.00, 'Sanjay Kumar');

INSERT INTO bills (bill_id, customer_id, reading_id, bill_date, due_date, units_consumed, energy_charge, fixed_charge, tax_amount, subsidy_amount, total_amount, payment_status, payment_date) VALUES
(1, 1, 1, '2026-06-02', '2026-06-20', 180.00, 430.00, 85.00, 46.35, 0.00, 561.35, 'PAID', '2026-06-15'),
(2, 1, 2, '2026-07-02', '2026-07-20', 195.00, 482.50, 85.00, 51.08, 0.00, 618.58, 'PAID', '2026-07-10'),
(3, 1, 3, '2026-08-02', '2026-08-20', 240.00, 640.00, 85.00, 65.25, 0.00, 790.25, 'UNPAID', NULL);

-- Seed Meter Readings & Bills for Priya Patel (CA-IND-1002) - Under 100 units with Subsidy
INSERT INTO meter_readings (reading_id, customer_id, reading_date, prev_reading, curr_reading, units_consumed, reader_name) VALUES
(4, 2, '2026-08-01', 800.00, 890.00, 90.00, 'Ramesh V');

INSERT INTO bills (bill_id, customer_id, reading_id, bill_date, due_date, units_consumed, energy_charge, fixed_charge, tax_amount, subsidy_amount, total_amount, payment_status, payment_date) VALUES
(4, 2, 4, '2026-08-02', '2026-08-20', 90.00, 135.00, 85.00, 19.80, 100.00, 139.80, 'UNPAID', NULL);

-- Seed Commercial Reading & Bill for Sharma Electronics (CA-IND-1003)
INSERT INTO meter_readings (reading_id, customer_id, reading_date, prev_reading, curr_reading, units_consumed, reader_name) VALUES
(5, 3, '2026-08-01', 4500.00, 4950.00, 450.00, 'Vikram Singh');

INSERT INTO bills (bill_id, customer_id, reading_id, bill_date, due_date, units_consumed, energy_charge, fixed_charge, tax_amount, subsidy_amount, total_amount, payment_status, payment_date) VALUES
(5, 3, 5, '2026-08-02', '2026-08-20', 450.00, 3425.00, 250.00, 441.00, 0.00, 4116.00, 'PAID', '2026-08-05');

-- Seed Industrial Reading & Bill for Apex Auto Forge (CA-IND-1004) - High Consumption Anomaly Sample
INSERT INTO meter_readings (reading_id, customer_id, reading_date, prev_reading, curr_reading, units_consumed, reader_name) VALUES
(6, 4, '2026-08-01', 50000.00, 52800.00, 2800.00, 'Mahesh Shinde');

INSERT INTO bills (bill_id, customer_id, reading_id, bill_date, due_date, units_consumed, energy_charge, fixed_charge, tax_amount, subsidy_amount, total_amount, payment_status, payment_date) VALUES
(6, 4, 6, '2026-08-02', '2026-08-20', 2800.00, 26900.00, 750.00, 4147.50, 0.00, 31797.50, 'UNPAID', NULL);

-- Seed AI Insights
INSERT INTO ai_insights (customer_id, is_anomaly, severity, anomaly_reason, predicted_next_units, predicted_next_bill, optimization_tips) VALUES
(1, FALSE, 'NONE', 'Normal consumption pattern consistent with 3-month seasonal trend.', 255.00, 843.50, 'Shift high power appliances like washing machines and EV chargers to off-peak hours (10:00 PM - 06:00 AM).'),
(2, FALSE, 'NONE', 'Consumption within subsidised band (<100 kWh). Excellent conservation.', 92.00, 142.00, 'Maintain usage under 100 units to retain ₹100 Government Electricity Subsidy benefit.'),
(4, TRUE, 'HIGH', 'Abnormal 65% surge in industrial energy load over baseline. Suspected power factor lag or machine leak.', 3100.00, 35200.00, 'Conduct immediate energy audit of heavy motor compressors. Check APFC panel capacitor bank health.');
