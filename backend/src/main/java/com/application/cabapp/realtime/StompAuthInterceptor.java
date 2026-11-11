package com.application.cabapp.realtime;

import java.util.List;

import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.MessagingException;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.ChannelInterceptor;
import org.springframework.messaging.support.MessageHeaderAccessor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.stereotype.Component;

import com.application.cabapp.exception.ApiException;
import com.application.cabapp.security.AuthPrincipal;
import com.application.cabapp.security.JwtService;
import com.application.cabapp.security.Role;
import com.application.cabapp.service.DriverService;
import com.application.cabapp.service.RideAccessService;

/**
 * Security for the WebSocket. Browsers can't add headers to the WebSocket
 * handshake, so the JWT comes in the STOMP CONNECT frame instead.
 *
 * - CONNECT needs a valid token.
 * - SUBSCRIBE /topic/rides/{id} is only for that ride's rider or driver.
 * - SUBSCRIBE /topic/pending/{type} is only for drivers of that vehicle type.
 * - Clients can't SEND anything; all changes go through the REST API.
 */
@Component
public class StompAuthInterceptor implements ChannelInterceptor {

    private static final String BEARER_PREFIX = "Bearer ";

    private final JwtService jwtService;
    private final RideAccessService rideAccess;
    private final DriverService driverService;

    public StompAuthInterceptor(JwtService jwtService, RideAccessService rideAccess, DriverService driverService) {
        this.jwtService = jwtService;
        this.rideAccess = rideAccess;
        this.driverService = driverService;
    }

    @Override
    public Message<?> preSend(Message<?> message, MessageChannel channel) {
        StompHeaderAccessor accessor = MessageHeaderAccessor.getAccessor(message, StompHeaderAccessor.class);
        if (accessor == null || accessor.getCommand() == null) return message;

        switch (accessor.getCommand()) {
            case CONNECT -> accessor.setUser(authenticate(accessor));
            case SUBSCRIBE -> authorizeSubscription(principalOf(accessor), accessor.getDestination());
            case SEND -> throw new MessagingException("Sending over the socket is not supported");
            default -> { /* heartbeats, UNSUBSCRIBE, DISCONNECT are fine */ }
        }
        return message;
    }

    private Authentication authenticate(StompHeaderAccessor accessor) {
        String header = accessor.getFirstNativeHeader("Authorization");
        if (header == null || !header.startsWith(BEARER_PREFIX)) {
            throw new MessagingException("Missing token");
        }
        AuthPrincipal principal = jwtService.parse(header.substring(BEARER_PREFIX.length()).trim())
                .orElseThrow(() -> new MessagingException("Invalid token"));
        return new UsernamePasswordAuthenticationToken(principal, null,
                List.of(new SimpleGrantedAuthority("ROLE_" + principal.role().name())));
    }

    private static AuthPrincipal principalOf(StompHeaderAccessor accessor) {
        if (accessor.getUser() instanceof Authentication auth && auth.getPrincipal() instanceof AuthPrincipal p) {
            return p;
        }
        throw new MessagingException("Not authenticated");
    }

    private void authorizeSubscription(AuthPrincipal principal, String destination) {
        try {
            var rideId = Topics.rideIdOf(destination);
            if (rideId.isPresent()) {
                rideAccess.getForParticipant(principal, rideId.get());
                return;
            }
            var vehicleType = Topics.vehicleTypeOf(destination);
            if (vehicleType.isPresent() && principal.is(Role.DRIVER)
                    && vehicleType.get().equals(driverService.loadCurrent(principal).getVehicleType())) {
                return;
            }
        } catch (ApiException e) {
            throw new MessagingException("Not allowed to subscribe to " + destination);
        }
        throw new MessagingException("Not allowed to subscribe to " + destination);
    }
}
