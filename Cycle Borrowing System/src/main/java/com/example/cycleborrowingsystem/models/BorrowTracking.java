package com.example.cycleborrowingsystem.models;

import java.sql.Timestamp;

public class BorrowTracking {
	private long cycleId;
	private long borrowerUserId;
	private String borrowerToken;
	private Double lat;
	private Double lon;
	private Timestamp updatedAt;

	public long getCycleId() { return cycleId; }
	public void setCycleId(long cycleId) { this.cycleId = cycleId; }
	public long getBorrowerUserId() { return borrowerUserId; }
	public void setBorrowerUserId(long borrowerUserId) { this.borrowerUserId = borrowerUserId; }
	public String getBorrowerToken() { return borrowerToken; }
	public void setBorrowerToken(String borrowerToken) { this.borrowerToken = borrowerToken; }
	public Double getLat() { return lat; }
	public void setLat(Double lat) { this.lat = lat; }
	public Double getLon() { return lon; }
	public void setLon(Double lon) { this.lon = lon; }
	public Timestamp getUpdatedAt() { return updatedAt; }
	public void setUpdatedAt(Timestamp updatedAt) { this.updatedAt = updatedAt; }
}
