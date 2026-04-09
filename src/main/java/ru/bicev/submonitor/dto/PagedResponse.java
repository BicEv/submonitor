package ru.bicev.submonitor.dto;

import java.util.List;

import org.springframework.data.domain.Page;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Ответ содержащий постраничные данные")
public record PagedResponse<T>(
        @Schema(description = "Список данных") List<T> content,
        @Schema(description = "Страница ответа", example = "2") int page,
        @Schema(description = "Размер страницы", example = "10") int size,
        @Schema(description = "Общее количество элементов", example = "15") long totalElements,
        @Schema(description = "Общее количество страниц", example = "7") int totalPages,
        @Schema(description = "Является ли страница первой", example = "false") boolean isFirst,
        @Schema(description = "Является ли страницы последней", example = "true") boolean last) {

    public static <T> PagedResponse<T> toPagedResponse(Page<T> page) {
        return new PagedResponse<>(page.getContent(), page.getNumber(), page.getSize(), page.getTotalElements(),
                page.getTotalPages(), page.isFirst(), page.isLast());
    }

}
