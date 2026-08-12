package com.redmath.exception_handler.dto;

import java.time.Instant;

public record ErrorResponse(
        Instant timestamp,
        String message,
        int status,
        String path
) {}
