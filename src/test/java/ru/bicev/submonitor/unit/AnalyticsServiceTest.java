package ru.bicev.submonitor.unit;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.util.List;

import org.jooq.impl.DSL;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import ru.bicev.submonitor.dto.analytics.CategoryStat;
import ru.bicev.submonitor.dto.analytics.CurrencyAmount;
import ru.bicev.submonitor.dto.analytics.ServiceStat;
import ru.bicev.submonitor.repository.JooqAnalyticsRepository;
import ru.bicev.submonitor.service.AnalyticsService;
import ru.bicev.submonitor.service.CurrencyService;
import ru.bicev.submonitor.service.SecurityService;

@ExtendWith(MockitoExtension.class)
public class AnalyticsServiceTest {

        @Mock
        private SecurityService securityService;

        @Mock
        private JooqAnalyticsRepository jooqAnalyticsRepository;

        @Mock
        private CurrencyService currencyService;

        @InjectMocks
        private AnalyticsService service;

        private List<CategoryStat> categoryStats;
        private List<ServiceStat> serviceStats;
        private List<CurrencyAmount> total;
        private List<CurrencyAmount> forecast;

        @BeforeEach
        void setUp() {
                categoryStats = List.of(
                                new CategoryStat("OTHER", "USD", BigDecimal.valueOf(10)),
                                new CategoryStat("OTHER", "EUR", BigDecimal.valueOf(11)),
                                new CategoryStat("EDUCATION", "RUB", BigDecimal.valueOf(5000)),
                                new CategoryStat("EDUCATION", "USD", BigDecimal.valueOf(10)));
                serviceStats = List.of(
                                new ServiceStat("SUB1", "USD", BigDecimal.valueOf(10)),
                                new ServiceStat("SUB2", "EUR", BigDecimal.valueOf(11)),
                                new ServiceStat("SUB3", "RUB", BigDecimal.valueOf(500)),
                                new ServiceStat("SUB4", "USD", BigDecimal.valueOf(10)));
                total = List.of(
                                new CurrencyAmount("USD", BigDecimal.valueOf(100)),
                                new CurrencyAmount("EUR", BigDecimal.valueOf(50)));

                forecast = List.of(
                                new CurrencyAmount("USD", BigDecimal.valueOf(15)),
                                new CurrencyAmount("EUR", BigDecimal.valueOf(25)));

        }

        @Test
        @DisplayName("Must return all category stats")
        void getCategoryStats_Success() {
                when(securityService.getCurrentUserId()).thenReturn(1L);
                when(jooqAnalyticsRepository.getExpensesByCategory(1L, DSL.noCondition())).thenReturn(categoryStats);
                when(currencyService.convertToBase(BigDecimal.valueOf(10), "USD")).thenReturn(BigDecimal.valueOf(900));
                when(currencyService.convertToBase(BigDecimal.valueOf(11), "EUR")).thenReturn(BigDecimal.valueOf(1100));
                when(currencyService.convertToBase(BigDecimal.valueOf(5000), "RUB"))
                                .thenReturn(BigDecimal.valueOf(5000));

                var result = service.getCategoryStats();

                assertAll(
                                () -> assertEquals(2, result.size()),
                                () -> assertEquals(BigDecimal.valueOf(2000), result.get(0).total()),
                                () -> assertEquals("OTHER", result.get(0).category()),
                                () -> assertEquals(BigDecimal.valueOf(5900), result.get(1).total()),
                                () -> assertEquals("EDUCATION", result.get(1).category()));

                verify(jooqAnalyticsRepository, times(1)).getExpensesByCategory(1L, DSL.noCondition());
        }

        @Test
        @DisplayName("Must return all category stats for month")
        void getCategoryStatsForMonth_Success() {
                when(securityService.getCurrentUserId()).thenReturn(1L);
                when(jooqAnalyticsRepository.getExpensesByCategory(eq(1L), any()))
                                .thenReturn(categoryStats);
                when(currencyService.convertToBase(BigDecimal.valueOf(10), "USD")).thenReturn(BigDecimal.valueOf(900));
                when(currencyService.convertToBase(BigDecimal.valueOf(11), "EUR")).thenReturn(BigDecimal.valueOf(1100));
                when(currencyService.convertToBase(BigDecimal.valueOf(5000), "RUB"))
                                .thenReturn(BigDecimal.valueOf(5000));

                var result = service.getCategoryStatsForMonth();

                assertAll(
                                () -> assertEquals(2, result.size()),
                                () -> assertEquals(BigDecimal.valueOf(2000), result.get(0).total()),
                                () -> assertEquals("OTHER", result.get(0).category()),
                                () -> assertEquals(BigDecimal.valueOf(5900), result.get(1).total()),
                                () -> assertEquals("EDUCATION", result.get(1).category()));

                verify(jooqAnalyticsRepository, times(1)).getExpensesByCategory(eq(1L), any());
        }

        @Test
        @DisplayName("Must return all category stats for year")
        void getCategoryStatsForYear_Success() {
                when(securityService.getCurrentUserId()).thenReturn(1L);
                when(jooqAnalyticsRepository.getExpensesByCategory(eq(1L), any()))
                                .thenReturn(categoryStats);
                when(currencyService.convertToBase(BigDecimal.valueOf(10), "USD")).thenReturn(BigDecimal.valueOf(900));
                when(currencyService.convertToBase(BigDecimal.valueOf(11), "EUR")).thenReturn(BigDecimal.valueOf(1100));
                when(currencyService.convertToBase(BigDecimal.valueOf(5000), "RUB"))
                                .thenReturn(BigDecimal.valueOf(5000));

                var result = service.getCategoryStatsForYear();

                assertAll(
                                () -> assertEquals(2, result.size()),
                                () -> assertEquals(BigDecimal.valueOf(2000), result.get(0).total()),
                                () -> assertEquals("OTHER", result.get(0).category()),
                                () -> assertEquals(BigDecimal.valueOf(5900), result.get(1).total()),
                                () -> assertEquals("EDUCATION", result.get(1).category()));

                verify(jooqAnalyticsRepository, times(1)).getExpensesByCategory(eq(1L), any());
        }

        @Test
        @DisplayName("Must return service stats")
        void getServiceStats_Success() {
                when(securityService.getCurrentUserId()).thenReturn(1L);
                when(jooqAnalyticsRepository.getExpensesByService(1L, DSL.noCondition())).thenReturn(serviceStats);
                when(currencyService.convertToBase(BigDecimal.valueOf(10), "USD")).thenReturn(BigDecimal.valueOf(900));
                when(currencyService.convertToBase(BigDecimal.valueOf(11), "EUR")).thenReturn(BigDecimal.valueOf(1100));
                when(currencyService.convertToBase(BigDecimal.valueOf(500), "RUB")).thenReturn(BigDecimal.valueOf(500));

                var result = service.getServiceStats();
                assertAll(
                                () -> assertEquals(4, result.size()),
                                () -> assertEquals(BigDecimal.valueOf(900), result.get(0).total()),
                                () -> assertEquals("SUB1", result.get(0).service()),
                                () -> assertEquals(BigDecimal.valueOf(1100), result.get(1).total()),
                                () -> assertEquals("SUB2", result.get(1).service()),
                                () -> assertEquals(BigDecimal.valueOf(500), result.get(2).total()),
                                () -> assertEquals("SUB3", result.get(2).service()),
                                () -> assertEquals(BigDecimal.valueOf(900), result.get(3).total()),
                                () -> assertEquals("SUB4", result.get(3).service()));

                verify(jooqAnalyticsRepository, times(1)).getExpensesByService(1L, DSL.noCondition());

        }

        @Test
        @DisplayName("Must return service stats for month")
        void getServiceStatsForMonth_Success() {
                when(securityService.getCurrentUserId()).thenReturn(1L);
                when(jooqAnalyticsRepository.getExpensesByService(eq(1L), any())).thenReturn(serviceStats);
                when(currencyService.convertToBase(BigDecimal.valueOf(10), "USD")).thenReturn(BigDecimal.valueOf(900));
                when(currencyService.convertToBase(BigDecimal.valueOf(11), "EUR")).thenReturn(BigDecimal.valueOf(1100));
                when(currencyService.convertToBase(BigDecimal.valueOf(500), "RUB")).thenReturn(BigDecimal.valueOf(500));

                var result = service.getServiceStatsForMonth();
                assertAll(
                                () -> assertEquals(4, result.size()),
                                () -> assertEquals(BigDecimal.valueOf(900), result.get(0).total()),
                                () -> assertEquals("SUB1", result.get(0).service()),
                                () -> assertEquals(BigDecimal.valueOf(1100), result.get(1).total()),
                                () -> assertEquals("SUB2", result.get(1).service()),
                                () -> assertEquals(BigDecimal.valueOf(500), result.get(2).total()),
                                () -> assertEquals("SUB3", result.get(2).service()),
                                () -> assertEquals(BigDecimal.valueOf(900), result.get(3).total()),
                                () -> assertEquals("SUB4", result.get(3).service()));

                verify(jooqAnalyticsRepository, times(1)).getExpensesByService(eq(1L), any());

        }

        @Test
        @DisplayName("Must return service stats for year")
        void getServiceStatsForYear_Success() {
                when(securityService.getCurrentUserId()).thenReturn(1L);
                when(jooqAnalyticsRepository.getExpensesByService(eq(1L), any())).thenReturn(serviceStats);
                when(currencyService.convertToBase(BigDecimal.valueOf(10), "USD")).thenReturn(BigDecimal.valueOf(900));
                when(currencyService.convertToBase(BigDecimal.valueOf(11), "EUR")).thenReturn(BigDecimal.valueOf(1100));
                when(currencyService.convertToBase(BigDecimal.valueOf(500), "RUB")).thenReturn(BigDecimal.valueOf(500));

                var result = service.getServiceStatsForYear();
                assertAll(
                                () -> assertEquals(4, result.size()),
                                () -> assertEquals(BigDecimal.valueOf(900), result.get(0).total()),
                                () -> assertEquals("SUB1", result.get(0).service()),
                                () -> assertEquals(BigDecimal.valueOf(1100), result.get(1).total()),
                                () -> assertEquals("SUB2", result.get(1).service()),
                                () -> assertEquals(BigDecimal.valueOf(500), result.get(2).total()),
                                () -> assertEquals("SUB3", result.get(2).service()),
                                () -> assertEquals(BigDecimal.valueOf(900), result.get(3).total()),
                                () -> assertEquals("SUB4", result.get(3).service()));

                verify(jooqAnalyticsRepository, times(1)).getExpensesByService(eq(1L), any());

        }

        @Test
        @DisplayName("Must return total stats")
        void getTotal_Success() {
                when(securityService.getCurrentUserId()).thenReturn(1L);
                when(jooqAnalyticsRepository.getTotal(1L, DSL.noCondition())).thenReturn(total);
                when(currencyService.convertToBase(BigDecimal.valueOf(100), "USD"))
                                .thenReturn(BigDecimal.valueOf(9000));
                when(currencyService.convertToBase(BigDecimal.valueOf(50), "EUR"))
                                .thenReturn(BigDecimal.valueOf(5500));

                var result = service.getTotal();

                assertEquals(BigDecimal.valueOf(14500), result.total());

                verify(jooqAnalyticsRepository, times(1)).getTotal(1L, DSL.noCondition());
        }

        @Test
        @DisplayName("Must return total stats for month")
        void getTotalForMonth_Success() {
                when(securityService.getCurrentUserId()).thenReturn(1L);
                when(jooqAnalyticsRepository.getTotal(eq(1L), any())).thenReturn(total);
                when(currencyService.convertToBase(BigDecimal.valueOf(100), "USD"))
                                .thenReturn(BigDecimal.valueOf(9000));
                when(currencyService.convertToBase(BigDecimal.valueOf(50), "EUR"))
                                .thenReturn(BigDecimal.valueOf(5500));

                var result = service.getTotalForMonth();

                assertEquals(BigDecimal.valueOf(14500), result.total());

                verify(jooqAnalyticsRepository, times(1)).getTotal(eq(1L), any());
        }

        @Test
        @DisplayName("Must return total stats for year")
        void getTotalForYear_Success() {
                when(securityService.getCurrentUserId()).thenReturn(1L);
                when(jooqAnalyticsRepository.getTotal(eq(1L), any())).thenReturn(total);
                when(currencyService.convertToBase(BigDecimal.valueOf(100), "USD"))
                                .thenReturn(BigDecimal.valueOf(9000));
                when(currencyService.convertToBase(BigDecimal.valueOf(50), "EUR"))
                                .thenReturn(BigDecimal.valueOf(5500));

                var result = service.getTotalForYear();

                assertEquals(BigDecimal.valueOf(14500), result.total());

                verify(jooqAnalyticsRepository, times(1)).getTotal(eq(1L), any());
        }

        @Test
        @DisplayName("Must return forecast")
        void getForecast_Success() {
                when(securityService.getCurrentUserId()).thenReturn(1L);
                when(jooqAnalyticsRepository.getForecast(1L)).thenReturn(forecast);
                when(currencyService.convertToBase(BigDecimal.valueOf(15), "USD")).thenReturn(BigDecimal.valueOf(1350));
                when(currencyService.convertToBase(BigDecimal.valueOf(25), "EUR")).thenReturn(BigDecimal.valueOf(2750));

                var result = service.getForecast();

                assertEquals(BigDecimal.valueOf(4100), result.total());

                verify(jooqAnalyticsRepository, times(1)).getForecast(1L);
        }

}
