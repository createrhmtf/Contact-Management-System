package com.cms.service.impl;

import com.cms.model.dto.AuthResponse;
import com.cms.model.dto.LoginRequest;
import com.cms.model.dto.RegisterRequest;
import com.cms.model.entity.User;
import com.cms.repository.UserRepository;
import com.cms.security.JwtUtil;
import com.cms.service.AuthService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Slf4j
public class AuthServiceImpl implements AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;

    @Override
    @Transactional
    public AuthResponse register(RegisterRequest request) {
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new IllegalArgumentException("Email already registered");
        }
        if (request.getPhoneNumber() != null
                && !request.getPhoneNumber().isBlank()
                && userRepository.existsByPhoneNumber(request.getPhoneNumber())) {
            throw new IllegalArgumentException("Phone already registered");
        }

        User user = User.builder()
                .firstName(request.getFirstName().trim())
                .lastName(trimToNull(request.getLastName()))
                .email(request.getEmail().trim())
                .phoneNumber(trimToNull(request.getPhoneNumber()))
                .passwordHash(passwordEncoder.encode(request.getPassword()))
                .build();

        userRepository.save(user);
        log.info("New user registered: {}", user.getEmail());

        String token = jwtUtil.generateToken(user.getEmail());

        return AuthResponse.builder()
                .token(token)
                .firstName(user.getFirstName())
                .email(user.getEmail())
                .build();
    }

    @Override
    public AuthResponse login(LoginRequest request) {
        User user = resolveUser(request.getEmailOrPhone());

        if (!passwordEncoder.matches(request.getPassword(), user.getPasswordHash())) {
            throw new IllegalArgumentException("Invalid password");
        }

        log.info("User logged in: {}", user.getEmail());
        String token = jwtUtil.generateToken(user.getEmail());

        return AuthResponse.builder()
                .token(token)
                .firstName(user.getFirstName())
                .email(user.getEmail())
                .build();
    }

    @Override
    @Transactional
    public void changePassword(String email, String currentPassword, String newPassword) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));

        if (!passwordEncoder.matches(currentPassword, user.getPasswordHash())) {
            throw new IllegalArgumentException("Current password is wrong");
        }

        user.setPasswordHash(passwordEncoder.encode(newPassword));
        userRepository.save(user);
        log.info("Password changed for: {}", email);
    }

    private User resolveUser(String emailOrPhone) {
        String identifier = emailOrPhone.trim();

        return userRepository.findByEmail(identifier)
                .or(() -> userRepository.findByPhoneNumber(identifier))
                .orElseThrow(() -> new IllegalArgumentException("User not found"));
    }

    private String trimToNull(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        return value.trim();
    }
}
