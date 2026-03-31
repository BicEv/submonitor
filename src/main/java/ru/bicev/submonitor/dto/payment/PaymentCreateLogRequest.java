package ru.bicev.submonitor.dto.payment;

import java.math.BigDecimal;
import java.time.LocalDate;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

public record PaymentCreateLogRequest(
        @NotNull(message = "Subscription id cannot be null") @Positive(message = "Subscription id cannot be zero or negative") Long subscriptionId,
        @Positive(message = "Amount cannot be zero or negative") BigDecimal amount,
        @NotNull(message = "Payment date cannot be null") LocalDate paymentDate) {

}
