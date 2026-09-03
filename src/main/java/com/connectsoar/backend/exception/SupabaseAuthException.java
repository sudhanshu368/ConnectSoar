package com.connectsoar.backend.exception;

import com.connectsoar.backend.enums.ErrorCode;
import org.springframework.http.HttpStatus;

public class SupabaseAuthException extends ApiException {

    public SupabaseAuthException(String message, int statusCode) {
        super(resolveErrorCode(statusCode), message, resolveHttpStatus(statusCode));
    }

    public int getStatusCode() {
        return getHttpStatus().value();
    }

    private static HttpStatus resolveHttpStatus(int statusCode) {
        try {
            return HttpStatus.valueOf(statusCode);
        } catch (Exception e) {
            return HttpStatus.BAD_REQUEST;
        }
    }

    private static ErrorCode resolveErrorCode(int statusCode) {
        return switch (statusCode) {
            case 401 -> ErrorCode.INVALID_CREDENTIALS;
            case 403 -> ErrorCode.FORBIDDEN;
            case 404 -> ErrorCode.USER_NOT_FOUND;
            case 429 -> ErrorCode.RATE_LIMITED;
            case 422 -> ErrorCode.VALIDATION_ERROR;
            default -> ErrorCode.BAD_REQUEST;
        };
    }
}
