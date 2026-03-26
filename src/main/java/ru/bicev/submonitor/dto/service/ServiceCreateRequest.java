package ru.bicev.submonitor.dto.service;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import ru.bicev.submonitor.entity.enums.ServiceCategory;

public record ServiceCreateRequest(
        @NotBlank @Size(min = 2, max = 20, message = "Service name length must be from 2 to 20") String name,
        @NotNull(message = "Category must be present") ServiceCategory serviceCategory) {

}
