package ru.bicev.submonitor.controller;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import ru.bicev.submonitor.dto.subscription.SubCreationRequest;
import ru.bicev.submonitor.dto.subscription.SubUpdateRequest;
import ru.bicev.submonitor.dto.subscription.SubscriptionDto;
import ru.bicev.submonitor.service.SubscriptionService;

/**
 * Контроллер для управления подписками пользователя
 */
@RestController
@RequestMapping("/api/v1/subscriptions")
@Slf4j
@RequiredArgsConstructor
public class SubsriptionRestController {

    private final SubscriptionService subscriptionService;

    /**
     * Метод создающий новую подписку в соответствии с запросом
     * 
     * @param request запрос, содержащий данные для создания подписки
     * @return ответ содержащий дто с данными созданной подписки
     */
    @PostMapping
    public ResponseEntity<SubscriptionDto> createSubscription(@Valid @RequestBody SubCreationRequest request) {
        var created = subscriptionService.createSubscription(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    /**
     * Метод получает подписку по ее идентификатору
     * 
     * @param subscriptionId идентификатор искомой подписки
     * @return ответ, содержащий дто найденной подписки
     */
    @GetMapping("/{subscriptionId}")
    public ResponseEntity<SubscriptionDto> getSubscriptionById(@PathVariable Long subscriptionId) {
        var subscription = subscriptionService.getSubscriptionById(subscriptionId);
        return ResponseEntity.status(HttpStatus.OK).body(subscription);
    }

    /**
     * Метод получает все подписки текущего пользователя
     * 
     * @return ответ содержащий список дто с данными всех подписок пользователя
     */
    @GetMapping
    public ResponseEntity<List<SubscriptionDto>> getAllSubscriptions() {
        var subscriptions = subscriptionService.getAllForCurrentSub();
        return ResponseEntity.status(HttpStatus.OK).body(subscriptions);
    }

    /**
     * Метод обновляющий подписку по ее идентификатору
     * 
     * @param subscriptionId идентификатор подписки для обновления
     * @param request        запрос содержащий данные для изменения
     * @return ответ с дто, сожержащим данные измененной подписки
     */
    @PatchMapping("/{subscriptionId}")
    public ResponseEntity<SubscriptionDto> updateSubscription(@PathVariable Long subscriptionId,
            @RequestBody SubUpdateRequest request) {
        var updated = subscriptionService.updateSubscription(subscriptionId, request);
        return ResponseEntity.status(HttpStatus.OK).body(updated);

    }

    /**
     * Метод устанавливающий подписку как удаленную
     * 
     * @param subscriptionId идентификатор подписки для удаления
     * @return ответ со статусом 204 No Content
     */
    @DeleteMapping("/{subscriptionId}")
    public ResponseEntity<Void> deleteSubscription(@PathVariable Long subscriptionId) {
        subscriptionService.deleteSubscription(subscriptionId);
        return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
    }

}
