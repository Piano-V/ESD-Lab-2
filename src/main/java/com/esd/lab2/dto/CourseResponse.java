package com.esd.lab2.dto;

/**
 * Response DTO for a Course. Never return the entity directly from a controller.
 */
public record CourseResponse(
        Long id,
        String code,
        String title,
        String description
) {}
