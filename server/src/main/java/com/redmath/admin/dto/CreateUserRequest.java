package com.redmath.admin.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Setter
public class CreateUserRequest {

    @Getter
    @NotBlank(message = "Name is required")
    @Size(max = 100)
    private String name;

    @NotBlank(message = "Email is required")
    @Email(message = "Invalid email")
    @Size(max = 150)
    private String email;

    @Getter
    @NotBlank(message = "Password is required")
    @Size(min = 6, max = 100)
    private String password;

    @Getter
    @NotBlank(message = "Address is required")
    @Size(max = 250)
    private String address;

    public CreateUserRequest() {
    }

    public String getEmail() {
        return email.toLowerCase();
    }

}
