package com.kiyoshi87.application.kyoshitsu.exceptions;

import com.kiyoshi87.application.kyoshitsu.model.ApiResponseEntity;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(ApiException.class)
    public ResponseEntity<ApiResponseEntity<?>> handleApiException(ApiException ex) {
        return ResponseEntity
                .badRequest()
                .body(ApiResponseEntity.error(ex.getMessage()));
    }

    @ExceptionHandler(RuntimeException.class)
    public ResponseEntity<ApiResponseEntity<?>> handleRuntime(RuntimeException ex) {
        return ResponseEntity.badRequest()
                .body(ApiResponseEntity.error(ex.getMessage()));
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiResponseEntity<?>> handleException(Exception ex) {
        return ResponseEntity.badRequest()
                .body(ApiResponseEntity.error(ex.getMessage()));
    }
}
