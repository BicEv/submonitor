package ru.bicev.submonitor.dto.service;

import io.swagger.v3.oas.annotations.media.Schema;
import ru.bicev.submonitor.entity.enums.ServiceCategory;

@Schema(description = "Запрос на обновление сервиса, указываются только поля, которые нужно изменить")
public record ServiceUpdateRequest(
        @Schema(description = "Название сервиса", example = "Pump up fitness") String name,
        @Schema(description = "Категория сервиса", example = "EDUCATION") ServiceCategory serviceCategory) {

}
