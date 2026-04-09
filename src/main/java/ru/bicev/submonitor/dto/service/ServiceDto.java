package ru.bicev.submonitor.dto.service;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Ответ с данными сервиса")
public record ServiceDto(
        @Schema(description = "Идентификатор сервиса", example = "105") Long id,
        @Schema(description = "Название сервиса", example = "Pump up chess club") String name,
        @Schema(description = "Категория сервиса", example = "OTHER") String serviceCategory) {

}
