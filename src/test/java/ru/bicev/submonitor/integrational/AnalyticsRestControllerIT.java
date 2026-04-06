package ru.bicev.submonitor.integrational;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import jakarta.persistence.EntityManager;
import ru.bicev.submonitor.dto.auth.LoginRequest;
import ru.bicev.submonitor.entity.PaymentLog;
import ru.bicev.submonitor.entity.Service;
import ru.bicev.submonitor.entity.Subscriber;
import ru.bicev.submonitor.entity.Subscription;
import ru.bicev.submonitor.entity.enums.BillingPeriod;
import ru.bicev.submonitor.entity.enums.PaymentStatus;
import ru.bicev.submonitor.entity.enums.ServiceCategory;
import ru.bicev.submonitor.repository.PaymentLogRepository;
import ru.bicev.submonitor.repository.ServiceRepository;
import ru.bicev.submonitor.repository.SubscriberRepository;
import ru.bicev.submonitor.repository.SubscriptionRepository;
import tools.jackson.databind.ObjectMapper;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@Transactional
@AutoConfigureMockMvc
public class AnalyticsRestControllerIT {

    @Autowired
    private PaymentLogRepository paymentLogRepository;

    @Autowired
    private ServiceRepository serviceRepository;

    @Autowired
    private SubscriptionRepository subscriptionRepository;

    @Autowired
    private SubscriberRepository subscriberRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private EntityManager em;

    @Autowired
    private MockMvc mockMvc;

    private Subscriber subscriber;
    private Service service1;
    private Service service2;
    private Service service3;
    private Subscription subscription1;
    private Subscription subscription2;
    private Subscription subscription3;
    List<PaymentLog> payments = new ArrayList<>();

    @BeforeEach
    void setUp() {
        subscriber = subscriberRepository
                .save(Subscriber.builder().username("testUser").password(passwordEncoder.encode("testPass"))
                        .email("test@mail.com").build());
        service1 = serviceRepository.save(
                Service.builder().name("TEST#1").owner(subscriber).serviceCategory(ServiceCategory.OTHER).build());
        service2 = serviceRepository.save(
                Service.builder().name("TEST#2").serviceCategory(ServiceCategory.OTHER).build());
        service3 = serviceRepository.save(
                Service.builder().name("TEST#3").owner(subscriber).serviceCategory(ServiceCategory.EDUCATION).build());
        subscription1 = subscriptionRepository.save(Subscription.builder().billingPeriod(BillingPeriod.MONTHLY)
                .currency("RUB").isActive(true).isDeleted(false).nextPayment(LocalDate.now().plusDays(7))
                .price(new BigDecimal(100)).service(service1).subscriber(subscriber).build());
        subscription2 = subscriptionRepository.save(Subscription.builder().billingPeriod(BillingPeriod.MONTHLY)
                .currency("RUB").isActive(true).isDeleted(false).nextPayment(LocalDate.now().plusDays(10))
                .price(new BigDecimal(1000)).service(service2).subscriber(subscriber).build());
        subscription3 = subscriptionRepository.save(Subscription.builder().billingPeriod(BillingPeriod.MONTHLY)
                .currency("RUB").isActive(true).isDeleted(false).nextPayment(LocalDate.now().plusDays(13))
                .price(new BigDecimal(100)).service(service3).subscriber(subscriber).build());

        for (int i = 0; i < 5; i++) {
            payments.add(paymentLogRepository.save(PaymentLog.builder().amount(subscription1.getPrice())
                    .paymentDate(LocalDate.now().minusDays(i * 30 + 1)).subscriber(subscriber)
                    .subscription(subscription1)
                    .status(PaymentStatus.SUCCESS).build()));
            payments.add(paymentLogRepository.save(PaymentLog.builder().amount(subscription2.getPrice())
                    .paymentDate(LocalDate.now().minusDays(i * 30 + 1)).subscriber(subscriber)
                    .subscription(subscription2)
                    .status(PaymentStatus.SUCCESS).build()));
            payments.add(paymentLogRepository.save(PaymentLog.builder().amount(subscription3.getPrice())
                    .paymentDate(LocalDate.now().minusDays(i * 30 + 1)).subscriber(subscriber)
                    .subscription(subscription3)
                    .status(PaymentStatus.SUCCESS).build()));

        }

        em.flush();
    }

    @Test
    @DisplayName("Must return categories analytics for month")
    void getStatByCategories_Month() throws Exception {
        double expectedEd = 100.0;
        double expectedOth = 1100.0;
        mockMvc.perform(get("/api/v1/analytics/categories")
                .header("Authorization", obtainToken()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$.[?(@.category == 'EDUCATION')].total").value(expectedEd))
                .andExpect(jsonPath("$.[?(@.category == 'OTHER')].total").value(expectedOth));
    }

    @Test
    @DisplayName("Must return categories analytics for year")
    void getStatByCategories_Year() throws Exception {
        double expectedEd = 400.0;
        double expectedOth = 4400.0;
        mockMvc.perform(get("/api/v1/analytics/categories")
                .param("period", "YEAR")
                .header("Authorization", obtainToken()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(
                        jsonPath("$.[?(@.category == 'EDUCATION')].total").value(expectedEd))
                .andExpect(
                        jsonPath("$.[?(@.category == 'OTHER')].total").value(expectedOth));
    }

    @Test
    @DisplayName("Must return categories analytics for all time")
    void getStatByCategories_All() throws Exception {
        double expectedEd = 500.0;
        double expectedOth = 5500.0;
        mockMvc.perform(get("/api/v1/analytics/categories")
                .param("period", "ALL")
                .header("Authorization", obtainToken()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(
                        jsonPath("$.[?(@.category == 'EDUCATION')].total").value(expectedEd))
                .andExpect(
                        jsonPath("$.[?(@.category == 'OTHER')].total").value(expectedOth));
    }

    @Test
    @DisplayName("Must return services analytics for month")
    void getStatByServices_Month() throws Exception {
        double expectedS1 = 100.0;
        double expectedS2 = 1000.0;
        double expectedS3 = 100.0;
        mockMvc.perform(get("/api/v1/analytics/services")
                .header("Authorization", obtainToken()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(3))
                .andExpect(jsonPath("$.[?(@.service == 'TEST#1')].total").value(expectedS1))
                .andExpect(jsonPath("$.[?(@.service == 'TEST#2')].total").value(expectedS2))
                .andExpect(jsonPath("$.[?(@.service == 'TEST#3')].total").value(expectedS3));
    }

    @Test
    @DisplayName("Must return services analytics for year")
    void getStatByServices_Year() throws Exception {
        double expectedS1 = 400.0;
        double expectedS2 = 4000.0;
        double expectedS3 = 400.0;
        mockMvc.perform(get("/api/v1/analytics/services")
                .param("period", "YEAR")
                .header("Authorization", obtainToken()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(3))
                .andExpect(jsonPath("$.[?(@.service == 'TEST#1')].total").value(expectedS1))
                .andExpect(jsonPath("$.[?(@.service == 'TEST#2')].total").value(expectedS2))
                .andExpect(jsonPath("$.[?(@.service == 'TEST#3')].total").value(expectedS3));
    }

    @Test
    @DisplayName("Must return services analytics for all time")
    void getStatByServices_All() throws Exception {
        double expectedS1 = 500.0;
        double expectedS2 = 5000.0;
        double expectedS3 = 500.0;
        mockMvc.perform(get("/api/v1/analytics/services")
                .param("period", "ALL")
                .header("Authorization", obtainToken()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(3))
                .andExpect(jsonPath("$.[?(@.service == 'TEST#1')].total").value(expectedS1))
                .andExpect(jsonPath("$.[?(@.service == 'TEST#2')].total").value(expectedS2))
                .andExpect(jsonPath("$.[?(@.service == 'TEST#3')].total").value(expectedS3));
    }

    @Test
    @DisplayName("Must return total for month")
    void getTotal_Month() throws Exception {
        double expected = 1200.0;
        mockMvc.perform(get("/api/v1/analytics/total")
                .header("Authorization", obtainToken()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.total").value(expected));
    }

    @Test
    @DisplayName("Must return total for year")
    void getTotal_Year() throws Exception {
        double expected = 4800.0;
        mockMvc.perform(get("/api/v1/analytics/total")
                .param("period", "YEAR")
                .header("Authorization", obtainToken()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.total").value(expected));
    }

    @Test
    @DisplayName("Must return total for all time")
    void getTotal_All() throws Exception {
        double expected = 6000.0;
        mockMvc.perform(get("/api/v1/analytics/total")
                .param("period", "ALL")
                .header("Authorization", obtainToken()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.total").value(expected));
    }

    @Test
    @DisplayName("Must return forecast")
    void getForecast() throws Exception {
        double expected = 1200.0;
        mockMvc.perform(get("/api/v1/analytics/forecast")
                .header("Authorization", obtainToken()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.total").value(expected));
    }

    private String obtainToken() throws Exception {
        String response = mockMvc.perform(post("/api/v1/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(new LoginRequest("testUser", "testPass")))).andReturn()
                .getResponse().getContentAsString();

        return "Bearer " + objectMapper.readTree(response).get("token").asString();
    }

}
