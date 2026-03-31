package ru.bicev.submonitor.controller;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import ru.bicev.submonitor.dto.service.ServiceCreateRequest;
import ru.bicev.submonitor.dto.service.ServiceDto;
import ru.bicev.submonitor.dto.service.ServiceUpdateRequest;
import ru.bicev.submonitor.service.ServiceService;

/**
 * Контроллер для управления сервисами
 */
@RestController
@RequestMapping("/api/v1/services")
@Slf4j
@RequiredArgsConstructor
public class ServiceRestController {

    private final ServiceService serviceService;

    /**
     * Создает новый сервис для пользователя в соответствии с запросом
     * 
     * @param request запрос на создание пользователя
     * @return дто, содержащее данные созданного сервиса
     */
    @PostMapping
    public ResponseEntity<ServiceDto> createService(@Valid @RequestBody ServiceCreateRequest request) {
        var createdService = serviceService.createService(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(createdService);
    }

    /**
     * Получает сервис по его идентификатору
     * 
     * @param serviceId идентификатор искомого сервиса
     * @return дто, содержащее данные искомого сервиса
     */
    @GetMapping("/{serviceId}")
    public ResponseEntity<ServiceDto> getServiceById(@PathVariable Long serviceId) {
        var service = serviceService.getServiceById(serviceId);
        return ResponseEntity.status(HttpStatus.OK).body(service);
    }

    /**
     * Получает список всех доступных для пользователя сервисов
     * 
     * @return список всех доступных для пользователя сервисов
     */
    @GetMapping
    public ResponseEntity<List<ServiceDto>> getAvailableServices() {
        var services = serviceService.getAvailableServices();
        return ResponseEntity.status(HttpStatus.OK).body(services);
    }

    /**
     * Метод обновляющий данные сервиса в соотвествии с запросом
     * 
     * @param serviceId идентификатор сервиса, подлежащего обновлению
     * @param request   запрос содержащий данные для обновления
     * @return дто, содержащее данные обновленного сервиса
     */
    @PatchMapping("/{serviceId}")
    public ResponseEntity<ServiceDto> updateService(@PathVariable Long serviceId,
            @RequestBody ServiceUpdateRequest request) {
        var updated = serviceService.updateService(serviceId, request);
        return ResponseEntity.status(HttpStatus.OK).body(updated);
    }

    /**
     * Удаляет сервис по его идентификатору
     * 
     * @param serviceId идентификатор сервиса. подлежащего удалению
     * @return ответ со статусом 204 No Content
     */
    @DeleteMapping("/{serviceId}")
    public ResponseEntity<Void> deleteService(@PathVariable Long serviceId) {
        serviceService.deleteService(serviceId);
        return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
    }

}
