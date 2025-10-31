package com.example.cycleborrowingsystem.controllers;

import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;

import com.example.cycleborrowingsystem.dao.PasswordUtil;
import com.example.cycleborrowingsystem.dao.UserDao;
import com.example.cycleborrowingsystem.dao.UserLocationTokenDao;
import com.example.cycleborrowingsystem.models.User;

public class UserSignupController {
	@FXML private TextField nameField;
	@FXML private TextField emailField;
	@FXML private PasswordField passwordField;

	@FXML
	private void onSignup() {
		String name = nameField.getText();
		String email = emailField.getText();
		String pass = passwordField.getText();
		if (name == null || name.isBlank() || email == null || email.isBlank() || pass == null || pass.isBlank()) {
			new Alert(Alert.AlertType.ERROR, "All fields are required").show();
			return;
		}
		try {
			UserDao userDao = new UserDao();
			userDao.createTableIfNotExists();
			User existing = userDao.findByEmail(email);
			if (existing != null) {
				new Alert(Alert.AlertType.WARNING, "Email already registered").show();
				return;
			}
			User u = new User();
			u.setName(name);
			u.setEmail(email);
			u.setPasswordHash(PasswordUtil.sha256(pass));
			long id = userDao.insert(u);
			if (id > 0) {
				UserLocationTokenDao tokenDao = new UserLocationTokenDao();
				tokenDao.createTableIfNotExists();
				tokenDao.getOrCreateToken(id);
			}
			new Alert(Alert.AlertType.INFORMATION, id > 0 ? "Signup successful. Please log in." : "Signup failed").show();
			if (id > 0) {
				try {
					com.example.cycleborrowingsystem.SceneManager.switchScene("/com/example/cycleborrowingsystem/login.fxml");
				} catch (Exception ignored) {}
			}
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
