package com.taskmanagement.dto.response;

import java.util.Set;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor 
@AllArgsConstructor 
public class AuthResponse {
    private String accessToken;
    private String refreshToken;
    private Long id;
    private String email;
    private String name;
    private Set<String> roles;
}