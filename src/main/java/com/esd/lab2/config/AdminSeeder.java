package com.esd.lab2.config;

import com.esd.lab2.entity.Role;
import com.esd.lab2.entity.User;
import com.esd.lab2.repository.UserRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

/**
 * Creates a single ADMIN user at startup if one does not already exist.
 * Credentials come from application.yml — never hardcoded.
 */
@Component
public class AdminSeeder implements ApplicationRunner {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Value("${admin.email:admin@esd.local}")
    private String adminEmail;

    @Value("${admin.password}")
    private String adminPassword;

    public AdminSeeder(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository  = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public void run(ApplicationArguments args) {
        String email = adminEmail.trim().toLowerCase();
        if (userRepository.existsByEmail(email)) {
            return; // already seeded
        }
        User admin = new User(
                email,
                passwordEncoder.encode(adminPassword),
                "Admin",
                Role.ADMIN
        );
        userRepository.save(admin);
        System.out.println("[AdminSeeder] Admin user created: " + email);
    }
}
