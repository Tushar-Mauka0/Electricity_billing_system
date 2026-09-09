package dao;

import config.DBConnection;
import model.MeterReading;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class MeterReadingDAO {

    public List<MeterReading> getAllReadings() {
        List<MeterReading> list = new ArrayList<>();
        String sql = "SELECT m.*, c.name AS consumer_name, c.consumer_no " +
                     "FROM meter_readings m " +
                     "JOIN customers c ON m.customer_id = c.customer_id " +
                     "ORDER BY m.reading_id DESC";
        try (Connection conn = DBConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                list.add(mapResultSetToMeterReading(rs));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }

    public List<MeterReading> getReadingsByCustomer(int customerId) {
        List<MeterReading> list = new ArrayList<>();
        String sql = "SELECT m.*, c.name AS consumer_name, c.consumer_no " +
                     "FROM meter_readings m " +
                     "JOIN customers c ON m.customer_id = c.customer_id " +
                     "WHERE m.customer_id = ? " +
                     "ORDER BY m.reading_date ASC";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, customerId);
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    list.add(mapResultSetToMeterReading(rs));
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }

    public MeterReading getLatestReadingForCustomer(int customerId) {
        String sql = "SELECT m.*, c.name AS consumer_name, c.consumer_no " +
                     "FROM meter_readings m " +
                     "JOIN customers c ON m.customer_id = c.customer_id " +
                     "WHERE m.customer_id = ? " +
                     "ORDER BY m.reading_date DESC, m.reading_id DESC LIMIT 1";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, customerId);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return mapResultSetToMeterReading(rs);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    public boolean addReading(MeterReading m) {
        String sql = "INSERT INTO meter_readings (customer_id, reading_date, prev_reading, curr_reading, units_consumed, reader_name) VALUES (?, ?, ?, ?, ?, ?)";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            pstmt.setInt(1, m.getCustomerId());
            pstmt.setString(2, m.getReadingDate() != null ? m.getReadingDate().toString() : new Date(System.currentTimeMillis()).toString());
            pstmt.setDouble(3, m.getPrevReading());
            pstmt.setDouble(4, m.getCurrReading());
            pstmt.setDouble(5, m.getUnitsConsumed());
            pstmt.setString(6, m.getReaderName());

            int affected = pstmt.executeUpdate();
            if (affected > 0) {
                try (ResultSet keys = pstmt.getGeneratedKeys()) {
                    if (keys.next()) {
                        m.setReadingId(keys.getInt(1));
                    }
                }
                return true;
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    private MeterReading mapResultSetToMeterReading(ResultSet rs) throws SQLException {
        MeterReading m = new MeterReading();
        m.setReadingId(rs.getInt("reading_id"));
        m.setCustomerId(rs.getInt("customer_id"));
        try {
            String rDateStr = rs.getString("reading_date");
            if (rDateStr != null && rDateStr.length() >= 10) {
                m.setReadingDate(Date.valueOf(rDateStr.substring(0, 10)));
            }
        } catch (Exception e) {
            m.setReadingDate(new Date(System.currentTimeMillis()));
        }
        m.setPrevReading(rs.getDouble("prev_reading"));
        m.setCurrReading(rs.getDouble("curr_reading"));
        m.setUnitsConsumed(rs.getDouble("units_consumed"));
        m.setReaderName(rs.getString("reader_name"));
        try {
            m.setCreatedAt(rs.getTimestamp("created_at"));
            m.setConsumerName(rs.getString("consumer_name"));
            m.setConsumerNo(rs.getString("consumer_no"));
        } catch (Exception ignored) {}
        return m;
    }
}
