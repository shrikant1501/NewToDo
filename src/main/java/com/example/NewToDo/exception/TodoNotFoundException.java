package com.example.NewToDo.exception;

/**
 * Custom exception thrown when a Todo is not found by ID
 * This is a RuntimeException, so it doesn't need to be declared in method signatures
 */
public class TodoNotFoundException extends RuntimeException {
    
    /**
     * Constructor with custom message
     */
    public TodoNotFoundException(String message) {
        super(message);
    }
    
    /**
     * Constructor with ID - creates a standard message
     */
    public TodoNotFoundException(Long id) {
        super("Todo with id " + id + " not found");
    }
    
    /**
     * Constructor with message and cause
     */
    public TodoNotFoundException(String message, Throwable cause) {
        super(message, cause);
    }
}

// Made with Bob
