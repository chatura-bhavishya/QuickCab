package com.application.cabapp.dto.response;

/** Returned by login: the JWT plus the logged-in account. */
public record AuthResponse<T>(String token, long expiresInSeconds, T account) {
}
