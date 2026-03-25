package ru.bicev.submonitor.dto.auth;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;

public record SingupRequest(
        @NotEmpty @Size(min = 6, message = "Username must be at least 6 characters long") String username,
        @NotEmpty @Size(min = 8, message = "Password must be at least 8 characters") String password,
        @Email(message = "Invalid email format") String email) {

}
