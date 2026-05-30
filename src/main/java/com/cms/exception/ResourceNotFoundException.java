package com.cms.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

/**
 * Thrown whenever a requested resource (contact, user, etc.) cannot be found
 * in the database. The @ResponseStatus annotation tells Spring to automatically
 * return HTTP 404 Not Found if this exception is not caught anywhere.
 *
 * Usage: throw new ResourceNotFoundException("Contact not found with id: " + id);
 */
@ResponseStatus(HttpStatus.NOT_FOUND)
public class ResourceNotFoundException extends RuntimeException {

    public ResourceNotFoundException(String message) {
        super(message);
    }
}
