package com.esd.lab2.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record CourseRequest(

        @NotBlank(message = "Course code is required")
        @Size(max = 20, message = "Code must be at most 20 characters")
        String code,

        @NotBlank(message = "Title is required")
        @Size(max = 150, message = "Title must be at most 150 characters")
        String title,

        @Size(max = 1000, message = "Description must be at most 1000 characters")
        String description
) {}
