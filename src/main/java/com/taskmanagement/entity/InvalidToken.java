package com.taskmanagement.entity;

import java.time.LocalDateTime;

import com.taskmanagement.entity.enums.UserRole;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import com.taskmanagement.security.JwtTokenProvider;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
public class InvalidToken {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String token;

    @Column(nullable = false)
    private LocalDateTime expiresAt;
    
    public InvalidToken(String token, LocalDateTime expiresAt) {
        this.token = token;
        this.expiresAt = expiresAt;
    }
}