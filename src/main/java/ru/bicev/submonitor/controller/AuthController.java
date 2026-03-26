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

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    @PostMapping("/register")
    public ResponseEntity<JwtResponse> register(@Valid @RequestBody SingupRequest request) {
        var created = authService.registerSubscriber(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    @PostMapping("/login")
    public ResponseEntity<JwtResponse> login(@Valid @RequestBody LoginRequest loginRequest) {
        var response = authService.login(loginRequest);
        return ResponseEntity.status(HttpStatus.OK).body(response);
    }

}
