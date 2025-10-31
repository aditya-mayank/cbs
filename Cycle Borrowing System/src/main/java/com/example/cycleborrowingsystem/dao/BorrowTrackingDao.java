package com.example.cycleborrowingsystem.dao;

import com.example.cycleborrowingsystem.db.Database;
import com.example.cycleborrowingsystem.models.BorrowTracking;

import java.sql.*;

public class BorrowTrackingDao {
	public void createTableIfNotExists() throws SQLException {
		try (Connection c = Database.getInstance().getConnection(); Statement st = c.createStatement()) {
			st.executeUpdate("CREATE TABLE IF NOT EXISTS borrow_tracking (" +
				"cycle_id BIGINT PRIMARY KEY, " +
				"borrower_user_id BIGINT NOT NULL, " +
				"borrower_token VARCHAR(64) NULL, " +
				"lat DOUBLE NULL, " +
				"lon DOUBLE NULL, " +
				"updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP" +
			") ENGINE=InnoDB DEFAULT CHARSET=utf8mb4");
			try (Statement st2 = c.createStatement()) {
				st2.executeUpdate("ALTER TABLE borrow_tracking ADD COLUMN borrower_token VARCHAR(64) NULL");
			} catch (SQLException ignore) { /* column may already exist */ }
		}
	}

	public void upsert(long cycleId, long borrowerUserId, String borrowerToken, Double lat, Double lon) throws SQLException {
		String sql = "INSERT INTO borrow_tracking(cycle_id,borrower_user_id,borrower_token,lat,lon) VALUES(?,?,?,?,?) ON DUPLICATE KEY UPDATE borrower_user_id=VALUES(borrower_user_id), borrower_token=VALUES(borrower_token), lat=VALUES(lat), lon=VALUES(lon)";
		try (Connection c = Database.getInstance().getConnection(); PreparedStatement ps = c.prepareStatement(sql)) {
			ps.setLong(1, cycleId);
			ps.setLong(2, borrowerUserId);
			ps.setString(3, borrowerToken);
			if (lat == null) ps.setNull(4, Types.DOUBLE); else ps.setDouble(4, lat);
			if (lon == null) ps.setNull(5, Types.DOUBLE); else ps.setDouble(5, lon);
			ps.executeUpdate();
		}
	}

	public void upsert(long cycleId, long borrowerUserId, Double lat, Double lon) throws SQLException {
		String sql = "INSERT INTO borrow_tracking(cycle_id,borrower_user_id,lat,lon) VALUES(?,?,?,?) ON DUPLICATE KEY UPDATE borrower_user_id=VALUES(borrower_user_id), lat=VALUES(lat), lon=VALUES(lon)";
		try (Connection c = Database.getInstance().getConnection(); PreparedStatement ps = c.prepareStatement(sql)) {
			ps.setLong(1, cycleId);
			ps.setLong(2, borrowerUserId);
			if (lat == null) ps.setNull(3, Types.DOUBLE); else ps.setDouble(3, lat);
			if (lon == null) ps.setNull(4, Types.DOUBLE); else ps.setDouble(4, lon);
			ps.executeUpdate();
		}
	}

	public BorrowTracking get(long cycleId) throws SQLException {
		String sql = "SELECT cycle_id,borrower_user_id,borrower_token,lat,lon,updated_at FROM borrow_tracking WHERE cycle_id=?";
		try (Connection c = Database.getInstance().getConnection(); PreparedStatement ps = c.prepareStatement(sql)) {
			ps.setLong(1, cycleId);
			try (ResultSet rs = ps.executeQuery()) {
				if (rs.next()) {
					BorrowTracking t = new BorrowTracking();
					t.setCycleId(rs.getLong("cycle_id"));
					t.setBorrowerUserId(rs.getLong("borrower_user_id"));
					t.setBorrowerToken(rs.getString("borrower_token"));
					Object latObj = rs.getObject("lat");
					Object lonObj = rs.getObject("lon");
					t.setLat(latObj == null ? null : rs.getDouble("lat"));
					t.setLon(lonObj == null ? null : rs.getDouble("lon"));
					t.setUpdatedAt(rs.getTimestamp("updated_at"));
					return t;
				}
			}
		}
		return null;
	}
}
