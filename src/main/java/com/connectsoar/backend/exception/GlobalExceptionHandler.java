package com.connectsoar.backend.exception;

import com.connectsoar.backend.dto.ApiError;
import com.connectsoar.backend.dto.ApiResponse;
import com.connectsoar.backend.enums.ErrorCode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.servlet.resource.NoResourceFoundException;

import java.util.HashMap;
import java.util.Map;

@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    @ExceptionHandler(ApiException.class)
    public ResponseEntity<ApiResponse<Object>> handleApiException(ApiException ex) {
        log.warn("API Exception [{}]: {}", ex.getErrorCode(), ex.getMessage());

        ApiResponse<Object> response = ApiResponse.builder()
                .success(false)
                .error(ApiError.builder()
                        .code(ex.getErrorCode().name())
                        .message(ex.getMessage())
                        .build())
                .data(ex.getData())
                .build();

        return new ResponseEntity<>(response, ex.getHttpStatus());
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiResponse<Object>> handleValidationException(MethodArgumentNotValidException ex) {
        Map<String, String> errors = new HashMap<>();
        for (FieldError fieldError : ex.getBindingResult().getFieldErrors()) {
            errors.put(fieldError.getField(), fieldError.getDefaultMessage());
        }

        log.warn("Validation failed: {}", errors);

        ApiResponse<Object> response = ApiResponse.builder()
                .success(false)
                .error(ApiError.builder()
                        .code(ErrorCode.VALIDATION_ERROR.name())
                        .message("Validation failed for one or more fields.")
                        .details(errors)
                        .build())
                .build();

        return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(MissingServletRequestParameterException.class)
    public ResponseEntity<ApiResponse<Object>> handleMissingParam(MissingServletRequestParameterException ex) {
        ApiResponse<Object> response = ApiResponse.builder()
                .success(false)
                .error(ApiError.builder()
                        .code(ErrorCode.BAD_REQUEST.name())
                        .message("Required parameter '" + ex.getParameterName() + "' is missing.")
                        .build())
                .build();

        return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(NoResourceFoundException.class)
    public ResponseEntity<ApiResponse<Object>> handleNotFound(NoResourceFoundException ex) {
        ApiResponse<Object> response = ApiResponse.builder()
                .success(false)
                .error(ApiError.builder()
                        .code(ErrorCode.RESOURCE_NOT_FOUND.name())
                        .message("Endpoint not found: " + ex.getResourcePath())
                        .build())
                .build();

        return new ResponseEntity<>(response, HttpStatus.NOT_FOUND);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiResponse<Object>> handleGeneralException(Exception ex) {
        log.error("Unhandled server exception: {}", ex.getMessage(), ex);

        ApiResponse<Object> response = ApiResponse.builder()
                .success(false)
                .error(ApiError.builder()
                        .code(ErrorCode.INTERNAL_SERVER_ERROR.name())
                        .message("An unexpected internal error occurred. Please try again later.")
                        .build())
                .build();

        return new ResponseEntity<>(response, HttpStatus.INTERNAL_SERVER_ERROR);
    }
}
