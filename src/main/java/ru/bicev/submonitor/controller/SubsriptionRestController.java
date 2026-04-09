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
import ru.bicev.submonitor.dto.subscription.SubCreationRequest;
import ru.bicev.submonitor.dto.subscription.SubUpdateRequest;
import ru.bicev.submonitor.dto.subscription.SubscriptionDto;
import ru.bicev.submonitor.service.SubscriptionService;

/**
 * Контроллер для управления подписками пользователя
 */
@RestController
@RequestMapping("/api/v1/subscriptions")
@Slf4j
@RequiredArgsConstructor
@Tag(name = "Подписки", description = "Управление подписками")
public class SubsriptionRestController {

        private final SubscriptionService subscriptionService;

        /**
         * Метод создающий новую подписку в соответствии с запросом
         * 
         * @param request запрос, содержащий данные для создания подписки
         * @return ответ содержащий дто с данными созданной подписки
         */
        @Operation(summary = "Создание подписки", description = "Позволяет создать новую подписку")
        @ApiResponses(value = {
                        @ApiResponse(responseCode = "201", description = "Подписка успешно создана"),
                        @ApiResponse(responseCode = "400", description = "Запрос содержит недопустимые данные", content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
                        @ApiResponse(responseCode = "403", description = "Пользователь не авторизован", content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
                        @ApiResponse(responseCode = "404", description = "Сервис с указанным в запросе идентификатором не найден", content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
        })
        @PostMapping
        public ResponseEntity<SubscriptionDto> createSubscription(@Valid @RequestBody SubCreationRequest request) {
                var created = subscriptionService.createSubscription(request);
                return ResponseEntity.status(HttpStatus.CREATED).body(created);
        }

        /**
         * Метод получает подписку по ее идентификатору
         * 
         * @param subscriptionId идентификатор искомой подписки
         * @return ответ, содержащий дто найденной подписки
         */
        @Operation(summary = "Получение подписки", description = "Позволяет получить подписку по идентификатору")
        @ApiResponses(value = {
                        @ApiResponse(responseCode = "200", description = "Подписка найдена"),
                        @ApiResponse(responseCode = "403", description = "Пользователь не авторизован", content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
                        @ApiResponse(responseCode = "404", description = "Подписка с указанным идентификатором не найдена", content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
        })
        @GetMapping("/{subscriptionId}")
        public ResponseEntity<SubscriptionDto> getSubscriptionById(
                        @Parameter(description = "Идентификатор подписки", example = "102") @PathVariable Long subscriptionId) {
                var subscription = subscriptionService.getSubscriptionById(subscriptionId);
                return ResponseEntity.status(HttpStatus.OK).body(subscription);
        }

        /**
         * Метод получает все подписки текущего пользователя
         * 
         * @return ответ содержащий список дто с данными всех подписок пользователя
         */
        @Operation(summary = "Получение подписок", description = "Позволяет получить список подписку пользователя")
        @ApiResponses(value = {
                        @ApiResponse(responseCode = "200", description = "Подписки найдены"),
                        @ApiResponse(responseCode = "403", description = "Пользователь не авторизован", content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
        })
        @GetMapping
        public ResponseEntity<List<SubscriptionDto>> getAllSubscriptions() {
                var subscriptions = subscriptionService.getAllForCurrentSub();
                return ResponseEntity.status(HttpStatus.OK).body(subscriptions);
        }

        /**
         * Метод обновляющий подписку по ее идентификатору
         * 
         * @param subscriptionId идентификатор подписки для обновления
         * @param request        запрос содержащий данные для изменения
         * @return ответ с дто, сожержащим данные измененной подписки
         */
        @Operation(summary = "Изменение подписки", description = "Позволяет изменить подписку по идентификатору")
        @ApiResponses(value = {
                        @ApiResponse(responseCode = "200", description = "Подписка изменена"),
                        @ApiResponse(responseCode = "403", description = "Пользователь не авторизован", content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
                        @ApiResponse(responseCode = "404", description = "Подписка с указанным идентификатором не найдена", content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
        })
        @PatchMapping("/{subscriptionId}")
        public ResponseEntity<SubscriptionDto> updateSubscription(
                        @Parameter(description = "Идентификатор подписки", example = "102") @PathVariable Long subscriptionId,
                        @RequestBody SubUpdateRequest request) {
                var updated = subscriptionService.updateSubscription(subscriptionId, request);
                return ResponseEntity.status(HttpStatus.OK).body(updated);

        }

        /**
         * Метод устанавливающий подписку как удаленную
         * 
         * @param subscriptionId идентификатор подписки для удаления
         * @return ответ со статусом 204 No Content
         */
        @Operation(summary = "Удаление подписки", description = "Позволяет удалить подписку по идентификатору")
        @ApiResponses(value = {
                        @ApiResponse(responseCode = "200", description = "Подписка удалена"),
                        @ApiResponse(responseCode = "403", description = "Пользователь не авторизован", content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
                        @ApiResponse(responseCode = "404", description = "Подписка с указанным идентификатором не найдена", content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
        })
        @DeleteMapping("/{subscriptionId}")
        public ResponseEntity<Void> deleteSubscription(
                        @Parameter(description = "Идентификатор подписки", example = "102") @PathVariable Long subscriptionId) {
                subscriptionService.deleteSubscription(subscriptionId);
                return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
        }

}
