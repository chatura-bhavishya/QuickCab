package com.application.cabapp.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.application.cabapp.model.Driver;

public interface DriverRepository extends JpaRepository<Driver, Long> {
    Optional<Driver> findByUsername(String username);

    boolean existsByUsername(String username);
}
