package ru.bicev.submonitor.dto.currency;

import java.math.BigDecimal;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public record FrankfurterRate(String quote, BigDecimal rate) {

}
