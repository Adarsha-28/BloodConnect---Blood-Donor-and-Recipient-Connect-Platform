package com.blooddonation.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PaginatedResponse<T> {
    private boolean success;
    private List<T> data;
    private int currentPage;
    private int totalPages;
    private long totalElements;

    public static <T> PaginatedResponse<T> of(List<T> data, int currentPage, int totalPages, long totalElements) {
        return PaginatedResponse.<T>builder()
                .success(true)
                .data(data)
                .currentPage(currentPage)
                .totalPages(totalPages)
                .totalElements(totalElements)
                .build();
    }
}
