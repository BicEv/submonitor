package ru.bicev.submonitor.dto.service;

import jakarta.validation.constraints.NotNull;
import ru.bicev.submonitor.entity.enums.ServiceCategory;

public record ServiceUpdateRequest(@NotNull(message = "Id cannot be null") Long id, String name,
        ServiceCategory serviceCategory) {

}
