package ru.bicev.submonitor.dto.analytics;

import java.math.BigDecimal;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Ответ с данными статистики по категории")
public record CategoryStatResponse(
        @Schema(description = "Название категории", example = "EDUCATION") String category,
        @Schema(description = "Сумма трат в указанной категории", example = "10.99") BigDecimal total) {

}
