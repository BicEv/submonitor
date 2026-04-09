package ru.bicev.submonitor.dto.error;

import java.time.LocalDateTime;
import java.util.Map;

import com.fasterxml.jackson.annotation.JsonInclude;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;

@Schema(description = "Ответ с данными об ошибке")
@Builder
public record ErrorResponse(
        @Schema(description = "Код ошибки", example = "404") int status,
        @Schema(description = "Сообщение об ошибке", example = "Subscription not found") String message,
        @Schema(description = "Время возникновения ошибки", example = "2026-03-01 14:32:01", type = "string") LocalDateTime timestamp,
        @Schema(description = "Таблица с указаниями поле - сообщение об ошибке для ошибок валидации") @JsonInclude(JsonInclude.Include.NON_NULL) Map<String, String> errors) {

}
