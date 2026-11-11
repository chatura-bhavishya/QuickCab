package com.application.cabapp.controller;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import com.application.cabapp.dto.request.SendMessageRequest;
import com.application.cabapp.dto.response.ChatMessageResponse;
import com.application.cabapp.security.AuthPrincipal;
import com.application.cabapp.service.ChatService;

import jakarta.validation.Valid;

/** In-ride chat between the rider and the assigned driver. */
@RestController
@RequestMapping("/api/chat")
public class ChatController {

    private final ChatService chatService;

    public ChatController(ChatService chatService) {
        this.chatService = chatService;
    }

    @PostMapping("/send")
    @ResponseStatus(HttpStatus.CREATED)
    public ChatMessageResponse send(@AuthenticationPrincipal AuthPrincipal principal,
                                    @Valid @RequestBody SendMessageRequest request) {
        return chatService.send(principal, request);
    }

    @GetMapping("/{rideId}")
    public List<ChatMessageResponse> history(@AuthenticationPrincipal AuthPrincipal principal,
                                             @PathVariable Long rideId) {
        return chatService.history(principal, rideId);
    }
}
