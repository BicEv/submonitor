package ru.bicev.submonitor.dto.auth;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotEmpty;

public record LoginRequest(
                @Schema(description = "Имя пользователя", example = "user_123Ivan") @NotEmpty(message = "Username cannot be empty") String username,
                @Schema(description = "Пароль пользователя", example = "80085AreAwesome") @NotEmpty(message = "Password cannot be empty") String password) {

}
