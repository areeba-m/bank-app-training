package com.redmath.authentication.dto;

import jakarta.validation.constraints.NotBlank;

public record SetNewPasswordRequest(
        @NotBlank String resetToken,
        @NotBlank String newPassword
) {
}
