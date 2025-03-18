package com.taskmanagement.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.taskmanagement.entity.InvalidToken;

public interface InvalidTokenRepository extends JpaRepository<InvalidToken, Long> {
    boolean existsByToken(String token);
}
