package ru.bicev.submonitor.dto.subscription;

import java.math.BigDecimal;

public record SubscriptionDto(Long id, BigDecimal price, String currency, String billingPeriod, String nextPayment,
        boolean isActive) {

}
