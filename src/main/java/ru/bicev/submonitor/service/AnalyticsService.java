package ru.bicev.submonitor.service;

import static ru.bicev.submonitor.jooq.Tables.PAYMENTS;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

import org.jooq.impl.DSL;
import org.springframework.stereotype.Service;

import lombok.RequiredArgsConstructor;
import lombok.var;
import lombok.extern.slf4j.Slf4j;
import ru.bicev.submonitor.dto.analytics.CategoryStat;
import ru.bicev.submonitor.dto.analytics.CategoryStatResponse;
import ru.bicev.submonitor.dto.analytics.CurrencyAmount;
import ru.bicev.submonitor.dto.analytics.ServiceStat;
import ru.bicev.submonitor.dto.analytics.ServiceStatResponse;
import ru.bicev.submonitor.dto.analytics.TotalResponse;
import ru.bicev.submonitor.repository.JooqAnalyticsRepository;

/**
 * Сервис для получения аналитических данных
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class AnalyticsService {

    private final SecurityService securityService;
    private final JooqAnalyticsRepository jooqAnalyticsRepository;
    private final CurrencyService currencyService;

    /**
     * Метод возвращающий список затрат по категориям за все время
     * 
     * @return список затрат по категорям за все время
     */
    public List<CategoryStatResponse> getCategoryStats() {
        Long userId = getUserId();
        log.debug("getCategoryStats() for user: {}", userId);
        var rawStats = jooqAnalyticsRepository.getExpensesByCategory(userId, DSL.noCondition());
        return mapToAggregated(rawStats);

    }

    /**
     * Метод возвращающий список затрат по категориям за месяц
     * 
     * @return список затрат по категорям за месяц
     */
    public List<CategoryStatResponse> getCategoryStatsForMonth() {
        Long userId = getUserId();
        log.debug("getCategoryStatsForMonth() for user: {} and month: {}", userId,
                getStartOfMonth().getMonth().toString());
        var rawStats = jooqAnalyticsRepository.getExpensesByCategory(userId,
                PAYMENTS.PAYMENT_DATE.ge(getStartOfMonth()));
        return mapToAggregated(rawStats);
    }

    /**
     * Метод возвращающий список затрат по категориям за год
     * 
     * @return список затрат по категорям за год
     */
    public List<CategoryStatResponse> getCategoryStatsForYear() {
        Long userId = getUserId();
        log.debug("getCategoryStatsForYear() for user: {} and year: {}", userId, getStartOfYear().getYear());
        var rawStats = jooqAnalyticsRepository.getExpensesByCategory(userId,
                PAYMENTS.PAYMENT_DATE.ge(getStartOfYear()));
        return mapToAggregated(rawStats);
    }

    /**
     * Метод возвращающий список затрат по сервисам за все время
     * 
     * @return список затрат по сервисам за все время
     */
    public List<ServiceStatResponse> getServiceStats() {
        Long userId = getUserId();
        log.debug("getServiceStats() for user: {}", userId);
        var rawStats = jooqAnalyticsRepository.getExpensesByService(userId, DSL.noCondition());
        return mapToConverted(rawStats);
    }

    /**
     * Метод возвращающий список затрат по сервисам за все месяц
     * 
     * @return список затрат по сервисам за все месяц
     */
    public List<ServiceStatResponse> getServiceStatsForMonth() {
        Long userId = getUserId();
        log.debug("getServiceStatsForMonth() for user: {} and month: {}", userId,
                getStartOfMonth().getMonth().toString());
        var rawStats = jooqAnalyticsRepository.getExpensesByService(userId,
                PAYMENTS.PAYMENT_DATE.ge(getStartOfMonth()));
        return mapToConverted(rawStats);
    }

    /**
     * Метод возвращающий список затрат по сервисам за все год
     * 
     * @return список затрат по сервисам за все год
     */
    public List<ServiceStatResponse> getServiceStatsForYear() {
        Long userId = getUserId();
        log.debug("getServiceStatsForYear() for user: {} and year: {}", userId, getStartOfYear().getYear());
        var rawStats = jooqAnalyticsRepository.getExpensesByService(userId, PAYMENTS.PAYMENT_DATE.ge(getStartOfYear()));
        return mapToConverted(rawStats);
    }

    /**
     * Метод возвращающий сумму всех затрат за все время
     * 
     * @return сумму всех затрат за все время
     */
    public TotalResponse getTotal() {
        Long userId = getUserId();
        log.debug("getTotal() for user: {}", userId);
        var amounts = jooqAnalyticsRepository.getTotal(userId, DSL.noCondition());
        return new TotalResponse(calculateTotalInBase(amounts));
    }

    /**
     * Метод возвращающий сумму всех затрат за месяц
     * 
     * @return сумма всех затрат за месяц
     */
    public TotalResponse getTotalForMonth() {
        Long userId = getUserId();
        log.debug("getTotalForMonth() for user: {} and month: {}", userId, getStartOfMonth().getMonth().toString());
        var amounts = jooqAnalyticsRepository.getTotal(userId, PAYMENTS.PAYMENT_DATE.ge(getStartOfMonth()));
        return new TotalResponse(calculateTotalInBase(amounts));
    }

    /**
     * Метод возвращающий сумму затрат за год
     * 
     * @return сумма затрат за год
     */
    public TotalResponse getTotalForYear() {
        Long userId = getUserId();
        log.debug("getTotalForYear() for user: {} and year: {}", userId, getStartOfYear().getYear());
        var amounts = jooqAnalyticsRepository.getTotal(userId, PAYMENTS.PAYMENT_DATE.ge(getStartOfYear()));
        return new TotalResponse(calculateTotalInBase(amounts));
    }

    /**
     * Метод возвращающий сумму всех запланированных активных платежей
     * 
     * @return сумма всех запланированных активных платежей
     */
    public TotalResponse getForecast() {
        Long userId = getUserId();
        log.debug("getForecast() for user: {}", userId);
        var amounts = jooqAnalyticsRepository.getForecast(userId);
        return new TotalResponse(calculateTotalInBase(amounts));

    }

    /**
     * Служебный метод возвращающий идетификатор текущего пользователя
     * 
     * @return идетнификатор текущего пользователя
     */
    private Long getUserId() {
        return securityService.getCurrentUserId();
    }

    /**
     * Служебный метод возвращающий дату начала текущего месяца
     * 
     * @return дата начала текущего месяца
     */
    private LocalDate getStartOfMonth() {
        return LocalDate.now().withDayOfMonth(1);
    }

    /**
     * Служебный метод возвращающий дату начала текущего года
     * 
     * @return дата начала текущего года
     */
    private LocalDate getStartOfYear() {
        return LocalDate.now().withMonth(1).withDayOfMonth(1);
    }

    /**
     * Служебный метод для конвертации валют полученных из базы данных записей в
     * базовую валюту приложения
     * 
     * @param rawStats список статистических данных по категориям
     * @return список затрат по категориям, конвертированных к базовой валюте
     *         приложения
     */
    private List<CategoryStatResponse> mapToAggregated(List<CategoryStat> rawStats) {
        var mapped = rawStats.stream()
                .collect(Collectors.groupingBy(
                        CategoryStat::category,
                        (Collectors.mapping(
                                stat -> currencyService.convertToBase(stat.total(), stat.currency()),
                                Collectors.reducing(BigDecimal.ZERO, BigDecimal::add)))));

        return mapped.entrySet().stream()
                .map(entry -> new CategoryStatResponse(entry.getKey(), entry.getValue()))
                .toList();
    }

    /**
     * Служебный метод для конвертации валют полученных из базы записей в базовую
     * валюту приложения
     * 
     * @param rawStats список статистических данных по сервисам
     * @return список затрат по сервисам, конвертированных к базовой валюте
     */
    private List<ServiceStatResponse> mapToConverted(List<ServiceStat> rawStats) {
        return rawStats.stream()
                .map(stat -> new ServiceStatResponse(stat.service(),
                        currencyService.convertToBase(stat.total(), stat.currency())))
                .toList();
    }

    /**
     * Служебный метод для конвертации валют полученных из базы записей в базовую
     * валюту приложения
     * 
     * @param amounts список статистических данных по затратам
     * @return сумма затрат, конвертированная к базовой валюте
     */
    private BigDecimal calculateTotalInBase(List<CurrencyAmount> amounts) {
        return amounts.stream()
                .map(ca -> currencyService.convertToBase(ca.total(), ca.currency()))
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

}
