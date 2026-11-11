package com.application.cabapp.controller;

import com.application.cabapp.model.ChatMessage;
import com.application.cabapp.service.ChatService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Thin HTTP adapter — all logic lives in ChatService.
 */
@RestController
@RequestMapping("/api/chat")
public class ChatController {

    @Autowired
    private ChatService chatService;

    @PostMapping("/send")
    public ResponseEntity<?> send(@RequestBody ChatMessage msg) {
        return chatService.send(msg);
    }

    @GetMapping("/{rideId}")
    public List<ChatMessage> history(@PathVariable Long rideId) {
        return chatService.history(rideId);
    }
}
