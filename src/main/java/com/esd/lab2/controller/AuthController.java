package com.esd.lab2.controller;

import com.esd.lab2.dto.LoginRequest;
import com.esd.lab2.dto.LoginResponse;
import com.esd.lab2.dto.SignupRequest;
import com.esd.lab2.dto.UserResponse;
import com.esd.lab2.security.JwtService;
import com.esd.lab2.security.TokenDenylist;
import com.esd.lab2.service.AuthService;
import io.jsonwebtoken.Claims;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Date;

/**
 * S1 — FILL IN MANUALLY (no AI for this slice).
 *
 * This controller handles all /api/auth endpoints.
 * In S1 you add the signup endpoint.
 * In S2 (AI) you will add login and later logout.
 *
 */
@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthService authService;
    private final TokenDenylist tokenDenylist;
    private final JwtService jwtService;

    public AuthController(AuthService authService, TokenDenylist tokenDenylist, JwtService jwtService) {
        this.authService = authService;
        this.tokenDenylist = tokenDenylist;
        this.jwtService = jwtService;
    }

    /**
     * A1 — POST /api/auth/signup
     * Public endpoint. Returns 201 with UserResponse on success.
     */
    @PostMapping("/signup")
    public ResponseEntity<UserResponse> signup(@Valid @RequestBody SignupRequest request) {
        UserResponse response = authService.signup(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    /**
     * A2 — POST /api/auth/login
     * Public endpoint. Returns 200 with LoginResponse on success.
     */
    @PostMapping("/login")
    public ResponseEntity<LoginResponse> login(@Valid @RequestBody LoginRequest request) {
        LoginResponse response = authService.login(request);
        return ResponseEntity.ok(response);
    }

    /**
     * A3 — POST /api/auth/logout
     * Protected endpoint. Invalidates caller's JWT server-side via TokenDenylist.
     * Returns 204 No Content.
     */
    @PostMapping("/logout")
    public ResponseEntity<Void> logout(@RequestHeader(value = "Authorization", required = false) String authHeader) {
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            String token = authHeader.substring(7);
            try {
                Claims claims = jwtService.parseToken(token);
                String jti = claims.getId();
                Date expiration = claims.getExpiration();
                if (jti != null && expiration != null) {
                    tokenDenylist.revoke(jti, expiration.toInstant());
                }
            } catch (Exception ignored) {
                // If token cannot be parsed, no further revocation is required
            }
        }
        return ResponseEntity.noContent().build();
    }
}
