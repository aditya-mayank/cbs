package com.example.cycleborrowingsystem.models;

public class Cycle {
	private long id;
	private String model;
	private String ownerEmail;
	private Double lat;
	private Double lon;
	private Long borrowedByUserId; // null if available

	public long getId() { return id; }
	public void setId(long id) { this.id = id; }
	public String getModel() { return model; }
	public void setModel(String model) { this.model = model; }
	public String getOwnerEmail() { return ownerEmail; }
	public void setOwnerEmail(String ownerEmail) { this.ownerEmail = ownerEmail; }
	public Double getLat() { return lat; }
	public void setLat(Double lat) { this.lat = lat; }
	public Double getLon() { return lon; }
	public void setLon(Double lon) { this.lon = lon; }
	public Long getBorrowedByUserId() { return borrowedByUserId; }
	public void setBorrowedByUserId(Long borrowedByUserId) { this.borrowedByUserId = borrowedByUserId; }
}
