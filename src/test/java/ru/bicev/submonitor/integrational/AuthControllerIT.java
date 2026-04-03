package ru.bicev.submonitor.integrational;

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
import ru.bicev.submonitor.dto.auth.SingupRequest;
import ru.bicev.submonitor.entity.Subscriber;
import ru.bicev.submonitor.repository.SubscriberRepository;
import tools.jackson.databind.ObjectMapper;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
public class AuthControllerIT {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private SubscriberRepository subscriberRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private ObjectMapper objectMapper;

    private SingupRequest singupRequest = new SingupRequest("testUser", "testPass", "test@mail.com");
    private SingupRequest invalidSingupRequest = new SingupRequest("", "", "bumBam");
    private LoginRequest loginRequest = new LoginRequest("testUser", "testPass");
    private LoginRequest invalidLoginRequest = new LoginRequest("testUser", "invalidPass");

    @Test
    @DisplayName("Must register new subscriber")
    void register_success() throws Exception {

        mockMvc.perform(post("/api/v1/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(singupRequest)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.username").value("testUser"))
                .andExpect(jsonPath("$.token").exists())
                .andExpect(jsonPath("$.id").exists());
    }

    @Test
    @DisplayName("Duplicate subscriber when creating new sub")
    void register_DuplicateSubscriber() throws Exception {
        subscriberRepository.save(Subscriber.builder().username("testUser").password(passwordEncoder.encode("testPass"))
                .email("test@mail.com").build());

        mockMvc.perform(post("/api/v1/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(singupRequest)))
                .andExpect(status().isBadRequest());

    }

    @Test
    @DisplayName("Validation testing")
    void register_InvalidUsernameEmail() throws Exception {
        mockMvc.perform(post("/api/v1/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(invalidSingupRequest)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("Login success")
    void login_Success() throws Exception {
        subscriberRepository.save(Subscriber.builder().username("testUser").password(passwordEncoder.encode("testPass"))
                .email("test@mail.com").build());

        mockMvc.perform(post("/api/v1/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(loginRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.username").value(loginRequest.username()))
                .andExpect(jsonPath("$.token").exists())
                .andExpect(jsonPath("$.id").exists());
    }

    @Test
    @DisplayName("Login invalid credentials")
    void login_InvalidCredentials() throws Exception {
        subscriberRepository.save(Subscriber.builder().username("testUser").password(passwordEncoder.encode("testPass"))
                .email("test@mail.com").build());

        mockMvc.perform(post("/api/v1/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(invalidLoginRequest)))
                .andExpect(status().isUnauthorized());
    }

}
