package com.application.cabapp.service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import com.application.cabapp.model.ChatMessage;
import com.application.cabapp.repository.ChatRepository;
import com.application.cabapp.repository.RideRepository;
import com.application.cabapp.validation.InputValidator;

/**
 * ChatService — owns ALL business logic for the ChatMessage entity.
 */
@Service
public class ChatService {

    private static final Set<String> SENDERS = Set.of("USER", "DRIVER");

    @Autowired
    private ChatRepository chatRepo;

    @Autowired
    private RideRepository rideRepo;

    public ResponseEntity<?> send(ChatMessage msg) {
        String error = InputValidator.firstError(
                msg.getRideId() == null || !rideRepo.existsById(msg.getRideId()) ? "Unknown ride" : null,
                msg.getSender() == null || !SENDERS.contains(msg.getSender()) ? "Invalid sender" : null,
                InputValidator.chatMessage(msg.getMessage()));
        if (error != null) return ResponseEntity.badRequest().body(error);

        // Server owns the id and timestamp.
        msg.setId(null);
        msg.setSentAt(LocalDateTime.now());
        return ResponseEntity.ok(chatRepo.save(msg));
    }

    public List<ChatMessage> history(Long rideId) {
        return chatRepo.findByRideIdOrderByIdAsc(rideId);
    }
}
