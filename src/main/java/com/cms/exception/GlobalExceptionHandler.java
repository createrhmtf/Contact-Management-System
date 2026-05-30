package com.cms.exception;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * GlobalExceptionHandler is a centralized place to handle ALL exceptions thrown
 * anywhere in the application and convert them into clean, consistent JSON responses.
 *
 * Without this class, Spring would return its own raw error pages or stack traces —
 * not something we want to send to API clients.
 *
 * @RestControllerAdvice means: "apply to all @RestController classes in this project"
 */
@RestControllerAdvice
@Slf4j
public class GlobalExceptionHandler {

    // ─── 404 Not Found ───────────────────────────────────────────────────────

    /**
     * Handles ResourceNotFoundException and UsernameNotFoundException.
     * Both mean "something was looked for but not found" → 404.
     */
    @ExceptionHandler({ResourceNotFoundException.class, UsernameNotFoundException.class})
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public Map<String, Object> handleNotFound(RuntimeException ex) {
        log.warn("Resource not found: {}", ex.getMessage());
        return buildError(HttpStatus.NOT_FOUND, ex.getMessage());
    }

    // ─── 403 Forbidden ───────────────────────────────────────────────────────

    /**
     * Handles our custom AccessDeniedException — thrown when a user tries to
     * read or modify a contact they do not own.
     */
    @ExceptionHandler(AccessDeniedException.class)
    @ResponseStatus(HttpStatus.FORBIDDEN)
    public Map<String, Object> handleAccessDenied(AccessDeniedException ex) {
        log.warn("Access denied: {}", ex.getMessage());
        return buildError(HttpStatus.FORBIDDEN, ex.getMessage());
    }

    // ─── 400 Bad Request — Validation errors (@Valid) ─────────────────────

    /**
     * Handles validation failures from @Valid/@RequestBody.
     * Collects all field errors and returns them as a list.
     * Example: { "errors": ["firstName: must not be blank"], "status": 400 }
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public Map<String, Object> handleValidationErrors(MethodArgumentNotValidException ex) {
        List<String> errors = ex.getBindingResult().getFieldErrors().stream()
                .map(error -> error.getField() + ": " + error.getDefaultMessage())
                .toList();

        Map<String, Object> body = buildError(HttpStatus.BAD_REQUEST, "Validation failed");
        body.put("errors", errors);
        return body;
    }

    // ─── 400 Bad Request — Business rule violations ───────────────────────

    /**
     * Handles IllegalArgumentException — thrown by the service layer for
     * business rule violations (e.g. "Email already registered").
     */
    @ExceptionHandler(IllegalArgumentException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public Map<String, Object> handleIllegalArgument(IllegalArgumentException ex) {
        log.warn("Business rule violation: {}", ex.getMessage());
        return buildError(HttpStatus.BAD_REQUEST, ex.getMessage());
    }

    // ─── 500 Internal Server Error — Catch-all ────────────────────────────

    /**
     * Catches any unexpected exception that wasn't handled above.
     * We log the full stack trace here but return a generic message to the client
     * — never expose internal error details to API consumers.
     */
    @ExceptionHandler(Exception.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public Map<String, Object> handleUnexpectedError(Exception ex) {
        log.error("Unexpected server error", ex);
        return buildError(HttpStatus.INTERNAL_SERVER_ERROR, "An unexpected error occurred. Please try again later.");
    }

    // ─── Helper: build a consistent error response body ───────────────────

    /**
     * Creates a standard error response map so ALL error responses from this
     * application have the same JSON structure:
     * {
     *   "status": 404,
     *   "error":  "Not Found",
     *   "message": "Contact not found with id: 5",
     *   "timestamp": "2024-01-15T10:30:00"
     * }
     */
    private Map<String, Object> buildError(HttpStatus status, String message) {
        // LinkedHashMap preserves insertion order — keeps JSON fields in a predictable sequence
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("status", status.value());
        body.put("error", status.getReasonPhrase());
        body.put("message", message);
        body.put("timestamp", LocalDateTime.now().toString());
        return body;
    }
}
