package ru.bicev.submonitor.service;

import java.math.BigDecimal;
import java.time.LocalDate;

import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import ru.bicev.submonitor.dto.PagedResponse;
import ru.bicev.submonitor.dto.payment.PaymentCreateLogRequest;
import ru.bicev.submonitor.dto.payment.PaymentLogDto;
import ru.bicev.submonitor.dto.payment.PaymentUpdateLogRequest;
import ru.bicev.submonitor.entity.PaymentLog;
import ru.bicev.submonitor.entity.Subscriber;
import ru.bicev.submonitor.entity.Subscription;
import ru.bicev.submonitor.entity.enums.PaymentStatus;
import ru.bicev.submonitor.exception.NotFoundException;
import ru.bicev.submonitor.repository.PaymentLogRepository;
import ru.bicev.submonitor.repository.SubscriptionRepository;

/**
 * Сервис для ручного управления платежными логами
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class PaymentLogService {

    private final PaymentLogRepository paymentLogRepository;
    private final SecurityService securityService;
    private final SubscriptionRepository subscriptionRepository;

    /**
     * Метод создающий новый платежный лог в соответствии с запросом
     * 
     * @param request запрос, содержащий данные лога и идентификатор подписки
     * @return дто, содержащее данные созданного лога
     * @throws NotFoundException.class если подписка с идентификатором из запроса не
     *                                 существует или не принадлежит текущему
     *                                 пользователю
     */
    @Transactional
    public PaymentLogDto createPaymentLog(PaymentCreateLogRequest request) {
        var subscriber = getSub();
        var subscription = subscriptionRepository.findByIdAndSubscriberId(request.subscriptionId(), subscriber.getId())
                .orElseThrow(() -> {
                    log.warn("Subscription with id: {} was not found or it doesn't belongs to user: {}",
                            request.subscriptionId(), subscriber.getId());
                    throw new NotFoundException("Subscription not found or doesn't belong to current user");
                });
        PaymentLog paymentLog = PaymentLog.builder()
                .subscriber(subscriber)
                .subscription(subscription)
                .amount(request.amount())
                .paymentDate(request.paymentDate())
                .status(PaymentStatus.SUCCESS)
                .build();
        var savedPaymentLog = paymentLogRepository.save(paymentLog);
        log.debug("Payment log created: {}", savedPaymentLog.getId());
        return toDto(savedPaymentLog);

    }

    /**
     * Метод для создания платежного лога для переданной подписки и ее владельца
     * 
     * @param subscription подписка для которой создается платежный лог
     */
    @Transactional
    public void createInternalPaymentLog(Subscription subscription) {
        var subscriber = subscription.getSubscriber();
        PaymentLog paymentLog = PaymentLog.builder()
                .subscriber(subscriber)
                .subscription(subscription)
                .paymentDate(LocalDate.now())
                .amount(subscription.getPrice())
                .status(PaymentStatus.SUCCESS)
                .build();
        paymentLogRepository.save(paymentLog);
        log.info("Created paymentLog: {} for subscriber: {} and subscription: {}", paymentLog.getId(),
                subscriber.getId(), subscription.getId());
    }

    /**
     * Метод возвращающий лог по указанному идентификатору
     * 
     * @param paymentLogId идентификатор искомого лога
     * @return дто, содержащее данные найденного лога
     * @throws NotFoundException.class если лога с таким идентификатором не
     *                                 существует или он не принадлежит текущему
     *                                 пользователю
     */
    @Transactional(readOnly = true)
    public PaymentLogDto getPaymentLogById(Long paymentLogId) {
        Long subId = getSubId();
        var payment = paymentLogRepository.findByIdAndSubscriberId(paymentLogId, subId).orElseThrow(() -> {
            log.warn("PaymentLog with id: {} was not found or it doesn't belongs to user: {}",
                    paymentLogId, subId);
            throw new NotFoundException("PaymentLog not found or doesn't belong to current user");
        });
        log.debug("PaymentLog: {} was found", payment.getId());
        return toDto(payment);
    }

    /**
     * Метод получающий страницу платежных логов для текущего пользователя
     * 
     * @return страница платежных логов текущего пользователя
     */
    @Transactional(readOnly = true)
    public PagedResponse<PaymentLogDto> getPaymentsForUser(Pageable pageable) {
        Long subId = getSubId();
        log.debug("Payments retrieved for user: {}", subId);

        var paymentDtos = paymentLogRepository.findAllBySubscriberId(subId, pageable).map(this::toDto);
        return PagedResponse.toPagedResponse(paymentDtos);
    }

    /**
     * Метод обновляющий платежный лог
     * 
     * @param paymentLogId идентификатор платежного лога
     * @param request      запрос, содержащий параметры для обновления
     * @return дто, содержащий данные обновленного лога
     * @throws NotFoundException.class если лога с таким идентификатором не найдено
     *                                 или подписки с указанным в запросе
     *                                 идентификатором не найдено или они не
     *                                 принадлежат текущему пользователю
     */
    @Transactional
    public PaymentLogDto updatePaymentLog(Long paymentLogId, PaymentUpdateLogRequest request) {
        Long subId = getSubId();
        var payment = paymentLogRepository.findByIdAndSubscriberId(paymentLogId, subId).orElseThrow(() -> {
            log.warn("PaymentLog with id: {} was not found or it doesn't belong to user: {}",
                    paymentLogId, subId);
            throw new NotFoundException("PaymentLog not found or doesn't belong to current user");
        });

        if (request.subscriptionId() != null) {
            var subscription = subscriptionRepository.findByIdAndSubscriberId(request.subscriptionId(), subId)
                    .orElseThrow(() -> {
                        log.warn("Subscription with id: {} was not found or it doesn't belong to user: {}",
                                request.subscriptionId(), subId);
                        throw new NotFoundException("Subscription not found or doesn't belong to current user");
                    });
            payment.setSubscription(subscription);
        }
        if (request.amount() != null && request.amount().compareTo(BigDecimal.ZERO) <= 0) {
            payment.setAmount(request.amount());
        }
        if (request.paymentDate() != null) {
            payment.setPaymentDate(request.paymentDate());
        }
        if (request.status() != null) {
            payment.setStatus(request.status());
        }
        return toDto(payment);

    }

    /**
     * Метод удаляющий платежный лог по его идентификатору
     * 
     * @param paymentLogId идентификатор лога подлежащего удалению
     * @throws NotFoundException.class если лог с таким идентифкатором не существует
     *                                 или не принадлежит текущему пользователю
     */
    @Transactional
    public void deletePaymentLog(Long paymentLogId) {
        Long subId = getSubId();
        var payment = paymentLogRepository.findByIdAndSubscriberId(paymentLogId, subId).orElseThrow(() -> {
            log.warn("PaymentLog with id: {} was not found or it doesn't belongs to user: {}",
                    paymentLogId, subId);
            throw new NotFoundException("PaymentLog not found or doesn't belong to current user");
        });
        paymentLogRepository.delete(payment);
    }

    /**
     * Служебный метод возвращающий идетификатор текущего пользователя
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
     * Служебный метод для преобразования сущность платежного лога в дто платежного
     * лога
     * 
     * @param paymentLog сущность для преобразования
     * @return дто с данными исходной сущности
     */
    private PaymentLogDto toDto(PaymentLog paymentLog) {
        return new PaymentLogDto(paymentLog.getId(), paymentLog.getSubscription().getId(),
                paymentLog.getSubscription().getService().getName(), paymentLog.getAmount(),
                paymentLog.getPaymentDate(), paymentLog.getStatus());
    }

}
