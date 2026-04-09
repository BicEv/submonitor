package ru.bicev.submonitor.dto.payment;

import java.math.BigDecimal;
import java.time.LocalDate;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

@Schema(description = "Запрос на создание платежного лога")
public record PaymentCreateLogRequest(
                @Schema(description = "Идентификатор подписки", example = "101") @NotNull(message = "Subscription id cannot be null") @Positive(message = "Subscription id cannot be zero or negative") Long subscriptionId,
                @Schema(description = "Стоимость подписки", example = "11.99") @Positive(message = "Amount cannot be zero or negative") BigDecimal amount,
                @Schema(description = "Дата платежа", example = "2026-01-05") @NotNull(message = "Payment date cannot be null") LocalDate paymentDate) {

}
