package ru.bicev.submonitor.controller;

import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
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
import ru.bicev.submonitor.dto.PagedResponse;
import ru.bicev.submonitor.dto.error.ErrorResponse;
import ru.bicev.submonitor.dto.payment.PaymentCreateLogRequest;
import ru.bicev.submonitor.dto.payment.PaymentLogDto;
import ru.bicev.submonitor.dto.payment.PaymentUpdateLogRequest;
import ru.bicev.submonitor.service.PaymentLogService;

/**
 * Контроллер для ручного управления платежными логами пользователя
 */
@RestController
@RequestMapping("/api/v1/logs")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Платежные логи", description = "Управление платежными логами подписок")
public class PaymentLogRestController {

        private final PaymentLogService paymentLogService;

        /**
         * Метод создающий новый лог в соответствии с заданным запросом
         * 
         * @param request запрос, содержащий данные для создания лога
         * @return ответ, содержащий данные созданного лога
         */
        @Operation(summary = "Создание платежного лога", description = "Позволяет создать новый платежный лог")
        @ApiResponses(value = {
                        @ApiResponse(responseCode = "201", description = "Лог успешно создан"),
                        @ApiResponse(responseCode = "400", description = "Запрос содержит недопустимые данные"),
                        @ApiResponse(responseCode = "403", description = "Пользователь не авторизован"),
                        @ApiResponse(responseCode = "404", description = "Подписка указанная в логе не найдена")
        })
        @PostMapping
        public ResponseEntity<PaymentLogDto> createPaymentLog(
                        @Valid @RequestBody PaymentCreateLogRequest request) {
                var created = paymentLogService.createPaymentLog(request);
                return ResponseEntity.status(HttpStatus.CREATED).body(created);
        }

        /**
         * Метод получающий лог по его идентификатору
         * 
         * @param logId идентификатор искомого лога
         * @return ответ, содержащий данные созданного лога
         */
        @Operation(summary = "Получение платежного лога", description = "Позволяет получить платежный лог по идентификатору")
        @ApiResponses(value = {
                        @ApiResponse(responseCode = "200", description = "Лог найден"),
                        @ApiResponse(responseCode = "403", description = "Пользователь не авторизован", content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
                        @ApiResponse(responseCode = "404", description = "Лог с указанным идентификатором не найден", content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
        })
        @GetMapping("/{logId}")
        public ResponseEntity<PaymentLogDto> getLogById(
                        @Parameter(description = "Идентификатор изменяемого лога", example = "102") @PathVariable Long logId) {
                var log = paymentLogService.getPaymentLogById(logId);
                return ResponseEntity.status(HttpStatus.OK).body(log);
        }

        /**
         * Метод получающий платежные логи постранично
         * 
         * @param pageable параметры постраничного запроса
         * @return ответ, содержащий параметры постраничного запроса и список платежных
         *         логов для указанных страниц
         */
        @Operation(summary = "Постраничное получение платежных логов", description = "Позволяет постранично получить платежные логи")
        @ApiResponses(value = {
                        @ApiResponse(responseCode = "200", description = "Страница возвращена"),
                        @ApiResponse(responseCode = "403", description = "Пользователь не авторизован", content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
        })
        @GetMapping
        public ResponseEntity<PagedResponse<PaymentLogDto>> getPagedLogs(
                        @PageableDefault(size = 10, sort = "paymentDate", direction = Sort.Direction.ASC) Pageable pageable) {
                var pagedResponse = paymentLogService.getPaymentsForUser(pageable);
                return ResponseEntity.status(HttpStatus.OK).body(pagedResponse);
        }

        /**
         * Метод, обновляющий платежный лог в соответствии с данными, указанными в
         * запросе
         * 
         * @param logId   идентификатор обновляемого платежного лога
         * @param request запрос, соедржащий данные для обновления
         * @return ответ, содержащий данные обновленного платежного лога
         */
        @Operation(summary = "Изменение платежного лога", description = "Позволяет изменить платежный лог по идентификатору")
        @ApiResponses(value = {
                        @ApiResponse(responseCode = "200", description = "Лог изменен"),
                        @ApiResponse(responseCode = "403", description = "Пользователь не авторизован", content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
                        @ApiResponse(responseCode = "404", description = "Лог с указанным идентификатором не найден", content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
        })
        @PatchMapping("/{logId}")
        public ResponseEntity<PaymentLogDto> updateLog(
                        @Parameter(description = "Идентификатор изменяемого лога", example = "102") @PathVariable Long logId,
                        @RequestBody PaymentUpdateLogRequest request) {
                var updated = paymentLogService.updatePaymentLog(logId, request);
                return ResponseEntity.status(HttpStatus.OK).body(updated);
        }

        /**
         * Метод удаляющий платежный лог по указанному идентификатору
         * 
         * @param logId идентификатор лога для удаления
         * @return пустой ответ со статусом 204 No Content
         */
        @Operation(summary = "Удаление платежного лога", description = "Позволяет удалить платежный лог по идентификатору")
        @ApiResponses(value = {
                        @ApiResponse(responseCode = "204", description = "Лог удален"),
                        @ApiResponse(responseCode = "403", description = "Пользователь не авторизован", content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
                        @ApiResponse(responseCode = "404", description = "Лог с указанным идентификатором не найден", content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
        })
        @DeleteMapping("/{logId}")
        public ResponseEntity<Void> deleteLog(
                        @Parameter(description = "Идентификатор изменяемого лога", example = "102") @PathVariable Long logId) {
                paymentLogService.deletePaymentLog(logId);
                return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
        }
}
