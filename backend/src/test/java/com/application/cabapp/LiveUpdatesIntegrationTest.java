package com.application.cabapp;

import static org.assertj.core.api.Assertions.assertThat;

import java.lang.reflect.Type;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.messaging.converter.MappingJackson2MessageConverter;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaders;
import org.springframework.messaging.simp.stomp.StompSession;
import org.springframework.messaging.simp.stomp.StompSessionHandlerAdapter;
import org.springframework.web.socket.WebSocketHttpHeaders;
import org.springframework.web.socket.client.standard.StandardWebSocketClient;
import org.springframework.web.socket.messaging.WebSocketStompClient;

import com.jayway.jsonpath.JsonPath;

/** Real server + real STOMP client: live signals reach the right people only. */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class LiveUpdatesIntegrationTest {

    private static final long TIMEOUT_SECONDS = 5;

    @LocalServerPort int port;
    @Autowired TestRestTemplate rest;

    // ----- REST helpers -----

    private String post(String path, String token, String json) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        if (token != null) headers.setBearerAuth(token);
        ResponseEntity<String> response = rest.exchange(path, HttpMethod.POST, new HttpEntity<>(json, headers), String.class);
        assertThat(response.getStatusCode().is2xxSuccessful()).as(path + " -> " + response.getBody()).isTrue();
        return response.getBody();
    }

    private String rider(String name) {
        post("/api/users/register", null, """
                {"username":"%s","email":"%s@example.com","password":"secret12","contact":"9876543210"}
                """.formatted(name, name));
        return JsonPath.read(post("/api/users/login", null,
                "{\"username\":\"%s\",\"password\":\"secret12\"}".formatted(name)), "$.token");
    }

    private String driver(String name) {
        post("/api/drivers/register", null, """
                {"username":"%s","password":"secret12","contact":"9876543210","vehicleType":"car","vehicleNumber":"KA01"}
                """.formatted(name));
        return JsonPath.read(post("/api/drivers/login", null,
                "{\"username\":\"%s\",\"password\":\"secret12\"}".formatted(name)), "$.token");
    }

    // ----- STOMP helpers -----

    /** Collects the first message, or the first error/disconnect, on a session. */
    private static final class Probe extends StompSessionHandlerAdapter {
        final CompletableFuture<Map<?, ?>> message = new CompletableFuture<>();
        final CompletableFuture<String> failure = new CompletableFuture<>();

        @Override public Type getPayloadType(StompHeaders headers) { return Map.class; }
        @Override public void handleFrame(StompHeaders headers, Object payload) {
            if (payload instanceof Map<?, ?> map) message.complete(map);
            else failure.complete("error frame");
        }
        @Override public void handleException(StompSession s, StompCommand c, StompHeaders h, byte[] p, Throwable e) {
            failure.complete(e.getMessage());
        }
        @Override public void handleTransportError(StompSession session, Throwable exception) {
            failure.complete("transport: " + exception.getClass().getSimpleName());
        }
    }

    private StompSession connect(String token, Probe probe) throws Exception {
        WebSocketStompClient client = new WebSocketStompClient(new StandardWebSocketClient());
        client.setMessageConverter(new MappingJackson2MessageConverter());
        StompHeaders connectHeaders = new StompHeaders();
        if (token != null) connectHeaders.add("Authorization", "Bearer " + token);
        return client.connectAsync("ws://localhost:" + port + "/ws", new WebSocketHttpHeaders(), connectHeaders, probe)
                .get(TIMEOUT_SECONDS, TimeUnit.SECONDS);
    }

    // ----- Tests -----

    @Test
    void riderHearsAboutTheDriverAcceptingAndStrangersAreRefused() throws Exception {
        String riderToken = rider("live_rider");
        String strangerToken = rider("live_stranger");
        String driverToken = driver("live_driver");
        Integer rideId = JsonPath.read(post("/api/rides/book", riderToken,
                "{\"pickup\":\"A\",\"drop\":\"B\",\"vehicleType\":\"car\"}"), "$.id");

        Probe riderProbe = new Probe();
        StompSession riderSession = connect(riderToken, riderProbe);
        riderSession.subscribe("/topic/rides/" + rideId, riderProbe);

        Probe strangerProbe = new Probe();
        StompSession strangerSession = connect(strangerToken, strangerProbe);
        strangerSession.subscribe("/topic/rides/" + rideId, strangerProbe);

        // Give the subscriptions a moment to register before triggering the change.
        Thread.sleep(300);
        post("/api/rides/" + rideId + "/accept", driverToken, null);

        Map<?, ?> signal = riderProbe.message.get(TIMEOUT_SECONDS, TimeUnit.SECONDS);
        assertThat(signal.get("type")).isEqualTo("RIDE");
        assertThat(signal.get("rideId")).isEqualTo(rideId);
        assertThat(signal.containsKey("otp")).isFalse();

        assertThat(strangerProbe.failure.get(TIMEOUT_SECONDS, TimeUnit.SECONDS)).isNotNull();
        assertThat(strangerProbe.message).isNotDone();
    }

    @Test
    void connectionsWithoutATokenAreRejected() throws Exception {
        Probe probe = new Probe();
        try {
            connect(null, probe);
        } catch (ExecutionException | TimeoutException refusedDuringConnect) {
            return; // refused before the session was established
        }
        // Otherwise the server must send an ERROR frame / drop the connection.
        assertThat(probe.failure.get(TIMEOUT_SECONDS, TimeUnit.SECONDS)).isNotNull();
    }
}
