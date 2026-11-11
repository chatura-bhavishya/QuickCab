package com.application.cabapp.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.options;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.time.LocalDateTime;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import com.application.cabapp.config.SecurityConfig;
import com.application.cabapp.dto.request.BookRideRequest;
import com.application.cabapp.dto.response.RideResponse;
import com.application.cabapp.exception.ApiException;
import com.application.cabapp.model.RideStatus;
import com.application.cabapp.security.AuthPrincipal;
import com.application.cabapp.security.JwtService;
import com.application.cabapp.security.Role;
import com.application.cabapp.service.RideService;

/** Web layer + real security config: authentication, validation and error mapping. */
@WebMvcTest(RideController.class)
@Import({SecurityConfig.class, JwtService.class})
class RideControllerSecurityTest {

    private static final AuthPrincipal RIDER = new AuthPrincipal(1L, Role.USER);

    @Autowired MockMvc mvc;
    @Autowired JwtService jwtService;
    @MockBean RideService rideService;

    private String bearer(AuthPrincipal principal) {
        return "Bearer " + jwtService.generateToken(principal);
    }

    private static RideResponse sampleRide() {
        return new RideResponse(100L, 1L, null, "A", "B", "car", RideStatus.PENDING, "1234",
                false, 150.0, false, LocalDateTime.now(), null, null, null, null, null);
    }

    @Test
    void requestsWithoutTokenAreRejected() throws Exception {
        mvc.perform(get("/api/rides/mine"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.error").value("Please log in to continue"));
        verify(rideService, never()).getMyRides(any());
    }

    @Test
    void invalidTokensAreRejected() throws Exception {
        mvc.perform(get("/api/rides/mine").header(HttpHeaders.AUTHORIZATION, "Bearer forged.token.value"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void callerIdentityComesFromTheToken() throws Exception {
        when(rideService.book(eq(RIDER), any(BookRideRequest.class))).thenReturn(sampleRide());

        // userId in the body is ignored; the server uses the token's subject.
        mvc.perform(post("/api/rides/book").header(HttpHeaders.AUTHORIZATION, bearer(RIDER))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"userId\":999,\"pickup\":\"A\",\"drop\":\"B\",\"vehicleType\":\"car\"}"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(100));
        verify(rideService).book(eq(RIDER), any(BookRideRequest.class));
    }

    @Test
    void invalidBodiesReturnFieldErrors() throws Exception {
        mvc.perform(post("/api/rides/book").header(HttpHeaders.AUTHORIZATION, bearer(RIDER))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"pickup\":\"\",\"drop\":\"B\",\"vehicleType\":\"truck\"}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.fieldErrors.pickup").value("Pickup location is required"))
                .andExpect(jsonPath("$.fieldErrors.vehicleType").value("Choose a valid vehicle type"));
        verify(rideService, never()).book(any(), any());
    }

    @Test
    void serviceErrorsBecomeCleanJson() throws Exception {
        when(rideService.getDetails(RIDER, 5L)).thenThrow(ApiException.forbidden("You do not have access to this ride"));

        mvc.perform(get("/api/rides/5").header(HttpHeaders.AUTHORIZATION, bearer(RIDER)))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.status").value(403))
                .andExpect(jsonPath("$.error").value("You do not have access to this ride"));
    }

    @Test
    void unexpectedErrorsDoNotLeakDetails() throws Exception {
        when(rideService.getMyRides(RIDER)).thenThrow(new IllegalStateException("SELECT * FROM rides failed"));

        mvc.perform(get("/api/rides/mine").header(HttpHeaders.AUTHORIZATION, bearer(RIDER)))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.error").value("Something went wrong. Please try again."))
                .andExpect(jsonPath("$.trace").doesNotExist());
    }

    @Test
    void malformedPathIdIsABadRequest() throws Exception {
        mvc.perform(get("/api/rides/not-a-number").header(HttpHeaders.AUTHORIZATION, bearer(RIDER)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void corsAllowsOnlyTheConfiguredFrontend() throws Exception {
        mvc.perform(options("/api/rides/mine")
                        .header(HttpHeaders.ORIGIN, "http://localhost:5173")
                        .header(HttpHeaders.ACCESS_CONTROL_REQUEST_METHOD, "GET"))
                .andExpect(status().isOk())
                .andExpect(header().string(HttpHeaders.ACCESS_CONTROL_ALLOW_ORIGIN, "http://localhost:5173"));

        mvc.perform(options("/api/rides/mine")
                        .header(HttpHeaders.ORIGIN, "https://evil.example")
                        .header(HttpHeaders.ACCESS_CONTROL_REQUEST_METHOD, "GET"))
                .andExpect(status().isForbidden());
    }

    @Test
    void listEndpointReturnsCallerRides() throws Exception {
        when(rideService.getMyRides(RIDER)).thenReturn(List.of(sampleRide()));

        mvc.perform(get("/api/rides/mine").header(HttpHeaders.AUTHORIZATION, bearer(RIDER)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].pickup").value("A"));
    }
}
