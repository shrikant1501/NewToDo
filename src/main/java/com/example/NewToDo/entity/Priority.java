package com.example.NewToDo.entity;

/**
 * Priority levels for todos
 * 
 * WHY WE NEED THIS:
 * - Instead of storing "high", "medium", "low" as strings (which can have typos)
 * - Enum ensures only valid values can be used
 * - Type-safe: Compiler catches errors
 * - Better performance: Stored as numbers in database
 * 
 * PROBLEM IT SOLVES:
 * Without enum: todo.setPriority("hihg"); // Typo! No error until runtime
 * With enum: todo.setPriority(Priority.HIGH); // Compiler checks it!
 */
public enum Priority {
    
    /**
     * Low priority - Can be done later
     * Example: "Organize desk", "Read article"
     */
    LOW,
    
    /**
     * Medium priority - Should be done soon
     * Example: "Reply to email", "Update documentation"
     */
    MEDIUM,
    
    /**
     * High priority - Urgent, do first
     * Example: "Fix critical bug", "Submit report"
     */
    HIGH;
    
    /**
     * Get display name for UI
     * Example: Priority.HIGH.getDisplayName() returns "High"
     */
    public String getDisplayName() {
        return this.name().charAt(0) + this.name().substring(1).toLowerCase();
    }
}

// Made with Bob
