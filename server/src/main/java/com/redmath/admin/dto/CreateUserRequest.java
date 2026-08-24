package com.redmath.admin.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record CreateUserRequest(

        @NotBlank(message = "Name is required")
        @Size(max = 100)
        String name,

        @NotBlank(message = "Email is required")
        @Email(message = "Invalid email")
        @Size(max = 150)
        String email,

        @NotBlank(message = "Address is required")
        @Size(max = 250)
        String address

) {
    public CreateUserRequest {
        if (email != null) {
            email = email.toLowerCase();
        }
    }
}
