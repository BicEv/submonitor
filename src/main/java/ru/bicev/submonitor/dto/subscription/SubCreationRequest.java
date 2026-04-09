package ru.bicev.submonitor.dto.subscription;

import java.math.BigDecimal;
import java.time.LocalDate;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.FutureOrPresent;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import ru.bicev.submonitor.entity.enums.BillingPeriod;
import ru.bicev.submonitor.util.SupportedCurrency;

@Schema(description = "Запрос на создание подписки")
public record SubCreationRequest(
        @Schema(description = "Идетификатор сервиса подписки", example = "199") @NotNull(message = "serviceId cannot be null") Long serviceId,
        @Schema(description = "Стоимость подписки", example = "199.99") @Positive(message = "Price must be bigger than zero") BigDecimal price,
        @Schema(description = "Валюта оплаты подписки", example = "EUR") @NotBlank(message = "Currency cannot be empty") @SupportedCurrency String currency,
        @Schema(description = "Платежный период подписки", example = "MONTHLY") @NotNull(message = "Billing period cannot be null") BillingPeriod billingPeriod,
        @Schema(description = "Дата следующего списания платежа", example = "2030-01-05") @FutureOrPresent LocalDate nextPayment) {

}
