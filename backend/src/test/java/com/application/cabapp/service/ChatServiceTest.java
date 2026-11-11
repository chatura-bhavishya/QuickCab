package com.application.cabapp.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.application.cabapp.dto.request.SendMessageRequest;
import com.application.cabapp.exception.ApiException;
import com.application.cabapp.model.ChatMessage;
import com.application.cabapp.model.Ride;
import com.application.cabapp.model.RideStatus;
import com.application.cabapp.repository.ChatRepository;
import com.application.cabapp.repository.RideRepository;
import com.application.cabapp.security.AuthPrincipal;
import com.application.cabapp.security.Role;

@ExtendWith(MockitoExtension.class)
class ChatServiceTest {

    @Mock ChatRepository chatRepository;
    @Mock RideRepository rideRepository;
    private ChatService chatService;

    @BeforeEach
    void setUp() {
        chatService = new ChatService(chatRepository, new RideAccessService(rideRepository));
    }

    private static Ride ride(Long driverId) {
        Ride r = new Ride();
        r.setId(5L);
        r.setUserId(1L);
        r.setDriverId(driverId);
        r.setStatus(RideStatus.ACCEPTED);
        return r;
    }

    @Test
    void senderComesFromTheToken() {
        when(rideRepository.findById(5L)).thenReturn(Optional.of(ride(9L)));
        when(chatRepository.save(any(ChatMessage.class))).thenAnswer(inv -> inv.getArgument(0));

        var sent = chatService.send(new AuthPrincipal(9L, Role.DRIVER), new SendMessageRequest(5L, " On my way "));

        assertThat(sent.sender()).isEqualTo(Role.DRIVER);
        assertThat(sent.message()).isEqualTo("On my way");
    }

    @Test
    void outsidersCannotReadOrSend() {
        when(rideRepository.findById(5L)).thenReturn(Optional.of(ride(9L)));
        AuthPrincipal stranger = new AuthPrincipal(3L, Role.USER);

        assertThatThrownBy(() -> chatService.history(stranger, 5L)).isInstanceOf(ApiException.class);
        assertThatThrownBy(() -> chatService.send(stranger, new SendMessageRequest(5L, "hi")))
                .isInstanceOf(ApiException.class);
        verify(chatRepository, never()).save(any());
    }

    @Test
    void chatNeedsAnAssignedDriver() {
        when(rideRepository.findById(5L)).thenReturn(Optional.of(ride(null)));

        assertThatThrownBy(() -> chatService.send(new AuthPrincipal(1L, Role.USER), new SendMessageRequest(5L, "hi")))
                .hasMessageContaining("once a driver accepts");
    }
}
