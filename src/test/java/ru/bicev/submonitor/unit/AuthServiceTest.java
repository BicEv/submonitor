package ru.bicev.submonitor.unit;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;

import ru.bicev.submonitor.dto.auth.JwtResponse;
import ru.bicev.submonitor.dto.auth.LoginRequest;
import ru.bicev.submonitor.dto.auth.SingupRequest;
import ru.bicev.submonitor.entity.Subscriber;
import ru.bicev.submonitor.exception.DuplicateSubscriberException;
import ru.bicev.submonitor.repository.SubscriberRepository;
import ru.bicev.submonitor.security.JwtUtil;
import ru.bicev.submonitor.security.UserDetailsImpl;
import ru.bicev.submonitor.service.AuthService;

@ExtendWith(MockitoExtension.class)
public class AuthServiceTest {

    @Mock
    private SubscriberRepository subscriberRepository;

    @Mock
    private JwtUtil jwtUtil;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private AuthenticationManager authenticationManager;

    @InjectMocks
    private AuthService service;

    private SingupRequest singupRequest;
    private Subscriber subscriber;

    @BeforeEach
    void setUp() {
        singupRequest = new SingupRequest("testUser", "testPass", "test@mail.com");
        subscriber = Subscriber.builder().id(1L).username("testUser").password("testPass").email("test@mail.com")
                .build();
        SecurityContextHolder.clearContext();
    }

    @Test
    @DisplayName("Should register new subscriber")
    void registerSubscriber_Success() {
        when(subscriberRepository.existsByUsername("testUser")).thenReturn(false);
        when(subscriberRepository.existsByEmail("test@mail.com")).thenReturn(false);
        when(passwordEncoder.encode("testPass")).thenReturn("encodedPass");
        when(subscriberRepository.save(any(Subscriber.class))).thenReturn(subscriber);

        UserDetailsImpl principal = new UserDetailsImpl(1L, "testUser", "encodedPass", "test@mail.com", List.of());
        Authentication auth = new UsernamePasswordAuthenticationToken(principal, null);

        when(authenticationManager.authenticate(any())).thenReturn(auth);
        when(jwtUtil.generateToken(auth)).thenReturn("mock-jwt-token");

        var result = service.registerSubscriber(singupRequest);

        assertNotNull(result);
        assertEquals(1L, result.id());
        assertEquals("mock-jwt-token", result.token());
        verify(subscriberRepository, times(1)).save(any(Subscriber.class));
    }

    @Test
    @DisplayName("Should throw exception when username already exists")
    void registerSubscriber_DuplicateUser_ThrowsException() {
        when(subscriberRepository.existsByUsername("testUser")).thenReturn(true);

        // Act & Assert
        assertThrows(DuplicateSubscriberException.class,
                () -> service.registerSubscriber(singupRequest));

        verify(subscriberRepository, never()).save(any());
        verify(authenticationManager, never()).authenticate(any());
    }

    @Test
    @DisplayName("Should login user")
    void login_Success() {
        LoginRequest request = new LoginRequest("testUser", "rawPass");

        UserDetailsImpl principal = new UserDetailsImpl(1L, "testUser", "encodedPass", "test@mail.com", List.of());

        Authentication auth = new UsernamePasswordAuthenticationToken(principal, null);

        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                .thenReturn(auth);

        when(jwtUtil.generateToken(auth)).thenReturn("mock-jwt-token");

        JwtResponse response = service.login(request);

        assertAll(
                () -> assertEquals("mock-jwt-token", response.token()),
                () -> assertEquals(1L, response.id()),
                () -> assertEquals("testUser", response.username()),
                () -> assertEquals(auth, SecurityContextHolder.getContext().getAuthentication()));

        verify(authenticationManager).authenticate(any());
        verify(jwtUtil).generateToken(auth);
    }

    @Test
    @DisplayName("Should throw Exception when invalid username/pass")
    void login_ThrowsException() {
        LoginRequest request = new LoginRequest("testUser", "rawPassword");
        Authentication auth = new UsernamePasswordAuthenticationToken(request.username(), request.password());

        when(authenticationManager.authenticate(auth)).thenThrow(BadCredentialsException.class);

        assertThrows(BadCredentialsException.class, () -> service.login(request));

        verify(jwtUtil, never()).generateToken(any());
    }

}
