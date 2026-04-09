package ru.bicev.submonitor.dto.payment;

import java.math.BigDecimal;
import java.time.LocalDate;

import io.swagger.v3.oas.annotations.media.Schema;
import ru.bicev.submonitor.entity.enums.PaymentStatus;

@Schema(description = "Запрос на изменение платежного лога, указываются только поля, которые нужно изменить")
public record PaymentUpdateLogRequest(
        @Schema(description = "Идентификатор подписки", example = "101") Long subscriptionId,
        @Schema(description = "Стоимость подписки", example = "11.99") BigDecimal amount,
        @Schema(description = "Дата платежа", example = "2026-01-05") LocalDate paymentDate,
        @Schema(description = "Статус платежа", example = "SUCCESS") PaymentStatus status) {

}
