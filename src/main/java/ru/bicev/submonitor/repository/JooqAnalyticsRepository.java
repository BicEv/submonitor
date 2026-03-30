package ru.bicev.submonitor.repository;

import java.math.BigDecimal;
import java.util.List;

import org.jooq.Condition;
import org.jooq.DSLContext;
import org.jooq.impl.DSL;
import org.springframework.stereotype.Repository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import ru.bicev.submonitor.dto.analytics.CategoryStat;
import ru.bicev.submonitor.dto.analytics.CurrencyAmount;
import ru.bicev.submonitor.dto.analytics.ServiceStat;

import static ru.bicev.submonitor.jooq.Tables.*;

/**
 * Репозиторий для аналитики трат на подписочные сервисы
 */
@Repository
@RequiredArgsConstructor
@Slf4j
public class JooqAnalyticsRepository {

    private final DSLContext dsl;
    private static final BigDecimal ZERO = BigDecimal.ZERO;

    /**
     * Метод возвращающий список трат, сгруппированных по категориям
     * 
     * @param userId        идентификатор пользователя
     * @param dateCondition условие для фильтрации по дате
     * @return список CategoryStat.class содержащий имя категории, валюту и сумму
     *         затрат на нее
     */
    public List<CategoryStat> getExpensesByCategory(Long userId, Condition dateCondition) {
        log.debug("getExpensesByCategory for: {} with: {}", userId, dateCondition.getName());
        return dsl.select(
                SERVICES.SERVICE_CATEGORY.as("category"),
                SUBSCRIPTIONS.CURRENCY.as("currency"),
                DSL.sum(PAYMENTS.AMOUNT).as("total"))
                .from(PAYMENTS)
                .join(SUBSCRIPTIONS).on(PAYMENTS.SUBSCRIPTION_ID.eq(SUBSCRIPTIONS.ID))
                .join(SERVICES).on(SUBSCRIPTIONS.SERVICE_ID.eq(SERVICES.ID))
                .where(PAYMENTS.SUBSCRIBER_ID.eq(userId))
                .and(dateCondition)
                .groupBy(SERVICES.SERVICE_CATEGORY, SUBSCRIPTIONS.CURRENCY)
                .fetchInto(CategoryStat.class);
    }

    /**
     * Метод возвращающий список трат, сгруппированных по сервисам
     * 
     * @param userId        идентификатор пользователя
     * @param dateCondition условие для фильтрации по дате
     * @return список ServiceStat.class содержащий имя сервиса, валюту и сумму
     *         затрат на
     *         него
     */
    public List<ServiceStat> getExpensesByService(Long userId, Condition dateCondition) {
        log.debug("getExpensesByService for: {} with: {}", userId, dateCondition.getName());
        return dsl.select(
                SERVICES.NAME.as("service"),
                SUBSCRIPTIONS.CURRENCY.as("currency"),
                DSL.coalesce(DSL.sum(PAYMENTS.AMOUNT), ZERO).as("total"))
                .from(PAYMENTS)
                .join(SUBSCRIPTIONS).on(PAYMENTS.SUBSCRIPTION_ID.eq(SUBSCRIPTIONS.ID))
                .join(SERVICES).on(SUBSCRIPTIONS.SERVICE_ID.eq(SERVICES.ID))
                .where(PAYMENTS.SUBSCRIBER_ID.eq(userId))
                .and(dateCondition)
                .groupBy(SERVICES.NAME, SUBSCRIPTIONS.CURRENCY)
                .fetchInto(ServiceStat.class);
    }

    /**
     * Метод возвращающий список сумм затрат за указанное время, сгруппированный по
     * валютам
     * 
     * @param userId        идентификатор пользователя
     * @param dateCondition условие для фильтрации по дате
     * @return список пар валюта: сумма затрат
     */
    public List<CurrencyAmount> getTotal(Long userId, Condition dateCondition) {
        log.debug("getTotal for: {} with: {}", userId, dateCondition.getName());
        return dsl.select(
                SUBSCRIPTIONS.CURRENCY.as("currency"),
                DSL.coalesce(DSL.sum(PAYMENTS.AMOUNT), ZERO).as("total"))
                .from(PAYMENTS)
                .join(SUBSCRIPTIONS).on(PAYMENTS.SUBSCRIPTION_ID.eq(SUBSCRIPTIONS.ID))
                .where(PAYMENTS.SUBSCRIBER_ID.eq(userId))
                .and(dateCondition)
                .groupBy(SUBSCRIPTIONS.CURRENCY)
                .fetchInto(CurrencyAmount.class);
    }

    /**
     * Метод возвращающий прогноз затрат на основе активных и не удаленных подписок
     * 
     * @param userId идентификатор пользователя
     * @return список пар валюта: сумма прогнозируемых затрат
     */
    public List<CurrencyAmount> getForecast(Long userId) {
        log.debug("getForecast for: {}", userId);
        return dsl.select(
                SUBSCRIPTIONS.CURRENCY.as("currency"),
                DSL.coalesce(DSL.sum(SUBSCRIPTIONS.PRICE), ZERO).as("total"))
                .from(SUBSCRIPTIONS)
                .where(SUBSCRIPTIONS.SUBSCRIBER_ID.eq(userId))
                .and(SUBSCRIPTIONS.IS_ACTIVE.isTrue())
                .and(SUBSCRIPTIONS.IS_DELETED.isFalse())
                .groupBy(SUBSCRIPTIONS.CURRENCY)
                .fetchInto(CurrencyAmount.class);
    }

}
