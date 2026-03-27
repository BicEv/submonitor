package ru.bicev.submonitor.service;

import java.time.LocalDate;
import java.util.List;

import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import ru.bicev.submonitor.entity.Subscription;
import ru.bicev.submonitor.entity.enums.BillingPeriod;
import ru.bicev.submonitor.repository.SubscriptionRepository;

@Service
@RequiredArgsConstructor
@Slf4j
public class PaymentAutomationService {

    private final SubscriptionRepository subscriptionRepository;
    private final PaymentLogService paymentLogService;

    @EventListener(ApplicationReadyEvent.class)
    @Scheduled(cron = "0 0 9 * * *")
    @Transactional
    public void processPendingPayments() {
        log.info("Starting automated processing...");
        LocalDate current = LocalDate.now();
        List<Subscription> toProcess = subscriptionRepository
                .findAllByIsActiveTrueAndIsDeletedFalseAndNextPaymentLessThanEqual(current);

        for (Subscription s : toProcess) {

            while (s.getNextPayment().compareTo(current) <= 0) {
                paymentLogService.createInternalPaymentLog(s);
                updateNextPaymentDate(s);
            }

        }
        log.info("Automated processing is over!");
    }

    private void updateNextPaymentDate(Subscription subscription) {
        LocalDate currentNext = subscription.getNextPayment();
        BillingPeriod period = subscription.getBillingPeriod();
        if (period == BillingPeriod.WEEKLY) {
            subscription.setNextPayment(currentNext.plusWeeks(1));
        } else if (period == BillingPeriod.MONTHLY) {
            subscription.setNextPayment(currentNext.plusMonths(1));
        } else {
            subscription.setNextPayment(currentNext.plusYears(1));
        }
    }

}
