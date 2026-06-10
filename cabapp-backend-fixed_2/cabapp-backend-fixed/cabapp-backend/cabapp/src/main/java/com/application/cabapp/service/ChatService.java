package com.application.cabapp.service;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.application.cabapp.model.ChatMessage;
import com.application.cabapp.repository.ChatRepository;

/**
 * ChatService — owns ALL business logic for the ChatMessage entity.
 */
@Service
public class ChatService {

    @Autowired
    private ChatRepository chatRepo;

    public ChatMessage send(ChatMessage msg) {
        return chatRepo.save(msg);
    }

    public List<ChatMessage> history(Long rideId) {
        return chatRepo.findByRideIdOrderByIdAsc(rideId);
    }
}
