package com.cms.support;

import com.cms.model.dto.AuthResponse;
import com.cms.model.dto.RegisterRequest;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

public final class AuthTestHelper {

    private AuthTestHelper() {
    }

    public static String registerAndObtainToken(MockMvc mockMvc, ObjectMapper objectMapper) throws Exception {
        return registerAndObtainToken(mockMvc, objectMapper, TestDataFactory.validRegisterRequest());
    }

    public static String registerAndObtainToken(
            MockMvc mockMvc,
            ObjectMapper objectMapper,
            RegisterRequest request) throws Exception {

        MvcResult result = mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andReturn();

        AuthResponse response = objectMapper.readValue(
                result.getResponse().getContentAsString(),
                AuthResponse.class);

        return response.getToken();
    }
}
