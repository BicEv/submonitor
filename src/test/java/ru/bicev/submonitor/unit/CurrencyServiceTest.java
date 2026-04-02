package ru.bicev.submonitor.unit;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.client.RestTemplate;

import ru.bicev.submonitor.dto.currency.FrankfurterRate;
import ru.bicev.submonitor.exception.NotFoundException;
import ru.bicev.submonitor.service.CurrencyService;

@ExtendWith(MockitoExtension.class)
public class CurrencyServiceTest {

    @Mock
    private RestTemplate restTemplate;

    @InjectMocks
    private CurrencyService service;

    @BeforeEach
    void serUp() {
        ReflectionTestUtils.setField(service, "baseCurrency", "RUB");
        ReflectionTestUtils.setField(service, "supportedCurrencies", List.of("USD", "EUR"));

        Map<String, BigDecimal> testRates = new ConcurrentHashMap<>();
        testRates.put("RUB", new BigDecimal(1.00));
        testRates.put("USD", new BigDecimal(2.00));
        testRates.put("EUR", new BigDecimal(3.00));

        ReflectionTestUtils.setField(service, "rates", testRates);
    }

    @Test
    @DisplayName("Must update rates from API")
    void updateRates_Success(){
        List<FrankfurterRate> mockRates = List.of(
                new FrankfurterRate("USD", BigDecimal.valueOf(90)),
                new FrankfurterRate("EUR", BigDecimal.valueOf(110)));

        ResponseEntity<List<FrankfurterRate>> response = ResponseEntity.ok(mockRates);

        when(restTemplate.exchange(
                anyString(),
                eq(HttpMethod.GET),
                isNull(),
                any(ParameterizedTypeReference.class)))
                .thenReturn(response);

        service.updateRates();

        BigDecimal result = service.convertToBase(BigDecimal.valueOf(900), "USD");

        assertEquals(0, BigDecimal.valueOf(10.00).compareTo(result));
    }

    @Test
    @DisplayName("Must return base amount if currency equal to base")
    void convertToBase_SuccessFromBase() {
        BigDecimal amount = new BigDecimal(100);

        var result = service.convertToBase(amount, "RUB");

        assertEquals(amount, result);
    }

    @Test
    @DisplayName("Must return converted amount if currency is supported")
    void convertToBase_Success() {
        BigDecimal amount = new BigDecimal(100);

        var result = service.convertToBase(amount, "USD");

        assertEquals(amount.divide(BigDecimal.valueOf(2), 2, RoundingMode.HALF_UP), result);
    }

    @Test
    @DisplayName("Must throw NotFoundException if currency is not supported")
    void convertToBase_ThrowsNotFoundException() {

        assertThrows(NotFoundException.class, () -> service.convertToBase(new BigDecimal(999), "ZIZ"));
    }

}
