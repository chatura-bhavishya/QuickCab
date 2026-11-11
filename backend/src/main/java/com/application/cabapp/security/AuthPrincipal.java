package com.application.cabapp.security;

import com.application.cabapp.exception.ApiException;

/**
 * The authenticated caller, decoded from the JWT. Users and drivers live in
 * separate tables, so an id is only meaningful together with its role.
 */
public record AuthPrincipal(Long id, Role role) {

    public boolean is(Role expected) {
        return role == expected;
    }

    /** Throws 403 unless the caller has the given role. */
    public void requireRole(Role expected) {
        if (!is(expected)) {
            throw ApiException.forbidden("This action is only available to " + expected.name().toLowerCase() + "s");
        }
    }
}
