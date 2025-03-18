package com.taskmanagement.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.taskmanagement.dto.request.AuthRequest;
import com.taskmanagement.dto.request.UserRequest;
import com.taskmanagement.dto.response.AuthResponse;
import com.taskmanagement.dto.response.UserResponse;
import com.taskmanagement.service.AuthService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import jakarta.validation.Valid;
import java.util.Set;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(AuthController.class)
@ExtendWith(MockitoExtension.class)
class AuthControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Mock
    private AuthService authService;

    @Autowired
    private ObjectMapper objectMapper;

    private AuthRequest authRequest;
    private AuthResponse authResponse;
    private UserRequest userRequest;
    private UserResponse userResponse;

    @BeforeEach
    void setUp() {
        authRequest = new AuthRequest();
        authRequest.setEmail("testuser@example.com");
        authRequest.setPassword("password");

        authResponse = new AuthResponse();
        authResponse.setAccessToken("test-access-token");
        authResponse.setRefreshToken("test-refresh-token");
        authResponse.setId(1L);
        authResponse.setEmail("testuser@example.com");
        authResponse.setName("Test User");
        authResponse.setRoles(Set.of("ROLE_USER"));

        userRequest = new UserRequest();
        userRequest.setName("New User");
        userRequest.setEmail("newuser@example.com");
        userRequest.setPassword("password");
        userRequest.setRoles(Set.of("ROLE_USER"));

        userResponse = new UserResponse();
        userResponse.setId(1L);
        userResponse.setName("New User");
        userResponse.setEmail("newuser@example.com");
        userResponse.setRoles(Set.of("ROLE_USER"));
    }

    @Test
    void login_ShouldReturnTokens() throws Exception {
        when(authService.login(any(AuthRequest.class))).thenReturn(authResponse);

        mockMvc.perform(post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(authRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.accessToken").value(authResponse.getAccessToken()))
                .andExpect(jsonPath("$.refreshToken").value(authResponse.getRefreshToken()));
    }

    @Test
    void register_ShouldReturnUserDetails() throws Exception {
        when(authService.register(any(UserRequest.class))).thenReturn(userResponse);

        mockMvc.perform(post("/api/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(userRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(userResponse.getId()))
                .andExpect(jsonPath("$.name").value(userResponse.getName()))
                .andExpect(jsonPath("$.email").value(userResponse.getEmail()));
    }

    @Test
    void refresh_ShouldReturnNewTokens() throws Exception {
        when(authService.refreshToken(anyString())).thenReturn(authResponse);

        mockMvc.perform(post("/api/auth/refresh")
                .header("Authorization", "Bearer refresh-token"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.accessToken").value(authResponse.getAccessToken()));
    }

    @Test
    void logout_ShouldReturnNoContent() throws Exception {
        doNothing().when(authService).logout(anyString());

        mockMvc.perform(post("/api/auth/logout")
                .header("Authorization", "Bearer test-token"))
                .andExpect(status().isOk());
    }
}