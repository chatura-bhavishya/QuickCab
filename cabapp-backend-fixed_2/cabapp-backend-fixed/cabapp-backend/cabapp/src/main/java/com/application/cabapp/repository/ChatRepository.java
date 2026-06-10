package com.application.cabapp.repository;

import com.application.cabapp.model.ChatMessage;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ChatRepository extends JpaRepository<ChatMessage, Long> {
    List<ChatMessage> findByRideIdOrderByIdAsc(Long rideId);
}
