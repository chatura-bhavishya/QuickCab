package com.application.cabapp.dto.response;

import java.util.Set;

/** A rider's previously used pickup and drop locations (for autocomplete). */
public record LocationsResponse(Set<String> pickups, Set<String> drops) {
}
