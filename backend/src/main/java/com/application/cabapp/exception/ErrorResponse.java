package com.application.cabapp.exception;

import java.util.Map;

import org.springframework.http.HttpStatus;

import com.fasterxml.jackson.annotation.JsonInclude;

/**
 * Body of every error response: { "status": 400, "error": "message", "fieldErrors": {...} }.
 * The frontend displays {@code error}; {@code fieldErrors} is only present for validation failures.
 */
@JsonInclude(JsonInclude.Include.NON_EMPTY)
public record ErrorResponse(int status, String error, Map<String, String> fieldErrors) {

    public static ErrorResponse of(HttpStatus status, String error) {
        return new ErrorResponse(status.value(), error, Map.of());
    }
}
