package ru.bicev.submonitor.integrational;

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
import ru.bicev.submonitor.dto.subscription.SubCreationRequest;
import ru.bicev.submonitor.dto.subscription.SubUpdateRequest;
import ru.bicev.submonitor.entity.Service;
import ru.bicev.submonitor.entity.Subscriber;
import ru.bicev.submonitor.entity.Subscription;
import ru.bicev.submonitor.entity.enums.BillingPeriod;
import ru.bicev.submonitor.entity.enums.ServiceCategory;
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

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
public class SubscriptionRestControllerIT {

        @Autowired
        private ServiceRepository serviceRepository;

        @Autowired
        private SubscriptionRepository subscriptionRepository;

        @Autowired
        private SubscriberRepository subscriberRepository;

        @Autowired
        private MockMvc mockMvc;

        @Autowired
        private ObjectMapper objectMapper;

        @Autowired
        private PasswordEncoder passwordEncoder;

        private Subscriber owner;
        private Service service;
        private Subscription subscription;
        private List<Subscription> subscriptions = new ArrayList<>();
        private Long subscriptionId;
        private Long serviceId;
        private SubCreationRequest creationRequest;
        private SubCreationRequest invalidIdCreationRequest = new SubCreationRequest(9999990L, new BigDecimal(100),
                        "RUB",
                        BillingPeriod.MONTHLY, LocalDate.now().plusDays(10));
        private SubCreationRequest invalidDataCreationRequest;
        private SubUpdateRequest updateRequest;

        @BeforeEach
        void setUp() {
                owner = subscriberRepository.save(Subscriber.builder().username("testUser").email("test@mail.com")
                                .password(passwordEncoder.encode("testPass")).build());

                service = serviceRepository
                                .save(Service.builder().name("Service").serviceCategory(ServiceCategory.EDUCATION)
                                                .build());
                serviceId = service.getId();
                creationRequest = new SubCreationRequest(serviceId, new BigDecimal(299.99), "RUB",
                                BillingPeriod.MONTHLY, LocalDate.now().plusDays(7));
                invalidDataCreationRequest = new SubCreationRequest(serviceId, new BigDecimal(00), "Not_Supported",
                                BillingPeriod.MONTHLY,
                                LocalDate.now().plusDays(1));
                updateRequest = new SubUpdateRequest(new BigDecimal(15.99), "USD", null, null, null);
                subscription = subscriptionRepository
                                .save(Subscription.builder().billingPeriod(BillingPeriod.YEARLY)
                                                .price(new BigDecimal(100))
                                                .currency("USD").isActive(true).isDeleted(false)
                                                .nextPayment(LocalDate.now().plusMonths(6))
                                                .service(service).subscriber(owner).build());
                subscriptionId = subscription.getId();

                for (int i = 0; i < 4; i++) {
                        subscriptions.add(subscriptionRepository
                                        .save(Subscription.builder().billingPeriod(BillingPeriod.YEARLY)
                                                        .price(new BigDecimal((1 + i) * 50))
                                                        .currency("USD").isActive(i == 3 ? false : true)
                                                        .isDeleted(i == 3 ? true : false)
                                                        .nextPayment(LocalDate.now().plusMonths(i))
                                                        .service(service).subscriber(owner).build()));
                }

        }

        @Test
        @DisplayName("Must create new subscription")
        void createSubscription_Success() throws Exception {
                mockMvc.perform(post("/api/v1/subscriptions")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(creationRequest))
                                .header("Authorization", obtainToken()))
                                .andExpect(status().isCreated())
                                .andExpect(jsonPath("$.id").exists())
                                .andExpect(jsonPath("$.price").value(creationRequest.price()))
                                .andExpect(jsonPath("$.nextPayment").value(creationRequest.nextPayment().toString()))
                                .andExpect(jsonPath("$.currency").value(creationRequest.currency()));

        }

        @Test
        @DisplayName("Must return 404 when service not found")
        void createSubscription_NotFound() throws Exception {
                mockMvc.perform(post("/api/v1/subscriptions")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(invalidIdCreationRequest))
                                .header("Authorization", obtainToken()))
                                .andExpect(status().isNotFound());
        }

        @Test
        @DisplayName("Must return 400 when data is invalid")
        void createSubscription_BadRequest() throws Exception {
                mockMvc.perform(post("/api/v1/subscriptions")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(invalidDataCreationRequest))
                                .header("Authorization", obtainToken()))
                                .andExpect(status().isBadRequest());
        }

        @Test
        @DisplayName("Must return subscription by id")
        void getSubscriptionById_Success() throws Exception {
                mockMvc.perform(get("/api/v1/subscriptions/" + subscriptionId)
                                .header("Authorization", obtainToken()))
                                .andExpect(status().isOk())
                                .andExpect(jsonPath("$.price").value(subscription.getPrice()))
                                .andExpect(jsonPath("$.billingPeriod").value(subscription.getBillingPeriod().name()))
                                .andExpect(jsonPath("$.nextPayment").value(subscription.getNextPayment().toString()));
        }

        @Test
        @DisplayName("Must return 404 when subscription not found")
        void getSubscriptionById_NotFound() throws Exception {
                mockMvc.perform(get("/api/v1/subscriptions/" + 999946846)
                                .header("Authorization", obtainToken()))
                                .andExpect(status().isNotFound());
        }

        @Test
        @DisplayName("Must return all not deleted subscriptions")
        void getAllSubscriptions_Success() throws Exception {
                mockMvc.perform(get("/api/v1/subscriptions")
                                .header("Authorization", obtainToken()))
                                .andExpect(status().isOk())
                                .andExpect(jsonPath("$.length()").value(4));
        }

        @Test
        @DisplayName("Must update subscription")
        void updateSubscription_Success() throws Exception {
                mockMvc.perform(patch("/api/v1/subscriptions/" + subscriptionId)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(updateRequest))
                                .header("Authorization", obtainToken()))
                                .andExpect(status().isOk())
                                .andExpect(jsonPath("$.price").value(updateRequest.price()))
                                .andExpect(jsonPath("$.currency").value(updateRequest.currency()))
                                .andExpect(jsonPath("$.nextPayment").value(subscription.getNextPayment().toString()))
                                .andExpect(jsonPath("$.billingPeriod").value(subscription.getBillingPeriod().name()))
                                .andExpect(jsonPath("$.isActive").value(subscription.isActive()));

        }

        @Test
        @DisplayName("Must return 404 when subscription not found")
        void updateSubscription_NotFound() throws Exception {
                mockMvc.perform(patch("/api/v1/subscriptions/" + 987798798)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(updateRequest))
                                .header("Authorization", obtainToken()))
                                .andExpect(status().isNotFound());

        }

        @Test
        @DisplayName("Must delete subscription by id")
        void deleteSubscription_Success() throws Exception {
                mockMvc.perform(delete("/api/v1/subscriptions/" + subscriptions.get(0).getId())
                                .header("Authorization", obtainToken()))
                                .andExpect(status().isNoContent());
        }

        @Test
        @DisplayName("Must return 404 when subscription not found")
        void deleteSubscription_NotFound() throws Exception {
                mockMvc.perform(delete("/api/v1/subscriptions/" + 498749844)
                                .header("Authorization", obtainToken()))
                                .andExpect(status().isNotFound());
        }

        private String obtainToken() throws Exception {
                String response = mockMvc.perform(post("/api/v1/auth/login")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(new LoginRequest("testUser", "testPass"))))
                                .andReturn()
                                .getResponse().getContentAsString();

                return "Bearer " + objectMapper.readTree(response).get("token").asString();
        }

}
