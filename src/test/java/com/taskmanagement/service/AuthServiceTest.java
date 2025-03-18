package com.taskmanagement.service;

import com.taskmanagement.dto.request.AuthRequest;
import com.taskmanagement.dto.request.UserRequest;
import com.taskmanagement.dto.response.AuthResponse;
import com.taskmanagement.dto.response.UserResponse;
import com.taskmanagement.entity.Role;
import com.taskmanagement.entity.User;
import com.taskmanagement.entity.enums.UserRole;
import com.taskmanagement.exception.ValidationException;
import com.taskmanagement.repository.RoleRepository;
import com.taskmanagement.repository.UserRepository;
import com.taskmanagement.security.JwtTokenProvider;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Collections;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private RoleRepository roleRepository;

    @Mock
    private JwtTokenProvider jwtTokenProvider;

    @Mock
    private AuthenticationManager authenticationManager;
    
    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private AuthServiceImpl authService;

    private User testUser;
    private Role userRole;
    private Set<String> roleNames;

    @BeforeEach
    void setUp() {
        userRole = new Role();
        userRole.setId(1L);
        userRole.setName(UserRole.ROLE_USER);

        Set<Role> roles = new HashSet<>();
        roles.add(userRole);

        testUser = new User();
        testUser.setId(1L);
        testUser.setUsername("Test User");
        testUser.setEmail("test@example.com");
        testUser.setPassword("encodedPassword");
        testUser.setRoles(roles);
        
        roleNames = new HashSet<>();
        roleNames.add(UserRole.ROLE_USER.name());
    }

    @Test
    void login_WithValidCredentials_ShouldReturnAuthResponse() {
        AuthRequest authRequest = new AuthRequest();
        authRequest.setEmail("test@example.com");
        authRequest.setPassword("password");
        
        Authentication authentication = mock(Authentication.class);
        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
            .thenReturn(authentication);
        when(userRepository.findByEmail("test@example.com")).thenReturn(Optional.of(testUser));
        when(jwtTokenProvider.generateToken(authentication)).thenReturn("mocked-jwt-token");

        AuthResponse response = authService.login(authRequest);

        assertNotNull(response);
        assertEquals("mocked-jwt-token", response.getAccessToken());
        assertEquals(testUser.getId(), response.getId());
        assertEquals(testUser.getEmail(), response.getEmail());
        assertEquals(testUser.getUsername(), response.getName());
        assertTrue(response.getRoles().contains(UserRole.ROLE_USER.name()));
        
        verify(authenticationManager).authenticate(any(UsernamePasswordAuthenticationToken.class));
        verify(userRepository).findByEmail("test@example.com");
        verify(jwtTokenProvider).generateToken(authentication);
    }
    
    @Test
    void login_WithInvalidCredentials_ShouldThrowException() {
        AuthRequest authRequest = new AuthRequest();
        authRequest.setEmail("test@example.com");
        authRequest.setPassword("wrongpassword");
        
        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                .thenThrow(new ValidationException("Invalid credentials"));

        assertThrows(ValidationException.class, () -> authService.login(authRequest));
        
        verify(authenticationManager).authenticate(any(UsernamePasswordAuthenticationToken.class));
        verifyNoInteractions(jwtTokenProvider);
    }

    @Test
    void register_WithValidData_ShouldReturnUserResponse() {
        UserRequest userRequest = new UserRequest();
        userRequest.setName("New User");
        userRequest.setEmail("new@example.com");
        userRequest.setPassword("newpassword");
        
        when(userRepository.existsByEmail("new@example.com")).thenReturn(false);
        when(roleRepository.findByName(UserRole.ROLE_USER)).thenReturn(Optional.of(userRole));
        when(passwordEncoder.encode("newpassword")).thenReturn("encodedPassword");
        when(userRepository.save(any(User.class))).thenReturn(testUser);

        UserResponse response = authService.register(userRequest);

        assertNotNull(response);
        assertEquals(testUser.getId(), response.getId());
        assertEquals(testUser.getEmail(), response.getEmail());
        assertEquals(testUser.getUsername(), response.getName());
        assertTrue(response.getRoles().contains(UserRole.ROLE_USER.name()));
        
        verify(userRepository).existsByEmail("new@example.com");
        verify(passwordEncoder).encode("newpassword");
        verify(roleRepository).findByName(UserRole.ROLE_USER);
        verify(userRepository).save(any(User.class));
    }

    @Test
    void register_WithExistingEmail_ShouldThrowException() {
        UserRequest userRequest = new UserRequest();
        userRequest.setName("Test User");
        userRequest.setEmail("test@example.com");
        userRequest.setPassword("password");
        
        when(userRepository.existsByEmail("test@example.com")).thenReturn(true);

        assertThrows(ValidationException.class, () -> authService.register(userRequest));
        
        verify(userRepository).existsByEmail("test@example.com");
        verifyNoMoreInteractions(userRepository);
        verifyNoInteractions(passwordEncoder, roleRepository);
    }
    
    @Test
    void refreshToken_WithValidToken_ShouldReturnNewAuthResponse() {
        String refreshToken = "valid-refresh-token";
        
        when(jwtTokenProvider.validateRefreshToken(refreshToken)).thenReturn(true);
        when(jwtTokenProvider.getUsernameFromToken(refreshToken)).thenReturn("test@example.com");
        when(userRepository.findByEmail("test@example.com")).thenReturn(Optional.of(testUser));
        when(jwtTokenProvider.generateToken(any(Authentication.class))).thenReturn("new-access-token");
        when(jwtTokenProvider.generateRefreshToken(any(Authentication.class))).thenReturn("new-refresh-token");

        AuthResponse response = authService.refreshToken(refreshToken);

        assertNotNull(response);
        assertEquals("new-access-token", response.getAccessToken());
        assertEquals("new-refresh-token", response.getRefreshToken());
        assertEquals(testUser.getId(), response.getId());
        assertEquals(testUser.getEmail(), response.getEmail());
        
        verify(jwtTokenProvider).validateRefreshToken(refreshToken);
        verify(jwtTokenProvider).getUsernameFromToken(refreshToken);
        verify(userRepository).findByEmail("test@example.com");
        verify(jwtTokenProvider).generateToken(any(Authentication.class));
        verify(jwtTokenProvider).generateRefreshToken(any(Authentication.class));
    }
    
    @Test
    void refreshToken_WithInvalidToken_ShouldThrowException() {
        String refreshToken = "invalid-refresh-token";
        
        when(jwtTokenProvider.validateRefreshToken(refreshToken)).thenReturn(false);

        assertThrows(ValidationException.class, () -> authService.refreshToken(refreshToken));
        
        verify(jwtTokenProvider).validateRefreshToken(refreshToken);
        verifyNoMoreInteractions(jwtTokenProvider);
        verifyNoInteractions(userRepository);
    }
}