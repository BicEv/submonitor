package ru.bicev.submonitor.dto.payment;

import java.math.BigDecimal;
import java.time.LocalDate;

import jakarta.validation.constraints.NotNull;
import ru.bicev.submonitor.entity.enums.PaymentStatus;

public record PaymentUpdateLogRequest(
        @NotNull(message = "Payment id cannot be null") Long id,
        Long subscriptionId,
        BigDecimal amount,
        LocalDate paymentDate,
        PaymentStatus status) {

}
