package com.cms.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

/**
 * Thrown whenever a user tries to access or modify a resource they do not own.
 * Returns HTTP 403 Forbidden automatically via @ResponseStatus.
 *
 * Usage: throw new AccessDeniedException("You do not own this contact");
 */
@ResponseStatus(HttpStatus.FORBIDDEN)
public class AccessDeniedException extends RuntimeException {

    public AccessDeniedException(String message) {
        super(message);
    }
}
