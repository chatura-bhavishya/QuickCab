package com.application.cabapp.validation;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

import org.junit.jupiter.api.Test;

class InputValidatorTest {

    @Test
    void acceptsValidRegistrationFields() {
        assertNull(InputValidator.username("rider_01"));
        assertNull(InputValidator.password("secret12"));
        assertNull(InputValidator.email("rider@example.com"));
        assertNull(InputValidator.contact("+91 98765-43210"));
        assertNull(InputValidator.vehicleType("car"));
        assertNull(InputValidator.vehicleNumber("KA 01 AB 1234"));
    }

    @Test
    void rejectsInvalidUsernames() {
        assertNotNull(InputValidator.username(null));
        assertNotNull(InputValidator.username("ab"));
        assertNotNull(InputValidator.username("a".repeat(InputValidator.USERNAME_MAX + 1)));
        assertNotNull(InputValidator.username("bad name"));
        assertNotNull(InputValidator.username("<script>"));
    }

    @Test
    void rejectsInvalidEmailsAndPhones() {
        assertNotNull(InputValidator.email("not-an-email"));
        assertNotNull(InputValidator.email("a@b"));
        assertNotNull(InputValidator.contact("12345"));
        assertNotNull(InputValidator.contact("phone-me"));
        assertNotNull(InputValidator.contact("1".repeat(InputValidator.PHONE_DIGITS_MAX + 1)));
    }

    @Test
    void passwordRules() {
        assertNotNull(InputValidator.password("12345"));
        assertNotNull(InputValidator.password("x".repeat(InputValidator.PASSWORD_MAX_BYTES + 1)));
        assertNull(InputValidator.optionalPassword(""));
        assertNotNull(InputValidator.optionalPassword("123"));
    }

    @Test
    void rideAndChatRules() {
        assertNotNull(InputValidator.vehicleType("truck"));
        assertNotNull(InputValidator.location("  ", "Pickup"));
        assertNotNull(InputValidator.location("x".repeat(InputValidator.LOCATION_MAX + 1), "Pickup"));
        assertNotNull(InputValidator.chatMessage("x".repeat(InputValidator.CHAT_MESSAGE_MAX + 1)));
        assertNotNull(InputValidator.rating(0.0));
        assertNotNull(InputValidator.rating(6.0));
        assertNotNull(InputValidator.rating(null));
        assertNull(InputValidator.rating(4.0));
    }
}
