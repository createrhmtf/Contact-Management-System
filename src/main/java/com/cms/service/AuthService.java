package com.cms.service;

import com.cms.model.dto.AuthResponse;
import com.cms.model.dto.LoginRequest;
import com.cms.model.dto.RegisterRequest;

public interface AuthService {

    AuthResponse register(RegisterRequest request);

    AuthResponse login(LoginRequest request);

    void changePassword(String email, String currentPassword, String newPassword);
}
