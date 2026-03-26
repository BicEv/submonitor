package ru.bicev.submonitor.dto.subscription;

import java.math.BigDecimal;
import java.time.LocalDate;

import jakarta.validation.constraints.FutureOrPresent;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import ru.bicev.submonitor.entity.enums.BillingPeriod;

public record SubCreationRequest(
        @NotNull(message = "serviceId cannot be null") Long serviceId,
        @Positive(message = "Price must be bigger than zero") BigDecimal price,
        @NotBlank(message = "Currency cannot be empty") String currency,
        @NotNull(message = "Billing period cannot be null") BillingPeriod billingPeriod,
        @FutureOrPresent LocalDate nextPayment) {

}
