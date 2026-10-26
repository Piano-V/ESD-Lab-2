package com.esd.lab2.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

/**
 * S2 — Request body for POST /api/auth/login.
 */
public record LoginRequest(

        @NotBlank
        @Email
        String email,

        @NotBlank
        String password
) {}
