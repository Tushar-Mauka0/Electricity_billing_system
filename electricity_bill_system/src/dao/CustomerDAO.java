package dao;

import config.DBConnection;
import model.Customer;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class CustomerDAO {

    public List<Customer> getAllCustomers() {
        List<Customer> list = new ArrayList<>();
        String sql = "SELECT * FROM customers ORDER BY customer_id DESC";
        try (Connection conn = DBConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                list.add(mapResultSetToCustomer(rs));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }

    public Customer getCustomerById(int customerId) {
        String sql = "SELECT * FROM customers WHERE customer_id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, customerId);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return mapResultSetToCustomer(rs);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    public Customer getCustomerByConsumerNo(String consumerNo) {
        String sql = "SELECT * FROM customers WHERE consumer_no = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, consumerNo);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return mapResultSetToCustomer(rs);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    public boolean addCustomer(Customer c) {
        String sql = "INSERT INTO customers (consumer_no, name, email, phone, address, category, discom_name, meter_no, connection_date) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            pstmt.setString(1, c.getConsumerNo());
            pstmt.setString(2, c.getName());
            pstmt.setString(3, c.getEmail());
            pstmt.setString(4, c.getPhone());
            pstmt.setString(5, c.getAddress());
            pstmt.setString(6, c.getCategory());
            pstmt.setString(7, c.getDiscomName());
            pstmt.setString(8, c.getMeterNo());
            pstmt.setString(9, c.getConnectionDate() != null ? c.getConnectionDate().toString() : new Date(System.currentTimeMillis()).toString());

            int affected = pstmt.executeUpdate();
            if (affected > 0) {
                try (ResultSet keys = pstmt.getGeneratedKeys()) {
                    if (keys.next()) {
                        c.setCustomerId(keys.getInt(1));
                    }
                }
                return true;
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    public boolean updateCustomer(Customer c) {
        String sql = "UPDATE customers SET name = ?, email = ?, phone = ?, address = ?, category = ?, discom_name = ?, meter_no = ? WHERE customer_id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, c.getName());
            pstmt.setString(2, c.getEmail());
            pstmt.setString(3, c.getPhone());
            pstmt.setString(4, c.getAddress());
            pstmt.setString(5, c.getCategory());
            pstmt.setString(6, c.getDiscomName());
            pstmt.setString(7, c.getMeterNo());
            pstmt.setInt(8, c.getCustomerId());
            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    public boolean deleteCustomer(int customerId) {
        String sql = "DELETE FROM customers WHERE customer_id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, customerId);
            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    public int getTotalCustomerCount() {
        String sql = "SELECT COUNT(*) FROM customers";
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

    public String generateNextConsumerNo() {
        int count = getTotalCustomerCount() + 1001;
        return "CA-IND-" + count;
    }

    private Customer mapResultSetToCustomer(ResultSet rs) throws SQLException {
        Customer c = new Customer();
        c.setCustomerId(rs.getInt("customer_id"));
        c.setConsumerNo(rs.getString("consumer_no"));
        c.setName(rs.getString("name"));
        c.setEmail(rs.getString("email"));
        c.setPhone(rs.getString("phone"));
        c.setAddress(rs.getString("address"));
        c.setCategory(rs.getString("category"));
        c.setDiscomName(rs.getString("discom_name"));
        c.setMeterNo(rs.getString("meter_no"));
        try {
            String dtStr = rs.getString("connection_date");
            if (dtStr != null && dtStr.length() >= 10) {
                c.setConnectionDate(Date.valueOf(dtStr.substring(0, 10)));
            }
        } catch (Exception e) {
            c.setConnectionDate(new Date(System.currentTimeMillis()));
        }
        try {
            c.setCreatedAt(rs.getTimestamp("created_at"));
        } catch (Exception ignored) {}
        return c;
    }
}
