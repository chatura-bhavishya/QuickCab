package com.application.cabapp.model;

import com.fasterxml.jackson.annotation.JsonProperty;

import jakarta.persistence.*;

@Entity
@Table(name = "drivers")
public class Driver {
    public static final double DEFAULT_RATING = 5.0;

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true)
    private String username;

    // BCrypt hash. Accepted in requests but never serialized into responses.
    @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
    private String password;
    private String contact;
    private String vehicleType; // see VehicleType
    private String vehicleNumber;
    private Double rating = DEFAULT_RATING;
    private Integer ratingCount = 0;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }
    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }
    public String getContact() { return contact; }
    public void setContact(String contact) { this.contact = contact; }
    public String getVehicleType() { return vehicleType; }
    public void setVehicleType(String vehicleType) { this.vehicleType = vehicleType; }
    public String getVehicleNumber() { return vehicleNumber; }
    public void setVehicleNumber(String vehicleNumber) { this.vehicleNumber = vehicleNumber; }
    public Double getRating() { return rating; }
    public void setRating(Double rating) { this.rating = rating; }
    public Integer getRatingCount() { return ratingCount; }
    public void setRatingCount(Integer ratingCount) { this.ratingCount = ratingCount; }
}
