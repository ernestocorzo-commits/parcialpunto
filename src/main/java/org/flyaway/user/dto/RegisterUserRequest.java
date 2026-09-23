package org.flyaway.user.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

public record RegisterUserRequest(
        @NotBlank(message = "firstName is required")
        @Pattern(regexp = ".*[A-Z].*", message = "firstName must contain at least one uppercase letter (A-Z)")
        String firstName,

        @NotBlank(message = "lastName is required")
        @Pattern(regexp = ".*[A-Z].*", message = "lastName must contain at least one uppercase letter (A-Z)")
        String lastName,

        @NotBlank(message = "email is required")
        @Email(regexp = "^[^@\\s]+@[^@\\s]+\\.[^@\\s]+$", message = "email must be a valid email address")
        String email,

        @NotBlank(message = "password is required")
        @Pattern(regexp = "^(?=.*[A-Za-z])(?=.*\\d).{8,}$",
                message = "password must have at least 8 characters, 1 letter and 1 number")
        String password
) {
}
