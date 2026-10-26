package com.esd.lab2.service;

import com.esd.lab2.dto.CourseRequest;
import com.esd.lab2.dto.CourseResponse;
import com.esd.lab2.entity.Course;
import com.esd.lab2.exception.DuplicateResourceException;
import com.esd.lab2.exception.ResourceNotFoundException;
import com.esd.lab2.repository.CourseRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class CourseService {

    private final CourseRepository courseRepository;

    public CourseService(CourseRepository courseRepository) {
        this.courseRepository = courseRepository;
    }

    public List<CourseResponse> findAll() {
        return courseRepository.findAll().stream()
                .map(this::toResponse)
                .toList();
    }

    public CourseResponse create(CourseRequest request) {
        if (courseRepository.existsByCode(request.code())) {
            throw new DuplicateResourceException(
                    "Course with code '" + request.code() + "' already exists");
        }
        Course course = new Course(request.code(), request.title(), request.description());
        return toResponse(courseRepository.save(course));
    }

    public CourseResponse findById(Long id) {
        Course course = courseRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Course not found: " + id));
        return toResponse(course);
    }

    // ── mapping ───────────────────────────────────────────────────────────────

    private CourseResponse toResponse(Course c) {
        return new CourseResponse(c.getId(), c.getCode(), c.getTitle(), c.getDescription());
    }
}
