package ru.bicev.submonitor.dto.error;

import java.time.LocalDateTime;
import java.util.Map;

import com.fasterxml.jackson.annotation.JsonInclude;

import lombok.Builder;

@Builder
public record ErrorResponse(
        int status,
        String message,
        LocalDateTime timestamp,
        @JsonInclude(JsonInclude.Include.NON_NULL) Map<String, String> errors) {

}
