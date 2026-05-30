package com.cms.controller;

import com.cms.exception.ResourceNotFoundException;
import com.cms.model.dto.UserDTO;
import com.cms.model.entity.User;
import com.cms.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * UserController handles HTTP requests related to the currently logged-in user's
 * own profile — fetching and updating their account details.
 *
 * Base URL: /api/users
 *
 * Security: All endpoints require a valid JWT token (enforced by Spring Security).
 * Users can only see and edit their OWN profile — no admin list-all-users here.
 */
@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
@Slf4j
public class UserController {

    // UserRepository talks directly to the database to fetch and save User records.
    private final UserRepository userRepository;

    // ─── Helper: extract the logged-in user's email from JWT ─────────────────
    //
    // After a successful login, Spring Security stores the authenticated user's
    // email (the "principal name") in the SecurityContext. This helper retrieves
    // it cleanly without repeating the long call chain in every method.
    private String getCurrentUserEmail() {
        return SecurityContextHolder.getContext().getAuthentication().getName();
    }

    // ─── Helper: convert User entity → UserDTO ────────────────────────────────
    //
    // Maps only the safe, public-facing fields. The entity's passwordHash field
    // is deliberately excluded — it must never be sent in an API response.
    private UserDTO mapToUserDTO(User user) {
        return UserDTO.builder()
                .id(user.getId())
                .firstName(user.getFirstName())
                .lastName(user.getLastName())
                .email(user.getEmail())
                .phoneNumber(user.getPhoneNumber())
                .createdAt(user.getCreatedAt())
                .build();
    }

    // =========================================================================
    // ENDPOINT 1: GET /api/users/me
    // Fetch the profile of the currently logged-in user.
    // =========================================================================

    /**
     * Returns the profile of the currently authenticated user.
     *
     * The "/me" pattern is a standard REST convention — the client doesn't need
     * to know their own user ID, Spring Security identifies them from the JWT token.
     *
     * Example: GET /api/users/me
     *
     * @return 200 OK with the user's public profile as a UserDTO
     */
    @GetMapping("/me")
    public ResponseEntity<UserDTO> getMyProfile() {

        // Step 1: Read the authenticated user's email from the JWT token.
        String userEmail = getCurrentUserEmail();

        // Step 2: Look up the full User record in the database by email.
        // Throws ResourceNotFoundException (→ 404) if the user no longer exists.
        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new ResourceNotFoundException("User not found: " + userEmail));

        // Step 3: Log the profile fetch for audit trail.
        log.info("Profile fetched for: {}", userEmail);

        // Step 4: Convert to DTO (strips sensitive fields) and return 200 OK.
        return ResponseEntity.ok(mapToUserDTO(user));
    }

    // =========================================================================
    // ENDPOINT 2: PUT /api/users/me
    // Update the profile of the currently logged-in user.
    // =========================================================================

    /**
     * Update the display name of the currently authenticated user.
     *
     * Only firstName and lastName are editable here. Email is the login identifier
     * and cannot be changed without re-verification. Password changes go through
     * a dedicated endpoint (POST /api/auth/change-password) for security reasons.
     *
     * Example: PUT /api/users/me
     * Body: { "firstName": "Jane", "lastName": "Smith" }
     *
     * @param userDTO the updated name fields from the request body
     * @return 200 OK with the updated UserDTO
     */
    @PutMapping("/me")
    public ResponseEntity<UserDTO> updateMyProfile(@RequestBody UserDTO userDTO) {

        // Step 1: Identify who is making this request via their JWT token.
        String userEmail = getCurrentUserEmail();

        // Step 2: Fetch the current User record from the database.
        // We always fetch first — never trust the client to send a full valid entity.
        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new ResourceNotFoundException("User not found: " + userEmail));

        // Step 3: Apply only the allowed updates — firstName and lastName.
        // We do NOT touch email, phoneNumber, passwordHash, or createdAt.
        user.setFirstName(userDTO.getFirstName());
        user.setLastName(userDTO.getLastName());

        // Step 4: Persist the change to the database.
        User savedUser = userRepository.save(user);

        // Step 5: Log the successful update.
        log.info("Profile updated for: {}", userEmail);

        // Step 6: Return the updated profile as a DTO with 200 OK.
        return ResponseEntity.ok(mapToUserDTO(savedUser));
    }
}
