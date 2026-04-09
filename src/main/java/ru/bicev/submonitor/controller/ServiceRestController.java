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

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import ru.bicev.submonitor.dto.error.ErrorResponse;
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
@Tag(name = "Подписочные сервисы", description = "Управление сервисами подписок")
public class ServiceRestController {

        private final ServiceService serviceService;

        /**
         * Создает новый сервис для пользователя в соответствии с запросом
         * 
         * @param request запрос на создание пользователя
         * @return дто, содержащее данные созданного сервиса
         */
        @Operation(summary = "Создание сервиса", description = "Позволяет создать новый подписочный сервис")
        @ApiResponses(value = {
                        @ApiResponse(responseCode = "201", description = "Сервис успешно создан"),
                        @ApiResponse(responseCode = "400", description = "Запрос содержит недопустимые данные", content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
                        @ApiResponse(responseCode = "403", description = "Пользователь не авторизован", content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
        })
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
        @Operation(summary = "Получение сервиса", description = "Позволяет получить подписочный сервис по идентификатору")
        @ApiResponses(value = {
                        @ApiResponse(responseCode = "200", description = "Сервис найден"),
                        @ApiResponse(responseCode = "403", description = "Пользователь не авторизован", content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
                        @ApiResponse(responseCode = "404", description = "Сервис не найден", content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
        })
        @GetMapping("/{serviceId}")
        public ResponseEntity<ServiceDto> getServiceById(
                        @Parameter(description = "Идентификатор сервиса", example = "102") @PathVariable Long serviceId) {
                var service = serviceService.getServiceById(serviceId);
                return ResponseEntity.status(HttpStatus.OK).body(service);
        }

        /**
         * Получает список всех доступных для пользователя сервисов
         * 
         * @return список всех доступных для пользователя сервисов
         */
        @Operation(summary = "Получение сервисов", description = "Позволяет получить достепные пользователю подписочные сервисы")
        @ApiResponses(value = {
                        @ApiResponse(responseCode = "200", description = "Сервисы найдены"),
                        @ApiResponse(responseCode = "403", description = "Пользователь не авторизован", content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
        })
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
        @Operation(summary = "Изменение сервиса", description = "Позволяет изменить подписочный сервис")
        @ApiResponses(value = {
                        @ApiResponse(responseCode = "200", description = "Сервис успешно изменен"),
                        @ApiResponse(responseCode = "403", description = "Пользователь не авторизован", content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
                        @ApiResponse(responseCode = "404", description = "Сервис не найден", content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
        })
        @PatchMapping("/{serviceId}")
        public ResponseEntity<ServiceDto> updateService(
                        @Parameter(description = "Идентификатор сервиса", example = "102") @PathVariable Long serviceId,
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
        @Operation(summary = "Удаление сервиса", description = "Позволяет удалить подписочный сервис")
        @ApiResponses(value = {
                        @ApiResponse(responseCode = "204", description = "Сервис успешно удален"),
                        @ApiResponse(responseCode = "403", description = "Пользователь не авторизован", content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
                        @ApiResponse(responseCode = "404", description = "Сервис не найден", content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
        })
        @DeleteMapping("/{serviceId}")
        public ResponseEntity<Void> deleteService(
                        @Parameter(description = "Идентификатор сервиса", example = "102") @PathVariable Long serviceId) {
                serviceService.deleteService(serviceId);
                return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
        }

}
