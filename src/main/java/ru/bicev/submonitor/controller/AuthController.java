package ru.bicev.submonitor.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import ru.bicev.submonitor.dto.auth.JwtResponse;
import ru.bicev.submonitor.dto.auth.LoginRequest;
import ru.bicev.submonitor.dto.auth.SingupRequest;
import ru.bicev.submonitor.dto.error.ErrorResponse;
import ru.bicev.submonitor.service.AuthService;

/**
 * Контроллер для регистрации и логина пользователя
 */
@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
@Tag(name = "Авторизация", description = "Регистрация и авторизация пользователей")
public class AuthController {

    private final AuthService authService;

    /**
     * Метод регистрирующий нового пользователя
     * 
     * @param request запрос на создание нового пользователя, содержащий юзернейм,
     *                email и пароль
     * @return ответ с jwt-токеном, идентификатором созданного пользователя и его
     *         юзернеймом
     */
    @PostMapping("/register")
    @Operation(summary = "Регистрация пользователя", description = "Позволяет зарегистрировать нового пользователя")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Пользователь успешно создан"),
            @ApiResponse(responseCode = "409", description = "Пользователь с указанным именем или email уже существует", content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    public ResponseEntity<JwtResponse> register(
            @Valid @RequestBody SingupRequest request) {
        var created = authService.registerSubscriber(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    /**
     * Метод для авторизации пользователя и получения jwt-токена
     * 
     * @param loginRequest запрос на авторизацию пользователя, содержащий юзернейм и
     *                     пароль
     * @return ответ с jwt-токеном, идентификатором созданного пользователя и его
     *         юзернеймом
     */
    @Operation(summary = "Авторизация пользователя", description = "Позволяет авторизировать пользователя")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Пользователь успешно авторизован"),
            @ApiResponse(responseCode = "401", description = "Имя пользователя или пароль неверны", content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    @PostMapping("/login")
    public ResponseEntity<JwtResponse> login(
            @Valid @RequestBody LoginRequest loginRequest) {
        var response = authService.login(loginRequest);
        return ResponseEntity.status(HttpStatus.OK).body(response);
    }

}
