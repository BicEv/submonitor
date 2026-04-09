package ru.bicev.submonitor.dto.auth;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;

public record SingupRequest(
                @Schema(description = "Уникальное имя пользователя", example = "user_123Ivan") @NotEmpty @Size(min = 6, message = "Username must be at least 6 characters long") String username,
                @Schema(description = "Пароль пользователя", example = "80085AreAwesome") @NotEmpty @Size(min = 8, message = "Password must be at least 8 characters") String password,
                @Schema(description = "Уникальное email пользователя", example = "123Ivan@gmail.com") @Email(message = "Invalid email format") String email) {

}
