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
     * @param userId идентификатор пользователя
     * @param dateCondition условие для фильтрации по дате
     * @return список CategoryStat.class содержащий имя категории и сумму затрат на нее
     */
    public List<CategoryStat> getExpensesByCategory(Long userId, Condition dateCondition) {
        log.debug("getExpensesByCategory for: {} with: {}",userId, dateCondition.getName());
        return dsl.select(
                SERVICES.SERVICE_CATEGORY.as("category"),
                DSL.sum(PAYMENTS.AMOUNT).as("total"))
                .from(PAYMENTS)
                .join(SUBSCRIPTIONS).on(PAYMENTS.SUBSCRIPTION_ID.eq(SUBSCRIPTIONS.ID))
                .join(SERVICES).on(SUBSCRIPTIONS.SERVICE_ID.eq(SERVICES.ID))
                .where(PAYMENTS.SUBSCRIBER_ID.eq(userId))
                .and(dateCondition)
                .groupBy(SERVICES.SERVICE_CATEGORY)
                .fetchInto(CategoryStat.class);
    }

    /**
     * Метод возвращающий список трат, сгруппированных по сервисам
     * @param userId идентификатор пользователя
     * @param dateCondition условие для фильтрации по дате
     * @return список ServiceStat.class содержащий имя сервиса и сумму затрат на него
     */
    public List<ServiceStat> getExpensesByService(Long userId, Condition dateCondition) {
        log.debug("getExpensesByService for: {} with: {}",userId, dateCondition.getName());
        return dsl.select(
                SERVICES.NAME.as("service"),
                DSL.sum(PAYMENTS.AMOUNT).as("total"))
                .from((PAYMENTS))
                .join(SUBSCRIPTIONS).on((PAYMENTS.SUBSCRIPTION_ID).eq(SUBSCRIPTIONS.ID))
                .join(SERVICES).on(SUBSCRIPTIONS.SERVICE_ID.eq(SERVICES.ID))
                .where(PAYMENTS.SUBSCRIBER_ID.eq(userId))
                .and(dateCondition)
                .groupBy(SERVICES.NAME)
                .fetchInto(ServiceStat.class);
    }

    /**
     * Метод возвращающий сумму затрат за указанный период
     * @param userId идентификатор пользователя
     * @param dateCondition условие для фильтрации по дате
     * @return сумма затрат за указанный период
     */
    public BigDecimal getTotal(Long userId, Condition dateCondition) {
        log.debug("getTotal for: {} with: {}",userId, dateCondition.getName());
        return dsl.select(DSL.coalesce(DSL.sum(PAYMENTS.AMOUNT), ZERO))
                .from(PAYMENTS)
                .where(PAYMENTS.SUBSCRIBER_ID.eq(userId))
                .and(dateCondition)
                .fetchOneInto(BigDecimal.class);
    }

    /**
     * Метод возвращающий прогноз затрат на основе активных и не удаленных подписок
     * @param userId идентификатор пользователя
     * @return сумму пронозируемых затрат
     */
    public BigDecimal getForecast(Long userId) {
        log.debug("getForecast for: {}",userId);
        return dsl.select(DSL.coalesce(DSL.sum(SUBSCRIPTIONS.PRICE), ZERO))
                .from(SUBSCRIPTIONS)
                .where(SUBSCRIPTIONS.SUBSCRIBER_ID.eq(userId))
                .and(SUBSCRIPTIONS.IS_ACTIVE.isTrue())
                .and(SUBSCRIPTIONS.IS_DELETED.isFalse())
                .fetchOneInto(BigDecimal.class);
    }

}
