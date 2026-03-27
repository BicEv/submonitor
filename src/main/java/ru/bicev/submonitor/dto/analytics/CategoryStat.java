package ru.bicev.submonitor.dto.analytics;

import java.beans.ConstructorProperties;
import java.math.BigDecimal;

public record CategoryStat(String category, BigDecimal total) {

    @ConstructorProperties({ "category", "total" })
    public CategoryStat(String category, BigDecimal total) {
        this.category = category;
        this.total = total;
    }

}
