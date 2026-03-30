package ru.bicev.submonitor.dto.analytics;

import java.math.BigDecimal;

public record CategoryStatResponse(String category, BigDecimal total) {

}
