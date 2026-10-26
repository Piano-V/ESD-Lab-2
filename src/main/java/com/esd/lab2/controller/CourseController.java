package com.esd.lab2.controller;

import com.esd.lab2.dto.CourseRequest;
import com.esd.lab2.dto.CourseResponse;
import com.esd.lab2.service.CourseService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Provides GET /api/courses (public) and POST /api/courses (currently unprotected).
 *
 * NOTE: In Slice 4 you will restrict POST /api/courses to ADMIN users only.
 *       Do NOT modify this file until you reach Slice 4.
 */
@RestController
@RequestMapping("/api/courses")
public class CourseController {

    private final CourseService courseService;

    public CourseController(CourseService courseService) {
        this.courseService = courseService;
    }

    /** P1 – public */
    @GetMapping
    public ResponseEntity<List<CourseResponse>> listCourses() {
        return ResponseEntity.ok(courseService.findAll());
    }

    @GetMapping("/{id}")
    public ResponseEntity<CourseResponse> getCourseById(@PathVariable Long id) {
        return ResponseEntity.ok(courseService.findById(id));
    }

    /** P3 – will be ADMIN-only after Slice 4 */
    @PostMapping
    public ResponseEntity<CourseResponse> createCourse(@Valid @RequestBody CourseRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(courseService.create(request));
    }
}
