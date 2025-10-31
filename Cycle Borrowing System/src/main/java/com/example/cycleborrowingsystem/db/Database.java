package com.example.cycleborrowingsystem.db;

import java.io.InputStream;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Properties;

public final class Database {
	private static volatile Database instance;
	private final String url;
	private final String user;
	private final String password;

	private Database() {
		Properties props = new Properties();
		try (InputStream in = Database.class.getClassLoader().getResourceAsStream("db.properties")) {
			if (in != null) props.load(in);
		} catch (Exception ignored) {}
		this.url = System.getProperty("DB_URL", props.getProperty("url", "jdbc:mysql://localhost:3306/cbs"));
		this.user = System.getProperty("DB_USER", props.getProperty("user", "root"));
		this.password = System.getProperty("DB_PASSWORD", props.getProperty("password", ""));
	}

	public static Database getInstance() {
		if (instance == null) {
			synchronized (Database.class) {
				if (instance == null) instance = new Database();
			}
		}
		return instance;
	}

	public Connection getConnection() throws SQLException {
		return DriverManager.getConnection(url, user, password);
	}
}
