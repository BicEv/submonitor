package ru.bicev.submonitor.controller;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import ru.bicev.submonitor.dto.AnalyticsPeriod;
import ru.bicev.submonitor.dto.analytics.CategoryStatResponse;
import ru.bicev.submonitor.dto.analytics.ServiceStatResponse;
import ru.bicev.submonitor.dto.analytics.TotalResponse;
import ru.bicev.submonitor.service.AnalyticsService;

/**
 * Контроллер для аналитики расходов на подписки
 */
@RestController
@RequestMapping("/api/v1/analytics")
@RequiredArgsConstructor
@Slf4j
public class AnalyticsRestController {

    private final AnalyticsService analyticsService;

    /**
     * Метод возвращающий расходы сгруппированные по категориям подписок за
     * указанный период
     * 
     * @param period период за который проводится аналитика, по умолчанию - месяц
     * @return ответ, содержащий список категорий и расходов на них
     */
    @GetMapping("/categories")
    public ResponseEntity<List<CategoryStatResponse>> getStatByCategories(
            @RequestParam(defaultValue = "MONTH") AnalyticsPeriod period) {
        var stats = switch (period) {
            case MONTH -> analyticsService.getCategoryStatsForMonth();
            case YEAR -> analyticsService.getCategoryStatsForYear();
            case ALL -> analyticsService.getCategoryStats();
        };
        return ResponseEntity.status(HttpStatus.OK).body(stats);
    }

    /**
     * Метод возвращающий расходы на сгруппированные по сервисам за указанный период
     * 
     * @param period период за который проводится аналитика, по умолчанию - месяц
     * @return ответ, содержащий список сервисов и расходов на них
     */
    @GetMapping("/services")
    public ResponseEntity<List<ServiceStatResponse>> getStatByServices(
            @RequestParam(defaultValue = "MONTH") AnalyticsPeriod period) {
        var stats = switch (period) {
            case MONTH -> analyticsService.getServiceStatsForMonth();
            case YEAR -> analyticsService.getServiceStatsForYear();
            case ALL -> analyticsService.getServiceStats();
        };
        return ResponseEntity.status(HttpStatus.OK).body(stats);
    }

    /**
     * Метод возвращающий сумму всех затрат на подписки за указанный период
     * 
     * @param period период за который проводится аналитика, по умолчанию - месяц
     * @return сумма всех расходов на подписки за указанный период
     */
    @GetMapping("/total")
    public ResponseEntity<TotalResponse> getTotal(
            @RequestParam(defaultValue = "MONTH") AnalyticsPeriod period) {
        var total = switch (period) {
            case MONTH -> analyticsService.getTotalForMonth();
            case YEAR -> analyticsService.getTotalForYear();
            case ALL -> analyticsService.getTotal();
        };
        return ResponseEntity.status(HttpStatus.OK).body(total);
    }

    /**
     * Метод возвращающий прогноз расходов учитвающий активные подписки
     * 
     * @return сумма всех прогнозируемых расходов
     */
    @GetMapping("/forecast")
    public ResponseEntity<TotalResponse> getForecast() {
        var forecast = analyticsService.getForecast();
        return ResponseEntity.status(HttpStatus.OK).body(forecast);
    }

}
