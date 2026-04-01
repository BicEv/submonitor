package ru.bicev.submonitor.unit;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import ru.bicev.submonitor.dto.payment.PaymentCreateLogRequest;
import ru.bicev.submonitor.dto.payment.PaymentUpdateLogRequest;
import ru.bicev.submonitor.entity.PaymentLog;
import ru.bicev.submonitor.entity.Service;
import ru.bicev.submonitor.entity.Subscriber;
import ru.bicev.submonitor.entity.Subscription;
import ru.bicev.submonitor.entity.enums.BillingPeriod;
import ru.bicev.submonitor.entity.enums.PaymentStatus;
import ru.bicev.submonitor.entity.enums.ServiceCategory;
import ru.bicev.submonitor.exception.NotFoundException;
import ru.bicev.submonitor.repository.PaymentLogRepository;
import ru.bicev.submonitor.repository.SubscriptionRepository;
import ru.bicev.submonitor.service.PaymentLogService;
import ru.bicev.submonitor.service.SecurityService;

@ExtendWith(MockitoExtension.class)
public class PaymentLogServiceTest {

    @Mock
    private PaymentLogRepository paymentLogRepository;

    @Mock
    private SecurityService securityService;

    @Mock
    private SubscriptionRepository subscriptionRepository;

    @InjectMocks
    private PaymentLogService service;

    private Subscriber subscriber;
    private Service subService;
    private Subscription subscription;
    private PaymentLog created;
    private PaymentLog updated;
    private List<PaymentLog> payments;
    private PaymentCreateLogRequest createLogRequest;
    private PaymentUpdateLogRequest updateLogRequest;

    @BeforeEach
    void setUp() {
        subscriber = Subscriber.builder()
                .id(1L)
                .username("testSubscriber")
                .password("password")
                .email("test@mail.com")
                .build();
        subService = Service.builder()
                .id(1L)
                .name("TEST_SERVICE")
                .serviceCategory(ServiceCategory.OTHER)
                .owner(null)
                .build();
        subscription = Subscription.builder()
                .id(1L)
                .service(subService)
                .price(BigDecimal.valueOf(10.99))
                .billingPeriod(BillingPeriod.MONTHLY)
                .currency("USD")
                .isActive(true)
                .isDeleted(false)
                .nextPayment(LocalDate.now().plusDays(7))
                .subscriber(subscriber)
                .build();
        created = PaymentLog.builder()
                .amount(BigDecimal.valueOf(14.99))
                .id(1L)
                .paymentDate(LocalDate.now().minusDays(7))
                .status(PaymentStatus.SUCCESS)
                .subscriber(subscriber)
                .subscription(subscription)
                .build();
        updated = PaymentLog.builder()
                .amount(BigDecimal.valueOf(9.99))
                .id(10L)
                .paymentDate(LocalDate.now().minusDays(14))
                .status(PaymentStatus.SUCCESS)
                .subscriber(subscriber)
                .subscription(subscription)
                .build();
        payments = List.of(
                PaymentLog.builder().id(20L).amount(BigDecimal.valueOf(3.99)).paymentDate(LocalDate.now().minusDays(15))
                        .subscriber(subscriber).subscription(subscription).status(PaymentStatus.SUCCESS).build(),
                PaymentLog.builder().id(21L).amount(BigDecimal.valueOf(4.99)).paymentDate(LocalDate.now().minusDays(15))
                        .subscriber(subscriber).subscription(subscription).status(PaymentStatus.SUCCESS).build());
        createLogRequest = new PaymentCreateLogRequest(1L, BigDecimal.valueOf(14.99), LocalDate.now().minusDays(7));
        updateLogRequest = new PaymentUpdateLogRequest(1L, BigDecimal.valueOf(20.99), null, PaymentStatus.FAILED);

    }

    @Test
    @DisplayName("Must create new log")
    void createPaymentLog_Success() {
        when(securityService.getCurrentSubscriber()).thenReturn(subscriber);
        when(subscriptionRepository.findByIdAndSubscriberId(1L, 1L)).thenReturn(Optional.of(subscription));
        when(paymentLogRepository.save(any(PaymentLog.class))).thenReturn(created);

        var result = service.createPaymentLog(createLogRequest);

        assertAll(
                () -> assertEquals(created.getId(), result.id()),
                () -> assertEquals(created.getAmount(), result.amount()),
                () -> assertEquals(created.getPaymentDate(), result.paymentDate()),
                () -> assertEquals(created.getStatus(), result.status()));

        verify(securityService, times(1)).getCurrentSubscriber();
        verify(subscriptionRepository, times(1)).findByIdAndSubscriberId(1L, 1L);
        verify(paymentLogRepository, times(1)).save(any(PaymentLog.class));
    }

    @Test
    @DisplayName("Must throw NotFoundException while creating new log")
    void createLog_ThrowsNotFoundException() {
        when(securityService.getCurrentSubscriber()).thenReturn(subscriber);
        when(subscriptionRepository.findByIdAndSubscriberId(1L, 1L)).thenReturn(Optional.empty());

        assertThrows(NotFoundException.class, () -> service.createPaymentLog(createLogRequest));

        verify(securityService, times(1)).getCurrentSubscriber();
        verify(subscriptionRepository, times(1)).findByIdAndSubscriberId(1L, 1L);
        verify(paymentLogRepository, never()).save(any(PaymentLog.class));
    }

    @Test
    @DisplayName("Must create internal log")
    void createInternalPaymentLog_Success() {
        service.createInternalPaymentLog(subscription);

        verify(paymentLogRepository, times(1)).save(any(PaymentLog.class));
    }

    @Test
    @DisplayName("Must retrieve log by id")
    void getPaymentLogById_Success() {
        when(securityService.getCurrentUserId()).thenReturn(1L);
        when(paymentLogRepository.findByIdAndSubscriberId(1L, 1L)).thenReturn(Optional.of(created));

        var result = service.getPaymentLogById(1L);

        assertAll(
                () -> assertEquals(created.getId(), result.id()),
                () -> assertEquals(created.getAmount(), result.amount()),
                () -> assertEquals(created.getPaymentDate(), result.paymentDate()),
                () -> assertEquals(created.getStatus(), result.status()));

        verify(securityService, times(1)).getCurrentUserId();
        verify(paymentLogRepository, times(1)).findByIdAndSubscriberId(1L, 1L);
    }

    @Test
    @DisplayName("Must throw NotFoundException when log is not found by id")
    void getPaymentLogById_ThrowsNotFoundException() {
        when(securityService.getCurrentUserId()).thenReturn(1L);
        when(paymentLogRepository.findByIdAndSubscriberId(1L, 1L)).thenReturn(Optional.empty());

        assertThrows(NotFoundException.class, () -> service.getPaymentLogById(1L));

        verify(securityService, times(1)).getCurrentUserId();
        verify(paymentLogRepository, times(1)).findByIdAndSubscriberId(1L, 1L);
    }

    @Test
    @DisplayName("Must retrieve logs list")
    void getPaymentsForUser_Success() {
        Pageable pageable = PageRequest.of(0, 10);
        Page<PaymentLog> page = new PageImpl<>(payments, pageable, payments.size());

        when(securityService.getCurrentUserId()).thenReturn(1L);
        when(paymentLogRepository.findAllBySubscriberId(1L, pageable)).thenReturn(page);

        var result = service.getPaymentsForUser(pageable);

        assertAll(
                () -> assertEquals(2, result.content().size()),
                () -> assertEquals(2, result.totalElements()),
                () -> assertEquals(1, result.totalPages()),
                () -> assertAll(
                        () -> assertEquals(payments.get(0).getAmount(), result.content().get(0).amount()),
                        () -> assertEquals(payments.get(0).getPaymentDate(), result.content().get(0).paymentDate()),
                        () -> assertEquals(payments.get(1).getAmount(), result.content().get(1).amount()),
                        () -> assertEquals(payments.get(1).getPaymentDate(), result.content().get(1).paymentDate())));

        verify(securityService, times(1)).getCurrentUserId();
        verify(paymentLogRepository, times(1)).findAllBySubscriberId(1L, pageable);
    }

    @Test
    @DisplayName("Must update log")
    void updatePaymentLog_Success() {
        when(securityService.getCurrentUserId()).thenReturn(1L);
        when(paymentLogRepository.findByIdAndSubscriberId(1L, 1L)).thenReturn(Optional.of(updated));
        when(subscriptionRepository.findByIdAndSubscriberId(1L, 1L)).thenReturn(Optional.of(subscription));

        var result = service.updatePaymentLog(1L, updateLogRequest);

        assertAll(
                () -> assertEquals(updateLogRequest.amount(), result.amount()),
                () -> assertEquals(updateLogRequest.status(), result.status()),
                () -> assertEquals(subscription.getService().getName(), result.subscriptionName()));

        verify(securityService, times(1)).getCurrentUserId();
        verify(paymentLogRepository, times(1)).findByIdAndSubscriberId(1L, 1L);
        verify(subscriptionRepository, times(1)).findByIdAndSubscriberId(1L, 1L);
    }

    @Test
    @DisplayName("Must throw NotFoundException when log is not found")
    void updatePaymentLog_ThrowsNotFoundException() {
        when(securityService.getCurrentUserId()).thenReturn(1L);
        when(paymentLogRepository.findByIdAndSubscriberId(1000L, 1L)).thenReturn(Optional.empty());

        assertThrows(NotFoundException.class, () -> service.updatePaymentLog(1000L, updateLogRequest));

        verify(securityService, times(1)).getCurrentUserId();
        verify(paymentLogRepository, times(1)).findByIdAndSubscriberId(1000L, 1L);
        verify(subscriptionRepository, never()).findByIdAndSubscriberId(1L, 1L);
    }

    @Test
    @DisplayName("Must throw NotFoundException when subscription is not found")
    void updatePaymentLog_ThrowsNotFoundException_WhenSubscriptionNotFound() {
        when(securityService.getCurrentUserId()).thenReturn(1L);
        when(paymentLogRepository.findByIdAndSubscriberId(1000L, 1L)).thenReturn(Optional.of(updated));
        when(subscriptionRepository.findByIdAndSubscriberId(1L, 1L)).thenReturn(Optional.empty());

        assertThrows(NotFoundException.class, () -> service.updatePaymentLog(1000L, updateLogRequest));

        verify(securityService, times(1)).getCurrentUserId();
        verify(paymentLogRepository, times(1)).findByIdAndSubscriberId(1000L, 1L);
        verify(subscriptionRepository, times(1)).findByIdAndSubscriberId(1L, 1L);
    }

    @Test
    @DisplayName("Must delete payment log")
    void deletePaymentLog_Success() {
        when(securityService.getCurrentUserId()).thenReturn(1L);
        when(paymentLogRepository.findByIdAndSubscriberId(1L, 1L)).thenReturn(Optional.of(created));

        service.deletePaymentLog(1L);
        verify(securityService, times(1)).getCurrentUserId();
        verify(paymentLogRepository, times(1)).findByIdAndSubscriberId(1L, 1L);
        verify(paymentLogRepository, times(1)).delete(created);
    }

    @Test
    @DisplayName("Must throw NotFoundException when log is not found")
    void deletePaymentLog_ThrowsNotFoundException() {
        when(securityService.getCurrentUserId()).thenReturn(1L);
        when(paymentLogRepository.findByIdAndSubscriberId(1L, 1L)).thenReturn(Optional.empty());

        assertThrows(NotFoundException.class, () -> service.deletePaymentLog(1L));

        verify(securityService, times(1)).getCurrentUserId();
        verify(paymentLogRepository, times(1)).findByIdAndSubscriberId(1L, 1L);
        verify(paymentLogRepository, never()).delete(created);
    }

}
