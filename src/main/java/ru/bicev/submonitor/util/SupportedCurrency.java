package ru.bicev.submonitor.util;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;

/**
 * Аннотация для валидации валюты, при создании подписок
 */
@Target({ ElementType.FIELD })
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy = CurrencyValidator.class)
public @interface SupportedCurrency {
    String message() default "Unsupported currency";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};

}
