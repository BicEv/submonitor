package ru.bicev.submonitor.dto.subscription;

import java.math.BigDecimal;
import java.time.LocalDate;

import ru.bicev.submonitor.entity.enums.BillingPeriod;

public record SubUpdateRequest(
                BigDecimal price,
                String currency,
                BillingPeriod billingPeriod,
                LocalDate nextPayment,
                Boolean isActive) {

}
