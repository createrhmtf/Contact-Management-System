package com.cms.controller;

import com.cms.model.dto.UserDTO;
import com.cms.model.entity.User;
import com.cms.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * UserController handles HTTP requests related to the currently logged-in user's
 * own profile — things like viewing or editing their own account details.
 *
 * Base URL: /api/users
 *
 * Note: We only expose actions for the logged-in user's OWN profile.
 * There is no admin "list all users" endpoint here — this keeps things simple and secure.
 */
@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
@Slf4j
public class UserController {

    // UserRepository talks directly to the database to fetch/save User records.
    private final UserRepository userRepository;

    // PasswordEncoder is injected here so it's available if we ever add a
    // change-password endpoint in the future. It's already declared as a
    // @Bean in SecurityConfig so Spring can inject it here automatically.
    private final PasswordEncoder passwordEncoder;

    // ─── Helper: extract the logged-in user's email from the JWT token ────────
    //
    // After a successful login, Spring Security stores the user's email (the
    // "username" in JWT terms) in the SecurityContext. This helper retrieves it
    // so we don't repeat the same long line in every endpoint.
    private String getCurrentUserEmail() {
        return SecurityContextHolder.getContext().getAuthentication().getName();
    }

    // ─── Helper: convert a User entity → UserDTO ─────────────────────────────
    //
    // We do this mapping manually here (no separate mapper class) because
    // UserDTO only needs a handful of safe, non-sensitive fields.
    // The User entity contains passwordHash — we deliberately leave that out.
    private UserDTO mapToUserDTO(User user) {
        UserDTO dto = new UserDTO();
        dto.setId(user.getId());
        dto.setFirstName(user.getFirstName());
        dto.setLastName(user.getLastName());
        dto.setEmail(user.getEmail());
        dto.setPhoneNumber(user.getPhoneNumber());
        dto.setCreatedAt(user.getCreatedAt());
        return dto;
    }

    // =========================================================================
    // ENDPOINT 1: GET /api/users/me
    // Fetch the profile of the currently logged-in user.
    // =========================================================================

    /**
     * Returns the profile of the currently authenticated user.
     *
     * "Me" endpoints are a common REST pattern — instead of requiring the client
     * to know their own user ID, they simply call /me and Spring Security
     * identifies who they are from their JWT token.
     *
     * Example: GET /api/users/me
     *
     * @return 200 OK with the user's profile as a UserDTO (no password included)
     */
    @GetMapping("/me")
    public ResponseEntity<UserDTO> getMyProfile() {

        // Step 1: Get the email address of the currently logged-in user.
        // This comes from the JWT token that was sent with the request.
        String userEmail = getCurrentUserEmail();

        // Step 2: Look up the full User record in the database by their email.
        // If for any reason the user doesn't exist (e.g. was deleted after login),
        // throw a RuntimeException to trigger a 500 error response.
        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new RuntimeException("User not found: " + userEmail));

        // Step 3: Log that the profile was successfully fetched.
        // This is useful for auditing and debugging in server logs.
        log.info("Profile fetched for: {}", userEmail);

        // Step 4: Convert the User entity to a UserDTO (excludes sensitive data
        // like the password hash) and return it wrapped in a 200 OK response.
        return ResponseEntity.ok(mapToUserDTO(user));
    }

    // =========================================================================
    // ENDPOINT 2: PUT /api/users/me
    // Update the profile of the currently logged-in user.
    // =========================================================================

    /**
     * Update the currently authenticated user's profile.
     *
     * Only firstName and lastName can be updated through this endpoint.
     * Email and phone number are considered immutable account identifiers
     * (changing them would require additional verification steps).
     * The password is managed separately via a dedicated change-password endpoint.
     *
     * Example: PUT /api/users/me
     * Body: { "firstName": "Jane", "lastName": "Doe" }
     *
     * @param userDTO the updated profile data from the request body
     * @return 200 OK with the updated UserDTO
     */
    @PutMapping("/me")
    public ResponseEntity<UserDTO> updateMyProfile(@RequestBody UserDTO userDTO) {

        // Step 1: Get the email address of the currently logged-in user from JWT.
        String userEmail = getCurrentUserEmail();

        // Step 2: Fetch the existing User record from the database.
        // We fetch the existing record so we can update only specific fields —
        // we never want to overwrite the entire user record from raw client input.
        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new RuntimeException("User not found: " + userEmail));

        // Step 3: Update ONLY the firstName and lastName fields.
        // We intentionally do NOT update email, phoneNumber, passwordHash, or createdAt
        // here — those require special handling (verification, hashing, etc.).
        user.setFirstName(userDTO.getFirstName());
        user.setLastName(userDTO.getLastName());

        // Step 4: Save the updated User back to the database.
        User savedUser = userRepository.save(user);

        // Step 5: Log the successful profile update.
        log.info("Profile updated for: {}", userEmail);

        // Step 6: Convert the saved entity to a UserDTO and return 200 OK.
        return ResponseEntity.ok(mapToUserDTO(savedUser));
    }
}
