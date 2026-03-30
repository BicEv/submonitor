package ru.bicev.submonitor.service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.ResponseEntity;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import ru.bicev.submonitor.dto.currency.FrankfurterRate;
import ru.bicev.submonitor.exception.NotFoundException;

/**
 * Сервис для автоматического получения текущего курса валют при запуске
 * приложения или ежедневно в 9:00
 */
@Service
@Slf4j
@RequiredArgsConstructor
public class CurrencyService {

    @Value("${submonitor.currency.base}")
    private String baseCurrency;

    @Value("${submonitor.currency.supported}")
    private List<String> supportedCurrencies;

    private final RestTemplate restTemplate;
    private final Map<String, BigDecimal> rates = new ConcurrentHashMap<>();
    private final String API_URL = "https://api.frankfurter.dev/v2/rates";

    /**
     * Метод для получения текущего курса валют с Frankfurter v2 API
     */
    @EventListener(ApplicationReadyEvent.class)
    @Scheduled(cron = "0 0 9 * * *")
    public void updateRates() {
        String url = buildApiUrl();
        try {
            log.info("Trying to update rates...");

            ResponseEntity<List<FrankfurterRate>> response = restTemplate.exchange(
                    url,
                    org.springframework.http.HttpMethod.GET,
                    null,
                    new ParameterizedTypeReference<List<FrankfurterRate>>() {
                    });
            List<FrankfurterRate> ratesList = response.getBody();

            if (ratesList != null) {
                ratesList.forEach(r -> rates.put(r.quote(), r.rate()));
                rates.put(baseCurrency, BigDecimal.ONE);
                log.info("Rates updated from Frankfurter v2. Rates size: {}", rates.size());
            }
        } catch (Exception e) {
            log.error("Failed to update exchange rates: {}", e.getMessage());
        }
    }

    /**
     * Метод осуществляющий конвертацию к базовой валюте
     * 
     * @param amount       сумма для конвертации
     * @param fromCurrency значение исходной валюты для конвертации
     * @return конвертированная сумма
     * @throws NotFoundException.class,     если такой валюты нет во хэш-таблице
     * @throws IllegalStateException.class, если коэффициент для целевой валюты
     *                                      отсутствует или меньше либо равен нулю
     */
    public BigDecimal convertToBase(BigDecimal amount, String fromCurrency) {
        String fromCurUpper = fromCurrency.toUpperCase();
        if (!rates.containsKey(fromCurUpper)) {
            log.error("Rate not found for: {}", fromCurUpper);
            throw new NotFoundException("Currency is not found in rates: " + fromCurUpper);
        }
        if (baseCurrency.equalsIgnoreCase(fromCurUpper))
            return amount;

        BigDecimal rate = rates.get(fromCurUpper);
        if (rate == null || rate.compareTo(BigDecimal.ZERO) <= 0) {
            log.error("Rate is null, or zero or less for currency: {}", fromCurUpper);
            throw new IllegalStateException("Rate is zero or less or null");
        }
        return amount.divide(rate, 2, RoundingMode.HALF_UP);
    }

    /**
     * Служебный метод для построения URL запроса на Frankfurter v2 API
     * 
     * @return URL строка запроса
     */
    private String buildApiUrl() {
        String currencies = String.join(",", supportedCurrencies);

        return UriComponentsBuilder.fromUriString(API_URL)
                .queryParam("base", baseCurrency.toUpperCase())
                .queryParam("quotes", currencies)
                .build()
                .toUriString();
    }

}
