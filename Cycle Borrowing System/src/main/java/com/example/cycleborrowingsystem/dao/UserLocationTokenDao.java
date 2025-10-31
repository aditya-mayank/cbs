package com.example.cycleborrowingsystem.dao;

import com.example.cycleborrowingsystem.db.Database;

import java.sql.*;
import java.util.UUID;

public class UserLocationTokenDao {
	public void createTableIfNotExists() throws SQLException {
		try (Connection c = Database.getInstance().getConnection(); Statement st = c.createStatement()) {
			st.executeUpdate("CREATE TABLE IF NOT EXISTS user_location_tokens (" +
				"user_id BIGINT PRIMARY KEY, " +
				"location_token VARCHAR(64) NOT NULL, " +
				"created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP, " +
				"FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE" +
			") ENGINE=InnoDB DEFAULT CHARSET=utf8mb4");
		}
	}

	public String getOrCreateToken(long userId) throws SQLException {
		String sql = "SELECT location_token FROM user_location_tokens WHERE user_id=?";
		try (Connection c = Database.getInstance().getConnection(); PreparedStatement ps = c.prepareStatement(sql)) {
			ps.setLong(1, userId);
			try (ResultSet rs = ps.executeQuery()) {
				if (rs.next()) return rs.getString("location_token");
			}
		}
		String token = UUID.randomUUID().toString().replace("-", "");
		String ins = "INSERT INTO user_location_tokens(user_id,location_token) VALUES(?,?) ON DUPLICATE KEY UPDATE location_token=VALUES(location_token)";
		try (Connection c = Database.getInstance().getConnection(); PreparedStatement ps = c.prepareStatement(ins)) {
			ps.setLong(1, userId);
			ps.setString(2, token);
			ps.executeUpdate();
		}
		return token;
	}

	public String getToken(long userId) throws SQLException {
		String sql = "SELECT location_token FROM user_location_tokens WHERE user_id=?";
		try (Connection c = Database.getInstance().getConnection(); PreparedStatement ps = c.prepareStatement(sql)) {
			ps.setLong(1, userId);
			try (ResultSet rs = ps.executeQuery()) {
				if (rs.next()) return rs.getString("location_token");
			}
		}
		return null;
	}
}

