package ru.bicev.submonitor.dto.service;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import ru.bicev.submonitor.entity.enums.ServiceCategory;

@Schema(description = "Запрос на создание сервиса")
public record ServiceCreateRequest(
                @Schema(description = "Название сервиса", example = "Pump up Fitness") @NotBlank @Size(min = 2, max = 20, message = "Service name length must be from 2 to 20") String name,
                @Schema(description = "Категория сервиса", example = "EDUCATION") @NotNull(message = "Category must be present") ServiceCategory serviceCategory) {

}
