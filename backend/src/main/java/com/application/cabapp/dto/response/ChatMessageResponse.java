package com.application.cabapp.dto.response;

import java.time.LocalDateTime;

import com.application.cabapp.model.ChatMessage;
import com.application.cabapp.security.Role;

public record ChatMessageResponse(Long id, Long rideId, Role sender, String message, LocalDateTime sentAt) {

    public static ChatMessageResponse from(ChatMessage m) {
        return new ChatMessageResponse(m.getId(), m.getRideId(), m.getSender(), m.getMessage(), m.getSentAt());
    }
}
