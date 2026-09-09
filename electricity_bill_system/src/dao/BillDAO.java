package dao;

import config.DBConnection;
import model.Bill;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class BillDAO {

    public List<Bill> getAllBills() {
        List<Bill> list = new ArrayList<>();
        String sql = "SELECT b.*, c.consumer_no, c.name AS customer_name, c.category, c.discom_name " +
                     "FROM bills b " +
                     "JOIN customers c ON b.customer_id = c.customer_id " +
                     "ORDER BY b.bill_id DESC";
        try (Connection conn = DBConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                list.add(mapResultSetToBill(rs));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }

    public List<Bill> getBillsByCustomer(int customerId) {
        List<Bill> list = new ArrayList<>();
        String sql = "SELECT b.*, c.consumer_no, c.name AS customer_name, c.category, c.discom_name " +
                     "FROM bills b " +
                     "JOIN customers c ON b.customer_id = c.customer_id " +
                     "WHERE b.customer_id = ? " +
                     "ORDER BY b.bill_date DESC";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, customerId);
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    list.add(mapResultSetToBill(rs));
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }

    public Bill getBillById(int billId) {
        String sql = "SELECT b.*, c.consumer_no, c.name AS customer_name, c.category, c.discom_name " +
                     "FROM bills b " +
                     "JOIN customers c ON b.customer_id = c.customer_id " +
                     "WHERE b.bill_id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, billId);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return mapResultSetToBill(rs);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    public boolean addBill(Bill b) {
        String sql = "INSERT INTO bills (customer_id, reading_id, bill_date, due_date, units_consumed, energy_charge, fixed_charge, tax_amount, subsidy_amount, total_amount, payment_status, payment_date) " +
                     "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            pstmt.setInt(1, b.getCustomerId());
            pstmt.setInt(2, b.getReadingId());
            pstmt.setString(3, b.getBillDate() != null ? b.getBillDate().toString() : new Date(System.currentTimeMillis()).toString());
            pstmt.setString(4, b.getDueDate() != null ? b.getDueDate().toString() : new Date(System.currentTimeMillis()).toString());
            pstmt.setDouble(5, b.getUnitsConsumed());
            pstmt.setDouble(6, b.getEnergyCharge());
            pstmt.setDouble(7, b.getFixedCharge());
            pstmt.setDouble(8, b.getTaxAmount());
            pstmt.setDouble(9, b.getSubsidyAmount());
            pstmt.setDouble(10, b.getTotalAmount());
            pstmt.setString(11, b.getPaymentStatus());
            pstmt.setString(12, b.getPaymentDate() != null ? b.getPaymentDate().toString() : null);

            int affected = pstmt.executeUpdate();
            if (affected > 0) {
                try (ResultSet keys = pstmt.getGeneratedKeys()) {
                    if (keys.next()) {
                        b.setBillId(keys.getInt(1));
                    }
                }
                return true;
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    public boolean markAsPaid(int billId, Date paymentDate) {
        String sql = "UPDATE bills SET payment_status = 'PAID', payment_date = ? WHERE bill_id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, paymentDate != null ? paymentDate.toString() : new Date(System.currentTimeMillis()).toString());
            pstmt.setInt(2, billId);
            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    public double getTotalRevenueCollected() {
        String sql = "SELECT SUM(total_amount) FROM bills WHERE payment_status = 'PAID'";
        try (Connection conn = DBConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            if (rs.next()) {
                return rs.getDouble(1);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return 0.0;
    }

    public int getPendingBillCount() {
        String sql = "SELECT COUNT(*) FROM bills WHERE payment_status = 'UNPAID' OR payment_status = 'OVERDUE'";
        try (Connection conn = DBConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            if (rs.next()) {
                return rs.getInt(1);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return 0;
    }

    private Bill mapResultSetToBill(ResultSet rs) throws SQLException {
        Bill b = new Bill();
        b.setBillId(rs.getInt("bill_id"));
        b.setCustomerId(rs.getInt("customer_id"));
        b.setReadingId(rs.getInt("reading_id"));
        try {
            String bDateStr = rs.getString("bill_date");
            if (bDateStr != null && bDateStr.length() >= 10) {
                b.setBillDate(Date.valueOf(bDateStr.substring(0, 10)));
            }
        } catch (Exception e) {
            b.setBillDate(new Date(System.currentTimeMillis()));
        }
        try {
            String dDateStr = rs.getString("due_date");
            if (dDateStr != null && dDateStr.length() >= 10) {
                b.setDueDate(Date.valueOf(dDateStr.substring(0, 10)));
            }
        } catch (Exception e) {
            b.setDueDate(new Date(System.currentTimeMillis()));
        }
        b.setUnitsConsumed(rs.getDouble("units_consumed"));
        b.setEnergyCharge(rs.getDouble("energy_charge"));
        b.setFixedCharge(rs.getDouble("fixed_charge"));
        b.setTaxAmount(rs.getDouble("tax_amount"));
        b.setSubsidyAmount(rs.getDouble("subsidy_amount"));
        b.setTotalAmount(rs.getDouble("total_amount"));
        b.setPaymentStatus(rs.getString("payment_status"));
        try {
            String pDateStr = rs.getString("payment_date");
            if (pDateStr != null && pDateStr.length() >= 10) {
                b.setPaymentDate(Date.valueOf(pDateStr.substring(0, 10)));
            }
        } catch (Exception ignored) {}

        try {
            b.setCreatedAt(rs.getTimestamp("created_at"));
            b.setConsumerNo(rs.getString("consumer_no"));
            b.setCustomerName(rs.getString("customer_name"));
            b.setCategory(rs.getString("category"));
            b.setDiscomName(rs.getString("discom_name"));
        } catch (Exception ignored) {}
        return b;
    }
}
