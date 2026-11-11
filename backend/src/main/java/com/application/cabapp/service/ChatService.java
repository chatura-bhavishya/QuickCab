package com.application.cabapp.service;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.application.cabapp.dto.request.SendMessageRequest;
import com.application.cabapp.dto.response.ChatMessageResponse;
import com.application.cabapp.exception.ApiException;
import com.application.cabapp.model.ChatMessage;
import com.application.cabapp.model.Ride;
import com.application.cabapp.repository.ChatRepository;
import com.application.cabapp.security.AuthPrincipal;

/** In-ride chat. Only the ride's rider and assigned driver can read or send messages. */
@Service
public class ChatService {

    private final ChatRepository chatRepository;
    private final RideAccessService rideAccess;

    public ChatService(ChatRepository chatRepository, RideAccessService rideAccess) {
        this.chatRepository = chatRepository;
        this.rideAccess = rideAccess;
    }

    @Transactional
    public ChatMessageResponse send(AuthPrincipal principal, SendMessageRequest request) {
        Ride ride = rideAccess.getForParticipant(principal, request.rideId());
        if (ride.getDriverId() == null) {
            throw ApiException.badRequest("Chat is available once a driver accepts the ride");
        }
        ChatMessage message = new ChatMessage();
        message.setRideId(ride.getId());
        message.setSender(principal.role());
        message.setMessage(request.message().trim());
        message.setSentAt(LocalDateTime.now());
        return ChatMessageResponse.from(chatRepository.save(message));
    }

    public List<ChatMessageResponse> history(AuthPrincipal principal, Long rideId) {
        rideAccess.getForParticipant(principal, rideId);
        return chatRepository.findByRideIdOrderByIdAsc(rideId).stream().map(ChatMessageResponse::from).toList();
    }
}
