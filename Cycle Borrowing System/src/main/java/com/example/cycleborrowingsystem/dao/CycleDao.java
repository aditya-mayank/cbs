package com.example.cycleborrowingsystem.dao;

import com.example.cycleborrowingsystem.db.Database;
import com.example.cycleborrowingsystem.models.Cycle;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class CycleDao {
	public void createTableIfNotExists() throws SQLException {
		try (Connection c = Database.getInstance().getConnection();
			 Statement st = c.createStatement()) {
			st.executeUpdate("CREATE TABLE IF NOT EXISTS cycles (" +
				"id BIGINT PRIMARY KEY AUTO_INCREMENT, " +
				"model VARCHAR(120) NOT NULL, " +
				"owner_email VARCHAR(120) NOT NULL, " +
				"lat DOUBLE NULL, " +
				"lon DOUBLE NULL, " +
				"borrowed_by_user_id BIGINT NULL, " +
				"INDEX (owner_email)" +
			") ENGINE=InnoDB DEFAULT CHARSET=utf8mb4");
		}
	}

	public List<Cycle> listAvailable() throws SQLException {
		String sql = "SELECT id,model,owner_email,lat,lon,borrowed_by_user_id FROM cycles WHERE borrowed_by_user_id IS NULL";
		try (Connection c = Database.getInstance().getConnection();
			 PreparedStatement ps = c.prepareStatement(sql);
			 ResultSet rs = ps.executeQuery()) {
			List<Cycle> list = new ArrayList<>();
			while (rs.next()) list.add(map(rs));
			return list;
		}
	}

	public List<Cycle> listByOwner(String ownerEmail) throws SQLException {
		String sql = "SELECT id,model,owner_email,lat,lon,borrowed_by_user_id FROM cycles WHERE owner_email=?";
		try (Connection c = Database.getInstance().getConnection();
			 PreparedStatement ps = c.prepareStatement(sql)) {
			ps.setString(1, ownerEmail);
			try (ResultSet rs = ps.executeQuery()) {
				List<Cycle> list = new ArrayList<>();
				while (rs.next()) list.add(map(rs));
				return list;
			}
		}
	}

	public List<Cycle> listBorrowedByUser(long userId) throws SQLException {
		String sql = "SELECT id,model,owner_email,lat,lon,borrowed_by_user_id FROM cycles WHERE borrowed_by_user_id=?";
		try (Connection c = Database.getInstance().getConnection();
			 PreparedStatement ps = c.prepareStatement(sql)) {
			ps.setLong(1, userId);
			try (ResultSet rs = ps.executeQuery()) {
				List<Cycle> list = new ArrayList<>();
				while (rs.next()) list.add(map(rs));
				return list;
			}
		}
	}

	public boolean borrow(long cycleId, long userId) throws SQLException {
		String sql = "UPDATE cycles SET borrowed_by_user_id=? WHERE id=? AND borrowed_by_user_id IS NULL";
		try (Connection c = Database.getInstance().getConnection();
			 PreparedStatement ps = c.prepareStatement(sql)) {
			ps.setLong(1, userId);
			ps.setLong(2, cycleId);
			return ps.executeUpdate() == 1;
		}
	}

	public boolean returnCycle(long cycleId, long userId) throws SQLException {
		String sql = "UPDATE cycles SET borrowed_by_user_id=NULL WHERE id=? AND borrowed_by_user_id=?";
		try (Connection c = Database.getInstance().getConnection();
			 PreparedStatement ps = c.prepareStatement(sql)) {
			ps.setLong(1, cycleId);
			ps.setLong(2, userId);
			return ps.executeUpdate() == 1;
		}
	}

	public long addCycle(Cycle cycle) throws SQLException {
		String sql = "INSERT INTO cycles(model,owner_email,lat,lon,borrowed_by_user_id) VALUES(?,?,?,?,NULL)";
		try (Connection c = Database.getInstance().getConnection();
			 PreparedStatement ps = c.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
			ps.setString(1, cycle.getModel());
			ps.setString(2, cycle.getOwnerEmail());
			if (cycle.getLat() == null) ps.setNull(3, Types.DOUBLE); else ps.setDouble(3, cycle.getLat());
			if (cycle.getLon() == null) ps.setNull(4, Types.DOUBLE); else ps.setDouble(4, cycle.getLon());
			ps.executeUpdate();
			try (ResultSet rs = ps.getGeneratedKeys()) {
				if (rs.next()) return rs.getLong(1);
			}
		}
		return -1L;
	}

	public boolean deleteOwned(long cycleId, String ownerEmail) throws SQLException {
		String sql = "DELETE FROM cycles WHERE id=? AND owner_email=? AND borrowed_by_user_id IS NULL";
		try (Connection c = Database.getInstance().getConnection();
			 PreparedStatement ps = c.prepareStatement(sql)) {
			ps.setLong(1, cycleId);
			ps.setString(2, ownerEmail);
			return ps.executeUpdate() == 1;
		}
	}

	// Admin
	public List<Cycle> listAll() throws SQLException {
		String sql = "SELECT id,model,owner_email,lat,lon,borrowed_by_user_id FROM cycles ORDER BY id DESC";
		try (Connection c = Database.getInstance().getConnection();
			 PreparedStatement ps = c.prepareStatement(sql);
			 ResultSet rs = ps.executeQuery()) {
			List<Cycle> list = new ArrayList<>();
			while (rs.next()) list.add(map(rs));
			return list;
		}
	}

	public boolean adminDelete(long cycleId) throws SQLException {
		String sql = "DELETE FROM cycles WHERE id=?";
		try (Connection c = Database.getInstance().getConnection();
			 PreparedStatement ps = c.prepareStatement(sql)) {
			ps.setLong(1, cycleId);
			return ps.executeUpdate() == 1;
		}
	}

	public boolean adminForceReturn(long cycleId) throws SQLException {
		String sql = "UPDATE cycles SET borrowed_by_user_id=NULL WHERE id=?";
		try (Connection c = Database.getInstance().getConnection();
			 PreparedStatement ps = c.prepareStatement(sql)) {
			ps.setLong(1, cycleId);
			return ps.executeUpdate() == 1;
		}
	}

	private Cycle map(ResultSet rs) throws SQLException {
		Cycle c = new Cycle();
		c.setId(rs.getLong("id"));
		c.setModel(rs.getString("model"));
		c.setOwnerEmail(rs.getString("owner_email"));
		Object latObj = rs.getObject("lat");
		Object lonObj = rs.getObject("lon");
		c.setLat(latObj == null ? null : rs.getDouble("lat"));
		c.setLon(lonObj == null ? null : rs.getDouble("lon"));
		Object b = rs.getObject("borrowed_by_user_id");
		c.setBorrowedByUserId(b == null ? null : rs.getLong("borrowed_by_user_id"));
		return c;
	}
}
