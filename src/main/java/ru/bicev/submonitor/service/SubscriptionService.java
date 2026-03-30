package ru.bicev.submonitor.service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import ru.bicev.submonitor.dto.subscription.SubCreationRequest;
import ru.bicev.submonitor.dto.subscription.SubUpdateRequest;
import ru.bicev.submonitor.dto.subscription.SubscriptionDto;
import ru.bicev.submonitor.entity.Subscriber;
import ru.bicev.submonitor.entity.Subscription;
import ru.bicev.submonitor.exception.NotFoundException;
import ru.bicev.submonitor.repository.ServiceRepository;
import ru.bicev.submonitor.repository.SubscriptionRepository;

/**
 * Сервис упараляющий подписками
 */
@Service
@Slf4j
@RequiredArgsConstructor
public class SubscriptionService {

    private final SecurityService securityService;
    private final ServiceRepository serviceRepository;
    private final SubscriptionRepository subscriptionRepository;

    /**
     * Метод создающий новую подписку на указанный в запросе сервис
     * 
     * @param request запрос, содержащий параметры подписки, которую нужно создать
     * @return дто, содержащее данные созданной подписки
     * @throws NotFoundException.class если сервис указанный в запросе не найден или
     *                                 принадлежит другому пользователю
     */
    @Transactional
    public SubscriptionDto createSubscription(SubCreationRequest request) {
        var sub = getSub();
        var service = serviceRepository.findByIdAndOwnerIdEqualsOrNull(request.serviceId(), sub.getId())
                .orElseThrow(() -> {
                    log.warn("Service: {} not found or doesn't belong to a current subscriber", request.serviceId());
                    throw new NotFoundException("Service not found with provided id: " + request.serviceId());
                });

        Subscription subscription = Subscription.builder()
                .service(service)
                .subscriber(sub)
                .price(request.price())
                .billingPeriod(request.billingPeriod())
                .currency(request.currency())
                .isActive(true)
                .nextPayment(request.nextPayment())
                .build();

        var savedSubscription = subscriptionRepository.save(subscription);
        log.debug("Subscription created: {}", savedSubscription.getId());
        return toDto(savedSubscription);

    }

    /**
     * Метод возвращающий подписку по ее идентификатору
     * 
     * @param subscriptionId идентификатор искомой подписки
     * @return дто, содержащее данные подписки
     * @throws NotFoundException.class если подписки с таким идентификатором не
     *                                 найдено или она не принадлежит текущему
     *                                 пользователю
     */
    @Transactional(readOnly = true)
    public SubscriptionDto getSubscriptionById(Long subscriptionId) {
        var subscription = subscriptionRepository.findByIdAndSubscriberId(subscriptionId, getSubId()).orElseThrow(
                () -> {
                    log.warn("Subscription: {} was not found", subscriptionId);
                    throw new NotFoundException("Subscription was not found or doesn't belong to a subscriber");
                });
        log.debug("Subscription: {} found", subscriptionId);
        return toDto(subscription);
    }

    /**
     * Метод возвращающий все подписки текущего пользователя не помеченные как
     * удаленные
     * 
     * @return список дто, содержащий данные не удаленных подписок
     */
    @Transactional(readOnly = true)
    public List<SubscriptionDto> getAllForCurrentSub() {
        Long subId = getSubId();
        var subscriptions = subscriptionRepository.findAllBySubscriberIdAndIsDeletedFalse(subId);
        log.debug("Subscriptions for subscriber: {} were found", subId);
        return subscriptions.stream().map(this::toDto).collect(Collectors.toList());
    }

    /**
     * Метод обновляющий данные подписки в соотвествии с запросом
     * 
     * @param request запрос содержащий идентификатор подписки для изменения и
     *                данные для обновления
     * @return дто, содержащее данные обновленной подписки
     * @throws NotFoundException.class если подписка с идентификатором из запроса не
     *                                 найдена или не принадлежит текущему
     *                                 пользователю
     */
    @Transactional
    public SubscriptionDto updateSubscription(Long subscriptionId, SubUpdateRequest request) {
        Long subId = getSubId();
        var subscription = subscriptionRepository.findByIdAndSubscriberId(subscriptionId, subId)
                .orElseThrow(() -> {
                    log.warn("Subscription: {} was not found or subscriber: {} is not the owner",
                            subscriptionId, subId);
                    throw new NotFoundException("Subscription was not found or doesn't belong to a subscriber");
                });
        if (request.price() != null && request.price().compareTo(BigDecimal.ZERO) > 0) {
            subscription.setPrice(request.price());
        }
        if (request.currency() != null) {
            subscription.setCurrency(request.currency());
        }
        if (request.billingPeriod() != null) {
            subscription.setBillingPeriod(request.billingPeriod());
        }
        if (request.nextPayment() != null && request.nextPayment().isAfter(LocalDate.now())) {
            subscription.setNextPayment(request.nextPayment());
        }
        if (request.isActive() != null) {
            subscription.setActive(request.isActive());
        }
        log.debug("Subscription: {} was updated", subscription.getId());
        return toDto(subscription);
    }

    /**
     * Метод устанавливающий флаг подписки как удаленная
     * 
     * @param subscriptionId идентификатор подписки, которую нужно пометить
     *                       удаленной
     * @throws NotFoundException.class если подписка с идентификатором из запроса не
     *                                 найдена или не принадлежит текущему
     *                                 пользователю
     */
    @Transactional
    public void deleteSubscription(Long subscriptionId) {
        Long subId = getSubId();
        var subscription = subscriptionRepository.findByIdAndSubscriberId(subscriptionId, subId).orElseThrow(() -> {
            log.warn("Subscription: {} was not found or subscriber: {} is not the owner",
                    subscriptionId, subId);
            throw new NotFoundException("Subscription was not found or doesn't belong to a subscriber");
        });
        subscription.setDeleted(true);
        subscription.setActive(false);
        log.debug("Subscription: {} was set deleted", subscription.getId());
    }

    /**
     * Служебный метод возвращающий идентификатор текущего пользователя
     * 
     * @return идентификатор текущего пользователя
     */
    private Long getSubId() {
        return securityService.getCurrentUserId();
    }

    /**
     * Служебный метод возвращающий текущего пользователя
     * 
     * @return текущий пользователь
     */
    private Subscriber getSub() {
        return securityService.getCurrentSubscriber();
    }

    /**
     * Служебный метод для маппинга сущности подписки в дто подписки
     * 
     * @param subscription сущность подлежащая преобразованию
     * @return дто, содержащий данные подписки
     */
    private SubscriptionDto toDto(Subscription subscription) {
        return new SubscriptionDto(subscription.getId(), subscription.getPrice(), subscription.getCurrency(),
                subscription.getBillingPeriod().name(), subscription.getNextPayment().toString(),
                subscription.isActive());
    }

}
