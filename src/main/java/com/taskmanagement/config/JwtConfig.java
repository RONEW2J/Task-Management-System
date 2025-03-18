package com.taskmanagement.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

@Configuration
public class JwtConfig {

    @Value("${app.jwt.secret}")
    private String jwtSecret;

    @Value("${app.jwt.expiration}")
    private int jwtExpiration;

    @Value("${app.jwt.header}")
    private String jwtHeader;

    @Value("${app.jwt.prefix}")
    private String jwtPrefix;

    public String getJwtSecret() {
        return jwtSecret;
    }

    public int getJwtExpiration() {
        return jwtExpiration;
    }

    public String getJwtHeader() {
        return jwtHeader;
    }

    public String getJwtPrefix() {
        return jwtPrefix;
    }
}