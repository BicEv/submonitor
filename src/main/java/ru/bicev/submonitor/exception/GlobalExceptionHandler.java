package ru.bicev.submonitor.exception;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import lombok.extern.slf4j.Slf4j;
import ru.bicev.submonitor.dto.error.ErrorResponse;

/**
 * Глобальный обработчик ошибок
 */
@RestControllerAdvice
@Slf4j
public class GlobalExceptionHandler {

    /**
     * Метод обрабатывающий NotFoundException.class
     * 
     * @param ex исключение, перехватываемое методом
     * @return ответ, содержащий статус, время ошибки и сообщение об ошибке
     */
    @ExceptionHandler(NotFoundException.class)
    public ResponseEntity<ErrorResponse> handleNotFound(NotFoundException ex) {
        log.warn("NotFoundException: {}", ex.getMessage());
        return buildResponse(HttpStatus.NOT_FOUND, ex.getMessage());
    }

    /**
     * Метод обрабатывающий DuplicateSubscriberException.class и
     * IllegalArgumentException.class
     * 
     * @param ex исключение, перехватываемое методом
     * @return ответ, содержащий статус, время ошибки и сообщение об ошибке
     */
    @ExceptionHandler({ DuplicateSubscriberException.class, IllegalArgumentException.class })
    public ResponseEntity<ErrorResponse> handleBadRequest(RuntimeException ex) {
        log.warn("DuplicateSubscriberException | IllegalArgumentException: {}", ex.getMessage());
        return buildResponse(HttpStatus.BAD_REQUEST, ex.getMessage());
    }

    /**
     * Метод обрабатывающий BadCredentialsException.class
     * 
     * @param ex исключение, перехватываемое методом
     * @return ответ, содержащий статус, время ошибки и сообщение об ошибке
     */
    @ExceptionHandler(BadCredentialsException.class)
    public ResponseEntity<ErrorResponse> handleBadCredentialsException(BadCredentialsException ex) {
        log.warn("BadCredentialsException: {}", ex.getMessage());
        return buildResponse(HttpStatus.UNAUTHORIZED, ex.getMessage());
    }

    /**
     * Метод обрабатывающий ошибки валидации MethodArgumentNotValidException.class
     * 
     * @param ex исключение, перехватываемое методом
     * @return ответ, содержащий статус, время ошибки и сообщение об ошибке
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleMathodArgumentNotValid(MethodArgumentNotValidException ex) {
        Map<String, String> errors = new HashMap<>();
        ex.getBindingResult().getAllErrors().forEach((error) -> {
            String fieldName = ((FieldError) error).getField();
            String errorMessage = error.getDefaultMessage();
            errors.put(fieldName, errorMessage);
        });
        log.warn("MethodArgumentNotValidException: {}", ex.getMessage());
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(
                ErrorResponse.builder()
                        .status(HttpStatus.BAD_REQUEST.value())
                        .message("Validation failed")
                        .timestamp(LocalDateTime.now())
                        .errors(errors)
                        .build());
    }

    /**
     * Метод обрабатывающий остальные исключения рантайма RuntimeException.class
     * 
     * @param ex исключение, перехватываемое методом
     * @return ответ, содержащий статус, время ошибки и сообщение об ошибке
     */
    @ExceptionHandler(RuntimeException.class)
    public ResponseEntity<ErrorResponse> handleRuntimeException(RuntimeException ex) {
        log.error("Unexpected error: {}", ex);
        return buildResponse(HttpStatus.INTERNAL_SERVER_ERROR, "Internal server error");
    }

    /**
     * Служебный метод, создающий сущность ответа, содержащую данные об ошибке
     * 
     * @param status  статус-код ошибки
     * @param message сообщение ошибки
     * @return сущность ответа
     */
    private ResponseEntity<ErrorResponse> buildResponse(HttpStatus status, String message) {
        return ResponseEntity.status(status)
                .body(
                        ErrorResponse.builder()
                                .status(status.value())
                                .message(message)
                                .timestamp(LocalDateTime.now())
                                .build());
    }

}
