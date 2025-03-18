package com.taskmanagement.service;

import com.taskmanagement.dto.request.UserRequest;
import com.taskmanagement.dto.response.UserResponse;
import com.taskmanagement.entity.enums.UserRole;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface UserService {
    Page<UserResponse> getAllUsers(Pageable pageable);
    UserResponse getCurrentUser();
    UserResponse updateUserRole(Long userId, UserRole role);
    UserResponse getUserById(Long id);
    UserResponse updateUser(Long id, UserRequest request);
    void deleteUser(Long id);
}