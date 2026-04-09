package ru.bicev.submonitor.dto.payment;

import java.math.BigDecimal;
import java.time.LocalDate;

import io.swagger.v3.oas.annotations.media.Schema;
import ru.bicev.submonitor.entity.enums.PaymentStatus;

@Schema(description = "Ответ с данными платежного лога")
public record PaymentLogDto(
        @Schema(description = "Идентификатор платежа", example = "501") Long id,
        @Schema(description = "Идентификатор подписки", example = "101") Long subsriptionId,
        @Schema(description = "Название подписки", example = "Netflix") String subscriptionName,
        @Schema(description = "Стоимость подписки", example = "10.99") BigDecimal amount,
        @Schema(description = "Дата платежа", example = "2026-01-01") LocalDate paymentDate,
        @Schema(description = "Статус платежа", example = "SUCCESS") PaymentStatus status) {

}
