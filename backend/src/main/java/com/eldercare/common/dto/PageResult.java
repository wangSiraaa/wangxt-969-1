package com.eldercare.common.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PageResult<T> implements Serializable {

    private List<T> content;
    private long totalElements;
    private int totalPages;
    private int page;
    private int size;

    public static <T> PageResult<T> of(List<T> content, long totalElements, int page, int size) {
        int totalPages = size > 0 ? (int) Math.ceil((double) totalElements / size) : 0;
        return new PageResult<>(content, totalElements, totalPages, page, size);
    }
}
