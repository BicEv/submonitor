package ru.bicev.submonitor.dto.subscription;

import java.math.BigDecimal;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Ответ содержащий данные подписки")
public record SubscriptionDto(
                @Schema(description = "Идентификатор подписки", example = "999") Long id,
                @Schema(description = "Стоимость подписки", example = "7.99") BigDecimal price,
                @Schema(description = "Валюта оплаты подписки", example = "RUB") String currency,
                @Schema(description = "Платежный период подписки", example = "YEARLY") String billingPeriod,
                @Schema(description = "Дата следующего платежа", example = "2027-09-03") String nextPayment,
                @Schema(description = "Является ли подписка активной", example = "false") boolean isActive) {

}
