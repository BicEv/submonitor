package ru.bicev.submonitor.dto.service;

import ru.bicev.submonitor.entity.enums.ServiceCategory;

public record ServiceUpdateRequest(String name,
                ServiceCategory serviceCategory) {

}
