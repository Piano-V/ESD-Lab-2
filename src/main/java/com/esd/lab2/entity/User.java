package com.esd.lab2.entity;

import jakarta.persistence.*;
import java.time.Instant;

/**
 * Mapped to the "users" table.
 * Note: "user" is a reserved word in PostgreSQL — the table name must be "users".
 */
@Entity
@Table(name = "users")
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 255)
    private String email;

    @Column(name = "password_hash", nullable = false, length = 255)
    private String passwordHash;

    @Column(name = "full_name", nullable = false, length = 100)
    private String fullName;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private Role role;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @PrePersist
    private void prePersist() {
        this.createdAt = Instant.now();
    }

    // ── Constructors ──────────────────────────────────────────────────────────

    public User() {}

    public User(String email, String passwordHash, String fullName, Role role) {
        this.email        = email;
        this.passwordHash = passwordHash;
        this.fullName     = fullName;
        this.role         = role;
    }

    // ── Getters & Setters ─────────────────────────────────────────────────────

    public Long getId()                     { return id; }
    public String getEmail()                { return email; }
    public void setEmail(String email)      { this.email = email; }
    public String getPasswordHash()         { return passwordHash; }
    public void setPasswordHash(String h)   { this.passwordHash = h; }
    public String getFullName()             { return fullName; }
    public void setFullName(String name)    { this.fullName = name; }
    public Role getRole()                   { return role; }
    public void setRole(Role role)          { this.role = role; }
    public Instant getCreatedAt()           { return createdAt; }
}
