package ru.bicev.submonitor.dto.analytics;

import java.math.BigDecimal;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Сумма трат")
public record TotalResponse(
        @Schema(description = "Сумма трат", example = "115.78") BigDecimal total) {

}
