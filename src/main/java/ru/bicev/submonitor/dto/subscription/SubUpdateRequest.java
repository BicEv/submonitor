package ru.bicev.submonitor.dto.subscription;

import java.math.BigDecimal;
import java.time.LocalDate;

import io.swagger.v3.oas.annotations.media.Schema;
import ru.bicev.submonitor.entity.enums.BillingPeriod;

@Schema(description = "Запрос на изменение данных подписки, указываются только поля, которые нужно изменить")
public record SubUpdateRequest(
        @Schema(description = "Новая стоимость подписки", example = "10.99") BigDecimal price,
        @Schema(description = "Новая валюта подписки", example = "USD") String currency,
        @Schema(description = "Новый платежный период", example = "YEARLY") BillingPeriod billingPeriod,
        @Schema(description = "Новая дата следующего платежа", example = "2028-06-02") LocalDate nextPayment,
        @Schema(description = "Новое значение активна ли подписка", example = "true") Boolean isActive) {

}
