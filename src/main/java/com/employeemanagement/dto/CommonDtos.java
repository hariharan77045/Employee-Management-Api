package com.employeemanagement.dto;

import java.util.List;

import org.springframework.data.domain.Page;

/** Small shared response shapes. */
public final class CommonDtos {

    private CommonDtos() {
    }

    /** Id/name pair for drop-downs; {@code detail} carries extra info such as a grade or location. */
    public record Lookup(Integer id, String name, String detail) {
    }

    /** Stable JSON shape for paged results (Spring's Page is not meant to be serialized directly). */
    public record PageResponse<T>(List<T> content, int page, int size, long totalElements, int totalPages) {

        public static <T> PageResponse<T> of(Page<T> page) {
            return new PageResponse<>(page.getContent(), page.getNumber(), page.getSize(),
                    page.getTotalElements(), page.getTotalPages());
        }
    }
}
