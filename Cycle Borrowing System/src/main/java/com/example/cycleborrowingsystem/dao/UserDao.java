package com.example.cycleborrowingsystem.dao;

import com.example.cycleborrowingsystem.db.Database;
import com.example.cycleborrowingsystem.models.User;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class UserDao {
	public void createTableIfNotExists() throws SQLException {
		try (Connection c = Database.getInstance().getConnection();
			 Statement st = c.createStatement()) {
			st.executeUpdate("CREATE TABLE IF NOT EXISTS users (" +
				"id BIGINT PRIMARY KEY AUTO_INCREMENT, " +
				"name VARCHAR(100) NOT NULL, " +
				"email VARCHAR(120) NOT NULL UNIQUE, " +
				"password_hash VARCHAR(64) NOT NULL" +
			") ENGINE=InnoDB DEFAULT CHARSET=utf8mb4");
		}
	}

	public long insert(User u) throws SQLException {
		String sql = "INSERT INTO users(name,email,password_hash) VALUES(?,?,?)";
		try (Connection c = Database.getInstance().getConnection();
			 PreparedStatement ps = c.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
			ps.setString(1, u.getName());
			ps.setString(2, u.getEmail());
			ps.setString(3, u.getPasswordHash());
			ps.executeUpdate();
			try (ResultSet rs = ps.getGeneratedKeys()) {
				if (rs.next()) return rs.getLong(1);
			}
		}
		return -1L;
	}

	public User findByEmail(String email) throws SQLException {
		String sql = "SELECT id,name,email,password_hash FROM users WHERE email=?";
		try (Connection c = Database.getInstance().getConnection();
			 PreparedStatement ps = c.prepareStatement(sql)) {
			ps.setString(1, email);
			try (ResultSet rs = ps.executeQuery()) {
				if (rs.next()) {
					User u = new User();
					u.setId(rs.getLong("id"));
					u.setName(rs.getString("name"));
					u.setEmail(rs.getString("email"));
					u.setPasswordHash(rs.getString("password_hash"));
					return u;
				}
			}
		}
		return null;
	}

	public List<User> listAll() throws SQLException {
		String sql = "SELECT id,name,email,password_hash FROM users ORDER BY id DESC";
		try (Connection c = Database.getInstance().getConnection();
			 PreparedStatement ps = c.prepareStatement(sql);
			 ResultSet rs = ps.executeQuery()) {
			List<User> list = new ArrayList<>();
			while (rs.next()) {
				User u = new User();
				u.setId(rs.getLong("id"));
				u.setName(rs.getString("name"));
				u.setEmail(rs.getString("email"));
				u.setPasswordHash(rs.getString("password_hash"));
				list.add(u);
			}
			return list;
		}
	}

	public boolean deleteById(long userId) throws SQLException {
		String sql = "DELETE FROM users WHERE id=?";
		try (Connection c = Database.getInstance().getConnection();
			 PreparedStatement ps = c.prepareStatement(sql)) {
			ps.setLong(1, userId);
			return ps.executeUpdate() == 1;
		}
	}
}
