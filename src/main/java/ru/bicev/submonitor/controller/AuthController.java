package ru.bicev.submonitor.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import ru.bicev.submonitor.dto.auth.JwtResponse;
import ru.bicev.submonitor.dto.auth.LoginRequest;
import ru.bicev.submonitor.dto.auth.SingupRequest;
import ru.bicev.submonitor.service.AuthService;

/**
 * Контроллер для регистрации и логина пользователя
 */
@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
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
    public ResponseEntity<JwtResponse> register(@Valid @RequestBody SingupRequest request) {
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
    @PostMapping("/login")
    public ResponseEntity<JwtResponse> login(@Valid @RequestBody LoginRequest loginRequest) {
        var response = authService.login(loginRequest);
        return ResponseEntity.status(HttpStatus.OK).body(response);
    }

}
