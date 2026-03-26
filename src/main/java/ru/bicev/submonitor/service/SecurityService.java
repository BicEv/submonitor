package ru.bicev.submonitor.service;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import ru.bicev.submonitor.entity.Subscriber;
import ru.bicev.submonitor.exception.NotFoundException;
import ru.bicev.submonitor.repository.SubscriberRepository;
import ru.bicev.submonitor.security.UserDetailsImpl;

/**
 * Сервис для получения идентификаторов пользователей или сущностей
 * пользователей
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class SecurityService {

    private final SubscriberRepository subscriberRepository;

    /**
     * Метод возвращающий идентификатор текущего пользователя, получая его из
     * контекста безопасности
     * 
     * @return идентификатор текущего пользователя
     * @throws IllegalStateException.class если контекст не содержит пользователя
     *                                     или его тип не соотвествует типу
     *                                     пользователей в приложении
     */
    public Long getCurrentUserId() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();

        if (auth == null || !(auth.getPrincipal() instanceof UserDetailsImpl)) {
            log.error("Cannot get subscriber id, user not authenticated");
            throw new IllegalStateException("Not authenticated or principal has wrong type");
        }
        UserDetailsImpl subscriber = (UserDetailsImpl) auth.getPrincipal();
        return subscriber.getId();
    }

    /**
     * Метод возвращающий текущего пользователя
     * 
     * @return текущий пользователь
     * @throws NotFoundException.class если пользователь с таким id не найден в базе
     *                                 данных
     */
    public Subscriber getCurrentSubscriber() {
        Long subId = getCurrentUserId();

        var subscriber = subscriberRepository.findById(subId)
                .orElseThrow(() -> {
                    log.error("Subscriber with id: {} is not found", subId);
                    throw new NotFoundException("Subscriber not found");
                });
        return subscriber;
    }

}
