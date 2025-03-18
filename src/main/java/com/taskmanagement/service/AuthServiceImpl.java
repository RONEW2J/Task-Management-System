package com.taskmanagement.service;

import com.taskmanagement.dto.request.AuthRequest;
import com.taskmanagement.dto.request.UserRequest;
import com.taskmanagement.dto.response.AuthResponse;
import com.taskmanagement.dto.response.UserResponse;
import com.taskmanagement.entity.InvalidToken;
import com.taskmanagement.entity.Role;
import com.taskmanagement.entity.User;
import com.taskmanagement.entity.enums.UserRole;
import com.taskmanagement.exception.ValidationException;
import com.taskmanagement.repository.InvalidTokenRepository;
import com.taskmanagement.repository.RoleRepository;
import com.taskmanagement.repository.UserRepository;
import com.taskmanagement.security.JwtTokenProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class AuthServiceImpl implements AuthService { 

    @Autowired
    private AuthenticationManager authenticationManager;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private RoleRepository roleRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private JwtTokenProvider jwtTokenProvider;

    @Override
    public AuthResponse login(AuthRequest authRequest) {
        Authentication authentication = authenticationManager.authenticate(
            new UsernamePasswordAuthenticationToken(
                authRequest.getEmail(),
                authRequest.getPassword()
            )
        );
        SecurityContextHolder.getContext().setAuthentication(authentication);
        String jwt = jwtTokenProvider.generateToken(authentication);
        User user = userRepository.findByEmail(authRequest.getEmail())
            .orElseThrow(() -> new ValidationException("User not found"));
        
        Set<String> roles = user.getRoles().stream()
            .map(role -> role.getName().name())
            .collect(Collectors.toSet());
        
            return new AuthResponse(
                jwt, 
                null,
                user.getId(), 
                user.getEmail(), 
                user.getUsername(), 
                roles
            );
    }

    @Override
    @Transactional
    public UserResponse register(UserRequest userRequest) {
        if (userRepository.existsByEmail(userRequest.getEmail())) {
            throw new ValidationException("Email is already taken");
        }
        User user = new User();
        user.setUsername(userRequest.getName());
        user.setEmail(userRequest.getEmail());
        user.setPassword(passwordEncoder.encode(userRequest.getPassword()));

        Set<Role> roles = new HashSet<>();
        if (userRequest.getRoles() != null && !userRequest.getRoles().isEmpty()) {
            roles = userRequest.getRoles().stream()
                    .map(roleName -> roleRepository.findByName(UserRole.valueOf(roleName))
                            .orElseThrow(() -> new ValidationException("Role not found")))
                    .collect(Collectors.toSet());
        } else {
            Role userRole = roleRepository.findByName(UserRole.ROLE_USER)
                    .orElseThrow(() -> new ValidationException("Default role not found"));
            roles.add(userRole);
        }
        user.setRoles(roles);
        User savedUser = userRepository.save(user);
        return new UserResponse(
            savedUser.getId(), 
            savedUser.getUsername(), 
            savedUser.getEmail(),
            savedUser.getRoles().stream()
                .map(role -> role.getName().name())
                .collect(Collectors.toSet())
        );
    }

    @Autowired
    private InvalidTokenRepository invalidTokenRepository;

    @Override
    public AuthResponse refreshToken(String refreshToken) {
        if (!jwtTokenProvider.validateRefreshToken(refreshToken)) {
            throw new ValidationException("Invalid refresh token");
        }

        String email = jwtTokenProvider.getUsernameFromToken(refreshToken);
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ValidationException("User not found"));

        Authentication authentication = new UsernamePasswordAuthenticationToken(
                email,
                null,
                user.getAuthorities()
        );

        String newJwt = jwtTokenProvider.generateToken(authentication);
        String newRefreshToken = jwtTokenProvider.generateRefreshToken(authentication);
        
        return new AuthResponse(
            newJwt, 
            newRefreshToken,
            user.getId(), 
            user.getEmail(), 
            user.getUsername(), 
            getRoleNames(user.getRoles())
        );
    }

    
    @Override
    public void logout(String token) {
        invalidTokenRepository.save(new InvalidToken(token, getExpirationFromToken(token)));
    }

    private Set<String> getRoleNames(Set<Role> roles) {
        return roles.stream()
                .map(role -> role.getName().name())
                .collect(Collectors.toSet());
    }

    private LocalDateTime getExpirationFromToken(String token) {
        return jwtTokenProvider.getExpirationFromToken(token);  
    }
}