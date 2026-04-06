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

import ru.bicev.submonitor.dto.auth.LoginRequest;
import ru.bicev.submonitor.dto.payment.PaymentCreateLogRequest;
import ru.bicev.submonitor.dto.payment.PaymentUpdateLogRequest;
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

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
public class PaymentLogRestControllerIT {

    @Autowired
    private PaymentLogRepository paymentLogRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private SubscriptionRepository subscriptionRepository;

    @Autowired
    private SubscriberRepository subscriberRepository;

    @Autowired
    private ServiceRepository serviceRepository;

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    private Subscriber subscriber;
    private Service service;
    private Subscription subscription1;
    private Long sub1Id;
    private Subscription subscription2;
    private Long sub2Id;
    private List<PaymentLog> payments = new ArrayList<>();
    private PaymentLog paymentLog;
    private Long logId;
    private PaymentCreateLogRequest createLogRequest;
    private PaymentCreateLogRequest invalidCreateRequest;

    private PaymentUpdateLogRequest updateLogRequest;

    @BeforeEach
    void setUp() {
        subscriber = subscriberRepository.save(Subscriber.builder().username("testUser").email("test@mail.com")
                .password(passwordEncoder.encode("testPass")).build());
        service = serviceRepository.save(Service.builder().name("Test Service").owner(subscriber)
                .serviceCategory(ServiceCategory.OTHER).build());
        subscription1 = subscriptionRepository.save(
                Subscription.builder().billingPeriod(BillingPeriod.MONTHLY).price(new BigDecimal(10.00)).currency("USD")
                        .isActive(true).isDeleted(false).service(service).subscriber(subscriber)
                        .nextPayment(LocalDate.now().plusDays(7)).build());
        sub1Id = subscription1.getId();
        subscription2 = subscriptionRepository.save(
                Subscription.builder().billingPeriod(BillingPeriod.MONTHLY).price(new BigDecimal(20.00)).currency("USD")
                        .isActive(true).isDeleted(false).service(service).subscriber(subscriber)
                        .nextPayment(LocalDate.now().plusDays(15)).build());
        sub2Id = subscription2.getId();
        paymentLog = paymentLogRepository.save(PaymentLog.builder().amount(new BigDecimal(100))
                .paymentDate(LocalDate.now().minusDays(5)).status(PaymentStatus.SUCCESS)
                .subscriber(subscriber).subscription(subscription1).build());
        logId = paymentLog.getId();
        for (int i = 0; i < 5; i++) {
            payments.add(paymentLogRepository.save(PaymentLog.builder().amount(new BigDecimal((i + 1) * 10))
                    .paymentDate(LocalDate.now().minusDays((i + 1) * 10)).status(PaymentStatus.SUCCESS)
                    .subscriber(subscriber).subscription(i % 2 == 0 ? subscription1 : subscription2).build()));
        }
        createLogRequest = new PaymentCreateLogRequest(sub1Id, new BigDecimal(100),
                LocalDate.now());
        invalidCreateRequest = new PaymentCreateLogRequest(sub2Id, new BigDecimal(-1),
                LocalDate.now());
        updateLogRequest = new PaymentUpdateLogRequest(sub2Id, new BigDecimal(75), null,
                null);

    }

    @Test
    @DisplayName("Must create new log")
    void createLogRequest_Success() throws Exception {
        mockMvc.perform(post("/api/v1/logs")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(createLogRequest))
                .header("Authorization", obtainToken()))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").exists())
                .andExpect(jsonPath("$.amount").value(createLogRequest.amount()))
                .andExpect(jsonPath("$.status").value(PaymentStatus.SUCCESS.name()))
                .andExpect(jsonPath("$.paymentDate").value(createLogRequest.paymentDate().toString()));
    }

    @Test
    @DisplayName("Must return Bad Request creating new log with invalid data")
    void createLogRequest_BadRequest() throws Exception {
        mockMvc.perform(post("/api/v1/logs")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(invalidCreateRequest))
                .header("Authorization", obtainToken()))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("Must return Not Found if subscription not exists")
    void createLogRequest_NotFound() throws Exception {
        mockMvc.perform(post("/api/v1/logs")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper
                        .writeValueAsString(new PaymentCreateLogRequest(999999L, new BigDecimal(10), LocalDate.now())))
                .header("Authorization", obtainToken()))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("Must return log by Id")
    void getLogById_Success() throws Exception {
        mockMvc.perform(get("/api/v1/logs/" + logId)
                .header("Authorization", obtainToken()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.amount").value(paymentLog.getAmount()))
                .andExpect(jsonPath("$.paymentDate").value(paymentLog.getPaymentDate().toString()))
                .andExpect(jsonPath("$.status").value(paymentLog.getStatus().toString()));

    }

    @Test
    @DisplayName("Must return Not Found when id is invalid")
    void getLogById_NotFound() throws Exception {
        mockMvc.perform(get("/api/v1/logs/" + 9999994)
                .header("Authorization", obtainToken()))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("Must return list of logs")
    void getPagedLogs_Success() throws Exception {
        mockMvc.perform(get("/api/v1/logs")
                .param("page", "0")
                .param("size", "10")
                .param("sort", "paymentDate,desc")
                .header("Authorization", obtainToken()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content.length()").value(6));
    }

    @Test
    @DisplayName("Must update log")
    void updateLog_Success() throws Exception {
        mockMvc.perform(patch("/api/v1/logs/" + logId)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(updateLogRequest))
                .header("Authorization", obtainToken()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.amount").value(updateLogRequest.amount()))
                .andExpect(jsonPath("$.subsriptionId").value(updateLogRequest.subscriptionId()));

    }

    @Test
    @DisplayName("Must return Not Found while updating log")
    void updateLog_NotFound() throws Exception {
        mockMvc.perform(patch("/api/v1/logs/" + 879866L)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(updateLogRequest))
                .header("Authorization", obtainToken()))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("Must delete log")
    void deleteLog_Success() throws Exception {
        mockMvc.perform(delete("/api/v1/logs/" + logId).header("Authorization", obtainToken()))
                .andExpect(status().isNoContent());

    }

    @Test
    @DisplayName("Must return Not Found while deleting log")
    void deleteLog_NotFound() throws Exception {
        mockMvc.perform(delete("/api/v1/logs/" + 4847555L).header("Authorization", obtainToken()))
                .andExpect(status().isNotFound());

    }

    private String obtainToken() throws Exception {
        String response = mockMvc.perform(post("/api/v1/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(new LoginRequest("testUser", "testPass")))).andReturn()
                .getResponse().getContentAsString();

        return "Bearer " + objectMapper.readTree(response).get("token").asString();
    }

}
