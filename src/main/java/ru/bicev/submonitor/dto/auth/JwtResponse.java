package ru.bicev.submonitor.dto.auth;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Ответ с данными авторизации и jwt токеном")
public record JwtResponse(
        @Schema(description = "Jwt токен", example = "eyJhbGciOiJIUzI1NiJ9...") String token,
        @Schema(description = "Идентификатор пользователя", example = "1024") Long id,
        @Schema(description = "Имя пользователя", example = "user_123Ivan") String username) {

}
