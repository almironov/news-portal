package com.dev.news.newsportal.exception;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpStatus;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.Map;

/**
 * Utility class for building error responses that are compatible with OpenAPI generated ErrorResponse classes.
 * This class provides a unified way to create error responses across all APIs.
 */
public class ErrorResponseBuilder {

    /**
     * Creates a News API ErrorResponse.
     */
    public static com.dev.news.newsportal.api.model.news.ErrorResponse createNewsErrorResponse(
            HttpStatus status, String message) {
        return new com.dev.news.newsportal.api.model.news.ErrorResponse()
                .timestamp(getCurrentTimestamp())
                .status(status.value())
                .error(status.getReasonPhrase())
                .message(message)
                .path(getCurrentPath());
    }

    /**
     * Creates a Users API ErrorResponse.
     */
    public static com.dev.news.newsportal.api.model.users.ErrorResponse createUsersErrorResponse(
            HttpStatus status, String message) {
        return new com.dev.news.newsportal.api.model.users.ErrorResponse()
                .timestamp(getCurrentTimestamp())
                .status(status.value())
                .error(status.getReasonPhrase())
                .message(message)
                .path(getCurrentPath());
    }

    /**
     * Creates a Comments API ErrorResponse.
     */
    public static com.dev.news.newsportal.api.model.comments.ErrorResponse createCommentsErrorResponse(
            HttpStatus status, String message) {
        return new com.dev.news.newsportal.api.model.comments.ErrorResponse()
                .timestamp(getCurrentTimestamp())
                .status(status.value())
                .error(status.getReasonPhrase())
                .message(message)
                .path(getCurrentPath());
    }

    /**
     * Creates a validation error message from field errors.
     */
    public static String createValidationMessage(Map<String, String> fieldErrors) {
        if (fieldErrors == null || fieldErrors.isEmpty()) {
            return "Validation failed";
        }

        StringBuilder message = new StringBuilder("Validation failed: ");
        fieldErrors.forEach((field, error) ->
                message.append(field).append(" - ").append(error).append("; "));

        // Remove the last "; "
        if (message.length() > 2) {
            message.setLength(message.length() - 2);
        }

        return message.toString();
    }

    /**
     * Gets the current timestamp as OffsetDateTime.
     */
    private static OffsetDateTime getCurrentTimestamp() {
        return LocalDateTime.now().atOffset(ZoneOffset.UTC);
    }

    /**
     * Gets the current request path.
     */
    private static String getCurrentPath() {
        try {
            ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
            if (attributes != null) {
                HttpServletRequest request = attributes.getRequest();
                return request.getRequestURI();
            }
        } catch (Exception e) {
            // Ignore and return default
        }
        return "/api";
    }
}