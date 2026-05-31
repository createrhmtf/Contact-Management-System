package com.cms.integration;

import com.cms.model.dto.AuthResponse;
import com.cms.model.dto.ChangePasswordRequest;
import com.cms.model.dto.LoginRequest;
import com.cms.model.dto.RegisterRequest;
import com.cms.repository.UserRepository;
import com.cms.support.AbstractIntegrationTest;
import com.cms.support.AuthTestHelper;
import com.cms.support.TestConstants;
import com.cms.support.TestDataFactory;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MvcResult;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@DisplayName("Authentication API Integration Tests")
class AuthApiIntegrationTest extends AbstractIntegrationTest {

    @Autowired
    private UserRepository userRepository;

    @Nested
    @DisplayName("Registration flow")
    class RegistrationFlow {

        @Test
        @DisplayName("TC-REG-01: POST /api/auth/register creates user and returns JWT")
        void register_persistsUserAndReturnsToken() throws Exception {
            RegisterRequest request = TestDataFactory.validRegisterRequest();

            MvcResult result = mockMvc.perform(post("/api/auth/register")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isCreated())
                    .andExpect(jsonPath("$.token").isNotEmpty())
                    .andExpect(jsonPath("$.email").value(request.getEmail()))
                    .andExpect(jsonPath("$.firstName").value(request.getFirstName()))
                    .andReturn();

            AuthResponse response = objectMapper.readValue(
                    result.getResponse().getContentAsString(),
                    AuthResponse.class);

            assertThat(userRepository.existsByEmail(request.getEmail())).isTrue();
            assertThat(userRepository.findByEmail(request.getEmail()))
                    .get()
                    .extracting("passwordHash")
                    .asString()
                    .isNotEqualTo(request.getPassword());
            assertThat(response.getToken()).isNotBlank();
        }

        @Test
        @DisplayName("TC-REG-02: register without last name succeeds")
        void register_withoutLastName_succeeds() throws Exception {
            RegisterRequest request = new RegisterRequest(
                    "Sara",
                    null,
                    "sara.no.last@test.com",
                    null,
                    TestConstants.VALID_PASSWORD);

            mockMvc.perform(post("/api/auth/register")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isCreated());

            assertThat(userRepository.findByEmail("sara.no.last@test.com"))
                    .get()
                    .extracting("lastName")
                    .isNull();
        }

        @Test
        @DisplayName("TC-REG-04: duplicate email returns 400")
        void register_duplicateEmail_returnsBadRequest() throws Exception {
            RegisterRequest request = TestDataFactory.validRegisterRequest();
            AuthTestHelper.registerAndObtainToken(mockMvc, objectMapper, request);

            mockMvc.perform(post("/api/auth/register")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.message").value("Email already registered"));
        }

        @Test
        @DisplayName("TC-REG-06: invalid email returns 400 with validation errors")
        void register_invalidEmail_returnsBadRequest() throws Exception {
            RegisterRequest request = new RegisterRequest(
                    "Bad",
                    "Email",
                    "not-an-email",
                    null,
                    TestConstants.VALID_PASSWORD);

            mockMvc.perform(post("/api/auth/register")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.errors").isArray());
        }

        @Test
        @DisplayName("TC-REG-07: short password returns 400")
        void register_shortPassword_returnsBadRequest() throws Exception {
            RegisterRequest request = new RegisterRequest(
                    "Short",
                    "Pass",
                    "short.pass@test.com",
                    null,
                    TestConstants.SHORT_PASSWORD);

            mockMvc.perform(post("/api/auth/register")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.errors").isArray());
        }
    }

    @Nested
    @DisplayName("Login flow")
    class LoginFlow {

        @Test
        @DisplayName("TC-LOG-01: login with email returns JWT")
        void login_withEmail_returnsToken() throws Exception {
            RegisterRequest registerRequest = TestDataFactory.registerRequest(
                    "login.email@test.com",
                    "03009998877");
            AuthTestHelper.registerAndObtainToken(mockMvc, objectMapper, registerRequest);

            LoginRequest loginRequest = new LoginRequest(
                    "login.email@test.com",
                    TestConstants.VALID_PASSWORD);

            mockMvc.perform(post("/api/auth/login")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(loginRequest)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.token").isNotEmpty())
                    .andExpect(jsonPath("$.email").value("login.email@test.com"));
        }

        @Test
        @DisplayName("TC-LOG-02: login with phone returns JWT")
        void login_withPhone_returnsToken() throws Exception {
            RegisterRequest registerRequest = TestDataFactory.registerRequest(
                    "login.phone@test.com",
                    "03001112233");
            AuthTestHelper.registerAndObtainToken(mockMvc, objectMapper, registerRequest);

            LoginRequest loginRequest = new LoginRequest("03001112233", TestConstants.VALID_PASSWORD);

            mockMvc.perform(post("/api/auth/login")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(loginRequest)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.token").isNotEmpty());
        }

        @Test
        @DisplayName("TC-LOG-03: wrong password returns 400")
        void login_wrongPassword_returnsBadRequest() throws Exception {
            AuthTestHelper.registerAndObtainToken(mockMvc, objectMapper);

            LoginRequest loginRequest = new LoginRequest(
                    TestConstants.USER_EMAIL,
                    "wrong-password");

            mockMvc.perform(post("/api/auth/login")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(loginRequest)))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.message").value("Invalid password"));
        }

        @Test
        @DisplayName("TC-LOG-04: unknown user returns 400")
        void login_unknownUser_returnsBadRequest() throws Exception {
            LoginRequest loginRequest = new LoginRequest(
                    "ghost.user@test.com",
                    TestConstants.VALID_PASSWORD);

            mockMvc.perform(post("/api/auth/login")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(loginRequest)))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.message").value("User not found"));
        }
    }

    @Nested
    @DisplayName("Change password flow")
    class ChangePasswordFlow {

        @Test
        @DisplayName("TC-PWD-01: authenticated user can change password and login with new password")
        void changePassword_thenLoginWithNewPassword_succeeds() throws Exception {
            RegisterRequest registerRequest = TestDataFactory.registerRequest(
                    "pwd.change@test.com",
                    "03004445566");
            AuthTestHelper.registerAndObtainToken(mockMvc, objectMapper, registerRequest);

            LoginRequest loginRequest = new LoginRequest(
                    "pwd.change@test.com",
                    TestConstants.VALID_PASSWORD);
            MvcResult loginResult = mockMvc.perform(post("/api/auth/login")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(loginRequest)))
                    .andExpect(status().isOk())
                    .andReturn();
            String token = objectMapper.readValue(
                            loginResult.getResponse().getContentAsString(), AuthResponse.class)
                    .getToken();

            ChangePasswordRequest changeRequest = TestDataFactory.validChangePasswordRequest();

            mockMvc.perform(put("/api/auth/change-password")
                            .header("Authorization", "Bearer " + token)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(changeRequest)))
                    .andExpect(status().isOk());

            mockMvc.perform(post("/api/auth/login")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(
                                    new LoginRequest("pwd.change@test.com", TestConstants.VALID_PASSWORD))))
                    .andExpect(status().isBadRequest());

            mockMvc.perform(post("/api/auth/login")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(
                                    new LoginRequest("pwd.change@test.com", TestConstants.NEW_PASSWORD))))
                    .andExpect(status().isOk());
        }

        @Test
        @DisplayName("TC-PWD-02: wrong current password returns 400")
        void changePassword_wrongCurrentPassword_returnsBadRequest() throws Exception {
            String token = AuthTestHelper.registerAndObtainToken(
                    mockMvc,
                    objectMapper,
                    TestDataFactory.registerRequest("pwd.wrong@test.com", "03007778899"));

            ChangePasswordRequest changeRequest = new ChangePasswordRequest(
                    "wrong-current",
                    TestConstants.NEW_PASSWORD);

            mockMvc.perform(put("/api/auth/change-password")
                            .header("Authorization", "Bearer " + token)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(changeRequest)))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.message").value("Current password is wrong"));
        }
    }
}
