package ru.bicev.submonitor.dto.analytics;

import java.math.BigDecimal;

public record ServiceStatResponse(String service, BigDecimal total) {

}
