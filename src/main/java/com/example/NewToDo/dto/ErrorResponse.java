package com.example.NewToDo.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.Map;

/**
 * Standardized error response format for all API errors
 * This ensures consistent error structure across the application
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ErrorResponse {
    
    /**
     * Timestamp when the error occurred
     */
    private LocalDateTime timestamp;
    
    /**
     * HTTP status code (e.g., 404, 400, 500)
     */
    private int status;
    
    /**
     * Error type/name (e.g., "Not Found", "Bad Request")
     */
    private String error;
    
    /**
     * Detailed error message
     */
    private String message;
    
    /**
     * API path where error occurred
     */
    private String path;
    
    /**
     * Field-level validation errors (for validation failures)
     * Key: field name, Value: error message
     */
    private Map<String, String> errors;
    
    /**
     * Constructor for simple errors (without field-level errors)
     */
    public ErrorResponse(LocalDateTime timestamp, int status, String error, String message, String path) {
        this.timestamp = timestamp;
        this.status = status;
        this.error = error;
        this.message = message;
        this.path = path;
    }
}

// Made with Bob
