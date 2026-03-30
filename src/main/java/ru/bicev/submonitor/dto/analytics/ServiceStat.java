package ru.bicev.submonitor.dto.analytics;

import java.beans.ConstructorProperties;
import java.math.BigDecimal;

public record ServiceStat(String service, String currency, BigDecimal total) {

    @ConstructorProperties({ "service", "currency", "total" })
    public ServiceStat(String service, String currency, BigDecimal total) {
        this.service = service;
        this.currency = currency;
        this.total = total;
    }

}
