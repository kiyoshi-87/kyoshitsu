package com.kiyoshi87.application.kyoshitsu.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ApiResponseEntity<T> {

    private static final String DEFAULT_ERROR_MESSAGE = "Unexpected error";

    private boolean success;
    private T data;
    private List<String> error;

    public static <T> ApiResponseEntity<T> success(T data) {
        return ApiResponseEntity.<T>builder()
                .success(true)
                .data(data)
                .build();
    }

    public static ApiResponseEntity<?> error(String message) {
        return ApiResponseEntity.builder()
                .success(false)
                .error(List.of(message == null ? DEFAULT_ERROR_MESSAGE : message))
                .build();
    }

    public static ApiResponseEntity<?> error(List<String> messages) {
        return ApiResponseEntity.builder()
                .success(false)
                .error(messages == null ? List.of(DEFAULT_ERROR_MESSAGE) : messages)
                .build();
    }
}
