package ru.bicev.submonitor.dto.analytics;

import java.beans.ConstructorProperties;
import java.math.BigDecimal;

public record ServiceStat(String service, BigDecimal total) {

    @ConstructorProperties({ "service", "total" })
    public ServiceStat(String service, BigDecimal total) {
        this.service = service;
        this.total = total;
    }

}
