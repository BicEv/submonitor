package ru.bicev.submonitor.service;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import ru.bicev.submonitor.dto.PagedResponse;
import ru.bicev.submonitor.dto.payment.PaymentCreateLogRequest;
import ru.bicev.submonitor.dto.payment.PaymentLogDto;
import ru.bicev.submonitor.entity.PaymentLog;
import ru.bicev.submonitor.entity.Subscriber;
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
        var payment = paymentLogRepository.findByIdAndSubscriberID(paymentLogId, subId).orElseThrow(() -> {
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
     * Метод удаляющий платежный лог по его идентификатору
     * 
     * @param paymentLogId идентификатор лога подлежащего удалению
     * @throws NotFoundException.class если лог с таким идентифкатором не существует
     *                                 или не принадлежит текущему пользователю
     */
    @Transactional
    public void deletePaymentLog(Long paymentLogId) {
        Long subId = getSubId();
        var payment = paymentLogRepository.findByIdAndSubscriberID(paymentLogId, subId).orElseThrow(() -> {
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
