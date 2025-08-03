package com.dev.news.newsportal.exception;

import java.util.HashMap;
import java.util.Map;

public class ValidationException extends RuntimeException {
    
    private final Map<String, String> errors;
    
    public ValidationException(String message) {
        super(message);
        this.errors = new HashMap<>();
    }
    
    public ValidationException(String message, Map<String, String> errors) {
        super(message);
        this.errors = errors;
    }
    
    public ValidationException(String field, String message) {
        super("Validation failed for field: " + field);
        this.errors = new HashMap<>();
        this.errors.put(field, message);
    }
    
    public Map<String, String> getErrors() {
        return errors;
    }
    
    public void addError(String field, String message) {
        this.errors.put(field, message);
    }
}