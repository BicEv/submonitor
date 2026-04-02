package ru.bicev.submonitor.unit;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import ru.bicev.submonitor.entity.Subscription;
import ru.bicev.submonitor.entity.enums.BillingPeriod;
import ru.bicev.submonitor.repository.SubscriptionRepository;
import ru.bicev.submonitor.service.PaymentAutomationService;
import ru.bicev.submonitor.service.PaymentLogService;

@ExtendWith(MockitoExtension.class)
public class PaymentAutomationServiceTest {

    @Mock
    private SubscriptionRepository subscriptionRepository;

    @Mock
    private PaymentLogService paymentLogService;

    @InjectMocks
    private PaymentAutomationService service;

    private Subscription subMonthly;

    @BeforeEach
    void setUp() {
        subMonthly = Subscription.builder()
                .id(1L)
                .billingPeriod(BillingPeriod.MONTHLY)
                .nextPayment(LocalDate.now())
                .isActive(true)
                .isDeleted(false)
                .currency("USD")
                .price(new BigDecimal(10.00))
                .build();
    }

    @Test
    @DisplayName("Should process payment for today and update next date")
    void processPendingPayments_Today() {
        when(subscriptionRepository.findAllByIsActiveTrueAndIsDeletedFalseAndNextPaymentLessThanEqual(LocalDate.now()))
                .thenReturn(List.of(subMonthly));

        service.processPendingPayments();

        verify(paymentLogService, times(1)).createInternalPaymentLog(subMonthly);

        assertEquals(LocalDate.now().plusMonths(1), subMonthly.getNextPayment());
    }

    @Test
    @DisplayName("Should create multiple logs if subscription is far behind")
    void processPendingPayments_CatchUp() {
        subMonthly.setNextPayment(LocalDate.now().minusMonths(2));

        when(subscriptionRepository.findAllByIsActiveTrueAndIsDeletedFalseAndNextPaymentLessThanEqual(LocalDate.now()))
                .thenReturn(List.of(subMonthly));

        service.processPendingPayments();

        verify(paymentLogService, times(3)).createInternalPaymentLog(subMonthly);

        assertEquals(LocalDate.now().plusMonths(1), subMonthly.getNextPayment());

    }

}
