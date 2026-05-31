package com.cms.support;

import com.cms.model.dto.ChangePasswordRequest;
import com.cms.model.dto.LoginRequest;
import com.cms.model.dto.RegisterRequest;
import com.cms.model.entity.User;

public final class TestDataFactory {

    private TestDataFactory() {
    }

    public static RegisterRequest validRegisterRequest() {
        return new RegisterRequest(
                TestConstants.USER_FIRST_NAME,
                TestConstants.USER_LAST_NAME,
                TestConstants.USER_EMAIL,
                TestConstants.USER_PHONE,
                TestConstants.VALID_PASSWORD);
    }

    public static RegisterRequest registerRequest(String email, String phone) {
        return new RegisterRequest(
                TestConstants.USER_FIRST_NAME,
                TestConstants.USER_LAST_NAME,
                email,
                phone,
                TestConstants.VALID_PASSWORD);
    }

    public static LoginRequest loginWithEmail(String email) {
        return new LoginRequest(email, TestConstants.VALID_PASSWORD);
    }

    public static LoginRequest loginWithPhone(String phone) {
        return new LoginRequest(phone, TestConstants.VALID_PASSWORD);
    }

    public static ChangePasswordRequest validChangePasswordRequest() {
        return new ChangePasswordRequest(TestConstants.VALID_PASSWORD, TestConstants.NEW_PASSWORD);
    }

    public static User persistedUser(String email, String encodedPassword) {
        return User.builder()
                .firstName(TestConstants.USER_FIRST_NAME)
                .lastName(TestConstants.USER_LAST_NAME)
                .email(email)
                .phoneNumber(TestConstants.USER_PHONE)
                .passwordHash(encodedPassword)
                .build();
    }
}
