package com.example.cycleborrowingsystem.controllers;

import com.example.cycleborrowingsystem.SceneManager;
import com.example.cycleborrowingsystem.Session;
import com.example.cycleborrowingsystem.dao.*;
import com.example.cycleborrowingsystem.models.BorrowRequest;
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

public class CyclesController implements Initializable {
	// Available tab
	@FXML private TableView<Cycle> availableTable;
	@FXML private TableColumn<Cycle, String> aModelCol;
	@FXML private TableColumn<Cycle, String> aOwnerCol;
	@FXML private TableColumn<Cycle, String> aLocationCol;
	@FXML private Button borrowBtn;

	// My cycles tab
	@FXML private TableView<Cycle> myTable;
	@FXML private TableColumn<Cycle, String> mModelCol;
	@FXML private TableColumn<Cycle, String> mLocationCol;
	@FXML private TableColumn<Cycle, String> mBorrowedCol;
	@FXML private TextField addModelField;
	@FXML private TextField addLatField;
	@FXML private TextField addLonField;
	@FXML private Button addCycleBtn;
	@FXML private Button removeCycleBtn;

	// Requests tab
	@FXML private TableView<BorrowRequest> incomingTable;
	@FXML private TableColumn<BorrowRequest, String> inCycleCol;
	@FXML private TableColumn<BorrowRequest, String> inBorrowerCol;
	@FXML private TableColumn<BorrowRequest, String> inStatusCol;
	@FXML private Button acceptBtn;
	@FXML private Button declineBtn;
	@FXML private TableView<BorrowRequest> outgoingTable;
	@FXML private TableColumn<BorrowRequest, String> outCycleCol;
	@FXML private TableColumn<BorrowRequest, String> outOwnerCol;
	@FXML private TableColumn<BorrowRequest, String> outStatusCol;

	// My borrowed tab
	@FXML private TableView<Cycle> borrowedTable;
	@FXML private TableColumn<Cycle, String> bModelCol;
	@FXML private TableColumn<Cycle, String> bOwnerCol;
	@FXML private TableColumn<Cycle, String> bLocationCol;
	@FXML private TextField manualLatLonField;
	@FXML private Button updateLocationBtn;
	@FXML private Button returnBtn;

	@FXML private Button refreshBtn;
	@FXML private Button backBtn;
	@FXML private Button logoutBtn;

	private final CycleDao cycleDao = new CycleDao();
	private final BorrowRequestDao requestDao = new BorrowRequestDao();
	private final BorrowTrackingDao trackingDao = new BorrowTrackingDao();
	private final UserLocationTokenDao tokenDao = new UserLocationTokenDao();

	private final ObservableList<Cycle> availableRows = FXCollections.observableArrayList();
	private final ObservableList<Cycle> myRows = FXCollections.observableArrayList();
	private final ObservableList<Cycle> borrowedRows = FXCollections.observableArrayList();
	private final ObservableList<BorrowRequest> incomingRows = FXCollections.observableArrayList();
	private final ObservableList<BorrowRequest> outgoingRows = FXCollections.observableArrayList();

	@Override
	public void initialize(URL location, ResourceBundle resources) {
		setupColumns();
		availableTable.setItems(availableRows);
		myTable.setItems(myRows);
		borrowedTable.setItems(borrowedRows);
		incomingTable.setItems(incomingRows);
		outgoingTable.setItems(outgoingRows);

		refreshBtn.setOnAction(e -> refreshAll());
		borrowBtn.setOnAction(e -> requestBorrow());
		addCycleBtn.setOnAction(e -> addCycle());
		removeCycleBtn.setOnAction(e -> removeSelectedOwned());
		acceptBtn.setOnAction(e -> acceptSelected());
		declineBtn.setOnAction(e -> declineSelected());
		updateLocationBtn.setOnAction(e -> shareMyLocation());
		returnBtn.setOnAction(e -> returnSelected());
		backBtn.setOnAction(e -> goBack());
		logoutBtn.setOnAction(e -> logout());
		refreshAll();
		Timeline autoRefresh = new Timeline(new KeyFrame(Duration.seconds(5), e -> refreshAll()));
		autoRefresh.setCycleCount(Timeline.INDEFINITE);
		autoRefresh.play();
	}

	private void setupColumns() {
		aModelCol.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getModel()));
		aOwnerCol.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getOwnerEmail()));
		aLocationCol.setCellValueFactory(c -> new SimpleStringProperty(formatLocation(c.getValue())));

		mModelCol.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getModel()));
		mLocationCol.setCellValueFactory(c -> new SimpleStringProperty(formatLocation(c.getValue())));
		mBorrowedCol.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getBorrowedByUserId() == null ? "-" : ("user#" + c.getValue().getBorrowedByUserId())));

		bModelCol.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getModel()));
		bOwnerCol.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getOwnerEmail()));
		bLocationCol.setCellValueFactory(c -> new SimpleStringProperty(formatLocation(c.getValue())));

		inCycleCol.setCellValueFactory(r -> new SimpleStringProperty(String.valueOf(r.getValue().getCycleId())));
		inBorrowerCol.setCellValueFactory(r -> new SimpleStringProperty("user#" + r.getValue().getBorrowerUserId()));
		inStatusCol.setCellValueFactory(r -> new SimpleStringProperty(r.getValue().getStatus()));

		outCycleCol.setCellValueFactory(r -> new SimpleStringProperty(String.valueOf(r.getValue().getCycleId())));
		outOwnerCol.setCellValueFactory(r -> new SimpleStringProperty(r.getValue().getOwnerEmail()));
		outStatusCol.setCellValueFactory(r -> new SimpleStringProperty(r.getValue().getStatus()));
	}

	private String formatLocation(Cycle cycle) {
		Double lat = cycle.getLat();
		Double lon = cycle.getLon();
		return (lat == null || lon == null) ? "-" : String.format("%.5f, %.5f", lat, lon);
	}

	private void ensureTables() throws SQLException {
		cycleDao.createTableIfNotExists();
		requestDao.createTableIfNotExists();
		trackingDao.createTableIfNotExists();
		tokenDao.createTableIfNotExists();
	}

	private void refreshAll() {
		User u = Session.getCurrentUser();
		try {
			ensureTables();
			List<Cycle> available = cycleDao.listAvailable();
			availableRows.setAll(available);
			if (u != null) {
				List<Cycle> my = cycleDao.listByOwner(u.getEmail());
				for (Cycle c : my) {
					if (c.getBorrowedByUserId() != null) {
						BorrowTracking tracking = trackingDao.get(c.getId());
						if (tracking != null && tracking.getBorrowerToken() != null) {
							LandingServer server = LandingServer.getInstance();
							if (server != null) {
								LandingServer.LastLocation loc = server.getLastLocation(tracking.getBorrowerToken());
								if (loc != null) {
									c.setLat(loc.lat);
									c.setLon(loc.lon);
								}
							}
						}
					}
				}
				myRows.setAll(my);
				borrowedRows.setAll(cycleDao.listBorrowedByUser(u.getId()));
				incomingRows.setAll(requestDao.listIncomingForOwner(u.getEmail()));
				outgoingRows.setAll(requestDao.listOutgoingForUser(u.getId()));
			}
		} catch (SQLException e) {
			new Alert(Alert.AlertType.ERROR, "DB error: " + e.getMessage()).show();
		}
	}

	private void requestBorrow() {
		Cycle selected = availableTable.getSelectionModel().getSelectedItem();
		if (selected == null) { new Alert(Alert.AlertType.WARNING, "Select an available cycle").show(); return; }
		User u = Session.getCurrentUser();
		if (u == null) { new Alert(Alert.AlertType.ERROR, "Not logged in").show(); return; }
		try {
			ensureTables();
			long id = requestDao.create(selected.getId(), selected.getOwnerEmail(), u.getId());
			if (id > 0) new Alert(Alert.AlertType.INFORMATION, "Request sent to owner").show();
			refreshAll();
		} catch (SQLException e) {
			new Alert(Alert.AlertType.ERROR, "DB error: " + e.getMessage()).show();
		}
	}

	private void acceptSelected() {
		User u = Session.getCurrentUser();
		if (u == null) { new Alert(Alert.AlertType.ERROR, "Not logged in").show(); return; }
		BorrowRequest req = incomingTable.getSelectionModel().getSelectedItem();
		if (req == null) { new Alert(Alert.AlertType.WARNING, "Select a request").show(); return; }
		if (!u.getEmail().equals(req.getOwnerEmail())) { new Alert(Alert.AlertType.ERROR, "Not your cycle").show(); return; }
		try {
			ensureTables();
			String borrowerToken = tokenDao.getToken(req.getBorrowerUserId());
			boolean s1 = requestDao.setStatus(req.getId(), "ACCEPTED");
			boolean s2 = cycleDao.borrow(req.getCycleId(), req.getBorrowerUserId());
			if (s1 && s2 && borrowerToken != null) {
				trackingDao.upsert(req.getCycleId(), req.getBorrowerUserId(), borrowerToken, null, null);
				LandingServer server = LandingServer.getInstance();
				if (server != null) {
					String host = server.getLocalAddress();
					int port = 8765;
					String url = "http://" + host + ":" + port + "/pair?token=" + borrowerToken;
					new Alert(Alert.AlertType.INFORMATION, "Share this link with the borrower to start live location:\n" + url).show();
				} else {
					new Alert(Alert.AlertType.WARNING, "Accepted. Start the pairing server on the owner machine to share a link.").show();
				}
			} else if (s1 && s2) {
				new Alert(Alert.AlertType.WARNING, "Accepted but borrower token not found").show();
			} else {
				new Alert(Alert.AlertType.WARNING, "Failed to accept").show();
			}
			refreshAll();
		} catch (SQLException e) {
			new Alert(Alert.AlertType.ERROR, "DB error: " + e.getMessage()).show();
		}
	}

	private void declineSelected() {
		BorrowRequest req = incomingTable.getSelectionModel().getSelectedItem();
		if (req == null) { new Alert(Alert.AlertType.WARNING, "Select a request").show(); return; }
		try {
			ensureTables();
			boolean ok = requestDao.setStatus(req.getId(), "DECLINED");
			if (ok) new Alert(Alert.AlertType.INFORMATION, "Declined").show(); else new Alert(Alert.AlertType.WARNING, "No change").show();
			refreshAll();
		} catch (SQLException e) {
			new Alert(Alert.AlertType.ERROR, "DB error: " + e.getMessage()).show();
		}
	}

	private void shareMyLocation() {
		User u = Session.getCurrentUser();
		if (u == null) { new Alert(Alert.AlertType.ERROR, "Not logged in").show(); return; }
		Cycle selected = borrowedTable.getSelectionModel().getSelectedItem();
		if (selected == null) { new Alert(Alert.AlertType.WARNING, "Select a borrowed cycle").show(); return; }
		Double lat = null, lon = null;
		String manual = manualLatLonField != null ? manualLatLonField.getText() : null;
		if (manual != null && !manual.isBlank() && manual.contains(",")) {
			try {
				String[] parts = manual.split(",");
				lat = Double.parseDouble(parts[0].trim());
				lon = Double.parseDouble(parts[1].trim());
			} catch (Exception ex) {
				new Alert(Alert.AlertType.ERROR, "Invalid lat,lon").show();
				return;
			}
		} else {
			LandingServer server = LandingServer.getInstance();
			if (server != null) {
				LandingServer.LastLocation last = server.getLastLocation(server.getPairToken());
				if (last != null) { lat = last.lat; lon = last.lon; }
			}
		}
		try {
			ensureTables();
			String myToken = tokenDao.getToken(u.getId());
			trackingDao.upsert(selected.getId(), u.getId(), myToken, lat, lon);
			new Alert(Alert.AlertType.INFORMATION, lat == null ? "Location cleared" : "Location shared. Owner can see it.").show();
		} catch (SQLException e) {
			new Alert(Alert.AlertType.ERROR, "DB error: " + e.getMessage()).show();
		}
	}

	private void addCycle() {
		User u = Session.getCurrentUser();
		if (u == null) {
			new Alert(Alert.AlertType.ERROR, "Not logged in").show();
			return;
		}
		String model = addModelField.getText();
		String latS = addLatField.getText();
		String lonS = addLonField.getText();
		if (model == null || model.isBlank()) {
			new Alert(Alert.AlertType.WARNING, "Enter model").show();
			return;
		}
		Double lat = null, lon = null;
		try {
			if (latS != null && !latS.isBlank()) lat = Double.parseDouble(latS);
			if (lonS != null && !lonS.isBlank()) lon = Double.parseDouble(lonS);
		} catch (NumberFormatException ex) {
			new Alert(Alert.AlertType.ERROR, "Invalid lat/lon").show();
			return;
		}
		try {
			ensureTables();
			Cycle c = new Cycle();
			c.setModel(model);
			c.setOwnerEmail(u.getEmail());
			c.setLat(lat);
			c.setLon(lon);
			long id = cycleDao.addCycle(c);
			if (id > 0) {
				addModelField.clear();
				addLatField.clear();
				addLonField.clear();
				refreshAll();
			} else {
				new Alert(Alert.AlertType.ERROR, "Failed to add cycle").show();
			}
		} catch (SQLException e) {
			new Alert(Alert.AlertType.ERROR, "DB error: " + e.getMessage()).show();
		}
	}

	private void removeSelectedOwned() {
		User u = Session.getCurrentUser();
		if (u == null) {
			new Alert(Alert.AlertType.ERROR, "Not logged in").show();
			return;
		}
		Cycle selected = myTable.getSelectionModel().getSelectedItem();
		if (selected == null) {
			new Alert(Alert.AlertType.WARNING, "Select one of your cycles").show();
			return;
		}
		try {
			ensureTables();
			boolean ok = cycleDao.deleteOwned(selected.getId(), u.getEmail());
			if (ok) {
				new Alert(Alert.AlertType.INFORMATION, "Removed").show();
				refreshAll();
			} else {
				new Alert(Alert.AlertType.WARNING, "Cannot remove (maybe borrowed?)").show();
			}
		} catch (SQLException e) {
			new Alert(Alert.AlertType.ERROR, "DB error: " + e.getMessage()).show();
		}
	}

	private void returnSelected() {
		User u = Session.getCurrentUser();
		if (u == null) {
			new Alert(Alert.AlertType.ERROR, "Not logged in").show();
			return;
		}
		Cycle selected = borrowedTable.getSelectionModel().getSelectedItem();
		if (selected == null) {
			new Alert(Alert.AlertType.WARNING, "Select a borrowed cycle").show();
			return;
		}
		try {
			ensureTables();
			boolean ok = cycleDao.returnCycle(selected.getId(), u.getId());
			if (ok) {
				new Alert(Alert.AlertType.INFORMATION, "Returned").show();
				refreshAll();
			} else {
				new Alert(Alert.AlertType.WARNING, "Failed to return").show();
			}
		} catch (SQLException e) {
			new Alert(Alert.AlertType.ERROR, "DB error: " + e.getMessage()).show();
		}
	}

	private void goBack() {
		try {
			SceneManager.switchScene("/com/example/cycleborrowingsystem/landing.fxml");
		} catch (Exception ignored) {}
	}

	private void logout() {
		Session.clear();
		goBack();
	}
}
