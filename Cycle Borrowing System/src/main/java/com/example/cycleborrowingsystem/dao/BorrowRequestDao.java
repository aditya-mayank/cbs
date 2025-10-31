package com.example.cycleborrowingsystem.dao;

import com.example.cycleborrowingsystem.db.Database;
import com.example.cycleborrowingsystem.models.BorrowRequest;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class BorrowRequestDao {
	public void createTableIfNotExists() throws SQLException {
		try (Connection c = Database.getInstance().getConnection(); Statement st = c.createStatement()) {
			st.executeUpdate("CREATE TABLE IF NOT EXISTS borrow_requests (" +
				"id BIGINT PRIMARY KEY AUTO_INCREMENT, " +
				"cycle_id BIGINT NOT NULL, " +
				"owner_email VARCHAR(120) NOT NULL, " +
				"borrower_user_id BIGINT NOT NULL, " +
				"status ENUM('PENDING','ACCEPTED','DECLINED') NOT NULL DEFAULT 'PENDING', " +
				"created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP, " +
				"INDEX (owner_email), INDEX (borrower_user_id), INDEX (cycle_id)" +
			") ENGINE=InnoDB DEFAULT CHARSET=utf8mb4");
		}
	}

	public long create(long cycleId, String ownerEmail, long borrowerUserId) throws SQLException {
		String sql = "INSERT INTO borrow_requests(cycle_id,owner_email,borrower_user_id,status) VALUES(?,?,?,'PENDING')";
		try (Connection c = Database.getInstance().getConnection();
			 PreparedStatement ps = c.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
			ps.setLong(1, cycleId);
			ps.setString(2, ownerEmail);
			ps.setLong(3, borrowerUserId);
			ps.executeUpdate();
			try (ResultSet rs = ps.getGeneratedKeys()) { if (rs.next()) return rs.getLong(1); }
		}
		return -1L;
	}

	public List<BorrowRequest> listIncomingForOwner(String ownerEmail) throws SQLException {
		String sql = "SELECT id,cycle_id,owner_email,borrower_user_id,status,created_at FROM borrow_requests WHERE owner_email=? AND status='PENDING' ORDER BY created_at DESC";
		try (Connection c = Database.getInstance().getConnection(); PreparedStatement ps = c.prepareStatement(sql)) {
			ps.setString(1, ownerEmail);
			try (ResultSet rs = ps.executeQuery()) {
				List<BorrowRequest> list = new ArrayList<>();
				while (rs.next()) list.add(map(rs));
				return list;
			}
		}
	}

	public List<BorrowRequest> listOutgoingForUser(long borrowerUserId) throws SQLException {
		String sql = "SELECT id,cycle_id,owner_email,borrower_user_id,status,created_at FROM borrow_requests WHERE borrower_user_id=? ORDER BY created_at DESC";
		try (Connection c = Database.getInstance().getConnection(); PreparedStatement ps = c.prepareStatement(sql)) {
			ps.setLong(1, borrowerUserId);
			try (ResultSet rs = ps.executeQuery()) {
				List<BorrowRequest> list = new ArrayList<>();
				while (rs.next()) list.add(map(rs));
				return list;
			}
		}
	}

	public boolean setStatus(long requestId, String status) throws SQLException {
		String sql = "UPDATE borrow_requests SET status=? WHERE id=?";
		try (Connection c = Database.getInstance().getConnection(); PreparedStatement ps = c.prepareStatement(sql)) {
			ps.setString(1, status);
			ps.setLong(2, requestId);
			return ps.executeUpdate() == 1;
		}
	}

	private BorrowRequest map(ResultSet rs) throws SQLException {
		BorrowRequest b = new BorrowRequest();
		b.setId(rs.getLong("id"));
		b.setCycleId(rs.getLong("cycle_id"));
		b.setOwnerEmail(rs.getString("owner_email"));
		b.setBorrowerUserId(rs.getLong("borrower_user_id"));
		b.setStatus(rs.getString("status"));
		b.setCreatedAt(rs.getTimestamp("created_at"));
		return b;
	}
}
