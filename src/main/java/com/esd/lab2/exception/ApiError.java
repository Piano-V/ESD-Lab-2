package com.esd.lab2.exception;

import java.time.Instant;
import java.util.Map;

/**
 * Standard error response shape used by all API errors.
 * fieldErrors is only populated for 400 validation errors.
 */
public record ApiError(
        Instant timestamp,
        int status,
        String error,
        String message,
        String path,
        Map<String, String> fieldErrors
) {}
