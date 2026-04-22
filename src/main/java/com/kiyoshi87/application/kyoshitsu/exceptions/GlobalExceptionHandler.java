package com.kiyoshi87.application.kyoshitsu.exceptions;

import com.kiyoshi87.application.kyoshitsu.model.ApiResponseEntity;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
@Slf4j
public class GlobalExceptionHandler {

    @ExceptionHandler(ApiException.class)
    public ResponseEntity<ApiResponseEntity<?>> handleApiException(ApiException ex) {
        return ResponseEntity
                .badRequest()
                .body(ApiResponseEntity.error(ex.getMessage()));
    }

    @ExceptionHandler(RuntimeException.class)
    public ResponseEntity<ApiResponseEntity<?>> handleRuntime(RuntimeException ex) {
        if (ex instanceof IllegalArgumentException) {
            return ResponseEntity
                    .badRequest()
                    .body(ApiResponseEntity.error(ex.getMessage()));
        }

        log.error("Unexpected runtime exception", ex);
        return ResponseEntity
                .status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ApiResponseEntity.error("Internal server error"));
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiResponseEntity<?>> handleException(Exception ex) {
        log.error("Unexpected exception", ex);
        return ResponseEntity
                .status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ApiResponseEntity.error("Internal server error"));
    }
}
