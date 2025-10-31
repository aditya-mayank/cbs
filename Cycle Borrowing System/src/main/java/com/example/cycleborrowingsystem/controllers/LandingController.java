package com.example.cycleborrowingsystem.controllers;

import com.example.cycleborrowingsystem.net.LandingServer;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.input.Clipboard;
import javafx.scene.input.ClipboardContent;

import java.awt.Desktop;
import java.net.URI;
import java.net.URL;
import java.util.ResourceBundle;

public class LandingController implements Initializable {
    @FXML private Button adminPanelBtn;
    @FXML private Button userPanelBtn;
    @FXML private TextField pairingField;
    @FXML private Button copyBtn;
    @FXML private Button openBtn;
    @FXML private Label locationStatusLabel;
    @FXML private TextField manualLocationField;
    @FXML private Button loginButton;
    @FXML private Button signupButton;

    private LandingServer landingServer;
    private final int serverPort = 8765;
    public static final String ADMIN_ID = "admin@cbs.local";
    public static final String ADMIN_PASSWORD = "Admin@1234";

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        pairingField.setEditable(false);
        pairingField.setOnMouseClicked(e -> pairingField.selectAll());
        copyBtn.setOnAction(e -> copyPairing());
        openBtn.setOnAction(e -> openPairing());
        adminPanelBtn.setOnAction(e -> openAdminLogin());
        userPanelBtn.setOnAction(e -> openUserLogin());
        loginButton.setOnAction(e -> openUserLogin());
        signupButton.setOnAction(e -> openUserSignup());
        startServer();
    }

    private void startServer() {
        try {
            landingServer = LandingServer.getInstance();
            if (landingServer == null) {
                landingServer = new LandingServer(serverPort, (lat, lon) -> {
                    String coords = String.format("%.6f, %.6f", lat, lon);
                    Platform.runLater(() -> {
                        locationStatusLabel.setText("Location received from phone: " + coords);
                        manualLocationField.setText(coords);
                    });
                });
                landingServer.start();
            } else {
                landingServer.setOnLocation((lat, lon) -> {
                    String coords = String.format("%.6f, %.6f", lat, lon);
                    Platform.runLater(() -> {
                        locationStatusLabel.setText("Location received from phone: " + coords);
                        manualLocationField.setText(coords);
                    });
                });
            }

            String host = landingServer.getLocalAddress();
            String url = "http://" + host + ":" + serverPort + "/pair?token=" + landingServer.getPairToken();
            pairingField.setText(url);

            LandingServer.LastLocation last = landingServer.getLastLocation(landingServer.getPairToken());
            if (last != null) {
                String coords = String.format("%.6f, %.6f", last.lat, last.lon);
                locationStatusLabel.setText("Last location: " + coords);
                manualLocationField.setText(coords);
            } else {
                locationStatusLabel.setText("Server running. Use the URL above on your phone.");
                manualLocationField.setText("");
            }
        } catch (Exception e) {
            locationStatusLabel.setText("Failed to start server: " + e.getMessage());
            manualLocationField.setText("");
        }
    }

    private void copyPairing() {
        ClipboardContent content = new ClipboardContent();
        content.putString(pairingField.getText());
        Clipboard.getSystemClipboard().setContent(content);
    }

    private void openPairing() {
        try {
            String url = pairingField.getText();
            if (url == null || !url.startsWith("http")) return;
            if (Desktop.isDesktopSupported()) Desktop.getDesktop().browse(new URI(url));
        } catch (Exception ignored) {}
    }

    public String getCurrentToken() {
        if (landingServer == null) return null;
        return landingServer.getPairToken();
    }

    private void openAdminLogin() {
        try {
            com.example.cycleborrowingsystem.SceneManager.switchScene("/com/example/cycleborrowingsystem/admin_login.fxml");
        } catch (Exception ignored) {}
    }

    private void openUserLogin() {
        try {
            com.example.cycleborrowingsystem.SceneManager.switchScene("/com/example/cycleborrowingsystem/login.fxml");
        } catch (Exception ignored) {}
    }

    private void openUserSignup() {
        try {
            com.example.cycleborrowingsystem.SceneManager.switchScene("/com/example/cycleborrowingsystem/signup.fxml");
        } catch (Exception ignored) {}
    }

    public void dispose() {
    }
}
