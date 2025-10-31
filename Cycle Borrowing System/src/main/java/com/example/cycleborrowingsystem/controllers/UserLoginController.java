package com.example.cycleborrowingsystem.controllers;

import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;

import com.example.cycleborrowingsystem.SceneManager;
import com.example.cycleborrowingsystem.Session;
import com.example.cycleborrowingsystem.dao.PasswordUtil;
import com.example.cycleborrowingsystem.dao.UserDao;
import com.example.cycleborrowingsystem.dao.UserLocationTokenDao;
import com.example.cycleborrowingsystem.models.User;

public class UserLoginController {
	@FXML private TextField emailField;
	@FXML private PasswordField passwordField;

	@FXML
	private void onLogin() {
		String email = emailField.getText();
		String pass = passwordField.getText();
		if (email == null || email.isBlank() || pass == null || pass.isBlank()) {
			new Alert(Alert.AlertType.ERROR, "Enter email and password").show();
			return;
		}
		try {
			UserDao userDao = new UserDao();
			userDao.createTableIfNotExists();
			User u = userDao.findByEmail(email);
			if (u == null) {
				new Alert(Alert.AlertType.ERROR, "No user found").show();
				return;
			}
			String hash = PasswordUtil.sha256(pass);
			if (!hash.equals(u.getPasswordHash())) {
				new Alert(Alert.AlertType.ERROR, "Invalid credentials").show();
				return;
			}
			UserLocationTokenDao tokenDao = new UserLocationTokenDao();
			tokenDao.createTableIfNotExists();
			tokenDao.getOrCreateToken(u.getId());
			Session.setCurrentUser(u);
			try {
				SceneManager.switchScene("/com/example/cycleborrowingsystem/cycles.fxml");
			} catch (Exception ignored) {}
		} catch (Exception ex) {
			new Alert(Alert.AlertType.ERROR, "Database error: " + ex.getMessage()).show();
		}
	}

	@FXML
	private void onBack() {
		try {
			com.example.cycleborrowingsystem.SceneManager.switchScene("/com/example/cycleborrowingsystem/landing.fxml");
		} catch (Exception ignored) {}
	}
}
