package ru.bicev.submonitor.dto.subscription;

import java.math.BigDecimal;
import java.time.LocalDate;

import jakarta.validation.constraints.NotNull;
import ru.bicev.submonitor.entity.enums.BillingPeriod;

public record SubUpdateRequest(
        @NotNull(message = "Id of updated subscription cannot be null") Long subscriptionId,
        BigDecimal price,
        String currency,
        BillingPeriod billingPeriod,
        LocalDate nextPayment,
        Boolean isActive) {

}
