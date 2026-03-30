package ru.bicev.submonitor.dto.analytics;

import java.beans.ConstructorProperties;
import java.math.BigDecimal;

public record CurrencyAmount(String currency, BigDecimal total) {
    @ConstructorProperties({ "currency", "total" })
    public CurrencyAmount(String currency, BigDecimal total) {
        this.currency = currency;
        this.total = total;
    }

}
