package com.application.cabapp.dto.response;

import com.application.cabapp.model.User;

/** Public view of a rider (no password). */
public record UserResponse(Long id, String username, String email, String contact) {

    public static UserResponse from(User user) {
        return new UserResponse(user.getId(), user.getUsername(), user.getEmail(), user.getContact());
    }
}
