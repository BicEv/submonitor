package ru.bicev.submonitor.dto.analytics;

import java.math.BigDecimal;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Ответ с данными статистики по сервису")
public record ServiceStatResponse(@Schema(description = "Название сервиса", example = "NETFLIX") String service,
        @Schema(description = "Сумма трат на указанный сервис", example = "15.99") BigDecimal total) {

}
