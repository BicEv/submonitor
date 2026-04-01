package ru.bicev.submonitor.unit;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import ru.bicev.submonitor.dto.service.ServiceCreateRequest;
import ru.bicev.submonitor.dto.service.ServiceUpdateRequest;
import ru.bicev.submonitor.entity.Service;
import ru.bicev.submonitor.entity.Subscriber;
import ru.bicev.submonitor.entity.enums.ServiceCategory;
import ru.bicev.submonitor.exception.NotFoundException;
import ru.bicev.submonitor.repository.ServiceRepository;
import ru.bicev.submonitor.service.SecurityService;
import ru.bicev.submonitor.service.ServiceService;

@ExtendWith(MockitoExtension.class)
public class ServiceServiceTest {

    @Mock
    private ServiceRepository serviceRepository;

    @Mock
    private SecurityService securityService;

    @InjectMocks
    private ServiceService service;

    private Subscriber subscriber;
    private Service created;
    private Service updated;
    private List<Service> services;
    private ServiceCreateRequest createRequest;
    private ServiceUpdateRequest updateRequest;

    @BeforeEach
    void setUp() {
        subscriber = Subscriber.builder()
                .id(1L)
                .lastLoggedAt(LocalDateTime.now())
                .createdAt(LocalDateTime.of(LocalDate.of(2025, 12, 31), LocalTime.of(12, 00)))
                .email("test@mail.com")
                .password("password")
                .username("testSubscriber")
                .build();
        created = Service.builder()
                .id(1L)
                .name("CREATED")
                .owner(subscriber)
                .serviceCategory(ServiceCategory.EDUCATION)
                .build();
        updated = Service.builder()
                .id(10L)
                .name("UPDATED")
                .owner(subscriber)
                .serviceCategory(ServiceCategory.OTHER)
                .build();
        services = List.of(
                Service.builder().id(11L).name("SERVICE1").owner(subscriber).serviceCategory(ServiceCategory.OTHER)
                        .build(),
                Service.builder().id(12L).name("SERVICE2").owner(subscriber).serviceCategory(ServiceCategory.OTHER)
                        .build(),
                Service.builder().id(13L).name("SERVICE3").owner(subscriber).serviceCategory(ServiceCategory.OTHER)
                        .build(),
                Service.builder().id(14L).name("SERVICE4").owner(subscriber).serviceCategory(ServiceCategory.OTHER)
                        .build());
        createRequest = new ServiceCreateRequest("CREATED", ServiceCategory.EDUCATION);
        updateRequest = new ServiceUpdateRequest("UPDATED SERVICE", ServiceCategory.SHOPPING);
    }

    @Test
    @DisplayName("Must create new service correctly")
    void createService_Success() {
        when(securityService.getCurrentSubscriber()).thenReturn(subscriber);
        when(serviceRepository.save(any(Service.class))).thenReturn(created);

        var result = service.createService(createRequest);

        assertNotNull(result);
        assertEquals(createRequest.name(), result.name());
        assertEquals(createRequest.serviceCategory().name(), result.serviceCategory());
        assertNotNull(result.id());

        verify(securityService, times(1)).getCurrentSubscriber();
        verify(serviceRepository, times(1)).save(any(Service.class));
    }

    @Test
    @DisplayName("Must retrieve service by Id")
    void getServiceById_Success() {
        when(securityService.getCurrentUserId()).thenReturn(1L);
        when(serviceRepository.findByIdAndOwnerIdEqualsOrNull(1L, 1L)).thenReturn(Optional.of(created));

        var result = service.getServiceById(1L);

        assertNotNull(result);
        assertEquals(created.getName(), result.name());
        assertEquals(created.getServiceCategory().name(), result.serviceCategory());

        verify(securityService, times(1)).getCurrentUserId();
        verify(serviceRepository, times(1)).findByIdAndOwnerIdEqualsOrNull(1L, 1L);
    }

    @Test
    @DisplayName("Must throw NotFoundException when service not found")
    void getServiceById_ThrowsNotFoundException() {
        when(securityService.getCurrentUserId()).thenReturn(1L);
        when(serviceRepository.findByIdAndOwnerIdEqualsOrNull(100L, 1L)).thenReturn(Optional.empty());

        assertThrows(NotFoundException.class, () -> service.getServiceById(100L));

        verify(securityService, times(1)).getCurrentUserId();
        verify(serviceRepository, times(1)).findByIdAndOwnerIdEqualsOrNull(100L, 1L);
    }

    @Test
    @DisplayName("Must retrieve services")
    void getAvailableServices_Success() {
        when(securityService.getCurrentUserId()).thenReturn(1L);
        when(serviceRepository.findAllAvailable(1L)).thenReturn(services);

        var result = service.getAvailableServices();

        assertNotNull(result);
        assertEquals(services.size(), result.size());
        assertEquals(services.get(0).getId(), result.get(0).id());
        assertEquals(services.get(0).getName(), result.get(0).name());
        assertEquals(services.get(0).getServiceCategory().name(), result.get(0).serviceCategory());
        assertEquals(services.get(1).getId(), result.get(1).id());
        assertEquals(services.get(1).getName(), result.get(1).name());
        assertEquals(services.get(1).getServiceCategory().name(), result.get(1).serviceCategory());
        assertEquals(services.get(2).getId(), result.get(2).id());
        assertEquals(services.get(2).getName(), result.get(2).name());
        assertEquals(services.get(2).getServiceCategory().name(), result.get(2).serviceCategory());

        verify(securityService, times(1)).getCurrentUserId();
        verify(serviceRepository, times(1)).findAllAvailable(1L);

    }

    @Test
    @DisplayName("Must update service by Id")
    void updateService_Success() {
        when(securityService.getCurrentUserId()).thenReturn(1L);
        when(serviceRepository.findByIdAndOwnerId(10L, 1L)).thenReturn(Optional.of(updated));

        var result = service.updateService(10L, updateRequest);

        assertNotNull(result);
        assertEquals(updateRequest.name(), result.name());
        assertEquals(updateRequest.serviceCategory().name(), result.serviceCategory());
        assertNotNull(result.id());

        verify(securityService, times(1)).getCurrentUserId();
        verify(serviceRepository, times(1)).findByIdAndOwnerId(10L, 1L);
    }

    @Test
    @DisplayName("Must throw NotFoundException when service not found")
    void updateService_ThrowsNotFoundException() {
        when(securityService.getCurrentUserId()).thenReturn(1L);
        when(serviceRepository.findByIdAndOwnerId(20L, 1L)).thenReturn(Optional.empty());

        assertThrows(NotFoundException.class, () -> service.updateService(20L, updateRequest));

        verify(securityService, times(1)).getCurrentUserId();
        verify(serviceRepository, times(1)).findByIdAndOwnerId(20L, 1L);
    }

    @Test
    @DisplayName("Must delete service by Id")
    void deleteService_Success() {
        when(securityService.getCurrentUserId()).thenReturn(1L);
        when(serviceRepository.findByIdAndOwnerId(1L, 1L)).thenReturn(Optional.of(created));

        service.deleteService(1L);

        verify(securityService, times(1)).getCurrentUserId();
        verify(serviceRepository, times(1)).delete(created);
    }

    @Test
    @DisplayName("Must throw NotFoundException when service not found")
    void deleteService_ThrowsNotFoundException() {
        when(securityService.getCurrentUserId()).thenReturn(1L);
        when(serviceRepository.findByIdAndOwnerId(100L, 1L)).thenReturn(Optional.empty());

        assertThrows(NotFoundException.class, () -> service.deleteService(100L));

        verify(securityService, times(1)).getCurrentUserId();
        verify(serviceRepository, never()).delete(any());
    }

}
