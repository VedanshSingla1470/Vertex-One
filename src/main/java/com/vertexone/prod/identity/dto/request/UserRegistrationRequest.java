
package com.vertexone.prod.identity.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record UserRegistrationRequest(

        @NotBlank(message = "Email is required")
        @Email(message = "Enter a valid email address")
        @Size(max = 150)
        String email,

        @NotBlank(message = "Password is required")
        @Size(min = 8, max = 72, message = "Password must contain 8-72 characters")
        String password,

        @NotBlank(message = "First name is required")
        @Size(max = 80)
        String firstName,

        @NotBlank(message = "Last name is required")
        @Size(max = 80)
        String lastName

) {}
