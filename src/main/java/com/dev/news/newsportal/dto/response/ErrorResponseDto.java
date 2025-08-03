package com.dev.news.newsportal.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

/**
 * Standard error response DTO for API error responses.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ErrorResponseDto {
    
    private int status;
    private String message;
    private LocalDateTime timestamp;
    
    @Builder.Default
    private Map<String, String> errors = new HashMap<>();
    
    /**
     * Creates a new ErrorResponseDto with the current timestamp.
     * 
     * @param status HTTP status code
     * @param message Error message
     * @return ErrorResponseDto instance
     */
    public static ErrorResponseDto of(int status, String message) {
        return ErrorResponseDto.builder()
                .status(status)
                .message(message)
                .timestamp(LocalDateTime.now())
                .build();
    }
    
    /**
     * Creates a new ErrorResponseDto with the current timestamp and field errors.
     * 
     * @param status HTTP status code
     * @param message Error message
     * @param errors Map of field errors
     * @return ErrorResponseDto instance
     */
    public static ErrorResponseDto of(int status, String message, Map<String, String> errors) {
        return ErrorResponseDto.builder()
                .status(status)
                .message(message)
                .timestamp(LocalDateTime.now())
                .errors(errors)
                .build();
    }
    
    /**
     * Adds a field error to the errors map.
     * 
     * @param field Field name
     * @param error Error message
     * @return this ErrorResponseDto instance for chaining
     */
    public ErrorResponseDto addError(String field, String error) {
        this.errors.put(field, error);
        return this;
    }
}