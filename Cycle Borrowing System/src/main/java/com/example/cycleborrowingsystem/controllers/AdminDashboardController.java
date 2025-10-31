package com.example.cycleborrowingsystem.controllers;

import com.example.cycleborrowingsystem.SceneManager;
import com.example.cycleborrowingsystem.dao.CycleDao;
import com.example.cycleborrowingsystem.dao.UserDao;
import com.example.cycleborrowingsystem.dao.BorrowTrackingDao;
import com.example.cycleborrowingsystem.models.BorrowTracking;
import com.example.cycleborrowingsystem.models.Cycle;
import com.example.cycleborrowingsystem.models.User;
import com.example.cycleborrowingsystem.net.LandingServer;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.util.Duration;

import java.net.URL;
import java.sql.SQLException;
import java.util.List;
import java.util.ResourceBundle;

public class AdminDashboardController implements Initializable {
	@FXML private Button backBtn;

	// cycles
	@FXML private TableView<Cycle> cyclesTable;
	@FXML private TableColumn<Cycle, String> cIdCol;
	@FXML private TableColumn<Cycle, String> cModelCol;
	@FXML private TableColumn<Cycle, String> cOwnerCol;
	@FXML private TableColumn<Cycle, String> cLocationCol;
	@FXML private TableColumn<Cycle, String> cBorrowedCol;
	@FXML private Button refreshCyclesBtn;
	@FXML private Button forceReturnBtn;
	@FXML private Button deleteCycleBtn;

	// users
	@FXML private TableView<User> usersTable;
	@FXML private TableColumn<User, String> uIdCol;
	@FXML private TableColumn<User, String> uNameCol;
	@FXML private TableColumn<User, String> uEmailCol;
	@FXML private Button refreshUsersBtn;
	@FXML private Button deleteUserBtn;

	private final CycleDao cycleDao = new CycleDao();
	private final UserDao userDao = new UserDao();
	private final BorrowTrackingDao trackingDao = new BorrowTrackingDao();
	private final ObservableList<Cycle> cycleRows = FXCollections.observableArrayList();
	private final ObservableList<User> userRows = FXCollections.observableArrayList();

	@Override
	public void initialize(URL location, ResourceBundle resources) {
		cIdCol.setCellValueFactory(c -> new SimpleStringProperty(String.valueOf(c.getValue().getId())));
		cModelCol.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getModel()));
		cOwnerCol.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getOwnerEmail()));
		cLocationCol.setCellValueFactory(c -> new SimpleStringProperty(formatLocation(c.getValue())));
		cBorrowedCol.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getBorrowedByUserId() == null ? "-" : ("user#" + c.getValue().getBorrowedByUserId())));
		cyclesTable.setItems(cycleRows);

		uIdCol.setCellValueFactory(u -> new SimpleStringProperty(String.valueOf(u.getValue().getId())));
		uNameCol.setCellValueFactory(u -> new SimpleStringProperty(u.getValue().getName()));
		uEmailCol.setCellValueFactory(u -> new SimpleStringProperty(u.getValue().getEmail()));
		usersTable.setItems(userRows);

		refreshCyclesBtn.setOnAction(e -> loadCycles());
		refreshUsersBtn.setOnAction(e -> loadUsers());
		forceReturnBtn.setOnAction(e -> forceReturn());
		deleteCycleBtn.setOnAction(e -> deleteCycle());
		deleteUserBtn.setOnAction(e -> deleteUser());
		backBtn.setOnAction(e -> goBack());

		loadCycles();
		loadUsers();

		Timeline autoRefresh = new Timeline(new KeyFrame(Duration.seconds(5), e -> loadCycles()));
		autoRefresh.setCycleCount(Timeline.INDEFINITE);
		autoRefresh.play();
	}

	private void loadCycles() {
		try {
			cycleDao.createTableIfNotExists();
			trackingDao.createTableIfNotExists();
			List<Cycle> list = cycleDao.listAll();
			// enrich with live borrower location if available
			LandingServer server = LandingServer.getInstance();
			for (Cycle c : list) {
				if (c.getBorrowedByUserId() != null && server != null) {
					BorrowTracking t = trackingDao.get(c.getId());
					if (t != null && t.getBorrowerToken() != null) {
						LandingServer.LastLocation last = server.getLastLocation(t.getBorrowerToken());
						if (last != null) {
							c.setLat(last.lat);
							c.setLon(last.lon);
						}
					}
				}
			}
			cycleRows.setAll(list);
		} catch (SQLException e) {
			new Alert(Alert.AlertType.ERROR, "DB error: " + e.getMessage()).show();
		}
	}

	private void loadUsers() {
		try {
			userDao.createTableIfNotExists();
			List<User> list = userDao.listAll();
			userRows.setAll(list);
		} catch (SQLException e) {
			new Alert(Alert.AlertType.ERROR, "DB error: " + e.getMessage()).show();
		}
	}

	private void forceReturn() {
		Cycle c = cyclesTable.getSelectionModel().getSelectedItem();
		if (c == null) { new Alert(Alert.AlertType.WARNING, "Select a cycle").show(); return; }
		try {
			boolean ok = cycleDao.adminForceReturn(c.getId());
			if (ok) { loadCycles(); }
			else new Alert(Alert.AlertType.WARNING, "No change").show();
		} catch (SQLException e) {
			new Alert(Alert.AlertType.ERROR, "DB error: " + e.getMessage()).show();
		}
	}

	private void deleteCycle() {
		Cycle c = cyclesTable.getSelectionModel().getSelectedItem();
		if (c == null) { new Alert(Alert.AlertType.WARNING, "Select a cycle").show(); return; }
		try {
			boolean ok = cycleDao.adminDelete(c.getId());
			if (ok) { loadCycles(); }
			else new Alert(Alert.AlertType.WARNING, "Delete failed").show();
		} catch (SQLException e) {
			new Alert(Alert.AlertType.ERROR, "DB error: " + e.getMessage()).show();
		}
	}

	private void deleteUser() {
		User u = usersTable.getSelectionModel().getSelectedItem();
		if (u == null) { new Alert(Alert.AlertType.WARNING, "Select a user").show(); return; }
		try {
			boolean ok = userDao.deleteById(u.getId());
			if (ok) { loadUsers(); }
			else new Alert(Alert.AlertType.WARNING, "Delete failed").show();
		} catch (SQLException e) {
			new Alert(Alert.AlertType.ERROR, "DB error: " + e.getMessage()).show();
		}
	}

	private void goBack() {
		try {
			SceneManager.switchScene("/com/example/cycleborrowingsystem/landing.fxml");
		} catch (Exception ignored) {}
	}

	private String formatLocation(Cycle c) {
		Double lat = c.getLat();
		Double lon = c.getLon();
		return (lat == null || lon == null) ? "-" : String.format("%.5f, %.5f", lat, lon);
	}
}
