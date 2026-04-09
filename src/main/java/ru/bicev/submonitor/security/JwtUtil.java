package ru.bicev.submonitor.security;

import java.nio.charset.StandardCharsets;
import java.util.Date;

import javax.crypto.SecretKey;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;

import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import lombok.extern.slf4j.Slf4j;

/**
 * Класс отвечающий за генерацию, валидацию и извлечение идентификатора
 * подписчика-пользователя из jwt токена
 */
@Component
@Slf4j
public class JwtUtil {

    @Value("${submonitor.jwt.secret}")
    private String jwtSecret;

    @Value("${submonitor.jwt.expirationMs}")
    private int jwtExpirationMs;

    private SecretKey getSigningKey() {
        return Keys.hmacShaKeyFor(jwtSecret.getBytes(StandardCharsets.UTF_8));
    }

    /**
     * Метод генерирующий jwt токен из переданной аутентификации
     * 
     * @param authentication аутентификация пользователя
     * @return строка - jwt токен
     */
    public String generateToken(Authentication authentication) {
        UserDetailsImpl userPrincipal = (UserDetailsImpl) authentication.getPrincipal();

        return Jwts.builder()
                .subject(userPrincipal.getUsername())
                .claim("userId", userPrincipal.getId())
                .issuedAt(new Date())
                .expiration(new Date((new Date().getTime() + jwtExpirationMs)))
                .signWith(getSigningKey())
                .compact();
    }

    /**
     * Метод извлекающий имя пользователя из jwt токена
     * 
     * @param token jwt токен
     * @return имя пользователя
     */
    public String getUsernameFromJwtToken(String token) {
        return Jwts.parser()
                .verifyWith(getSigningKey())
                .build()
                .parseSignedClaims(token)
                .getPayload()
                .getSubject();
    }

    /**
     * Метод извлекающий идентификатор пользователя из токена
     * 
     * @param token jwt токен
     * @return идентификатор пользователя
     */
    public Long getIdFromJwtToken(String token) {
        return Jwts.parser()
                .verifyWith(getSigningKey())
                .build()
                .parseSignedClaims(token)
                .getPayload()
                .get("userId", Long.class);
    }

    /**
     * Метод выполняющий проверку jwt токена на валидность
     * 
     * @param token jwt токен
     * @return true если токен валиден, false если нет
     */
    public boolean validateJwtToken(String token) {
        try {
            Jwts.parser().verifyWith(getSigningKey()).build().parseSignedClaims(token);
            return true;
        } catch (JwtException | IllegalArgumentException e) {
            log.warn("Invalid token was passed: {}", token);
        }
        return false;
    }

}
