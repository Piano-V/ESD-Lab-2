package com.esd.lab2.dto;

/**
 * S2 — Response body for POST /api/auth/login.
 */
public record LoginResponse(

        String accessToken,
        String tokenType,
        long expiresIn
) {
    public LoginResponse(String accessToken, long expiresIn) {
        this(accessToken, "Bearer", expiresIn);
    }
}
