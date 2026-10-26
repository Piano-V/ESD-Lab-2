package com.esd.lab2.service;

import com.esd.lab2.dto.LoginRequest;
import com.esd.lab2.dto.LoginResponse;
import com.esd.lab2.dto.SignupRequest;
import com.esd.lab2.dto.UserResponse;
import com.esd.lab2.entity.Role;
import com.esd.lab2.entity.User;
import com.esd.lab2.exception.DuplicateResourceException;
import com.esd.lab2.exception.InvalidCredentialsException;
import com.esd.lab2.repository.UserRepository;
import com.esd.lab2.security.JwtService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

/**
 * S1 — FILL IN MANUALLY (no AI for this slice).
 *
 * Steps to implement signup():
 *   1. Normalise the email: trim it and convert to lowercase.
 *   2. Check if the email already exists using userRepository.existsByEmail(...).
 *      If it does, throw DuplicateResourceException with a helpful message.
 *   3. Hash the password using passwordEncoder.encode(...).
 *      Never store plaintext. Never use new BCryptPasswordEncoder() — use the injected bean.
 *   4. Create a new User entity. Role must always be STUDENT — do not read role from the request.
 *   5. Save the user with userRepository.save(...).
 *   6. Return a UserResponse built from the saved entity.
 *
 * S2: login() implemented with JwtService.
 */
@Service
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;

    public AuthService(UserRepository userRepository, PasswordEncoder passwordEncoder, JwtService jwtService) {
        this.userRepository  = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtService      = jwtService;
    }

    /**
     * Registers a new user. Role is always STUDENT regardless of what the request contains.
     */
    public UserResponse signup(SignupRequest request) {
        // 1: normalise the email (trim + lowercase)
        String email = request.email().trim().toLowerCase();

        // 2: check for duplicate email and throw DuplicateResourceException if found
        if (userRepository.existsByEmail(email)) {
            throw new DuplicateResourceException("An account with email " + email + " already exists");
        }

        // 3: hash the password using the injected passwordEncoder bean
        String hashedPassword = passwordEncoder.encode(request.password());

        // 4: build and save the User entity (role = Role.STUDENT always)
        User user = new User();
        user.setEmail(email);
        user.setPasswordHash(hashedPassword);
        user.setFullName(request.fullName().trim());
        user.setRole(Role.STUDENT);

        User savedUser = userRepository.save(user);

        // 5: return a UserResponse — never return the User entity directly
        return toResponse(savedUser);
    }

    /**
     * S2: Authenticates a user and issues a JWT.
     * Throws InvalidCredentialsException with identical message for unknown email or wrong password.
     */
    public LoginResponse login(LoginRequest request) {
        String email = request.email().trim().toLowerCase();

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new InvalidCredentialsException("Invalid email or password"));

        if (!passwordEncoder.matches(request.password(), user.getPasswordHash())) {
            throw new InvalidCredentialsException("Invalid email or password");
        }

        String token = jwtService.generateToken(user);
        return new LoginResponse(token, "Bearer", jwtService.getExpirationSeconds());
    }

    // ── helper ────────────────────────────────────────────────────────────────

    private UserResponse toResponse(User user) {
        return new UserResponse(
                user.getId(),
                user.getEmail(),
                user.getFullName(),
                user.getRole(),
                user.getCreatedAt()
        );
    }
}
