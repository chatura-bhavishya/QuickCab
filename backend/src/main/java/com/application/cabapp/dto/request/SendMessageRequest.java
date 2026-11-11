package com.application.cabapp.dto.request;

import static com.application.cabapp.validation.ValidationRules.*;

import jakarta.validation.constraints.*;

/** Body of POST /api/chat/send. The sender is taken from the JWT, never from the body. */
public record SendMessageRequest(
        @NotNull(message = "Unknown ride")
        Long rideId,

        @NotBlank(message = "Message cannot be empty")
        @Size(max = CHAT_MESSAGE_MAX, message = "Message is too long")
        String message) {
}
