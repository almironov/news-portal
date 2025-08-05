package com.dev.news.newsportal.exception;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

/**
 * Global exception handler for the application.
 * Provides consistent error responses for different types of exceptions.
 */
@RestControllerAdvice
public class GlobalExceptionHandler {

    /**
     * Handles ResourceNotFoundException and returns a 404 Not Found response.
     *
     * @param ex the exception
     * @return ResponseEntity with error details
     */
    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<Object> handleResourceNotFoundException(ResourceNotFoundException ex) {
        String requestPath = getRequestPath();
        Object errorResponse = createErrorResponseForPath(requestPath, HttpStatus.NOT_FOUND, ex.getMessage());
        return new ResponseEntity<>(errorResponse, HttpStatus.NOT_FOUND);
    }

    /**
     * Handles DuplicateResourceException and returns a 409 Conflict response.
     *
     * @param ex the exception
     * @return ResponseEntity with error details
     */
    @ExceptionHandler(DuplicateResourceException.class)
    public ResponseEntity<Object> handleDuplicateResourceException(DuplicateResourceException ex) {
        String requestPath = getRequestPath();
        Object errorResponse = createErrorResponseForPath(requestPath, HttpStatus.CONFLICT, ex.getMessage());
        return new ResponseEntity<>(errorResponse, HttpStatus.CONFLICT);
    }

    /**
     * Handles ValidationException and returns a 400 Bad Request response.
     *
     * @param ex the exception
     * @return ResponseEntity with error details
     */
    @ExceptionHandler(ValidationException.class)
    public ResponseEntity<Object> handleValidationException(ValidationException ex) {
        String requestPath = getRequestPath();
        Object errorResponse = createErrorResponseForPath(requestPath, HttpStatus.BAD_REQUEST, ex.getMessage());
        return new ResponseEntity<>(errorResponse, HttpStatus.BAD_REQUEST);
    }

    /**
     * Handles MethodArgumentNotValidException (validation errors from @Valid annotation)
     * and returns a 400 Bad Request response with field-specific error details.
     *
     * @param ex the exception
     * @return ResponseEntity with error details
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Object> handleMethodArgumentNotValidException(MethodArgumentNotValidException ex) {
        String requestPath = getRequestPath();

        // Build validation error message
        StringBuilder errorMessage = new StringBuilder("Validation failed");
        if (!ex.getBindingResult().getAllErrors().isEmpty()) {
            errorMessage.append(": ");
            ex.getBindingResult().getAllErrors().forEach(error -> {
                String fieldName = ((FieldError) error).getField();
                String message = error.getDefaultMessage();
                errorMessage.append(fieldName).append(" ").append(message).append("; ");
            });
        }

        Object errorResponse = createErrorResponseForPath(requestPath, HttpStatus.BAD_REQUEST, errorMessage.toString());
        return new ResponseEntity<>(errorResponse, HttpStatus.BAD_REQUEST);
    }

    /**
     * Handles HttpMessageNotReadableException (invalid JSON, etc.)
     * and returns a 400 Bad Request response.
     *
     * @param ex the exception
     * @return ResponseEntity with error details
     */
    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<Object> handleHttpMessageNotReadableException(HttpMessageNotReadableException ex) {
        String requestPath = getRequestPath();
        Object errorResponse = createErrorResponseForPath(requestPath, HttpStatus.BAD_REQUEST,
                "Invalid request body: " + ex.getMessage());
        return new ResponseEntity<>(errorResponse, HttpStatus.BAD_REQUEST);
    }

    /**
     * Handles all other exceptions and returns a 500 Internal Server Error response.
     *
     * @param ex the exception
     * @return ResponseEntity with error details
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<Object> handleException(Exception ex) {
        String requestPath = getRequestPath();
        Object errorResponse = createErrorResponseForPath(requestPath, HttpStatus.INTERNAL_SERVER_ERROR,
                "An unexpected error occurred: " + ex.getMessage());
        return new ResponseEntity<>(errorResponse, HttpStatus.INTERNAL_SERVER_ERROR);
    }

    /**
     * Gets the current request path from the request context.
     *
     * @return the request path or empty string if not available
     */
    private String getRequestPath() {
        try {
            ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.currentRequestAttributes();
            HttpServletRequest request = attributes.getRequest();
            return request.getRequestURI();
        } catch (Exception e) {
            return "";
        }
    }

    /**
     * Creates the appropriate error response based on the request path.
     *
     * @param requestPath the request path
     * @param status      HTTP status
     * @param message     error message
     * @return the appropriate error response object
     */
    private Object createErrorResponseForPath(String requestPath, HttpStatus status, String message) {
        if (requestPath.contains("/api/v1/news")) {
            return ErrorResponseBuilder.createNewsErrorResponse(status, message);
        } else if (requestPath.contains("/api/v1/users")) {
            return ErrorResponseBuilder.createUsersErrorResponse(status, message);
        } else if (requestPath.contains("/api/v1/comments")) {
            return ErrorResponseBuilder.createCommentsErrorResponse(status, message);
        } else {
            // Default to news error response for unknown paths
            return ErrorResponseBuilder.createNewsErrorResponse(status, message);
        }
    }
}