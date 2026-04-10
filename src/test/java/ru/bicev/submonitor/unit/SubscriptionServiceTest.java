package ru.bicev.submonitor.unit;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
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

import ru.bicev.submonitor.dto.subscription.SubCreationRequest;
import ru.bicev.submonitor.dto.subscription.SubUpdateRequest;
import ru.bicev.submonitor.entity.Service;
import ru.bicev.submonitor.entity.Subscriber;
import ru.bicev.submonitor.entity.Subscription;
import ru.bicev.submonitor.entity.enums.BillingPeriod;
import ru.bicev.submonitor.entity.enums.ServiceCategory;
import ru.bicev.submonitor.exception.NotFoundException;
import ru.bicev.submonitor.repository.ServiceRepository;
import ru.bicev.submonitor.repository.SubscriptionRepository;
import ru.bicev.submonitor.service.CurrencyService;
import ru.bicev.submonitor.service.SecurityService;
import ru.bicev.submonitor.service.SubscriptionService;

@ExtendWith(MockitoExtension.class)
public class SubscriptionServiceTest {

        @Mock
        private SecurityService securityService;

        @Mock
        private SubscriptionRepository subscriptionRepository;

        @Mock
        private ServiceRepository serviceRepository;

        @Mock
        private CurrencyService currencyService;

        @InjectMocks
        private SubscriptionService service;

        private Subscriber subscriber;
        private Service subService;
        private Subscription created;
        private Subscription updated;
        private List<Subscription> subscriptions;
        private SubCreationRequest creationRequest;
        private SubUpdateRequest updateRequest;

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
                created = Subscription.builder()
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
                updated = Subscription.builder()
                                .id(10L)
                                .service(subService)
                                .price(BigDecimal.valueOf(9.99))
                                .billingPeriod(BillingPeriod.MONTHLY)
                                .currency("EUR")
                                .isActive(true)
                                .isDeleted(false)
                                .nextPayment(LocalDate.now().plusDays(7))
                                .subscriber(subscriber)
                                .build();
                subscriptions = List.of(
                                Subscription.builder().id(10L).billingPeriod(BillingPeriod.MONTHLY)
                                                .price(BigDecimal.valueOf(10.99))
                                                .currency("USD").nextPayment(LocalDate.now().plusDays(10)).build(),
                                Subscription.builder().id(11L).billingPeriod(BillingPeriod.YEARLY)
                                                .price(BigDecimal.valueOf(11.99))
                                                .currency("RUB").nextPayment(LocalDate.now().plusDays(11)).build(),
                                Subscription.builder().id(12L).billingPeriod(BillingPeriod.WEEKLY)
                                                .price(BigDecimal.valueOf(12.99))
                                                .currency("EUR").nextPayment(LocalDate.now().plusDays(12)).build());
                creationRequest = new SubCreationRequest(1L, BigDecimal.valueOf(10.99), "USD", BillingPeriod.MONTHLY,
                                LocalDate.now().plusDays(7));
                updateRequest = new SubUpdateRequest(BigDecimal.valueOf(300), "RUB", null, null, false);

        }

        @Test
        @DisplayName("Must create new subscription")
        void createSubscription_Success() {
                when(securityService.getCurrentSubscriber()).thenReturn(subscriber);
                when(serviceRepository.findByIdAndOwnerIdEqualsOrNull(1L, 1L)).thenReturn(Optional.of(subService));
                when(subscriptionRepository.save(any(Subscription.class))).thenReturn(created);

                var result = service.createSubscription(creationRequest);

                assertNotNull(result);
                assertAll(
                                () -> assertEquals(created.getPrice(), result.price()),
                                () -> assertEquals(created.getCurrency(), result.currency()),
                                () -> assertEquals(created.getNextPayment().toString(), result.nextPayment()),
                                () -> assertEquals(created.getBillingPeriod().name(), result.billingPeriod()),
                                () -> assertEquals(created.getId(), result.id()));

                verify(securityService, times(1)).getCurrentSubscriber();
                verify(serviceRepository, times(1)).findByIdAndOwnerIdEqualsOrNull(1L, 1L);
                verify(subscriptionRepository, times(1)).save(any(Subscription.class));
        }

        @Test
        @DisplayName("Must throw exception when create new subscription")
        void createSubscription_ThrowsNotFoundException() {
                when(securityService.getCurrentSubscriber()).thenReturn(subscriber);
                when(serviceRepository.findByIdAndOwnerIdEqualsOrNull(1L, 1L)).thenReturn(Optional.empty());

                assertThrows(NotFoundException.class, () -> service.createSubscription(creationRequest));

                verify(securityService, times(1)).getCurrentSubscriber();
                verify(serviceRepository, times(1)).findByIdAndOwnerIdEqualsOrNull(1L, 1L);
        }

        @Test
        @DisplayName("Must retrieve subscription by id")
        void getSubscriptionById_Success() {
                when(securityService.getCurrentUserId()).thenReturn(1L);
                when(subscriptionRepository.findByIdAndSubscriberId(1L, 1L)).thenReturn(Optional.of(created));

                var result = service.getSubscriptionById(1L);

                assertNotNull(result);
                assertAll(
                                () -> assertEquals(created.getPrice(), result.price()),
                                () -> assertEquals(created.getCurrency(), result.currency()),
                                () -> assertEquals(created.getNextPayment().toString(), result.nextPayment()),
                                () -> assertEquals(created.getBillingPeriod().name(), result.billingPeriod()),
                                () -> assertEquals(created.getId(), result.id()));

                verify(securityService, times(1)).getCurrentUserId();
                verify(subscriptionRepository, times(1)).findByIdAndSubscriberId(1L, 1L);

        }

        @Test
        @DisplayName("Must throw exception when subscription not found")
        void getSubscriptionById_ThrowsNotFoundException() {
                when(securityService.getCurrentUserId()).thenReturn(1L);
                when(subscriptionRepository.findByIdAndSubscriberId(1L, 1L)).thenReturn(Optional.empty());

                assertThrows(NotFoundException.class, () -> service.getSubscriptionById(1L));

                verify(securityService, times(1)).getCurrentUserId();
                verify(subscriptionRepository, times(1)).findByIdAndSubscriberId(1L, 1L);
        }

        @Test
        @DisplayName("Must return all subscriptions")
        void getAllForCurrentSub_Success() {
                when(securityService.getCurrentUserId()).thenReturn(1L);
                when(subscriptionRepository.findAllBySubscriberIdAndIsDeletedFalse(1L)).thenReturn(subscriptions);

                var result = service.getAllForCurrentSub();

                assertAll(
                                () -> assertEquals(subscriptions.size(), result.size()),
                                () -> assertEquals(subscriptions.get(0).getPrice(), result.get(0).price()),
                                () -> assertEquals(subscriptions.get(0).getBillingPeriod().name(),
                                                result.get(0).billingPeriod()),
                                () -> assertEquals(subscriptions.get(0).getCurrency(), result.get(0).currency()),
                                () -> assertEquals(subscriptions.get(1).getPrice(), result.get(1).price()),
                                () -> assertEquals(subscriptions.get(1).getBillingPeriod().name(),
                                                result.get(1).billingPeriod()),
                                () -> assertEquals(subscriptions.get(1).getCurrency(), result.get(1).currency()),
                                () -> assertEquals(subscriptions.get(2).getPrice(), result.get(2).price()),
                                () -> assertEquals(subscriptions.get(2).getBillingPeriod().name(),
                                                result.get(2).billingPeriod()),
                                () -> assertEquals(subscriptions.get(2).getCurrency(), result.get(2).currency()));

                verify(securityService, times(1)).getCurrentUserId();
                verify(subscriptionRepository, times(1)).findAllBySubscriberIdAndIsDeletedFalse(1L);
        }

        @Test
        @DisplayName("Must update subscription")
        void updateSubscription_Success() {
                when(securityService.getCurrentUserId()).thenReturn(1L);
                when(subscriptionRepository.findByIdAndSubscriberId(10L, 1L)).thenReturn(Optional.of(updated));
                when(currencyService.isSupported(updateRequest.currency())).thenReturn(true);

                var result = service.updateSubscription(10L, updateRequest);

                assertAll(
                                () -> assertEquals(updateRequest.isActive(), result.isActive()),
                                () -> assertEquals(updateRequest.currency(), result.currency()),
                                () -> assertEquals(updated.getPrice(), result.price()));

                verify(securityService, times(1)).getCurrentUserId();
                verify(subscriptionRepository, times(1)).findByIdAndSubscriberId(10L, 1L);

        }

        @Test
        @DisplayName("Must throw exception when subscription not found")
        void updateSubscription_ThrowsNotFoundException() {
                when(securityService.getCurrentUserId()).thenReturn(1L);
                when(subscriptionRepository.findByIdAndSubscriberId(10L, 1L)).thenReturn(Optional.empty());

                assertThrows(NotFoundException.class, () -> service.updateSubscription(10L, updateRequest));

                verify(securityService, times(1)).getCurrentUserId();
                verify(subscriptionRepository, times(1)).findByIdAndSubscriberId(10L, 1L);

        }

        @Test
        @DisplayName("Must create new subscription")
        void deleteSubscription_Success() {
                when(securityService.getCurrentUserId()).thenReturn(1L);
                when(subscriptionRepository.findByIdAndSubscriberId(1L, 1L)).thenReturn(Optional.of(created));

                service.deleteSubscription(1L);

                assertEquals(true, created.isDeleted());
                assertEquals(false, created.isActive());

                verify(securityService, times(1)).getCurrentUserId();
        }

        @Test
        @DisplayName("Must throw exception when subscription not found")
        void deleteSubscription_ThrowsNotFoundException() {
                when(securityService.getCurrentUserId()).thenReturn(1L);
                when(subscriptionRepository.findByIdAndSubscriberId(1L, 1L)).thenReturn(Optional.empty());

                assertThrows(NotFoundException.class, () -> service.deleteSubscription(1L));

                verify(securityService, times(1)).getCurrentUserId();

        }

}
