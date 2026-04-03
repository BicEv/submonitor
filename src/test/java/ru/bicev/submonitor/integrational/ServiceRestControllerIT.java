package ru.bicev.submonitor.integrational;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

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

import jakarta.transaction.Transactional;
import ru.bicev.submonitor.dto.auth.LoginRequest;
import ru.bicev.submonitor.dto.service.ServiceCreateRequest;
import ru.bicev.submonitor.dto.service.ServiceUpdateRequest;
import ru.bicev.submonitor.entity.Service;
import ru.bicev.submonitor.entity.Subscriber;
import ru.bicev.submonitor.entity.enums.ServiceCategory;
import ru.bicev.submonitor.repository.ServiceRepository;
import ru.bicev.submonitor.repository.SubscriberRepository;
import tools.jackson.databind.ObjectMapper;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
public class ServiceRestControllerIT {

    @Autowired
    private ServiceRepository serviceRepository;

    @Autowired
    private SubscriberRepository subscriberRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private MockMvc mockMvc;

    private ServiceCreateRequest createRequest = new ServiceCreateRequest("Test Service", ServiceCategory.OTHER);
    private ServiceCreateRequest invalidCreateRequest = new ServiceCreateRequest("", null);
    private ServiceUpdateRequest updateRequest = new ServiceUpdateRequest("Updated Service",
            ServiceCategory.HEALTH_FITNESS);
    private Subscriber owner;
    private Service service;
    private Long serviceId;
    private List<Service> services = new ArrayList<>();

    @BeforeEach
    void setUp() {
        owner = subscriberRepository.save(Subscriber.builder().username("testUser").email("test@mail.com")
                .password(passwordEncoder.encode("testPass")).build());

        service = serviceRepository
                .save(Service.builder().name("Service").serviceCategory(ServiceCategory.EDUCATION).build());
        serviceId = service.getId();

        for (int i = 0; i < 5; i++) {
            services.add(
                    serviceRepository
                            .save(Service.builder().name("Service " + i).serviceCategory(ServiceCategory.OTHER)
                                    .owner(owner)
                                    .build()));
        }

    }

    @Test
    @DisplayName("Must create new service")
    void createService_Success() throws Exception {
        mockMvc.perform(post("/api/v1/services")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(createRequest))
                .header("Authorization", obtainToken()))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").exists())
                .andExpect(jsonPath("$.name").value(createRequest.name()))
                .andExpect(jsonPath("$.serviceCategory").value(createRequest.serviceCategory().name()));
    }

    @Test
    @DisplayName("Invalid creation request")
    void createService_InvalidRequest() throws Exception {
        mockMvc.perform(post("/api/v1/services")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(invalidCreateRequest))
                .header("Authorization", obtainToken()))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("Must return service by id")
    void getServiceById_Success() throws Exception {
        mockMvc.perform(get("/api/v1/services/" + serviceId)
                .header("Authorization", obtainToken()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(serviceId))
                .andExpect(jsonPath("$.name").value(service.getName()))
                .andExpect(jsonPath("$.serviceCategory").value(service.getServiceCategory().name()));

    }

    @Test
    @DisplayName("404 when service not found")
    void getServiceById_NotFound() throws Exception {
        mockMvc.perform(get("/api/v1/services/" + 777513)
                .header("Authorization", obtainToken()))
                .andExpect(status().isNotFound());

    }

    @Test
    @DisplayName("Must return all available services")
    void getAvailableServices_Success() throws Exception {
        mockMvc.perform(get("/api/v1/services")
                .header("Authorization", obtainToken()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(6));
    }

    @Test
    @DisplayName("Must not update service if not owner")
    void updateService_Success() throws Exception {
        mockMvc.perform(patch("/api/v1/services/" + services.get(0).getId())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(updateRequest))
                .header("Authorization", obtainToken()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value(updateRequest.name()))
                .andExpect(jsonPath("$.serviceCategory").value(updateRequest.serviceCategory().name()));
    }

    @Test
    @DisplayName("Must not update service if not owner")
    void updateService_NotOwner() throws Exception {
        mockMvc.perform(patch("/api/v1/services/" + serviceId)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(updateRequest))
                .header("Authorization", obtainToken()))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("Must return 404 when service to udpate is not found")
    void updateService_NotFound() throws Exception {
        mockMvc.perform(patch("/api/v1/services/" + 9997856)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(updateRequest))
                .header("Authorization", obtainToken()))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("Must delete service by id")
    void deleteService_Success() throws Exception {
        mockMvc.perform(delete("/api/v1/services/" + services.get(1).getId())
                .header("Authorization", obtainToken()))
                .andExpect(status().isNoContent());
    }

    @Test
    @DisplayName("Must not delete service by id if not owner")
    void deleteService_NotOwner() throws Exception {
        mockMvc.perform(delete("/api/v1/services/" + serviceId)
                .header("Authorization", obtainToken()))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("Must return 404 when service is not found")
    void deleteService_NotFound() throws Exception {
        mockMvc.perform(delete("/api/v1/services/" + 8416846)
                .header("Authorization", obtainToken()))
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
