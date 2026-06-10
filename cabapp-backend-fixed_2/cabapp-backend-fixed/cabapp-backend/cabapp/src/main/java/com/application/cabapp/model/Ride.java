package com.application.cabapp.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "rides")
public class Ride {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Long userId;
    private Long driverId; // null until accepted
    private String pickup;

    // "drop" is a reserved word in MySQL/SQL. Quote the column name so the
    // generated INSERT/UPDATE/SELECT statements are valid SQL.
    @Column(name = "`drop`")
    private String drop;

    private String vehicleType;
    // PENDING, ACCEPTED, DENIED, ON_THE_WAY, ARRIVED, STARTED, COMPLETED
    private String status = "PENDING";
    private String otp;
    private boolean paid = false;
    // Payment amount (fare) for this ride, computed at booking time.
    private Double amount;
    private LocalDateTime createdAt = LocalDateTime.now();

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Long getUserId() { return userId; }
    public void setUserId(Long userId) { this.userId = userId; }
    public Long getDriverId() { return driverId; }
    public void setDriverId(Long driverId) { this.driverId = driverId; }
    public String getPickup() { return pickup; }
    public void setPickup(String pickup) { this.pickup = pickup; }
    public String getDrop() { return drop; }
    public void setDrop(String drop) { this.drop = drop; }
    public String getVehicleType() { return vehicleType; }
    public void setVehicleType(String vehicleType) { this.vehicleType = vehicleType; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public String getOtp() { return otp; }
    public void setOtp(String otp) { this.otp = otp; }
    public boolean isPaid() { return paid; }
    public void setPaid(boolean paid) { this.paid = paid; }
    public Double getAmount() { return amount; }
    public void setAmount(Double amount) { this.amount = amount; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
}
