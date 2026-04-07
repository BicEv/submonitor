package ru.bicev.submonitor.util;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import lombok.RequiredArgsConstructor;
import ru.bicev.submonitor.service.CurrencyService;

/**
 * Класс валидатор проверяющий валюту при создании подписок
 */
@RequiredArgsConstructor
public class CurrencyValidator implements ConstraintValidator<SupportedCurrency, String> {

    private final CurrencyService currencyService;

    /**
     * метод проверяющий содержится ли указанная валюта в сервисе конвертации валют
     */
    @Override
    public boolean isValid(String value, ConstraintValidatorContext context) {
        if (value == null)
            return false;

        return currencyService.isSupported(value.toUpperCase());
    }

}
