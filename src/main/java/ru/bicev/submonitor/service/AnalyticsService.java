package ru.bicev.submonitor.service;

import static ru.bicev.submonitor.jooq.Tables.PAYMENTS;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import org.jooq.impl.DSL;
import org.springframework.stereotype.Service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import ru.bicev.submonitor.dto.analytics.CategoryStat;
import ru.bicev.submonitor.dto.analytics.ServiceStat;
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

    /**
     * Метод возвращающий список затрат по категориям за все время
     * 
     * @return список затрат по категорям за все время
     */
    public List<CategoryStat> getCategoryStats() {
        Long userId = getUserId();
        log.debug("getCategoryStats() for user: {}", userId);
        return jooqAnalyticsRepository.getExpensesByCategory(userId, DSL.noCondition());
    }

    /**
     * Метод возвращающий список затрат по категориям за месяц
     * 
     * @return список затрат по категорям за месяц
     */
    public List<CategoryStat> getCategoryStatsForMonth() {
        Long userId = getUserId();
        log.debug("getCategoryStatsForMonth() for user: {} and month", userId, getStartOfMonth().getMonth().toString());
        return jooqAnalyticsRepository.getExpensesByCategory(userId, PAYMENTS.PAYMENT_DATE.ge(getStartOfMonth()));
    }

    /**
     * Метод возвращающий список затрат по категориям за год
     * 
     * @return список затрат по категорям за год
     */
    public List<CategoryStat> getCategoryStatsForYear() {
        Long userId = getUserId();
        log.debug("getCategoryStatsForYear() for user: {} and year", userId, getStartOfYear().getYear());
        return jooqAnalyticsRepository.getExpensesByCategory(userId, PAYMENTS.PAYMENT_DATE.ge(getStartOfYear()));
    }

    /**
     * Метод возвращающий список затрат по сервисам за все время
     * 
     * @return список затрат по сервисам за все время
     */
    public List<ServiceStat> getServiceStats() {
        Long userId = getUserId();
        log.debug("getServiceStats() for user: {}", userId);
        return jooqAnalyticsRepository.getExpensesByService(userId, DSL.noCondition());
    }

    /**
     * Метод возвращающий список затрат по сервисам за все месяц
     * 
     * @return список затрат по сервисам за все месяц
     */
    public List<ServiceStat> getServiceStatsForMonth() {
        Long userId = getUserId();
        log.debug("getServiceStatsForMonth() for user: {} and month", userId, getStartOfMonth().getMonth().toString());
        return jooqAnalyticsRepository.getExpensesByService(userId, PAYMENTS.PAYMENT_DATE.ge(getStartOfMonth()));
    }

    /**
     * Метод возвращающий список затрат по сервисам за все год
     * 
     * @return список затрат по сервисам за все год
     */
    public List<ServiceStat> getServiceStatsForYear() {
        Long userId = getUserId();
        log.debug("getServiceStatsForYear() for user: {} and year", userId, getStartOfYear().getYear());
        return jooqAnalyticsRepository.getExpensesByService(userId, PAYMENTS.PAYMENT_DATE.ge(getStartOfYear()));
    }

    /**
     * Метод возвращающий сумму всех затрат за месяц
     * 
     * @return сумма всех затрат за месяц
     */
    public BigDecimal getTotalForMonth() {
        Long userId = getUserId();
        log.debug("getTotalForMonth() for user: {} and month: {}", userId, getStartOfMonth().getMonth().toString());
        return jooqAnalyticsRepository.getTotal(userId, PAYMENTS.PAYMENT_DATE.ge(getStartOfMonth()));
    }

    /**
     * Метод возвращающий сумму затрат за год
     * 
     * @return сумма затрат за год
     */
    public BigDecimal getTotalForYear() {
        Long userId = getUserId();
        log.debug("getTotalForYear() for user: {} and year: {}", userId, getStartOfYear().getYear());
        return jooqAnalyticsRepository.getTotal(userId, PAYMENTS.PAYMENT_DATE.ge(getStartOfYear()));
    }

    /**
     * Метод возвращающий сумму всех запланированных активных платежей
     * 
     * @return сумма всех запланированных активных платежей
     */
    public BigDecimal getUpcoming() {
        Long userId = getUserId();
        log.debug("getUpcoming() for user: {}", userId);
        return jooqAnalyticsRepository.getForecast(userId);
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

}
