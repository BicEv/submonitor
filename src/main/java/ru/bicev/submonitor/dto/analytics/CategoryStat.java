package ru.bicev.submonitor.dto.analytics;

import java.beans.ConstructorProperties;
import java.math.BigDecimal;

public record CategoryStat(String category, String currency, BigDecimal total) {

    @ConstructorProperties({ "category", "currency", "total" })
    public CategoryStat(String category, String currency, BigDecimal total) {
        this.category = category;
        this.currency = currency;
        this.total = total;
    }

}
