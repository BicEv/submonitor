package ru.bicev.submonitor.dto.payment;

import java.math.BigDecimal;
import java.time.LocalDate;

import ru.bicev.submonitor.entity.enums.PaymentStatus;

public record PaymentLogDto(Long id, Long subsriptionId, String subscriptionName, BigDecimal amount, LocalDate paymentDate, PaymentStatus status) {

}
