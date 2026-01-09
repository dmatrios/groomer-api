package com.groomerapp.api.shared.web;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.util.List;

@Getter
@Builder
@AllArgsConstructor
public class ApiResponse<T> {

    private final T data;
    private final Meta meta;
    private final List<String> warnings;

    // ✅ Nuevo: error cuando falla
    private final ApiError error;

    /* =======================
       OK helpers
    ======================= */

    public static <T> ApiResponse<T> ok(T data) {
        return ApiResponse.<T>builder()
                .data(data)
                .build();
    }

    public static <T> ApiResponse<T> ok(T data, List<String> warnings) {
        return ApiResponse.<T>builder()
                .data(data)
                .warnings(warnings)
                .build();
    }

    public static <T> ApiResponse<T> ok(T data, Meta meta) {
        return ApiResponse.<T>builder()
                .data(data)
                .meta(meta)
                .build();
    }

    /* =======================
       ERROR helpers
    ======================= */

    public static <T> ApiResponse<T> error(String message, String code) {
        return ApiResponse.<T>builder()
                .error(ApiError.builder()
                        .message(message)
                        .code(code)
                        .build())
                .build();
    }

    public static <T> ApiResponse<T> error(String message, String code, List<String> details) {
        return ApiResponse.<T>builder()
                .error(ApiError.builder()
                        .message(message)
                        .code(code)
                        .details(details)
                        .build())
                .build();
    }

    /* =======================
       Inner DTOs
    ======================= */

    @Getter
    @Builder
    @AllArgsConstructor
    public static class Meta {
        private final Integer page;
        private final Integer size;
        private final Long totalElements;
        private final Integer totalPages;
        private final String sort;
    }

    @Getter
    @Builder
    @AllArgsConstructor
    public static class ApiError {
        private final String code;
        private final String message;
        private final List<String> details;
    }
}
