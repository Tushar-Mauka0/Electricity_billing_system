package dao;

import config.DBConnection;
import model.AIInsight;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class AIInsightDAO {

    public List<AIInsight> getAllInsights() {
        List<AIInsight> list = new ArrayList<>();
        String sql = "SELECT a.*, c.consumer_no, c.name AS customer_name, c.category " +
                     "FROM ai_insights a " +
                     "JOIN customers c ON a.customer_id = c.customer_id " +
                     "ORDER BY a.insight_id DESC";
        try (Connection conn = DBConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                list.add(mapResultSetToInsight(rs));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }

    public List<AIInsight> getAnomaliesOnly() {
        List<AIInsight> list = new ArrayList<>();
        String sql = "SELECT a.*, c.consumer_no, c.name AS customer_name, c.category " +
                     "FROM ai_insights a " +
                     "JOIN customers c ON a.customer_id = c.customer_id " +
                     "WHERE a.is_anomaly = 1 OR a.is_anomaly = true " +
                     "ORDER BY a.insight_id DESC";
        try (Connection conn = DBConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                list.add(mapResultSetToInsight(rs));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }

    public AIInsight getInsightByCustomer(int customerId) {
        String sql = "SELECT a.*, c.consumer_no, c.name AS customer_name, c.category " +
                     "FROM ai_insights a " +
                     "JOIN customers c ON a.customer_id = c.customer_id " +
                     "WHERE a.customer_id = ? " +
                     "ORDER BY a.insight_id DESC LIMIT 1";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, customerId);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return mapResultSetToInsight(rs);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    public boolean saveOrUpdateInsight(AIInsight insight) {
        AIInsight existing = getInsightByCustomer(insight.getCustomerId());
        if (existing != null) {
            String sql = "UPDATE ai_insights SET is_anomaly = ?, severity = ?, anomaly_reason = ?, predicted_next_units = ?, predicted_next_bill = ?, optimization_tips = ?, generated_at = CURRENT_TIMESTAMP WHERE insight_id = ?";
            try (Connection conn = DBConnection.getConnection();
                 PreparedStatement pstmt = conn.prepareStatement(sql)) {
                pstmt.setBoolean(1, insight.isAnomaly());
                pstmt.setString(2, insight.getSeverity());
                pstmt.setString(3, insight.getAnomalyReason());
                pstmt.setDouble(4, insight.getPredictedNextUnits());
                pstmt.setDouble(5, insight.getPredictedNextBill());
                pstmt.setString(6, insight.getOptimizationTips());
                pstmt.setInt(7, existing.getInsightId());
                return pstmt.executeUpdate() > 0;
            } catch (SQLException e) {
                e.printStackTrace();
            }
        } else {
            String sql = "INSERT INTO ai_insights (customer_id, is_anomaly, severity, anomaly_reason, predicted_next_units, predicted_next_bill, optimization_tips) VALUES (?, ?, ?, ?, ?, ?, ?)";
            try (Connection conn = DBConnection.getConnection();
                 PreparedStatement pstmt = conn.prepareStatement(sql)) {
                pstmt.setInt(1, insight.getCustomerId());
                pstmt.setBoolean(2, insight.isAnomaly());
                pstmt.setString(3, insight.getSeverity());
                pstmt.setString(4, insight.getAnomalyReason());
                pstmt.setDouble(5, insight.getPredictedNextUnits());
                pstmt.setDouble(6, insight.getPredictedNextBill());
                pstmt.setString(7, insight.getOptimizationTips());
                return pstmt.executeUpdate() > 0;
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
        return false;
    }

    public int getAnomalyCount() {
        String sql = "SELECT COUNT(*) FROM ai_insights WHERE is_anomaly = 1 OR is_anomaly = true";
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

    private AIInsight mapResultSetToInsight(ResultSet rs) throws SQLException {
        AIInsight a = new AIInsight();
        a.setInsightId(rs.getInt("insight_id"));
        a.setCustomerId(rs.getInt("customer_id"));
        a.setAnomaly(rs.getBoolean("is_anomaly"));
        a.setSeverity(rs.getString("severity"));
        a.setAnomalyReason(rs.getString("anomaly_reason"));
        a.setPredictedNextUnits(rs.getDouble("predicted_next_units"));
        a.setPredictedNextBill(rs.getDouble("predicted_next_bill"));
        a.setOptimizationTips(rs.getString("optimization_tips"));
        try {
            a.setGeneratedAt(rs.getTimestamp("generated_at"));
            a.setConsumerNo(rs.getString("consumer_no"));
            a.setCustomerName(rs.getString("customer_name"));
            a.setCategory(rs.getString("category"));
        } catch (Exception ignored) {}
        return a;
    }
}
