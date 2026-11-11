package com.application.cabapp.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.application.cabapp.model.ChatMessage;

public interface ChatRepository extends JpaRepository<ChatMessage, Long> {
    List<ChatMessage> findByRideIdOrderByIdAsc(Long rideId);
}
