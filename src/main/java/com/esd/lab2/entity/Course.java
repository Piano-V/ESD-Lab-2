package com.esd.lab2.entity;

import jakarta.persistence.*;

@Entity
@Table(name = "courses")
public class Course {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 20)
    private String code;

    @Column(nullable = false, length = 150)
    private String title;

    @Column(length = 1000)
    private String description;

    // ── Constructors ──────────────────────────────────────────────────────────

    public Course() {}

    public Course(String code, String title, String description) {
        this.code        = code;
        this.title       = title;
        this.description = description;
    }

    // ── Getters & Setters ─────────────────────────────────────────────────────

    public Long getId()                        { return id; }
    public String getCode()                    { return code; }
    public void setCode(String code)           { this.code = code; }
    public String getTitle()                   { return title; }
    public void setTitle(String title)         { this.title = title; }
    public String getDescription()             { return description; }
    public void setDescription(String desc)    { this.description = desc; }
}
