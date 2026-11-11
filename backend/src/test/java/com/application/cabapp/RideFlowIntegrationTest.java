package com.application.cabapp;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultMatcher;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;

import com.jayway.jsonpath.JsonPath;

/**
 * Full application on an in-memory H2 database: two riders and two drivers
 * go through a complete ride, checking every ownership rule on the way.
 */
@SpringBootTest
@AutoConfigureMockMvc
class RideFlowIntegrationTest {

    @Autowired MockMvc mvc;

    private String call(MockHttpServletRequestBuilder request, String token, String body, ResultMatcher expected)
            throws Exception {
        if (token != null) request.header(HttpHeaders.AUTHORIZATION, "Bearer " + token);
        if (body != null) request.contentType(MediaType.APPLICATION_JSON).content(body);
        return mvc.perform(request).andExpect(expected).andReturn().getResponse().getContentAsString();
    }

    private String registerAndLoginRider(String name) throws Exception {
        call(post("/api/users/register"), null, """
                {"username":"%s","email":"%s@example.com","password":"secret12","contact":"9876543210"}
                """.formatted(name, name), status().isCreated());
        String login = call(post("/api/users/login"), null,
                "{\"username\":\"%s\",\"password\":\"secret12\"}".formatted(name), status().isOk());
        return JsonPath.read(login, "$.token");
    }

    private String registerAndLoginDriver(String name, String vehicle) throws Exception {
        call(post("/api/drivers/register"), null, """
                {"username":"%s","password":"secret12","contact":"9876543210","vehicleType":"%s","vehicleNumber":"KA 01 AB 1234"}
                """.formatted(name, vehicle), status().isCreated());
        String login = call(post("/api/drivers/login"), null,
                "{\"username\":\"%s\",\"password\":\"secret12\"}".formatted(name), status().isOk());
        return JsonPath.read(login, "$.token");
    }

    @Test
    void completeRideWithOwnershipChecks() throws Exception {
        String rider = registerAndLoginRider("alice");
        String otherRider = registerAndLoginRider("bob");
        String driver = registerAndLoginDriver("dave", "car");
        String otherDriver = registerAndLoginDriver("erin", "car");

        // Passwords never come back from the API.
        call(get("/api/users/me"), rider, null, jsonPath("$.password").doesNotExist());

        // Rider books; fare and OTP are set by the server.
        String booked = call(post("/api/rides/book"), rider,
                "{\"pickup\":\"Home\",\"drop\":\"Office\",\"vehicleType\":\"car\",\"amount\":1}", status().isCreated());
        Integer rideId = JsonPath.read(booked, "$.id");
        String otp = JsonPath.read(booked, "$.otp");
        assertThat((Double) JsonPath.read(booked, "$.amount")).isEqualTo(150.0);

        // Nobody else can see it; drivers see it in the pending list without the OTP.
        call(get("/api/rides/" + rideId), otherRider, null, status().isForbidden());
        call(get("/api/rides/pending"), driver, null, jsonPath("$[0].otp").doesNotExist());
        call(post("/api/rides/book"), driver, "{\"pickup\":\"A\",\"drop\":\"B\",\"vehicleType\":\"car\"}",
                status().isForbidden());

        // First driver wins; the second gets a conflict.
        call(post("/api/rides/" + rideId + "/accept"), driver, null, status().isOk());
        call(post("/api/rides/" + rideId + "/accept"), otherDriver, null, status().isConflict());
        call(post("/api/rides/" + rideId + "/status"), otherDriver, "{\"status\":\"ON_THE_WAY\"}",
                status().isForbidden());

        // Chat is private to the two participants.
        call(post("/api/chat/send"), driver, "{\"rideId\":" + rideId + ",\"message\":\"On my way\"}",
                status().isCreated());
        call(get("/api/chat/" + rideId), rider, null, jsonPath("$[0].sender").value("DRIVER"));
        call(get("/api/chat/" + rideId), otherRider, null, status().isForbidden());

        // Driver progresses the ride; can't skip steps; never sees the OTP.
        call(post("/api/rides/" + rideId + "/status"), driver, "{\"status\":\"ARRIVED\"}", status().isBadRequest());
        call(post("/api/rides/" + rideId + "/status"), driver, "{\"status\":\"ON_THE_WAY\"}", status().isOk());
        call(post("/api/rides/" + rideId + "/status"), driver, "{\"status\":\"ARRIVED\"}", status().isOk());
        call(get("/api/rides/" + rideId), driver, null, jsonPath("$.ride.otp").doesNotExist());

        String wrongOtp = otp.equals("1000") ? "1001" : "1000";
        call(post("/api/rides/" + rideId + "/start"), driver, "{\"otp\":\"" + wrongOtp + "\"}", status().isBadRequest());
        call(post("/api/rides/" + rideId + "/start"), driver, "{\"otp\":\"" + otp + "\"}",
                jsonPath("$.status").value("STARTED"));
        call(post("/api/rides/" + rideId + "/cancel"), rider, null, status().isBadRequest());
        call(post("/api/rides/" + rideId + "/pay"), driver, null, jsonPath("$.status").value("COMPLETED"));

        // Only the rider can rate, and only once.
        call(post("/api/rides/" + rideId + "/rate"), otherRider, "{\"rating\":1}", status().isForbidden());
        call(post("/api/rides/" + rideId + "/rate"), rider, "{\"rating\":4}", jsonPath("$.ratingCount").value(1));
        call(post("/api/rides/" + rideId + "/rate"), rider, "{\"rating\":4}", status().isConflict());

        // Each side's history contains only their own rides.
        call(get("/api/rides/mine"), rider, null, jsonPath("$.length()").value(1));
        call(get("/api/rides/mine"), otherRider, null, jsonPath("$.length()").value(0));
        call(get("/api/rides/mine"), driver, null, jsonPath("$[0].otp").doesNotExist());
    }
}
