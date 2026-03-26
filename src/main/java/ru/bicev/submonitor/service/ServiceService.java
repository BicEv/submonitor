package ru.bicev.submonitor.service;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.transaction.annotation.Transactional;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import ru.bicev.submonitor.dto.service.ServiceCreateRequest;
import ru.bicev.submonitor.dto.service.ServiceDto;
import ru.bicev.submonitor.dto.service.ServiceUpdateRequest;
import ru.bicev.submonitor.entity.Service;
import ru.bicev.submonitor.entity.Subscriber;
import ru.bicev.submonitor.exception.NotFoundException;
import ru.bicev.submonitor.repository.ServiceRepository;

/**
 * Сервис для управления подписочными сервисами
 */
@org.springframework.stereotype.Service
@RequiredArgsConstructor
@Slf4j
public class ServiceService {

    private final ServiceRepository serviceRepository;
    private final SecurityService securityService;

    /**
     * Метод создающий новый сервис в базе данных
     * 
     * @param request запрос с данными для создания сервиса
     * @return дто, содержащее данные созданного сервиса
     */
    @Transactional
    public ServiceDto createService(ServiceCreateRequest request) {
        var sub = getSub();

        Service service = Service.builder()
                .name(request.name())
                .serviceCategory(request.serviceCategory())
                .owner(sub)
                .build();

        var savedService = serviceRepository.save(service);
        log.debug("Service created: {}, id:{}", savedService.getName(), savedService.getId());
        return toDto(savedService);
    }

    /**
     * Метод получающий сервис по его идентификатору
     * 
     * @param serviceId - идентификатор искомого сервиса
     * @return дто, содержащее данные сервиса
     * @throws NotFoundException.class если сервис с таким идентификатором не найден
     *                                 в базе данных
     */
    @Transactional(readOnly = true)
    public ServiceDto getServiceById(Long serviceId) {
        var service = serviceRepository.findByIdAndOwnerIdEqualsOrNull(serviceId, getSubId())
                .orElseThrow(() -> {
                    log.warn("Service not found, id: {}", serviceId);
                    throw new NotFoundException("Service not found with id: " + serviceId);
                });
        log.debug("Service found, id: {}", service.getId());
        return toDto(service);
    }

    /**
     * Метод получающий список всех доступных сервисов
     * 
     * @return список дто содержащий данные всех доступных сервисов
     */
    @Transactional(readOnly = true)
    public List<ServiceDto> getAvailableServices() {
        Long subId = getSubId();
        var services = serviceRepository.findAllAvailable(subId);
        log.debug("Services found for subscriber: {}", subId);
        return services.stream().map(this::toDto).collect(Collectors.toList());
    }

    /**
     * Метод обновляющий данные сервиса
     * 
     * @param request запрос, содержащий идентификатор обновляемого сервиса и данные
     *                для обновления
     * @return дто, содержащее данные обновленного сервиса
     * @throws NotFoundException.class если сервис с таким идентификатором не найден
     *                                 или не принадлежит текущему пользователю
     */
    @Transactional
    public ServiceDto updateService(ServiceUpdateRequest request) {
        Long subId = getSubId();
        var service = serviceRepository.findByIdAndOwnerId(request.id(), subId)
                .orElseThrow(() -> {
                    log.warn("Service: {} was not found or subscriber: {} is not the owner", request.id(), subId);
                    throw new NotFoundException("Service was not found or principal is not the owner");
                });

        if (request.name() != null && request.name().length() >= 2) {
            service.setName(request.name());
        }
        if (request.serviceCategory() != null) {
            service.setServiceCategory(request.serviceCategory());
        }
        log.debug("Service: {} was updated", service.getId());
        return toDto(service);
    }

    /**
     * Метод удаляющий сервис по его идентификатору
     * 
     * @param serviceId идентификатор сервиса, подлежащего удалению
     * @throws NotFoundException.class если сервис с таким идентификатором не найден
     *                                 или не принадлежит текущему пользователю
     */
    @Transactional
    public void deleteService(Long serviceId) {
        var service = serviceRepository.findByIdAndOwnerId(serviceId, getSubId())
                .orElseThrow(() -> {
                    log.warn("Service: {} was not found or doesn't belongs to a subscriber", serviceId);
                    throw new NotFoundException("Service was not found or principal is not the owner");
                });
        log.debug("Service: {} was deleted", service.getId());
        serviceRepository.delete(service);
    }

    /**
     * Служебный метод возвращающий идентификатор текущего пользователя
     * 
     * @return идентификатор текущего пользователя
     */
    private Long getSubId() {
        return securityService.getCurrentUserId();
    }

    /**
     * Служебный метод возвращающий текущего пользователя
     * 
     * @return текущий пользователь
     */
    private Subscriber getSub() {
        return securityService.getCurrentSubscriber();
    }

    /**
     * Служебный метод для маппинга сущности сервиса в дто сервис
     * 
     * @param service сущность, подлежащая преобразованию
     * @return дто, содержащее данные исходной сущности
     */
    private ServiceDto toDto(Service service) {
        return new ServiceDto(service.getId(), service.getName(), service.getServiceCategory().name());
    }

}
