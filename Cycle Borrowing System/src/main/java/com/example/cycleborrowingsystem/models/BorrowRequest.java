package com.example.cycleborrowingsystem.models;

import java.sql.Timestamp;

public class BorrowRequest {
	private long id;
	private long cycleId;
	private String ownerEmail;
	private long borrowerUserId;
	private String status;
	private Timestamp createdAt;

	public long getId() { return id; }
	public void setId(long id) { this.id = id; }
	public long getCycleId() { return cycleId; }
	public void setCycleId(long cycleId) { this.cycleId = cycleId; }
	public String getOwnerEmail() { return ownerEmail; }
	public void setOwnerEmail(String ownerEmail) { this.ownerEmail = ownerEmail; }
	public long getBorrowerUserId() { return borrowerUserId; }
	public void setBorrowerUserId(long borrowerUserId) { this.borrowerUserId = borrowerUserId; }
	public String getStatus() { return status; }
	public void setStatus(String status) { this.status = status; }
	public Timestamp getCreatedAt() { return createdAt; }
	public void setCreatedAt(Timestamp createdAt) { this.createdAt = createdAt; }
}
