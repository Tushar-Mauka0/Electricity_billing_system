package dao;

import config.DBConnection;
import model.SlabRate;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class SlabRateDAO {

    public List<SlabRate> getAllSlabRates() {
        List<SlabRate> list = new ArrayList<>();
        String sql = "SELECT * FROM tariff_slabs ORDER BY category ASC, min_units ASC";
        try (Connection conn = DBConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                list.add(mapResultSetToSlabRate(rs));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }

    public List<SlabRate> getSlabRatesByCategory(String category) {
        List<SlabRate> list = new ArrayList<>();
        String sql = "SELECT * FROM tariff_slabs WHERE category = ? ORDER BY min_units ASC";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, category);
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    list.add(mapResultSetToSlabRate(rs));
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }

    public boolean addSlabRate(SlabRate s) {
        String sql = "INSERT INTO tariff_slabs (category, min_units, max_units, rate_per_unit, fixed_charge, tax_percent, subsidy_amount) VALUES (?, ?, ?, ?, ?, ?, ?)";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            pstmt.setString(1, s.getCategory());
            pstmt.setInt(2, s.getMinUnits());
            pstmt.setInt(3, s.getMaxUnits());
            pstmt.setDouble(4, s.getRatePerUnit());
            pstmt.setDouble(5, s.getFixedCharge());
            pstmt.setDouble(6, s.getTaxPercent());
            pstmt.setDouble(7, s.getSubsidyAmount());

            int affected = pstmt.executeUpdate();
            if (affected > 0) {
                try (ResultSet keys = pstmt.getGeneratedKeys()) {
                    if (keys.next()) {
                        s.setSlabId(keys.getInt(1));
                    }
                }
                return true;
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    public boolean updateSlabRate(SlabRate s) {
        String sql = "UPDATE tariff_slabs SET category = ?, min_units = ?, max_units = ?, rate_per_unit = ?, fixed_charge = ?, tax_percent = ?, subsidy_amount = ? WHERE slab_id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, s.getCategory());
            pstmt.setInt(2, s.getMinUnits());
            pstmt.setInt(3, s.getMaxUnits());
            pstmt.setDouble(4, s.getRatePerUnit());
            pstmt.setDouble(5, s.getFixedCharge());
            pstmt.setDouble(6, s.getTaxPercent());
            pstmt.setDouble(7, s.getSubsidyAmount());
            pstmt.setInt(8, s.getSlabId());

            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    public boolean deleteSlabRate(int slabId) {
        String sql = "DELETE FROM tariff_slabs WHERE slab_id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, slabId);
            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    private SlabRate mapResultSetToSlabRate(ResultSet rs) throws SQLException {
        SlabRate s = new SlabRate();
        s.setSlabId(rs.getInt("slab_id"));
        s.setCategory(rs.getString("category"));
        s.setMinUnits(rs.getInt("min_units"));
        s.setMaxUnits(rs.getInt("max_units"));
        s.setRatePerUnit(rs.getDouble("rate_per_unit"));
        s.setFixedCharge(rs.getDouble("fixed_charge"));
        s.setTaxPercent(rs.getDouble("tax_percent"));
        s.setSubsidyAmount(rs.getDouble("subsidy_amount"));
        return s;
    }
}
