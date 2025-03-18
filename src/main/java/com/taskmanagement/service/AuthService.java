package com.taskmanagement.service;

import com.taskmanagement.dto.request.AuthRequest;
import com.taskmanagement.dto.request.UserRequest;
import com.taskmanagement.dto.response.AuthResponse;
import com.taskmanagement.dto.response.UserResponse;

public interface AuthService {
    AuthResponse login(AuthRequest authRequest);
    UserResponse register(UserRequest userRequest);
    AuthResponse refreshToken(String refreshToken);
    void logout(String token);
}