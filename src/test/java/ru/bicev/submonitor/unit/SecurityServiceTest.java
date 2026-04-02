package ru.bicev.submonitor.unit;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

import ru.bicev.submonitor.entity.Subscriber;
import ru.bicev.submonitor.repository.SubscriberRepository;
import ru.bicev.submonitor.security.UserDetailsImpl;
import ru.bicev.submonitor.service.SecurityService;

@ExtendWith(MockitoExtension.class)
public class SecurityServiceTest {

    @Mock
    private SubscriberRepository subscriberRepository;

    @InjectMocks
    private SecurityService service;

    @BeforeEach
    void setUp() {
        SecurityContextHolder.clearContext();
    }

    @Test
    @DisplayName("Should return user ID from SecurityContext")
    void getCurrentUserId_Success() {
        UserDetailsImpl principal = new UserDetailsImpl(1L, "testUser", "testPass", "test@mail.com", List.of());
        Authentication auth = new UsernamePasswordAuthenticationToken(principal, principal.getAuthorities());
        SecurityContextHolder.getContext().setAuthentication(auth);

        Long userId = service.getCurrentUserId();

        assertEquals(1L, userId);
    }

    @Test
    @DisplayName("Should throw IllegalStateException when not authenticated")
    void getCurrentUserId_ThrowsIllegalStateException() {
        assertThrows(IllegalStateException.class, () -> service.getCurrentUserId());
    }

    @Test
    @DisplayName("Should return subscriber entity from DB")
    void getCurrentSubscriber_Success() {
        UserDetailsImpl principal = new UserDetailsImpl(1L, "testUser", "testPass", "test@mail.com", List.of());
        Authentication auth = new UsernamePasswordAuthenticationToken(principal, null, principal.getAuthorities());
        SecurityContextHolder.getContext().setAuthentication(auth);

        Subscriber subscriber = Subscriber.builder().id(1L).email("test@mail.com").build();
        when(subscriberRepository.findById(1L)).thenReturn(Optional.of(subscriber));

        Subscriber result = service.getCurrentSubscriber();

        assertNotNull(result);
        assertEquals(1L, result.getId());
        verify(subscriberRepository).findById(1L);
    }

}
