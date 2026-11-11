package com.application.cabapp.realtime;

import java.util.Map;

import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

/**
 * Pushes "something changed" signals to subscribed browsers.
 *
 * Only ids go over the socket, never ride data: clients re-fetch through the
 * REST API, which already knows who may see what (e.g. the OTP). Sending after
 * commit means the re-fetch is guaranteed to see the new state.
 */
@Component
public class RideEventBroadcaster {

    private final SimpMessagingTemplate messaging;

    public RideEventBroadcaster(SimpMessagingTemplate messaging) {
        this.messaging = messaging;
    }

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void onRideChanged(RideChangedEvent event) {
        Map<String, Object> payload = Map.of("type", event.kind().name(), "rideId", event.rideId());
        messaging.convertAndSend(Topics.ride(event.rideId()), payload);
        if (event.pendingListChanged() && event.vehicleType() != null) {
            messaging.convertAndSend(Topics.pending(event.vehicleType()), payload);
        }
    }
}
